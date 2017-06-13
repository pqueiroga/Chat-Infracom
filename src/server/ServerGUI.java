package server;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

import utility.server.ServerAPI;

/**
 * Janela do servidor
 * @author Pedro Queiroga <psq@cin.ufpe.br>
 *
 */
public class ServerGUI extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerGUI frame = new ServerGUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ServerGUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 300, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0, 0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 240, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JLabel lblListaDeUsuarios = new JLabel("Lista de usuários online");
		GridBagConstraints gbc_lblListaDeUsuarios = new GridBagConstraints();
		gbc_lblListaDeUsuarios.gridwidth = 4;
		gbc_lblListaDeUsuarios.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblListaDeUsuarios.insets = new Insets(0, 0, 5, 5);
		gbc_lblListaDeUsuarios.gridx = 0;
		gbc_lblListaDeUsuarios.gridy = 0;
		contentPane.add(lblListaDeUsuarios, gbc_lblListaDeUsuarios);
		
		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.gridwidth = 4;
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		contentPane.add(scrollPane, gbc_scrollPane);
		
		JTextPane usuariosTextPane = new JTextPane();
		scrollPane.setViewportView(usuariosTextPane);
		usuariosTextPane.setEditable(false);
		
		ArrayList<String> listaDeUsuarios = new ArrayList<String>();
		listaDeUsuarios.add("Pedro");
		listaDeUsuarios.add("Daniel");
		(new Thread(new AtualizaLista(listaDeUsuarios, usuariosTextPane))).start();
		(new Thread(new ServidorComeco(listaDeUsuarios))).start();
		(new Thread(new listTester())).start();
	}
}

/**
 * thread que simula um cliente, para testes apenas.
 * @author Pedro Queiroga <psq@cin.ufpe.br>
 *
 */
class listTester implements Runnable {

	@Override
	public void run() {
		Scanner in = new Scanner(System.in);
		String usr, pw;
		int cadastro;
		while (true) {
			System.out.println("Insira usr");
			usr = in.nextLine();
			System.out.println("Insira pw");
			pw = in.nextLine();
			System.out.println("Cadastro(0) ou Login(1)?");
			cadastro = Integer.parseInt(in.nextLine());
			int codigo = ServerAPI.loginCadastro(usr, pw, cadastro);
			// sucesso
			if (codigo == 1) {
				// login
				if (cadastro == 1) {
					System.out.println("Login efetuado com sucesso");
				} else { // cadastro
					System.out.println("Cadastro efetuado com sucesso");
				}
			} else if (codigo == 0) { // falhou
				// login
				if (cadastro == 1) {
					System.out.println("Usuário ou senha incorretos");
				} else { // cadastro
					System.out.println("Nome de usuário indisponível " + usr);
				}
			} else { // codigo -1
				System.out.println("Usuário já está online");
			}
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
