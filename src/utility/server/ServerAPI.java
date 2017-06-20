package utility.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

import utility.buffer.BufferMethods;
import utility.security.PasswordSecurity;

/**
 * Essa classe deve prover uma série de métodos que o cliente pode usar para
 * se comunicar com o servidor
 * @author Pedro Queiroga <psq@cin.ufpe.br>
 *
 */
public class ServerAPI {
	
	private String ip;
	private int port;
	
	public ServerAPI(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}
	
	/**
	 * Método que cliente usa para solicitar cadastro ao servidor
	 * @param username Nome de usuário validado
	 * @param password Senha validada
	 * @return -2 = alguma exception nesse método. -1 = usuario ja online
	 * 0 = falha comum (login invlido, senha, usrname indisponivel)
	 * 1 = sucesso
	 * @throws IOException 
	 * @throws GeneralSecurityException 
	 */
	public boolean cadastro(String username, String password) throws IOException, GeneralSecurityException {
		Socket connectionSocket = null;
		try {
			// se conecta ao servidor de operacoes em contas
			connectionSocket = new Socket(this.ip, this.port);
			OutputStream outToServer = connectionSocket.getOutputStream();
			InputStream inFromServer = connectionSocket.getInputStream();
			
			// cadastro(0), login(1)		
			outToServer.write(0);
			
			int bdOK = inFromServer.read();
			
			if (bdOK == 1) {
				byte[] buffer = new byte[256];
				
				BufferMethods.writeString(username, outToServer);
				
				// recebe o salt, que sempre tera 32 chars
				inFromServer.read(buffer, 0, 32);
				byte[] salt = PasswordSecurity.fromHex(BufferMethods.byteArraytoString(buffer, 32));
				String nicepw = null;
				nicepw = PasswordSecurity.generateStrongPasswordHash(password, salt).split(":")[2];
				
				// envia a senha criptografada
				BufferMethods.toByteArray(buffer, nicepw);
				outToServer.write(buffer, 0, 128);
				// recebe do servidor status do cadastro (sucesso ou falha)
				int codigo = inFromServer.read();
				connectionSocket.close();
				if (codigo == 1) {
					return true; 
				} else {
					return false;
				}
			} else {
				return false;
			}
		} catch (IOException e) {
			throw e;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw e;
		} finally {
			if (connectionSocket != null) {
				if (!connectionSocket.isClosed()) {
					try {
						connectionSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * Método que cliente usa para solicitar login.
	 * @param username Nome de usuario
	 * @param password Senha
	 * @return dois valores, um ServerSocket e um inteiro. O inteiro é um código de status (sucesso ou pq falhou).
	 * Se falhou, retorna uma ServerSocket nula.
	 * @throws GeneralSecurityException 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Map.Entry<ArrayList<ServerSocket>, Integer> login(String username, String password)
			throws GeneralSecurityException, IOException {
		ArrayList<ServerSocket> returnSocket = new ArrayList<ServerSocket>();
		Socket connectionSocket = null;
		int status = -2;
		try {
			// se conecta ao servidor de operações
			connectionSocket = new Socket(this.ip, this.port);
			OutputStream outToServer = connectionSocket.getOutputStream();
			InputStream inFromServer = connectionSocket.getInputStream();
			// cadastro(0), login(1)		
			outToServer.write(1);
			
			int bdOK = inFromServer.read();
			
			if (bdOK == 1) {
				byte[] buffer1 = new byte[256];
				
				BufferMethods.writeString(username, outToServer);
				
				// recebe o salt, que sempre tera 32 chars
				inFromServer.read(buffer1, 0, 32);
				byte[] salt = PasswordSecurity.fromHex(BufferMethods.byteArraytoString(buffer1, 32));
				String nicepw = null;
				nicepw = PasswordSecurity.generateStrongPasswordHash(password, salt).split(":")[2];
				
				// envia a senha criptografada
				BufferMethods.toByteArray(buffer1, nicepw);
				outToServer.write(buffer1, 0, 128);
				// recebe do servidor status do cadastro (sucesso ou falha)
				status = inFromServer.read();
				
				// agora checamos se acertamos as credenciais, se acertamos então podemos criar uma ServerSocket
				// e dar essa porta pro servidor constatar que estamos online e podemos receber por essa porta.
				int portaDaSessao = 2030;
				if (status == 1) {
					while (returnSocket.size() != 6 && portaDaSessao < 65525) {
						ServerSocket temp1 = null;
						ServerSocket temp2 = null;
						ServerSocket temp3 = null;
						ServerSocket temp4 = null;
						ServerSocket temp5 = null;
						ServerSocket temp6 = null;
						try {
							temp1 = new ServerSocket(portaDaSessao);
							returnSocket.add(temp1); // chat
							portaDaSessao++;
							temp2 = new ServerSocket(portaDaSessao);
							returnSocket.add(temp2); // transferencia
							portaDaSessao++;
							temp3 = new ServerSocket(portaDaSessao);
							returnSocket.add(temp3); // rtt
							portaDaSessao++;
							temp4 = new ServerSocket(portaDaSessao);
							returnSocket.add(temp4); // lista de amigos
							portaDaSessao++;
							temp5 = new ServerSocket(portaDaSessao);
							returnSocket.add(temp5); // solicitação de amizade
							portaDaSessao++;
							temp6 = new ServerSocket(2030);
							returnSocket.add(temp6); // ping
						} catch (IOException e) {
							returnSocket.clear();
							if (temp1 != null) {
								temp1.close();
							}
							if (temp2 != null) {
								temp2.close();
							}
							if (temp3 != null) {
								temp3.close();
							}
							if (temp4 != null) {
								temp4.close();
							}
							if (temp5 != null) {
								temp5.close();
							}
							System.out.println(portaDaSessao + " indisponível, tentando com a próxima seq de 6 portas...");
							portaDaSessao++;
						}
					}
					if (!returnSocket.isEmpty()) {
						// diz que conseguiu um servidor
						outToServer.write(1);
						byte[] buffer = new byte[256];
						BufferMethods.toByteArray(buffer, returnSocket.get(0).getLocalPort());
						// envia número de porta
						outToServer.write(buffer, 0, 5);
					} else {
						outToServer.write(0);
					}
				}
			}	
		} catch (IOException e) {
			throw e;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw e;
		} finally {
			if (connectionSocket != null) {
				if (!connectionSocket.isClosed()) {
					try {
						connectionSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return new AbstractMap.SimpleEntry<ArrayList<ServerSocket>, Integer>(returnSocket, new Integer(status));
	}
	
	/**
	 * Tira username da lista de onlines que o servidor tem
	 * @param username Usuário a ser retirado da lista de onlines
	 * @return verdadeiro se conseguiu tirar, falso cc.
	 * @throws IOException 
	 */
	public boolean logout(String username) throws IOException {
		Socket connectionSocket = null;
		try {
			// se conecta ao servidor de operacoes em contas
			connectionSocket = new Socket(this.ip, this.port);
			OutputStream outToServer = connectionSocket.getOutputStream();
			InputStream inFromServer = connectionSocket.getInputStream();
			
			// diz que quer logout
			outToServer.write(2);
			
			// envia o nome de usuario
			BufferMethods.writeString(username, outToServer);
			
			if (inFromServer.read() == 1) {
				return true;
			} else {
				return false;
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (connectionSocket != null) {
				if (!connectionSocket.isClosed()) {
					try {
						connectionSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * Pega lista de usuários onlines que o servidor tem.
	 * TODO pegaAmigosOnlines(String user)
	 * @return um ArrayList<String> que é a lista de usuários online. Cada elemento um <usuário> (<ip>, <porta>)
	 * onde ele poderia ser acessado
	 * @throws IOException 
	 */
	public ArrayList<String> pegaOnlines() throws IOException {
		ArrayList<String> retorno = new ArrayList<String>();
		Socket connectionSocket = null;
		try {
			connectionSocket = new Socket(this.ip, this.port);
			OutputStream outToServer = connectionSocket.getOutputStream();
			InputStream inFromServer = connectionSocket.getInputStream();
			
			outToServer.write(3);
			
			int listSize = inFromServer.read();
			for (int i = 0; i < listSize; i++) {
				retorno.add(BufferMethods.readString(inFromServer));
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (connectionSocket != null) {
				if (!connectionSocket.isClosed()) {
					try {
						connectionSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return retorno;
	}
	
	/**
	 * 
	 * @param user usuário ativo da solicitação
	 * @param password usuário passivo da solicitação
	 * @return
	 * -1 quer dizer que o servidor não conseguiu se conectar ao banco de dados!<br> 
	 * 0 quer dizer que o pedido n foi efetuado ok por alguma razão que NÃO é relação já existente!<br>
	 * 1 quer dizer OK!<br>
	 * 2 quer dizer que já existe uma relação para esses usuários ou que algum dos dois usuários não existe!<br>
	 * @throws IOException 
	 */
	public int solicitaAmizade(String user, String friend) throws IOException {
		Socket connectionSocket = null;
		try {
			connectionSocket = new Socket(this.ip, this.port);
			OutputStream outToServer = connectionSocket.getOutputStream();
			InputStream inFromServer = connectionSocket.getInputStream();
			
			outToServer.write(4); // diz que quer solicitar amizade
			
			int bdOK = inFromServer.read();
			if (bdOK == 1) {
				BufferMethods.writeString(user, outToServer);
				BufferMethods.writeString(friend, outToServer);
				int status = inFromServer.read();
				return status;
			} else {
				return -1;
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (connectionSocket != null) {
				if (!connectionSocket.isClosed()) {
					try {
						connectionSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param user que está aceitando
	 * @param password usuário que solicitou amizade originalmente
	 * @return
	 * -1 quer dizer que o servidor não conseguiu se conectar ao banco de dados!<br> 
	 * 0 quer dizer que a operação não foi concluída por alguma razão que não é falha na conexão com BD!<br>
	 * 1 quer dizer OK!<br>
	 * @throws IOException 
	 */
	public int aceitarAmizade(String user, String friend) throws IOException {
		return aceitaRecusaAmizade(user, friend, 5);
	}
	
	/**
	 * 
	 * @param user usuário que está recusando
	 * @param password usuário que solicitou amizade originalmente
	 * @return
	 * -1 quer dizer que o servidor não conseguiu se conectar ao banco de dados!<br> 
	 * 0 quer dizer que a operação não foi concluída por alguma razão que não é falha na conexão com BD!<br>
	 * 1 quer dizer operação OK!<br>
	 * @throws IOException 
	 */
	public int recusarAmizade(String user, String friend) throws IOException {
		return aceitaRecusaAmizade(user, friend, 6);
	}
	
	/**
	 * Operação simétrica, remove relação então um para de ser amigo do outro.
	 * @param user usuário que está removendo
	 * @param friend usuário que está sendo removido
	 * @return 
	 * -1 quer dizer que o servidor não conseguiu se conectar ao banco de dados!<br> 
	 * 0 quer dizer que a operação não foi concluída por alguma razão que não é falha na conexão com BD!<br>
	 * 1 quer dizer operação OK!<br>
	 * @throws IOException
	 */
	public int removerAmigo(String user, String friend) throws IOException {
		return aceitaRecusaAmizade(user, friend, 7);
	}
	
	/**
	 * pega lista de amigos que estão online
	 * @param user
	 * @return lista de amigos online, <username> (<ip>, <port>)
	 * @throws IOException
	 */
	public ArrayList<String> pegaAmigosOnlines(String user) throws IOException {
		return listaSolicitacaoAmigos(user, 8);
	}
	
	/**
	 * 
	 * @param user
	 * @return lista de solicitacoes pendentes: cada elemento é um username
	 * @throws IOException
	 */
	public ArrayList<String> pegaSolicitacoesPendentes(String user) throws IOException {
		return listaSolicitacaoAmigos(user, 9);
	}
	
	public ArrayList<String> pegaAmigos(String user) throws IOException {
		return listaSolicitacaoAmigos(user, 10);
	}
	
	/**
	 * @param user
	 * @return
	 * @throws IOException
	 */
	private ArrayList<String> listaSolicitacaoAmigos(String user, int op) throws IOException {
		ArrayList<String> retorno = new ArrayList<String>();
		Socket connectionSocket = null;
		try {
			connectionSocket = new Socket(this.ip, this.port);
			OutputStream outToServer = connectionSocket.getOutputStream();
			InputStream inFromServer = connectionSocket.getInputStream();
			
			outToServer.write(op);
			int bdOK = inFromServer.read();
			if (bdOK == 1) {
				BufferMethods.writeString(user, outToServer);
				int listSize = inFromServer.read();
				for (int i = 0; i < listSize; i++) {
					retorno.add(BufferMethods.readString(inFromServer));
				}
			}			
		} catch (IOException e) {
			throw e;
		} finally {
			if (connectionSocket != null) {
				if (!connectionSocket.isClosed()) {
					try {
						connectionSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return retorno;
	}

	/**
	 * @param user
	 * @param friend
	 * @return
	 * @throws IOException
	 */
	private int aceitaRecusaAmizade(String user, String friend, int op) throws IOException {
		Socket connectionSocket = null;
		try {
			connectionSocket = new Socket(this.ip, this.port);
			OutputStream outToServer = connectionSocket.getOutputStream();
			InputStream inFromServer = connectionSocket.getInputStream();
			
			outToServer.write(op); // diz que quer recusar amizade
			
			int bdOK = inFromServer.read();
			if (bdOK == 1) {
				BufferMethods.writeString(user, outToServer);
				BufferMethods.writeString(friend, outToServer);
				return inFromServer.read();
			} else {
				return -1; 
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (connectionSocket != null) {
				if (!connectionSocket.isClosed()) {
					try {
						connectionSocket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
