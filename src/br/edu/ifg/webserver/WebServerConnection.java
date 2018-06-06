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
 * Classe utilizada para realização de conexão com servidor multi thread, 
 * realizando requisição de arquivos. Retornando o status da requisição, 
 * se o arquivo existe (200 ok), ou caso não exista, exibindo a informaçao 
 * de que o mesmo não foi encontrado(404 not found).
 */

public class WebServerConnection extends Thread {

	private BufferedReader in;
	private OutputStream out;
	private Socket clientSocket;

	/**
	   * Método construtor.
	   * É utilizado para estabelecer comunicação com cliente.
	   * @param clienteSocket : informações de conexão do socket do cliente
	   * @throws IOException
	   * @author Raul Araujo, Geovany Santa Cruz, Raissa Pereira, Paulo Branco
	   * @since 1.0
	   * @version 1.0
	   */
	
	public WebServerConnection(Socket clientSocket) {
		System.out.println("ConexÃ£o com cliente em " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
		try {
			this.clientSocket = clientSocket;
			this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // criação do buffer utilizado para leitura.
			this.out = clientSocket.getOutputStream(); // criação do buffer utilizado para envio de mensagens.
			this.start(); // Dá start na thread, chamando o método "run()".
		} catch (IOException e) {
			System.err.println("Connection:" + e.getMessage()); //Caso aconteça algum exceção, a mensagem é exibida.
		}
	}

	/**
	* O metodo run(), irá realizar a leitura do comando a ser executado
	* e caso seja GET, o mesmo irá pegar o nome do arquivo solicitado, 
	* verificar se o mesmo existe, caso exista, exibirá o contéudo do 
	* arquivo. Se o arquivo solicitado não existir, o mesmo exibirá o arquivo
	* notfound, exibindo a mensagem de que o arquivo não foi encontrado.
	*
	* @throws EOFException
	* @throws IOException
	*/
	public void run() {
		try {
			// Verificando se já foi tudo carregado
			if (this.in.ready()) {
				String commands = this.in.readLine(); // Pegando o comando a ser executado.
				
				if (!commands.isEmpty()) {
					String header = "";
					byte[] byteFiles = null;
					String command[] = commands.split(" "); //comando recebido sendo separado por espaço.
					
					if (command[0].equals("GET")) { //caso o comando seja igual a GET
						Path path = Paths.get("WebContent" + command[1]);
						File file = new File(path.toAbsolutePath().toString()); //Irá no buscar no diretório pelo nome enviado o arquivo.
						
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
					
		        	System.out.println("Close connection"); //Exibe a mensagem que a conexão foi fechada.
				}
			}
		} catch (EOFException e) {
			System.err.println("EOF:" + e.getMessage());
		} catch (IOException e) {
			System.err.println("IO:" + e.getMessage());
		} finally {
			try {
				out.close(); // Fecha conexão do buffer de escrita
		        in.close(); // Fecha conexão do buffer de leitura
		        clientSocket.close(); // Fecha conexão do socket do cliente
			} catch (IOException e) {
				System.err.println("close:" + e.getMessage());
			}
		}
	}
}
