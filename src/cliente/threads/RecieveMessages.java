package cliente.threads;
import java.net.*;
import protocol.*;
import java.io.*;
import javax.swing.*;
public class RecieveMessages implements Runnable {
	//lembrar de mudar para nosso protocolo
	private ServerSocket rcv_skt;
	private JTextPane textPane;
	public RecieveMessages(ServerSocket rcv_skt, JTextPane textPane) {
		this.rcv_skt=rcv_skt;
		this.textPane=textPane;
	}
	public void run() {
		while(true){
			try{
				/*DatagramSocket rcvskt=new DatagramSocket(port);
				PipedInputStream dataReader=new PipedInputStream();
				byte[]b=new byte[8];
				DatagramPacket rcvpkt=new DatagramPacket(b, 8);
				PReceiver rcv=new PReceiver(dataReader, rcvskt, rcvpkt);
				rcv.run();
				textPane.setText(textPane.getText()+dataReader.available()+'\n');*/
				Socket socket=rcv_skt.accept();
				InputStreamReader in = new InputStreamReader(socket.getInputStream());
				BufferedReader buff=new BufferedReader(in);
				//ver se modifico p ele pegar possivelmente uma msg com varias linhas
				String msg = buff.readLine();
				textPane.setText(textPane.getText()+msg);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
}
