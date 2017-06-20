package server;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import utility.server.ServerAPI;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import javax.swing.JButton;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Janela do servidor
 * @author Pedro Queiroga <psq@cin.ufpe.br>
 *
 */
public class ServerGUI extends JFrame {

	private JPanel contentPane;
	private JTextField txtPorta;
	ArrayList<String> listaDeUsuarios;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerGUI frame = new ServerGUI();
//					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public ServerGUI() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		listaDeUsuarios = new ArrayList<String>();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 300, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 240, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JLabel lblListaDeUsuarios = new JLabel("Lista de usuários online");
		GridBagConstraints gbc_lblListaDeUsuarios = new GridBagConstraints();
		gbc_lblListaDeUsuarios.gridwidth = 4;
		gbc_lblListaDeUsuarios.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblListaDeUsuarios.insets = new Insets(0, 0, 5, 0);
		gbc_lblListaDeUsuarios.gridx = 0;
		gbc_lblListaDeUsuarios.gridy = 0;
		contentPane.add(lblListaDeUsuarios, gbc_lblListaDeUsuarios);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridheight = 4;
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		JTextPane usuariosTextPane = new JTextPane();
		scrollPane.setViewportView(usuariosTextPane);
		usuariosTextPane.setEditable(false);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		GridBagConstraints gbc_splitPane = new GridBagConstraints();
		gbc_splitPane.anchor = GridBagConstraints.NORTH;
		gbc_splitPane.insets = new Insets(0, 0, 5, 0);
		gbc_splitPane.gridx = 3;
		gbc_splitPane.gridy = 1;
		contentPane.add(splitPane, gbc_splitPane);
		
		txtPorta = new JTextField();
		txtPorta.setToolTipText("A porta onde o servidor deve rodar");
		txtPorta.setText("Porta");
		splitPane.setRightComponent(txtPorta);
		txtPorta.setColumns(2);
		
		JButton btnIniciar = new JButton("Iniciar");
		btnIniciar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					int port = Integer.parseInt(txtPorta.getText());
					(new Thread(new AtualizaLista(listaDeUsuarios, usuariosTextPane))).start();
					(new Thread(new ServidorComeco(listaDeUsuarios, port))).start();
					setTitle("Funcionando na porta " + port);
//					(new Thread(new listTester())).start();
					btnIniciar.setEnabled(false);
					txtPorta.setEnabled(false);
				} catch (NumberFormatException e) {
					txtPorta.setText("Porta");
				}
			}
		});
		splitPane.setLeftComponent(btnIniciar);
		
		txtPorta.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						int port = Integer.parseInt(txtPorta.getText());
						(new Thread(new AtualizaLista(listaDeUsuarios, usuariosTextPane))).start();
						(new Thread(new ServidorComeco(listaDeUsuarios, port))).start();
						setTitle("Funcionando na porta " + port);
//						(new Thread(new listTester())).start();
						btnIniciar.setEnabled(false);
						txtPorta.setEnabled(false);
					} catch (NumberFormatException e) {
						txtPorta.setText("Porta");
					}
				}
			}
		});	
		
		txtPorta.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				try {
					Integer.parseInt(txtPorta.getText());
				} catch (NumberFormatException e) {
					txtPorta.setText("");
				}
			}
		});
		txtPorta.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				try {
					Integer.parseInt(txtPorta.getText());
				} catch (NumberFormatException e) {
					txtPorta.setText("Porta");
				}
			}
		});
		
		this.setVisible(true);
		
		/*
		 *  TODO arrumar esse troço. A ideia é avisar pra todo mundo que o servidor vai ficar fora do ar.
		 *  Provavelmente vamos precisar abrir uma thread no cliente que fica esperando só por esse sinal
		 *  e quando receber, avisa pro cliente com um JDialog e fecha a aplicação qdo ele clicar OK!
		 */
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				synchronized (listaDeUsuarios) {
					for (String str : listaDeUsuarios) {
						try {
							str = str.substring(str.indexOf('(') + 1, str.lastIndexOf(')'));
							String tokens[] = str.split(", ");
							Socket connectionSocket = new Socket(tokens[0], Integer.parseInt(tokens[1]) + 5);
							OutputStream os = connectionSocket.getOutputStream();
							os.write(0);
							connectionSocket.close();
						} catch (Exception e1) {
							System.out.println("Provavelmente não deu para avisar pra (" + str + ") que "
									+ "o servidor vai ficar fora do ar agora.\n" + e1.getClass().getName() + 
									" " + e1.getMessage());
						}
					}
				}
			}
		});
	}
}

