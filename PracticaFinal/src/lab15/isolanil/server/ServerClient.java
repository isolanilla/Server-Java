package lab15.isolanil.server;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import lab15.isolanil.cache.Cache;





public class ServerClient extends Thread{

	private ServerSocket socketClient;
	
	private Cache cache;


	public ServerClient(Cache cache,ServerSocket socketClient){
		this.cache = cache;
		this.socketClient = socketClient;
		run();
	}

	private void serveClient(BufferedReader in, PrintWriter out,Socket fd) throws IOException {
		String peticion = in.readLine();

		peticion = peticion.substring(peticion.indexOf("/")).split(" ")[0];
		System.out.println("peticion " + peticion);
		String respuesta;
		try {
			respuesta = cache.get(peticion);
			out.println("HTTP/1.1 200 OK");
			out.println("Content-Type: text/html; charset=utf-8\n");
		} catch (FileNotFoundException e) {
			respuesta= "NOT FOUND";
			out.println("HTTP/1.1 404 Not Found");
			out.println("Content-Type: text/plain; charset=utf-8\n");
		}
		
		out.println(respuesta);
		if(out != null)
			out.close();
		if(in != null)
			in.close();
		if(fd != null)
			fd.close();
	}

	public void run(){
		for(;;){
			try {
				serve();
			} catch (java.net.SocketException e){
				System.out.println("SERVIDOR CLIENTES CERRADO");
				System.exit(0);
			} catch (IOException e) {
				System.err.println("SOCKET Client: "+ e);
			}
		}
	}

	private void serve() throws IOException {

		final Socket fd = socketClient.accept();
		System.out.println("nueva peticion http");
		Thread srvClient = new Thread() {
			public void run() {
				BufferedReader in;
				PrintWriter out;
				try {
					in =  new BufferedReader(new InputStreamReader(fd.getInputStream())); 
					out = new PrintWriter(new OutputStreamWriter(fd.getOutputStream()));
					serveClient(in, out,fd);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};srvClient.start();

	}


}
