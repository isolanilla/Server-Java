package lab15.isolanil.cache;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Page {

	static volatile int lastUsed = 0;
	
	public String name;
	public long fechaInclusion;
	public String content;
	public long UltimaModificacion;
	public int use;
	public int size;
	
	public Page(String name, String dir) throws FileNotFoundException,IOException{
		this.name = name;
		this.use = newUse();
		File file = new File(dir+name);
		this.UltimaModificacion = file.lastModified();
		this.size = (int) file.length();
		this.fechaInclusion = System.currentTimeMillis()*1000000;
		this.content = addContent(dir+name);
		
	}
	
	public Page(String name,int size,long fI){
		this.name = name;
		this.size = size;
		this.fechaInclusion = fI;
	}
	
	public synchronized String addContent(String path) throws FileNotFoundException,IOException {
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

