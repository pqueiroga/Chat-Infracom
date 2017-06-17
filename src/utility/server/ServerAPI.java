package utility.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
	/**
	 * Método que cliente usa para solicitar cadastro ao servidor
	 * @param username Nome de usuário validado
	 * @param password Senha validada
	 * @return -2 = alguma exception nesse método. -1 = usuario ja online
	 * 0 = falha comum (login invlido, senha, usrname indisponivel)
	 * 1 = sucesso
	 */
	public static boolean cadastro(String username, String password) {
		boolean retorno = false;
		Socket connectionSocket = null;
		try {
			// se conecta ao servidor de operacoes em contas
			connectionSocket = new Socket("localhost", 2020);
			int codigo = corpoComum(username, password, connectionSocket, 0);
			if (codigo == 1) {
				retorno = true; 
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	 * Método que cliente usa para solicitar login.
	 * @param username Nome de usuario
	 * @param password Senha
	 * @return dois valores, um ServerSocket e um inteiro. O inteiro é um código de status (sucesso ou pq falhou).
	 * Se falhou, retorna uma ServerSocket nula.
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public static Map.Entry<ServerSocket, Integer> login(String username, String password) {
		ServerSocket returnSocket = null;
		Socket connectionSocket = null;
		int status = -2;
		try {
			// se conecta ao servidor de operações
			connectionSocket = new Socket("localhost", 2020);
			OutputStream outToServer = connectionSocket.getOutputStream();
			InputStream inFromServer = connectionSocket.getInputStream();
			// salva a porta que foi alocada a ele, a qual o servidor poderá passar pra outros usuários
//			int portaDaSessao = connectionSocket.getLocalPort();
			
			status = corpoComum(username, password, outToServer, inFromServer, 1);
			
			// agora checamos se acertamos as credenciais, se acertamos então podemos criar uma ServerSocket
			// e dar essa porta pro servidor constatar que estamos online e podemos receber por essa porta.
			int portaDaSessao = 2030;
			if (status == 1) {
				while (returnSocket == null && portaDaSessao < 65535) {
					try {
						returnSocket = new ServerSocket(portaDaSessao);
					} catch (BindException e) {
						System.out.println(portaDaSessao + " indisponível, tentando com a próxima porta...");
						portaDaSessao++;
					}
				}
				if (returnSocket != null) {
					// diz que conseguiu um servidor
					outToServer.write(1);
					byte[] buffer = new byte[256];
					BufferMethods.toByteArray(buffer, returnSocket.getLocalPort());
					// envia número de porta
					outToServer.write(buffer, 0, 5);
					
					/* esta parte foi retirada pois o IP que é enviado é o 0.0.0.0
					 * troquei pra que o servidor pegue o IP da conexão com
					 * connectionSocket mesmo.
					 */
					// coloca IP no buffer
//					BufferMethods.toByteArray(buffer, returnSocket.getInetAddress().getHostAddress());
					// diz o tamanho da string que o servidor deve esperar por
//					outToServer.write(returnSocket.getInetAddress().getHostAddress().length());
					// envia IP
//					outToServer.write(buffer, 0, returnSocket.getInetAddress().getHostAddress().length());
				} else {
					outToServer.write(0);
				}
			}
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		return new AbstractMap.SimpleEntry<ServerSocket, Integer>(returnSocket, new Integer(status));
	}
	
	public static boolean logout(String username) {
		boolean retorno = false;
		Socket connectionSocket = null;
		try {
			// se conecta ao servidor de operacoes em contas
			connectionSocket = new Socket("localhost", 2020);
			OutputStream outToServer = connectionSocket.getOutputStream();
			InputStream inFromServer = connectionSocket.getInputStream();
			
			// diz que quer logout
			outToServer.write(2);
			
			// envia o nome de usuario
			BufferMethods.writeString(username, outToServer);
			
			if (inFromServer.read() == 1) {
				retorno = true;
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
	public static ArrayList<String> pegaOnlines() {
		ArrayList<String> retorno = new ArrayList<String>();
		Socket connectionSocket = null;
		try {
			connectionSocket = new Socket("localhost", 2020);
			OutputStream outToServer = connectionSocket.getOutputStream();
			InputStream inFromServer = connectionSocket.getInputStream();
			
			outToServer.write(3);
			
			int listSize = inFromServer.read();
			for (int i = 0; i < listSize; i++) {
				retorno.add(BufferMethods.readString(inFromServer));
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
	 * @param username Nome de usuario
	 * @param password Senha
	 * @param connectionSocket Socket que esta conectada ao servidor de operações em contas
	 * @param funcao cadastro(0) ou login(1)
	 * @return um código de status
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeySpecException
	 */
	private static int corpoComum(String username, String password, Socket connectionSocket, int funcao)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		OutputStream outToServer = connectionSocket.getOutputStream();
		InputStream inFromServer = connectionSocket.getInputStream();
		
		// cadastro(0), login(1)		
		outToServer.write(funcao);
		
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
		return codigo;
	}
	
	private static int corpoComum(String username, String password, OutputStream outToServer,
			InputStream inFromServer, int funcao)
			throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
		
		// cadastro(0), login(1)		
		outToServer.write(funcao);
		
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
		return codigo;
	}
}
