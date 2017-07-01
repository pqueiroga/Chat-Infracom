package cliente;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import protocol.DGServerSocket;
import protocol.DGSocket;
import utility.buffer.BufferMethods;

public class Chat extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1864226400918464241L;
	private JPanel contentPane;
	private JTextField txtTypeYourMessage;
	private JTextField txtTypeFilePath;
	private String txtTypeMsg = "Digite uma mensagem";
	private String txtTypeFile = "Caminho do arquivo";
//	private OutputStream outToFriend;
//	private InputStream inFromFriend;
	private DGSocket friendSocket;
	private boolean servicoStatusMsgOk;
	private boolean sentMsg, msgNova;
	private boolean friendOffline;
	private JFrame chatframe;
	private DateFormat dateFormat;
	private DGSocket msgStatusSocket;
	private Thread msgstatusthread;
	private Thread msgrcv;
	private JLabel lblMsgInfo;
	private int[] pktsPerdidos;
	
	/**
	 * Create the frame.
	 */
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new Chat("eu", "ele",null, null, null, null, null, true); //new Socket("localhost", 2030));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public Chat(String usr, String friend, DGSocket friendSocketConstrutor, DGSocket msgStatusSocketConstrutor,
			ArrayList<DGServerSocket> SSList, ArrayList<String> amigos, int[] pktsPerdidos, boolean initVisible) throws IOException {
		setResizable(false);

		this.pktsPerdidos = pktsPerdidos;
		this.servicoStatusMsgOk = true;
		this.dateFormat = new SimpleDateFormat("HH:mm:ss");

		this.sentMsg = false;
		this.msgNova = false;
		this.chatframe = this;
		this.chatframe.setVisible(initVisible);
		setTitle(usr + " conversa com " + friend);
//		this.outToFriend = connectionSocket.getOutputStream();
//		this.inFromFriend = connectionSocket.getInputStream();
		
//		InputStream msgStatusInput = msgStatusSocket.getInputStream();
//		OutputStream msgStatusOutput = msgStatusSocket.getOutputStream();
		this.friendSocket = friendSocketConstrutor;
		this.msgStatusSocket = msgStatusSocketConstrutor;
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 647); //549, 537);
		this.contentPane = new JPanel();
		int randomNum = ThreadLocalRandom.current().nextInt(1, 9 + 1);
		Color cor;
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
		this.contentPane.setBackground(cor);
		this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(this.contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{288, 101, 0};
		gbl_contentPane.rowHeights = new int[]{419, 23, 23, 23, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		this.contentPane.setLayout(gbl_contentPane);
		
		//Pasta de arquivos
		File folder=new File("./download_folder/");
		folder.mkdir();
		//em todas as threads trocar TCP pelo nosso protocolo
		//Thread para envio de mensagens
//		Thread send= new Thread(new SendText(DestinationIP, DestinationPort));
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		this.contentPane.add(scrollPane, gbc_scrollPane);
		JTextPane msgTextPane = new JTextPane();
		msgTextPane.setEditable(false);
		scrollPane.setViewportView(msgTextPane);
		
		StyledDocument docMsg = msgTextPane.getStyledDocument();
		Style styleFrom = msgTextPane.addStyle("FROM", null);
		StyleConstants.setBold(styleFrom, true);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 1;
		gbc_scrollPane_1.gridy = 0;
		this.contentPane.add(scrollPane_1, gbc_scrollPane_1);
		
		JPanel panel = new JPanel();
		scrollPane_1.setViewportView(panel);
		//Thread para receber mensagens
		//lebrar de mudar para nosso protocolo
		msgrcv = new Thread(new ReceiveMessages(friend, docMsg, styleFrom));
		msgrcv.start();
		//Thread para enviar arquivos
		
		//Thread para receber arquivos
		
		
		this.txtTypeYourMessage = new JTextField();
		
		lblMsgInfo = new JLabel("");
		lblMsgInfo.setFont(new Font("Dialog", Font.BOLD, 10));
		lblMsgInfo.setVerticalAlignment(SwingConstants.TOP);
		GridBagConstraints gbc_lblMsgInfo = new GridBagConstraints();
		gbc_lblMsgInfo.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblMsgInfo.insets = new Insets(0, 0, 5, 5);
		gbc_lblMsgInfo.gridx = 0;
		gbc_lblMsgInfo.gridy = 1;
		this.contentPane.add(lblMsgInfo, gbc_lblMsgInfo);
		this.txtTypeYourMessage.setText(this.txtTypeMsg);
		this.txtTypeYourMessage.setForeground(Color.LIGHT_GRAY);
		GridBagConstraints gbc_txtTypeYourMessage = new GridBagConstraints();
		gbc_txtTypeYourMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTypeYourMessage.insets = new Insets(0, 0, 5, 5);
		gbc_txtTypeYourMessage.gridx = 0;
		gbc_txtTypeYourMessage.gridy = 2;
		this.contentPane.add(this.txtTypeYourMessage, gbc_txtTypeYourMessage);
		this.txtTypeYourMessage.setColumns(10);
		
		JButton btnEnviarMsg = new JButton("Enviar");
		GridBagConstraints gbc_btnEnviarMsg = new GridBagConstraints();
		gbc_btnEnviarMsg.fill = GridBagConstraints.BOTH;
		gbc_btnEnviarMsg.insets = new Insets(0, 0, 5, 0);
		gbc_btnEnviarMsg.gridx = 1;
		gbc_btnEnviarMsg.gridy = 2;
		this.contentPane.add(btnEnviarMsg, gbc_btnEnviarMsg);
		
		this.txtTypeFilePath = new JTextField();
		this.txtTypeFilePath.setForeground(Color.LIGHT_GRAY);
		this.txtTypeFilePath.setText(this.txtTypeFile);
		GridBagConstraints gbc_txtTypeFilePath = new GridBagConstraints();
		gbc_txtTypeFilePath.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTypeFilePath.insets = new Insets(0, 0, 0, 5);
		gbc_txtTypeFilePath.gridx = 0;
		gbc_txtTypeFilePath.gridy = 3;
		this.contentPane.add(this.txtTypeFilePath, gbc_txtTypeFilePath);
		this.txtTypeFilePath.setColumns(10);
		
		JButton btnEnviarArquivo = new JButton("Enviar arquivo");
		GridBagConstraints gbc_btnEnviarArquivo = new GridBagConstraints();
		gbc_btnEnviarArquivo.fill = GridBagConstraints.BOTH;
		gbc_btnEnviarArquivo.gridx = 1;
		gbc_btnEnviarArquivo.gridy = 3;
		this.contentPane.add(btnEnviarArquivo, gbc_btnEnviarArquivo);
		
		msgstatusthread = new Thread(new MsgStatusIn(lblMsgInfo));
		msgstatusthread.start();
		
		btnEnviarArquivo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO abrir thread para envio de arquivos
				
			}
		});
		
		this.txtTypeYourMessage.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					if (!(txtTypeYourMessage.getText().isEmpty() || 
							(txtTypeYourMessage.getText().equals(txtTypeMsg) &&
							txtTypeYourMessage.getForeground().equals(Color.LIGHT_GRAY)))) {
						try {
							if (friendOffline) {
								Iterator<String> it = amigos.iterator();
								while (it.hasNext()) {
									String str = it.next();
									int fP = str.indexOf('('), lP = str.lastIndexOf(')');
									if (fP != -1 && lP != -1) {
										String friendIP = str.substring(fP + 1, str.indexOf(','));
										try {
											int friendPort = Integer.parseInt(str.substring(str.indexOf(',') + 2, lP));
											friendSocket = new DGSocket(pktsPerdidos, friendIP, friendPort);
											msgStatusSocket = new DGSocket(pktsPerdidos, friendIP, friendPort +1);
											friendOffline = false;
											msgstatusthread = new Thread(new MsgStatusIn(lblMsgInfo));
											msgrcv = new Thread(new ReceiveMessages(friend, docMsg, styleFrom));
											msgstatusthread.start();
											msgrcv.start();
											BufferMethods.writeString(usr, friendSocket);
										} catch (NumberFormatException e) {
											System.out.println("erro tentando pegar porta");
										}
										break;
									}
								}
							} else {
								BufferMethods.writeChatString(txtTypeYourMessage.getText(), friendSocket);
								synchronized (lblMsgInfo) {
									lblMsgInfo.setForeground(Color.ORANGE);
									lblMsgInfo.setText("mensagem enviada");
									sentMsg = true;
								}
							}
							synchronized (docMsg) {
								docMsg.insertString(docMsg.getLength(), dateFormat.format(new Date(System.currentTimeMillis()))
										+ " <" + usr + ">" + " ", styleFrom);
								docMsg.insertString(docMsg.getLength(), txtTypeYourMessage.getText() + '\n', null);
							}
							txtTypeYourMessage.setText("");
						} catch (SocketException e) {
							lblMsgInfo.setForeground(Color.RED);
							lblMsgInfo.setText(friend + " está offline.");
							friendOffline = true;
						} catch (BadLocationException e) {
							// não deveria dar isto
							e.printStackTrace();
						} catch (Exception e) {
							lblMsgInfo.setForeground(Color.RED);
							lblMsgInfo.setText("mensagem não foi enviada");
							e.printStackTrace();
						}
					}
				}
				if (servicoStatusMsgOk && msgNova) {
					try {
//						msgStatusOutput.write(2);
						BufferMethods.sendFeedBack(2, msgStatusSocket);
						msgNova = false;
					} catch (Exception e1) {
						e1.printStackTrace();
						synchronized (lblMsgInfo) {
							lblMsgInfo.setForeground(Color.RED);
							lblMsgInfo.setText("serviço de status de mensagem quebrou");
							servicoStatusMsgOk = false;
						}
					}
				}
			}
		});
		
		btnEnviarMsg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (!(txtTypeYourMessage.getText().isEmpty() || 
						(txtTypeYourMessage.getText().equals(txtTypeMsg) &&
						txtTypeYourMessage.getForeground().equals(Color.LIGHT_GRAY)))) {
					try {
						if (friendOffline) {
							Iterator<String> it = amigos.iterator();
							while (it.hasNext()) {
								String str = it.next();
								int fP = str.indexOf('('), lP = str.lastIndexOf(')');
								if (fP != -1 && lP != -1) {
									String friendIP = str.substring(fP + 1, str.indexOf(','));
									try {
										int friendPort = Integer.parseInt(str.substring(str.indexOf(',') + 2, lP));
										friendSocket = new DGSocket(pktsPerdidos, friendIP, friendPort);
										msgStatusSocket = new DGSocket(pktsPerdidos, friendIP, friendPort +1);
										friendOffline = false;
										msgstatusthread = new Thread(new MsgStatusIn(lblMsgInfo));
										msgrcv = new Thread(new ReceiveMessages(friend, docMsg, styleFrom));
										msgstatusthread.start();
										msgrcv.start();
										BufferMethods.writeString(usr, friendSocket);
									} catch (NumberFormatException e) {
										System.out.println("erro tentando pegar porta");
									}
									break;
								}
							}
						}
						BufferMethods.writeChatString(txtTypeYourMessage.getText(), friendSocket);
						synchronized (lblMsgInfo) {
							lblMsgInfo.setForeground(Color.ORANGE);
							lblMsgInfo.setText("mensagem enviada");
							sentMsg = true;
						}
						synchronized (docMsg) {
							docMsg.insertString(docMsg.getLength(), dateFormat.format(new Date(System.currentTimeMillis()))
									+ " <" + usr + ">" + " ", styleFrom);
							docMsg.insertString(docMsg.getLength(), txtTypeYourMessage.getText() + '\n', null);
						}
						txtTypeYourMessage.setText("");
					} catch (SocketException e) {
						lblMsgInfo.setForeground(Color.RED);
						lblMsgInfo.setText(friend + " está offline.");
						friendOffline = true;
					} catch (BadLocationException e) {
						// não deveria dar isto
						e.printStackTrace();
					} catch (Exception e) {
						lblMsgInfo.setForeground(Color.RED);
						lblMsgInfo.setText("mensagem não foi enviada");
						e.printStackTrace();
					}
				}
				if (servicoStatusMsgOk && msgNova) {
					try {
//						msgStatusOutput.write(2);
						BufferMethods.sendFeedBack(2, msgStatusSocket);
						msgNova = false;
					} catch (Exception e1) {
						e1.printStackTrace();
						synchronized (lblMsgInfo) {
							lblMsgInfo.setForeground(Color.RED);
							lblMsgInfo.setText("serviço de status de mensagem quebrou");
							servicoStatusMsgOk = false;
						}
					}
				}
			}
		});
		
		this.txtTypeYourMessage.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				if (txtTypeYourMessage.getText().isEmpty()) {
					txtTypeYourMessage.setForeground(Color.LIGHT_GRAY);
					txtTypeYourMessage.setText(txtTypeMsg);
				}
			}
			public void focusGained(FocusEvent e) {
				if (txtTypeYourMessage.getText().isEmpty() || 
						(txtTypeYourMessage.getText().equals(txtTypeMsg) &&
						txtTypeYourMessage.getForeground().equals(Color.LIGHT_GRAY))) {
					txtTypeYourMessage.setText("");
					txtTypeYourMessage.setForeground(Color.BLACK);
				}
				if (servicoStatusMsgOk && msgNova) {
					try {
//						msgStatusOutput.write(2);
						BufferMethods.sendFeedBack(2, msgStatusSocket);
						msgNova = false;
					} catch (IOException e1) {
						e1.printStackTrace();
						synchronized (lblMsgInfo) {
							lblMsgInfo.setForeground(Color.RED);
							lblMsgInfo.setText("serviço de status de mensagem quebrou");
							servicoStatusMsgOk = false;
						}
					}
				}
			}
		});
		
		this.txtTypeFilePath.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				if (txtTypeFilePath.getText().isEmpty()) {
					txtTypeFilePath.setForeground(Color.LIGHT_GRAY);
					txtTypeFilePath.setText(txtTypeFile);
				}
			}
			public void focusGained(FocusEvent e) {
				if (txtTypeFilePath.getText().isEmpty() || 
						(txtTypeFilePath.getText().equals(txtTypeFile) &&
						txtTypeFilePath.getForeground().equals(Color.LIGHT_GRAY))) {
					txtTypeFilePath.setText("");
					txtTypeFilePath.setForeground(Color.BLACK);
				}
				if (servicoStatusMsgOk && msgNova) {
					try {
//						msgStatusOutput.write(2);
						BufferMethods.sendFeedBack(2, msgStatusSocket);
						msgNova = false;
					} catch (IOException e1) {
						e1.printStackTrace();
						synchronized (lblMsgInfo) {
							lblMsgInfo.setForeground(Color.RED);
							lblMsgInfo.setText("serviço de status de mensagem quebrou");
							servicoStatusMsgOk = false;
						}
					}
				}
			}
		});
		
		this.addWindowFocusListener(new WindowAdapter() {
		    public void windowGainedFocus(WindowEvent e) {
		        txtTypeYourMessage.requestFocusInWindow();
		        if (servicoStatusMsgOk && msgNova) {
					try {
//						msgStatusOutput.write(2);
						BufferMethods.sendFeedBack(2, msgStatusSocket);
						msgNova = false;
					} catch (IOException e1) {
						e1.printStackTrace();
						synchronized (lblMsgInfo) {
							lblMsgInfo.setForeground(Color.RED);
							lblMsgInfo.setText("serviço de status de mensagem quebrou");
							servicoStatusMsgOk = false;
						}
					}
				}
		    }
		});
	}
	
	class MsgStatusIn implements Runnable {
		
		private JLabel lblMsgInfo;

		public MsgStatusIn(JLabel lblMsgInfo) {
			this.lblMsgInfo = lblMsgInfo;
		}
		
		public void run() {
			while (true) {
				try { //TODO implementar essas exceções
//					int status = msgStatusInput.read();
					int status = BufferMethods.receiveFeedBack(msgStatusSocket);
					if (status == 1) {
						synchronized (lblMsgInfo) {
							lblMsgInfo.setForeground(Color.YELLOW);
							lblMsgInfo.setText("mensagem recebida");
						}
					} else if (status == 2) {
						if (sentMsg) {
							synchronized (lblMsgInfo) {
								lblMsgInfo.setForeground(Color.GREEN);
								lblMsgInfo.setText("mensagem lida");
							}
						}
					}
				} catch (ConnectException | PortUnreachableException e) {
					e.printStackTrace();
					synchronized (lblMsgInfo) {
						lblMsgInfo.setForeground(Color.RED);
						lblMsgInfo.setText("serviço de status de mensagem quebrou");
					}
					break;
				} catch (IOException e) {
					e.printStackTrace();
					lblMsgInfo.setForeground(Color.RED);
					lblMsgInfo.setText("serviço de status de mensagem quebrou");
					servicoStatusMsgOk = false;
					break;
				} 
			}
		}
	}
	
	class ReceiveMessages implements Runnable {
//		private InputStream inFromFriend;
		private StyledDocument docMsg;
		private String friend;
		private Style styleFrom;
//		private OutputStream msgStatusOutput;
		private boolean finished = false;
		
		public ReceiveMessages(String friend, StyledDocument docMsg, Style styleFrom) {
//			this.inFromFriend = inFromFriend;
			this.docMsg = docMsg;
			this.friend = friend;
			this.styleFrom = styleFrom;
//			this.msgStatusOutput = msgStatusOutput;
		}
		public void run() {
			while (!finished) {
				try {				
					String msg = BufferMethods.readChatString(friendSocket);
					if (!chatframe.isVisible()) {
						chatframe.setVisible(true);
					}
					synchronized (docMsg) {
						docMsg.insertString(docMsg.getLength(), dateFormat.format(new Date(System.currentTimeMillis()))
								+ " <" + friend + ">" + " ", styleFrom);
						docMsg.insertString(docMsg.getLength(), msg + '\n', null);
						msgNova = true;
					}
//					msgStatusOutput.write(1);
					BufferMethods.sendFeedBack(1, msgStatusSocket);

				} catch(Exception e){
					break;
				}
			}
		}
		public void finish() {
			this.finished = true;
		}
	}
}
