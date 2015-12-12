package lab15.isolanil.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

import lab15.isolanil.cache.Page;


/**
 * typees messages:
 * Tflsuh : Tflush[1]
 * Rflush: Rflush[1] string[n]
 * 
 * Tdel: Tdel[1] ruta[n]
 * Rdel: Rdel[1] string [n]
 * 
 * Tls: Tls[1]
 * Rls: Rls[1] nElementos[4] string[n] size[n] fecha[8]
 * 
 * Tpolicy: tpolicy[1] politica[n]
 * Rpolicy: Rpolicy[1] string[n]
 * 
 * Tquit: Tquit[1]
 * Rquit: Rquit[1] string[n]
 * 
 **/

public class MessagesAdmin {

	/**
	 * Tipo de mensaje que ira en cada mensaje para identificarlos
	 */
	public static final byte TFLUSH	 = 65;
	public static final byte RFLUSH = 66;
	public static final byte TDEL = 67;
	public static final byte RDEL = 68;
	public static final byte TLS = 69;
	public static final byte RLS = 70;
	public static final byte TPOLICY = 71;
	public static final byte RPOLICY = 72;
	public static final byte TQUIT = 73;
	public static final byte RQUIT = 74;

	public final byte type;

	public MessagesAdmin(byte type) {
		this.type = type;
	}

	public void sendTo(DataOutputStream out) throws IOException{
		out.writeByte(type);
	}

	public String getString() {
		return null;
	}

	public  static synchronized MessagesAdmin rcv (DataInputStream in) throws IOException {
		byte type = 0;

		try {
			type = in.readByte();
		}catch(EOFException e){
			throw e;
		}
		switch(type){
		case TFLUSH: return new Tflush(in);
		case RFLUSH: return new Rflush(in);
		case TDEL: return new Tdel(in);
		case RDEL:return new Rdel(in);
		case TLS: return new Tls(in);
		case RLS: return new Rls(in);
		case TPOLICY: return new Tpolicy(in);
		case RPOLICY:return new Rpolicy(in);
		case TQUIT: return new Tquit(in);
		case RQUIT:return new Rquit(in);
		}
		
		return null;
	}

	static class Tflush extends MessagesAdmin {

		public Tflush() throws IOException{
			super(TFLUSH);

		}

		public Tflush(DataInputStream in) throws IOException{
			super(TFLUSH);
		}

		public void sendTo(DataOutputStream out) throws IOException{
			synchronized (out) {
				super.sendTo(out);
			}	
		}



	}

	public static class Rflush extends MessagesAdmin {
		public String msgRflush;

		public Rflush(String msgRflush) throws IOException{
			super(RFLUSH);
			this.msgRflush = msgRflush;

		}

		public Rflush(DataInputStream in) throws IOException{
			super(RFLUSH);
			msgRflush = in.readUTF();
		}

		public void sendTo(DataOutputStream out) throws IOException{
			synchronized (out) {
				super.sendTo(out);
				out.writeUTF(msgRflush);
				out.flush();
			}	
		}
		public String getString(){
			return msgRflush;
		}


	}

	static class Tdel extends MessagesAdmin{
		private String path;

		public Tdel(String path) {
			super(TDEL);
			this.path = path;
		}

		public Tdel (DataInputStream in) throws IOException{
			super(TDEL);
			path = in.readUTF();

		}

		public void sendTo(DataOutputStream out) throws IOException{
			synchronized (out) {
				super.sendTo(out);
				out.writeUTF(path);
				out.flush();
			}
		}
		public String getString(){
			return path;
		}




	}

	public static class Rdel extends MessagesAdmin{

		private String msgRdel;

		public Rdel(String msgRdel) throws IOException{
			super(RDEL);
			this.msgRdel = msgRdel;

		}

		public Rdel(DataInputStream in) throws IOException{
			super(RDEL);
			msgRdel = in.readUTF();
		}

		public void sendTo(DataOutputStream out) throws IOException{
			synchronized (out) {
				super.sendTo(out);
				out.writeUTF(msgRdel);
				out.flush();
			}	
		}
		public String getString(){
			return msgRdel;
		}

	}

	static class Tls extends MessagesAdmin{
		public Tls() throws IOException{
			super(TLS);

		}

		public Tls(DataInputStream in) throws IOException{
			super(TLS);
		}

		public void sendTo(DataOutputStream out) throws IOException{
			synchronized (out) {
				super.sendTo(out);
			}	
		}

	}

	public static class Rls extends MessagesAdmin{
		private Page[] pages;
		byte[] bytesLong = new  byte[8];
		byte[] bytesInt = new byte[4];
		
		public Rls(Page[] pages) {
			super(RLS);
			this.pages = pages;
		}

