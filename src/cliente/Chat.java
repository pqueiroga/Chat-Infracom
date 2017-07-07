package cliente;
import java.awt.Color;
import java.awt.Desktop;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import protocol.DGSocket;
import utility.buffer.BufferMethods;

public class Chat extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1864226400918464241L;
	private JPanel contentPane;
	private JTextArea txtTypeYourMessage;
	private String txtTypeMsg = "Digite uma mensagem";

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
	private int[] pktsPerdidos; // ponteiro importante
	
	private ConcurrentHashMap<String, DownloadPainel> downloads;
	
	private int friendPort;
	private String friendIP;
	
	private String meUserName;
	
	private ArrayList<String> amigos;
	
	private StyledDocument docMsg;
	private Style styleFrom;
	private String friendName;
	
	private JPanel downloadPanel;
	private JButton openButton;
    private JFileChooser fc;
    private JLabel lblEasteregg;
    private JScrollPane txtTypeScrollPane;
    private JScrollPane scrollPane;
    private JTextPane msgTextPane;
    
    private double pDescartaPacotes;
    /**
	 * Create the frame.
	 */
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new Chat("eu", "ele", null, null, null, null, 0, true); //new Socket("localhost", 2030));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public Chat(String usr, String friend, DGSocket friendSocketConstrutor,
			DGSocket msgStatusSocketConstrutor,	ArrayList<String> amigos,
			int[] pktsPerdidos, double pDescartaPacotes, boolean initVisible) {
		setResizable(false);
		
		this.downloads = new ConcurrentHashMap<String, DownloadPainel>();
		
		this.pDescartaPacotes = pDescartaPacotes;
		
		this.friendName = friend;
		
		this.amigos = amigos;
		this.meUserName = usr;
		
		for (int i = 0; i < 3; i++) {
			try {
				String[] fetched = fetchFriendInfo();
				if (fetched != null) {
					if (fetched[1] != null && Integer.parseInt(fetched[2]) != 0) {
						friendIP = fetched[1];
						friendPort = Integer.parseInt(fetched[2]);
						break;
					}
				} 
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		if (friendIP == null || friendPort == 0) {
			System.err.println("não foi possível pegar o ip ou porta no firstfetch");
		}
		
		this.pktsPerdidos = pktsPerdidos;
		this.servicoStatusMsgOk = true;
		this.dateFormat = new SimpleDateFormat("HH:mm:ss");
		
		fc = new JFileChooser();
		openButton = new JButton("Escolher arquivo");
		openButton.setToolTipText("Escolher arquivo para enviar pro amigo");

		this.sentMsg = false;
		this.msgNova = false;
		this.chatframe = this;
		setTitle(usr + " conversa com " + friend);
		
		this.friendSocket = friendSocketConstrutor;
		this.msgStatusSocket = msgStatusSocketConstrutor;
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 647);
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
		
		scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		this.contentPane.add(scrollPane, gbc_scrollPane);
		msgTextPane = new JTextPane();
		msgTextPane.setToolTipText("Área de texto para conversas");
		try {
			File file = new File("historicos_" + usr + File.separator + friend);
			byte[] encoded = Files.readAllBytes(Paths.get(file.getAbsolutePath()));
			msgTextPane.setForeground(Color.GRAY);
			msgTextPane.setText(new String(encoded, "UTF-8"));
			msgTextPane.setForeground(Color.BLACK);
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		
		msgTextPane.setEditable(false);
		scrollPane.setViewportView(msgTextPane);
		
		docMsg = msgTextPane.getStyledDocument();
		styleFrom = msgTextPane.addStyle("FROM", null);
		StyleConstants.setBold(styleFrom, true);
		
		JScrollPane scrollPane_1 = new JScrollPane(
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
	            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_1.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_1.gridx = 1;
		gbc_scrollPane_1.gridy = 0;
		this.contentPane.add(scrollPane_1, gbc_scrollPane_1);
		
		downloadPanel = new JPanel();
		downloadPanel.setToolTipText("Área de downloads e uploads");
		downloadPanel.setLayout(new BoxLayout(downloadPanel, BoxLayout.Y_AXIS));
		scrollPane_1.setViewportView(downloadPanel);
		//Thread para receber mensagens
		msgrcv = new Thread(new ReceiveMessages());
		msgrcv.start();		
		
		this.txtTypeYourMessage = new JTextArea();
		txtTypeYourMessage.setToolTipText("Digite uma mensagem aqui...");
		this.txtTypeYourMessage.setLineWrap(true);
		this.txtTypeYourMessage.setWrapStyleWord(true);
		txtTypeScrollPane = new JScrollPane(this.txtTypeYourMessage);
		
		lblMsgInfo = new JLabel("");
		lblMsgInfo.setToolTipText("Estado da comunicação entre vocês");
		lblMsgInfo.setFont(new Font("Dialog", Font.BOLD, 10));
		lblMsgInfo.setVerticalAlignment(SwingConstants.TOP);
		GridBagConstraints gbc_lblMsgInfo = new GridBagConstraints();
		gbc_lblMsgInfo.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblMsgInfo.insets = new Insets(0, 0, 5, 5);
		gbc_lblMsgInfo.gridx = 0;
		gbc_lblMsgInfo.gridy = 1;
		this.contentPane.add(lblMsgInfo, gbc_lblMsgInfo);
		
		lblEasteregg = new JLabel("easter egg");
		GridBagConstraints gbc_lblEasteregg = new GridBagConstraints();
		gbc_lblEasteregg.insets = new Insets(0, 0, 5, 0);
		gbc_lblEasteregg.gridx = 1;
		gbc_lblEasteregg.gridy = 1;
		contentPane.add(lblEasteregg, gbc_lblEasteregg);
		this.txtTypeYourMessage.setText(this.txtTypeMsg);
		this.txtTypeYourMessage.setForeground(Color.LIGHT_GRAY);
		GridBagConstraints gbc_txtTypeYourMessage = new GridBagConstraints();
		gbc_txtTypeYourMessage.gridheight = 2;
		gbc_txtTypeYourMessage.fill = GridBagConstraints.BOTH;
		gbc_txtTypeYourMessage.insets = new Insets(0, 0, 0, 5);
		gbc_txtTypeYourMessage.gridx = 0;
		gbc_txtTypeYourMessage.gridy = 2;
		this.contentPane.add(txtTypeScrollPane, gbc_txtTypeYourMessage);
//		this.txtTypeYourMessage.setColumns(10);
		
		JButton btnEnviarMsg = new JButton("Enviar");
		btnEnviarMsg.setToolTipText("Enviar MENSAGEM! não cabia tudo hehe");
		GridBagConstraints gbc_btnEnviarMsg = new GridBagConstraints();
		gbc_btnEnviarMsg.fill = GridBagConstraints.BOTH;
		gbc_btnEnviarMsg.insets = new Insets(0, 0, 5, 0);
		gbc_btnEnviarMsg.gridx = 1;
		gbc_btnEnviarMsg.gridy = 2;
		this.contentPane.add(btnEnviarMsg, gbc_btnEnviarMsg);
		
		GridBagConstraints gbc_openButton = new GridBagConstraints();
		gbc_openButton.fill = GridBagConstraints.BOTH;
		gbc_openButton.gridx = 1;
		gbc_openButton.gridy = 3;
		this.contentPane.add(openButton, gbc_openButton);
		
		msgstatusthread = new Thread(new MsgStatusIn());
		msgstatusthread.start();
		
		txtTypeYourMessage.setEnabled(true);
		txtTypeYourMessage.setEditable(true);
		
		this.txtTypeYourMessage.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					txtTypeYourMessage.setText(txtTypeYourMessage.getText().trim());
					enviaMsg(usr, friend, amigos, docMsg, styleFrom);
				}
				feedbackaMsgStatus();
			}
		});
		
		btnEnviarMsg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				enviaMsg(usr, friend, amigos, docMsg, styleFrom);
				feedbackaMsgStatus();
			}
		});
		
		this.txtTypeYourMessage.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				String txtMessage = txtTypeYourMessage.getText().trim();
				if (txtMessage.isEmpty()) {
					txtTypeYourMessage.setForeground(Color.LIGHT_GRAY);
					txtTypeYourMessage.setText(txtTypeMsg);
				}
			}
			public void focusGained(FocusEvent e) {
				String txtMessage = txtTypeYourMessage.getText().trim();
				if (txtMessage.isEmpty() || 
						(txtMessage.equals(txtTypeMsg) &&
						txtTypeYourMessage.getForeground().equals(Color.LIGHT_GRAY))) {
					txtTypeYourMessage.setText("");
					txtTypeYourMessage.setForeground(Color.BLACK);
				}
				feedbackaMsgStatus();
			}
		});
		
		this.addWindowFocusListener(new WindowAdapter() {
		    public void windowGainedFocus(WindowEvent e) {
		        txtTypeYourMessage.requestFocusInWindow();
		        feedbackaMsgStatus();
		    }
		});
		
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				openButton.setEnabled(false);
	            int returnVal = fc.showOpenDialog(Chat.this);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	                File file = fc.getSelectedFile();
	                Thread tUploadaArquivo = new Thread(new uploadsArquivo(file));
	                tUploadaArquivo.start();	
            	}
	            openButton.setEnabled(true);
            }
		});
		
		lblEasteregg.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				String[] opcoes = {"Ok", "Ok"};
				JOptionPane.showOptionDialog(Chat.this, "Espero que tenha voltado ao normal agora."
						, "JFileChooser te bugou?",JOptionPane.YES_NO_OPTION,
						JOptionPane.PLAIN_MESSAGE, null, opcoes, opcoes[0]);
			}
		});
	
		this.chatframe.setVisible(initVisible);

	}
	
	class uploadsArquivo implements Runnable {
		
		File file;
		
		public uploadsArquivo(File file) {
			this.file = file;
		}
		
		public void run() {
			if (file.length() == 0) {
				JOptionPane.showMessageDialog(Chat.this,
						"Arquivo com 0 bytes hehe",
						"Erro", JOptionPane.WARNING_MESSAGE);
				return;
			}
			DGSocket dgsUploader;
    		long[] estimatedRTT = {-1};
			FileInputStream fInputStream;
			try {
				fInputStream = new FileInputStream(file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(Chat.this,
						"Arquivo não encontrado",
						"Erro", JOptionPane.WARNING_MESSAGE);
				return;
			}
			if (friendIP == null || friendPort == 0) {
				for (int i = 0; i < 3; i++) {
					try {
						String[] fetched = fetchFriendInfo();
						if (fetched != null) {
							if (fetched[1] != null && Integer.parseInt(fetched[2]) != 0) {
								friendIP = fetched[1];
								friendPort = Integer.parseInt(fetched[2]);
								break;
							}
						} 
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}
			if (friendIP == null || friendPort == 0) {
				JOptionPane.showMessageDialog(Chat.this,
						"Não foi possível pegar a porta do seu amigo, ops.",
						"Erro", JOptionPane.WARNING_MESSAGE);
				return;
			}
			
        	try {
        		dgsUploader = new DGSocket(estimatedRTT, pDescartaPacotes, Chat.this.pktsPerdidos,
        				friendIP,
        				friendPort);
        	} catch (Exception e) {
        		if (fInputStream != null) {
        			try {
						fInputStream.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        		}
        		e.printStackTrace();
				JOptionPane.showMessageDialog(Chat.this,
						"Erro tentando se conectar a (" +
						friendIP + ", " +
						friendPort + ")",
						"Erro", JOptionPane.WARNING_MESSAGE);
				return;
        	}
        	try {
        		// diz o meu nome ;-;
        		BufferMethods.writeString(meUserName, dgsUploader);
        		
        		// diz que quer download ;-;
        		BufferMethods.sendInt(1, dgsUploader);
        		
        		// descobre se o cara está pronto pro upload
        		BufferMethods.receiveFeedBack(dgsUploader);
        		
                // diz nome do arquivo que estarei enviando
        		BufferMethods.writeChatString(file.getName(), dgsUploader);
        	} catch (Exception e) {
        		try {
					dgsUploader.close(false);
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
        		if (fInputStream != null) {
        			try {
						fInputStream.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        		}
        		e.printStackTrace();
				JOptionPane.showMessageDialog(Chat.this,
						"Erro durante transferência",
						"Erro", JOptionPane.WARNING_MESSAGE);
				return;
        	}
			System.out.println(file.getAbsolutePath());
			System.out.println("fileSize: " + file.length());
			long fileLength = file.length();
			try {
				// diz quantos bytes estarei enviando
				BufferMethods.sendLong(fileLength, dgsUploader);
			} catch (Exception e) {
				try {
					dgsUploader.close(false);
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				if (fInputStream != null) {
        			try {
						fInputStream.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        		}
        		e.printStackTrace();
				JOptionPane.showMessageDialog(Chat.this,
						"Erro durante transferência",
						"Erro", JOptionPane.WARNING_MESSAGE);
				return;
        	}
			
			long remainingSize = file.length();
			byte[] buffer = new byte[4096];
			int bytesRead;
			JProgressBar progressBar = new JProgressBar();
			progressBar.setString("");
			progressBar.setStringPainted(true);
			JButton btnAbrir = new JButton("Abrir");
			btnAbrir.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					try {
						Desktop.getDesktop().open(file);
					} catch (IOException e) {
						JOptionPane.showMessageDialog(Chat.this,
								"Erro tentando abrir arquivo " + file.getName(),
								"Erro", JOptionPane.WARNING_MESSAGE);
					}
				}
			});
			DownloadPainel downloadInfo = new DownloadPainel(file, progressBar, btnAbrir);
			downloadInfo.setAbrir(true);
			downloadPanel.add(downloadInfo);
			downloadPanel.validate();
			long t0 = System.currentTimeMillis();
			long toDownload = remainingSize;
			long tf, deltaT;
			double estimativa = 5;
			try {
				while (remainingSize > 0  && (bytesRead = fInputStream.read(buffer, 0,
						(int)Math.min(buffer.length, remainingSize))) != -1) {
					remainingSize -= bytesRead;
					System.out.println("bytesRead: " + bytesRead + "\nremainingSize: " + remainingSize);
					dgsUploader.send(buffer, bytesRead);
					
					tf = System.currentTimeMillis();
					downloadInfo.setLblRTT(estimatedRTT[0]);
					deltaT = tf - t0;
					estimativa = atualizaProgresso(fileLength, toDownload, remainingSize,
							deltaT, estimativa, progressBar);
				}
				downloadInfo.killlblRTT();
				downloads.put(file.getName(), downloadInfo);
				progressBar.setStringPainted(false);
			} catch (Exception e) {
				downloadPanel.remove(downloadInfo);
				downloadPanel.repaint();
				downloadPanel.validate();
				try {
					dgsUploader.close(false);
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				if (fInputStream != null) {
        			try {
						fInputStream.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        		}
        		e.printStackTrace();
				JOptionPane.showMessageDialog(Chat.this,
						"Erro durante transferência",
						"Erro", JOptionPane.WARNING_MESSAGE);
				return;
			}
			try {
				fInputStream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				dgsUploader.close(false);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	class MsgStatusIn implements Runnable {
		
		public void run() {
			while (true) {
				try { //TODO implementar essas exceções
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
					} else if (status == 3) {
						// arquivo conseguiu ser aberto
						try {
							String fileName = BufferMethods.readChatString(msgStatusSocket);
							downloads.get(fileName).setLblAberto("Aberto por " + friendName);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
				} catch (SocketException e) {
					e.printStackTrace();
					synchronized (lblMsgInfo) {
						lblMsgInfo.setForeground(Color.RED);
						lblMsgInfo.setText("serviço de status de mensagem quebrou");
						servicoStatusMsgOk = false;
					}
					friendOffline = true;
					break;
				}
			}
		}
	}
	
	class BaixaArquivo implements Runnable {
		
		private DGSocket connectionSocket;
		private long[] estimatedrtt;
		public BaixaArquivo(DGSocket dgs, long[] estimatedrtt) {
			this.connectionSocket = dgs;
			this.estimatedrtt = estimatedrtt;
		}
		
		public void run() {
			DownloadPainel downloadInfo = null;
			try {
				String directory = "Download_Dump" + File.separator;
				BufferMethods.sendFeedBack(1, connectionSocket);
				String fileName = BufferMethods.readChatString(connectionSocket);
				
				JProgressBar progressBar = new JProgressBar();
				progressBar.setString("");
				progressBar.setStringPainted(true);
//				fileName = fileName.replaceAll(" ", "");
				System.out.println(directory + fileName);
				File arquivoReceptor = new File(directory + fileName);
				if (arquivoReceptor.isFile()) {
					arquivoReceptor.delete();
				}
				arquivoReceptor.createNewFile();
				JButton btnAbrir = new JButton("Abrir");
				downloadInfo = new DownloadPainel(arquivoReceptor, progressBar, btnAbrir);
				downloadInfo.setAbrir(false);
				downloadPanel.add(downloadInfo);
				downloadPanel.validate();
				long remainingSize = 0;
				try (FileOutputStream outToFile = new FileOutputStream(arquivoReceptor)) {
					long fileSize = BufferMethods.receiveLong(connectionSocket);
					System.out.println("fileSize: " + fileSize);
					remainingSize = fileSize;
					byte[] buffer = new byte[4096];
					int bytesRead = 0;
					
					long t0 = System.currentTimeMillis();
					long toDownload = remainingSize;
					long tf, deltaT;
					double estimativa = 5;
					while (true) {
						bytesRead = connectionSocket.receive(buffer, (int)Math.min(buffer.length, remainingSize));
						if (bytesRead == -1) break;
						remainingSize -= bytesRead;
						
						tf = System.currentTimeMillis();
						downloadInfo.setLblRTT(estimatedrtt[0]);
						deltaT = tf - t0;
						estimativa = atualizaProgresso(fileSize, toDownload, remainingSize,
								deltaT, estimativa, progressBar);
						
						System.out.println("bytesRead: " + bytesRead+ "\nremainingSize: " + remainingSize);
						outToFile.write(buffer, 0, bytesRead);
						if (remainingSize == 0) break;
					}
					btnAbrir.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							try {
								Desktop.getDesktop().open(arquivoReceptor);
								msgNova = true;
								feedbackaMsgStatus(3);
								BufferMethods.writeChatString(fileName, msgStatusSocket);
							} catch (IOException e) {
								JOptionPane.showMessageDialog(Chat.this,
										"Erro tentando abrir arquivo " + arquivoReceptor.getName(),
										"Erro", JOptionPane.WARNING_MESSAGE);
							}
						}
					});
					downloadInfo.setAbrir(true);
					progressBar.setStringPainted(false);
					downloadInfo.killlblRTT();
				} catch (Exception e) {
					if (downloadInfo != null && remainingSize != 0) {
						downloadPanel.remove(downloadInfo);
						downloadPanel.repaint();
						downloadPanel.validate();
					}
					if (remainingSize != 0) {
						arquivoReceptor.delete();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					connectionSocket.close(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	class ReceiveMessages implements Runnable {
		private boolean finished = false;

		public void run() {
			while (!finished) {
				try {	
					String msg = BufferMethods.readChatString(friendSocket);
					if (!chatframe.isVisible()) {
						chatframe.setVisible(true);
					}
					synchronized (docMsg) {
						docMsg.insertString(docMsg.getLength(), dateFormat.format(new Date(System.currentTimeMillis()))
								+ " <" + friendName + "> ", styleFrom);
						docMsg.insertString(docMsg.getLength(), msg + '\n', null);
						msgNova = true;
					}
					BufferMethods.sendFeedBack(1, msgStatusSocket);

				} catch(Exception e) {
					synchronized (lblMsgInfo) {
						lblMsgInfo.setForeground(Color.RED);
						lblMsgInfo.setText(friendName + " está offline.");
					}
					friendOffline = true;
					break;
				}
			}
		}
		public void finish() {
			this.finished = true;
		}
	}
	
	public void comecaBaixaArquivos(DGSocket connectionSocket, long[] estimatedrtt) {
		(new Thread(new BaixaArquivo(connectionSocket, estimatedrtt))).start();
	}
	
	private double atualizaProgresso(long fileLength, long toDownload, long remainingSize,
			long deltaT, double estimativa, JProgressBar progressBar) {
		// aging
		double alfa = 0.00390625, amostraAtual;
		amostraAtual = ((deltaT * toDownload / (double) (toDownload - remainingSize)) - deltaT) / 1000;
		estimativa = (1 - alfa) * estimativa + alfa * amostraAtual;
		StringBuilder quickConc;
		progressBar.setValue((int)(( (fileLength - remainingSize) / (double)fileLength) * 100));
		quickConc = new StringBuilder();
		quickConc.append(String.format("%.0f",estimativa)).append(" s");
		progressBar.setString(quickConc.toString());
		
		return estimativa;
	}

	private void enviaMsg(String usr, String friend, ArrayList<String> amigos, StyledDocument docMsg, Style styleFrom) {
		String txtMessage = txtTypeYourMessage.getText().trim();
		if (!(txtMessage.isEmpty() || 
				(txtMessage.equals(txtTypeMsg) &&
				txtTypeYourMessage.getForeground().equals(Color.LIGHT_GRAY)))) {
			if (txtMessage.length() > 256) {
				JOptionPane.showMessageDialog(Chat.this,
						"Sua mensagem é grande demais hehe (maior que 256 caracteres)",
						"Mensagem grande demais",
			            JOptionPane.WARNING_MESSAGE);
				return;
			}
			try {
				if (friendOffline) {
//					JOptionPane.showMessageDialog(Chat.this,
//							"Friend offline, vamos tentar contatá-lo de novo",
//							"Opa!", JOptionPane.WARNING_MESSAGE);
					Iterator<String> it = Chat.this.amigos.iterator();
					while (it.hasNext()) {
						String str = it.next();
						int fP = str.indexOf('('), lP = str.lastIndexOf(')');
						if (fP != -1 && lP != -1) {
							String friendname = str.substring(0, fP - 1);
							if (friendname.equals(friend)) {
								friendIP = str.substring(fP + 1, str.indexOf(','));
								try {
									friendPort = Integer.parseInt(str.substring(str.indexOf(',') + 2, lP));
//									JOptionPane.showMessageDialog(Chat.this,
//											"Encontramos " + friendname + " em " + friendIP + ", "
//											+ friendPort,
//											"Aha!", JOptionPane.WARNING_MESSAGE);

									friendSocket = new DGSocket(0, pDescartaPacotes, Chat.this.pktsPerdidos, friendIP, friendPort);
									BufferMethods.writeString(usr, friendSocket);
									BufferMethods.sendInt(0, friendSocket);
									msgStatusSocket = new DGSocket(0, pDescartaPacotes, Chat.this.pktsPerdidos, friendIP, friendPort);
									msgstatusthread = new Thread(new MsgStatusIn());
									msgrcv = new Thread(new ReceiveMessages());
									msgstatusthread.start();
									msgrcv.start();
									servicoStatusMsgOk = true;
									friendOffline = false;
									Thread.sleep(1000);
									synchronized (lblMsgInfo) {
										lblMsgInfo.setForeground(Color.GREEN);
										lblMsgInfo.setText("Reconectados");
									}
								} catch (NumberFormatException e) {
									System.out.println("erro tentando pegar porta");
								}
								break;
							}
						}
					}
				} else {
					BufferMethods.writeChatString(txtMessage, friendSocket);
					synchronized (lblMsgInfo) {
						lblMsgInfo.setForeground(Color.ORANGE);
						lblMsgInfo.setText("mensagem enviada");
						sentMsg = true;
					}
					synchronized (docMsg) {
						docMsg.insertString(docMsg.getLength(), dateFormat.format(new Date(System.currentTimeMillis()))
								+ " <" + usr + "> ", styleFrom);
						docMsg.insertString(docMsg.getLength(), txtMessage + '\n', null);
					}
					txtTypeYourMessage.setText("");
				}
			} catch (SocketException e) {
				try {
					friendSocket.close(false);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					msgStatusSocket.close(false);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				synchronized (lblMsgInfo) {
					lblMsgInfo.setForeground(Color.RED);
					lblMsgInfo.setText(friend + " está offline.");
				}
				friendOffline = true;
			} catch (BadLocationException e) {
				// não deveria dar isto
				e.printStackTrace();
			} catch (Exception e) {
				try {
					friendSocket.close(false);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					msgStatusSocket.close(false);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				synchronized (lblMsgInfo) {
					lblMsgInfo.setForeground(Color.RED);
					lblMsgInfo.setText("mensagem não foi enviada");
				}
				friendOffline = true;
				e.printStackTrace();
			}
		}
	}

	private void feedbackaMsgStatus() {
		feedbackaMsgStatus(2);
	}
	
	private void feedbackaMsgStatus(int status) {
		if (servicoStatusMsgOk && msgNova) {
			try {
				BufferMethods.sendFeedBack(status, msgStatusSocket);
				msgNova = false;
			} catch (Exception e1) {
				e1.printStackTrace();
				synchronized (lblMsgInfo) {
					lblMsgInfo.setForeground(Color.RED);
					lblMsgInfo.setText("serviço de status de mensagem quebrou");
					servicoStatusMsgOk = false;
					friendOffline = true;
				}
			}
		}
	}
	
	public void setAmigos(ArrayList<String> amigos) {
		this.amigos = amigos;
	}
	
	public String[] fetchFriendInfo() {
		Iterator<String> it = amigos.iterator();
		String[] retorno = new String[3];
		while (it.hasNext()) {
			String str = it.next();
			int fP = str.indexOf('('), lP = str.lastIndexOf(')');
			if (fP != -1 && lP != -1) {
				retorno[1] = str.substring(fP + 1, str.indexOf(','));
				retorno[0] = str.substring(0, fP - 1);
				try {
					retorno[2] = str.substring(str.indexOf(',') + 2, lP);
//					JOptionPane.showMessageDialog(Chat.this,
//							retorno[0] + " (" + retorno[1] + ", " + retorno[2] + ")",
//							"Erro", JOptionPane.WARNING_MESSAGE);
					if (retorno[0].equals(Chat.this.friendName)) {
//						JOptionPane.showMessageDialog(Chat.this,
//								retorno[0] + " (" + retorno[1] + ", " + retorno[2] + ")",
//								"Erro", JOptionPane.WARNING_MESSAGE);
						return retorno;
					}
				} catch (NumberFormatException e) {
					System.out.println("erro tentando pegar porta");
				}
			}
		}
		return null;
	}
	
	public void setStuff(DGSocket friendSocket, DGSocket msgStatusSocket) {
		try {
			this.friendSocket.close(false);
		} catch (Exception e) {}
		try {
			this.msgStatusSocket.close(false);
		} catch (Exception e) {}
		lblMsgInfo.setText("");
		friendOffline = false;
		msgrcv.interrupt();
		msgstatusthread.interrupt();
		this.friendSocket = friendSocket;
		this.msgStatusSocket = msgStatusSocket;
		msgrcv = new Thread(new ReceiveMessages());
		msgstatusthread = new Thread(new MsgStatusIn());
		msgrcv.start();
		msgstatusthread.start();
	}
	
	public String getDocMsg() throws BadLocationException {
		return docMsg.getText(0, docMsg.getLength());
	}

	public void setpDescartaPacotes(double pDescartaPacotes) {
		this.pDescartaPacotes = pDescartaPacotes;
	}
}