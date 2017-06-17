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

import utility.buffer.BufferMethods;
import utility.security.PasswordSecurity;

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
			
			// recebe operacao
			int operacao = inFromClient.read();
			byte[] buffer = new byte[256];
			
			if (operacao == 3) { // enviar lista de online
				synchronized (listaDeUsuarios) {
					outToClient.write(listaDeUsuarios.size());
					for (int i = 0; i < listaDeUsuarios.size(); i++) {
						BufferMethods.writeString(listaDeUsuarios.get(i), outToClient);
					}
				}
			} else if (operacao == 2) { // logout
				/* para deslogar, basta receber o nome do usuário e retirá-lo
				 * do registro de usuários online. Garantindo que esta operação
				 * só pode ser chamada por um cliente quando já logado, não é preciso
				 * se preocupar com autenticação pra evitar que qualquer um deslogue qualquer outro.
				 * Checar pelo IP deve ser o suficiente hehe.
				 * Caso contrário, como autenticar sem que o usuário tenha que informar a senha pra logout
				 * (que não faz sentido nenhum) e sem guardar a senha do usuário localmente também, que
				 * queremos evitar. Uma solução seria fazer com que o lado cliente possa ter o hash
				 * e no caso de logout checar pelo menos pelo hash...
				 */
				// recebe nome de usuario
				String username = BufferMethods.readString(inFromClient);
				boolean logouOut = false;
				synchronized (listaDeUsuarios) {
					int pos = usuarioListaOnline(listaDeUsuarios, username, connectionSocket.getInetAddress().getHostAddress());
					if (pos != -1 ) {
						listaDeUsuarios.remove(pos);
						listaDeUsuarios.notify();
						logouOut = true;
					}
				}
				if (logouOut) {
					outToClient.write(1);
				} else {
					outToClient.write(0);
				}
			} else if (operacao == 1) { // login
				// recebe nome de usuario
				String username = BufferMethods.readString(inFromClient);
				// autenticacao
				String usrSalt = null;
				synchronized (usuariosCadastrados) {
					usrSalt = getUsrSalt(usuariosCadastrados, username);
				}
				if (usrSalt == null) { // se nao tiver salt, gera. se tiver ai manda o que tem
					byte[] salt = PasswordSecurity.getSalt().getBytes("ASCII");
					usrSalt = PasswordSecurity.toHex(salt);
				}
				BufferMethods.toByteArray(buffer, usrSalt);
				// usrSalt sempre tera 32 caracteres.
				outToClient.write(buffer, 0, 32);
				
				// recebe senha
				inFromClient.read(buffer, 0, 128);
				String usrPw = BufferMethods.byteArraytoString(buffer, 128);
				
				// atualizar listaDeUsuarios online
				boolean usrExiste = false;
				synchronized (usuariosCadastrados) {
					usrExiste = usrExisteBinario(usuariosCadastrados, username, usrPw);
				}
				// checa se username existe, se password bate
				if (usrExiste) {
					// checa se usuario ja esta online
					int usrOn = -1;
					synchronized (listaDeUsuarios) {
						usrOn = usuarioListaOnline(listaDeUsuarios, username);
					}
					if (usrOn != -1) {
						System.out.println("Usuario já está online");
						outToClient.write(2);
					} else {
						outToClient.write(1);
						// descobre se o cliente conseguiu um servidor
						int servOK = inFromClient.read();
						if (servOK == 1) {
							
							// lê o número de porta
							inFromClient.read(buffer, 0, 5);
							int port = Integer.parseInt(BufferMethods.byteArraytoString(buffer, 5));
							
							/* esta parte foi retirada pois o IP que ele recebe é o 0.0.0.0
							 * troquei pra que o servidor pegue o IP da outra ponta da conexão
							 * com connectionSocket mesmo.
							 */
							// lê comprimento da string IP
//							int iplen = inFromClient.read();
							// lê IP
//							inFromClient.read(buffer, 0, iplen);
//							String IP = BufferMethods.byteArraytoString(buffer, iplen);
							synchronized (listaDeUsuarios) {
								listaDeUsuarios.add( username + " ("
										+ connectionSocket.getInetAddress().getHostAddress() + ", "
										+ port + ")");
								listaDeUsuarios.sort(String::compareToIgnoreCase);
								listaDeUsuarios.notify();
							}
						} else {
							System.out.println("o cara n conseguiu achar porta pro servidor lol");
						}
					}
				} else {
					System.out.println("Usuário ou senha incorretos");
					outToClient.write(0);
				}
			} else if (operacao == 0) { // cadastro
				// recebe nome de usuario
				String username = BufferMethods.readString(inFromClient);
				// autenticacao
				String usrSalt = null;
				synchronized (usuariosCadastrados) {
					usrSalt = getUsrSalt(usuariosCadastrados, username);
				}
				if (usrSalt == null) { // se nao tiver salt, gera. se tiver ai manda o que tem
					byte[] salt = PasswordSecurity.getSalt().getBytes("ASCII");
					usrSalt = PasswordSecurity.toHex(salt);
				}
				BufferMethods.toByteArray(buffer, usrSalt);
				// usrSalt sempre tera 32 caracteres.
				outToClient.write(buffer, 0, 32);
				
				// recebe senha
				inFromClient.read(buffer, 0, 128);
				String usrPw = BufferMethods.byteArraytoString(buffer, 128);
				
				boolean cadastroOk = false;
				synchronized (usuariosCadastrados) {
					cadastroOk = cadastroBinario(usuariosCadastrados, username, usrPw, usrSalt);
				}
				if(cadastroOk) {
					// conseguiu cadastrar
					System.out.println("Cadastro OK de " + username);
					outToClient.write(1);
				} else {
					// nao conseguiu cadastrar
					System.out.println("Não consegui cadastrar " + username);
					outToClient.write(0);
				}
			}
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
	 * faz busca binaria na lista de usuarios online, checa IP.
	 * serve para saber se pode fazer login ou se isso seria ilegal.
	 * @param listaDeUsuarios ArrayList com usuarios online.
	 * @param usr Nome de usuario.
	 * @param hostAddress IP.
	 * @return Posição na lista, -1 se não foi encontrado.
	 */
	private int usuarioListaOnline(ArrayList<String> listaDeUsuarios, String usr, String hostAddress) {
		int l = 0, r = listaDeUsuarios.size() - 1, m, comp;
		String str;
		if (r >= 0) {
			do {
				m = (int) ((l + r) / 2);
				str = listaDeUsuarios.get(m);
				comp = (usr + " (" + hostAddress + ",").compareToIgnoreCase(str.
						substring(0, str.indexOf(",") + 1));
				if (comp == 0) {
					return m;
				} else if (comp < 0) {
					r = m - 1;
				} else {
					l = m + 1;
				}
			} while (l <= r);
		}
		return -1;
	}

	/**
	 * faz busca binaria na lista de usuarios online
	 * serve para saber se pode fazer login ou se isso seria ilegal.
	 * @param listaDeUsuarios ArrayList com usuarios online.
	 * @param usr Nome de usuario.
	 * @return Posição na lista, -1 se não foi encontrado.
	 */
	private int usuarioListaOnline(ArrayList<String> listaDeUsuarios, String usr) {
		int l = 0, r = listaDeUsuarios.size() - 1, m, comp;
		String str;
		if (r >= 0) {
			do {
				m = (int) ((l + r) / 2);
				str = listaDeUsuarios.get(m);
				comp = (usr + " (").compareToIgnoreCase(str.
						substring(0, str.indexOf('(') + 1));
				if (comp == 0) {
					return m;
				} else if (comp < 0) {
					r = m - 1;
				} else {
					l = m + 1;
				}
			} while (l <= r);
		}
		return -1;
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
}
