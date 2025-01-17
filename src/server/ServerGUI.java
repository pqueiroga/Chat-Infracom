package server;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import protocol.DGServerSocket;

/**
 * Janela do servidor
 * @author Pedro Queiroga <psq@cin.ufpe.br>
 *
 */
public class ServerGUI extends JFrame {

	/**
	 * sei lá
	 */
	private static final long serialVersionUID = -2300047174303145972L;
	private JPanel contentPane;
	private JTextField txtPorta;
	ArrayList<String> listaDeUsuarios;
	private DGServerSocket wSocket;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new ServerGUI();
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
		setResizable(false);
		listaDeUsuarios = new ArrayList<String>();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 300, 485);
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
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
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
		
		JLabel lblPacotesperdidos = new JLabel("0");
		GridBagConstraints gbc_lblPacotesperdidos = new GridBagConstraints();
		gbc_lblPacotesperdidos.anchor = GridBagConstraints.EAST;
		gbc_lblPacotesperdidos.gridx = 3;
		gbc_lblPacotesperdidos.gridy = 4;
		contentPane.add(lblPacotesperdidos, gbc_lblPacotesperdidos);
		
		JButton btnIniciar = new JButton("Iniciar");
		btnIniciar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				serverGo(usuariosTextPane, lblPacotesperdidos, btnIniciar);
			}
		});
		splitPane.setLeftComponent(btnIniciar);
		
		txtPorta.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
					serverGo(usuariosTextPane, lblPacotesperdidos, btnIniciar);
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
		 *  A ideia era avisar pra todo mundo que o servidor vai ficar fora do ar.
		 *  Mas não precisa mais, já que o cliente fica cutucando o servidor regularmente.
		 */
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (wSocket != null) {
					try {
						wSocket.close();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
	}

	private void serverGo(JTextPane usuariosTextPane, JLabel lblPacotesperdidos, JButton btnIniciar) {
		try {
			int port = Integer.parseInt(txtPorta.getText());
			wSocket = new DGServerSocket(port, false);
			(new Thread(new AtualizaLista(listaDeUsuarios, usuariosTextPane))).start();
			(new Thread(new ServidorComeco(listaDeUsuarios, wSocket, txtPorta,
					btnIniciar, lblPacotesperdidos))).start();
			setTitle("Funcionando na porta " + port);
			btnIniciar.setEnabled(false);
			txtPorta.setEnabled(false);
		} catch (NumberFormatException e) {
			txtPorta.setText("Porta");
		} catch (BindException e) {
			txtPorta.setText("Porta");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

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
