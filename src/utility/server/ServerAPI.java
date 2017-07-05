package utility.server;

import java.io.IOException;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;

import protocol.DGServerSocket;
import protocol.DGSocket;
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
	private int[] pktsPerdidos;
	private double pDescartaPacotes;
	
	public ServerAPI(double pDescartaPacotes, int[] pktsPerdidos, String ip, int port) {
		this.ip = ip;
		this.port = port;
		this.pktsPerdidos = pktsPerdidos;
		this.pDescartaPacotes = pDescartaPacotes;
	}
	
	/**
	 * Método que cliente usa para solicitar cadastro ao servidor
	 * @param username Nome de usuário validado
	 * @param password Senha validada
	 * @return -1 = erro BD
	 * 0 = falha sem muita razao clara
	 * 1 = sucesso
	 * 2 = username indisponivel
	 * 3 = usrname invalido, senha invalida, nunca deveria dar se o cliente validar
	 * @throws IOException 
	 * @throws GeneralSecurityException 
	 */
	public int cadastro(String username, String password) throws IOException, GeneralSecurityException {
		DGSocket connectionSocket = null;
		try {
			// se conecta ao servidor de operacoes em contas
			connectionSocket = new DGSocket(pDescartaPacotes, pktsPerdidos, this.ip, this.port);
//			OutputStream outToServer = connectionSocket.getOutputStream();
//			InputStream inFromServer = connectionSocket.getInputStream();
			
			// cadastro(0), login(1)		
//			outToServer.write(0);
			BufferMethods.sendInt(0, connectionSocket);
			
//			int bdOK = inFromServer.read();
			int bdOK = BufferMethods.receiveFeedBack(connectionSocket);
			
			if (bdOK == 1) {
				byte[] buffer = new byte[256];
				
				BufferMethods.writeString(username, connectionSocket);
				
				// recebe o salt, que sempre tera 32 chars
//				inFromServer.read(buffer, 0, 32);
				connectionSocket.receive(buffer, 32);
				byte[] salt = PasswordSecurity.fromHex(BufferMethods.byteArraytoString(buffer, 32));
				String nicepw = null;
				nicepw = PasswordSecurity.generateStrongPasswordHash(password, salt).split(":")[2];
				
				// envia a senha criptografada
				BufferMethods.toByteArray(buffer, nicepw);
//				outToServer.write(buffer, 0, 128);
				connectionSocket.send(buffer, 128);
				// recebe do servidor status do cadastro (sucesso ou falha)
//				int codigo = inFromServer.read();
				int codigo = BufferMethods.receiveFeedBack(connectionSocket);
				connectionSocket.close(false);
				return codigo;
			} else {
				return -1;
			}
		} catch (IOException e) {
			throw e;
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw e;
		} finally {
			if (connectionSocket != null) {
				if (!connectionSocket.isClosed()) {
					try {
						connectionSocket.close(false);
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
	 * Se falhou, retorna uma ServerSocket nula.<br>
	 * -1 quer dizer que cliente não conseguiu portas<br>
	 * 0 quer dizer usuário ou senha incorretos<br>
	 * 1 quer dizer OK<br>
	 * 2 quer dizer usuário já está online
	 * @throws GeneralSecurityException 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public Map.Entry<ArrayList<DGServerSocket>, Integer> login(String username, String password)
			throws GeneralSecurityException, IOException {
		ArrayList<DGServerSocket> returnSocket = new ArrayList<DGServerSocket>();
		DGSocket connectionSocket = null;
		int status = -2;
		try {
			// se conecta ao servidor de operações
			connectionSocket = new DGSocket(pDescartaPacotes, pktsPerdidos, this.ip, this.port);
			System.out.println("Consegui uma connectionsocket pra falar com o server");
//			OutputStream outToServer = connectionSocket.getOutputStream();
//			InputStream inFromServer = connectionSocket.getInputStream();
			// cadastro(0), login(1)		
//			outToServer.write(1);
			BufferMethods.sendInt(1, connectionSocket);
			
//			int bdOK = inFromServer.read();
			int bdOK = BufferMethods.receiveFeedBack(connectionSocket);
			
			if (bdOK == 1) {
				byte[] buffer1 = new byte[256];
				
				BufferMethods.writeString(username, connectionSocket);
				
				// recebe o salt, que sempre tera 32 chars
//				inFromServer.read(buffer1, 0, 32);
				connectionSocket.receive(buffer1, 32);
				byte[] salt = PasswordSecurity.fromHex(BufferMethods.byteArraytoString(buffer1, 32));
				String nicepw = null;
				nicepw = PasswordSecurity.generateStrongPasswordHash(password, salt).split(":")[2];
				
				// envia a senha criptografada
				BufferMethods.toByteArray(buffer1, nicepw);
//				outToServer.write(buffer1, 0, 128);
				connectionSocket.send(buffer1, 128);
				// recebe do servidor status do login (sucesso ou falha)
//				status = inFromServer.read();
				status = BufferMethods.receiveFeedBack(connectionSocket);
				
				// agora checamos se acertamos as credenciais, se acertamos então podemos criar uma ServerSocket
				// e dar essa porta pro servidor constatar que estamos online e podemos receber por essa porta.
				int portaDaSessao = 2030;
				if (status == 1) {
					while (returnSocket.size() != 6 && portaDaSessao < 65525) {
						DGServerSocket temp1 = null;
						DGServerSocket temp2 = null;
						DGServerSocket temp3 = null;
						DGServerSocket temp4 = null;
						DGServerSocket temp5 = null;
						DGServerSocket temp6 = null;
						try {
							temp1 = new DGServerSocket(portaDaSessao);
							returnSocket.add(temp1); // chat
							portaDaSessao++;
							temp2 = new DGServerSocket(portaDaSessao);
							returnSocket.add(temp2); // transferencia
							portaDaSessao++;
							temp3 = new DGServerSocket(portaDaSessao);
							returnSocket.add(temp3); // rtt
							portaDaSessao++;
							temp4 = new DGServerSocket(portaDaSessao);
							returnSocket.add(temp4); // lista de amigos
							portaDaSessao++;
							temp5 = new DGServerSocket(portaDaSessao);
							returnSocket.add(temp5); // solicitação de amizade
							portaDaSessao++;
							temp6 = new DGServerSocket(portaDaSessao);
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
//						outToServer.write(1);
						BufferMethods.sendFeedBack(1, connectionSocket);
//						byte[] buffer = new byte[256];
//						BufferMethods.toByteArray(buffer, returnSocket.get(0).getLocalPort());
						// envia número de porta
//						outToServer.write(buffer, 0, 5);
						BufferMethods.sendInt(returnSocket.get(0).getLocalPort(), connectionSocket);
					} else {
						status = -1;
//						outToServer.write(0);
						BufferMethods.sendFeedBack(0, connectionSocket);
					}
				}
			}	
		} finally {
			if (connectionSocket != null) {
				if (!connectionSocket.isClosed()) {
					try {
						connectionSocket.close(false);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return new AbstractMap.SimpleEntry<ArrayList<DGServerSocket>, Integer>(returnSocket, new Integer(status));
	}
	
	/**
	 * Método que cliente usa para login automático quando consegue reconectar ao servidor.
	 * @param username Nome de usuario
	 * @return um código de status (sucesso ou pq falhou).
	 * Se falhou, retorna uma ServerSocket nula.<br>
	 * -2 quere dizer erro na conexão com BD<br>
	 * 0 quer dizer usuário incorreto (não deveria dar)<br>
	 * 1 quer dizer OK<br>
	 * 2 quer dizer usuário já está online
	 * @throws GeneralSecurityException 
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public int login(String username, int chatPort)
			throws GeneralSecurityException, IOException {
		DGSocket connectionSocket = null;
		int status = -2;
		try {
			// se conecta ao servidor de operações
			connectionSocket = new DGSocket(pDescartaPacotes, pktsPerdidos, this.ip, this.port);
//			OutputStream outToServer = connectionSocket.getOutputStream();
//			InputStream inFromServer = connectionSocket.getInputStream();
			// cadastro(0), login(1)		
//			outToServer.write(11);
			BufferMethods.sendInt(11, connectionSocket);
			
//			int bdOK = inFromServer.read();
			int bdOK = BufferMethods.receiveFeedBack(connectionSocket);
			
			if (bdOK == 1) {				
				BufferMethods.writeString(username, connectionSocket);
				
				// recebe do servidor status do login (sucesso ou falha)
//				status = inFromServer.read();
				status = BufferMethods.receiveFeedBack(connectionSocket);
				
				if (status == 1) {
//					byte[] buffer = new byte[256];
//					BufferMethods.toByteArray(buffer, chatPort);
					// envia número de porta
//					outToServer.write(buffer, 0, 5);
					BufferMethods.sendInt(chatPort, connectionSocket);
				}
			}	
		} catch (IOException e) {
			throw e;
		} finally {
			if (connectionSocket != null) {
				if (!connectionSocket.isClosed()) {
					try {
						connectionSocket.close(false);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		return status;
	}
	
	/**
	 * Tira username da lista de onlines que o servidor tem
	 * @param username Usuário a ser retirado da lista de onlines
	 * @return verdadeiro se conseguiu tirar, falso cc.
	 * @throws IOException 
	 */
	public boolean logout(String username) throws IOException {
		DGSocket connectionSocket = null;
		try {
			// se conecta ao servidor de operacoes em contas
			connectionSocket = new DGSocket(pDescartaPacotes, pktsPerdidos, this.ip, this.port);
//			OutputStream outToServer = connectionSocket.getOutputStream();
//			InputStream inFromServer = connectionSocket.getInputStream();
			
			// diz que quer logout
//			outToServer.write(2);
			BufferMethods.sendInt(2, connectionSocket);
			
			// envia o nome de usuario
			BufferMethods.writeString(username, connectionSocket);
			
			if (BufferMethods.receiveFeedBack(connectionSocket) == 1) {
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
						connectionSocket.close(false);
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
		DGSocket connectionSocket = null;
		try {
			connectionSocket = new DGSocket(pDescartaPacotes, pktsPerdidos, this.ip, this.port);
//			OutputStream outToServer = connectionSocket.getOutputStream();
//			InputStream inFromServer = connectionSocket.getInputStream();
			
//			outToServer.write(3);
			BufferMethods.sendInt(3, connectionSocket);
			
//			int listSize = inFromServer.read();
			int listSize = BufferMethods.receiveInt(connectionSocket);
			for (int i = 0; i < listSize; i++) {
				retorno.add(BufferMethods.readString(connectionSocket));
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (connectionSocket != null) {
				if (!connectionSocket.isClosed()) {
					try {
						connectionSocket.close(false);
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
		DGSocket connectionSocket = null;
		try {
			connectionSocket = new DGSocket(pDescartaPacotes, pktsPerdidos, this.ip, this.port);
//			OutputStream outToServer = connectionSocket.getOutputStream();
//			InputStream inFromServer = connectionSocket.getInputStream();
			
//			outToServer.write(4); // diz que quer solicitar amizade
			BufferMethods.sendInt(4, connectionSocket);
			
//			int bdOK = inFromServer.read();
			int bdOK = BufferMethods.receiveFeedBack(connectionSocket);
			if (bdOK == 1) {
				BufferMethods.writeString(user, connectionSocket);
				BufferMethods.writeString(friend, connectionSocket);
//				int status = inFromServer.read();
				int status = BufferMethods.receiveFeedBack(connectionSocket);
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
						connectionSocket.close(false);
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
		DGSocket connectionSocket = null;
		try {
			connectionSocket = new DGSocket(pDescartaPacotes, pktsPerdidos, this.ip, this.port);
//			OutputStream outToServer = connectionSocket.getOutputStream();
//			InputStream inFromServer = connectionSocket.getInputStream();
			
//			outToServer.write(op);
			BufferMethods.sendInt(op, connectionSocket);
//			int bdOK = inFromServer.read();
			int bdOK = BufferMethods.receiveFeedBack(connectionSocket);
			if (bdOK == 1) {
				BufferMethods.writeString(user, connectionSocket);
//				int listSize = inFromServer.read();
				int listSize = BufferMethods.receiveInt(connectionSocket);
				for (int i = 0; i < listSize; i++) {
					retorno.add(BufferMethods.readString(connectionSocket));
				}
			}			
		} catch (IOException e) {
			throw e;
		} finally {
			if (connectionSocket != null) {
				if (!connectionSocket.isClosed()) {
					try {
						connectionSocket.close(false);
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
		DGSocket connectionSocket = null;
		try {
			connectionSocket = new DGSocket(pDescartaPacotes, pktsPerdidos, this.ip, this.port);
//			OutputStream outToServer = connectionSocket.getOutputStream();
//			InputStream inFromServer = connectionSocket.getInputStream();
			
//			outToServer.write(op); // diz que quer recusar amizade
			BufferMethods.sendInt(op, connectionSocket);
			
//			int bdOK = inFromServer.read();
			int bdOK = BufferMethods.receiveFeedBack(connectionSocket);
			if (bdOK == 1) {
				BufferMethods.writeString(user, connectionSocket);
				BufferMethods.writeString(friend, connectionSocket);
//				return inFromServer.read();
				return BufferMethods.receiveFeedBack(connectionSocket);
			} else {
				return -1; 
			}
		} catch (IOException e) {
			throw e;
		} finally {
			if (connectionSocket != null) {
				if (!connectionSocket.isClosed()) {
					try {
						connectionSocket.close(false);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
}
