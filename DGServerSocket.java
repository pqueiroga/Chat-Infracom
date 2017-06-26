package rdt;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class DGServerSocket {
	
	private int headerLength = 14;
	private int ackNum;
	private DatagramSocket socket;
	
	public DGServerSocket(int port) throws SocketException {
		this.socket = new DatagramSocket(port);
	}
	
	public static void main(String[] args) throws Exception {
		DGServerSocket teste = new DGServerSocket(2020);
		Remetente teste2 = teste.accept();
		byte[] data = new byte[1024];
		System.out.println("Chamei receive na main do dgserversocket");
		teste2.receive(data, 2);
//		System.out.println(receiver.getLength());
		System.out.println(new String(data, 0, 2, "UTF-8"));
		teste2.send(data, 2);
		ArrayList<String> nnteste = new ArrayList<String>();
		String teste3;
		for (int i = 10; i < 100; i++) {
			data = new byte[1024];
			teste2.receive(data, 4);
			Thread.sleep(500);
			teste3 = new String(data, 0, 4, "UTF-8");
			nnteste.add(teste3);
		}
		int anterior = 9;
		int atual = 10;
		for (String str : nnteste) {
			atual = Integer.parseInt(str.substring(str.indexOf('i') + 1, str.length()));
			if (anterior > atual || anterior + 1 != atual) {
				System.out.println("FORA DE ORDEM");
				break;
			}
			anterior = atual;
		}
		System.out.println(nnteste.toString());
	}
	
	public Remetente accept() throws IOException {
		byte[] data = new byte[headerLength];
		DatagramPacket inicia = new DatagramPacket(data, headerLength);
		do {
			// PASSIVE OPEN
			socket.receive(inicia);
		} while (!getSyn(inicia.getData()));
		
		System.out.println("Recebi SYN");
		
		this.ackNum = getSeqNum(data) + 1;
		Remetente retorno2 = new Remetente(inicia.getAddress().getHostName(), inicia.getPort(), "SYN RECEIVED", this.ackNum);
		while (!retorno2.getEstado().equals("ESTABLISHED"));

		return retorno2;
	}
		
	private int getSeqNum(byte[] data) {
		return (int) (data[0] << 24) + (int) (data[1] << 16) + (int) (data[2] << 8) + (int) data[3];
	}
	
	private boolean getSyn(byte[] data) {
		try { 
			return data[11] == 1 ? true : false;
		} catch (Exception e) {
			return false;
		}
	}
}
