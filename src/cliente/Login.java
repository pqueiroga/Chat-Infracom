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
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Map;
import javax.swing.JTextPane;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.Color;
public class Login extends JFrame {
	private ServerAPI toServer;
	private JPanel contentPane;
	private JTextField usrTextField;
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
		toServer = new ServerAPI("localhost", 2020);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 376, 289);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{5, 80, 80, 80, 80, 5};
		gbl_contentPane.rowHeights = new int[]{31, 17, 0, 23, 0, 0, 47, 0, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JLabel lblUsername = new JLabel("Usuário");
		lblUsername.setFont(new Font("Arial", Font.BOLD, 14));
		GridBagConstraints gbc_lblUsername = new GridBagConstraints();
		gbc_lblUsername.gridwidth = 4;
		gbc_lblUsername.fill = GridBagConstraints.BOTH;
		gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
		gbc_lblUsername.gridx = 1;
		gbc_lblUsername.gridy = 1;
		contentPane.add(lblUsername, gbc_lblUsername);
		
		usrTextField = new JTextField();
		GridBagConstraints gbc_usrTextField = new GridBagConstraints();
		gbc_usrTextField.fill = GridBagConstraints.HORIZONTAL;
		gbc_usrTextField.insets = new Insets(0, 0, 5, 5);
		gbc_usrTextField.gridwidth = 4;
		gbc_usrTextField.gridx = 1;
		gbc_usrTextField.gridy = 2;
		contentPane.add(usrTextField, gbc_usrTextField);
		usrTextField.setColumns(10);
		
		JLabel lblUsrInfo = new JLabel("");
		lblUsrInfo.setForeground(Color.RED);
		lblUsrInfo.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblUsrInfo = new GridBagConstraints();
		gbc_lblUsrInfo.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblUsrInfo.gridwidth = 3;
		gbc_lblUsrInfo.insets = new Insets(0, 0, 5, 5);
		gbc_lblUsrInfo.gridx = 2;
		gbc_lblUsrInfo.gridy = 3;
		contentPane.add(lblUsrInfo, gbc_lblUsrInfo);
		
		JLabel lblPassword = new JLabel("Senha");
		lblPassword.setFont(new Font("Arial", Font.BOLD, 14));
		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
		gbc_lblPassword.gridwidth = 4;
		gbc_lblPassword.anchor = GridBagConstraints.NORTH;
		gbc_lblPassword.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblPassword.insets = new Insets(0, 0, 5, 5);
		gbc_lblPassword.gridx = 1;
		gbc_lblPassword.gridy = 4;
		contentPane.add(lblPassword, gbc_lblPassword);
		
		passwordField = new JPasswordField();
		GridBagConstraints gbc_passwordField = new GridBagConstraints();
		gbc_passwordField.fill = GridBagConstraints.BOTH;
		gbc_passwordField.insets = new Insets(0, 0, 5, 5);
		gbc_passwordField.gridwidth = 4;
		gbc_passwordField.gridx = 1;
		gbc_passwordField.gridy = 5;
		contentPane.add(passwordField, gbc_passwordField);
		
		JLabel lblPwInfo = new JLabel("");
		lblPwInfo.setForeground(Color.RED);
		lblPwInfo.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblPwInfo = new GridBagConstraints();
		gbc_lblPwInfo.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblPwInfo.gridwidth = 3;
		gbc_lblPwInfo.insets = new Insets(0, 0, 5, 5);
		gbc_lblPwInfo.gridx = 2;
		gbc_lblPwInfo.gridy = 6;
		contentPane.add(lblPwInfo, gbc_lblPwInfo);
		
		JButton btnCadastro = new JButton("Cadastrar");
		GridBagConstraints gbc_btnCadastro = new GridBagConstraints();
		gbc_btnCadastro.gridwidth = 2;
		gbc_btnCadastro.fill = GridBagConstraints.BOTH;
		gbc_btnCadastro.insets = new Insets(0, 0, 0, 5);
		gbc_btnCadastro.gridx = 1;
		gbc_btnCadastro.gridy = 7;
		contentPane.add(btnCadastro, gbc_btnCadastro);
		
		JButton btnLogin = new JButton("Entrar");
		GridBagConstraints gbc_btnLogin = new GridBagConstraints();
		gbc_btnLogin.insets = new Insets(0, 0, 0, 5);
		gbc_btnLogin.gridwidth = 2;
		gbc_btnLogin.fill = GridBagConstraints.BOTH;
		gbc_btnLogin.gridx = 3;
		gbc_btnLogin.gridy = 7;
		contentPane.add(btnLogin, gbc_btnLogin);
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				usrTextField.setEnabled(false);
				passwordField.setEnabled(false);
				String usrOk = validaUsr(usrTextField.getText());
				String pwOk = validaPw(new String(passwordField.getPassword()));
				if (usrOk.isEmpty() && pwOk.isEmpty()) {
					try {
						Map.Entry<ArrayList<ServerSocket>, Integer> mp = toServer.login(usrTextField.getText(), new String(passwordField.getPassword()));
						int status = mp.getValue().intValue();
						if (status == 1) {
		//					//lembrar de mudar p o nosso protocolo
							Profile p=new Profile(null);
							p.setVisible(true);
							setVisible(false); // assim poderíamos fazer setVisible(true) qdo fechasse a janela que essa abre.
							lblUsrInfo.setForeground(Color.GREEN);
							lblUsrInfo.setText("Login efetuado com sucesso"); // esse daqui nunca vai ser visto
						} else if (status == 2) {
							lblUsrInfo.setForeground(Color.RED);
							lblUsrInfo.setText("Usuário já está online");
						} else if (status == 0) {
							lblUsrInfo.setForeground(Color.RED);
							lblUsrInfo.setText("Usuário ou senha incorretos");
						}
					} catch (Exception e1) {
						lblUsrInfo.setForeground(Color.RED);
						lblUsrInfo.setText("Erro tosco durante o login");
						e1.printStackTrace();
					}
				} else {
					lblUsrInfo.setForeground(Color.RED);
					lblUsrInfo.setText(usrOk);
					lblPwInfo.setForeground(Color.RED);
					lblPwInfo.setText(pwOk);
				}
				usrTextField.setEnabled(true);
				passwordField.setEnabled(true);
			}
		});
		btnCadastro.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				usrTextField.setEnabled(false);
				passwordField.setEnabled(false);
				btnCadastro.setEnabled(false);
				btnLogin.setEnabled(false);
				System.out.println(usrTextField.getText());
				String usrOk = validaUsr(usrTextField.getText());
				String pwOk = validaPw(new String(passwordField.getPassword()));
				if (usrOk.isEmpty() && pwOk.isEmpty()) {
					try {
						int b = toServer.cadastro(usrTextField.getText(), new String(passwordField.getPassword()));
						if (b == 1) {
							lblUsrInfo.setForeground(Color.GREEN);
							lblUsrInfo.setText("Cadastro efetuado com sucesso: " + usrTextField.getText());
						} else if (b == 2) {
							lblUsrInfo.setForeground(Color.RED);
							lblUsrInfo.setText("Nome de usuário indisponível: " + usrTextField.getText());
						} else if (b == 0 || b == -1) {
							lblUsrInfo.setForeground(Color.RED);
							lblUsrInfo.setText("Não foi possível cadastrar: " + usrTextField.getText());
						} else if (b == 3) {
							lblUsrInfo.setForeground(Color.RED);
							lblUsrInfo.setText("Nome de usuário ou senha inválidos: " + usrTextField.getText());
						}
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (GeneralSecurityException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else {
					lblUsrInfo.setForeground(Color.RED);
					lblUsrInfo.setText(usrOk);
					lblPwInfo.setForeground(Color.RED);
					lblPwInfo.setText(pwOk);
				}
				usrTextField.setEnabled(true);
				passwordField.setEnabled(true);
				btnCadastro.setEnabled(true);
				btnLogin.setEnabled(true);
			}
		});
		
		usrTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				lblUsrInfo.setForeground(Color.RED);
				String checa = usrTextField.getText();
				lblUsrInfo.setText(validaUsr(checa));
			}
		});
		passwordField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				lblUsrInfo.setForeground(Color.RED);
				String checa = new String(passwordField.getPassword());
				lblPwInfo.setText(validaPw(checa));
			}
		});
		
		usrTextField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				lblUsrInfo.setForeground(Color.RED);
				String checa = usrTextField.getText();
				if (checa.matches(".*[^A-Za-z0-9].*")) {
					lblUsrInfo.setText("Caractere inválido");
				} else if (checa.length() > 20) {
					lblUsrInfo.setText("Nome de usuário grande demais");
				} else if (checa.length() < 3 && !checa.isEmpty()) {
					lblUsrInfo.setText("Nome de usuário pequeno demais");
				} else {
					lblUsrInfo.setText("");
				}
			}
		});
		
		passwordField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				lblUsrInfo.setForeground(Color.RED);
				String checa = new String(passwordField.getPassword());
				if (checa.matches(".*[^A-Za-z0-9!@#\\$%&\\*\\(\\)\\-_=\\+\\[\\{\\]\\}].*")) {
					lblPwInfo.setText("Caractere inválido");
				} else if (checa.length() > 30) {
					lblPwInfo.setText("Senha grande demais");
				} else if (checa.length() < 3 && !checa.isEmpty()) {
					lblPwInfo.setText("Senha pequena demais");
				} else {
					lblPwInfo.setText("");
				}
			}
		});
		
	}
	
	static String validaUsr(String usr) {
		if (usr.matches(".*[^A-Za-z0-9].*")) {
			return "Caractere inválido";
		} else if (usr.length() > 20) {
			return "Nome de usuário grande demais";
		} else if (usr.length() < 3) {
			return "Nome de usuário pequeno demais";
		} else {
			return "";
		}
	}
	
	static String validaPw(String pw) {
		if (pw.matches(".*[^A-Za-z0-9!@#\\$%&\\*\\(\\)\\-_=\\+\\[\\{\\]\\}].*")) {
			return "Caractere inválido";
		} else if (pw.length() > 30) {
			return "Senha grande demais";
		} else if (pw.length() < 3) {
			return "Senha pequena demais";
		} else {
			return "";
		}
	}
}
