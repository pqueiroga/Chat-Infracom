package cliente.threads;
import java.net.*;
import protocol.*;
import utility.buffer.BufferMethods;

import java.io.*;
import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
public class ReceiveMessages implements Runnable {
	//lembrar de mudar para nosso protocolo
	private InputStream inFromFriend;
	private StyledDocument docMsg;
	private String friend;
	private Style styleFrom;
	private OutputStream msgStatusOutput;
	
	public ReceiveMessages(String friend, InputStream inFromFriend, StyledDocument docMsg, Style styleFrom,
			OutputStream msgStatusOutput) {
		this.inFromFriend = inFromFriend;
		this.docMsg = docMsg;
		this.friend = friend;
		this.styleFrom = styleFrom;
		this.msgStatusOutput = msgStatusOutput;
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
//				Socket socket=rcv_skt.accept();
//				InputStreamReader in = new InputStreamReader(socket.getInputStream());
//				BufferedReader buff=new BufferedReader(in);
//				//ver se modifico p ele pegar possivelmente uma msg com varias linhas
//				String msg = buff.readLine();
				
				String msg = BufferMethods.readChatString(inFromFriend);
				synchronized (docMsg) {
					docMsg.insertString(docMsg.getLength(), friend + ": ", styleFrom);
					docMsg.insertString(docMsg.getLength(), msg + '\n', null);
				}
				msgStatusOutput.write(1);
			} catch(Exception e){
				break;
			}
		}
	}
}
