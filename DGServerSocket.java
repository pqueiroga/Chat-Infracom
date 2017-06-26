package rdt;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;

public class DGServerSocket {
	
	private String ESTADO = "CLOSED";
	
	private int remotePort;
	private InetAddress remoteInetAddress;
	private int ackNum;
	private int nextSeqNum;
	private int sendBase;
	private int sendWindowSize;
	private short rcvwnd;
	private byte[] sendBuffer = new byte[262144];
	private byte[] rcvBuffer = new byte[262144];
	private DatagramSocket socket;
	private boolean close = false, timerOn = false;
	
	public DGServerSocket(int port) throws SocketException {
		this.socket = new DatagramSocket(port);
	}
	
	public static void main(String[] args) throws Exception {
//		RDTSocket eu = new RDTSocket(2020);
//		(new Thread(eu)).start();
//		Scanner in = new Scanner(System.in);
//		String teste = in.nextLine();
//		byte[] data = eu.rdt_rcv();
//		eu.close();
//		System.out.println(new String(data, "UTF-8"));
//		System.out.println(teste);
//		in.close();
		DGServerSocket teste = new DGServerSocket(2020);
		Remetente teste2 = teste.accept();
		byte[] data = new byte[1024];
		DatagramPacket receiver = new DatagramPacket(data, data.length);
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
			teste3 = new String(data, 0, 4, "UTF-8");
			nnteste.add(teste3);
//			System.out.println(nnteste);
//			System.out.println(teste3 + " ");
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
		byte[] data = new byte[13];
		DatagramPacket inicia = new DatagramPacket(data, 13);
		do {
			// PASSIVE OPEN
			socket.receive(inicia);
			ESTADO = "LISTEN";
		} while (!getSyn(inicia.getData()));
		
		System.out.println("Recebi SYN");
		
		this.ackNum = getSeqNum(data) + 1;
		Remetente retorno2 = new Remetente(inicia.getAddress().getHostName(), inicia.getPort(), "SYN RECEIVED", this.ackNum);
//		cabecalha(data, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 1, (byte) 0);
//		DatagramSocket retorno = new DatagramSocket();
//		InetAddress remoteIA = inicia.getAddress();
//		int remotePort = inicia.getPort();
//		inicia = new DatagramPacket(data, 13, remoteIA, remotePort);
//		retorno.connect(inicia.getSocketAddress());
//		retorno.send(inicia); // syn-ack
//		ESTADO = "SYN RECEIVED";
////		do { não é necessário, pelo connect.
//			retorno.receive(inicia); // recebe ack
//			ESTADO = "ESTABLISHED";
////		} while (inicia.getPort() != remotePort && inicia.getAddress() != remoteIA); // descarta se não for da conexão
		
		return retorno2;
	}
	
	private void cabecalha(byte[] data, int nextSeqNum, int ackNum,
			short rcvwnd, byte ack, byte syn, byte fin) {
		// numero de sequencia
		data[0] = (byte) (nextSeqNum >>> 24);
	    data[1] = (byte) (nextSeqNum >>> 16);
	    data[2] = (byte) (nextSeqNum >>> 8);
	    data[3] = (byte) (nextSeqNum & 0x000000ffL);
	    
	    // ackNum
	    data[4] = (byte) (ackNum >>> 24);
	    data[5] = (byte) (ackNum >>> 16);
	    data[6] = (byte) (ackNum >>> 8);
	    data[7] = (byte) (ackNum & 0x000000ffL);
	    
	    // rcvwnd
	    data[8] = (byte) (rcvwnd >>> 8);
	    data[9] = (byte) (rcvwnd & 0x00ffL);
	    
	    // ack, syn, fin
	    data[10] = ack;
	    data[11] = syn;
	    data[12] = fin;
	}
	
	private int getackNum(byte[] data) {
		return (int) (data[4] << 24) + (int) (data[5] << 16) + (int) (data[6] << 8) + (int) data[7];
	}
	
	private int getSeqNum(byte[] data) {
		return (int) (data[0] << 24) + (int) (data[1] << 16) + (int) (data[2] << 8) + (int) data[3];
	}
	
	private boolean getAck(byte[] data) {
		try {
			return data[10] == 1 ? true : false;
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean getSyn(byte[] data) {
		try { 
			return data[11] == 1 ? true : false;
		} catch (Exception e) {
			return false;
		}
	}
	
	private boolean getFin(byte[] data) {
		try {
			return data[12] == 1 ? true : false;
		} catch (Exception e) {
			return false;
		}
	}
	
	private byte[] getdata(DatagramPacket pkt) {
		byte[] retorno = new byte[pkt.getLength() - 13];
		for (int i = 13, j = 0; i < pkt.getLength(); i++, j++) {
			retorno[j] = pkt.getData()[i];
		}
		return retorno;
	}
}
