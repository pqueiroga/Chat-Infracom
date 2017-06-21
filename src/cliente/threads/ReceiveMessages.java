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
		while (true) {
			try {				
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
