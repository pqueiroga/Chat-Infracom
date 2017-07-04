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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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
import javax.swing.BoxLayout;

public class Chat extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1864226400918464241L;
	private JPanel contentPane;
	private JTextArea txtTypeYourMessage;
	private String txtTypeMsg = "Digite uma mensagem";
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
	private int[] pktsPerdidos; // ponteiro importante
	private List<DGServerSocket> SSList;
	
	private int friendUploadPort;
	
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
    /**
	 * Create the frame.
	 */
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new Chat("eu", "ele", 0, null, null, null, null, null, true); //new Socket("localhost", 2030));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public Chat(String usr, String friend, int friendUploadPort, DGSocket friendSocketConstrutor,
			DGSocket msgStatusSocketConstrutor, ArrayList<DGServerSocket> SSList,
			ArrayList<String> amigos, int[] pktsPerdidos, boolean initVisible) throws IOException {
		setResizable(false);
		
		this.friendName = friend;
		
		this.amigos = amigos;
		this.meUserName = usr;
		
		this.friendUploadPort = friendUploadPort;

		this.SSList = SSList;
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
			
		
//		msgTextPane.setText(arg0);
		msgTextPane.setEditable(false);
		scrollPane.setViewportView(msgTextPane);
		
		docMsg = msgTextPane.getStyledDocument();
		styleFrom = msgTextPane.addStyle("FROM", null);
		StyleConstants.setBold(styleFrom, true);
		
		JScrollPane scrollPane_1 = new JScrollPane();
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
		//Thread para enviar arquivos
		
		//Thread para receber arquivos
		
		
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
		
//		DownloadPainel downloadInfo = new DownloadPainel("Nome do Arquivo", new JProgressBar());
//		downloadPanel.add(downloadInfo);
//		
//		downloadInfo = new DownloadPainel("Nome do Arquivo");
//		downloadPanel.add(downloadInfo);
//		
//		downloadInfo = new DownloadPainel("Nome do Arquivo");
//		downloadPanel.add(downloadInfo);
//		
//		downloadInfo = new DownloadPainel("Nome do Arquivo");
//		downloadPanel.add(downloadInfo);
//		
//		downloadInfo = new DownloadPainel("Nome do Arquivo");
//		downloadPanel.add(downloadInfo);
//		
//		downloadInfo = new DownloadPainel("Nome do Arquivo");
//		downloadPanel.add(downloadInfo);
//		
//		downloadInfo = new DownloadPainel("Nome do Arquivo");
//		downloadPanel.add(downloadInfo);
//		
//		downloadInfo = new DownloadPainel("Nome do Arquivo");
//		downloadPanel.add(downloadInfo);
//		
//		downloadInfo = new DownloadPainel("Nome do Arquivo");
//		downloadPanel.add(downloadInfo);
		
//		btnEnviarArquivo.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent e) {
//				TODO abrir thread para envio de arquivos
//				try {
//					DGSocket teste = new DGSocket(Chat.this.pktsPerdidos,
//							friendSocket.getInetAddress().getHostName(), friendSocket.getPort() + 2);
//					
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//				String directory = "Upload_Pool" + File.separator;
//				String fileName = txtTypeFilePath.getText();
//				File uploadFile = new File(directory + fileName);
//				if (uploadFile.isFile()) {
//					// diz nome do arquivo que estarei enviando
//					BufferMethods.writeString(fileName, teste);
//					System.out.println(directory + fileName);
//					// diz quantos bytes estarei enviando
//					System.out.println("fileSize: " + uploadFile.length());
//
//					BufferMethods.sendLong(uploadFile.length(), teste);
//					
//					long remainingSize = uploadFile.length();
//					byte[] buffer = new byte[1024];
//					int bytesRead;
//					FileInputStream fInputStream = new FileInputStream(uploadFile);
//					
//					while (remainingSize > 0  && (bytesRead = fInputStream.read(buffer, 0,
//							(int)Math.min(buffer.length, remainingSize))) != -1) {
//						remainingSize -= bytesRead;
//						System.out.println("bytesRead: " + bytesRead + "\nremainingSize: " + remainingSize);
//						teste.send(buffer, bytesRead);
//					}
//					fInputStream.close();
//				}
//				
//				teste.close();
//				System.out.println("Enquanto fecha eu posso continuar fazendo coisas");
//			}
//		});
		
		this.txtTypeYourMessage.addKeyListener(new KeyAdapter() {

			@Override
			public void keyReleased(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					txtTypeYourMessage.setEnabled(true);
					txtTypeYourMessage.setEditable(true);
					txtTypeYourMessage.setText(txtTypeYourMessage.getText().trim());
					enviaMsg(usr, friend, amigos, docMsg, styleFrom);
				}
				feedbackaMsgStatus();
			}
		});
		
		btnEnviarMsg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				txtTypeYourMessage.setEnabled(true);
				txtTypeYourMessage.setEditable(true);
				enviaMsg(usr, friend, amigos, docMsg, styleFrom);
				feedbackaMsgStatus();
			}
		});
		
		this.txtTypeYourMessage.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				txtTypeYourMessage.setEnabled(true);
				txtTypeYourMessage.setEditable(true);
				String txtMessage = txtTypeYourMessage.getText().trim();
				if (txtMessage.isEmpty()) {
					txtTypeYourMessage.setForeground(Color.LIGHT_GRAY);
					txtTypeYourMessage.setText(txtTypeMsg);
				}
			}
			public void focusGained(FocusEvent e) {
				txtTypeYourMessage.setEnabled(true);
				txtTypeYourMessage.setEditable(true);
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
		    	txtTypeYourMessage.setEnabled(true);
				txtTypeYourMessage.setEditable(true);
		        txtTypeYourMessage.requestFocusInWindow();
		        feedbackaMsgStatus();
		    }
		});
		
		openButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				txtTypeYourMessage.setEnabled(true);
				txtTypeYourMessage.setEditable(true);
				openButton.setEnabled(false);
	            int returnVal = fc.showOpenDialog(Chat.this);
	            txtTypeYourMessage.setEnabled(true);
				txtTypeYourMessage.setEditable(true);
	            if (returnVal == JFileChooser.APPROVE_OPTION) {
	            	txtTypeYourMessage.setEnabled(true);
	    			txtTypeYourMessage.setEditable(true);
	                File file = fc.getSelectedFile();
	                Thread tUploadaArquivo = new Thread(new uploadsArquivo(file));
	                tUploadaArquivo.start();	
	                txtTypeYourMessage.setEnabled(true);
	    			txtTypeYourMessage.setEditable(true);
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
//				File file = new File("historicos_" + usr + File.separator + friend);
//				try {
//					file.createNewFile();
//					try (PrintWriter pw = new PrintWriter(file)) {
//						pw.print(Chat.this.getDocMsg());
//					} catch (BadLocationException ex) {
//						// TODO Auto-generated catch block
//						ex.printStackTrace();
//					}
//				} catch (IOException ex) {
//					// TODO Auto-generated catch block
//					ex.printStackTrace();
//				}
//				
//				scrollPane.remove(msgTextPane);
//				contentPane.remove(scrollPane);
//				txtTypeScrollPane.remove(txtTypeYourMessage);
//				contentPane.remove(txtTypeScrollPane);
//				contentPane.remove(lblMsgInfo);
//				
//				scrollPane = new JScrollPane();
//				GridBagConstraints gbc_scrollPane = new GridBagConstraints();
//				gbc_scrollPane.fill = GridBagConstraints.BOTH;
//				gbc_scrollPane.insets = new Insets(0, 0, 5, 5);
//				gbc_scrollPane.gridx = 0;
//				gbc_scrollPane.gridy = 0;
//				contentPane.add(scrollPane, gbc_scrollPane);
//				msgTextPane = new JTextPane();
//				msgTextPane.setToolTipText("Área de texto para conversas");
//				try {
//					File file2 = new File("historicos_" + usr + File.separator + friend);
//					byte[] encoded = Files.readAllBytes(Paths.get(file2.getAbsolutePath()));
//					msgTextPane.setForeground(Color.GRAY);
//					msgTextPane.setText(new String(encoded, "UTF-8"));
//					msgTextPane.setForeground(Color.BLACK);
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//				
//				msgTextPane.setEditable(false);
//				scrollPane.setViewportView(msgTextPane);
//				
//				docMsg = msgTextPane.getStyledDocument();
//				styleFrom = msgTextPane.addStyle("FROM", null);
//				StyleConstants.setBold(styleFrom, true);
//				
//				txtTypeYourMessage = new JTextArea();
//				txtTypeYourMessage.setToolTipText("Digite uma mensagem aqui...");
//				txtTypeYourMessage.setLineWrap(true);
//				txtTypeYourMessage.setWrapStyleWord(true);
//				txtTypeScrollPane = new JScrollPane(txtTypeYourMessage);
//				
//				lblMsgInfo = new JLabel("");
//				lblMsgInfo.setToolTipText("Estado da comunicação entre vocês");
//				lblMsgInfo.setFont(new Font("Dialog", Font.BOLD, 10));
//				lblMsgInfo.setVerticalAlignment(SwingConstants.TOP);
//				GridBagConstraints gbc_lblMsgInfo = new GridBagConstraints();
//				gbc_lblMsgInfo.anchor = GridBagConstraints.NORTHWEST;
//				gbc_lblMsgInfo.insets = new Insets(0, 0, 5, 5);
//				gbc_lblMsgInfo.gridx = 0;
//				gbc_lblMsgInfo.gridy = 1;
//				contentPane.add(lblMsgInfo, gbc_lblMsgInfo);
//				
//				txtTypeYourMessage.setText(txtTypeMsg);
//				txtTypeYourMessage.setForeground(Color.LIGHT_GRAY);
//				GridBagConstraints gbc_txtTypeYourMessage = new GridBagConstraints();
//				gbc_txtTypeYourMessage.gridheight = 2;
//				gbc_txtTypeYourMessage.fill = GridBagConstraints.BOTH;
//				gbc_txtTypeYourMessage.insets = new Insets(0, 0, 0, 5);
//				gbc_txtTypeYourMessage.gridx = 0;
//				gbc_txtTypeYourMessage.gridy = 2;
//				contentPane.add(txtTypeScrollPane, gbc_txtTypeYourMessage);
//				
//				txtTypeYourMessage.addKeyListener(new KeyAdapter() {
//
//					@Override
//					public void keyReleased(KeyEvent arg0) {
//						if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
//							txtTypeYourMessage.setEnabled(true);
//							txtTypeYourMessage.setEditable(true);
//							txtTypeYourMessage.setText(txtTypeYourMessage.getText().trim());
//							enviaMsg(usr, friend, amigos, docMsg, styleFrom);
//						}
//						feedbackaMsgStatus();
//					}
//				});
//				
//				txtTypeYourMessage.addFocusListener(new FocusListener() {
//					public void focusLost(FocusEvent e) {
//						txtTypeYourMessage.setEnabled(true);
//						txtTypeYourMessage.setEditable(true);
//						String txtMessage = txtTypeYourMessage.getText().trim();
//						if (txtMessage.isEmpty()) {
//							txtTypeYourMessage.setForeground(Color.LIGHT_GRAY);
//							txtTypeYourMessage.setText(txtTypeMsg);
//						}
//					}
//					public void focusGained(FocusEvent e) {
//						txtTypeYourMessage.setEnabled(true);
//						txtTypeYourMessage.setEditable(true);
//						String txtMessage = txtTypeYourMessage.getText().trim();
//						if (txtMessage.isEmpty() || 
//								(txtMessage.equals(txtTypeMsg) &&
//								txtTypeYourMessage.getForeground().equals(Color.LIGHT_GRAY))) {
//							txtTypeYourMessage.setText("");
//							txtTypeYourMessage.setForeground(Color.BLACK);
//						}
//						feedbackaMsgStatus();
//					}
//				});
//				scrollPane.repaint();
//				scrollPane.validate();
//				
//				contentPane.repaint();
//				contentPane.validate();
				
			}
		});
		
