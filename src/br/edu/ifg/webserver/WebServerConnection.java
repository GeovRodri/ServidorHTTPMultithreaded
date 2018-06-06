package br.edu.ifg.webserver;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Classe utilizada para realiza��o de conex�o com servidor multi thread, 
 * realizando requisi��o de arquivos. Retornando o status da requisi��o, 
 * se o arquivo existe (200 ok), ou caso n�o exista, exibindo a informa�ao 
 * de que o mesmo n�o foi encontrado(404 not found).
 */

public class WebServerConnection extends Thread {

	private BufferedReader in;
	private OutputStream out;
	private Socket clientSocket;

	/**
	   * M�todo construtor.
	   * � utilizado para estabelecer comunica��o com cliente.
	   * @param clienteSocket : informa��es de conex�o do socket do cliente
	   * @throws IOException
	   * @author Raul Araujo, Geovany Santa Cruz, Raissa Pereira, Paulo Branco
	   * @since 1.0
	   * @version 1.0
	   */
	
	public WebServerConnection(Socket clientSocket) {
		System.out.println("Conexão com cliente em " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
		try {
			this.clientSocket = clientSocket;
			this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // cria��o do buffer utilizado para leitura.
			this.out = clientSocket.getOutputStream(); // cria��o do buffer utilizado para envio de mensagens.
			this.start(); // D� start na thread, chamando o m�todo "run()".
		} catch (IOException e) {
			System.err.println("Connection:" + e.getMessage()); //Caso aconte�a algum exce��o, a mensagem � exibida.
		}
	}

	/**
	* O metodo run(), ir� realizar a leitura do comando a ser executado
	* e caso seja GET, o mesmo ir� pegar o nome do arquivo solicitado, 
	* verificar se o mesmo existe, caso exista, exibir� o cont�udo do 
	* arquivo. Se o arquivo solicitado n�o existir, o mesmo exibir� o arquivo
	* notfound, exibindo a mensagem de que o arquivo n�o foi encontrado.
	*
	* @throws EOFException
	* @throws IOException
	*/
	public void run() {
		try {
			// Verificando se j� foi tudo carregado
			if (this.in.ready()) {
				String commands = this.in.readLine(); // Pegando o comando a ser executado.
				
				if (!commands.isEmpty()) {
					String header = "";
					byte[] byteFiles = null;
					String command[] = commands.split(" "); //comando recebido sendo separado por espa�o.
					
					if (command[0].equals("GET")) { //caso o comando seja igual a GET
						Path path = Paths.get("WebContent" + command[1]);
						File file = new File(path.toAbsolutePath().toString()); //Ir� no buscar no diret�rio pelo nome enviado o arquivo.
						
						if (file.exists()) { // Verificando se o arquivo existe
					        byteFiles = Files.readAllBytes(path); // tranformando o arquivo em um array de bytes
					        String fileType =  URLConnection.guessContentTypeFromName(file.getName()); // Capturando o nome do arquivo
					        
					        // Montando o header
					        header += "HTTP/1.0 200 GET OK\r\n"; // Colocando o status como 200 porque o arquivo foi encontrado.
							header += "Content-Type: " + fileType + "\r\n"; // Acrescenta o tipo do arquivo(exemplo: txt, hmtl) a mensagem.
							header += "Content-Length: " + byteFiles.length + "\r\n";
							header += "Connection: keep-alive\r\n";
							header += "\r\n";
						} else {
							
							Path pathFileNotFound = Paths.get("WebContent/notFound.html"); // Pegando path do arquivo notFound
							byteFiles = Files.readAllBytes(pathFileNotFound); // tranformando o arquivo em um array de bytes
							
							// Montando o header
							header += "HTTP/1.0 404 OK\r\n"; // Colocando o status 404, informando que o arquivo nao foi encontrado.
							header += "Content-Type: text/html\r\n"; // Acrescenta o tipo do arquivo(exemplo: txt, hmtl) a mensagem.
							header += "Content-Length: " + byteFiles.length + "\r\n";
							header += "Connection: keep-alive\r\n";
							header += "\r\n";
						}
					} else {
						Path pathFileNotFound = Paths.get("WebContent/metodo.html"); // Pegando path do arquivo notFound
						byteFiles = Files.readAllBytes(pathFileNotFound); // tranformando o arquivo em um array de bytes
						
						// Montando o header
						header += "HTTP/1.0 501 OK\r\n"; // Colocando o status 404, informando que o arquivo nao foi encontrado.
						header += "Content-Type: text/html\r\n"; // Acrescenta o tipo do arquivo(exemplo: txt, hmtl) a mensagem.
						header += "Content-Length: " + byteFiles.length + "\r\n";
						header += "Connection: keep-alive\r\n";
						header += "\r\n";
					}
					
					this.out.write(header.getBytes()); // Enviando o header
					this.out.write(byteFiles); // Enviando o body
					
		        	System.out.println("Close connection"); //Exibe a mensagem que a conex�o foi fechada.
				}
			}
		} catch (EOFException e) {
			System.err.println("EOF:" + e.getMessage());
		} catch (IOException e) {
			System.err.println("IO:" + e.getMessage());
		} finally {
			try {
				out.close(); // Fecha conex�o do buffer de escrita
		        in.close(); // Fecha conex�o do buffer de leitura
		        clientSocket.close(); // Fecha conex�o do socket do cliente
			} catch (IOException e) {
				System.err.println("close:" + e.getMessage());
			}
		}
	}
}
