package cliente;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import cliente.threads.RecieveMessages;
import utility.server.ServerAPI;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.Timer;

import cliente.threads.*;
import java.net.*;
import java.util.ArrayList;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.ScrollPane;

import javax.swing.JList;
import javax.swing.JScrollPane;
import java.awt.FlowLayout;

public class Profile extends JFrame {

	private JPanel contentPane;
	private JPanel panel;
	private int delay;
	private int servNResponde;
	private Profile frame;
	/**
	 * Create the frame.
	 */
	
//	public static void main(String[] args) {
//		EventQueue.invokeLater(new Runnable() {
//			public void run() {
//				try {
//					Profile frame = new Profile(new ArrayList<ServerSocket>(), "Pedro?", new ServerAPI("localhost", 2020));
//					frame.setVisible(true);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//	}
	//lembrar de mudar para nosso protocolo
	public Profile(ArrayList<ServerSocket> listenList, String username, String ip, int port) {
		this.delay = 5000;
		this.servNResponde = 0;
		this.frame = this;
		ServerAPI toServer = new ServerAPI(ip, port);
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
			}
		});
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 328, 530);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JLabel lblNomeDeUsurio = new JLabel("NOME DE USUÁRIO");
		lblNomeDeUsurio.setFont(new Font("Dialog", Font.BOLD, 16));
		GridBagConstraints gbc_lblNomeDeUsurio = new GridBagConstraints();
		gbc_lblNomeDeUsurio.insets = new Insets(0, 0, 5, 0);
		gbc_lblNomeDeUsurio.anchor = GridBagConstraints.WEST;
		gbc_lblNomeDeUsurio.gridx = 0;
		gbc_lblNomeDeUsurio.gridy = 0;
		contentPane.add(lblNomeDeUsurio, gbc_lblNomeDeUsurio);
		
		JLabel lblListaDeAmigos = new JLabel("Lista de amigos");
		GridBagConstraints gbc_lblListaDeAmigos = new GridBagConstraints();
		gbc_lblListaDeAmigos.insets = new Insets(0, 0, 5, 0);
		gbc_lblListaDeAmigos.anchor = GridBagConstraints.WEST;
		gbc_lblListaDeAmigos.gridx = 0;
		gbc_lblListaDeAmigos.gridy = 1;
		contentPane.add(lblListaDeAmigos, gbc_lblListaDeAmigos);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 2;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		panel = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		scrollPane.setViewportView(panel);
		
		try {
			ArrayList<String> amigos = toServer.pegaAmigos(username);
			for (String str : amigos) {
				System.out.println(str);
				panel.add(new JButton(str));
			}
			panel.validate();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		ActionListener updatesFL = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
		    	ServerAPI toServerConc = new ServerAPI(ip, port);
				panel.removeAll();
				panel.repaint();
				try {
					ArrayList<String> amigos = toServerConc.pegaAmigos(username);
					for (String str : amigos) {
						System.out.println(str);
						panel.add(new JButton(str));
					}
					panel.validate();
				} catch (ConnectException e) {
					// TODO JOptionPane para que o cliente decida se quer fechar o programa
					// ou não, já que ele pode continuar falando com quem ele já está falando
					if (servNResponde < 5) {
						servNResponde++;
						System.out.println("Servidor não respondeu, tentando reconectar...");
					} else {
						System.out.println("Servidor aparentemente offline. Fechando o programa.");
						frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
					}
				} catch (IOException e) {
					if (servNResponde < 5) {
						servNResponde++;
					} else {
						System.out.println("Servidor aparentemente offline. Fechando programa.");
						frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
					}			
					e.printStackTrace();
				} 
				System.out.println("Estou esperando 5s!");
			}
		};
		new Timer(delay, updatesFL).start();
	}
}
