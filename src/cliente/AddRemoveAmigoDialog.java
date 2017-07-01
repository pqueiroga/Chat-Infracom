package cliente;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import utility.server.ServerAPI;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class AddRemoveAmigoDialog extends JDialog {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2192680467119389610L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textFieldFriend;
	
	public AddRemoveAmigoDialog(int[] pktsPerdidos, String username, String ip, int port) {
		ServerAPI toServer = new ServerAPI(pktsPerdidos, ip, port);
		int randomNum = ThreadLocalRandom.current().nextInt(3, 9 + 1);
		Color cor;
		switch (randomNum) {
		case 3:
			cor = Color.BLUE;
			break;
		case 4:
			cor = Color.CYAN;
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
		getContentPane().setBackground(cor);
		randomNum = ThreadLocalRandom.current().nextInt(3, 9 + 1);
		switch (randomNum) {
		case 3:
			cor = Color.BLUE;
			break;
		case 4:
			cor = Color.CYAN;
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
		setBackground(cor);
		setResizable(false);
		setTitle("Adicionar amigo");
		setBounds(100, 100, 323, 200);
		getContentPane().setLayout(new BorderLayout());
		randomNum = ThreadLocalRandom.current().nextInt(3, 9 + 1);
		switch (randomNum) {
		case 3:
			cor = Color.BLUE;
			break;
		case 4:
			cor = Color.CYAN;
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
		contentPanel.setBackground(cor);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		GridBagLayout gbl_contentPanel = new GridBagLayout();
		gbl_contentPanel.columnWidths = new int[]{226, 0};
		gbl_contentPanel.rowHeights = new int[]{15, 28, 15, 0};
		gbl_contentPanel.columnWeights = new double[]{0.0, Double.MIN_VALUE};
		gbl_contentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPanel.setLayout(gbl_contentPanel);
		
		JLabel lblFriend = new JLabel("Nome de usuário do amigo");
		GridBagConstraints gbc_lblFriend = new GridBagConstraints();
		gbc_lblFriend.anchor = GridBagConstraints.NORTH;
		gbc_lblFriend.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblFriend.insets = new Insets(0, 0, 5, 0);
		gbc_lblFriend.gridx = 0;
		gbc_lblFriend.gridy = 0;
		contentPanel.add(lblFriend, gbc_lblFriend);
		textFieldFriend = new JTextField();


		GridBagConstraints gbc_textFieldFriend = new GridBagConstraints();
		gbc_textFieldFriend.fill = GridBagConstraints.BOTH;
		gbc_textFieldFriend.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldFriend.gridx = 0;
		gbc_textFieldFriend.gridy = 1;
		contentPanel.add(textFieldFriend, gbc_textFieldFriend);
		textFieldFriend.setColumns(10);
		
		JLabel lblInfo = new JLabel("");
		lblInfo.setFont(new Font("Dialog", Font.BOLD, 10));
		GridBagConstraints gbc_lblInfo = new GridBagConstraints();
		gbc_lblInfo.anchor = GridBagConstraints.NORTHEAST;
		gbc_lblInfo.gridx = 0;
		gbc_lblInfo.gridy = 2;
		contentPanel.add(lblInfo, gbc_lblInfo);
		{
			JPanel buttonPane = new JPanel();
			randomNum = ThreadLocalRandom.current().nextInt(3, 9 + 1);
			switch (randomNum) {
			case 3:
				cor = Color.BLUE;
				break;
			case 4:
				cor = Color.CYAN;
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
			buttonPane.setBackground(cor);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				
				JButton btnAdicionar = new JButton("Adicionar");
				btnAdicionar.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						try {
							String usrOk = Login.validaUsr(textFieldFriend.getText());
							if (usrOk.isEmpty()) {
								int status = toServer.solicitaAmizade(username, textFieldFriend.getText());
								if (status == -1) {
									lblInfo.setForeground(Color.RED);
									lblInfo.setText("Servidor não conseguiu se conectar ao banco de dados");
								} else if (status == 0) {
									lblInfo.setForeground(Color.RED);
									lblInfo.setText("Não foi possível adicionar " + textFieldFriend.getText());
								} else if (status == 1) {
									lblInfo.setForeground(Color.GREEN);
									lblInfo.setText("Solicitação enviada com sucesso");
								} else if (status == 2) {
									lblInfo.setForeground(Color.ORANGE);
									lblInfo.setText("Usuário não existe, ou vocês já são amigos/pendentes");
								}
							} else {
								lblInfo.setForeground(Color.RED);
								lblInfo.setText(usrOk);
							}
						} catch (IOException e1) {
							lblInfo.setForeground(Color.RED);
							lblInfo.setText("Não foi possível se conectar ao servidor");
							e1.printStackTrace();
						}
					}
				});
				buttonPane.add(btnAdicionar);
			}
			{
				JButton cancelButton = new JButton("Fechar");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				
				JButton btnRemover = new JButton("Remover");
				btnRemover.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						String usrOk = Login.validaUsr(textFieldFriend.getText());
						if (usrOk.isEmpty()) {
							String[] opcoes = {"Sim", "Não"};
							int yesno = JOptionPane.showOptionDialog(null, "Tem certeza que deseja remover "
									+ textFieldFriend.getText() +"?", "Remoção de amigo", JOptionPane.YES_NO_OPTION,
									JOptionPane.PLAIN_MESSAGE, null, opcoes, opcoes[0]);
							if (yesno == JOptionPane.YES_OPTION) {
								try {
									int status = toServer.removerAmigo(username, textFieldFriend.getText());
									if (status == -1) {
										lblInfo.setForeground(Color.RED);
										lblInfo.setText("Servidor não conseguiu se conectar ao banco de dados");
									} else if (status == 0) {
										lblInfo.setForeground(Color.RED);
										lblInfo.setText("Não foi possível remover " + textFieldFriend.getText());
									} else if (status == 1) {
										lblInfo.setForeground(Color.GREEN);
										lblInfo.setText("Remoção concluída");
									}
								} catch (IOException e1) {
									lblInfo.setForeground(Color.RED);
									lblInfo.setText("Não foi possível se conectar ao servidor");
									e1.printStackTrace();
								}
							}
						} else {
							lblInfo.setForeground(Color.RED);
							lblInfo.setText(usrOk);
						}
					}
				});
				buttonPane.add(btnRemover);
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		textFieldFriend.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent arg0) {
				lblInfo.setForeground(Color.RED);
				String checa = textFieldFriend.getText();
				lblInfo.setText(Login.validaUsr(checa));
			}
		});
		
		textFieldFriend.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				lblInfo.setForeground(Color.RED);
				String checa = textFieldFriend.getText();
				if (checa.matches(".*[^A-Za-z0-9].*")) {
					lblInfo.setText("Caractere inválido");
				} else if (checa.length() > 20) {
					lblInfo.setText("Nome de usuário grande demais");
				} else if (checa.length() < 3 && !checa.isEmpty()) {
					lblInfo.setText("Nome de usuário pequeno demais");
				} else {
					lblInfo.setText("");
				}
			}
		});
	}
}
