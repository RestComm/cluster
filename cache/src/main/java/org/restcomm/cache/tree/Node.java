package org.restcomm.cache.tree;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.infinispan.Cache;


public class Node {

	private static final Logger logger = Logger.getLogger(Node.class);
	
	private final static String DATA_SEPARATOR = "_/_";
	private final static String NODE = "node";
	
	private final Cache c;
	private final Fqn nodeFqn;
	
		
	public Node(Cache c, Fqn fqn){
		this.c = c;
		this.nodeFqn = fqn;
		
		
	}
	
	public boolean exists(){
		return this.c.get(nodeFqn.toString()) != null;		
	}
	
	public void remove(){		
		this.c.remove(nodeFqn.toString());
		for(Object name : this.c.keySet()){
			if(name instanceof String){
				if(((String)name).startsWith(nodeFqn.toString())){
					this.c.remove(name);
				}
			}
		}
		
	}
	
	public Object put(Object key, Object value){
		return this.c.put(nodeFqn.toString() + DATA_SEPARATOR + key, value);
	}
	
	public Object get(Object key){
		return this.c.get(nodeFqn.toString() + DATA_SEPARATOR + key);
	}
	
	public void create(){		
		this.c.put(nodeFqn.toString(), NODE);
		
	}




	public Fqn getNodeFqn() {
		return nodeFqn;
	}
	
	public Set<String> getChildNames(){
		Set<String> res = new HashSet<String>();
		for(Object name : this.c.keySet()){
			if(name instanceof String){
				if(((String)name).startsWith(nodeFqn.toString()) && !name.equals(nodeFqn.toString()) && c.get(name).equals(NODE)){					
					//Separator as well!					
					res.add(((String)name).substring(nodeFqn.toString().length()+1));
				}
			}
		}
		return res;
	}
	
}
