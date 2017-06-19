package cliente;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import cliente.threads.RecieveMessages;

import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;

import utility.server.*;
import java.net.*;
import java.util.Map;
import javax.swing.JTextPane;
public class Login extends JFrame {
	private ServerAPI toServer;
	private JPanel contentPane;
	private JTextField textField;
	private JPasswordField passwordField;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Login frame = new Login();
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
	public Login() {
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JTextPane warning_window = new JTextPane();
		warning_window.setFont(new Font("Arial", Font.PLAIN, 20));
		warning_window.setEditable(false);
		warning_window.setBounds(34, 31, 336, 39);
		warning_window.setVisible(false);
		contentPane.add(warning_window);
		
		JLabel lblUsername = new JLabel("Username:");
		lblUsername.setFont(new Font("Arial", Font.BOLD, 14));
		lblUsername.setBounds(34, 94, 80, 17);
		contentPane.add(lblUsername);
		
		JLabel lblPassword = new JLabel("Password:");
		lblPassword.setFont(new Font("Arial", Font.BOLD, 14));
		lblPassword.setBounds(34, 137, 80, 17);
		contentPane.add(lblPassword);
		
		textField = new JTextField();
		textField.setBounds(111, 94, 259, 18);
		contentPane.add(textField);
		textField.setColumns(10);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(111, 137, 259, 17);
		contentPane.add(passwordField);
		
		JButton btnLogin = new JButton("Login");
		btnLogin.setBounds(289, 178, 89, 23);
		contentPane.add(btnLogin);
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Map.Entry<ServerSocket, Integer> mp=toServer.login(textField.getText(), new String(passwordField.getPassword()));
				int status=mp.getValue().intValue();
				if(status==1){
					//lembrar de mudar p o nosso protocolo
					Profile p=new Profile(mp.getKey());
					p.setVisible(true);
				}else{
					if(status==0){
						warning_window.setText("Usuario nao Existe");
					}else{
						warning_window.setText("Usuario ja Esta Online");
					}
				}
			}
		});
		
		JButton btnCadastro = new JButton("Sign Up");
		btnCadastro.setBounds(176, 178, 89, 23);
		contentPane.add(btnCadastro);
		btnCadastro.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean b=toServer.cadastro(textField.getText(), new String(passwordField.getPassword()));
				if(b){
					warning_window.setText("Cadastro Realizado com Sucesso!!!");
				}else{
					warning_window.setText("Usuário já Cadastrado!");
				}
				warning_window.setVisible(true);
			}
		});
	}
}
