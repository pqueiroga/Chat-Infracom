package cliente.threads;
import java.net.*;
import java.io.*;
import protocol.*;
public class SendText implements Runnable {
	private int port;
	private String Address;
	private String msg;
	public SendText(String Address, int port){
		this.port=port;
		this.Address=Address;
	}
	public void send(String msg){
		this.msg=msg;
		run();
	}
	public void run() {
		try{
/*		InetAddress Address=InetAddress.getByName(this.Address);
		DatagramSocket sendSocket=new DatagramSocket(port, Address);
		DatagramPacket pkt_to_send=new DatagramPacket(msg.getBytes(), msg.getBytes().length);
		PSender sender=new PSender(sendSocket, pkt_to_send);
		sender.run();	*/
		Socket socket = new Socket(Address, port);
		DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		out.write(msg.getBytes());
		socket.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
