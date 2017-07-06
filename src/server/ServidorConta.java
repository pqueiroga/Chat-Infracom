package server;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentMap;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import protocol.DGSocket;
import utility.buffer.BufferMethods;
import utility.security.PasswordSecurity;

/**
 * Classe que executa as funcoes de login e cadastro.
 * @author Pedro Queiroga <psq@cin.ufpe.br>
 *
 */
public class ServidorConta implements Runnable {

	private DGSocket connectionSocket;
	private ArrayList<String> listaDeUsuarios;
	private ConcurrentMap<String, Long> timer;
	
	public ServidorConta(ConcurrentMap<String, Long> timer, DGSocket connectionSocket, ArrayList<String> listaDeUsuarios) {
		this.connectionSocket = connectionSocket;
		this.listaDeUsuarios = listaDeUsuarios;
		this.timer = timer;
	}
	
	@Override
	public void run() {
		while (true) {
		try {
			int operacao = BufferMethods.receiveInt(connectionSocket);
			byte[] buffer = new byte[256];

			
			if (operacao == 11) { // login automatico
				BancoAmizade banco = new BancoAmizade();
				banco.conectar();
				if (banco.conectado()) {
					BufferMethods.sendFeedBack(1, connectionSocket);
					String username = BufferMethods.readString(connectionSocket);
					if (banco.usuarioExiste(username)) {
						// checa se usuario ja esta online
						int usrOn = -1;
						synchronized (listaDeUsuarios) {
							usrOn = usuarioListaOnline(listaDeUsuarios, username);
						}
						if (usrOn != -1) {
							System.out.println("Usuario já está online");
							BufferMethods.sendFeedBack(2, connectionSocket);
						} else {
							BufferMethods.sendFeedBack(1, connectionSocket);
							
							// lê o número de porta
//							int port = BufferMethods.receiveInt(connectionSocket);
							System.out.println("Indo inserir na lista");
							synchronized (listaDeUsuarios) {
								listaDeUsuarios.add( username + " ("
										+ connectionSocket.getInetAddress().getHostAddress() + ", "
										+ connectionSocket.getPort() + ")");
								listaDeUsuarios.sort(String::compareToIgnoreCase);
								listaDeUsuarios.notify();
							}
							System.out.println(listaDeUsuarios);
							synchronized (timer) {
								timer.put(username, new Long(System.currentTimeMillis()));
							}
						}
					} else {
						BufferMethods.sendFeedBack(0, connectionSocket);
					}
				} else {
					BufferMethods.sendFeedBack(0, connectionSocket);
				}
			} else if (operacao == 10) { // lista amigos (inclusive offline)
				// se conecta ao banco de dados
				BancoAmizade banco = new BancoAmizade();
				banco.conectar();
				if (banco.conectado()) {
					ArrayList<String> flOnline = new ArrayList<String>();
					ArrayList<String> flOffline = new ArrayList<String>();
					BufferMethods.sendFeedBack(1, connectionSocket);
					
					String username = BufferMethods.readString(connectionSocket);
					ArrayList<String> fl = banco.listarAmigos(username);
					banco.desconectar();
					synchronized (listaDeUsuarios) {
						int j = 0;
						boolean jOk;
						while (j < fl.size()) {
							jOk = false;
							for (int i = 0; i < listaDeUsuarios.size(); i++) {
								String temp = listaDeUsuarios.get(i);
								if (temp.indexOf(' ') != -1) {
									if ((temp.substring(0, temp.indexOf(' '))).equals(fl.get(j))) {
										flOnline.add(temp);
										jOk = true;
										break;
									}
								}
							}
							if (!jOk) {
								flOffline.add(fl.get(j));
							}
							j++;
						}
					}
					
					BufferMethods.sendInt(flOnline.size() + flOffline.size(), connectionSocket);
					for (String str : flOnline) {
						BufferMethods.writeString(str, connectionSocket);
					}
					for (String str : flOffline) {
						BufferMethods.writeString(str, connectionSocket);
					}
					synchronized (timer) {
						timer.put(username, new Long(System.currentTimeMillis()));						
					}
				} else {
					BufferMethods.sendFeedBack(0, connectionSocket);
				}
			} else if (operacao == 9) { // lista solicitações pendentes
				// se conecta ao banco de dados
				BancoAmizade banco = new BancoAmizade();
				banco.conectar();
				if (banco.conectado()) {
					BufferMethods.sendFeedBack(1, connectionSocket);
					String username = BufferMethods.readString(connectionSocket);
					ArrayList<String> solpen = banco.pedidosPendentes(username);
					banco.desconectar();
					BufferMethods.sendInt(solpen.size(), connectionSocket);
					for (String str : solpen) {
						BufferMethods.writeString(str, connectionSocket);
					}
					synchronized (timer) {
						timer.put(username, new Long(System.currentTimeMillis()));						
					}
				} else {
					BufferMethods.sendFeedBack(0, connectionSocket);
				}
			} if (operacao == 8) { // lista amigos online
				// se conecta ao banco de dados
				BancoAmizade banco = new BancoAmizade();
				banco.conectar();
				if (banco.conectado()) {
					ArrayList<String> flOnline = new ArrayList<String>();
//					outToClient.write(1);
					BufferMethods.sendFeedBack(1, connectionSocket);
					String username = BufferMethods.readString(connectionSocket);
					ArrayList<String> fl = banco.listarAmigos(username);
					banco.desconectar();
					synchronized (listaDeUsuarios) {
						int j = 0;
//						boolean jOk;
						while (j < fl.size()) {
//							jOk = false;
							for (int i = 0; i < listaDeUsuarios.size(); i++) {
								if (j >= fl.size()) {
									break;
								}
								String temp = listaDeUsuarios.get(i);
								if (temp.indexOf(' ') != -1) {
									if ((temp.substring(0, temp.indexOf(' '))).equals(fl.get(j))) {
										flOnline.add(temp);
//										jOk = true;
										break;
									}
								}
							}
							j++;
						}
					}
					
//					outToClient.write(flOnline.size());
					BufferMethods.sendInt(flOnline.size(), connectionSocket);
					for (String str : flOnline) {
						BufferMethods.writeString(str, connectionSocket);
					}
					synchronized (timer) {
						timer.put(username, new Long(System.currentTimeMillis()));						
					}
				} else {
//					outToClient.write(0);
					BufferMethods.sendFeedBack(0, connectionSocket);
				}
			} else if (operacao == 6 || operacao == 7) { // recusar amizade ou remover (mesmo procedimento)
				// se conecta ao banco de dados
				BancoAmizade banco = new BancoAmizade();
				banco.conectar();
				if (banco.conectado()) {
//					outToClient.write(1); // conseguiu se conectar ao BD
					BufferMethods.sendFeedBack(1, connectionSocket);
					String username = BufferMethods.readString(connectionSocket);
					String friend = BufferMethods.readString(connectionSocket);
					
					boolean recusou = banco.removerAmigo(username, friend);
					banco.desconectar();
					if (recusou) {
//						outToClient.write(1); // conseguiu executar a operação
						BufferMethods.sendFeedBack(1, connectionSocket);
					} else {
//						outToClient.write(0); // não conseguiu executar a operação
						BufferMethods.sendFeedBack(0, connectionSocket);
					}
				} else {
//					outToClient.write(0); // não conseguiu se conectar ao BD
					BufferMethods.sendFeedBack(0, connectionSocket);
				}
			} else if (operacao == 5) { // aceitar amizade
				// se conecta ao banco de dados
				BancoAmizade banco = new BancoAmizade();
				banco.conectar();
				if (banco.conectado()) {
//					outToClient.write(1); // conseguiu se conectar ao BD
					BufferMethods.sendFeedBack(1, connectionSocket);
					String username = BufferMethods.readString(connectionSocket);
					String friend = BufferMethods.readString(connectionSocket);
				
					boolean aceitado = banco.aceitarAmizade(username, friend);
					banco.desconectar();
					if (aceitado) {
//						outToClient.write(1); // só quer dizer que conseguiu mexer direitinho no banco de dados
						BufferMethods.sendFeedBack(1, connectionSocket);
						// não quer dizer que não já tinha sido aceito antes, mas se for o caso, não
						// atrapalha em nada.
					} else {
						// diz que não conseguiu executar a operação no banco de dados :/
//						outToClient.write(0);
						BufferMethods.sendFeedBack(0, connectionSocket);
					}
				} else {
//					outToClient.write(0); // não conseguiu se conectar ao BD
					BufferMethods.sendFeedBack(0, connectionSocket);
				}
			} else if (operacao == 4) { // pedido de amizade
				// se conecta ao banco de dados
				BancoAmizade banco = new BancoAmizade();
				banco.conectar();
				if (banco.conectado()) {
					// diz que conseguiu se conectar ao banco de dados
//					outToClient.write(1);
					BufferMethods.sendFeedBack(1, connectionSocket);
					// lê nome do usuário do agente ativo do pedido de amizade
					String username = BufferMethods.readString(connectionSocket);
					// lê nome de usuário do agente passivo do pedido de amizade
					String friend = BufferMethods.readString(connectionSocket);
					try {
						boolean adicionado = banco.pedirAmizade(username, friend);
						if (adicionado) {
//							outToClient.write(1); // 1 quer dizer que o pedido foi efetuado ok
							BufferMethods.sendFeedBack(1, connectionSocket);
						} else {
//							outToClient.write(0);
							BufferMethods.sendFeedBack(0, connectionSocket);
							// 0 quer dizer que o pedido n foi efetuado ok por alguma razão que NÃO é
							// relação já existente!
						}
					} catch (MySQLIntegrityConstraintViolationException e) {
//						outToClient.write(2);
						BufferMethods.sendFeedBack(2, connectionSocket);
						// 2 quer dizer que já existe uma relação para esses usuários
						// ou que algum dos dois usuários não existe!
					} finally {
						banco.desconectar();
					}
				} else {
					// diz que não conseguiu se conectar ao banco de dados
//					outToClient.write(0);
					BufferMethods.sendFeedBack(0, connectionSocket);
				}				
			} else if (operacao == 3) { // enviar lista de online
				synchronized (listaDeUsuarios) {
//					outToClient.write(listaDeUsuarios.size());
					BufferMethods.sendInt(listaDeUsuarios.size(), connectionSocket);
					for (int i = 0; i < listaDeUsuarios.size(); i++) {
						BufferMethods.writeString(listaDeUsuarios.get(i), connectionSocket);
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
				String username = BufferMethods.readString(connectionSocket);
				boolean logouOut = false;
				int pos = -1;
				synchronized (listaDeUsuarios) {
					pos = usuarioListaOnline(listaDeUsuarios, username, connectionSocket.getInetAddress().getHostAddress());
					if (pos != -1 ) {
						listaDeUsuarios.remove(pos);
						listaDeUsuarios.notify();
						logouOut = true;
					}
				}
				if (logouOut && pos != -1) {
					synchronized(timer) {
						timer.remove(username);
					}
				}
				if (logouOut) {
//					outToClient.write(1);
					BufferMethods.sendFeedBack(1, connectionSocket);
				} else {
//					outToClient.write(0);
					BufferMethods.sendFeedBack(0, connectionSocket);
				}
			} else if (operacao == 1) { // login
				// se conecta ao banco de dados
				BancoAmizade banco = new BancoAmizade();
				banco.conectar();
				if (banco.conectado()) {
//					outToClient.write(1); // conseguiu se conectar ao banco de dados
					BufferMethods.sendFeedBack(1, connectionSocket);
					// recebe nome de usuario
					String username = BufferMethods.readString(connectionSocket);
					// autenticacao
					String usrSalt = null;
					ArrayList<String> userInfo = banco.getInfo(username);
					banco.desconectar();
//					synchronized (usuariosCadastrados) {
//						usrSalt = getUsrSalt(usuariosCadastrados, username);
//					}
					if (userInfo.isEmpty()) { // se nao tiver salt, usuário não existe. mascara isso mandando
						// um sal pra ele ficar sem saber se eh usuário ou senha o problema
						byte[] salt = PasswordSecurity.getSalt().getBytes("ASCII");
						usrSalt = PasswordSecurity.toHex(salt);
					} else {
						usrSalt = userInfo.get(1); // salt é na coluna 1
					}
					BufferMethods.toByteArray(buffer, usrSalt);
					// usrSalt sempre tera 32 caracteres.
//					outToClient.write(buffer, 0, 32);
					connectionSocket.send(buffer, 32);
					
					// recebe senha
//					inFromClient.read(buffer, 0, 128);
					connectionSocket.receive(buffer, 128);
					String usrPw = BufferMethods.byteArraytoString(buffer, 128);
					
					// atualizar listaDeUsuarios online
					boolean usrOK;
					if (userInfo.isEmpty()) { // usuário não cadastrado
						usrOK = false;
					} else {
						if (userInfo.get(2).equals(usrPw) && userInfo.get(1).equals(usrSalt)) {
							usrOK = true;
						} else {
							usrOK = false;
						}
					}
					// checa se username existe, se password bate
					if (usrOK) {
						// checa se usuario ja esta online
						int usrOn = -1;
						synchronized (listaDeUsuarios) {
							usrOn = usuarioListaOnline(listaDeUsuarios, username);
						}
						if (usrOn != -1) {
							System.out.println("Usuario já está online");
//							outToClient.write(2);
							BufferMethods.sendFeedBack(2, connectionSocket);
						} else {
//							outToClient.write(1);
							BufferMethods.sendFeedBack(1, connectionSocket);
							// descobre se o cliente conseguiu um servidor
//							int servOK = inFromClient.read();
//							int servOK = BufferMethods.receiveFeedBack(connectionSocket);
//							if (servOK == 1) {
								
								// lê o número de porta
//								inFromClient.read(buffer, 0, 5);
//								int port = Integer.parseInt(BufferMethods.byteArraytoString(buffer, 5));
//								int port = BufferMethods.receiveInt(connectionSocket);
								
								synchronized (listaDeUsuarios) {
									listaDeUsuarios.add(username + " ("
											+ connectionSocket.getInetAddress().getHostAddress() + ", "
											+ connectionSocket.getPort() + ")");
									listaDeUsuarios.sort(String::compareToIgnoreCase);
									listaDeUsuarios.notify();
								}
								synchronized (timer) {
									timer.put(username, new Long(System.currentTimeMillis()));						
								}
//							} else {
//								System.out.println("o cara n conseguiu achar porta pro servidor lol");
//							}
						}
					} else {
						System.out.println("Usuário ou senha incorretos");
//						outToClient.write(0);
						BufferMethods.sendFeedBack(0, connectionSocket);
					}
				} else {
//					outToClient.write(0); // não conseguiu se conectar ao banco de dados
					BufferMethods.sendFeedBack(0, connectionSocket);
				}
			} else if (operacao == 0) { // cadastro
				// se conecta ao banco de dados
				BancoAmizade banco = new BancoAmizade();
				banco.conectar();
				if (banco.conectado()) {
//					outToClient.write(1); // conseguiu se conectar ao BD.
					BufferMethods.sendFeedBack(1, connectionSocket);
					
					// recebe nome de usuario
					String username = BufferMethods.readString(connectionSocket);
					// autenticacao
					String usrSalt = null;
					ArrayList<String> userInfo = banco.getInfo(username);
//					synchronized (usuariosCadastrados) {
//						usrSalt = getUsrSalt(usuariosCadastrados, username);
//					}
					if (userInfo.isEmpty()) { // se nao tiver salt, usuário não existe, que é o que queremos
						byte[] salt = PasswordSecurity.getSalt().getBytes("ASCII");
						usrSalt = PasswordSecurity.toHex(salt);
					} else {
						usrSalt = userInfo.get(1); // salt eh na coluna 1
					}
					BufferMethods.toByteArray(buffer, usrSalt);
					// usrSalt sempre tera 32 caracteres.
//					outToClient.write(buffer, 0, 32);
					connectionSocket.send(buffer, 32);
					
					// recebe senha
//					inFromClient.read(buffer, 0, 128);
					connectionSocket.receive(buffer, 128);
					String usrPw = BufferMethods.byteArraytoString(buffer, 128);
					
					boolean cadastroOk;
					if (username.length() > 20 || usrSalt.length() > 32 || usrPw.length() > 128) {
//						outToClient.write(3);
						BufferMethods.sendFeedBack(3, connectionSocket);
					} else {
						try {
							cadastroOk = banco.cadastrarUsuario(username, usrSalt, usrPw);
							if (cadastroOk) {
								// conseguiu cadastrar
								System.out.println("Cadastro OK de " + username);
//								outToClient.write(1);
								BufferMethods.sendFeedBack(1, connectionSocket);
							} else {
								// nao conseguiu cadastrar
								System.out.println("Não consegui cadastrar " + username);
//								outToClient.write(0);
								BufferMethods.sendFeedBack(0, connectionSocket);
							}
						} catch (MySQLIntegrityConstraintViolationException e) {
//							outToClient.write(2);
							BufferMethods.sendFeedBack(2, connectionSocket);
						}
					}
					banco.desconectar();
				} else {
					// não conseguiu se conectar ao BD.
					BufferMethods.sendFeedBack(0, connectionSocket);
				}
			}
			connectionSocket.close(true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// nunca deveria dar isto... pls.
			e.printStackTrace();
		} //finally {
//			if (connectionSocket != null) {
//				if (connectionSocket.isClosed() == false) {
//					try {
//						connectionSocket.close(true);
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//		}
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
}
