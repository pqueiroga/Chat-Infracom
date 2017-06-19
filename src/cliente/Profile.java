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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import java.awt.Color;
import javax.swing.JButton;
import javax.swing.JTextPane;
import cliente.threads.*;

public class Profile extends JFrame {

	private JPanel contentPane;
	private JTextField txtTypeYourFriends;

	/**
	 * Create the frame.
	 */
	//lembrar de mudar para nosso protocolo
	public Profile(ServerSocket rcv_port) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblConnectTo = new JLabel("Request Friendship or Start a Conversation:");
		lblConnectTo.setFont(new Font("Arial", Font.BOLD, 14));
		lblConnectTo.setBounds(20, 0, 349, 33);
		contentPane.add(lblConnectTo);
		
		txtTypeYourFriends = new JTextField();
		txtTypeYourFriends.setForeground(Color.LIGHT_GRAY);
		txtTypeYourFriends.setFont(new Font("Tahoma", Font.PLAIN, 11));
		txtTypeYourFriends.setText("Type your friend's name");
		txtTypeYourFriends.setBounds(10, 33, 363, 20);
		contentPane.add(txtTypeYourFriends);
		txtTypeYourFriends.setColumns(10);
		txtTypeYourFriends.addFocusListener(new FocusListener() {
			
			public void focusLost(FocusEvent e) {
				txtTypeYourFriends.setForeground(Color.LIGHT_GRAY);
				txtTypeYourFriends.setText("Type your friend's name");
			}
			public void focusGained(FocusEvent e) {
				txtTypeYourFriends.setText("");
				txtTypeYourFriends.setForeground(Color.BLACK);
			}
		});
		
		JButton btnStartConversation = new JButton("Invite");
		btnStartConversation.setForeground(new Color(0, 0, 255));
		btnStartConversation.setBounds(243, 61, 130, 20);
		contentPane.add(btnStartConversation);
		
		JTextPane textPane = new JTextPane();
		textPane.setBounds(24, 108, 349, 96);
		contentPane.add(textPane);
		
		JLabel lblFriendsOnline = new JLabel("Friends Online:");
		lblFriendsOnline.setFont(new Font("Arial", Font.BOLD, 14));
		lblFriendsOnline.setBounds(127, 77, 115, 20);
		contentPane.add(lblFriendsOnline);
		
		JButton btnStartConversation_1 = new JButton("Start Conversation");
		btnStartConversation_1.setForeground(new Color(0, 128, 0));
		btnStartConversation_1.setBounds(40, 60, 130, 20);
		contentPane.add(btnStartConversation_1);
		btnStartConversation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
			}
		});
	}
}
