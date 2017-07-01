package cliente;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.ConnectException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import protocol.DGServerSocket;
import utility.server.ServerAPI;

public class Login extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -662516472107674260L;
	private ServerAPI toServer;
	private JPanel contentPane;
	private JTextField usrTextField;
	private JPasswordField passwordField;
	private JTextField txtIp;
	private JTextField txtPort;
	private JLabel lblNotbroken;
	private boolean notBrokenFinished = false;
	private JButton btnLogin;
	private JLabel lblUsername;
	private JLabel lblUsrInfo;
	private JLabel lblPassword;
	private JLabel lblPwInfo;
	private JLabel lblServ;
	private JLabel lblPorta;
	private JLabel lblIpinfo;
	private JLabel lblPortinfo;
	private JButton btnCadastro;
	
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
		setResizable(false);
		int randomNum = ThreadLocalRandom.current().nextInt(2, 9 + 1);
		Color cor;
		switch (randomNum) {
		case 2:
			cor = Color.BLUE;
			break;
		case 3:
			cor = Color.CYAN;
			break;
		case 4:
			cor = Color.GRAY;
			break;
		case 5:
			cor = Color.LIGHT_GRAY;
			break;
		case 6:
			cor = Color.MAGENTA;
			break;
		case 7:
			cor = Color.PINK;
			break;
		case 8:
			cor = Color.WHITE;
			break;
		case 9:
			cor = Color.YELLOW;
			break;
		default:
			cor = null;	
		}
		setBackground(cor);
		setTitle("untitled spaceship");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 467, 289);
		contentPane = new JPanel();
		contentPane.setBackground(cor);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{5, 80, 80, 80, 80, 5};
		gbl_contentPane.rowHeights = new int[]{31, 17, 0, 23, 0, 0, 23, 0, 0, 23, 0, 12, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 1.0, 0.0, 1.0, 1.0, 0.0};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		lblUsername = new JLabel("Usuário");
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
		
		lblUsrInfo = new JLabel("");
		lblUsrInfo.setForeground(Color.RED);
		lblUsrInfo.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblUsrInfo = new GridBagConstraints();
		gbc_lblUsrInfo.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblUsrInfo.gridwidth = 3;
		gbc_lblUsrInfo.insets = new Insets(0, 0, 5, 5);
		gbc_lblUsrInfo.gridx = 2;
		gbc_lblUsrInfo.gridy = 3;
		contentPane.add(lblUsrInfo, gbc_lblUsrInfo);
		
		lblPassword = new JLabel("Senha");
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
		
		lblPwInfo = new JLabel("");
		lblPwInfo.setForeground(Color.RED);
		lblPwInfo.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblPwInfo = new GridBagConstraints();
		gbc_lblPwInfo.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblPwInfo.gridwidth = 3;
		gbc_lblPwInfo.insets = new Insets(0, 0, 5, 5);
		gbc_lblPwInfo.gridx = 2;
		gbc_lblPwInfo.gridy = 6;
		contentPane.add(lblPwInfo, gbc_lblPwInfo);
		
		lblServ = new JLabel("IP");
		GridBagConstraints gbc_lblServ = new GridBagConstraints();
		gbc_lblServ.anchor = GridBagConstraints.WEST;
		gbc_lblServ.gridwidth = 2;
		gbc_lblServ.insets = new Insets(0, 0, 5, 5);
		gbc_lblServ.gridx = 1;
		gbc_lblServ.gridy = 7;
		contentPane.add(lblServ, gbc_lblServ);
		
		lblPorta = new JLabel("Porta");
		GridBagConstraints gbc_lblPorta = new GridBagConstraints();
		gbc_lblPorta.anchor = GridBagConstraints.WEST;
		gbc_lblPorta.insets = new Insets(0, 0, 5, 5);
		gbc_lblPorta.gridx = 4;
		gbc_lblPorta.gridy = 7;
		contentPane.add(lblPorta, gbc_lblPorta);
		
		txtIp = new JTextField();
		GridBagConstraints gbc_txtIp = new GridBagConstraints();
		gbc_txtIp.gridwidth = 2;
		gbc_txtIp.insets = new Insets(0, 0, 5, 5);
		gbc_txtIp.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtIp.gridx = 1;
		gbc_txtIp.gridy = 8;
		contentPane.add(txtIp, gbc_txtIp);
		txtIp.setColumns(10);
		
		txtPort = new JTextField();
		GridBagConstraints gbc_txtPort = new GridBagConstraints();
		gbc_txtPort.insets = new Insets(0, 0, 5, 5);
		gbc_txtPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtPort.gridx = 4;
		gbc_txtPort.gridy = 8;
		contentPane.add(txtPort, gbc_txtPort);
		txtPort.setColumns(10);
		
		lblIpinfo = new JLabel("");
		lblIpinfo.setForeground(Color.RED);
		lblIpinfo.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblIpinfo = new GridBagConstraints();
		gbc_lblIpinfo.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblIpinfo.gridwidth = 2;
		gbc_lblIpinfo.insets = new Insets(0, 0, 5, 5);
		gbc_lblIpinfo.gridx = 1;
		gbc_lblIpinfo.gridy = 9;
		contentPane.add(lblIpinfo, gbc_lblIpinfo);
		
		lblPortinfo = new JLabel("");
		lblPortinfo.setForeground(Color.RED);
		lblPortinfo.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblPortinfo = new GridBagConstraints();
		gbc_lblPortinfo.gridwidth = 2;
		gbc_lblPortinfo.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblPortinfo.insets = new Insets(0, 0, 5, 5);
		gbc_lblPortinfo.gridx = 3;
		gbc_lblPortinfo.gridy = 9;
		contentPane.add(lblPortinfo, gbc_lblPortinfo);
		
		btnCadastro = new JButton("Cadastrar");
		GridBagConstraints gbc_btnCadastro = new GridBagConstraints();
		gbc_btnCadastro.gridwidth = 2;
		gbc_btnCadastro.fill = GridBagConstraints.BOTH;
		gbc_btnCadastro.insets = new Insets(0, 0, 5, 5);
		gbc_btnCadastro.gridx = 1;
		gbc_btnCadastro.gridy = 10;
		contentPane.add(btnCadastro, gbc_btnCadastro);
		
		btnLogin = new JButton("Entrar");
		GridBagConstraints gbc_btnLogin = new GridBagConstraints();
		gbc_btnLogin.insets = new Insets(0, 0, 5, 5);
		gbc_btnLogin.gridwidth = 2;
		gbc_btnLogin.fill = GridBagConstraints.BOTH;
		gbc_btnLogin.gridx = 3;
		gbc_btnLogin.gridy = 10;
		contentPane.add(btnLogin, gbc_btnLogin);
		
		lblNotbroken = new JLabel("     ");
		GridBagConstraints gbc_lblNotbroken = new GridBagConstraints();
		gbc_lblNotbroken.anchor = GridBagConstraints.SOUTHWEST;
		gbc_lblNotbroken.gridwidth = 4;
		gbc_lblNotbroken.insets = new Insets(0, 0, 0, 5);
		gbc_lblNotbroken.gridx = 1;
		gbc_lblNotbroken.gridy = 11;
		contentPane.add(lblNotbroken, gbc_lblNotbroken);
		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				usrTextField.setEnabled(false);
				passwordField.setEnabled(false);
				btnCadastro.setEnabled(false);
				btnLogin.setEnabled(false);
				txtIp.setEnabled(false);
				txtPort.setEnabled(false);
				String usrOk = validaUsr(usrTextField.getText());
				String pwOk = validaPw(new String(passwordField.getPassword()));
				String ipOk = validaIp(txtIp.getText());
				String portOk = validaPort(txtPort.getText());
				if (usrOk.isEmpty() && pwOk.isEmpty() && ipOk.isEmpty() && portOk.isEmpty()) {
					(new Thread(new loga())).start();
//					try {
//						notBrokenFinished = false;
////						(new Thread(new naoQuebrou())).start();
//						toServer = new ServerAPI(txtIp.getText(), Integer.parseInt(txtPort.getText()));
//						Map.Entry<ArrayList<DGServerSocket>, Integer> mp = toServer.login(usrTextField.getText(), new String(passwordField.getPassword()));
//						notBrokenFinished = true;
//						int status = mp.getValue().intValue();
//						if (status == 1) {
//							Profile p = new Profile(mp.getKey(), usrTextField.getText(), txtIp.getText(), Integer.parseInt(txtPort.getText()));
//							p.setVisible(true);
//							setVisible(false); // assim poderíamos fazer setVisible(true) qdo fechasse a janela que essa abre.
//							lblUsrInfo.setForeground(Color.GREEN);
//							lblUsrInfo.setText("Login efetuado com sucesso"); // esse daqui nunca vai ser visto
//						} else if (status == 2) {
//							lblUsrInfo.setForeground(Color.RED);
//							lblUsrInfo.setText("Usuário já está online");
//						} else if (status == 0) {
//							lblUsrInfo.setForeground(Color.RED);
//							lblUsrInfo.setText("Usuário ou senha incorretos");
//						} else if (status == -1) {
//							lblUsrInfo.setForeground(Color.RED);
//							lblUsrInfo.setText("Não conseguimos portas livres");
//						}
//					} catch (ConnectException e1) {
//						notBrokenFinished = true;
//						if (e1.getMessage().equals("Connection refused (Connection refused)")) {
//							lblUsrInfo.setForeground(Color.RED);
//							lblUsrInfo.setText("Não foi possível se conectar ao servidor");
//						}
//					} catch (Exception e1) {
//						notBrokenFinished = true;
//						lblUsrInfo.setForeground(Color.RED);
//						lblUsrInfo.setText("Erro tosco durante o login");
//						e1.printStackTrace();
//					}
				} else {
					lblUsrInfo.setForeground(Color.RED);
					lblUsrInfo.setText(usrOk);
					lblPwInfo.setText(pwOk);
					lblPortinfo.setText(portOk);
					lblIpinfo.setText(ipOk);
					usrTextField.setEnabled(true);
					passwordField.setEnabled(true);
					btnCadastro.setEnabled(true);
					btnLogin.setEnabled(true);
					txtIp.setEnabled(true);
					txtPort.setEnabled(true);
				}