		public Rls(DataInputStream in) throws IOException{
			super(RLS);
			int length = in.readInt();
			pages = new Page[length];
			for(int i = 0; i < length; i++){
				byte[] bytesInt = new  byte[4];
				byte[] bytesLong = new  byte[8];
				String name = in.readUTF();
				in.read(bytesInt, 0, 4);
				in.read(bytesLong, 0, 8);
				pages[i] = new Page(name,toInt(bytesInt),toLong(bytesLong));
			}
		}


		public void sendTo(DataOutputStream out) throws   IOException{
			synchronized (out) {
				byte[] bytesInt = new  byte[4];
				byte[] byteslong = new  byte[8];
				super.sendTo(out);
				out.writeInt(pages.length);
				for(int i = 0; i < pages.length; i++){
					out.writeUTF(pages[i].name);
				
					bytesInt = toLittleEndian(pages[i].size);
					out.write(bytesInt,0,bytesInt.length);
					
					byteslong = toLittleEndian(pages[i].fechaInclusion);
					out.write(byteslong,0,byteslong.length);
				}

				out.flush();
			}
		}
		
		private int toInt(byte[] bytes){
			
			return (( bytes[3]& 0xFF  ) << 24) | (( bytes[2]& 0xFF  ) << 16) |
		            (( bytes[1]& 0xFF  ) << 8)| (( bytes[0]& 0xFF));
		}
		
		private long toLong(byte[] bytes){
			long value = 0;
			for (int i = 0; i < bytes.length; i++)
			{
			   value += ((long) bytes[i] & 0xffL) << (8 * i);
			}
			return value;
			
		}
		private byte[] toLittleEndian(long f) {
			bytesLong[7] = (byte)((f >> 56) & 0xffL);
			bytesLong[6] = (byte)((f >> 48) & 0xffL);
			bytesLong[5] = (byte)((f >> 40) & 0xffL);
			bytesLong[4] = (byte)((f >> 32) & 0xffL);
			bytesLong[3] = (byte)((f >> 24) & 0xffL);
			bytesLong[2] = (byte)((f >> 16) & 0xffL);
			bytesLong[1] = (byte)((f >> 8) & 0xffL);
			bytesLong[0] = (byte)((f) & 0xffL);
					
			return bytesLong;
		}

		private byte[] toLittleEndian(int size) {
			
			bytesInt[3] = (byte)((size >> 24) & 0xFF);
			bytesInt[2] = (byte)((size >> 16) & 0xFF);
			bytesInt[1] = (byte)((size >> 8) & 0xFF);
			bytesInt[0] = (byte)((size) & 0xFF);
			
			return bytesInt;
		}

		public Page[] getPages(){
			return pages;
		}


	}

	public static class Tpolicy extends MessagesAdmin{
		private String typePolicy;
		public Tpolicy(String typePolicy) throws IOException{
			super(TPOLICY);
			this.typePolicy = typePolicy;


		}

		public Tpolicy(DataInputStream in) throws IOException{
			super(TPOLICY);
			typePolicy = in.readUTF();
		}

		public void sendTo(DataOutputStream out) throws IOException{
			synchronized (out) {
				super.sendTo(out);
				out.writeUTF(typePolicy);
				out.flush();
			}	
		}

		public String getString(){
			return typePolicy;
		}
	}

	public static class Rpolicy extends MessagesAdmin{
		private String msgRpolicy;

		public Rpolicy(String msgRpolicy) throws IOException{
			super(RPOLICY);
			this.msgRpolicy = msgRpolicy;

		}

		public Rpolicy(DataInputStream in) throws IOException{
			super(RPOLICY);
			msgRpolicy = in.readUTF();
		}

		public void sendTo(DataOutputStream out) throws IOException{
			synchronized (out) {
				super.sendTo(out);
				out.writeUTF(msgRpolicy);
				out.flush();
			}	
		}
		public String getString(){
			return msgRpolicy;
		}

	}

	static class Tquit extends MessagesAdmin {

		public Tquit() throws IOException{
			super(TQUIT);

		}

		public Tquit(DataInputStream in) throws IOException{
			super(TQUIT);
		}

		public void sendTo(DataOutputStream out) throws IOException{
			synchronized (out) {
				super.sendTo(out);
			}	
		}


	}

	public static class Rquit extends MessagesAdmin {
		private String msgRquit;

		public Rquit(String msgRquit) throws IOException{
			super(RQUIT);
			this.msgRquit = msgRquit;

		}

		public Rquit(DataInputStream in) throws IOException{
			super(RFLUSH);
			msgRquit = in.readUTF();
		}

		public void sendTo(DataOutputStream out) throws IOException{
			synchronized (out) {
				super.sendTo(out);
				out.writeUTF(msgRquit);
				out.flush();
			}	
		}

		public String getString(){
			return msgRquit;
		}

	}
}