/**
 * thread que simula um cliente, para testes apenas.
 * @author Pedro Queiroga <psq@cin.ufpe.br>
 *
 *//*
class listTester implements Runnable {

	@Override
	public void run() {
		Scanner in = new Scanner(System.in);
		String usr, pw;
		int operacao, status;
		ServerAPI servapi = new ServerAPI("localhost", 2020);
		while (true) {
			System.out.println("0 - Cadastro\n"
					+ "1 - Login\n"
					+ "2 - Logout\n"
					+ "3 - Listar onlines\n"
					+ "4 - Solicitar amizade\n"
					+ "5 - Aceitar amizade\n"
					+ "6 - Recusar amizade\n"
					+ "7 - Remover amigo\n"
					+ "8 - Listar amigos online\n"
					+ "9 - Listar solicitações pendentes\n"
					+ "10 - Listar amigos\n");
			operacao = Integer.parseInt(in.nextLine());
			switch (operacao) {
			case 0: // cadastro
				boolean cadastroOK = false;
				System.out.println("Insira usr");
				usr = in.nextLine();
				System.out.println("Insira pw");
				pw = in.nextLine();
				try {
					cadastroOK = servapi.cadastro(usr, pw);
				} catch (IOException | GeneralSecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (cadastroOK) {
					System.out.println("Cadastro efetuado com sucesso");
				} else {
					System.out.println("Nome de usuário indisponível " + usr);
				}
				break;
			case 1: // login
				System.out.println("Insira usr");
				usr = in.nextLine();
				System.out.println("Insira pw");
				pw = in.nextLine();
				Entry<ArrayList<ServerSocket>, Integer> loginTuple = null;
				try {
					loginTuple = servapi.login(usr, pw);
				} catch (GeneralSecurityException | IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				ServerSocket welcomeSocket = null;
				status = -1;
				if (loginTuple != null) {
					status = loginTuple.getValue().intValue();
					if (loginTuple.getKey().size() != 6) {
//						welcomeSocket = null;
					} else {
						for (int i = 0; i < 6; i++) {
							try {
								loginTuple.getKey().get(i).close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
				if (status == 2) {
					System.out.println("Usuário já está online");
				} else if (status == 0) {
					System.out.println("Usuário ou senha incorretos");
				} else if (status == -1) {
					System.out.println("Erro tosco durante login");
				} else if (status == 1) {
					System.out.println("Login efetuado com sucesso");
				}
				break;
			case 2: // logout
				boolean logoutOK = false;
				System.out.println("Insira usr");
				usr = in.nextLine();
				try {
					logoutOK = servapi.logout(usr);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (logoutOK) {
					System.out.println("Logout efetuado com sucesso");
				} else {
					System.out.println("Deu alguma bronca no logout");
				}
				break;
			case 3: // listar onlines
				String str = null;
				try {
					str = AtualizaLista.prettyListToString(servapi.pegaOnlines());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(str);
				break;
			case 4: // solicitar amizade
				System.out.println("Insira usr");
				usr = in.nextLine();
				System.out.println("Insira friend");
				pw = in.nextLine();
				try {
					status = servapi.solicitaAmizade(usr, pw);
					if (status == -1) {
						System.out.println("Servidor não conseguiu se conectar ao BD!");
					} else if (status == 0) {
						System.out.println("pedido n foi efetuado ok por alguma razão que NÃO é relação já existente!");
					} else if (status == 1) {
						System.out.println("OK!");
					} else if (status == 2) {
						System.out.println("já existe uma relação para esses usuários ou que algum dos dois usuários não existe!");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 5: // aceitar amizade
				System.out.println("Insira usr");
				usr = in.nextLine();
				System.out.println("Insira friend");
				pw = in.nextLine();
				try {
					status = servapi.aceitarAmizade(usr, pw);
					if (status == -1) {
						System.out.println("Servidor não conseguiu se conectar ao BD!");
					} else if (status == 0) {
						System.out.println("operação não foi concluída por alguma razão que não é falha na conexão com BD!");
					} else if (status == 1) {
						System.out.println("OK!");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 6: // recusar amizade
				System.out.println("Insira usr");
				usr = in.nextLine();
				System.out.println("Insira friend");
				pw = in.nextLine();
				try {
					status = servapi.recusarAmizade(usr, pw);
					if (status == -1) {
						System.out.println("Servidor não conseguiu se conectar ao BD!");
					} else if (status == 0) {
						System.out.println("operação não foi concluída por alguma razão que não é falha na conexão com BD!");
					} else if (status == 1) {
						System.out.println("OK!");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 7: // remover amigo
				System.out.println("Insira usr");
				usr = in.nextLine();
				System.out.println("Insira friend");
				pw = in.nextLine();
				try {
					status = servapi.removerAmigo(usr, pw);
					if (status == -1) {
						System.out.println("Servidor não conseguiu se conectar ao BD!");
					} else if (status == 0) {
						System.out.println("operação não foi concluída por alguma razão que não é falha na conexão com BD!");
					} else if (status == 1) {
						System.out.println("OK!");
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 8: // listar amigos online
				System.out.println("Insira usr");
				usr = in.nextLine();
				try {
					ArrayList<String> lista = servapi.pegaAmigosOnlines(usr);
					System.out.println(AtualizaLista.prettyListToString(lista));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 9: // listar solicitacoes pendentes
				System.out.println("Insira usr");
				usr = in.nextLine();
				try {
					ArrayList<String> lista = servapi.pegaSolicitacoesPendentes(usr);
					System.out.println(AtualizaLista.prettyListToString(lista));
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			case 10:
				System.out.println("Insira usr");
				usr = in.nextLine();
				try {
					ArrayList<String> lista = servapi.pegaAmigos(usr);
					System.out.println(AtualizaLista.prettyListToString(lista));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}	
}
*/
/**
 * Thread que atualiza a lista de usuarios online na janela do servidor
 * sempre que um usuario entra ou sai.
 * @author Pedro Queiroga <psq@cin.ufpe.br>
 *
 */
class AtualizaLista implements Runnable {
	
	ArrayList<String> listaDeUsuarios;
	JTextPane usuariosTextPane;
	
	public AtualizaLista(ArrayList<String> listaDeUsuarios, JTextPane usuariosTextPane) {
		this.listaDeUsuarios = listaDeUsuarios;
		this.usuariosTextPane = usuariosTextPane;
	}
	@Override
	public void run() {
		while (true) {
			synchronized (listaDeUsuarios) {
				usuariosTextPane.setText(prettyListToString(listaDeUsuarios));
				try {
					// age como uma thread consumidora que deve ser acordada sempre
					// que tiver alguma modificacao na lista de usuarios online.
					listaDeUsuarios.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Pega um ArrayList de Strings e transforma numa String, separando cada elemento
	 * por um line feed.
	 * @param al ArrayList de Strings a ser separado por line feeds.
	 * @return Uma string como explicado acima
	 */
	public static String prettyListToString(ArrayList<String> al) {
		StringBuilder retorno = new StringBuilder();
		Iterator<String> alIterator = al.iterator();
		while (alIterator.hasNext()) {
			retorno.append(alIterator.next()).append("\n");
		}
		return retorno.toString();
	}
}
