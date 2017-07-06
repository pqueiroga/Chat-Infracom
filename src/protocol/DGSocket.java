package protocol;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import utility.buffer.BufferMethods;

public class DGSocket {
	
	private double pDescartaPacote = 0.0;
	
	private boolean portUnreachable = false;
	private boolean connectionRefused = false;
	private int timeoutTries = 0;
	
	Random random = new Random();
	
	private String ESTADO;
	
	private short RcvBuffer = 50;
	private int headerLength = 14;
	
	private boolean recuperacaoRapida = false;
	private short congwin = 1;
	private short ssthresh = 20;
	private byte acksDuplicados = 0;
	private short varPrevencao = 0;
	
	private int remotePort;
	private InetAddress remoteInetAddress;
	private int ackNum;
	private int nextSeqNum;
	private int lastPacketSent = -1;
	private int sendBase; // sendBase - 1 nos diz LastPacketAcked
	private int sendWindowSize;
	private int rcvwnd;
	private DatagramPacket[] testeSendBuffer = new DatagramPacket[RcvBuffer];
	private byte[] testeSendBufferEstado = new byte[RcvBuffer]; // 1 n enviado, 2 enviado, 3 reconhecido
//	private int[] acksDuplicados = new int[RcvBuffer];
	private DatagramPacket[] testeRcvBuffer = new DatagramPacket[RcvBuffer];
	private boolean[] testeRcvBufferEstado = new boolean[RcvBuffer]; // true posição ocupada, false posição livre
	
	/**
	 * rcvBase nos diz onde está/estará o pacote válido para ser pego.
	 * rcvBase - 1 nos diz o último pacote lido pela aplicação
	 */
	private int rcvBase;
	private int lastPacketRcvd;
	private int rcvLastAcked;
	private DatagramSocket socket;
	private int pktTimer, ackTimer; // diz qual pacote/ack está associado ao timer
	private boolean closed = false, msgTimerOn = false, ackTimerOn = false, ackMeTimerOn = false;
	private long timeOutInterval = 1000;
	private long timeOutRTT = 1000;
	private long timeSent, timeAcked, sampleRTT, devRTT;
	private long[] estimatedRTT = {-1};
	private int packetSample = -1;
	
	private Timer msgSentTimer = new Timer(true); // quero que sejam daemons e não impeçam ngm
	private Timer delayedAckTimer = new Timer(true);
	private Timer ackMeTimer = new Timer(true);
	
	private AckMePls testeAckMeTimerTask = new AckMePls();
	private DelayedAckTimeOut testeDelayedAckTimerTask = new DelayedAckTimeOut();
	private MsgSentTimeOut testeMsgSentTimerTask = new MsgSentTimeOut();
	
	private Thread tEnvia, tRecebe;
	
	private int[] pktsPerdidos;
	
	public DGSocket(long[] estimatedrtt, double pDescartaPacotes, int[] pktsPerdidos, String remoteIP, int remotePort, String ESTADO, int ackNum) throws IOException {
//		this(pktsPerdidos, -1, remoteIP, remotePort, ESTADO, ackNum);
		this(estimatedrtt, pDescartaPacotes, pktsPerdidos, -1, remoteIP, remotePort, ESTADO, ackNum);
	}
	
//	public DGSocket(String remoteIP, int remotePort) throws IOException {
//		this(new int[1], -1, remoteIP, remotePort, "CLOSED", 0);
//	}
	
	public DGSocket(long[] estimatedrtt, double pDescartaPacotes, int[] pktsPerdidos, String remoteIP, int remotePort) throws IOException {
		this(estimatedrtt, pDescartaPacotes, pktsPerdidos, -1, remoteIP, remotePort, "CLOSED", 0);
	}	
	
	public DGSocket(double pDescartaPacotes, int[] pktsPerdidos, String remoteIP, int remotePort) throws IOException {
		this(null, pDescartaPacotes, pktsPerdidos, -1, remoteIP, remotePort, "CLOSED", 0);
	}
	
	public DGSocket(double pDescartaPacotes, int[] pktsPerdidos, int port, String remoteIP, int remotePort) throws IOException {
		this(null, pDescartaPacotes, pktsPerdidos, port, remoteIP, remotePort, "CLOSED", 0);
	}
	public DGSocket(int[] pktsPerdidos, String remoteIP, int remotePort) throws IOException {
		this(pktsPerdidos, -1, remoteIP, remotePort, "CLOSED", 0);
	}
	public DGSocket(int[] pktsPerdidos, int port, String remoteIP, int remotePort) throws IOException {
		this(pktsPerdidos, port, remoteIP, remotePort, "CLOSED", 0);
	}
	
	public DGSocket(int[] pktsPerdidos, int port, String remoteIP, int remotePort, String ESTADO, int ackNum) throws IOException {
		this(null, 0, pktsPerdidos, port, remoteIP, remotePort, ESTADO, ackNum);
	}
	
	public DGSocket(long[] estimatedrtt, double pDescartaPacotes, int[] pktsPerdidos, int port, String remoteIP, int remotePort, String ESTADO, int ackNum) throws IOException {
		if (estimatedrtt == null) {
			this.estimatedRTT[0] = -1;
		} else {
			this.estimatedRTT = estimatedrtt;
		}
		this.estimatedRTT[0] = -1; // pra caso alguém tenha usado errado, ele sempre tem que começar com
		// -1 anyways.
		System.out.println("pDescartaPacotes quando chega em DGSocket: " + pDescartaPacotes);
		this.pDescartaPacote = pDescartaPacotes;
		this.pktsPerdidos = pktsPerdidos;
		this.ESTADO = ESTADO;
		byte[] data = new byte[1024];
		this.nextSeqNum = this.sendBase = this.rcvBase = this.rcvLastAcked = 0; //new Random().nextInt(Integer.MAX_VALUE - 1);
		this.rcvwnd = this.sendWindowSize = RcvBuffer;
		switch (this.ESTADO) {
		case "CLOSED":
			if (port == -1) {
				this.socket = new DatagramSocket();
			} else {
				this.socket = new DatagramSocket(null);
				this.socket.setReuseAddress(true);
				SocketAddress sockaddr = new InetSocketAddress(port);
				this.socket.bind(sockaddr);
			}
			this.remotePort = remotePort;
			this.remoteInetAddress = InetAddress.getByName(remoteIP);
			this.socket.connect(remoteInetAddress, remotePort);

			tRecebe = new Thread(new RecebeDados());
			tEnvia = new Thread(new EnviaDados());
			tEnvia.setDaemon(true);
			tRecebe.setDaemon(true);
			tEnvia.start();
			tRecebe.start();
			// ACTIVE OPEN
			send(null, 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0); 
			System.out.println("Eu: " +this.socket.getLocalAddress().getHostName() +", " + this.socket.getLocalPort() + " Depois do send do SYN");
			// SYN SENT
			this.ESTADO = "SYN SENT";
			
			// recebe syn + ack
			System.out.println(this.socket.getLocalAddress().getHostName() +", " + this.socket.getLocalPort() + " Vou receber syn + ack");
			receive(data, 1024);
			System.out.println("Eu: " +this.socket.getLocalAddress().getHostName() +", " + this.socket.getLocalPort() + " Logo depois de receber syn + ack no construtor de " + this.remoteInetAddress.getHostName() + ", " + this.remotePort);
			break;
		case "SYN RECEIVED":
			this.socket = new DatagramSocket();
			this.socket.connect(InetAddress.getByName(remoteIP), remotePort);
			System.out.println("conectei");
			this.ackNum = ackNum;
			this.rcvBase++;
			this.remoteInetAddress = InetAddress.getByName(remoteIP);
			this.remotePort = remotePort;
			tRecebe = new Thread(new RecebeDados());
			tEnvia = new Thread(new EnviaDados());
			tEnvia.setDaemon(true);
			tRecebe.setDaemon(true);
			tEnvia.start();
			tRecebe.start();
			System.out.println("comecei as threads");
			send(null, 0, (byte) 1, (byte) 1, (byte) 0, (byte) 0); // manda syn + ack
			System.out.println(this.socket.getLocalAddress().getHostName() +", " + this.socket.getLocalPort() + " " +"depois do send de syn ack pra " + this.remoteInetAddress.getHostName() +", " + this.remotePort);
			
		}
	}
	
