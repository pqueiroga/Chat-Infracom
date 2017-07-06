package cliente;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;

import protocol.DGServerSocket;
import protocol.DGSocket;
import utility.buffer.BufferMethods;
import utility.server.ServerAPI;
import javax.swing.JSlider;

public class Profile extends JFrame implements ChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3600369938906919072L;
	private JPanel contentPane;
	private JPanel panel;
	private int delay = 5000;
	private Profile frame;
	private boolean noServer;
	private String friendIP;
	private String friendname;
	private int friendPort;
	private JDialog addRemoveAmigo;
	private String contatandoServStr;
	private ArrayList<String> amigos;
	private String ip;
	private int port;
	private String username;
	private DGServerSocket listenSocket;
	private JLabel lblConectividadeComServidor;
	private JButton btnAddRemoveFriend;
	private JLabel lblPacotesPerdidos;
	
	private double pDescartaPacotes = 0;
	
	private ConcurrentHashMap<String, Chat> chats;
	
	private int[] pktsPerdidos = new int[1];
	private JSlider slider;
	/**
	 * Create the frame.
	 */
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Profile frame = new Profile(null, "QQQQQQQQQQQQQQQQQQQ", "localhost", 2020);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	public Profile(DGServerSocket listenSocket, String username, String ip, int port) {
		setResizable(false);
		setTitle(username);
		this.chats = new ConcurrentHashMap<String, Chat>();
		this.listenSocket = listenSocket;
		this.username = username;
		this.ip = ip;
		this.port = port;
		this.noServer = false;
		this.frame = this;
		this.addRemoveAmigo = new AddRemoveAmigoDialog(pktsPerdidos, username, ip, port);
		this.addRemoveAmigo.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.contatandoServStr = "Tentando contatar servidor";
		ServerAPI toServer = new ServerAPI(pDescartaPacotes, pktsPerdidos, ip, port);
		
		this.amigos = new ArrayList<String>();
		
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
			break;
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
		gbl_contentPane.columnWidths = new int[]{186, 156, 55, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JLabel lblNomeDeUsurio = new JLabel(username);
		lblNomeDeUsurio.setFont(new Font("Dialog", Font.BOLD, 20));
		GridBagConstraints gbc_lblNomeDeUsurio = new GridBagConstraints();
		gbc_lblNomeDeUsurio.gridwidth = 3;
		gbc_lblNomeDeUsurio.insets = new Insets(0, 0, 5, 0);
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
		btnAddRemoveFriend = new JButton("Adicionar ou remover amigo");
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
			break;
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
		
		File folder = new File("Download_Dump" + File.separator);
		folder.mkdir();
		File folder2 = new File("historicos_" + username + File.separator);
		folder2.mkdir();
		
		panel = new JPanel();
		panel.setBackground(cor);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		scrollPane.setViewportView(panel);
		
		lblConectividadeComServidor = new JLabel("Conectividade com servidor");
		lblConectividadeComServidor.setForeground(Color.GREEN);
		GridBagConstraints gbc_lblConectividadeComServidor = new GridBagConstraints();
		gbc_lblConectividadeComServidor.anchor = GridBagConstraints.WEST;
		gbc_lblConectividadeComServidor.insets = new Insets(0, 0, 0, 5);
		gbc_lblConectividadeComServidor.gridx = 0;
		gbc_lblConectividadeComServidor.gridy = 3;
		contentPane.add(lblConectividadeComServidor, gbc_lblConectividadeComServidor);
		
		slider = new JSlider();
		slider.setMinorTickSpacing(5);
		slider.setMajorTickSpacing(10);
		slider.setPaintTicks(true);
		slider.setMaximum(50);
		slider.setToolTipText("0~5% de perda forçada de pacotes");
		slider.setValue(0);
		GridBagConstraints gbc_slider = new GridBagConstraints();
		gbc_slider.fill = GridBagConstraints.HORIZONTAL;
		gbc_slider.insets = new Insets(0, 0, 0, 5);
		gbc_slider.gridx = 1;
		gbc_slider.gridy = 3;
		contentPane.add(slider, gbc_slider);
				
		lblPacotesPerdidos = new JLabel("0");
		lblPacotesPerdidos.setToolTipText("Pacotes perdidos");
		lblPacotesPerdidos.setForeground(Color.GREEN);
		GridBagConstraints gbc_lblPacotesPerdidos = new GridBagConstraints();
		gbc_lblPacotesPerdidos.anchor = GridBagConstraints.EAST;
		gbc_lblPacotesPerdidos.gridx = 2;
		gbc_lblPacotesPerdidos.gridy = 3;
		contentPane.add(lblPacotesPerdidos, gbc_lblPacotesPerdidos);
		
//		Thread espconv = new Thread(new esperaConversas(username, listenSocket));
//		espconv.start();
		Thread espconex = new Thread(new EsperaConexao());
		espconex.start();
		Thread tUpdatesFL = new Thread(new updatesFL());
		tUpdatesFL.start();
		Thread tAtualizaPktsPerdidos = new Thread(new atualizaPktsPerdidos());
		tAtualizaPktsPerdidos.start();
//		Thread tEsperaArquivo = new Thread(new EsperaArquivos());
//		tEsperaArquivo.start();
		
		slider.addChangeListener(this);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				if (!noServer) {
					try {
						toServer.logout(username);
					} catch (IOException e) {
						System.out.println("Não foi possível deslogar");
					}
				}
//				for (DGServerSocket ss : listenSocket) {
					if (listenSocket != null) {
						if (!listenSocket.isClosed()) {
							try {
								listenSocket.close();
								System.out.println("Fechei " + listenSocket.toString());
							} catch (IOException e) {
								System.out.println("Não consegui fechar " + listenSocket.toString());
							}
						}
					}
//				}
				tUpdatesFL.interrupt();
//				espconv.interrupt();
				espconex.interrupt();
				tAtualizaPktsPerdidos.interrupt();
//				tEsperaArquivo.interrupt();
				
				for (Entry<String, Chat> xat : chats.entrySet()) {
					File file = new File("historicos_" + username + File.separator + xat.getKey());
					try {
						file.createNewFile();
						try (PrintWriter pw = new PrintWriter(file)) {
							pw.print(xat.getValue().getDocMsg());
						} catch (BadLocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				System.exit(0);
			}
		});
		
		btnAddRemoveFriend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addRemoveAmigo.setVisible(true);			
			}
		});
		
		setVisible(true);
	}
	
	class updatesFL implements Runnable {

		@Override
		public void run() {
			while (true) {
		    	ServerAPI toServerConc = new ServerAPI(pDescartaPacotes, pktsPerdidos, ip, port);
		    	try {
					ArrayList<String> pendentes = toServerConc.pegaSolicitacoesPendentes(username);
					if (lblConectividadeComServidor.getText().equals(contatandoServStr)) {
						try {
							int loginstatus = toServerConc.login(username, listenSocket.getLocalPort());
							if (loginstatus == 2) {
								toServerConc.logout(username);
								toServerConc.login(username, listenSocket.getLocalPort());
								lblConectividadeComServidor.setForeground(Color.GREEN);
								lblConectividadeComServidor.setText("Servidor OK");
								btnAddRemoveFriend.setEnabled(true);
								noServer = false;
							}
						} catch (GeneralSecurityException e) {
							// nunca deveria dar
							e.printStackTrace();
						}
					}
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
					lblConectividadeComServidor.setForeground(Color.GREEN);
					lblConectividadeComServidor.setText("Servidor OK");
					btnAddRemoveFriend.setEnabled(true);
					noServer = false;
				} catch (Exception e) {
					btnAddRemoveFriend.setEnabled(false);
					noServer = true;
					lblConectividadeComServidor.setForeground(Color.YELLOW);
					lblConectividadeComServidor.setText(contatandoServStr);
				}
				try {
					amigos.clear();
					ArrayList<String> temp = toServerConc.pegaAmigos(username);
					for (String gambiarra : temp) {
						amigos.add(gambiarra);
					}
					if (lblConectividadeComServidor.getText().equals(contatandoServStr)) {
						try {
							int loginstatus = toServerConc.login(username, listenSocket.getLocalPort());
							if (loginstatus == 2) {
								toServerConc.logout(username);
								toServerConc.login(username, listenSocket.getLocalPort());
								lblConectividadeComServidor.setForeground(Color.GREEN);
								lblConectividadeComServidor.setText("Servidor OK");
								btnAddRemoveFriend.setEnabled(true);
								noServer = false;
							}
						} catch (GeneralSecurityException e) {
							// nunca deveria dar
							e.printStackTrace();
						}
					}
					panel.removeAll();
					panel.repaint();
					Iterator<String> it = amigos.iterator();
					while (it.hasNext()) {
						String str = it.next();
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
						String friendIPLocal = friendIP;
						int friendPortLocal = friendPort;
						String friendnameLocal = friendname;
						btn.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e1) {
								if (!friendIP.isEmpty()) {
									try {
										if (chats.containsKey(friendnameLocal)) {
											chats.get(friendnameLocal).setVisible(true);
										} else {
											DGSocket connectionSocket = new DGSocket(pDescartaPacotes, pktsPerdidos, friendIPLocal, friendPortLocal);
											BufferMethods.writeString(username, connectionSocket);
											BufferMethods.sendInt(0, connectionSocket);
											DGSocket msgStatusSocket = new DGSocket(pDescartaPacotes, pktsPerdidos, friendIPLocal, friendPortLocal);
											Chat novoChat = new Chat(username, friendnameLocal, connectionSocket,
													msgStatusSocket, amigos, pktsPerdidos, pDescartaPacotes, true);
											chats.put(friendnameLocal, novoChat);
										}
									} catch (Exception e) {
										e.printStackTrace();
									    JOptionPane.showMessageDialog(frame, "Não foi possível alcançar " + friendnameLocal +
									    		", " + friendIPLocal + ", " + friendPortLocal, "Erro",
									            JOptionPane.WARNING_MESSAGE);
									}
								}
							}
						});
						panel.add(btn);
					}
					panel.validate();
					lblConectividadeComServidor.setForeground(Color.GREEN);
					lblConectividadeComServidor.setText("Servidor OK");
					btnAddRemoveFriend.setEnabled(true);
					noServer = false;
				} catch (Exception e) {
					btnAddRemoveFriend.setEnabled(false);
					noServer = true;
					lblConectividadeComServidor.setForeground(Color.YELLOW);
					lblConectividadeComServidor.setText(contatandoServStr);			
					e.printStackTrace();
				} 
				try {
					Thread.sleep(delay);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	class atualizaPktsPerdidos implements Runnable {
		public void run() {
			while (true) {
				synchronized (pktsPerdidos) {
					lblPacotesPerdidos.setText(pktsPerdidos[0] + "");
					if (pktsPerdidos[0] > 100) {
						lblPacotesPerdidos.setForeground(Color.RED);
					} else if (pktsPerdidos[0] > 50) {
						lblPacotesPerdidos.setForeground(Color.YELLOW);
					}
					try {
						pktsPerdidos.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	class EsperaConexao implements Runnable {
		public void run() {
			while (true) {
				DGSocket connectionSocket = null;
				try {
					long[] estimatedRTT = {-1};
					connectionSocket = listenSocket.accept(estimatedRTT, pDescartaPacotes, pktsPerdidos);
					String friendname = BufferMethods.readString(connectionSocket);
					int opcode = BufferMethods.receiveInt(connectionSocket);
					if (opcode == 0) {
						DGSocket msgStatusSocket = listenSocket.accept(pDescartaPacotes, pktsPerdidos);
						if (chats.containsKey(friendname)) {
							chats.get(friendname).setStuff(connectionSocket, msgStatusSocket);
						} else {
							Chat novoChat = new Chat(username, friendname, connectionSocket, msgStatusSocket,
									amigos, pktsPerdidos, pDescartaPacotes, false);
							chats.put(friendname, novoChat);
						}
					} else if (opcode == 1) {
						if (chats.containsKey(friendname)) {
							chats.get(friendname).setVisible(true);
							chats.get(friendname).comecaBaixaArquivos(connectionSocket, estimatedRTT);
						} else {
							connectionSocket.close(true);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		JSlider source = (JSlider) arg0.getSource();
		if (!source.getValueIsAdjusting()) {
			pDescartaPacotes = source.getValue() / 1000.00;
			System.out.println("pDescartaPacotes em profile: " + pDescartaPacotes);
			source.setToolTipText("" + (pDescartaPacotes * 100) + "% de perda forçada de pacotes");
			for (Entry<String, Chat> xat : chats.entrySet()) {
				xat.getValue().setpDescartaPacotes(pDescartaPacotes);
			}
		}
	}
}
