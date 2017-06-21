package cliente;
import cliente.threads.*;
import utility.buffer.BufferMethods;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import java.awt.Color;
import java.io.*;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JProgressBar;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.SwingConstants;
import java.awt.Font;
public class Chat extends JFrame {

	private JPanel contentPane;
	private JTextField txtTypeYourMessage;
	private JTextField txtTypeFilePath;
	private String txtTypeMsg = "Digite uma mensagem";
	private String txtTypeFile = "Caminho do arquivo";
	private OutputStream outToFriend;
	private InputStream inFromFriend;
	private int statusMsgEnviada;
	private boolean servicoStatusMsgOk;
	
	/**
	 * Create the frame.
	 */
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Chat frame = new Chat("eu", "ele",null, null, null); //new Socket("localhost", 2030));
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public Chat(String usr, String friend, Socket connectionSocket, Socket msgStatusSocket, ArrayList<ServerSocket> SSList) throws IOException {
		// TODO lembrar de mudar p nosso protocolo
		servicoStatusMsgOk = true;
		setTitle(usr + " conversa com " + friend);
		outToFriend = connectionSocket.getOutputStream();
		inFromFriend = connectionSocket.getInputStream();
		
		InputStream msgStatusInput = msgStatusSocket.getInputStream();
		OutputStream msgStatusOutput = msgStatusSocket.getOutputStream();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 549, 537);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{318, 101, 0};
		gbl_contentPane.rowHeights = new int[]{419, 23, 23, 23, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
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
		contentPane.add(scrollPane, gbc_scrollPane);
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
		contentPane.add(scrollPane_1, gbc_scrollPane_1);
		
		JPanel panel = new JPanel();
		scrollPane_1.setViewportView(panel);
		//Thread para receber mensagens
		//lebrar de mudar para nosso protocolo
		Thread msgrcv = new Thread(new ReceiveMessages(friend, inFromFriend, docMsg, styleFrom, msgStatusOutput));
		msgrcv.start();
		//Thread para enviar arquivos
		
		//Thread para receber arquivos
		
		
		txtTypeYourMessage = new JTextField();
		
		JLabel lblMsgInfo = new JLabel("");
		lblMsgInfo.setFont(new Font("Dialog", Font.BOLD, 10));
		lblMsgInfo.setVerticalAlignment(SwingConstants.TOP);
		GridBagConstraints gbc_lblMsgInfo = new GridBagConstraints();
		gbc_lblMsgInfo.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblMsgInfo.insets = new Insets(0, 0, 5, 5);
		gbc_lblMsgInfo.gridx = 0;
		gbc_lblMsgInfo.gridy = 1;
		contentPane.add(lblMsgInfo, gbc_lblMsgInfo);
		txtTypeYourMessage.setText(txtTypeMsg);
		txtTypeYourMessage.setForeground(Color.LIGHT_GRAY);
		GridBagConstraints gbc_txtTypeYourMessage = new GridBagConstraints();
		gbc_txtTypeYourMessage.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTypeYourMessage.insets = new Insets(0, 0, 5, 5);
		gbc_txtTypeYourMessage.gridx = 0;
		gbc_txtTypeYourMessage.gridy = 2;
		contentPane.add(txtTypeYourMessage, gbc_txtTypeYourMessage);
		txtTypeYourMessage.setColumns(10);
		
		JButton btnEnviarMsg = new JButton("Enviar");
		GridBagConstraints gbc_btnEnviarMsg = new GridBagConstraints();
		gbc_btnEnviarMsg.fill = GridBagConstraints.BOTH;
		gbc_btnEnviarMsg.insets = new Insets(0, 0, 5, 0);
		gbc_btnEnviarMsg.gridx = 1;
		gbc_btnEnviarMsg.gridy = 2;
		contentPane.add(btnEnviarMsg, gbc_btnEnviarMsg);
		
		txtTypeFilePath = new JTextField();
		txtTypeFilePath.setForeground(Color.LIGHT_GRAY);
		txtTypeFilePath.setText(txtTypeFile);
		GridBagConstraints gbc_txtTypeFilePath = new GridBagConstraints();
		gbc_txtTypeFilePath.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTypeFilePath.insets = new Insets(0, 0, 0, 5);
		gbc_txtTypeFilePath.gridx = 0;
		gbc_txtTypeFilePath.gridy = 3;
		contentPane.add(txtTypeFilePath, gbc_txtTypeFilePath);
		txtTypeFilePath.setColumns(10);
		
		JButton btnEnviarArquivo = new JButton("Enviar arquivo");
		GridBagConstraints gbc_btnEnviarArquivo = new GridBagConstraints();
		gbc_btnEnviarArquivo.fill = GridBagConstraints.BOTH;
		gbc_btnEnviarArquivo.gridx = 1;
		gbc_btnEnviarArquivo.gridy = 3;
		contentPane.add(btnEnviarArquivo, gbc_btnEnviarArquivo);
		
		Thread msgstatusthread = new Thread(new MsgStatusIn(msgStatusInput, lblMsgInfo));
		msgstatusthread.start();
		
		btnEnviarArquivo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO abrir thread para envio de arquivos
				
			}
		});
		
		txtTypeYourMessage.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					try {
						BufferMethods.writeChatString(txtTypeYourMessage.getText(), outToFriend);
						synchronized (lblMsgInfo) {
							lblMsgInfo.setForeground(Color.ORANGE);
							lblMsgInfo.setText("mensagem enviada");
						}
						synchronized (docMsg) {
							docMsg.insertString(docMsg.getLength(), usr + ": ", styleFrom);
							docMsg.insertString(docMsg.getLength(), txtTypeYourMessage.getText() + '\n', null);
						}
						txtTypeYourMessage.setText("");
					} catch (BadLocationException e) {
						// não deveria dar isto
						e.printStackTrace();
					} catch (Exception e) {
						statusMsgEnviada = 0;
						lblMsgInfo.setForeground(Color.RED);
						lblMsgInfo.setText("mensagem não foi enviada");
						e.printStackTrace();
					}
				}
				if (servicoStatusMsgOk) {
					try {
						msgStatusOutput.write(2);
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
		
		btnEnviarMsg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					BufferMethods.writeChatString(txtTypeYourMessage.getText(), outToFriend);
					synchronized (lblMsgInfo) {
						lblMsgInfo.setForeground(Color.ORANGE);
						lblMsgInfo.setText("mensagem enviada");
					}
					synchronized (docMsg) {
						docMsg.insertString(docMsg.getLength(), usr + ": ", styleFrom);
						docMsg.insertString(docMsg.getLength(), txtTypeYourMessage.getText() + '\n', null);
					}
					txtTypeYourMessage.setText("");
				} catch (BadLocationException e) {
					// não deveria dar isto
					e.printStackTrace();
				} catch (Exception e) {
					statusMsgEnviada = 0;
					lblMsgInfo.setForeground(Color.RED);
					lblMsgInfo.setText("mensagem não foi enviada");
					e.printStackTrace();
				}
			}
		});
		
		txtTypeYourMessage.addFocusListener(new FocusListener() {
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
				if (servicoStatusMsgOk) {
					try {
						msgStatusOutput.write(2);
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
		
		txtTypeFilePath.addFocusListener(new FocusListener() {
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
			}
		});
	}
	
	class MsgStatusIn implements Runnable {
		
		private InputStream msgStatusInput;
		private JLabel lblMsgInfo;

		public MsgStatusIn(InputStream msgStatusInput, JLabel lblMsgInfo) {
			this.msgStatusInput = msgStatusInput;
			this.lblMsgInfo = lblMsgInfo;
		}
		
		public void run() {
			while (true) {
				try {
					int status = msgStatusInput.read();
					if (status == 1) {
						synchronized (lblMsgInfo) {
							lblMsgInfo.setForeground(Color.YELLOW);
							lblMsgInfo.setText("mensagem recebida");
						}
					} else if (status == 2) {
						synchronized (lblMsgInfo) {
							lblMsgInfo.setForeground(Color.GREEN);
							lblMsgInfo.setText("mensagem lida");
						}
					}
				} catch (ConnectException e) {
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
}