	public void send(byte[] data, int length, byte ack, byte syn, byte fin, byte ackMe) throws IOException {
		if (!ESTADO.equals("ESTABLISHED") && connectionRefused) {
			throw new ConnectException("Connection refused (Connection refused)");
		}
		if (closed) {
			throw new SocketException("DGSocket já está fechada.");
		}
		if (portUnreachable) {
			throw new PortUnreachableException("Provavelmente o end host caiu");
		}
		System.out.println(this.socket.getLocalAddress().getHostName() +", " + this.socket.getLocalPort() + " " +"Tentarei ganhar lock de testeSendBuffer no send");
		synchronized (this.testeSendBuffer) {
			if (Math.abs(this.sendBase - this.nextSeqNum) >= RcvBuffer) {
				System.out.println(this.sendBase + ", " + this.nextSeqNum);
			}
			while (Math.abs(this.sendBase - this.nextSeqNum) >= RcvBuffer) {
				try {
					System.out.println(this.sendBase + ", " + this.nextSeqNum);
					System.out.println(this.socket.getLocalAddress().getHostName() +", " + this.socket.getLocalPort() + " " +"APLICAÇÃO ESPERANDO POIS NÃO PODE COLOCAR MAIS NADA NO SEND BUFFER!");
					this.testeSendBuffer.notifyAll(); // avisa pra caso a envia dados esteja parada
					this.testeSendBuffer.wait();
					if (!ESTADO.equals("ESTABLISHED") && connectionRefused) {
						throw new ConnectException("Connection refused (Connection refused)");
					}
					if (closed) {
						throw new SocketException("DGSocket já está fechada.");
					}
					if (portUnreachable) {
						throw new PortUnreachableException("Provavelmente o end host caiu");
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//			System.out.println("Ganhei lock de testeSendBuffer no send");
			byte[] guardar = new byte[headerLength + length];
			rcvwnd = (RcvBuffer - ((lastPacketRcvd + 1) - (rcvBase - 1)));
			System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + (lastPacketRcvd + 1)
					+ " - (" + rcvBase + " - 1)))");
			cabecalha(guardar, nextSeqNum, ackNum, rcvwnd, ack, syn, fin, ackMe);
			for (int i = headerLength, j = 0; i < headerLength + length && j < data.length; ++i, ++j) {
				guardar[i] = data[j];
			}
			testeSendBuffer[circulariza(this.nextSeqNum)] = new DatagramPacket(guardar, guardar.length,
					this.remoteInetAddress, this.remotePort);
			this.testeSendBufferEstado[circulariza(this.nextSeqNum)] = 1;

			this.nextSeqNum++;
			System.out.println(this.socket.getLocalAddress().getHostName() +", " + this.socket.getLocalPort() + " " +"Coloquei o pacote " + (this.nextSeqNum - 1) + " no sendBuffer pra ser enviado,"
					+ " na posição " + circulariza(this.nextSeqNum - 1) +
					"\nEle tem " + (testeSendBuffer[circulariza(this.nextSeqNum -1)].getLength() -14) +
					" bytes de carga útil");
			testeSendBuffer.notify();
//			System.out.println("Notifiquei o envia dados do testeSendBuffer OK");
		}
	}
	
	public void send(byte[] data, int length) throws IOException {
		send(data, length, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
	}
	
	public int receive(byte[] data, int length) throws SocketException {
		if (!ESTADO.equals("ESTABLISHED") && connectionRefused) {
			throw new ConnectException("Connection refused (Connection refused)");
		}
		if (closed) {
			throw new SocketException("DGSocket já está fechada.");
		}
		if (portUnreachable) {
			throw new PortUnreachableException("Provavelmente o end host caiu");
		}
		int b = 0;
		
		synchronized (testeRcvBuffer) {
			System.out.println("Ganhei lock no testeRcvBuffer em receive");
			while (rcvBase == ackNum) {
				// pois não tem nenhum pacote ainda
				try {
					System.out.println("Estarei esperando por rcvBase != ackNum em receive");
					testeRcvBuffer.wait();
					System.out.println("portUnreachable: " + portUnreachable + 
							"\nclosed: " + closed + "\nconnectionRefused: " + connectionRefused);
					if (!ESTADO.equals("ESTABLISHED") && connectionRefused) {
						throw new ConnectException("Connection refused (Connection refused)");
					}
					if (portUnreachable) {
						throw new PortUnreachableException("Provavelmente o end host caiu");
					} 
					if (closed) {
						throw new SocketException("DGSocket já está fechada.");
					}
					System.out.println("Acabou a espera do testeRcvBuffer em receive");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println("Vou tentar dar o pacote " + rcvBase + " para a aplicação");
			System.out.println("Ele tem " + (testeRcvBuffer[circulariza(rcvBase)].getLength() - 14) + 
					" bytes de carga útil.");
			byte[] temp = testeRcvBuffer[circulariza(rcvBase)].getData();
			for (int i = 0, j = headerLength; i < length && j < testeRcvBuffer[circulariza(rcvBase)].getLength(); ++i, ++j) {
				data[i] = temp[j];
				++b;
			}
			System.out.println("Mandei pra aplicação o pacote " + rcvBase);
				testeRcvBufferEstado[circulariza(rcvBase)] = false;
			rcvBase += 1;
			testeRcvBuffer.notifyAll(); 
			// avisa pois pode ter uma thread esperando pra poder receber mais coisa,
			// esse método libera espaço no testercvbuffer
		}
		return b;
	}
	
	public static void main(String[] args) throws IOException {
		DGSocket teste = new DGSocket(new int[1], "localhost", 2020);
//		byte[] teste1 = "oi".getBytes("UTF-8");
//		System.out.println("Vou usar o send na main");
//		teste.send(teste1, teste1.length);
//		System.out.println("Usei o send na main");
//		byte[] teste2 = new byte[6];
//		teste.receive(teste2, 2);
//
//		for (int i = 1000; i < 10000; i++) {
//			teste1 = ("oi" + i).getBytes("UTF-8");
//			teste.send(teste1, 6);
//		}
//		System.out.println("Fim");
		
		
		String directory = "Upload_Pool" + File.separator;
		String fileName = "Groovin' Magic.flac";
		File uploadFile = new File("/home/pedro/InfraComProject/Upload_Pool/" + fileName);
		if (uploadFile.isFile()) {
			// diz nome do arquivo que estarei enviando
			BufferMethods.writeString(fileName, teste);
			System.out.println(directory + fileName);
			// diz quantos bytes estarei enviando
			System.out.println("fileSize: " + uploadFile.length());

			BufferMethods.sendLong(uploadFile.length(), teste);
			
			long remainingSize = uploadFile.length();
			byte[] buffer = new byte[1024];
			int bytesRead;
			FileInputStream fInputStream = new FileInputStream(uploadFile);
			
			while (remainingSize > 0  && (bytesRead = fInputStream.read(buffer, 0,
					(int)Math.min(buffer.length, remainingSize))) != -1) {
				remainingSize -= bytesRead;
				System.out.println("bytesRead: " + bytesRead + "\nremainingSize: " + remainingSize);
				teste.send(buffer, bytesRead);
			}
			fInputStream.close();
		}
		
		teste.close(false);
		System.out.println("Enquanto fecha eu posso continuar fazendo coisas");
	}
	
	public String getEstado() {
		return this.ESTADO;
	}
	
	public InetAddress getInetAddress() {
		return this.remoteInetAddress;
	}
	
	public int getPort() {
		return this.remotePort;
	}
	
	public boolean isClosed() {
		return this.closed;
	}
	
	private int circulariza(int index) {
		return index % RcvBuffer;
	}
	
	private boolean podeEnviar() {
//		int temp = Math.max(lastPacketSent, 0);
//		if ((temp - sendBase + 1) > Math.min(congwin, sendWindowSize)) {
//			System.out.println(temp + ", " + sendBase +", " +congwin + ", " + sendWindowSize);
//			return false;
//		}
		if (congwin < 1) {
			congwin = 1;
		}
		for (int i = sendBase; i < sendBase + Math.min(congwin, sendWindowSize) && i < nextSeqNum; ++i) {//i < nextSeqNum && i < testeSendBufferEstado.length; i++) {
//			System.out.println("testeSendBufferEstado[circulariza(i) (" + circulariza(i) + ")]: " +testeSendBufferEstado[circulariza(i)]);
//		for (int i = temp; i < temp + Math.min(sendWindowSize, congwin) && i < nextSeqNum; ++i) {
			if (testeSendBufferEstado[circulariza(i)] == 1) {
				return true;
			}
		}
		return false;
	}
	
	private int transmitidoNaoReconhecido(int end) {
		for (int i = sendBase; i <= lastPacketSent && i < nextSeqNum && i < end && i < sendBase + RcvBuffer; ++i) {
			if (testeSendBufferEstado[circulariza(i)] == 2) {
				return i;
			}
		}
		return -1;
	}
	
//	private int recebidoNaoReconhecido(int end) {
//		for (int i = rcvLastAcked; i < end && i < rcvLastAcked + RcvBufferSize; ++i) {
//			if (testeRcvBufferEstado[circulariza(i)]) {
//				return i;
//			}
//		}
//		return -1;
//	}
	
	private int descobreProximoEsperado() {
		System.out.println("descobreProximoEsperado() -> lastPacketRcvd: " + lastPacketRcvd);
		for (int i = ackNum; i <= lastPacketRcvd; ++i) {
//			System.out.println("testeRcvBufferEstado[circulariza(i) (" + circulariza(i) + ")]: " +testeRcvBufferEstado[circulariza(i)]);
			if (!testeRcvBufferEstado[circulariza(i)]) {
				System.out.println("proximo esperado: " + i);
				return i;
			}
		}
		System.out.println("proximo esperado: " + (lastPacketRcvd + 1));
		return lastPacketRcvd + 1;
	}
	
	public void close(boolean forcar) throws IOException {
		if (!closed) {
			(new Thread(new ClosesStuff(forcar))).start();
		} else {
			throw new IOException("DGSocket já está fechada.");
		}
	}
	
	class ClosesStuff implements Runnable {
		
		private boolean forcar;
		public ClosesStuff (boolean forcar) {
			this.forcar = forcar;
		}
		public void run() {
			int i = 0;
			if (!forcar) {
				while (sendBase != nextSeqNum || rcvBase != ackNum) {
					if (i >= 480 || portUnreachable || connectionRefused || !ESTADO.equals("ESTABLISHED"))
						break; // esperar até 4 minutos hehehe.
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
						break;
					}
					++i;
				}
			}
			synchronized (testeRcvBuffer) {
				testeRcvBuffer.notifyAll();
			}
			synchronized (testeSendBuffer) {
				testeSendBuffer.notifyAll();
			}
			// TODO FIN
			byte[] ack = new byte[headerLength];
			rcvwnd = (RcvBuffer - ((lastPacketRcvd + 1) - (rcvBase - 1)));

			cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
			DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
			if (ackTimer > rcvLastAcked) {
				rcvLastAcked = ackTimer;
			}
			try {
				socket.send(ACK);
				socket.send(ACK);
				socket.send(ACK);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ackMeTimer.cancel();
			delayedAckTimer.cancel();
			msgSentTimer.cancel();
			closed = true;
			try {
				tEnvia.interrupt();
			} catch (Exception e) {}
			try {
				tRecebe.interrupt();
			} catch (Exception e) {}
//			tEnvia.join();
//			tRecebe.join();
			socket.close();
		}
	}
	
	class EnviaDados implements Runnable {
		public void run() {
			while (!closed) {
				synchronized (testeSendBuffer) {
					while (!podeEnviar()) {
						try {
							System.out.println("Estou esperando no testeSendBuffer pra enviar dados");
							System.out.println(lastPacketSent + ", " + sendBase +", " +congwin + ", " + sendWindowSize);
							if (sendWindowSize == 0) {
								synchronized (ackMeTimer) {
									
									testeAckMeTimerTask.cancel();
									testeAckMeTimerTask = new AckMePls();

									try {
										ackMeTimer.schedule(testeAckMeTimerTask, timeOutRTT);
									} catch (IllegalStateException e) {
										testeAckMeTimerTask.cancel();
										testeAckMeTimerTask = new AckMePls();
										ackMeTimer.schedule(testeAckMeTimerTask, timeOutRTT);
									}
									ackMeTimerOn = true;
								}
							}
							testeSendBuffer.wait();
							System.out.println("Saí da espera no testeSendBuffer, sendWindowSize: " + sendWindowSize);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							System.out.println("Caught it");
							e.printStackTrace();
							try {
								close(false);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
								return;
							}
							return;
						}
					}
					// se chegou aqui quer dizer que tem coisa no sendBuffer para enviar
					System.out.println("Saí da espera no testeSendBuffer de verdade com janela receptora do dest (" +
					 sendWindowSize + ") e enviarei dados");
					try {
//						for (int i = sendBase; i < sendBase + sendWindowSize && i < nextSeqNum &&
//								(lastPacketSent - (sendBase - 1)) <= Math.min(sendWindowSize, congwin); i++) {//i < nextSeqNum; i++) {
						System.out.println((lastPacketSent + 1) - (sendBase - 1) + ", " + Math.min(sendWindowSize, congwin));
						while (((lastPacketSent + 1) - (sendBase - 1)) <= Math.min(sendWindowSize, congwin)) {
							System.out.println((lastPacketSent + 1) - (sendBase - 1) + ", " + Math.min(sendWindowSize, congwin));
//							assert i < sendBase + sendWindowSize : sendBase + " + " + sendWindowSize + " >= " + i;
							if (testeSendBufferEstado[circulariza(lastPacketSent + 1)] == 1) {
//								assert i < sendBase + sendWindowSize : sendBase + " + " + sendWindowSize + " >= " + i + "    2";
//								System.out.println("Janela receptora do destinatário (" + sendWindowSize +")");
//								System.out.println("Pacotes no ar: " + (i - 1 - (sendBase - 1)));
								
								synchronized (ackMeTimer) {
									testeAckMeTimerTask.cancel();
									testeAckMeTimerTask = new AckMePls();
									ackMeTimerOn = false;
								}
								System.out.println(socket.getLocalAddress().getHostName() +", " + socket.getLocalPort() + " " +"Enviei o pacote " + (lastPacketSent + 1) + "\nEle tem " + (testeSendBuffer[circulariza(lastPacketSent + 1)].getLength() -14) +
										" bytes de carga útil");
								socket.send(testeSendBuffer[circulariza(lastPacketSent + 1)]);
								if (packetSample == -1) {
									timeSent = System.currentTimeMillis();
//									sampleRTT = -1;
									packetSample = lastPacketSent;
								}
								++lastPacketSent;
								
								synchronized (msgSentTimer) {
									if (!msgTimerOn) {
										pktTimer = sendBase;
										timeoutTries = 0;
										timeOutInterval = timeOutRTT;
										try {
											msgSentTimer.schedule(testeMsgSentTimerTask, timeOutInterval);
										} catch (IllegalStateException e) {
											testeMsgSentTimerTask.cancel();
											testeMsgSentTimerTask = new MsgSentTimeOut();
											msgSentTimer.schedule(testeMsgSentTimerTask, timeOutInterval);
										}
										msgTimerOn = true;
									}
								}

								testeSendBufferEstado[circulariza(lastPacketSent)] = 2;
								synchronized (delayedAckTimer) {
									if (ackTimerOn) {
										if (ackTimer <= ackNum) {
											testeDelayedAckTimerTask.cancel();
											testeDelayedAckTimerTask = new DelayedAckTimeOut();
											ackTimerOn = false;
										}
									}
								}
							} else {
								break;
							}
						}
					} catch (PortUnreachableException e1) {
						e1.printStackTrace();
						portUnreachable = true;
						try {
							close(false);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			System.out.println("Terminei EnviaDados");
		}
	}
	
	class AckMePls extends TimerTask {
		public void run() {
			try {
				synchronized (ackMeTimer) {
					try {
						System.out.println("Enviando ackMe pois sendWindowSize == " + sendWindowSize);
//						try {
//							testeAckMeTimerTask.cancel();
//						} catch (IllegalStateException e) {
//							testeAckMeTimerTask = new AckMePls();
//						}
						testeAckMeTimerTask.cancel();
						testeAckMeTimerTask = new AckMePls();
						byte[] data = new byte[headerLength];
						rcvwnd = (RcvBuffer - ((lastPacketRcvd + 1) - (rcvBase - 1)));
	//					System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
	//							+ " - (" + rcvBase + " - 1)))");
						
						cabecalha(data, nextSeqNum, ackNum, rcvwnd, (byte) 0, (byte) 0, (byte) 0, (byte) 1);
						System.out.println("SeqNum: " + nextSeqNum +
								"\nackNum: " + ackNum +
								"\nrcvwnd: " + rcvwnd +
								"\nack: " + getAck(data) +
								"\nackme: " + getAckMe(data));
						DatagramPacket dp = new DatagramPacket(data, headerLength, remoteInetAddress, remotePort);
						socket.send(dp);
						if (sendWindowSize == 0) {
							try {
								ackMeTimer.schedule(testeAckMeTimerTask, timeOutRTT);
							} catch (IllegalStateException e) {
								testeAckMeTimerTask.cancel();
								testeAckMeTimerTask = new AckMePls();
								ackMeTimer.schedule(testeAckMeTimerTask, timeOutRTT);
							}
						} else {
							ackMeTimerOn = false;
						}
					} catch (IOException e) {
						// TODO
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace(); 
			}
		}
	}
	
	class MsgSentTimeOut extends TimerTask {
		public void run() {
			try {
				synchronized (pktsPerdidos) {
					++pktsPerdidos[0];
					pktsPerdidos.notify();
				}
				ssthresh = (short) Math.max((congwin >> 1), 1);
				congwin = 1;
				acksDuplicados = 0;
				recuperacaoRapida = false;
				if ((!ESTADO.equals("ESTABLISHED") && timeoutTries > 4) || timeoutTries > 10) {
					System.out.println(socket.getLocalAddress().getHostName() +", " + socket.getLocalPort() + " " +"end host não responde.");
					connectionRefused = true;
					close(true);
					return;
				}
				synchronized (msgSentTimer) {
					System.out.println(socket.getLocalAddress().getHostName() +", " + socket.getLocalPort() + " " +"Pacote " + pktTimer + " deu timeout depois"
							+ " de " + timeOutInterval + " ms");
					try {
						testeMsgSentTimerTask.cancel();
						testeMsgSentTimerTask = new MsgSentTimeOut();
						
						byte[] newData = testeSendBuffer[circulariza(pktTimer)].getData();
						rcvwnd = (RcvBuffer - ((lastPacketRcvd + 1) - (rcvBase - 1)));
	//					System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
	//							+ " - (" + rcvBase + " - 1)))");
						setRcvwnd(newData, rcvwnd);
						testeSendBuffer[circulariza(pktTimer)] = new DatagramPacket(newData, testeSendBuffer[circulariza(pktTimer)].getLength(),
								testeSendBuffer[circulariza(pktTimer)].getAddress(), testeSendBuffer[circulariza(pktTimer)].getPort());
						if (pktTimer == packetSample) {
							timeSent = System.currentTimeMillis();
						}
						socket.send(testeSendBuffer[circulariza(pktTimer)]);
						if (timeOutInterval < (timeOutRTT << 3) && timeOutInterval < 6000) { // duplicação do tempo de expiração
							timeOutInterval = timeOutInterval << 1;
						}
						try {
							++timeoutTries;
							msgSentTimer.schedule(testeMsgSentTimerTask, timeOutInterval);
						} catch (IllegalStateException e) {
							testeMsgSentTimerTask.cancel();
							testeMsgSentTimerTask = new MsgSentTimeOut();
							msgSentTimer.schedule(testeMsgSentTimerTask, timeOutInterval);
						}
					} catch (PortUnreachableException e) {
						e.printStackTrace();
						portUnreachable = true;
						try {
							close(false);
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						return;
					} catch (IOException e) {
						// TODO rip
						e.printStackTrace();
					}
				}
			} catch (Exception e) {}
		}
	}
	
	class DelayedAckTimeOut extends TimerTask {
		public void run() {
			try {
				synchronized (delayedAckTimer) {
					System.out.println("Delayed Ack " + ackTimer + " deve ser enviado agora!");
					try {
//						try {
//							delayedAckTimer.cancel();
//							delayedAckTimer = new Timer();
//						} catch (IllegalStateException e) {
//							delayedAckTimer = new Timer();
//						}
						
						testeDelayedAckTimerTask.cancel();
						testeDelayedAckTimerTask = new DelayedAckTimeOut();
						
						ackTimerOn = false;
	
						byte[] ack = new byte[headerLength];
						rcvwnd = (RcvBuffer - ((lastPacketRcvd + 1) - (rcvBase - 1)));
	//					System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
	//							+ " - (" + rcvBase + " - 1)))");
						cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
						DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
						// esperou 500 ms pela chegada de outro pacote na ordem, que deveria cancelar.
						// se não foi cancelado ou seja, entrou aqui, então envia o ACK mesmo.
						// // testeRcvBufferEstado[circulariza(ackTimer)] = 2;
						if (ackTimer > rcvLastAcked) {
							rcvLastAcked = ackTimer;
						}
						socket.send(ACK);
					} catch (IOException e) {
						// TODO rip
						e.printStackTrace();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	class RecebeDados implements Runnable {
		
		public void run() {
			while (!closed) {
				try {
					byte[] data = new byte[4110]; // carga útil máxima de 4096
					DatagramPacket dp = new DatagramPacket(data, data.length);
					System.out.println("Vou esperar por um datagrama no RecebeDados");
					socket.receive(dp);
					System.out.println("Recebi um datagrama no RecebeDados");
					System.out.println("SeqNum: " + getSeqNum(data) +
							"\nackNum: " + getackNum(data) +
							"\nrcvwnd: " + getRcvwnd(data) +
							"\nEle tem " + (dp.getLength() -14) +
							" bytes de carga útil" +
							"\ndado: " + new String(data, 14, dp.getLength() - 14, "UTF-8"));
					
					System.out.println("pDescartaPacote: " + pDescartaPacote);
					if (random.nextDouble() < pDescartaPacote) { // simula perda de pacotes
						System.out.println("Perdi o datagram ehehe");
						continue;
					}
					
					if (getAckMe(data) && !getAck(data)) { 
						// aqui é para o caso do host remetente estar vendo
						// um sendWindowSize 0 e ficar mandando pacotes pra mim só para receber
						// o ack de volta e ser atualizado sobre o sendWindowSize.
						System.out.println("Recebi um ackme");
						byte[] ack = new byte[headerLength];
						rcvwnd = (RcvBuffer - ((lastPacketRcvd + 1) - (rcvBase - 1)));
						System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + (lastPacketRcvd + 1)
								+ " - (" + rcvBase + " - 1)))");
						// envia de volta o ack-ackMe
						cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 1);
						System.out.println("enviei de volta ack-ackme");
						System.out.println("SeqNum: " + nextSeqNum +
								"\nackNum: " + ackNum +
								"\nrcvwnd: " + rcvwnd +
								"\nack: " + getAck(ack) +
								"\nackme: " + getAckMe(ack));
						DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
						// envio imediato de um ACK
						socket.send(ACK);
						continue;
					}
					
					
//					if (getSeqNum(data) > lastPacketRcvd + rcvwnd) {
//						System.out.println("Não cabe na janela de recepção, descartei essa merda");
//						byte[] ack = new byte[headerLength];
//						rcvwnd = (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
//						System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
//								+ " - (" + rcvBase + " - 1))) = " + rcvwnd);
//						cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
//						DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
//						// envio imediato de um ACK
//						socket.send(ACK);
//						continue;
//					}
					
//					System.out.println("Rcvwnd que tem no pacote " + getRcvwnd(data));
					sendWindowSize = getRcvwnd(data); // testando se isso melhora o paralelismo
					
					System.out.println("Recebi o pacote " + getSeqNum(dp.getData()) + " em RecebeDados\nESTADO == " + ESTADO);
					System.out.println("rcvwnd que tem no pacote: " + getRcvwnd(data));
					boolean apenasAck = false;
					if (getAck(data) && dp.getLength() == headerLength && !getSyn(data) && !getFin(data)) {
						apenasAck = true;
					}
					switch (ESTADO) {
					case "SYN RECEIVED":
						if (getAck(data)) {
							System.out.println("Eu: " + socket.getLocalAddress().getHostName() +
									", " + socket.getLocalPort() + " Recebi ack de " + dp.getAddress().getHostName() + ", " +
									dp.getPort());

							synchronized (msgSentTimer) {
								for (int i = sendBase; i < getackNum(data); ++i) {
									testeSendBufferEstado[circulariza(i)] = 3;
								}
								testeMsgSentTimerTask.cancel();
								testeMsgSentTimerTask = new MsgSentTimeOut();
								
								msgTimerOn = false;

								sendBase = getackNum(data);
								sendWindowSize = getRcvwnd(data);
								int peppa = transmitidoNaoReconhecido(nextSeqNum);
								if (peppa != -1) { // se tiver algum segmento não reconhecido, começar temporizador pra ele
									pktTimer = peppa;
									try {
										timeoutTries = 0;
										msgSentTimer.schedule(testeMsgSentTimerTask, timeOutInterval);
									} catch (IllegalStateException e) {
										testeMsgSentTimerTask.cancel();
										testeMsgSentTimerTask = new MsgSentTimeOut();
										msgSentTimer.schedule(testeMsgSentTimerTask, timeOutInterval);
									}
									msgTimerOn = true;
								}
							}
							ESTADO = "ESTABLISHED";
						}
						continue;
					case "SYN SENT":
						if (getSyn(data) && getAck(data)) {
							System.out.println("Eu: " + socket.getLocalAddress().getHostName() +
									", " + socket.getLocalPort() + " Recebi syn-ack de " + dp.getAddress().getHostName() + ", " +
									dp.getPort());
							remotePort = dp.getPort();
							remoteInetAddress = dp.getAddress();
							socket.connect(remoteInetAddress, remotePort);
							synchronized (msgSentTimer) {
								for (int i = sendBase; i < getackNum(data); ++i) {
									testeSendBufferEstado[circulariza(i)] = 3;
								}
//								try {
//									msgSentTimer.cancel();
//									msgSentTimer = new Timer();
//								} catch (IllegalStateException e) {
//									msgSentTimer = new Timer();
//								}
								testeMsgSentTimerTask.cancel();
								testeMsgSentTimerTask = new MsgSentTimeOut();
								msgTimerOn = false;
								sendBase = getackNum(data);
								int peppa = transmitidoNaoReconhecido(nextSeqNum);
								if (peppa != -1) { // se tiver algum segmento não reconhecido, começar temporizador pra ele
									pktTimer = peppa;
									try {
										timeoutTries = 0;
										msgSentTimer.schedule(testeMsgSentTimerTask, timeOutInterval);
									} catch (IllegalStateException e) {
										testeMsgSentTimerTask.cancel();
										testeMsgSentTimerTask = new MsgSentTimeOut();
										msgSentTimer.schedule(testeMsgSentTimerTask, timeOutInterval);
									}
									msgTimerOn = true;
								}
							}
//							socket.connect(remoteInetAddress, remotePort);
							byte[] ack = new byte[headerLength];
							lastPacketRcvd = ackNum;
							++ackNum;
							sendWindowSize = getRcvwnd(data);
							rcvwnd = (RcvBuffer - ((lastPacketRcvd + 1) - (rcvBase - 1)));
//							System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
//									+ " - (" + rcvBase + " - 1)))");
							cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
							DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
							// envio imediato de um ACK
							
							synchronized (delayedAckTimer) {
								if (ackTimerOn) {
//									try {
//										delayedAckTimer.cancel();
//										delayedAckTimer = new Timer();
//									} catch (IllegalStateException e) {
//										delayedAckTimer = new Timer();
//									}
									testeDelayedAckTimerTask.cancel();
									testeDelayedAckTimerTask = new DelayedAckTimeOut();
									ackTimerOn = false;

								}
							}
							rcvLastAcked = ackNum - 1;
							socket.send(ACK);
							System.out.println(socket.getLocalAddress().getHostName() +", " + socket.getLocalPort() + " " +"Mandei o ack " + ackNum);
							synchronized (testeRcvBuffer) {
								testeRcvBuffer[circulariza(getSeqNum(data))] = dp;
								// // testeRcvBufferEstado[circulariza(getSeqNum(data))] = 2;
								testeRcvBuffer.notify();
							}
							ESTADO = "ESTABLISHED";
						}
						continue;
					case "ESTABLISHED":
						// GERA ACK
						if (!apenasAck) { // não se acka um ack
							if (getSeqNum(data) > lastPacketRcvd + rcvwnd) {
								System.out.println("Não cabe na janela de recepção, descartei essa merda");
								byte[] ack = new byte[headerLength];
								rcvwnd = (RcvBuffer - ((lastPacketRcvd + 1) - (rcvBase - 1)));
								System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + (lastPacketRcvd + 1)
										+ " - (" + rcvBase + " - 1))) = " + rcvwnd);
								cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
								DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
								// envio imediato de um ACK
								socket.send(ACK);
								continue;
							}
							synchronized (testeRcvBuffer) {
//								System.out.println("Recebi o pacote " + getSeqNum(data) + " ESTADO ESTABLISHED");
								if (getSeqNum(data) > lastPacketRcvd) {
									lastPacketRcvd = getSeqNum(data);
									System.out.println("lastPacketRcvd: " + lastPacketRcvd);
								}
								if (ackNum == getSeqNum(data)) {
									// chegada de datagrama com número de sequência esperado
									
									// guarda datagrama no lugar certo
									testeRcvBuffer[circulariza(getSeqNum(data))] = dp;
									testeRcvBufferEstado[circulariza(getSeqNum(data))] = true;
									testeRcvBuffer.notify();
									
									synchronized (testeSendBuffer) {
										sendWindowSize = getRcvwnd(data);
										testeSendBuffer.notifyAll();
									}
									
//									int peppa = recebidoNaoReconhecido(ackNum);

									
									// esse pacote pode ter sido um que preenche lacuna. preciso buscar nos estados
									// e colocar o próximo esperado (ackNum) no primeiro com estado 0 que eu encontrar.
									boolean preenche = false;
									int peppa2 = descobreProximoEsperado();
									ackNum = getSeqNum(data) + 1;
									if (peppa2 != -1 && ackNum != peppa2) {
										// se próximo esperado for diferente do depois do que chegou,
										// quer dizer que conseguiu conectar blocos (preencheu lacuna)
										ackNum = peppa2;
										preenche = true;
									}
									if (preenche) {
										// chegada de um segmento que preenche, parcial ou completamente, a lacuna de dados recebidos
										synchronized (delayedAckTimer) {
											if (ackTimerOn) {
//												try {
//													delayedAckTimer.cancel();
//													delayedAckTimer = new Timer();
//												} catch (IllegalStateException e) {
//													delayedAckTimer = new Timer();
//												}
												testeDelayedAckTimerTask.cancel();
												testeDelayedAckTimerTask = new DelayedAckTimeOut();
												ackTimerOn = false;

											}
										}
										System.out.println("segmento que preenche");

										byte[] ack = new byte[headerLength];
										rcvwnd = (RcvBuffer - ((lastPacketRcvd + 1) - (rcvBase - 1)));
										System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + (lastPacketRcvd + 1)
												+ " - (" + rcvBase + " - 1)))");
										System.out.println("Minha rcvwnd: " + rcvwnd);
										cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
										DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
										// envio imediato de um ACK
										socket.send(ACK);
										rcvLastAcked = ackNum - 1;
										System.out.println(socket.getLocalAddress().getHostName() +", " + socket.getLocalPort() + " " +"Mandei o ack " + ackNum);
										for (int i = 0; i < ackNum; ++i) { // ack cumulativo enviado, atualizar
											// testeRcvBufferEstado[circulariza(i)] = 2;
										}
									} else if (rcvLastAcked == ackNum - 2) {										

										System.out.println(socket.getLocalAddress().getHostName() +", " + socket.getLocalPort() + " " +"Coloquei ack " + ackNum+ " como delayed ack");
										// segmento na ordem, todos os dados até o número de seq esperado já tiveram seu ACK enviado
										rcvwnd = (RcvBuffer - ((lastPacketRcvd + 1) - (rcvBase - 1)));
										System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + (lastPacketRcvd + 1)
												+ " - (" + rcvBase + " - 1)))");
										System.out.println("Minha rcvwnd: " + rcvwnd);
										synchronized (delayedAckTimer) {
											ackTimer = ackNum;
											try {
												delayedAckTimer.schedule(testeDelayedAckTimerTask, 500);
											} catch (IllegalStateException e) {
												testeDelayedAckTimerTask.cancel();
												testeDelayedAckTimerTask = new DelayedAckTimeOut();
												delayedAckTimer.schedule(testeDelayedAckTimerTask, 500);
											}
											ackTimerOn = true;
										}
									} else {
										// segmento na ordem, tem outro segmento na ordem esperando por transmissão de ACK
										synchronized (delayedAckTimer) {
											if (ackTimerOn) {
//												try {
//													delayedAckTimer.cancel();
//													delayedAckTimer = new Timer();
//												} catch (IllegalStateException e) {
//													delayedAckTimer = new Timer();
//												}
												
												testeDelayedAckTimerTask.cancel();
												testeDelayedAckTimerTask = new DelayedAckTimeOut();
												ackTimerOn = false;
											}
										}
											
										System.out.println("segmento na ordem, tem outro na ordem esperando por transmissão de ack");
										System.out.println("rcvLastAcked: " + rcvLastAcked);
										byte[] ack = new byte[headerLength];
										rcvwnd = (RcvBuffer - ((lastPacketRcvd + 1) - (rcvBase - 1)));
										System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + (lastPacketRcvd + 1)
												+ " - (" + rcvBase + " - 1)))");
										System.out.println("Minha rcvwnd: " + rcvwnd);

										cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
										DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
										// envio imediato de um único ACK cumulativo, reconhecendo ambos os pacotes
										System.out.println("Construi um ack:");
										System.out.println("SeqNum: " + nextSeqNum +
												"\nackNum: " + ackNum +
												"\nrcvwnd: " + rcvwnd +
												"\nack: " + getAck(data) +
												"\nackme: " + getAckMe(data));
										rcvLastAcked = ackNum - 1;
										System.out.println("pDescartaPacote: " + pDescartaPacote);
										if (random.nextDouble() >= pDescartaPacote) { // simula perda de acks no caminho
											socket.send(ACK);
											System.out.println("Mandei o ack " + ackNum);
										} else {
											System.out.println("Descartei o ack que ia enviar hehe");
										}
										
										
										// testeRcvBufferEstado[circulariza(peppa)] = 2; // ack enviado
										// // testeRcvBufferEstado[circulariza(peppa + 1)] = 2; // ack enviado
									}
								} else if (ackNum < getSeqNum(data)) {
									// chegada de um segmento fora de ordem com número de sequência mais alto do que o esperado
									// lacuna detectada
									synchronized (delayedAckTimer) {
										if (ackTimerOn) {
											testeDelayedAckTimerTask.cancel();
											testeDelayedAckTimerTask = new DelayedAckTimeOut();
											ackTimerOn = false;
										}
									}
									
									synchronized (testeSendBuffer) {
										sendWindowSize = getRcvwnd(data);
										testeSendBuffer.notifyAll();
									}
									
									System.out.println(ackNum + " < " +getSeqNum(data));
									// guarda datagrama no lugar certo
									testeRcvBuffer[circulariza(getSeqNum(data))] = dp;
									testeRcvBufferEstado[circulariza(getSeqNum(data))] = true;
									testeRcvBuffer.notify();
									
									byte[] ack = new byte[headerLength];
									rcvwnd = (RcvBuffer - ((lastPacketRcvd + 1) - (rcvBase - 1)));
									System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + (lastPacketRcvd + 1)
											+ " - (" + rcvBase + " - 1)))");
									System.out.println("Minha rcvwnd: " + rcvwnd);

									cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
									DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
									// envio imediato de um ACK duplicado, indicando número de sequência do pacote seguinte esperado
									socket.send(ACK);
									System.out.println("Mandei o ack " + ackNum);

								} else {
									// recebi pacote que já reconheci, repetir ack
									
									synchronized (testeSendBuffer) {
										sendWindowSize = getRcvwnd(data);
										testeSendBuffer.notifyAll();
									}
									
									System.out.println("Recebi pacote que já reconheci, repetir ack");
									byte[] ack = new byte[headerLength];
									rcvwnd = (RcvBuffer - ((lastPacketRcvd + 1) - (rcvBase - 1)));
									System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + (lastPacketRcvd + 1)
											+ " - (" + rcvBase + " - 1)))");
									System.out.println("Minha rcvwnd: " + rcvwnd);

									cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
									DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
									
									synchronized (delayedAckTimer) {
										if (ackTimerOn) {
											testeDelayedAckTimerTask.cancel();
											testeDelayedAckTimerTask = new DelayedAckTimeOut();
											ackTimerOn = false;
										}
									}
									
									socket.send(ACK);
									System.out.println(socket.getLocalAddress().getHostName() +", " + socket.getLocalPort() + " " +"Mandei o ack " + ackNum);
								}
							}
						}
						synchronized (testeSendBuffer) {
							if (getAckMe(data) && getAck(data)) {
								System.out.println(socket.getLocalAddress().getHostName() +", " + socket.getLocalPort() + " " +"Recebi um ack-ackme");
								synchronized (ackMeTimer) {
									if (ackMeTimerOn) {
										testeAckMeTimerTask.cancel();
										testeAckMeTimerTask = new AckMePls();
										ackMeTimerOn = false;
									}
								}
								sendWindowSize = getRcvwnd(data);
								System.out.println("acknum do ack-ackme: " + getackNum(data) +
										"\nrcvwnd do ack-ackme: " + getRcvwnd(data) +
										"\nsendWindowSize pra mim agora: " + sendWindowSize);
								if (getackNum(data) > sendBase) {
									if (getackNum(data) > packetSample && packetSample != -1) {
										estimaRTT();
									}
									System.out.println("sendBase = " + getackNum(data));
									sendBase = getackNum(data);
									synchronized (msgSentTimer) {
										
										testeMsgSentTimerTask.cancel();
										testeMsgSentTimerTask = new MsgSentTimeOut();
										msgTimerOn = false; // pq chegou e tal
										int peppa = transmitidoNaoReconhecido(nextSeqNum);
										System.out.println("transmitidoNaoReconhecido: " + peppa +
												"\nlastPacketSent: " + lastPacketSent);
										if (peppa != -1) { // se tiver algum segmento não reconhecido, começar temporizador pra ele
											pktTimer = peppa;
											timeOutInterval = timeOutRTT;
											try {
												timeoutTries = 0;
												msgSentTimer.schedule(testeMsgSentTimerTask, timeOutInterval);
											} catch (IllegalStateException e) {
												testeMsgSentTimerTask.cancel();
												testeMsgSentTimerTask = new MsgSentTimeOut();
												msgSentTimer.schedule(testeMsgSentTimerTask, timeOutInterval);
											}
											msgTimerOn = true;
										}
									}
								}
								testeSendBuffer.notifyAll();

							} else if (getackNum(data) > sendBase) {
								if (getackNum(data) > packetSample && packetSample != -1) {
									estimaRTT();
								}
								
								sendWindowSize = getRcvwnd(data);	
								if (recuperacaoRapida) {
									// recuperação rápida
									congwin = (short) Math.max(ssthresh, 1);
									acksDuplicados = 0;
									recuperacaoRapida = false;
								} else if (congwin < ssthresh) {
									//partida lenta
									congwin = (short) (congwin + (getackNum(data) - sendBase));
								} else {
									//prevenção de congestionamento
									varPrevencao = (short) (varPrevencao + (getackNum(data) - sendBase));
									if (varPrevencao >= congwin) {
										++congwin;
										varPrevencao = 0;
									}
								}
								acksDuplicados = 0;
								
								System.out.println(socket.getLocalAddress().getHostName() +", " + socket.getLocalPort() + " " +"Recebi ack " + getackNum(data));
								
								for (int i = sendBase; i < getackNum(data); ++i) {
									testeSendBufferEstado[circulariza(i)] = 3;
								}
								sendBase = getackNum(data);
								System.out.println(socket.getLocalAddress().getHostName() +", " + socket.getLocalPort() + " " +"sendBase agora é " + sendBase);

								synchronized (msgSentTimer) {
									
									testeMsgSentTimerTask.cancel();
									testeMsgSentTimerTask = new MsgSentTimeOut();
									msgTimerOn = false; // pq chegou e tal
									int peppa = transmitidoNaoReconhecido(nextSeqNum);
									System.out.println("transmitidoNaoReconhecido: " + peppa +
											"\nlastPacketSent: " + lastPacketSent +
											"\ntimeOutRTT: " + timeOutRTT);
									if (peppa != -1) { // se tiver algum segmento não reconhecido, começar temporizador pra ele
										pktTimer = peppa;
										timeOutInterval = timeOutRTT;
										try {
											timeoutTries = 0;
											msgSentTimer.schedule(testeMsgSentTimerTask, timeOutInterval);
										} catch (IllegalStateException e) {
											testeMsgSentTimerTask.cancel();
											testeMsgSentTimerTask = new MsgSentTimeOut();
											msgSentTimer.schedule(testeMsgSentTimerTask, timeOutInterval);
										}
										msgTimerOn = true;
									}
								}
								testeSendBuffer.notifyAll();
							} else if (apenasAck && getackNum(data) < nextSeqNum) {
								
								sendWindowSize = getRcvwnd(data);
								
								// ack duplicado. apenasAck é para não achar que dado chegando é ack duplicado.
								// < nextSeqNum é para não garantir que ele só está reenviando ack do último segmento
								// que enviei
								System.out.println("Recebi ack duplicado de " + getackNum(data));
//								acksDuplicados[circulariza(getackNum(data))]++;
								if (recuperacaoRapida) {
									++congwin;
								} else {
									++acksDuplicados;
								}
								if (acksDuplicados == 3) { // retransmissão rápida
									synchronized (pktsPerdidos) {
										++pktsPerdidos[0];
										pktsPerdidos.notify();
									}
									recuperacaoRapida = true;
									ssthresh = (short) Math.max((congwin >> 1), 1);
									congwin = (short) (ssthresh + 3);
									
									byte[] newData = testeSendBuffer[circulariza(getackNum(data))].getData();
									rcvwnd = (RcvBuffer - ((lastPacketRcvd + 1) - (rcvBase - 1)));
									System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + (lastPacketRcvd + 1)
											+ " - (" + rcvBase + " - 1)))");
									System.out.println("Minha rcvwnd: " + rcvwnd);
									// antes de retransmitir o pacote, atualiza o campo rcvwnd dele hehe.
									setRcvwnd(newData, rcvwnd);
									testeSendBuffer[circulariza(getackNum(data))] = new DatagramPacket(newData, testeSendBuffer[circulariza(getackNum(data))].getLength(),
											testeSendBuffer[circulariza(getackNum(data))].getAddress(), testeSendBuffer[circulariza(getackNum(data))].getPort());
									socket.send(testeSendBuffer[circulariza(getackNum(data))]); // envia logo segmento perdido
									timeSent = System.currentTimeMillis();
									System.out.println("Retransmiti rapidamente o pacote " + getackNum(data) +
											"\nCujo seqNum é " + getSeqNum(testeSendBuffer[circulariza(getackNum(data))].getData()));
//									acksDuplicados[circulariza(getackNum(data))] = 0;
									acksDuplicados = 0;
									synchronized (msgSentTimer) {
										if (!msgTimerOn) {
											pktTimer = getackNum(data);
											packetSample = pktTimer;
											timeOutInterval = timeOutRTT;
											try {
												timeoutTries = 0;
												msgSentTimer.schedule(testeMsgSentTimerTask, timeOutInterval);
											} catch (IllegalStateException e) {
												testeMsgSentTimerTask.cancel();
												testeMsgSentTimerTask = new MsgSentTimeOut();
												msgSentTimer.schedule(testeMsgSentTimerTask, timeOutInterval);
											}
											msgTimerOn = true;
										}
									}
								}
								testeSendBuffer.notifyAll();
							}
						}
					}
				} catch (PortUnreachableException e1) {
					e1.printStackTrace();
					portUnreachable = true;
					try {
						close(false);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					if (!closed) {
						e.printStackTrace();
					}
				}
				if (rcvwnd < 0 || sendWindowSize > 500)
					System.exit(0);
			}
			System.out.println("Terminei RecebeDados");
		}
		
	}
	
	private void cabecalha(byte[] data, int nextSeqNum, int ackNum,
			int rcvwnd, byte ack, byte syn, byte fin, byte ackMe) {
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
	    
	    data[13] = ackMe;
	}
	
	private void setRcvwnd(byte[] data, int rcvwnd) {
		data[8] = (byte) (rcvwnd >>> 8);
		data[9] = (byte) (rcvwnd & 0x00ffL);
	}

	
	private int getackNum(byte[] data) {
//		return (int) (data[4] << 24) + (int) (data[5] << 16) + (int) (data[6] << 8) + (int) data[7];
		return ((data[4] & 0xFF) << 24) + ((data[5] & 0xFF) << 16) + ((data[6] & 0xFF) << 8) + ((data[7] & 0xFF));
	}
	
	private int getSeqNum(byte[] data) {
//		return (int) (data[0] << 24) + (int) (data[1] << 16) + (int) (data[2] << 8) + (int) data[3];
		return ((data[0] & 0xFF) << 24) + ((data[1] & 0xFF) << 16) + ((data[2] & 0xFF) << 8) + ((data[3] & 0xFF));
	}
	
	private int getRcvwnd(byte[] data) {
//		return (short) ((short) (data[8] << 8) + (short) (data[9]));
		int ret = ((data[8] & 0xFF) << 8) + ((data[9] & 0xFF)); // dessa forma não vai fazer 199 virar -57...
		if (ret > RcvBuffer) {
			System.err.println("Rcvwnd veio negativa, puta que pariu caralho");
			ret = 0;
		}
		return ret;
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
	
	private boolean getAckMe(byte[] data) {
		try {
			return data[13] == 1 ? true : false;
		} catch (Exception e) {
			return false;
		}
	}

	private void estimaRTT() {
		timeAcked = System.currentTimeMillis();
		System.out.println("sampleRTT calculada a partir de: " + packetSample);
		packetSample = -1;
		sampleRTT = timeAcked - timeSent;
		
		if (sampleRTT < 50) {
			sampleRTT = 50;
		}
		
		System.out.println("sampleRTT: " + sampleRTT);
		if (estimatedRTT[0] == -1) {
			estimatedRTT[0] = sampleRTT;
		}
		estimatedRTT[0] = (((estimatedRTT[0] << 2) + (estimatedRTT[0] << 1) // 7estimatedRTT/8 hehehehe
				+ estimatedRTT[0]) >> 3) + (sampleRTT >> 3);
		System.out.println("estimatedRTT: " + estimatedRTT[0]);
		
		devRTT = (((devRTT << 1) + devRTT) >> 2) + (Math.abs(sampleRTT - estimatedRTT[0]) >> 2);
		System.out.println("devRTT: " + devRTT);
		
		timeOutRTT = estimatedRTT[0] + (devRTT << 2);
	}
}