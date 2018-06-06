package br.edu.ifg.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Classe utilizada como servidor para processamento de multi threads.
 * O mesmo fica no aguardo da conexão do cliente. Após a conexão, realiza 
 * a criação de uma thread para cada requisição.
 */

public class WebServerMultithreading {

	public static final int SERVER_PORT = 6789;

	public static void main(String args[]) {
		ServerSocket listenSocket = null;
		
		try {
			listenSocket = new ServerSocket(SERVER_PORT); // Instanciando o servidor
			
			// While utilizado para sempre ficar esperando uma requisição
			while (true) {
				System.out.println("Servidor esperando na porta " + SERVER_PORT + "...");
				
				// Aceitando a requisição e criando uma thread que irá processar a requisição e enviar a resposta.
				Socket clientSocket = listenSocket.accept();
				WebServerConnection c = new WebServerConnection(clientSocket);
			}
		} catch (IOException e) {
			System.err.println("IO: " + e.getMessage());
		} finally {
			if (listenSocket != null)
				try {
					listenSocket.close(); // Encerrando a conexão do servidor
				} catch (IOException e) {
					System.err.println("close:" + e.getMessage());
				}
		}
	}
}
