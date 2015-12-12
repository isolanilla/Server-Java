package lab15.isolanil.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import lab15.isolanil.cache.Cache;
import lab15.isolanil.cache.Page;
import lab15.isolanil.client.MessagesAdmin;

public class ServerAdmin extends Thread {


	private final int PORTADMIN = 9090;
	private ServerSocket socketAdmin;
	private Cache cache;
	
	private ServerSocket socketClient;
	

	public ServerAdmin(Cache cache, ServerSocket socketClient){
		this.cache = cache;
		this.socketClient = socketClient;
		try {
			socketAdmin = new ServerSocket(PORTADMIN);
		} catch (IOException e) {
			System.err.println("RED EN USO!");
			
		}
		run();
	}

	public void run(){
		for(;;){
			try {
				serve();
			} catch (java.net.SocketException e){
				System.out.println("SERVIDOR ADMIN CERRADO");
				System.exit(0);
			} catch (IOException e) {
				System.err.println("SOCKET ADMIN:"+ e);
			}
		}
	}
	
	private void serveClientAdmin(DataInputStream in, DataOutputStream out,	Socket fd) throws IOException {
		MessagesAdmin mAnw = null;
		MessagesAdmin mRcv = null;
		try {
			mRcv = MessagesAdmin.rcv(in);
		

			switch(mRcv.type){
			case MessagesAdmin.TFLUSH:
				cache.flush();
				mAnw = new MessagesAdmin.Rflush("OK");
				mAnw.sendTo(out);
				break;
	
			case MessagesAdmin.TDEL:
				boolean del = cache.del(mRcv.getString());
				if (del){
					mAnw = new MessagesAdmin.Rdel("OK");
				}else{
					mAnw = new MessagesAdmin.Rdel("NOT FOUND");
				}
				mAnw.sendTo(out);
				break;
			case MessagesAdmin.TPOLICY:
				String oldPolicy = cache.policy(mRcv.getString());
				String response;
				if (oldPolicy != null){
					response = ("changed from " +  oldPolicy + " to " +  mRcv.getString());
				}else {
					response = "politica no valida";
				}
				
				mAnw = new MessagesAdmin.Rpolicy(response);
				mAnw.sendTo(out);
				break;
			case MessagesAdmin.TLS:
				Page[] pages = cache.ls();
				mAnw = new MessagesAdmin.Rls(pages);
				mAnw.sendTo(out);
				break;
			case MessagesAdmin.TQUIT:
				mAnw = new MessagesAdmin.Rquit("OK");
				mAnw.sendTo(out);
				socketAdmin.close();
				socketClient.close();
			}	
		} catch (EOFException e) {
			System.out.println("tipo mensaje no valido");
		}
		

	}


	private void serve() throws IOException {

		final Socket fd = socketAdmin.accept();
		System.out.println("###Nuevo cliente de control aceptado###");
		Thread srvClient = new Thread() {
			public void run() {

				DataInputStream in;
				DataOutputStream out;
				try {
					in = new DataInputStream(new BufferedInputStream(fd.getInputStream()));
					out =new DataOutputStream( new BufferedOutputStream(fd.getOutputStream()));
					serveClientAdmin(in, out,fd);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				}

			}


		};srvClient.start();

	}


}
