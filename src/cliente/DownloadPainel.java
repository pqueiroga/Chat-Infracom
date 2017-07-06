package cliente;

import java.awt.EventQueue;
import java.io.File;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class DownloadPainel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4828429621225721687L;
	public int percentage = 0;
	private JButton btnAbrir;
	private JLabel lblRTT;
	JLabel lblAberto;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					new DownloadPainel(null, new JProgressBar(), new JButton());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the panel.
	 */
	public DownloadPainel(File file, JProgressBar progressBar, JButton btnAbrir) {
		this.btnAbrir = btnAbrir;
		int teste = 133;
		this.setSize(teste, 87);
		
		JLabel lblNomearquivo = new JLabel(file.getName());
		lblNomearquivo.setToolTipText(lblNomearquivo.getText());
		lblRTT = new JLabel("rtt: ");
		lblRTT.setToolTipText("teste do caralho");
		
		lblAberto = new JLabel("");
		
		DownloadPainel.this.btnAbrir.setToolTipText("Abrir " + file.getName());
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
						.addComponent(progressBar, teste, teste, teste)
						.addComponent(DownloadPainel.this.btnAbrir, teste, teste, teste)
						.addComponent(lblNomearquivo, teste, teste, teste)
						.addComponent(lblRTT, teste, teste, teste)
						.addComponent(lblAberto, teste, teste, teste))
					.addContainerGap(teste, teste))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addComponent(lblNomearquivo)
					.addGap(5)
					.addComponent(progressBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addGap(5)
					.addComponent(DownloadPainel.this.btnAbrir)
					.addGap(3)
					.addComponent(lblRTT)
					.addGap(3)
					.addComponent(lblAberto)
					.addGap(12))
		);
		setLayout(groupLayout);
	}
	
	public void setLblAberto(String status) {
		lblAberto.setText(status);
		lblAberto.setToolTipText(lblAberto.getText());
	}
	
	public void killlblRTT() {
		this.remove(lblRTT);
		repaint();
		validate();
	}
	
	public void setLblRTT(long rtt) {
		lblRTT.setText("rtt: " + rtt + " ms");
	}
	
	public void setAbrir(boolean bulian) {
		btnAbrir.setEnabled(bulian);
	}
}
