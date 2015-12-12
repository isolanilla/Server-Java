package lab15.isolanil.server;


import java.io.IOException;
import java.net.ServerSocket;

import lab15.isolanil.cache.*;


public class Server {
	
	private Thread ServerClient;
	private final int PORTCLIENT = 8181;
	private ServerSocket socketClient;
	
	private Thread ServerAdmin;

	private Cache cache;

	public Server(String path){
		this.cache = new Cache(path);
		try {
			socketClient = new ServerSocket(PORTCLIENT);
		} catch (IOException e) {
			System.err.println("RED EN USO");
			System.exit(0);
		}
		startServe();
		
	}
	
	private void startServe() {
				
		ServerClient = new Thread() {
			public void run() {
				new ServerClient(cache,socketClient);
			}
		};ServerClient.start();
		
		ServerAdmin = new Thread() {
			public void run() {
				new ServerAdmin(cache,socketClient);
			}
		};ServerAdmin.start();
		System.out.println("star server");
		
	}


	public static void main(String[] args) throws IOException {
		if(args.length == 1){
			new Server(args[0]);
		}else{
			System.err.println("Numero de argumentos erroneos");
			System.exit(1);
		}

	}
}