//				usrTextField.setEnabled(true);
//				passwordField.setEnabled(true);
//				btnCadastro.setEnabled(true);
//				btnLogin.setEnabled(true);
			}
		});
		btnCadastro.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				usrTextField.setEnabled(false);
				passwordField.setEnabled(false);
				btnCadastro.setEnabled(false);
				btnLogin.setEnabled(false);
				txtIp.setEnabled(false);
				txtPort.setEnabled(false);
				String usrOk = validaUsr(usrTextField.getText());
				String pwOk = validaPw(new String(passwordField.getPassword()));
				String ipOk = validaIp(txtIp.getText());
				String portOk = validaPort(txtPort.getText());
				if (usrOk.isEmpty() && pwOk.isEmpty() && ipOk.isEmpty() && portOk.isEmpty()) {
					(new Thread(new cadastra())).start();
//					try {
//						notBrokenFinished = false;
////						(new Thread(new naoQuebrou())).start(); 
//						// TODO DESCOBRIR COMO FAZER A GUI NÃO FICAR FREEZADA POR ESSES LISTENERS
//						toServer = new ServerAPI(txtIp.getText(), Integer.parseInt(txtPort.getText()));
//						int b = toServer.cadastro(usrTextField.getText(), new String(passwordField.getPassword()));
//						notBrokenFinished = true;
//						if (b == 1) {
//							lblUsrInfo.setForeground(Color.GREEN);
//							lblUsrInfo.setText("Cadastro efetuado com sucesso: " + usrTextField.getText());
//						} else if (b == 2) {
//							lblUsrInfo.setForeground(Color.RED);
//							lblUsrInfo.setText("Nome de usuário indisponível: " + usrTextField.getText());
//						} else if (b == 0 || b == -1) {
//							lblUsrInfo.setForeground(Color.RED);
//							lblUsrInfo.setText("Não foi possível cadastrar: " + usrTextField.getText());
//						} else if (b == 3) {
//							lblUsrInfo.setForeground(Color.RED);
//							lblUsrInfo.setText("Nome de usuário ou senha inválidos: " + usrTextField.getText());
//						}
//					} catch (ConnectException e1) {
//						notBrokenFinished = true;
//						if (e1.getMessage().equals("Connection refused (Connection refused)")) {
//							lblUsrInfo.setForeground(Color.RED);
//							lblUsrInfo.setText("Não foi possível se conectar ao servidor");
//						}
//					} catch (IOException e1) {
//						notBrokenFinished = true;
//						lblUsrInfo.setForeground(Color.RED);
//						lblUsrInfo.setText("Não foi possível cadastrar: " + usrTextField.getText());
//						e1.printStackTrace();
//					} catch (GeneralSecurityException e1) {
//						notBrokenFinished = true;
//						lblUsrInfo.setForeground(Color.RED);
//						lblUsrInfo.setText("Não foi possível cadastrar: " + usrTextField.getText());
//						e1.printStackTrace();
//					}
				} else {
					lblUsrInfo.setForeground(Color.RED);
					lblUsrInfo.setText(usrOk);
					lblPwInfo.setText(pwOk);
					lblPortinfo.setText(portOk);
					lblIpinfo.setText(ipOk);
					usrTextField.setEnabled(true);
					passwordField.setEnabled(true);
					btnCadastro.setEnabled(true);
					btnLogin.setEnabled(true);
					txtIp.setEnabled(true);
					txtPort.setEnabled(true);
				}
