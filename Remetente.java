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
	
	private short RcvBuffer = 200;
	
	private int remotePort;
	private InetAddress remoteInetAddress;
	private int ackNum;
	private int nextSeqNum; // nextSeqNum - 1 nos diz LastPacketSent
	private int sendBase; // sendBase - 1 nos diz LastPacketAcked
	private int sendWindowSize;
	private short rcvwnd;
	private DatagramPacket[] testeSendBuffer = new DatagramPacket[100];
	private byte[] testeSendBufferEstado = new byte[RcvBuffer]; // 1 n enviado, 2 enviado, 3 reconhecido
	private int[] acksDuplicados = new int[RcvBuffer];
	private DatagramPacket[] testeRcvBuffer = new DatagramPacket[RcvBuffer];
	private int[] testeRcvBufferEstado = new int[RcvBuffer]; // 1 recebido, 2 ack enviado, 3 dado pra camada superior
	
	/**
	 * rcvBase nos diz onde está o pacote válido para ser pego.
	 * rcvBase - 1 nos diz o último pacote lido pela aplicação
	 */
	private int rcvBase;
	private int lastPacketRcvd;
	private int rcvLastAcked;
	private DatagramSocket socket;
	private int pktTimer, ackTimer; // diz qual pacote/ack está associado ao timer
	private boolean close = false, msgTimerOn = false, ackTimerOn = false;
	private long timeOutInterval = 1000;
	private long timeOutRTT = 1000;
	private long timeSent, timeAcked, sampleRTT, estimatedRTT = 0, devRTT;
	private int packetSample;
	
	private Timer msgSentTimer = new Timer();
	private Timer delayedAckTimer = new Timer();
	
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
		this.rcvwnd = RcvBuffer;
		switch (this.ESTADO) {
		case "CLOSED":
			if (port == -1) {
				this.socket = new DatagramSocket();
			} else {
				this.socket = new DatagramSocket(port);
			}
			this.remotePort = remotePort;
			this.remoteInetAddress = InetAddress.getByName(remoteIP);
	//		byte[] data = new byte[13];
	//		cabecalha(data, this.nextSeqNum, this.ackNum, this.rcvwnd, (byte) 0, (byte) 1, (byte) 0);
			tRecebe = new Thread(new RecebeDados());
			tEnvia = new Thread(new EnviaDados());
			tEnvia.start();
			tRecebe.start();
			// ACTIVE OPEN
			send(null, 0, (byte) 0, (byte) 1, (byte) 0); 
			// SYN SENT
			this.ESTADO = "SYN SENT";
	//		DatagramPacket iniciador = new DatagramPacket(data, 13, InetAddress.getByName(remoteIP), remotePort);
	//		this.socket.send(iniciador);
	//		this.nextSeqNum++;
	//		this.socket.receive(iniciador);
			
	//		do {
			// recebe syn + ack
			System.out.println("Vou receber syn + ack");
				receive(data, 1024);
			System.out.println("Logo depois de receber syn + ack no construtor");
				
				// manda ack
//				send(null, 0, (byte) 1, (byte) 0, (byte) 0);
				// ESTABLISHED
//				this.ESTADO = "ESTABLISHED";
	//		} while (!(getAck(data) || getSyn(data)));
				
	//		byte[] synack = iniciador.getData();
	//		this.sendBase = getackNum(synack);
	//		this.remoteInetAddress = iniciador.getAddress();
	//		this.remotePort = iniciador.getPort();
	//		this.socket.connect(this.remoteInetAddress, this.remotePort);
	//		this.ackNum = getnextSeqNum(data) + 1;
	//		cabecalha(data, this.nextSeqNum, this.ackNum, this.rcvwnd, (byte) 1, (byte) 0, (byte) 0);
	//		iniciador = new DatagramPacket(data, 13, this.remoteInetAddress, this.remotePort);
	//		this.socket.send(iniciador);
	//		tRecebe = new Thread(new RecebeDados());
	//		tEnvia = new Thread(new EnviaDados());
	//		tEnvia.start();
	//		tRecebe.start();
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
			send(null, 0, (byte) 1, (byte) 1, (byte) 0); // manda syn + ack
			this.socket.connect(this.remoteInetAddress, this.remotePort);
//			receive(data, 1024); // espera receber ACK
//			this.ESTADO = "ESTABLISHED";
		}
	}
	
	public void send(byte[] data, int length, byte ack, byte syn, byte fin) throws IOException {
		System.out.println("Tentarei ganhar lock de testeSendBuffer no send");
		synchronized (this.testeSendBuffer) {
			System.out.println("Ganhei lock de testeSendBuffer no send");
//			for (int i = this.sendBase, j = 0; i < this.sendBase + length; i++, j++) {
//				this.sendBuffer[i] = data[j];
//			}
//			this.nextSeqNum += length;
//			this.sendBuffer.notify();
//			this.dgLength[p] = length;
//			p = p + 1;
			byte[] guardar = new byte[13 + length];
			rcvwnd =  (short) (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
			cabecalha(guardar, nextSeqNum, ackNum, rcvwnd, ack, syn, fin);
			for (int i = 13, j = 0; i < 13 + length && j < data.length; i++, j++) {
				guardar[i] = data[j];
			}
			testeSendBuffer[this.nextSeqNum] = new DatagramPacket(guardar, guardar.length,
					this.remoteInetAddress, this.remotePort);
			this.testeSendBufferEstado[this.nextSeqNum] = 1;
			this.nextSeqNum++;
			System.out.println("Coloquei o pacote " + (this.nextSeqNum - 1) + " no sendBuffer pra ser enviado");
			testeSendBuffer.notify();
			System.out.println("Notifiquei o envia dados do testeSendBuffer OK");
		}
	}
	
	public void send(byte[] data, int length) throws IOException {
		send(data, length, (byte) 1, (byte) 0, (byte) 0);
	}
	
	public int receive(byte[] data, int length) {
		int b = 0;
		System.out.println("Tentarei ganhar lock no testeRcvBuffer em receive");
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
			System.out.println("Vou tentar dar o pacote " + rcvBase + " para a aplicação");
			byte[] temp = testeRcvBuffer[rcvBase].getData();
			for (int i = 0, j = 13; i < length && j < testeRcvBuffer[rcvBase].getLength(); i++, j++) {
				data[i] =  temp[j];
				b++;
			}
			System.out.println("Mandei pra aplicação o pacote " + rcvBase);
//			testeRcvBufferEstado[rcvBase] = 3;
			rcvBase += 1;
		}
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
	
	public boolean podeEnviar() {
		for (int i = sendBase; i < nextSeqNum && i < testeSendBufferEstado.length; i++) {
			if (testeSendBufferEstado[i] == 1) {
				return true;
			}
		}
		return false;
	}
	
	public int transmitidoNaoReconhecido(int end) {
		for (int i = sendBase; i < nextSeqNum && i < end && i < testeSendBufferEstado.length; i++) {
			if (testeSendBufferEstado[i] == 2) {
				return i;
			}
		}
		return -1;
	}
	
	public int recebidoNaoReconhecido(int end) {
		for (int i = rcvLastAcked; i < end && i < testeRcvBufferEstado.length; i++) {
			if (testeRcvBufferEstado[i] == 1) {
				return i;
			}
		}
		return -1;
	}
	
	public int descobreProximoEsperado() {
		for (int i = ackNum; i < testeRcvBufferEstado.length; i++) {
			if (testeRcvBufferEstado[i] == 0) {
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
			boolean enviou = false;
			while (!close) {
				synchronized (testeSendBuffer) {
					while (!podeEnviar()) { //nextSeqNum == sendBase) {
						enviou = false;
						try {
							System.out.println("Estou esperando no testeSendBuffer pra enviar dados");
							testeSendBuffer.wait();
							System.out.println("Saí da espera no testeSendBuffer");
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					// se chegou aqui quer dizer que tem coisa no sendBuffer para enviar
					System.out.println("Saí da espera no testeSendBuffer de verdade e enviarei dados");
					try {
//						byte[] data = new byte[nextSeqNum - sendBase + 13];
//						cabecalha(data, nextSeqNum, ackNum,
//								rcvwnd, (byte) 1, (byte) 0, (byte) 0);
//						for (int i = sendBase, j = 13; i < nextSeqNum; i++, j++) {
//							data[j] = sendBuffer[i];
//						}
//						DatagramPacket dp = new DatagramPacket(data, data.length,
//								remoteInetAddress, remotePort);
//						System.out.println("vai lá");
						for (int i = sendBase; i < nextSeqNum; i++) {
							if (i - (sendBase - 1) > rcvwnd) break; // lastByteSent - lastByteAcked
							if (testeSendBufferEstado[i] == 1) {
//								for (int j = 0; j < testeSendBuffer[i].getData().length;j++) {
//									System.out.print(testeSendBuffer[i].getData()[j] + ", ");
//								}
								System.out.println("Enviei o pacote " + i);
								socket.send(testeSendBuffer[i]);
								enviou = true;
								if (sampleRTT != -1) {
									timeSent = System.currentTimeMillis();
									sampleRTT = -1;
									packetSample = i;
								}
								testeSendBufferEstado[i] = 2;
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
						synchronized (msgSentTimer) {
							if (!msgTimerOn && enviou) {
								pktTimer = sendBase;
								timeOutInterval = timeOutRTT;
								msgSentTimer.schedule(new MsgSentTimeOut(), timeOutInterval);
								msgTimerOn = true;
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
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
					byte[] newData = testeSendBuffer[pktTimer].getData();
					rcvwnd =  (short) (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
					setRcvwnd(newData, rcvwnd);
					testeSendBuffer[pktTimer] = new DatagramPacket(newData, testeSendBuffer[pktTimer].getLength(),
							testeSendBuffer[pktTimer].getAddress(), testeSendBuffer[pktTimer].getPort());
					socket.send(testeSendBuffer[pktTimer]);
					if (timeOutInterval < 10000) { // duplicação do tempo de expiração
						timeOutInterval = timeOutInterval << 1;
					}
					msgSentTimer.schedule(new MsgSentTimeOut(), timeOutInterval);
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
					byte[] ack = new byte[13];
					rcvwnd =  (short) (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
					cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0);
					DatagramPacket ACK = new DatagramPacket(ack, 13, remoteInetAddress, remotePort);
					// esperou 500 ms pela chegada de outro pacote na ordem, que deveria cancelar.
					// se não foi cancelado ou seja, entrou aqui, então envia o ACK mesmo.
					testeRcvBufferEstado[ackTimer] = 2;
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
					socket.receive(dp);
//					if (random.nextBoolean()) { // simula perda de pacotes
//						continue;
//					}
					
					if (random.nextDouble() < pDescartaPacote) { // simula perda de pacotes
						continue;
					}
					
					System.out.println("Recebi o pacote " + getSeqNum(dp.getData()) + " em RecebeDados\nESTADO == " + ESTADO);
					boolean apenasAck = false;
					if (getAck(data) && dp.getLength() == 13 && !getSyn(data) && !getFin(data)) {
						apenasAck = true;
					}
					switch (ESTADO) {
					case "SYN RECEIVED":
						if (getAck(data)) {
							System.out.println("Recebi ack");

							synchronized (msgSentTimer) {
								for (int i = sendBase; i < getackNum(data); i++) {
									testeSendBufferEstado[i] = 3;
								}
								msgSentTimer.cancel();
								msgTimerOn = false;
								msgSentTimer = new Timer();
								sendBase = getackNum(data);
								int peppa = transmitidoNaoReconhecido(nextSeqNum);
								if (peppa != -1) { // se tiver algum segmento não reconhecido, começar temporizador pra ele
									pktTimer = peppa;
//									timeOutInterval = timeOutRTT;
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
									testeSendBufferEstado[i] = 3;
								}
								msgSentTimer.cancel();
								msgSentTimer = new Timer();
								msgTimerOn = false;
								sendBase = getackNum(data);
								int peppa = transmitidoNaoReconhecido(nextSeqNum);
								if (peppa != -1) { // se tiver algum segmento não reconhecido, começar temporizador pra ele
									pktTimer = peppa;
//									timeOutInterval = timeOutRTT;
									msgSentTimer.schedule(new MsgSentTimeOut(), timeOutInterval);
									msgTimerOn = true;
								}
							}
							socket.connect(remoteInetAddress, remotePort);
							byte[] ack = new byte[13];
							ackNum++;
							sendWindowSize = getRcvwnd(dp.getData());
							rcvwnd =  (short) (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
							cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0);
							DatagramPacket ACK = new DatagramPacket(ack, 13, remoteInetAddress, remotePort);
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
								testeRcvBuffer[getSeqNum(data)] = dp;
								testeRcvBufferEstado[getSeqNum(data)] = 2;
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
								if (ackNum == getSeqNum(data)) {
//									// chegada de datagrama com número de sequência esperado
//									apenasAck = false;
////									testeRcvBuffer[rcvLength] = dp;
//									testeRcvBuffer[ackNum] = dp;
////									testeRcvBufferEstado[rcvLength] = 1;
//									testeRcvBufferEstado[ackNum] = 1;
////									rcvLength += 1;
//									rcvNext += 1;
//									testeRcvBuffer.notify();
									
									// guarda datagrama no lugar certo
									testeRcvBuffer[getSeqNum(data)] = dp;
									testeRcvBufferEstado[getSeqNum(data)] = 1;
									testeRcvBuffer.notify();
									
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

										byte[] ack = new byte[13];
										rcvwnd =  (short) (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
										cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0);
										DatagramPacket ACK = new DatagramPacket(ack, 13, remoteInetAddress, remotePort);
										// envio imediato de um ACK
										socket.send(ACK);
										System.out.println("Mandei o ack " + ackNum);
										for (int i = 0; i < ackNum; i++) { // ack cumulativo enviado, atualizar
											testeRcvBufferEstado[i] = 2;
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

										byte[] ack = new byte[13];
										rcvwnd =  (short) (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
										cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0);
										DatagramPacket ACK = new DatagramPacket(ack, 13, remoteInetAddress, remotePort);
										// envio imediato de um único ACK cumulativo, reconhecendo ambos os pacotes
//										if (random.nextBoolean()) { // simula perda de acks no caminho
//											socket.send(ACK);
//										}
										
										
										if (random.nextDouble() < pDescartaAck) { // simula perda de acks
											socket.send(ACK);
										}
										
										System.out.println("Mandei o ack " + ackNum);
										

										testeRcvBufferEstado[peppa] = 2; // ack enviado
										testeRcvBufferEstado[peppa + 1] = 2; // ack enviado
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
									System.out.println(ackNum + " < " +getSeqNum(data));
									// guarda datagrama no lugar certo
									testeRcvBuffer[getSeqNum(data)] = dp;
									testeRcvBufferEstado[getSeqNum(data)] = 1;
									testeRcvBuffer.notify();
									
									byte[] ack = new byte[13];
									rcvwnd =  (short) (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
									cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0);
									DatagramPacket ACK = new DatagramPacket(ack, 13, remoteInetAddress, remotePort);
									// envio imediato de um ACK duplicado, indicando número de sequência do pacote seguinte esperado
									socket.send(ACK);
									System.out.println("Mandei o ack " + ackNum);

								} else {
									// recebi pacote que já reconheci, repetir ack
									System.out.println("Recebi pacote que já reconheci, repetir ack");
									byte[] ack = new byte[13];
									rcvwnd =  (short) (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
									cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0);
									DatagramPacket ACK = new DatagramPacket(ack, 13, remoteInetAddress, remotePort);
									
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
							if (getackNum(data) > sendBase) {
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
									testeSendBufferEstado[i] = 3;
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
								// ack duplicado. apenasAck é para não achar que dado chegando é ack duplicado.
								// < nextSeqNum é para não garantir que ele só está reenviando ack do último segmento
								// que enviei
								System.out.println("Recebi ack duplicado de " + getackNum(data));
								acksDuplicados[getackNum(data)]++;
								if (acksDuplicados[getackNum(data)] == 3) { // retransmissão rápida
//									cabecalha(data, getackNum(data), ackNum,
//											rcvwnd, (byte) 1, (byte) 0, (byte) 0);
//									for (int i = sendBase, j = 13; i < nextSeqNum; i++, j++) {
//										data[j] = sendBuffer[i];
//									}
//									DatagramPacket dp = new DatagramPacket(data, data.length,
//											remoteInetAddress, remotePort);
									byte[] newData = testeSendBuffer[getackNum(data)].getData();
									rcvwnd =  (short) (RcvBuffer - (lastPacketRcvd - (rcvBase - 1)));
									setRcvwnd(newData, rcvwnd);
									testeSendBuffer[getackNum(data)] = new DatagramPacket(newData, testeSendBuffer[getackNum(data)].getLength(),
											testeSendBuffer[getackNum(data)].getAddress(), testeSendBuffer[getackNum(data)].getPort());
									socket.send(testeSendBuffer[getackNum(data)]); // envia logo segmento perdido
									System.out.println("Retransmiti rapidamente o pacote " + getackNum(data));
									acksDuplicados[getackNum(data)] = 0;
									synchronized (msgSentTimer) {
										if (!msgTimerOn) {
											timeOutInterval = timeOutRTT;
											msgSentTimer.schedule(new MsgSentTimeOut(), timeOutInterval);
											msgTimerOn = true;
											pktTimer = getackNum(data);
										}
									}
								}
							}
						}
					}
/*
					// descartar se não for da conexão
					// não precisa mais disso pois eu usei o connect
//					if (dp.getPort() == remotePort && dp.getAddress() == remoteInetAddress) {
						data = dp.getData();
						if (getSyn(data) && getAck(data)) {
							if (!connectionEstablished) {
								remotePort = dp.getPort();
								remoteInetAddress = dp.getAddress();
								socket.connect(remoteInetAddress, remotePort);
								connectionEstablished = true;
							}
						}
						boolean apenasAck = true;
						// GERA ACK
						if (!(getAck(data) || dp.getLength() == 13)) { // não se acka um ack
							synchronized (testeRcvBuffer) {
								if (ackNum == getSeqNum(data)) {
									// chegada de segmento com número de sequência esperado
									apenasAck = false;
//									testeRcvBuffer[rcvLength] = dp;
									testeRcvBuffer[ackNum] = dp;
//									testeRcvBufferEstado[rcvLength] = 1;
									testeRcvBufferEstado[ackNum] = 1;
//									rcvLength += 1;
									rcvNext += 1;
									testeRcvBuffer.notify();
									

									
									int peppa = recebidoNaoReconhecido(ackNum);
									
									// esse pacote pode ter sido um que preenche lacuna. preciso buscar nos estados
									// e colocar o próximo esperado (ackNum) no primeiro com estado 0 que eu encontrar.
									boolean preenche = false;
									int peppa2 = descobreProximoEsperado();
									if (peppa2 != -1 && ackNum != peppa2) {
										ackNum = peppa2;
										preenche = true;
									} else {
										System.out.println("ERRO NO DESCOBRE PROXIMO ESTADO, NÃO FAZ SENTIDO DAR -1");
									}
//									int peppa = recebidoNaoReconhecido(rcvLength);
//									ackNum = getSeqNum(data) + 1;
									if (preenche) {
										// chegada de um segmento que preenche, parcial ou completamente, a lacuna de dados recebidos
										if (ackTimerOn) {
											delayedAckTimer.cancel();
											ackTimerOn = false;
											delayedAckTimer = new Timer();
										}
										byte[] ack = new byte[13];
										cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0);
										DatagramPacket ACK = new DatagramPacket(ack, 13, remoteInetAddress, remotePort);
										// envio imediato de um ACK
										socket.send(ACK);
										for (int i = 0; i < ackNum; i++) { // ack cumulativo enviado, atualizar
											testeRcvBufferEstado[i] = 2;
										}
									} else if (peppa == -1) {
										// todos os dados até o número de seq esperado já tiveram seu ACK enviado
										delayedAckTimer.schedule(new DelayedAckTimeOut(), 500);
										ackTimerOn = true;
									} else {
										// Outro segmento na ordem esperando por transmissão de ACK
										if (ackTimerOn) {
											delayedAckTimer.cancel();
											ackTimerOn = false;
											delayedAckTimer = new Timer();
										}
										byte[] ack = new byte[13];
										cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0);
										DatagramPacket ACK = new DatagramPacket(ack, 13, remoteInetAddress, remotePort);
										// envio imediato de um único ACK cumulativo, reconhecendo ambos os pacotes
										socket.send(ACK);
										testeRcvBufferEstado[peppa] = 2; // ack enviado
										testeRcvBufferEstado[peppa + 1] = 2; // ack enviado
									}
								} else if (ackNum < getSeqNum(data)) {
									// chegada de um segmento fora de ordem com número de sequência mais alto do que o esperado
									// lacuna detectada
									if (ackTimerOn) {
										delayedAckTimer.cancel();
										ackTimerOn = false;
										delayedAckTimer = new Timer();
									}
									// guarda ele no lugar certo
									testeRcvBuffer[getSeqNum(data)] = dp;
									testeRcvBufferEstado[getSeqNum(data)] = 1;
									byte[] ack = new byte[13];
									cabecalha(ack, nextSeqNum, ackNum, rcvwnd, (byte) 1, (byte) 0, (byte) 0);
									DatagramPacket ACK = new DatagramPacket(ack, 13, remoteInetAddress, remotePort);
									// envio imediato de um ACK duplicado, indicando número de sequência do pacote seguinte esperado
									socket.send(ACK);
								}
							}
						}
						synchronized (testeSendBuffer) {
							if (getackNum(data) > sendBase) {
								for (int i = sendBase; i < getackNum(data); i++) {
									testeSendBufferEstado[i] = 3;
								}
								sendBase = getackNum(data);
								synchronized (msgSentTimer) {
									msgTimerOn = false; // pq chegou e tal
									msgSentTimer.cancel();
									msgSentTimer = new Timer();
									int peppa = transmitidoNaoReconhecido(nextSeqNum);
									if (peppa != -1) { // se tiver algum segmento não reconhecido, começar temporizador pra ele
										pktTimer = peppa;
										msgSentTimer.schedule(new MsgSentTimeOut(), timeOutInterval);
										msgTimerOn = true;
									}
								}
								testeSendBuffer.notify();
							} else if (apenasAck) { // ack duplicado. apenasAck é para não achar que dado chegando é ack duplicado.
								acksDuplicados[getackNum(data)]++;
								if (acksDuplicados[getackNum(data)] == 3) { // retransmissão rápida
//									cabecalha(data, getackNum(data), ackNum,
//											rcvwnd, (byte) 1, (byte) 0, (byte) 0);
//									for (int i = sendBase, j = 13; i < nextSeqNum; i++, j++) {
//										data[j] = sendBuffer[i];
//									}
//									DatagramPacket dp = new DatagramPacket(data, data.length,
//											remoteInetAddress, remotePort);
									socket.send(testeSendBuffer[getackNum(data)]); // envia logo segmento perdido
									acksDuplicados[getackNum(data)] = 0;
									if (!msgTimerOn) {
										msgSentTimer.schedule(new MsgSentTimeOut(), timeOutInterval);
										msgTimerOn = true;
										pktTimer = getackNum(data);
									}
								}
							}
						}
//					}*/
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
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
	
	private void setRcvwnd(byte[] data, short rcvwnd) {
		data[8] = (byte) (rcvwnd >>> 8);
		data[9] = (byte) (rcvwnd & 0x00ffL);
	}

	
	private int getackNum(byte[] data) {
		return (int) (data[4] << 24) + (int) (data[5] << 16) + (int) (data[6] << 8) + (int) data[7];
	}
	
	private int getSeqNum(byte[] data) {
		return (int) (data[0] << 24) + (int) (data[1] << 16) + (int) (data[2] << 8) + (int) data[3];
	}
	
	private short getRcvwnd(byte[] data) {
		return (short) ((short) (data[8] << 8) + (short) (data[9]));
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
}
