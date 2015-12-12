package lab15.isolanil.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

import lab15.isolanil.cache.Page;



public class ClientAdmin {

	private int port;
	private String host;
	
	private Socket fd;
	
	private DataInputStream in;
	private DataOutputStream out;
	
	
	/**
	 * Constructor del cliente
	 * @param host nombre de la maquina
	 * @param port puerto al que nos atamos
	 */
	public ClientAdmin(String host, int port){
		this.port = port;
		this.host = host;
	}
	
	public void starClient() throws UnknownHostException, IOException{
			fd = new Socket(host,port);
			in = new DataInputStream(fd.getInputStream());
			out = new DataOutputStream(fd.getOutputStream());
	}
	
	private void flush() throws IOException{
		MessagesAdmin msg = new MessagesAdmin.Tflush();
		msg.sendTo(out);
		
		MessagesAdmin answ =  MessagesAdmin.rcv(in);
		System.out.println(answ.getString());
		
	}
	private void del(String path) throws IOException {
		MessagesAdmin msg = new MessagesAdmin.Tdel(path);
		msg.sendTo(out);
		
		MessagesAdmin answ =  MessagesAdmin.rcv(in);
		System.out.println(answ.getString());
		
	}
	
	private void ls() throws IOException {
		MessagesAdmin msg = new MessagesAdmin.Tls();
		msg.sendTo(out);
		
		MessagesAdmin answ =  MessagesAdmin.rcv(in); 
		Page[] pages = ((MessagesAdmin.Rls)answ).getPages();
		if(pages.length == 0 ){
			System.out.println("Cache vacía");
		}
		for(int i = 0; i < pages.length; i++){
			System.out.println(pages[i].name + " " + pages[i].size + " bytes " + new Date(pages[i].fechaInclusion/1000000));
		}
	}
	
	private void policy(String policy) throws IOException {
		MessagesAdmin msg = new MessagesAdmin.Tpolicy(policy);
		msg.sendTo(out);
		
		MessagesAdmin answ =  MessagesAdmin.rcv(in);
		System.out.println(answ.getString());
	}

	
	private void quit() throws IOException {
		MessagesAdmin msg = new MessagesAdmin.Tquit();
		msg.sendTo(out);
		
		MessagesAdmin answ =  MessagesAdmin.rcv(in);
		
		System.out.println(answ.getString());
	}

	
	private void redigirPeticion(String[] args) throws IOException {
		if(args[0].equals("flush") && args.length == 1){
			flush();
		}else if(args[0].equals("del") && args.length == 2){
			del(args[1]);
		}else if(args[0].equals("ls") && args.length == 1){
			ls();
		}else if(args[0].equals("policy") && args.length == 2){
			policy(args[1]);
		}else if(args[0].equals("quit") && args.length == 1){
			quit();
		}else{
			System.err.println("PETICION NO VALIDA ");
		}
		
	}
	
	private void close() throws IOException {
		if(fd != null)
			fd.close();
		if(in != null)
			in.close();
		if(out != null)
			out.close();
		
	}
	
	
	public static void main(String[] args) {
		
		if(args.length > 2 || args.length < 1 ){
			System.err.println("Numero de argumentos erroneos");
			System.exit(1); 
		}
		
		ClientAdmin clienteAdmin = new ClientAdmin("localhost", 9090);
		try {
			clienteAdmin.starClient();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Servidor inactivo");
			System.exit(1); 
		}

		try {
			clienteAdmin.redigirPeticion(args);
			clienteAdmin.close();
		} catch (IOException e) {
			//
		}
		
		
		
		
	}

	

	
}