//				usrTextField.setEnabled(true);
//				passwordField.setEnabled(true);
//				btnCadastro.setEnabled(true);
//				btnLogin.setEnabled(true);
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
				String checa = new String(passwordField.getPassword());
				lblPwInfo.setText(validaPw(checa));
			}
		});
		
		txtPort.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				lblPortinfo.setForeground(Color.RED);
				String checa = txtPort.getText();
				lblPortinfo.setText(validaPort(checa));
			}
		});
		txtIp.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				lblIpinfo.setForeground(Color.RED);
				String checa = txtIp.getText();
				lblIpinfo.setText(validaIp(checa));
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
		
		txtPort.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				lblPortinfo.setForeground(Color.RED);
				String checa = txtPort.getText();
				if (!checa.isEmpty()) {
					lblPortinfo.setText(validaPort(checa));
				} else {
					lblPortinfo.setText("");
				}
			}
		});
		
		txtIp.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				lblIpinfo.setForeground(Color.RED);
				String checa = txtIp.getText();
				if (!checa.isEmpty()) {
					lblIpinfo.setText(validaIp(checa));
				} else {
					lblIpinfo.setText("");
				}
			}
		});
		
	}
	
	class cadastra implements Runnable {
		public void run() {
			try {
				notBrokenFinished = false;
				(new Thread(new naoQuebrou())).start(); 
				toServer = new ServerAPI(txtIp.getText(), Integer.parseInt(txtPort.getText()));
				int b = toServer.cadastro(usrTextField.getText(), new String(passwordField.getPassword()));
				notBrokenFinished = true;
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
			} catch (ConnectException e1) {
				notBrokenFinished = true;
				if (e1.getMessage().equals("Connection refused (Connection refused)")) {
					lblUsrInfo.setForeground(Color.RED);
					lblUsrInfo.setText("Não foi possível se conectar ao servidor");
				}
			} catch (IOException e1) {
				notBrokenFinished = true;
				lblUsrInfo.setForeground(Color.RED);
				lblUsrInfo.setText("Não foi possível cadastrar: " + usrTextField.getText());
				e1.printStackTrace();
			} catch (GeneralSecurityException e1) {
				notBrokenFinished = true;
				lblUsrInfo.setForeground(Color.RED);
				lblUsrInfo.setText("Não foi possível cadastrar: " + usrTextField.getText());
				e1.printStackTrace();
			} finally {
				usrTextField.setEnabled(true);
				passwordField.setEnabled(true);
				btnCadastro.setEnabled(true);
				btnLogin.setEnabled(true);
				txtIp.setEnabled(true);
				txtPort.setEnabled(true);
			}
		}
	}
	
	class loga implements Runnable { 
		public void run() {
			try {
				notBrokenFinished = false;
				(new Thread(new naoQuebrou())).start();
				toServer = new ServerAPI(txtIp.getText(), Integer.parseInt(txtPort.getText()));
				Map.Entry<ArrayList<DGServerSocket>, Integer> mp = toServer.login(usrTextField.getText(), new String(passwordField.getPassword()));
				notBrokenFinished = true;
				int status = mp.getValue().intValue();
				if (status == 1) {
					Profile p = new Profile(mp.getKey(), usrTextField.getText(), txtIp.getText(), Integer.parseInt(txtPort.getText()));
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
				} else if (status == -1) {
					lblUsrInfo.setForeground(Color.RED);
					lblUsrInfo.setText("Não conseguimos portas livres");
				}
			} catch (ConnectException e1) {
				notBrokenFinished = true;
				if (e1.getMessage().equals("Connection refused (Connection refused)")) {
					lblUsrInfo.setForeground(Color.RED);
					lblUsrInfo.setText("Não foi possível se conectar ao servidor");
				}
			} catch (Exception e1) {
				notBrokenFinished = true;
				lblUsrInfo.setForeground(Color.RED);
				lblUsrInfo.setText("Erro tosco durante o login");
				e1.printStackTrace();
			} finally {
				usrTextField.setEnabled(true);
				passwordField.setEnabled(true);
				btnCadastro.setEnabled(true);
				btnLogin.setEnabled(true);
				txtIp.setEnabled(true);
				txtPort.setEnabled(true);
			}
		}
	}
	
	class naoQuebrou implements Runnable {
		public void run() {
			lblNotbroken.setText(".");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			while (!notBrokenFinished) {
				if (lblNotbroken.getText().length() > 15) {
					lblNotbroken.setText("");
				}
				lblNotbroken.setText(lblNotbroken.getText() + ".");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			lblNotbroken.setText("     ");
		}
	}
	
	static String validaIp(String ip) {
		if (!(ip.matches("([0-9]{1,3}\\.){3}[0-9]{1,3}") || ip.equals("localhost"))) {
			return "IP inválido";
		} else {
			return "";
		}
	}
	
	static String validaPort(String port) {
		try {
			int porta = Integer.parseInt(port);
			if (porta > 65535 || porta < 1) {
				return "Porta inválida";
			} else {
				return "";
			}
		} catch (NumberFormatException e) {
			return "Porta inválida";
		}
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
