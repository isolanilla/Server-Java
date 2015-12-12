package lab15.isolanil.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Page {

	static volatile int lastUsed = 0;
	
	public String name;
	public long fechaInclusion;
	String content;
	public long UltimaModificacion;
	public int use;
	
	public Page(String name, String dir) throws FileNotFoundException,IOException{
		this.name = name;
		//date = d.getTime();
		this.use = newUse();
		File file = new File(dir+name);

		UltimaModificacion = file.lastModified();
		this.content = addContent(dir+name);
		System.out.println("contenido pagina: " + content);
		
		
	}
	
	public synchronized String addContent(String path) throws FileNotFoundException,IOException {
		System.out.println("actualizo contenido");
		byte[] buffer = new byte[1024];
		int n;
		String content = "";
		FileInputStream fs = new FileInputStream(path);
		
		
		while(fs.available() > 0){
			n = fs.read(buffer);
			content += new String(buffer, 0, n); //O el offset hasta n
		}
		fs.close();
		return content;
	}

	public void visited(){
		this.use = newUse();
	}
	
	protected static synchronized int newUse() {
		++lastUsed;
		return lastUsed;
	}
	
	
}

