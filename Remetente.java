package rdt;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Remetente {
	
	private double pDescartaAck = 0.025;
	private double pDescartaPacote = 0.025;
	
	Random random = new Random();
	
	private String ESTADO;
	
	private short RcvBuffer = 2;
	private short RcvBufferSize = 200;
	private int headerLength = 14;
	
	private int remotePort;
	private InetAddress remoteInetAddress;
	private int ackNum;
	private int nextSeqNum; // nextSeqNum - 1 nos diz LastPacketSent
	private int sendBase; // sendBase - 1 nos diz LastPacketAcked
	private int sendWindowSize;
	private int rcvwnd;
	private DatagramPacket[] testeSendBuffer = new DatagramPacket[RcvBufferSize];
	private byte[] testeSendBufferEstado = new byte[RcvBufferSize]; // 1 n enviado, 2 enviado, 3 reconhecido
	private int[] acksDuplicados = new int[RcvBufferSize];
	private DatagramPacket[] testeRcvBuffer = new DatagramPacket[RcvBufferSize];
	private int[] testeRcvBufferEstado = new int[RcvBufferSize]; // 1 recebido, 2 ack enviado, 3 dado pra camada superior
	
	/**
	 * rcvBase nos diz onde está o pacote válido para ser pego.
	 * rcvBase - 1 nos diz o último pacote lido pela aplicação
	 */
	private int rcvBase;
	private int lastPacketRcvd;
	private int rcvLastAcked;
	private DatagramSocket socket;
	private int pktTimer, ackTimer; // diz qual pacote/ack está associado ao timer
	private boolean close = false, msgTimerOn = false, ackTimerOn = false, ackMeTimerOn = false;
	private long timeOutInterval = 1000;
	private long timeOutRTT = 1000;
	private long timeSent, timeAcked, sampleRTT, estimatedRTT = 0, devRTT;
	private int packetSample;
	
	private Timer msgSentTimer = new Timer();
	private Timer delayedAckTimer = new Timer();
	private Timer ackMeTimer = new Timer();
	
	private Thread tEnvia, tRecebe;
	
	public Remetente(String remoteIP, int remotePort, String ESTADO, int ackNum) throws IOException {
		this(-1, remoteIP, remotePort, ESTADO, ackNum);
	}
	
	public Remetente(String remoteIP, int remotePort) throws IOException {
		this(-1, remoteIP, remotePort, "CLOSED", 0);
	}
	public Remetente(int port, String remoteIP, int remotePort) throws IOException {
		this(port, remoteIP, remotePort, "CLOSED", 0);
	}
	
	public Remetente(int port, String remoteIP, int remotePort, String ESTADO, int ackNum) throws IOException {
		this.ESTADO = ESTADO;
		byte[] data = new byte[1024];
		this.nextSeqNum = this.sendBase = this.rcvBase = this.rcvLastAcked = 0; //new Random().nextInt(Integer.MAX_VALUE - 1);
		this.rcvwnd = this.sendWindowSize = RcvBuffer;
		switch (this.ESTADO) {
		case "CLOSED":
			if (port == -1) {
				this.socket = new DatagramSocket();
			} else {
				this.socket = new DatagramSocket(port);
			}
			this.remotePort = remotePort;
			this.remoteInetAddress = InetAddress.getByName(remoteIP);

			tRecebe = new Thread(new RecebeDados());
			tEnvia = new Thread(new EnviaDados());
			tEnvia.start();
			tRecebe.start();
			// ACTIVE OPEN
			send(null, 0, (byte) 0, (byte) 1, (byte) 0, (byte) 0); 
			// SYN SENT
			this.ESTADO = "SYN SENT";
			
			// recebe syn + ack
			System.out.println("Vou receber syn + ack");
				receive(data, 1024);
			System.out.println("Logo depois de receber syn + ack no construtor");
			break;
		case "SYN RECEIVED":
			this.ackNum = ackNum;
			this.rcvBase++;
			this.socket = new DatagramSocket();
			this.remoteInetAddress = InetAddress.getByName(remoteIP);
			this.remotePort = remotePort;
			tRecebe = new Thread(new RecebeDados());
			tEnvia = new Thread(new EnviaDados());
			tEnvia.start();
			tRecebe.start();
			send(null, 0, (byte) 1, (byte) 1, (byte) 0, (byte) 0); // manda syn + ack
			this.socket.connect(this.remoteInetAddress, this.remotePort);
		}
	}
	
	public void send(byte[] data, int length, byte ack, byte syn, byte fin, byte ackMe) throws IOException {
		System.out.println("Tentarei ganhar lock de testeSendBuffer no send");
		synchronized (this.testeSendBuffer) {
			System.out.println("Ganhei lock de testeSendBuffer no send");
			byte[] guardar = new byte[headerLength + length];
			rcvwnd = (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
			System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
					+ " - (" + rcvBase + " - 1)))");
			cabecalha(guardar, nextSeqNum, ackNum, rcvwnd, ack, syn, fin, ackMe);
			for (int i = headerLength, j = 0; i < headerLength + length && j < data.length; i++, j++) {
				guardar[i] = data[j];
			}
			testeSendBuffer[(this.nextSeqNum)] = new DatagramPacket(guardar, guardar.length,
					this.remoteInetAddress, this.remotePort);
			this.testeSendBufferEstado[(this.nextSeqNum)] = 1;

			this.nextSeqNum++;
			System.out.println("Coloquei o pacote " + (this.nextSeqNum - 1) + " no sendBuffer pra ser enviado");
			testeSendBuffer.notify();
			System.out.println("Notifiquei o envia dados do testeSendBuffer OK");
		}
	}
	
	public void send(byte[] data, int length) throws IOException {
		send(data, length, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
	}
	
	public int receive(byte[] data, int length) {
		int b = 0;
		System.out.println("Tentarei ganhar lock no testeRcvBuffer em receive");
//		synchronized (testeRcvBuffer) {
//			System.out.println("Ganhei lock no testeRcvBuffer em receive");
//			while (rcvBase == ackNum) {
//				// pois não tem nenhum pacote ainda
//				try {
//					System.out.println("Estarei esperando por rcvBase != ackNum em receive");
//					testeRcvBuffer.wait();
//					System.out.println("Acabou a espera do testeRcvBuffer em receive");
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			System.out.println("Vou tentar dar o pacote " + rcvBase + " para a aplicação");
//			byte[] temp = testeRcvBuffer[rcvBase].getData();
//			for (int i = 0, j = headerLength; i < length && j < testeRcvBuffer[rcvBase].getLength(); i++, j++) {
//				data[i] =  temp[j];
//				b++;
//			}
//			System.out.println("Mandei pra aplicação o pacote " + rcvBase);
////			testeRcvBufferEstado[rcvBase] = 3;
//			rcvBase += 1;
//		}
		
		synchronized (testeRcvBuffer) {
			System.out.println("Ganhei lock no testeRcvBuffer em receive");
			while (rcvBase == ackNum) {
				// pois não tem nenhum pacote ainda
				try {
					System.out.println("Estarei esperando por rcvBase != ackNum em receive");
					testeRcvBuffer.wait();
					System.out.println("Acabou a espera do testeRcvBuffer em receive");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		System.out.println("Vou tentar dar o pacote " + rcvBase + " para a aplicação");
		byte[] temp = testeRcvBuffer[(rcvBase)].getData();
		for (int i = 0, j = headerLength; i < length && j < testeRcvBuffer[(rcvBase)].getLength(); i++, j++) {
			data[i] =  temp[j];
			b++;
		}
		System.out.println("Mandei pra aplicação o pacote " + rcvBase);
//			testeRcvBufferEstado[rcvBase] = 3;
		rcvBase += 1;
		
		return b;
	}
	
	public static void main(String[] args) throws IOException {
		Remetente teste = new Remetente("localhost", 2020);
		byte[] teste1 = "oi".getBytes("UTF-8");
		System.out.println("Vou usar o send na main");
		teste.send(teste1, teste1.length);
		System.out.println("Usei o send na main");
		byte[] teste2 = new byte[4];
		teste.receive(teste2, 2);

		for (int i = 10; i < 100; i++) {
			teste1 = ("oi" + i).getBytes("UTF-8");
			teste.send(teste1, 4);
		}

	}
	
	public String getEstado() {
		return this.ESTADO;
	}
	
//	private int circulariza(int index) {
//		return (index % RcvBufferSize + RcvBufferSize) % RcvBufferSize;
//	}
	
	private boolean podeEnviar() {
		for (int i = sendBase; i < sendBase + sendWindowSize && i < nextSeqNum; i++) {//i < nextSeqNum && i < testeSendBufferEstado.length; i++) {
			if (testeSendBufferEstado[(i)] == 1) {
				return true;
			}
		}
		return false;
	}
	
	private int transmitidoNaoReconhecido(int end) {
		for (int i = sendBase; i < sendBase + sendWindowSize && i < nextSeqNum && i < end && i < testeSendBufferEstado.length; i++) {
			if (testeSendBufferEstado[(i)] == 2) {
				return i;
			}
		}
		return -1;
	}
	
	private int recebidoNaoReconhecido(int end) {
		for (int i = rcvLastAcked; i < end && i < testeRcvBufferEstado.length; i++) {
			if (testeRcvBufferEstado[(i)] == 1) {
				return i;
			}
		}
		return -1;
	}
	
	private int descobreProximoEsperado() {
		for (int i = ackNum; i < testeRcvBufferEstado.length; i++) {
			if (testeRcvBufferEstado[(i)] == 0) {
				return i;
			}
		}
		return -1;
	}
	
	public void close() {
		this.close = true;
		tEnvia.interrupt();
		tRecebe.interrupt();
//		tEnvia.join();
//		tRecebe.join();
		this.socket.close();
	}
	
	class EnviaDados implements Runnable {
		public void run() {
			while (!close) {
				synchronized (testeSendBuffer) {
					while (!podeEnviar()) {
						try {
							System.out.println("Estou esperando no testeSendBuffer pra enviar dados");
							if (sendWindowSize == 0) {
								synchronized (ackMeTimer) {
									if (ackMeTimerOn) {
										try {
											ackMeTimer.cancel();
											ackMeTimer = new Timer();
										} catch (IllegalStateException e) {
											ackMeTimer = new Timer();
										}
									}
									ackMeTimerOn = true;
									try {
										ackMeTimer.schedule(new AckMePls(), timeOutRTT);
									} catch (IllegalStateException e) {
										ackMeTimer = new Timer();
										ackMeTimer.schedule(new AckMePls(), timeOutRTT);
									}
								}
							}
							testeSendBuffer.wait();
							System.out.println("Saí da espera no testeSendBuffer");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					// se chegou aqui quer dizer que tem coisa no sendBuffer para enviar
					System.out.println("Saí da espera no testeSendBuffer de verdade com janela receptora do dest (" +
					 sendWindowSize + ") e enviarei dados");
					try {
						for (int i = sendBase; i < nextSeqNum && i < sendBase + sendWindowSize; i++) {//i < nextSeqNum; i++) {
							if (testeSendBufferEstado[(i)] == 1) {
								System.out.println("Janela receptora do destinatário (" + sendWindowSize +")");
								System.out.println("Pacotes no ar: " + (i - 1 - (sendBase - 1)));
								while (i - (sendBase - 1) > sendWindowSize) { // lastByteSent - lastByteAcked
									System.out.println("Pacotes no ar: " + (i - 1 - (sendBase - 1)));
									try {
										System.out.println("Vou esperar a janela receptora do destinatário (" + sendWindowSize + ")"
												+ " liberar espaço.");
										if (sendWindowSize == 0) {
											synchronized (ackMeTimer) {
												if (ackMeTimerOn) {
													try {
														ackMeTimer.cancel();
														ackMeTimer = new Timer();
													} catch (IllegalStateException e) {
														ackMeTimer = new Timer();
													}
												}
												ackMeTimerOn = true;
												try {
													ackMeTimer.schedule(new AckMePls(), timeOutRTT);
												} catch (IllegalStateException e) {
													ackMeTimer = new Timer();
													ackMeTimer.schedule(new AckMePls(), timeOutRTT);
												}
											}
										}
										testeSendBuffer.wait();
									} catch (InterruptedException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
								synchronized (ackMeTimer) {
									if (ackMeTimerOn) {
										ackMeTimer.cancel();
										ackMeTimer = new Timer();
										ackMeTimerOn = false;
									}
								}
								System.out.println("Enviei o pacote " + i);
								socket.send(testeSendBuffer[(i)]);
								
								synchronized (msgSentTimer) {
									if (!msgTimerOn) {// && enviou) {
										pktTimer = sendBase;
										timeOutInterval = timeOutRTT;
										msgSentTimer.schedule(new MsgSentTimeOut(), timeOutInterval);
										msgTimerOn = true;
									}
								}
								
								if (sampleRTT != -1) {
									timeSent = System.currentTimeMillis();
									sampleRTT = -1;
									packetSample = i;
								}
								testeSendBufferEstado[(i)] = 2;
								synchronized (delayedAckTimer) {
									if (ackTimerOn) {
										if (ackTimer <= ackNum) {
											delayedAckTimer.cancel();
											ackTimerOn = false;
											delayedAckTimer = new Timer();
										}
									}
								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	class AckMePls extends TimerTask {
		public void run() {
			synchronized (ackMeTimer) {
				try {
					System.out.println("Enviando ackMe pois sendWindowSize == " + sendWindowSize);
					ackMeTimer.cancel();
					ackMeTimer = new Timer();
					byte[] data = new byte[headerLength];
					rcvwnd = (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
					System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
							+ " - (" + rcvBase + " - 1)))");
					
					cabecalha(data, nextSeqNum, ackNum, rcvwnd, (byte) 0, (byte) 0, (byte) 0, (byte) 1);
					DatagramPacket dp = new DatagramPacket(data, headerLength, remoteInetAddress, remotePort);
					socket.send(dp);
					try {
						ackMeTimer.schedule(new AckMePls(), timeOutRTT);
					} catch (IllegalStateException e) {
						ackMeTimer = new Timer();
						ackMeTimer.schedule(new AckMePls(), timeOutRTT);
					}
				} catch (IOException e) {
					// TODO
					e.printStackTrace();
				}
			}
		}
	}
	
	class MsgSentTimeOut extends TimerTask {
		public void run() {
			synchronized (msgSentTimer) {
				System.out.println("Pacote " + pktTimer + " deu timeout.");
				try {
					msgSentTimer.cancel();
					msgSentTimer = new Timer();
					byte[] newData = testeSendBuffer[(pktTimer)].getData();
					rcvwnd = (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
					System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
							+ " - (" + rcvBase + " - 1)))");
					setRcvwnd(newData, rcvwnd);
					testeSendBuffer[(pktTimer)] = new DatagramPacket(newData, testeSendBuffer[(pktTimer)].getLength(),
							testeSendBuffer[(pktTimer)].getAddress(), testeSendBuffer[(pktTimer)].getPort());
					socket.send(testeSendBuffer[(pktTimer)]);
					if (timeOutInterval < 10000) { // duplicação do tempo de expiração
						timeOutInterval = timeOutInterval << 1;
					}
					try {
						msgSentTimer.schedule(new MsgSentTimeOut(), timeOutInterval);
					} catch (IllegalStateException e) {
						msgSentTimer = new Timer();
						msgSentTimer.schedule(new MsgSentTimeOut(), timeOutInterval);
					}
				} catch (IOException e) {
					// TODO rip
					e.printStackTrace();
				}
			}
		}
	}
	
	class DelayedAckTimeOut extends TimerTask {
		public void run() {
			synchronized (delayedAckTimer) {
				System.out.println("Delayed Ack " + ackTimer + " deve ser enviado agora!");
				try {
					delayedAckTimer.cancel();
					ackTimerOn = false;
					delayedAckTimer = new Timer();
					byte[] ack = new byte[headerLength];
					rcvwnd = (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
					System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
							+ " - (" + rcvBase + " - 1)))");
					cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
					DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
					// esperou 500 ms pela chegada de outro pacote na ordem, que deveria cancelar.
					// se não foi cancelado ou seja, entrou aqui, então envia o ACK mesmo.
					testeRcvBufferEstado[(ackTimer)] = 2;
					if (ackTimer > rcvLastAcked) {
						rcvLastAcked = ackTimer;
					}
					socket.send(ACK);
				} catch (IOException e) {
					// TODO rip
					e.printStackTrace();
				}
			}
		}
	}
	
	class RecebeDados implements Runnable {
		
		public void run() {
			while (!close) {
				try {
					byte[] data = new byte[1024];
					DatagramPacket dp = new DatagramPacket(data, data.length);
					System.out.println("Vou esperar por um datagrama no RecebeDados");
					socket.receive(dp);
					System.out.println("Recebi um datagrama no RecebeDados");
					System.out.println("SeqNum: " + getSeqNum(data) +
							"\nackNum: " + getackNum(data) +
							"\nrcvwnd: " + getRcvwnd(data));
					
					if (random.nextDouble() < pDescartaPacote) { // simula perda de pacotes
						System.out.println("Perdi o datagram ehehe");
						continue;
					}
					
					System.out.println("Rcvwnd que tem no pacote " + getRcvwnd(data));
					sendWindowSize = getRcvwnd(data); // testando se isso melhora o paralelismo
					
					if (getAckMe(data) && !getAck(data)) { 
						// aqui é para o caso do host remetente estar vendo
						// um sendWindowSize 0 e ficar mandando pacotes pra mim só para receber
						// o ack de volta e ser atualizado sobre o sendWindowSize.
						byte[] ack = new byte[headerLength];
						rcvwnd = (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
						System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
								+ " - (" + rcvBase + " - 1)))");
						// envia de volta o ack-ackMe
						cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 1);
						DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
						// envio imediato de um ACK
						socket.send(ACK);
						timeOutInterval = timeOutRTT;
						continue;
					}
					
					System.out.println("Recebi o pacote " + getSeqNum(dp.getData()) + " em RecebeDados\nESTADO == " + ESTADO);
					System.out.println("rcvwnd que tem no pacote: " + getRcvwnd(data));
					boolean apenasAck = false;
					if (getAck(data) && dp.getLength() == headerLength && !getSyn(data) && !getFin(data)) {
						apenasAck = true;
					}
					switch (ESTADO) {
					case "SYN RECEIVED":
						if (getAck(data)) {
							System.out.println("Recebi ack");

							synchronized (msgSentTimer) {
								for (int i = sendBase; i < getackNum(data); i++) {
									testeSendBufferEstado[(i)] = 3;
								}
								msgSentTimer.cancel();
								msgTimerOn = false;
								msgSentTimer = new Timer();
								sendBase = getackNum(data);
								sendWindowSize = getRcvwnd(data);
								int peppa = transmitidoNaoReconhecido(nextSeqNum);
								if (peppa != -1) { // se tiver algum segmento não reconhecido, começar temporizador pra ele
									pktTimer = peppa;
									msgSentTimer.schedule(new MsgSentTimeOut(), timeOutInterval);
									msgTimerOn = true;
								}
							}
							ESTADO = "ESTABLISHED";
						}
						continue;
					case "SYN SENT":
						if (getSyn(data) && getAck(data)) {
							System.out.println("Recebi syn-ack");
							remotePort = dp.getPort();
							remoteInetAddress = dp.getAddress();

							synchronized (msgSentTimer) {
								for (int i = sendBase; i < getackNum(data); i++) {
									testeSendBufferEstado[(i)] = 3;
								}
								msgSentTimer.cancel();
								msgSentTimer = new Timer();
								msgTimerOn = false;
								sendBase = getackNum(data);
								int peppa = transmitidoNaoReconhecido(nextSeqNum);
								if (peppa != -1) { // se tiver algum segmento não reconhecido, começar temporizador pra ele
									pktTimer = peppa;
									msgSentTimer.schedule(new MsgSentTimeOut(), timeOutInterval);
									msgTimerOn = true;
								}
							}
							socket.connect(remoteInetAddress, remotePort);
							byte[] ack = new byte[headerLength];
							lastPacketRcvd = ackNum;
							ackNum++;
							sendWindowSize = getRcvwnd(data);
							rcvwnd = (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
							System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
									+ " - (" + rcvBase + " - 1)))");
							cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
							DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
							// envio imediato de um ACK
							
							synchronized (delayedAckTimer) {
								if (ackTimerOn) {
									delayedAckTimer.cancel();
									ackTimerOn = false;
									delayedAckTimer = new Timer();
								}
							}
							
							socket.send(ACK);
							System.out.println("Mandei o ack " + ackNum);
							synchronized (testeRcvBuffer) {
								testeRcvBuffer[(getSeqNum(data))] = dp;
								testeRcvBufferEstado[(getSeqNum(data))] = 2;
								testeRcvBuffer.notify();
							}
							ESTADO = "ESTABLISHED";
						}
						continue;
					case "ESTABLISHED":
						// GERA ACK
						if (!apenasAck) { // não se acka um ack
							synchronized (testeRcvBuffer) {
								System.out.println("Recebi o pacote " + getSeqNum(data) + " ESTADO ESTABLISHED");
								if (getSeqNum(data) > lastPacketRcvd) {
									lastPacketRcvd = getSeqNum(data);
									System.out.println("lastPacketRcvd: " + lastPacketRcvd);
								}
								if (ackNum == getSeqNum(data)) {
//									// chegada de datagrama com número de sequência esperado
									
									// guarda datagrama no lugar certo
									testeRcvBuffer[(getSeqNum(data))] = dp;
									testeRcvBufferEstado[(getSeqNum(data))] = 1;
									testeRcvBuffer.notify();
									
									sendWindowSize = getRcvwnd(data);
									
									int peppa = recebidoNaoReconhecido(ackNum);

									
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
												delayedAckTimer.cancel();
												ackTimerOn = false;
												delayedAckTimer = new Timer();
											}
										}
										System.out.println("segmento que preenche");

										byte[] ack = new byte[headerLength];
										rcvwnd = (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
										System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
												+ " - (" + rcvBase + " - 1)))");
										System.out.println("Minha rcvwnd: " + rcvwnd);
										cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
										DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
										// envio imediato de um ACK
										socket.send(ACK);
										System.out.println("Mandei o ack " + ackNum);
										for (int i = 0; i < ackNum; i++) { // ack cumulativo enviado, atualizar
											testeRcvBufferEstado[(i)] = 2;
										}
									} else if (peppa == -1) {										

										System.out.println("Coloquei ack " + ackNum+ " como delayed ack");
										// segmento na ordem, todos os dados até o número de seq esperado já tiveram seu ACK enviado
										synchronized (delayedAckTimer) {
											delayedAckTimer.schedule(new DelayedAckTimeOut(), 500);
											ackTimerOn = true;
											ackTimer = ackNum;
										}
									} else {
										// segmento na ordem, tem outro segmento na ordem esperando por transmissão de ACK
										synchronized (delayedAckTimer) {
											if (ackTimerOn) {
												delayedAckTimer.cancel();
												ackTimerOn = false;
												delayedAckTimer = new Timer();
											}
										}
											
										System.out.println("segmento na ordem, tem outro na ordem esperando por transmissão de ack");

										byte[] ack = new byte[headerLength];
										rcvwnd = (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
										System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
												+ " - (" + rcvBase + " - 1)))");
										System.out.println("Minha rcvwnd: " + rcvwnd);

										cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
										DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
										// envio imediato de um único ACK cumulativo, reconhecendo ambos os pacotes
										System.out.println("Construi um ack:\n");
										System.out.println("SeqNum: " + nextSeqNum +
												"\nackNum: " + ackNum +
												"\nrcvwnd: " + rcvwnd);
										if (random.nextDouble() >= pDescartaAck) { // simula perda de acks no caminho
											socket.send(ACK);
										} else {
											System.out.println("Descartei o ack que ia enviar hehe");
										}
										
										System.out.println("Mandei o ack " + ackNum);
										
										testeRcvBufferEstado[(peppa)] = 2; // ack enviado
										testeRcvBufferEstado[(peppa + 1)] = 2; // ack enviado
										rcvLastAcked = peppa + 1;
									}
								} else if (ackNum < getSeqNum(data)) {
									// chegada de um segmento fora de ordem com número de sequência mais alto do que o esperado
									// lacuna detectada
									synchronized (delayedAckTimer) {
										if (ackTimerOn) {
											delayedAckTimer.cancel();
											ackTimerOn = false;
											delayedAckTimer = new Timer();
										}
									}
									
									sendWindowSize = getRcvwnd(data);
									
									System.out.println(ackNum + " < " +getSeqNum(data));
									// guarda datagrama no lugar certo
									testeRcvBuffer[(getSeqNum(data))] = dp;
									testeRcvBufferEstado[(getSeqNum(data))] = 1;
									testeRcvBuffer.notify();
									
									byte[] ack = new byte[headerLength];
									rcvwnd = (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
									System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
											+ " - (" + rcvBase + " - 1)))");
									System.out.println("Minha rcvwnd: " + rcvwnd);

									cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
									DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
									// envio imediato de um ACK duplicado, indicando número de sequência do pacote seguinte esperado
									socket.send(ACK);
									System.out.println("Mandei o ack " + ackNum);

								} else {
									// recebi pacote que já reconheci, repetir ack
									
									sendWindowSize = getRcvwnd(data);

									System.out.println("Recebi pacote que já reconheci, repetir ack");
									byte[] ack = new byte[headerLength];
									rcvwnd = (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
									System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
											+ " - (" + rcvBase + " - 1)))");
									System.out.println("Minha rcvwnd: " + rcvwnd);

									cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0, (byte) 0);
									DatagramPacket ACK = new DatagramPacket(ack, headerLength, remoteInetAddress, remotePort);
									
									synchronized (delayedAckTimer) {
										if (ackTimerOn) {
											delayedAckTimer.cancel();
											ackTimerOn = false;
											delayedAckTimer = new Timer();
										}
									}
									
									socket.send(ACK);
									System.out.println("Mandei o ack " + ackNum);
								}
							}
						}
						synchronized (testeSendBuffer) {
							if (getAckMe(data) && getAck(data)) {
								System.out.println("Recebi um ack-ackme");
								sendWindowSize = getRcvwnd(data);
								testeSendBuffer.notify();
								synchronized (ackMeTimer) {
									if (ackMeTimerOn) {
										ackMeTimer.cancel();
										ackMeTimer = new Timer();
										ackMeTimerOn = false;
									}
								}
								System.out.println("acknum do ack-ackme: " + getackNum(data));
								if (getackNum(data) > sendBase) {
									System.out.println("sendBase = " + getackNum(data));
									sendBase = getackNum(data);
								}
							} else if (getackNum(data) > sendBase) {
								
								sendWindowSize = getRcvwnd(data);

								System.out.println("Recebi ack " + getackNum(data));
								if (getackNum(data) > packetSample) {
									timeAcked = System.currentTimeMillis();
									sampleRTT = timeAcked - timeSent;
									if (estimatedRTT == 0) {
										estimatedRTT = sampleRTT;
									}
									estimatedRTT = ((long) (estimatedRTT * 0.875)) + (sampleRTT >> 3);
									
									devRTT = ((long) (devRTT * 0.75)) + (Math.abs(sampleRTT - estimatedRTT) >> 2);
									
									timeOutRTT = estimatedRTT + (devRTT << 2);
								}
								for (int i = sendBase; i < getackNum(data); i++) {
									testeSendBufferEstado[(i)] = 3;
								}
								sendBase = getackNum(data);
								System.out.println("sendBase agora é " + sendBase);

								synchronized (msgSentTimer) {
									msgTimerOn = false; // pq chegou e tal
									msgSentTimer.cancel();
									msgSentTimer = new Timer();
									int peppa = transmitidoNaoReconhecido(nextSeqNum);
									if (peppa != -1) { // se tiver algum segmento não reconhecido, começar temporizador pra ele
										pktTimer = peppa;
										timeOutInterval = timeOutRTT;
										msgSentTimer.schedule(new MsgSentTimeOut(), timeOutInterval);
										msgTimerOn = true;
									}
								}
								testeSendBuffer.notify();
							} else if (apenasAck && getackNum(data) < nextSeqNum) {
								
								sendWindowSize = getRcvwnd(data);
								
								// ack duplicado. apenasAck é para não achar que dado chegando é ack duplicado.
								// < nextSeqNum é para não garantir que ele só está reenviando ack do último segmento
								// que enviei
								System.out.println("Recebi ack duplicado de " + getackNum(data));
								acksDuplicados[(getackNum(data))]++;
								if (acksDuplicados[(getackNum(data))] == 3) { // retransmissão rápida
									byte[] newData = testeSendBuffer[(getackNum(data))].getData();
									rcvwnd = (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
									System.out.println("rcvwnd = " + "(" + RcvBuffer + " - (" + lastPacketRcvd
											+ " - (" + rcvBase + " - 1)))");
									System.out.println("Minha rcvwnd: " + rcvwnd);
									// antes de retransmitir o pacote, atualiza o campo rcvwnd dele hehe.
									setRcvwnd(newData, rcvwnd);
									testeSendBuffer[(getackNum(data))] = new DatagramPacket(newData, testeSendBuffer[(getackNum(data))].getLength(),
											testeSendBuffer[(getackNum(data))].getAddress(), testeSendBuffer[(getackNum(data))].getPort());
									socket.send(testeSendBuffer[(getackNum(data))]); // envia logo segmento perdido
									System.out.println("Retransmiti rapidamente o pacote " + getackNum(data));
									acksDuplicados[(getackNum(data))] = 0;
									synchronized (msgSentTimer) {
										if (!msgTimerOn) {
											timeOutInterval = timeOutRTT;
											msgSentTimer.schedule(new MsgSentTimeOut(), timeOutInterval);
											msgTimerOn = true;
											pktTimer = getackNum(data);
										}
									}
								}
								testeSendBuffer.notify();
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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
		return ((data[8] & 0xFF) << 8) + ((data[9] & 0xFF)); // dessa forma não vai fazer 199 virar -57...
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
}
