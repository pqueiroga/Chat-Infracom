package server;

import java.io.IOException;
import java.net.BindException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.swing.JButton;
import javax.swing.JTextField;

import protocol.DGServerSocket;
import protocol.DGSocket;

/**
 * Classe que roda o servidor deste servico na porta "bem conhecida".
 * @author Pedro Queiroga <psq@cin.ufpe.br>
 *
 */
public class ServidorComeco implements Runnable {
	ArrayList<String> listaDeUsuarios;
	DGServerSocket servidor;
	JTextField txtPorta;
	JButton btnIniciar;
	
	public ServidorComeco(ArrayList<String> listaDeUsuarios, DGServerSocket wSocket, JTextField txtPorta, JButton btnIniciar) {
		this.listaDeUsuarios = listaDeUsuarios;
		this.servidor = wSocket;
		this.txtPorta = txtPorta;
		this.btnIniciar = btnIniciar;
	}
	
	public void run() {
		ConcurrentMap<String, Long> timer = new ConcurrentHashMap<String, Long>();
		(new Thread(new TimeOutThread(timer))).start();
		while (true) {
			try {
				DGSocket connectionSocket = servidor.accept();
				(new Thread(new ServidorConta(timer,
						connectionSocket, listaDeUsuarios))).start();
			} catch (SocketException e) {
				e.printStackTrace();
				if (e.getMessage().equals("DGServerSocket já está fechada.")) {
					try {
						servidor = new DGServerSocket(servidor.getLocalPort());
					} catch (Exception e1) {
						txtPorta.setText("Porta");
						txtPorta.setEnabled(true);
						btnIniciar.setEnabled(true);
						e1.printStackTrace();
						return; // a ideia é matar essa thread daqui pra ser revivida com um
						// apertar de botão ou enter, se ela der merda hehe.
					}
				}
			} catch (Exception e) {
				System.err.println("ERRO: Erro desconhecido ao aceitar conexão.");
				e.printStackTrace();
			}
		}
	}
	
	class TimeOutThread implements Runnable {
		private ConcurrentMap<String, Long> timer;
		
		public TimeOutThread(ConcurrentMap<String, Long> timer) {
			this.timer = timer;
		}
		
		public void run() {
			long newTime;
			while (true) {
				synchronized (timer) {
					newTime = System.currentTimeMillis();
					for (ConcurrentMap.Entry<String, Long> entry : timer.entrySet()) {
						if (newTime - entry.getValue().longValue() > 25000) {
							synchronized (listaDeUsuarios) {
								int pos = usuarioListaOnline(listaDeUsuarios, entry.getKey());
								if (pos != -1 ) {
									timer.remove(entry.getKey());
									listaDeUsuarios.remove(pos);
									listaDeUsuarios.notify();
								}
							}
						}
					}	
				}
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		/** copiado pra n tocar no que funciona
		 * 
		 * @param listaDeUsuarios
		 * @param usr
		 * @return
		 */
		private int usuarioListaOnline(ArrayList<String> listaDeUsuarios, String usr) {
			int l = 0, r = listaDeUsuarios.size() - 1, m, comp;
			String str;
			if (r >= 0) {
				do {
					m = (int) ((l + r) / 2);
					str = listaDeUsuarios.get(m);
					comp = (usr + " (").compareToIgnoreCase(str.
							substring(0, str.indexOf('(') + 1));
					if (comp == 0) {
						return m;
					} else if (comp < 0) {
						r = m - 1;
					} else {
						l = m + 1;
					}
				} while (l <= r);
			}
			return -1;
		}
	}
}
