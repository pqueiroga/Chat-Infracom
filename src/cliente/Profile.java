package cliente;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import cliente.threads.ReceiveMessages;
import utility.buffer.BufferMethods;
import utility.server.ServerAPI;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.awt.Color;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.Timer;

import cliente.threads.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.ScrollPane;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

public class Profile extends JFrame {

	private JPanel contentPane;
	private JPanel panel;
	private int delay;
	private int servNResponde;
	private Profile frame;
	private boolean noServer;
	private String friendIP;
	private String friendname;
	private int friendPort;
	private JDialog addRemoveAmigo;
	/**
	 * Create the frame.
	 */
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Profile frame = new Profile(new ArrayList<ServerSocket>(), "QQQQQQQQQQQQQQQQQQQ", "localhost", 2020);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	// TODO lembrar de mudar para nosso protocolo
	public Profile(ArrayList<ServerSocket> listenList, String username, String ip, int port) {
		setResizable(false);
		this.delay = 5000;
		this.noServer = false;
		this.servNResponde = 0;
		this.frame = this;
		this.addRemoveAmigo = new AddRemoveAmigoDialog(username, ip, port);
		this.addRemoveAmigo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		ServerAPI toServer = new ServerAPI(ip, port);
		
		int randomNum = ThreadLocalRandom.current().nextInt(3, 9 + 1);
		Color cor;
		switch (randomNum) {
		case 3:
			cor = Color.BLUE;
			break;
		case 4:
			cor = Color.CYAN;
			break;
		case 5:
			cor = Color.GRAY;
			break;
		case 6:
			cor = Color.LIGHT_GRAY;
		case 7:
			cor = Color.MAGENTA;
			break;
		case 8:
			cor = Color.PINK;
			break;
		case 9:
			cor = Color.WHITE;
			break;
		default:
			cor = null;	
		}
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 647);
		contentPane = new JPanel();
		contentPane.setBackground(cor);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{186, 55, 55, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JLabel lblNomeDeUsurio = new JLabel(username);
		lblNomeDeUsurio.setFont(new Font("Dialog", Font.BOLD, 20));
		GridBagConstraints gbc_lblNomeDeUsurio = new GridBagConstraints();
		gbc_lblNomeDeUsurio.gridwidth = 3;
		gbc_lblNomeDeUsurio.insets = new Insets(0, 0, 5, 5);
		gbc_lblNomeDeUsurio.anchor = GridBagConstraints.WEST;
		gbc_lblNomeDeUsurio.gridx = 0;
		gbc_lblNomeDeUsurio.gridy = 0;
		contentPane.add(lblNomeDeUsurio, gbc_lblNomeDeUsurio);
		
		JLabel lblListaDeAmigos = new JLabel("Lista de amigos");
		GridBagConstraints gbc_lblListaDeAmigos = new GridBagConstraints();
		gbc_lblListaDeAmigos.insets = new Insets(0, 0, 5, 5);
		gbc_lblListaDeAmigos.anchor = GridBagConstraints.WEST;
		gbc_lblListaDeAmigos.gridx = 0;
		gbc_lblListaDeAmigos.gridy = 1;
		contentPane.add(lblListaDeAmigos, gbc_lblListaDeAmigos);
		

		GridBagConstraints gbc_btnAddFriend = new GridBagConstraints();
		gbc_btnAddFriend.gridwidth = 2;
		gbc_btnAddFriend.insets = new Insets(0, 0, 5, 0);
		gbc_btnAddFriend.gridx = 1;
		gbc_btnAddFriend.gridy = 1;
		JButton btnAddRemoveFriend = new JButton("Adicionar ou remover amigo");
		contentPane.add(btnAddRemoveFriend, gbc_btnAddFriend);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 3;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 2;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		randomNum = ThreadLocalRandom.current().nextInt(1, 9 + 1);
		switch (randomNum) {
		case 1:
			cor = Color.BLACK;
			break;
		case 2:
			cor = Color.BLUE;
			break;
		case 3:
			cor = Color.CYAN;
			break;
		case 4:
			cor = Color.DARK_GRAY;
			break;
		case 5:
			cor = Color.GRAY;
			break;
		case 6:
			cor = Color.LIGHT_GRAY;
		case 7:
			cor = Color.MAGENTA;
			break;
		case 8:
			cor = Color.PINK;
			break;
		case 9:
			cor = Color.WHITE;
			break;
		default:
			cor = null;	
		}
		
		panel = new JPanel();
		panel.setBackground(cor);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		scrollPane.setViewportView(panel);
		
		JLabel lblConectividadeComServidor = new JLabel("Conectividade com servidor");
		lblConectividadeComServidor.setForeground(Color.GREEN);
		GridBagConstraints gbc_lblConectividadeComServidor = new GridBagConstraints();
		gbc_lblConectividadeComServidor.anchor = GridBagConstraints.WEST;
		gbc_lblConectividadeComServidor.insets = new Insets(0, 0, 0, 5);
		gbc_lblConectividadeComServidor.gridx = 0;
		gbc_lblConectividadeComServidor.gridy = 3;
		contentPane.add(lblConectividadeComServidor, gbc_lblConectividadeComServidor);
		
		JLabel lblPacotesPerdidos = new JLabel("0");
		lblPacotesPerdidos.setToolTipText("Pacotes perdidos");
		lblPacotesPerdidos.setForeground(Color.GREEN);
		GridBagConstraints gbc_lblPacotesPerdidos = new GridBagConstraints();
		gbc_lblPacotesPerdidos.anchor = GridBagConstraints.EAST;
		gbc_lblPacotesPerdidos.gridx = 2;
		gbc_lblPacotesPerdidos.gridy = 3;
		contentPane.add(lblPacotesPerdidos, gbc_lblPacotesPerdidos);
		
		try {
			ArrayList<String> amigos = toServer.pegaAmigos(username);
			for (String str : amigos) {
				JButton btn = new JButton(str);
				int fP = str.indexOf('('), lP = str.lastIndexOf(')');
				if (fP == -1 || lP == -1) {
					btn.setEnabled(false);
				} else {
					friendIP = str.substring(fP + 1, str.indexOf(','));
					friendname = str.substring(0, fP - 1);
					try {
						friendPort = Integer.parseInt(str.substring(str.indexOf(',') + 2, lP));
					} catch (NumberFormatException e) {
						System.out.println("erro tentando pegar porta");
					}
				}
				btn.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e1) {
						if (!friendIP.isEmpty()) {
							Chat chat = null;
							try {
								Socket connectionSocket = new Socket(friendIP, friendPort);
								BufferMethods.writeString(username, connectionSocket.getOutputStream());
								Socket msgStatusSocket = new Socket(friendIP, friendPort +1);
								chat = new Chat(username, friendname, connectionSocket, msgStatusSocket, 
										listenList);
								chat.setVisible(true);
							} catch (IOException e) {
							    JOptionPane.showMessageDialog(frame, "Não foi possível alcançar " + friendname, "Erro",
							            JOptionPane.WARNING_MESSAGE);
								e.printStackTrace();
							}
						}
					}
				});
				panel.add(btn);
			}
			panel.validate();
			lblConectividadeComServidor.setText("Servidor OK");
		} catch (ConnectException e) {
			servNResponde++;
			System.out.println("Servidor não respondeu, tentando reconectar...");
			lblConectividadeComServidor.setForeground(Color.YELLOW);
			lblConectividadeComServidor.setText("Tentando contatar servidor");
		} catch (IOException e) {
			servNResponde++;
			System.out.println("Servidor não respondeu, tentando reconectar...");
			lblConectividadeComServidor.setForeground(Color.YELLOW);
			lblConectividadeComServidor.setText("Tentando contatar servidor");
			e.printStackTrace();
		}
		Thread espconv = new Thread(new esperaConversas(username, listenList));
		espconv.start();
		ActionListener updatesFL = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (!noServer) {
			    	ServerAPI toServerConc = new ServerAPI(ip, port);
			    	try {
						ArrayList<String> pendentes = toServerConc.pegaSolicitacoesPendentes(username);
						for (String str : pendentes) {
							String[] opcoes = {"Sim", "Não"};
							int yesno = JOptionPane.showOptionDialog(frame, str +" pediu para ser seu amigo(a)."
									+ " Deseja aceitar?", "Solicitação de amizade",JOptionPane.YES_NO_OPTION,
									JOptionPane.PLAIN_MESSAGE, null, opcoes, opcoes[0]);
							if (yesno == JOptionPane.YES_OPTION) {
								try {
									int status = toServerConc.aceitarAmizade(username, str);
									if (status == -1) { // erro no BD
									    JOptionPane.showMessageDialog(frame, "Não foi possível acessar o banco de dados", "Erro",
									            JOptionPane.WARNING_MESSAGE);
									} else if (status == 0) { // erro sem ser no BD
									    JOptionPane.showMessageDialog(frame, "Não foi possível aceitar " + str, "Erro",
									            JOptionPane.WARNING_MESSAGE);
									} else if (status == 1) { // OK
										// não faz nada
									}
								} catch (IOException e) {
								    JOptionPane.showMessageDialog(frame, "Não foi possível aceitar " + str, "Erro",
								            JOptionPane.WARNING_MESSAGE);
									e.printStackTrace();
								}
							} else { 
								try {
									int status = toServerConc.recusarAmizade(username, str);
									if (status == -1) { // erro no BD
									    JOptionPane.showMessageDialog(frame, "Não foi possível acessar o banco de dados", "Erro",
									            JOptionPane.WARNING_MESSAGE);
									} else if (status == 0) { // erro sem ser no BD
									    JOptionPane.showMessageDialog(frame, "Não foi possível recusar " + str, "Erro",
									            JOptionPane.WARNING_MESSAGE);
									} else if (status == 1) { // OK
										// não faz nada
									}
								} catch (IOException e) {
								    JOptionPane.showMessageDialog(frame, "Não foi possível recusar " + str, "Erro",
								            JOptionPane.WARNING_MESSAGE);
									e.printStackTrace();
								}
							}
														
						}
					} catch (IOException e) {			
						e.printStackTrace();
					}
					panel.removeAll();
					panel.repaint();
					try {
						ArrayList<String> amigos = toServerConc.pegaAmigos(username);
						for (String str : amigos) {
							JButton btn = new JButton(str);
							int fP = str.indexOf('('), lP = str.lastIndexOf(')');
							if (fP == -1 || lP == -1) {
								btn.setEnabled(false);
							} else {
								friendIP = str.substring(fP + 1, str.indexOf(','));
								friendname = str.substring(0, fP - 1);
								try {
									friendPort = Integer.parseInt(str.substring(str.indexOf(',') + 2, lP));
								} catch (NumberFormatException e) {
									System.out.println("erro tentando pegar porta");
								}
							}
							btn.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e1) {
									if (!friendIP.isEmpty()) {
										Chat chat;
										try {
											Socket connectionSocket = new Socket(friendIP, friendPort);
											BufferMethods.writeString(username, connectionSocket.getOutputStream());
											Socket msgStatusSocket = new Socket(friendIP, friendPort +1);
											chat = new Chat(username, friendname, connectionSocket, msgStatusSocket, 
													listenList);
											chat.setVisible(true);
										} catch (IOException e) {
											
											e.printStackTrace();
										}
									}
								}
							});
							panel.add(btn);
						}
						panel.validate();
					} catch (ConnectException e) {
						if (servNResponde < 5) {
							servNResponde++;
							lblConectividadeComServidor.setForeground(Color.YELLOW);
							lblConectividadeComServidor.setText("Tentando contatar servidor");
						} else {
							String[] opcoes = {"Sim", "Não"};
							int yesno = JOptionPane.showOptionDialog(frame, "Servidor aparentemente offline."
									+ " Deseja fechar o programa?", "Servidor irresponsivo",JOptionPane.YES_NO_OPTION,
									JOptionPane.PLAIN_MESSAGE, null, opcoes, opcoes[0]);
							if (yesno == JOptionPane.YES_OPTION) {
								frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
							} else { 
								noServer = true;
								lblConectividadeComServidor.setForeground(Color.RED);
								lblConectividadeComServidor.setText("Não conectado ao servidor");
							}
						}
					} catch (IOException e) {
						if (servNResponde < 5) {
							servNResponde++;
							lblConectividadeComServidor.setForeground(Color.YELLOW);
							lblConectividadeComServidor.setText("Tentando contatar servidor");
						} else {
							String[] opcoes = {"Sim", "Não"};
							int yesno = JOptionPane.showOptionDialog(frame, "Servidor aparentemente offline."
									+ " Deseja fechar o programa?", "Servidor irresponsivo",JOptionPane.YES_NO_OPTION,
									JOptionPane.PLAIN_MESSAGE, null, opcoes, opcoes[0]);
							if (yesno == JOptionPane.YES_OPTION) {
								frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
							} else { 
								noServer = true;
								lblConectividadeComServidor.setForeground(Color.RED);
								lblConectividadeComServidor.setText("Não conectado ao servidor");
							}
						}			
						e.printStackTrace();
					} 
				}
			}
		};
		new Timer(delay, updatesFL).start();
		
