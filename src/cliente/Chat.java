package cliente;
import cliente.threads.*;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JTextPane;
import javax.swing.JScrollPane;
import javax.swing.JButton;
import javax.swing.ImageIcon;
import java.awt.Color;
import java.io.*;
import javax.swing.JProgressBar;
public class Chat extends JFrame {

	private JPanel contentPane;
	private JTextField txtTypeYourMessage;
	private JTextField txtTypeFilePath;
	
	/**
	 * Create the frame.
	 */
	public Chat(String DestinationIP, int DestinationPort, int LocalPort) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(32, 11, 375, 158);
		contentPane.add(scrollPane);
		JTextPane textPane = new JTextPane();
		textPane.setEditable(false);
		scrollPane.setViewportView(textPane);
		
		//Pasta de arquivos
		File folder=new File("./download_folder/");
		folder.mkdir();
		//em todas as threads trocar TCP pelo nosso protocolo
		//Thread para envio de mensagens
		Thread send= new Thread(new SendText(DestinationIP, DestinationPort));
		//Thread para receber mensagens
		Thread msgrcv=new Thread(new RecieveMessages(LocalPort, textPane));
		//Thread para enviar arquivos
		
		//Thread para receber arquivos
		
		
		txtTypeYourMessage = new JTextField();
		txtTypeYourMessage.setText("Type your message here");
		txtTypeYourMessage.setForeground(Color.LIGHT_GRAY);
		txtTypeYourMessage.setBounds(32, 180, 288, 20);
		contentPane.add(txtTypeYourMessage);
		txtTypeYourMessage.setColumns(10);
		txtTypeYourMessage.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				txtTypeYourMessage.setForeground(Color.LIGHT_GRAY);
				txtTypeYourMessage.setText("Type your message here");
			}
			public void focusGained(FocusEvent e) {
				txtTypeYourMessage.setText("");
				txtTypeYourMessage.setForeground(Color.BLACK);
			}
		});
		
		JButton btnInvite = new JButton("Send");
		btnInvite.setBounds(318, 179, 89, 23);
		contentPane.add(btnInvite);
		btnInvite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textPane.setText("You:"+'\n'+'"'+txtTypeYourMessage.getText()+'"'+'\n');
				
			}
		});
		
		JButton btnAddFile = new JButton("Add File");
		btnAddFile.setBounds(318, 202, 89, 23);
		contentPane.add(btnAddFile);
		btnAddFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				//abrir thread para envio de arquivos
				
			}
		});
		
		txtTypeFilePath = new JTextField();
		txtTypeFilePath.setForeground(Color.LIGHT_GRAY);
		txtTypeFilePath.setText("Type file path here");
		txtTypeFilePath.setBounds(32, 203, 288, 20);
		contentPane.add(txtTypeFilePath);
		txtTypeFilePath.setColumns(10);
		
		JProgressBar progressBar = new JProgressBar();
		progressBar.setBounds(32, 234, 375, 16);
		progressBar.setVisible(false);
		contentPane.add(progressBar);
		
		txtTypeFilePath.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				txtTypeFilePath.setForeground(Color.LIGHT_GRAY);
				txtTypeFilePath.setText("Type file path here");
			}
			public void focusGained(FocusEvent e) {
				txtTypeFilePath.setText("");
				txtTypeFilePath.setForeground(Color.BLACK);
			}
		});
	}
}