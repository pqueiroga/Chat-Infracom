package server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import security.PasswordSecurity;

/**
 * Classe que executa as funcoes de login e cadastro.
 * @author Pedro Queiroga <psq@cin.ufpe.br>
 *
 */
public class ServidorCadastroLogin implements Runnable {

	private Socket connectionSocket;
	private ArrayList<String> listaDeUsuarios;
	private File usuariosCadastrados;

	public ServidorCadastroLogin(Socket connectionSocket, ArrayList<String> listaDeUsuarios,
			File usuariosCadastrados) {
		this.connectionSocket = connectionSocket;
		this.listaDeUsuarios = listaDeUsuarios;
		this.usuariosCadastrados = usuariosCadastrados;
	}
	
	@Override
	public void run() {
		try {
			OutputStream outToClient = connectionSocket.getOutputStream();
			InputStream inFromClient = connectionSocket.getInputStream();
			
			int cadastroLogin = inFromClient.read();
			byte[] buffer = new byte[256];
			
			int strlen = inFromClient.read();
			inFromClient.read(buffer, 0, strlen);
			String usrName = byteArraytoString(buffer, strlen);
			
			String usrSalt = null;
			synchronized (usuariosCadastrados) {
				usrSalt = getUsrSalt(usuariosCadastrados, usrName);
			}
			if (usrSalt == null) { // se nao tiver salt, gera. se tiver ai manda o que tem
				byte[] salt = PasswordSecurity.getSalt().getBytes("ASCII");
				System.out.println(salt + " has length: " + salt.length);
				usrSalt = PasswordSecurity.toHex(salt);
				System.out.println(usrSalt + " has length: " + usrSalt.length());

			}
			toByteArray(buffer, usrSalt);
			// usrSalt sempre tera 32 caracteres.
			outToClient.write(buffer, 0, 32);
			
//			strlen = inFromClient.read(); sempre 128
			inFromClient.read(buffer, 0, 128);
			String usrPw = byteArraytoString(buffer, 128);
			
			if (cadastroLogin == 0) { // cadastro
				synchronized (usuariosCadastrados) {
					if(cadastroBinario(usuariosCadastrados, usrName, usrPw, usrSalt)) {
						// conseguiu cadastrar
						System.out.println("Cadastro OK de " + usrName + ":" + usrPw);
					} else {
						// nao conseguiu cadastrar
						System.out.println("Não consegui cadastrar " + usrName + ":" + usrPw);
					}
				}
			} else { // login
				// atualizar listaDeUsuarios online
				boolean usrExiste = false;
				synchronized (usuariosCadastrados) {
					usrExiste = usrExisteBinario(usuariosCadastrados, usrName, usrPw);
				}
				// checa se username existe, se password bate
				if (usrExiste) {
					// checa se usuario ja esta online
					synchronized (listaDeUsuarios) {
						if (usuarioListaOnline(listaDeUsuarios, usrName)) {
							System.out.println("Usuario já está online");
						} else {
							listaDeUsuarios.add( usrName + " ("
									+ connectionSocket.getInetAddress().getHostAddress() + ", "
									+ connectionSocket.getPort() + ")");
							listaDeUsuarios.sort(String::compareToIgnoreCase);
							listaDeUsuarios.notify();
						}
					}
				} else {
					System.out.println("Usuario ou senha incorretos");
				}
			} // TODO logout
			connectionSocket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * faz busca binaria na lista de usuarios online
	 * serve para saber se pode fazer login ou se isso seria ilegal.
	 * @param listaDeUsuarios ArrayList com usuarios online.
	 * @param usr Nome de usuario.
	 * @return Verdadeiro se usuario ja esta online.
	 */
	private boolean usuarioListaOnline(ArrayList<String> listaDeUsuarios, String usr) {
		int l = 0, r = listaDeUsuarios.size() - 1, m, comp;
		String str;
		boolean uniqueUsr = true;
		if (r >= 0) {
			do {
				m = (int) ((l + r) / 2);
				str = listaDeUsuarios.get(m);
				comp = (usr + " (").compareToIgnoreCase(str.
						substring(0, str.indexOf('(') + 1));
				if (comp == 0) {
					uniqueUsr = false;
					break;
				} else if (comp < 0) {
					r = m - 1;
				} else {
					l = m + 1;
				}
			} while (l <= r);
		}
		return !uniqueUsr;
	}
	
	/**
	 * Cria uma cadeia de caracteres a partir de um arranjo de bytes.
	 * @param buf Buffer que deve ser transformado em uma cadeia de caracteres.
	 * @param strlen Tamanho da cadeia de caracteres.
	 * @return Uma cadeia composta pelos caracteres de buf.
	 */
	private String byteArraytoString(byte[] buf, int strlen) {
		String retorno = "";
		for (int i = 0; i < strlen; i++) {
			retorno += (char) buf[i];
		}
		return retorno;
	}
	
	/**
	 * busca binaria para, caso usuario esteja disponivel,
	 *  cadastrar no lugar certo (mantendo ordenacao).
	 * @param usuariosCadastrados Arquivo dos usuarios.
	 * @param usr Nome de usuario.
	 * @param pw Senha.
	 * @return Verdadeiro se conseguiu cadastrar, falso caso contrario.
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	static boolean cadastroBinario(File usuariosCadastrados, String usr, String pw,
			String salt)
			throws NumberFormatException, IOException {
		FileReader fr = new FileReader(usuariosCadastrados);
		BufferedReader br = new BufferedReader(fr);
		int qtdUsr = Integer.parseInt(br.readLine());
		String line;
		ArrayList<String> usernames = new ArrayList<String>();
		ArrayList<String> passwords = new ArrayList<String>();
		ArrayList<String> salts = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			String[] tokens = line.split(":");
			usernames.add(tokens[0]);
			salts.add(tokens[1]);
			passwords.add(tokens[2]);
		}
		br.close();
		fr.close();
		int l = 0, r = qtdUsr - 1, m, comp;
		boolean uniqueUsr = true;
		if (r >= 0) {
			do {
				m = (int) ((l + r) / 2);
				comp = usr.compareToIgnoreCase(usernames.get(m));
				if (comp == 0) {
					uniqueUsr = false;
					break;
				} else if (comp < 0) {
					r = m - 1;
				} else {
					l = m + 1;
				}
			} while (l <= r);
		}
		if (uniqueUsr) {
			usernames.add(l, usr);
			salts.add(l, salt);
			passwords.add(l, pw);
			FileWriter fw = new FileWriter(usuariosCadastrados);
			fw.write(qtdUsr + 1 + "\n");
			for (int i = 0; i < usernames.size(); i++) {
				fw.write(usernames.get(i) + ":" + salts.get(i) + ":" + passwords.get(i));
				if (i < usernames.size()) {
					fw.write("\n");
				}
			}
			fw.close();
		}
		return uniqueUsr;
	}
	
	/**
	 * Busca binaria para saber se usuario existe, pro login.
	 * Checa se senha esta correta.
	 * @param usuariosCadastrados Arquivo dos usuarios.
	 * @param usr Nome de usuario.
	 * @param pw Senha.
	 * @return Verdadeiro se usuario existe, falso caso contrario.
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	static boolean usrExisteBinario(File usuariosCadastrados, String usr, String pw)
			throws NumberFormatException, IOException {
		FileReader fr = new FileReader(usuariosCadastrados);
		BufferedReader br = new BufferedReader(fr);
		int qtdUsr = Integer.parseInt(br.readLine());
		String line;
		ArrayList<String> usernames = new ArrayList<String>();
		ArrayList<String> passwords = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			String[] tokens = line.split(":");
			usernames.add(tokens[0]);
			passwords.add(tokens[2]);
		}
		br.close();
		fr.close();
		int l = 0, r = qtdUsr - 1, m, comp;
		boolean usrOK = false;
		if (r >= 0) {
			do {
				m = (int) ((l + r) / 2);
				comp = usr.compareToIgnoreCase(usernames.get(m));
				if (comp == 0) {
					if (passwords.get(m).equals(pw)) {
						usrOK = true;
					}
					break;
				} else if (comp < 0) {
					r = m - 1;
				} else {
					l = m + 1;
				}
			} while (l <= r);
		}
		return usrOK;
	}
	
	/**
	 * 
	 * @param usuariosCadastrados
	 * @param usr
	 * @return nulo se usuario nao existe
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	static String getUsrSalt(File usuariosCadastrados, String usr)
			throws NumberFormatException, IOException {
		FileReader fr = new FileReader(usuariosCadastrados);
		BufferedReader br = new BufferedReader(fr);
		int qtdUsr = Integer.parseInt(br.readLine());
		String line;
		ArrayList<String> usernames = new ArrayList<String>();
		ArrayList<String> salts = new ArrayList<String>();
		while ((line = br.readLine()) != null) {
			String[] tokens = line.split(":");
			usernames.add(tokens[0]);
			salts.add(tokens[1]);
		}
		br.close();
		fr.close();
		int l = 0, r = qtdUsr - 1, m, comp;
		String usrSalt = null;
		if (r >= 0) {
			do {
				m = (int) ((l + r) / 2);
				comp = usr.compareToIgnoreCase(usernames.get(m));
				if (comp == 0) {
					usrSalt = salts.get(m);
					break;
				} else if (comp < 0) {
					r = m - 1;
				} else {
					l = m + 1;
				}
			} while (l <= r);
		}
		return usrSalt;
	}
	
	/**
	 * Coloca uma cadeia num arranjo de bytes.
	 * @param buf Arranjo de bytes que deve receber a String str.
	 * @param str String que deve ser colocada no arranjo buf.
	 */
	private void toByteArray(byte[] buf, String str) {
		for (int i = 0; i < str.length(); i++) {
			buf[i] = (byte) str.charAt(i);
		}
	}

}