//		ActionListener checaPendentes = new ActionListener() {
//			public void actionPerformed(ActionEvent evt) {
//				if (!noServer) {
//			    	ServerAPI toServerConc = new ServerAPI(ip, port);
//					
//					try {
//						ArrayList<String> amigos = toServerConc.pegaAmigos(username);
//						panel.removeAll();
//						panel.repaint();
//						for (String str : amigos) {
//							JButton btn = new JButton(str);
//							int fP = str.indexOf('('), lP = str.lastIndexOf(')');
//							if (fP == -1 || lP == -1) {
//								btn.setEnabled(false);
//							} else {
//								friendIP = str.substring(fP + 1, str.indexOf(','));
//								friendname = str.substring(0, fP - 1);
//								try {
//									friendPort = Integer.parseInt(str.substring(str.indexOf(',') + 2, lP));
//								} catch (NumberFormatException e) {
//									System.out.println("erro tentando pegar porta");
//								}
//							}
//							btn.addActionListener(new ActionListener() {
//								public void actionPerformed(ActionEvent e1) {
//									if (!friendIP.isEmpty()) {
//										Chat chat = null;
//										try {
//											Socket connectionSocket = new Socket(friendIP, friendPort);
//											BufferMethods.writeString(username, connectionSocket.getOutputStream());
//											Socket msgStatusSocket = new Socket(friendIP, friendPort +1);
//											chat = new Chat(username, friendname, connectionSocket, msgStatusSocket, 
//													listenList);
//											chat.setVisible(true);
//										} catch (IOException e) {
//										    JOptionPane.showMessageDialog(frame, "Não foi possível alcançar " + friendname, "Erro",
//										            JOptionPane.WARNING_MESSAGE);
//											e.printStackTrace();
//										}
//									}
//								}
//							});
//							panel.add(btn);
//						}
//						panel.validate();
//						lblConectividadeComServidor.setText("Servidor OK");
//					} catch (ConnectException e) {
//						servNResponde++;
//						System.out.println("Servidor não respondeu, tentando reconectar...");
//						lblConectividadeComServidor.setForeground(Color.YELLOW);
//						lblConectividadeComServidor.setText("Tentando contatar servidor");
//					} catch (IOException e) {
//						servNResponde++;
//						System.out.println("Servidor não respondeu, tentando reconectar...");
//						lblConectividadeComServidor.setForeground(Color.YELLOW);
//						lblConectividadeComServidor.setText("Tentando contatar servidor");
//						e.printStackTrace();
//					}
//				}
//			}
//		};
//		new Timer(delay, checaPendentes).start();
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				try {
					toServer.logout(username);
				} catch (IOException e) {
					System.out.println("Não foi possível deslogar");
				}
				for (ServerSocket ss : listenList) {
					if (ss != null) {
						if (!ss.isClosed()) {
							try {
								ss.close();
							} catch (IOException e) {
								System.out.println("Não consegui fechar " + ss.toString());
							}
						}
					}
				}
				espconv.interrupt();
			}
		});
		
		btnAddRemoveFriend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addRemoveAmigo.setVisible(true);			
			}
		});
	}
	
	class esperaConversas implements Runnable {
		
		private ArrayList<ServerSocket> listenList;
		private String username;

		public esperaConversas(String username, ArrayList<ServerSocket> listenList) {
			this.username = username;
			this.listenList = listenList;
		}
		
		public void run() {
			Chat chat;
			while (true) {
				Socket connectionSocket = null;
				try {
					connectionSocket = listenList.get(0).accept();
					String friendname = BufferMethods.readString(connectionSocket.getInputStream());
					Socket msgStatusSocket = listenList.get(1).accept();
					chat = new Chat(username, friendname, connectionSocket, msgStatusSocket, listenList);
					chat.setVisible(true);
				} catch (SocketException e) {
					if (connectionSocket != null) {
						try {
							connectionSocket.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					e.printStackTrace();
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
