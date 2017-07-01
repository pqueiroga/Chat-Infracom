package protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

public class DGServerSocket {
	
	private int headerLength = 14;
	private int ackNum;
	private DatagramSocket socket;
	private boolean closed;
	
	public DGServerSocket(int port) throws SocketException {
		this.socket = new DatagramSocket(port);
	}
	
	public static void main(String[] args) throws Exception {
		DGServerSocket teste = new DGServerSocket(2020);
		System.out.println("Criei teste");
		DGSocket teste2 = teste.accept(new int[1]);
		System.out.println("Recebi teste2");
		byte[] data = new byte[1024];
		System.out.println("Chamei receive na main do dgserversocket");
		teste2.receive(data, 2);
		System.out.println("Saí do primeiro receive");
//		System.out.println(receiver.getLength());
		System.out.println(new String(data, 0, 2, "UTF-8"));
		teste2.send(data, 2);
		ArrayList<String> nnteste = new ArrayList<String>();
		String teste3;
		data = new byte[1024];
		for (int i = 1000; i < 10000; i++) {
			teste2.receive(data, 6);
//			Thread.sleep(500);
			teste3 = new String(data, 0, 6, "UTF-8");
			nnteste.add(teste3);
		}
		int anterior = 999;
		int atual = 1000;
		for (String str : nnteste) {
			atual = Integer.parseInt(str.substring(str.indexOf('i') + 1, str.length()));
			if (anterior > atual || anterior + 1 != atual) {
				System.out.println("FORA DE ORDEM: anterior = " + anterior + "\natual = " + atual);
			}
			anterior = atual;
		}
		System.out.println(nnteste.toString());
		teste2.close();
		System.out.println("Enquanto fecha eu posso continuar fazendo as coisas");
	}
	
	public DGSocket accept() throws IOException {
		return accept(new int[1]);
	}
	
	public DGSocket accept(int[] pktsPerdidos) throws IOException {
		byte[] data = new byte[headerLength];
		DatagramPacket inicia = new DatagramPacket(data, headerLength);
		do {
			// PASSIVE OPEN
			socket.receive(inicia);
		} while (!getSyn(inicia.getData()));
		
		System.out.println("Recebi SYN");
		
		this.ackNum = getSeqNum(data) + 1;
		
		DGSocket retorno2 = new DGSocket(pktsPerdidos, inicia.getAddress().getHostName(), inicia.getPort(), "SYN RECEIVED", this.ackNum);
//		System.out.println("Vou travar esperando estado hehe");
//		while (!retorno2.getEstado().equals("ESTABLISHED"));
//		System.out.println("estado established, vou devolver a socket pra main!");

		return retorno2;
	}
	
	public void close() throws SocketException {
		this.socket.close();
		this.closed = true;
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

	public int getLocalPort() {
		return this.socket.getLocalPort();
	}
	
	public boolean isClosed() {
		return this.closed;
	}
}