//		Thread tEsperaArquivo = new Thread(new EsperaArquivos());
//		tEsperaArquivo.start();
		
		this.chatframe.setVisible(initVisible);

	}
	
	class uploadsArquivo implements Runnable {
		
		File file;
		
		public uploadsArquivo(File file) {
			this.file = file;
		}
		
		public void run() {
			txtTypeYourMessage.setEnabled(true);
			txtTypeYourMessage.setEditable(true);
			if (file.length() == 0) {
				JOptionPane.showMessageDialog(Chat.this,
						"Arquivo com 0 bytes hehe",
						"Erro", JOptionPane.WARNING_MESSAGE);
				return;
			}
			DGSocket dgsUploader;
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
        	try {
        		dgsUploader = new DGSocket(Chat.this.pktsPerdidos,
        				friendSocket.getInetAddress().getHostName(),
        				friendUploadPort);
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
						friendSocket.getInetAddress().getHostName() + ", " +
						friendUploadPort + ")",
						"Erro", JOptionPane.WARNING_MESSAGE);
				return;
        	}
        	try {
        		
        		// diz o meu nome ;-;
        		BufferMethods.writeString(meUserName, dgsUploader);
                // diz nome do arquivo que estarei enviando
        		BufferMethods.writeString(file.getName(), dgsUploader);
        	} catch (Exception e) {
        		try {
					dgsUploader.close();
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
					dgsUploader.close();
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
			DownloadPainel downloadInfo = new DownloadPainel(file, progressBar);
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
					deltaT = tf - t0;
					estimativa = atualizaProgresso(fileLength, toDownload, remainingSize,
							deltaT, estimativa, progressBar);
				}
				progressBar.setStringPainted(false);
				txtTypeYourMessage.setEnabled(true);
				txtTypeYourMessage.setEditable(true);
			} catch (Exception e) {
				downloadPanel.remove(downloadInfo);
				downloadPanel.repaint();
				downloadPanel.validate();
				try {
					dgsUploader.close();
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
				dgsUploader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			txtTypeYourMessage.setEnabled(true);
			txtTypeYourMessage.setEditable(true);
		}
	}
	
	class MsgStatusIn implements Runnable {
		
		public void run() {
			while (true) {
				try { //TODO implementar essas exceções
//					int status = msgStatusInput.read();
					int status = BufferMethods.receiveFeedBack(msgStatusSocket);
					txtTypeYourMessage.setEnabled(true);
					txtTypeYourMessage.setEditable(true);
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
				} catch (SocketException e) {
					e.printStackTrace();
					synchronized (lblMsgInfo) {
						lblMsgInfo.setForeground(Color.RED);
						lblMsgInfo.setText("serviço de status de mensagem quebrou");
						servicoStatusMsgOk = false;
					}
					break;
				}
			}
		}
	}
	
	class BaixaArquivo implements Runnable {
		
		private DGSocket connectionSocket;
		
		public BaixaArquivo(DGSocket dgs) {
			this.connectionSocket = dgs;
		}
		
		public void run() {
			txtTypeYourMessage.setEnabled(true);
			txtTypeYourMessage.setEditable(true);
			DownloadPainel downloadInfo = null;
			FileOutputStream outToFile = null;
			try {
				String directory = "Download_Dump" + File.separator;
				String fileName = BufferMethods.readString(connectionSocket);
				
				JProgressBar progressBar = new JProgressBar();
				progressBar.setString("");
				progressBar.setStringPainted(true);
				fileName = fileName.replaceAll(" ", "");
				System.out.println(directory + fileName);
				File arquivoReceptor = new File(directory + fileName);
				if (arquivoReceptor.isFile()) {
					arquivoReceptor.delete();
				}
				arquivoReceptor.createNewFile();
				downloadInfo = new DownloadPainel(arquivoReceptor, progressBar);
				downloadInfo.setAbrir(false);
				downloadPanel.add(downloadInfo);
				downloadPanel.validate();
				long remainingSize = 0;
				try {
					long fileSize = BufferMethods.receiveLong(connectionSocket);
					System.out.println("fileSize: " + fileSize);
					remainingSize = fileSize;
					byte[] buffer = new byte[4096];
					int bytesRead = 0;
					outToFile = new FileOutputStream(arquivoReceptor);
					
					long t0 = System.currentTimeMillis();
					long toDownload = remainingSize;
					long tf, deltaT;
					double estimativa = 5;
					while (true) {
						bytesRead = connectionSocket.receive(buffer, (int)Math.min(buffer.length, remainingSize));
						if (bytesRead == -1) break;
						remainingSize -= bytesRead;
						
						tf = System.currentTimeMillis();
						deltaT = tf - t0;
						estimativa = atualizaProgresso(fileSize, toDownload, remainingSize,
								deltaT, estimativa, progressBar);
						
						System.out.println("bytesRead: " + bytesRead+ "\nremainingSize: " + remainingSize);
						outToFile.write(buffer, 0, bytesRead);
						if (remainingSize == 0) break;
					}
				} catch (Exception e) {
					if (downloadInfo != null && remainingSize != 0) {
						downloadPanel.remove(downloadInfo);
						downloadPanel.repaint();
						downloadPanel.validate();
					}
				}
				downloadInfo.setAbrir(true);
				progressBar.setStringPainted(false);
				try {
					outToFile.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					connectionSocket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				txtTypeYourMessage.setEnabled(true);
				txtTypeYourMessage.setEditable(true);
				try {
					outToFile.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					connectionSocket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
//	class EsperaArquivos implements Runnable {
//		public void run() {
//			while (true) {
//				DGSocket connectionSocket = null;
//				try {
//					connectionSocket = SSList.get(2).accept(pktsPerdidos);
//					String friendUploader = BufferMethods.readString(connectionSocket);
//					Chat.this.setVisible(true);
//					(new Thread(new BaixaArquivo(connectionSocket))).start();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}
	
	class ReceiveMessages implements Runnable {
		private boolean finished = false;

		public void run() {
			while (!finished) {
				try {	
					String msg = BufferMethods.readChatString(friendSocket);
					txtTypeYourMessage.setEnabled(true);
					txtTypeYourMessage.setEditable(true);
					if (!chatframe.isVisible()) {
						chatframe.setVisible(true);
					}
					synchronized (docMsg) {
						docMsg.insertString(docMsg.getLength(), dateFormat.format(new Date(System.currentTimeMillis()))
								+ " <" + friendName + "> ", styleFrom);
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
	
	public void comecaBaixaArquivos(DGSocket connectionSocket) {
		(new Thread(new BaixaArquivo(connectionSocket))).start();
	}
	
	private double atualizaProgresso(long fileLength, long toDownload, long remainingSize,
			long deltaT, double estimativa, JProgressBar progressBar) {
		// aging
		double alfa = 0.00390625, amostraAtual;
		//		if (((int) (deltaT / 1000)) % 2 == 0) {
		amostraAtual = ((deltaT * toDownload / (double) (toDownload - remainingSize)) - deltaT) / 1000;
		estimativa = (1 - alfa) * estimativa + alfa * amostraAtual;
		//		}
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
								String friendIP = str.substring(fP + 1, str.indexOf(','));
								try {
									int friendPort = Integer.parseInt(str.substring(str.indexOf(',') + 2, lP));
//									JOptionPane.showMessageDialog(Chat.this,
//											"Encontramos " + friendname + " em " + friendIP + ", "
//											+ friendPort,
//											"Aha!", JOptionPane.WARNING_MESSAGE);

									friendUploadPort = friendPort + 2;
									friendSocket = new DGSocket(Chat.this.pktsPerdidos, friendIP, friendPort);
									BufferMethods.writeString(usr, friendSocket);
									BufferMethods.sendInt(SSList.get(2).getLocalPort(), friendSocket);
									msgStatusSocket = new DGSocket(Chat.this.pktsPerdidos, friendIP, friendPort +1);
									msgstatusthread = new Thread(new MsgStatusIn());
									msgrcv = new Thread(new ReceiveMessages());
									msgstatusthread.start();
									msgrcv.start();
									servicoStatusMsgOk = true;
									friendOffline = false;
									Thread.sleep(500);
									lblMsgInfo.setText("");
//									BufferMethods.writeChatString(txtMessage, friendSocket);
//									synchronized (lblMsgInfo) {
//										lblMsgInfo.setForeground(Color.ORANGE);
//										lblMsgInfo.setText("mensagem enviada");
//										sentMsg = true;
//									}
//									synchronized (docMsg) {
//										docMsg.insertString(docMsg.getLength(), dateFormat.format(new Date(System.currentTimeMillis()))
//												+ " <" + usr + "> ", styleFrom);
//										docMsg.insertString(docMsg.getLength(), txtMessage + '\n', null);
//									}
//									txtTypeYourMessage.setText("");
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
					friendSocket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					msgStatusSocket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				lblMsgInfo.setForeground(Color.RED);
				lblMsgInfo.setText(friend + " está offline.");
				friendOffline = true;
			} catch (BadLocationException e) {
				// não deveria dar isto
				e.printStackTrace();
			} catch (Exception e) {
				try {
					friendSocket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					msgStatusSocket.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				lblMsgInfo.setForeground(Color.RED);
				lblMsgInfo.setText("mensagem não foi enviada");
				friendOffline = true;
				e.printStackTrace();
			}
		}
	}

	private void feedbackaMsgStatus() {
		if (servicoStatusMsgOk && msgNova) {
			try {
				BufferMethods.sendFeedBack(2, msgStatusSocket);
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
	
	public void setStuff(int uploadPort, DGSocket friendSocket, DGSocket msgStatusSocket) {
		try {
			this.friendSocket.close();
		} catch (Exception e) {}
		try {
			this.msgStatusSocket.close();
		} catch (Exception e) {}
		lblMsgInfo.setText("");
		friendOffline = false;
		msgrcv.interrupt();
		msgstatusthread.interrupt();
		this.friendSocket = friendSocket;
		this.msgStatusSocket = msgStatusSocket;
		this.friendUploadPort = uploadPort;
		msgrcv = new Thread(new ReceiveMessages());
		msgstatusthread = new Thread(new MsgStatusIn());
		msgrcv.start();
		msgstatusthread.start();
	}
	
	public String getDocMsg() throws BadLocationException {
		return docMsg.getText(0, docMsg.getLength());
	}
	
	private void resetThisShit() {
		
	}
	
//	public void setfriendUploadPort(int uploadPort) {
//		this.friendUploadPort = uploadPort;
//	}
//	
//	public void setfriendSocket(DGSocket friendSocket) {
//		try {
//			this.friendSocket.close();
//		} catch (Exception e) {}
//		msgrcv.interrupt();
//		this.friendSocket = friendSocket;
//	}
//	
//	public void setMsgStatusSocket(DGSocket msgStatusSocket) {
//		try {
//			this.msgStatusSocket.close();
//		} catch (Exception e) {}
//		msgstatusthread.interrupt();
//		this.msgStatusSocket = msgStatusSocket;
//	}
}