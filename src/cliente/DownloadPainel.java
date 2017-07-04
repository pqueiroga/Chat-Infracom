package cliente;

import javax.swing.JPanel;
import java.awt.GridBagLayout;
import javax.swing.JProgressBar;

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister.Pack;

import java.awt.Desktop;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;

public class DownloadPainel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4828429621225721687L;
	public int percentage = 0;
	private JButton btnAbrir;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JPanel teste = new DownloadPainel(null, new JProgressBar());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the panel.
	 */
	public DownloadPainel(File file, JProgressBar progressBar) {
		int teste = 120;
		this.setSize(120, 87);
		
		JLabel lblNomearquivo = new JLabel(file.getName());
		
//		JProgressBar progressBar = new JProgressBar();
//		progressBar.setValue(percentage);
		
		btnAbrir = new JButton("Abrir");
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(progressBar, GroupLayout.DEFAULT_SIZE, teste, Short.MAX_VALUE)
						.addComponent(btnAbrir, GroupLayout.DEFAULT_SIZE, teste, Short.MAX_VALUE)
						.addComponent(lblNomearquivo, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(lblNomearquivo)
					.addGap(5)
					.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(5)
					.addComponent(btnAbrir))
					.addGap(80)
		);
		setLayout(groupLayout);
		
		btnAbrir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				try {
					Desktop.getDesktop().open(file);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(DownloadPainel.this,
							"Erro tentando abrir arquivo " + file.getName(),
							"Erro", JOptionPane.WARNING_MESSAGE);
				}
//				String exe = "xdg-open " + file.getAbsolutePath();
//				System.out.println(exe);
//				try {
//					Runtime.getRuntime().exec(exe);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
			}
		});
	}
	
	public void setAbrir(boolean bulian) {
		btnAbrir.setEnabled(bulian);
	}
}
