package utility.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import utility.buffer.BufferMethods;
import utility.security.PasswordSecurity;

/**
 * Essa classe deve prover uma série de métodos que o cliente pode usar para
 * se comunicar com o servidor
 * @author Pedro Queiroga <psq@cin.ufpe.br>
 *
 */
public class ServerAPI {
	// TODO fazer retornar um booleano pro cliente conferir se conseguiu
	// cadastrar/logar com sucesso ou não.
	/**
	 * Função que cliente usa para solicitar loing/cadastro ao servidor
	 * @param username Nome de usuário
	 * @param password Senha
	 * @param funcao Cadastro ou Login
	 * @return -2 = alguma exception nesse método. -1 = usuario ja online
	 * 0 = falha comum (login invlido, senha, usrname indisponivel)
	 * 1 = sucesso
	 */
	public static int loginCadastro(String username, String password, int funcao) {
		int codigo = -2;
		try {
			Socket connectionSocket = new Socket("localhost", 2020);
			OutputStream outToServer = connectionSocket.getOutputStream();
			InputStream inFromServer = connectionSocket.getInputStream();
			
			// 0 = cadastro, 1 = login		
			outToServer.write(funcao);
			
			byte[] buffer = new byte[256];

			outToServer.write(username.length());
			BufferMethods.toByteArray(buffer, username);
			outToServer.write(buffer, 0, username.length());
			
			// recebe o salt, que sempre tera 32 chars
			inFromServer.read(buffer, 0, 32);
			byte[] salt = PasswordSecurity.fromHex(BufferMethods.byteArraytoString(buffer, 32));
			String nicepw = null;
			try {
				nicepw = PasswordSecurity.generateStrongPasswordHash(password, salt).split(":")[2];
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
//				outToServer.write(nicepw.length()); sempre 128
			BufferMethods.toByteArray(buffer, nicepw);
			outToServer.write(buffer, 0, 128);
			codigo = inFromServer.read();
			connectionSocket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return codigo;
	} 
}
