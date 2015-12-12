package lab15.isolanil.cache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Cache {
	private   Map<String,Page> cache;
	private   String dir;
	private   String POLICY;
	private final   int SIZECACHE = 5;

	public Cache(String path){
		this.dir = path;
		cache = new HashMap<String, Page>();
		this.POLICY = "rnd"; //por defecto
	}

	/**
	 * Elimina la cache del todo
	 */
	public synchronized   void flush(){
		cache.clear();
	}


	/**
	 * Metodo que elimina de cache el fichero que me pasan
	 * @param path
	 * @return si ha podido borrar el fichero o no
	 */
	public synchronized   boolean del(String path){
		if(cache.remove(path) != null){
			return true;
		}

		return false;
	}


	/**
	 * Metodo que tendra en cuenta la politica de reemplazamiento
	 * @param policy
	 * @return 
	 */
	public synchronized String policy(String policy){
		if(policy.equals("fifo") || policy.equals("rnd") || policy.equals("lru")){
			String oldPolicy = POLICY;
			POLICY = policy; 
			return oldPolicy;
		}
		return null;
					
		
	}

	private void lru(Page page){
		Object[] cacheKeys = cache.keySet().toArray();
		Page remove = cache.get(cacheKeys[0]);
		for (int i = 1; i < cacheKeys.length; i++) {
			if(remove.use >= cache.get(cacheKeys[i]).use){
				remove = cache.get(cacheKeys[i]);
			}
		}
		System.out.println("remove : " + remove.name);
		cache.remove(remove.name);
		cache.put(page.name, page);
	}

	private void fifo(Page page){
		Object[] cacheKeys = cache.keySet().toArray();
		Page remove = cache.get(cacheKeys[0]);
		for (int i = 1; i < cacheKeys.length; i++) {
			if(remove.fechaInclusion >= cache.get(cacheKeys[i]).fechaInclusion){
				remove = cache.get(cacheKeys[i]);
			}
		}
		System.out.println("remove : " + remove.name);
		cache.remove(remove.name);
		cache.put(page.name, page);
	}

	private void rnd(Page page){
		Object[] cacheKeys = cache.keySet().toArray();
		Object key = cacheKeys[new Random().nextInt(cacheKeys.length)];
		System.out.println("remove : " + cache.get(key).name);
		cache.remove(key);
		cache.put(page.name, page);
	}


	/**
	 * @return contenga la cache
	 */
	public synchronized Page[] ls(){
		Page[] pages = new Page[cache.size()];
		int i = 0;
		for(Page value : cache.values()){
			pages[i] = value;
			i++;
		}
		return pages; 		
	}

	/**
	 * Añade la pagina a la cache
	 * atendiendo a la politica 
	 * @param page
	 */
	private synchronized  void addPage(String path, Page page){
		if(cache.size() < SIZECACHE){
			cache.put(path,page);
			return;
		}
		if(cache.size() == SIZECACHE){
			switch (POLICY) {
			case "lru":
				System.out.println("LRU");
				lru(page);
				break;

			case "fifo":
				System.out.println("fifo");
				fifo(page);
				break;

			case "rnd":
				System.out.println("RND");
				rnd(page);
				break;

			default:
				break;
			}
		}	
	}

	public synchronized String get(String peticion) throws FileNotFoundException, IOException {
		Page page = cache.get(peticion);
		if(page == null){
			page = new Page(peticion,dir);
			addPage(peticion, page);
		}else{
			page = ultimaVersion(page);
		}
		
		page.visited();
		return page.content;
	}

	private Page ultimaVersion(Page page) throws FileNotFoundException, IOException {
		long version =  new File(dir+page.name).lastModified();
		
		if(page.UltimaModificacion < version){
			page = new Page(page.name,dir);
			cache.put(page.name, page);
		}
		
		return page;
	}

}
