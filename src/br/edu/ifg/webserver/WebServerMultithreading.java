package br.edu.ifg.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classe utilizada como servidor para processamento de multi threads.
 * O mesmo fica no aguardo da conex�o do cliente. Ap�s a conex�o, realiza 
 * a cria��o de uma thread para cada requisi��o.
 */

public class WebServerMultithreading {

	public static final int SERVER_PORT = 6789;

	public static void main(String args[]) {
		ServerSocket listenSocket = null;
		
		try {
			listenSocket = new ServerSocket(SERVER_PORT); // Instanciando o servidor
			
			// While utilizado para sempre ficar esperando uma requisi��o
			while (true) {
				System.out.println("Servidor esperando na porta " + SERVER_PORT + "...");
				
				// Aceitando a requisi��o e criando uma thread que ir� processar a requisi��o e enviar a resposta.
				Socket clientSocket = listenSocket.accept();
				WebServerConnection c = new WebServerConnection(clientSocket);
			}
		} catch (IOException e) {
			System.err.println("IO: " + e.getMessage());
		} finally {
			if (listenSocket != null)
				try {
					listenSocket.close(); // Encerrando a conex�o do servidor
				} catch (IOException e) {
					System.err.println("close:" + e.getMessage());
				}
		}
	}
}
