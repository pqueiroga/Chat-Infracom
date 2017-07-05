package protocol;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import utility.buffer.BufferMethods;

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
//		byte[] data = new byte[1024];
//		System.out.println("Chamei receive na main do dgserversocket");
//		teste2.receive(data, 2);
//		System.out.println("Saí do primeiro receive");
////		System.out.println(receiver.getLength());
//		System.out.println(new String(data, 0, 2, "UTF-8"));
//		teste2.send(data, 2);
//		ArrayList<String> nnteste = new ArrayList<String>();
//		String teste3;
//		data = new byte[1024];
//		for (int i = 1000; i < 10000; i++) {
//			teste2.receive(data, 6);
////			Thread.sleep(500);
//			teste3 = new String(data, 0, 6, "UTF-8");
//			nnteste.add(teste3);
//		}
//		int anterior = 999;
//		int atual = 1000;
//		for (String str : nnteste) {
//			atual = Integer.parseInt(str.substring(str.indexOf('i') + 1, str.length()));
//			if (anterior > atual || anterior + 1 != atual) {
//				System.out.println("FORA DE ORDEM: anterior = " + anterior + "\natual = " + atual);
//			}
//			anterior = atual;
//		}
//		System.out.println(nnteste.toString());
		
		
		String directory = "/home/pedro/InfraComProject/Download_Dump/"; //"Download_Dump" + File.separator;
		String fileName = BufferMethods.readString(teste2);
		fileName = fileName.replaceAll(" ", "");
		System.out.println(directory + fileName);
		File arquivoReceptor = new File(directory + fileName);
		if (arquivoReceptor.isFile()) {
			arquivoReceptor.delete();
		}
		arquivoReceptor.createNewFile();
		long fileSize = BufferMethods.receiveLong(teste2);
		System.out.println("fileSize: " + fileSize);
		long remainingSize = fileSize;
		byte[] buffer = new byte[1024];
		int bytesRead = 0;
		FileOutputStream outToFile = new FileOutputStream(arquivoReceptor);
		while (true) {
			try {
				bytesRead = teste2.receive(buffer, (int)Math.min(buffer.length, remainingSize));
			} catch (Exception e) {
				bytesRead = -1;
			}
			if (bytesRead == -1) break;
			remainingSize -= bytesRead;
			System.out.println("bytesRead: " + bytesRead+ "\nremainingSize: " + remainingSize);
			outToFile.write(buffer, 0, bytesRead);
			if (remainingSize == 0) break;
		}
		outToFile.close();
		String exe = "xdg-open " + new File(directory).getAbsolutePath() + File.separator + fileName;
		System.out.println(exe);
		try {
			Runtime.getRuntime().exec(exe);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		teste2.close(false);
		System.out.println("Enquanto fecha eu posso continuar fazendo as coisas");
	}
	
	public DGSocket accept() throws Exception {
		if (this.closed) {
			throw new SocketException("DGServerSocket já está fechada.");
		}
		return accept(new int[1]);
	}
	
	public DGSocket accept(int[] pktsPerdidos) throws Exception {
		if (this.closed) {
			throw new SocketException("DGServerSocket já está fechada.");
		}
		
		byte[] data = new byte[headerLength];
		DatagramPacket inicia = new DatagramPacket(data, headerLength);
		boolean aceitou = false;
		DGSocket retorno2 = null;
		do {
			do {
				// PASSIVE OPEN
				socket.receive(inicia);
			} while (!getSyn(inicia.getData()));
			
			System.out.println("Recebi SYN de " + inicia.getAddress().getHostName() + ", " + 
			inicia.getPort());
			
			this.ackNum = getSeqNum(data) + 1;
			
			retorno2 = new DGSocket(pktsPerdidos, inicia.getAddress().getHostName(),
					inicia.getPort(), "SYN RECEIVED", this.ackNum);
			System.out.println("Vou travar esperando estado hehe");
	
			while (true) {
				try {
					Thread.sleep(200);
					if (retorno2.isClosed()) {
//						throw new Exception("Erro tosco tentando aceitar conexão");
						System.out.println("Erro tosco tentando aceitar conexão");
						break;
					} 
					if (retorno2.getEstado().equals("ESTABLISHED")) {
						aceitou = true;
						break;
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("estado established, vou devolver a socket pra main!");
		} while (!aceitou); // tentativa de dar flush nos pacotes fantasma
		return retorno2;
	}
	
	public void close() throws SocketException {
		this.socket.close();
		this.closed = true;
	}
		
	private int getSeqNum(byte[] data) {
//		return (int) (data[0] << 24) + (int) (data[1] << 16) + (int) (data[2] << 8) + (int) data[3];
		return ((data[0] & 0xFF) << 24) + ((data[1] & 0xFF) << 16) + ((data[2] & 0xFF) << 8) + ((data[3] & 0xFF));
	}
	
//	private int getackNum(byte[] data) {
////		return (int) (data[4] << 24) + (int) (data[5] << 16) + (int) (data[6] << 8) + (int) data[7];
//		return ((data[4] & 0xFF) << 24) + ((data[5] & 0xFF) << 16) + ((data[6] & 0xFF) << 8) + ((data[7] & 0xFF));
//	}
	
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
