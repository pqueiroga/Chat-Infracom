package server;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Classe que roda o servidor deste servico na porta "bem conhecida".
 * @author Pedro Queiroga <psq@cin.ufpe.br>
 *
 */
public class ServidorComeco implements Runnable {
	ArrayList<String> listaDeUsuarios;
	ServerSocket servidor;
	
	public ServidorComeco(ArrayList<String> listaDeUsuarios, ServerSocket wSocket) {
		this.listaDeUsuarios = listaDeUsuarios;
		this.servidor = wSocket;
	}
	
	public void run() {
		try {
//			File usuariosCadastrados = new File("testeDatabase");
//			if (!usuariosCadastrados.isFile()) {
//				FileWriter fw = new FileWriter(usuariosCadastrados);
//				fw.write(0 + "\n");
//				fw.close();
//			}
			while (true) {
				Socket connectionSocket = servidor.accept();
				(new Thread(new ServidorConta(
						connectionSocket, listaDeUsuarios))).start();
			}
		} catch (BindException e) {
			System.err.println("ERRO: Esta porta já está em uso.");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("ERRO: Erro desconhecido ao tentar iniciar servidor.");
			e.printStackTrace();
		} finally {
		}
	}
}
