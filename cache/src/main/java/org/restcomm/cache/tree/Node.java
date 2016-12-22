package org.restcomm.cache.tree;

import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.infinispan.Cache;


public class Node {
	// TODO 1: Node vs Node<K,V>? Data/Structure? Fqn? Keys/Values?
	// TODO 2: constructor, create, exists, put, set, remove, getKeys, getValues
	// TODO 3: Child: add, has, get, remove
	// TODO 4: Children: get, remove

	private static final Logger logger = Logger.getLogger(Node.class);
	
	private final static String DATA_SEPARATOR = "_/_";
	private final static String NODE = "node";
	private final static String KEY = "_KEYOBJ";
	private final static String VALUE = "_VALUEOBJ";
	
	private final Cache cache;
	private final Fqn nodeFqn;

	// FIXME
	private final Set<Node> children;

	public Node(Cache c, Fqn fqn){
		this.cache = c;
		this.nodeFqn = fqn;
		this.children = new HashSet<Node>();
	}
	
	public boolean exists() {
		return this.cache.get(nodeFqn.toString()) != null;
	}
	
	public void remove() {
		//
		String stringFqn = nodeFqn.toString();
		// remove NODE
		this.cache.remove(stringFqn);
		// find and remove all DATA (KEY, VALUE)
		String stringKey;
		for (Object key : this.cache.keySet()) {
			// keys always are Strings
			if (key instanceof String) {
				stringKey = (String)key;
				if (stringKey.startsWith(stringFqn)){
					this.cache.remove(key);
				}
			}
		}

		// TODO: check children
		
	}

	public void create(){
		this.cache.put(nodeFqn.toString(), NODE);
	}

	//public Object put(Object key, Object value){
	//	return this.cache.put(nodeFqn.toString() + DATA_SEPARATOR + key.toString(), value);
	//}

	public Object put(Object key, Object value){
		this.cache.put(nodeFqn.toString() + DATA_SEPARATOR + key.toString() + KEY, key);
		return this.cache.put(nodeFqn.toString() + DATA_SEPARATOR + key.toString() + VALUE, value);
	}

	//public Object get(Object key){
	//	return this.cache.get(nodeFqn.toString() + DATA_SEPARATOR + key.toString());
	//}

	public Object getKey(Object key){
		return this.cache.get(nodeFqn.toString() + DATA_SEPARATOR + key.toString() + KEY);
	}

	public Object get(Object key){
		return this.cache.get(nodeFqn.toString() + DATA_SEPARATOR + key.toString());
	}

	//public Object remove(Object key) {
	//	return this.cache.remove(nodeFqn.toString() + DATA_SEPARATOR + key.toString());
	//}

	public Object remove(Object key) {
		this.cache.remove(nodeFqn.toString() + DATA_SEPARATOR + key.toString() + KEY);
		return this.cache.remove(nodeFqn.toString() + DATA_SEPARATOR + key.toString());
	}

	public Fqn getNodeFqn() {
		return nodeFqn;
	}

	public Fqn getFqn() {
		return nodeFqn;
	}


	// Sets: ChildNames (old), ChildrenNames, ChildrenKeys, ChildrenValues

	public Set<Object> getKeys() {
		logger.debug("**** nodeFqn: "+this.nodeFqn);
		logger.debug("**** getKeys: "+this.cache.keySet());
		logger.debug("**** getChildrenValues: "+this.getChildrenValues());
		return this.getChildrenKeys();
	}

	public Set<String> getChildNames() {
		Set<String> res = new HashSet<String>();
		for (Object key : this.cache.keySet()) {
			if (key instanceof String){
				if (((String)key).startsWith(nodeFqn.toString()) &&
						!key.equals(nodeFqn.toString()) &&
						cache.get(key).equals(NODE)) {
					//Separator as well!
					String name = ((String)key).substring(nodeFqn.toString().length()+1);
					res.add(name);
				}
			}
		}
		return res;
	}

	public Set<Object> getChildrenNames() {
		Set<Object> res = new HashSet<Object>();
		for (Object key : this.cache.keySet()) {
			if (key instanceof String){
				if (((String)key).startsWith(nodeFqn.toString()) &&
						!key.equals(nodeFqn.toString()) &&
						cache.get(key).equals(NODE)) {
					//Separator as well!
					String name = ((String)key).substring(nodeFqn.toString().length()+1);
					res.add(name);
				}
			}
		}
		return res;
	}

	public Set<Object> getChildrenKeys() {
		Set<Object> res = new HashSet<Object>();
		String stringFqn = nodeFqn.toString();
		logger.debug("**** getChildrenKeys stringFqn: "+stringFqn);
		String stringKey;
		for (Object key : this.cache.keySet()) {
			if (key instanceof String) {
				stringKey = (String)key;
				if (stringKey.startsWith(stringFqn) &&
						!key.equals(stringFqn)) {
					if (stringKey.contains(KEY)) {
						Object objectKey = this.cache.get(key);
						logger.debug("**** getChildrenKeys cacheKey: " + key);
						logger.debug("**** getChildrenKeys objectKey: " + objectKey + ", " +
								objectKey.getClass().getCanonicalName());
						res.add(objectKey);
					}
				}
			}
		}
		return res;
	}

	public Set<Object> getChildrenValues() {
		Set<Object> res = new HashSet<Object>();
		String stringFqn = nodeFqn.toString();
		logger.debug("**** getChildrenKeys stringFqn: "+stringFqn);
		String stringKey;
		for (Object key : this.cache.keySet()) {
			if (key instanceof String){
				stringKey = (String)key;
				if (stringKey.startsWith(stringFqn) &&
						!key.equals(stringFqn)) {
					if (stringKey.contains(VALUE)) {
						Object objectKey = this.cache.get(key);
						logger.debug("**** getChildrenKeys cacheKey: " + key);
						logger.debug("**** getChildrenKeys objectKey: " + objectKey + ", " +
								objectKey.getClass().getCanonicalName());
						res.add(objectKey);
					}
				}
			}
		}
		return res;
	}


	// Child: add, has, get, remove

	public Node addChild(Fqn childFqn) {
		logger.debug("**** ADD CHILD: "+childFqn+"["+childFqn.size()+"] FOR nodeFqn: "+this.nodeFqn);
		if (childFqn.size() > 0) {
			for (int i = 0; i < childFqn.size(); i++) {
				logger.debug("**** childFqn: ["+i+"]: "+childFqn.get(i)+
						", class: "+childFqn.get(i).getClass().getCanonicalName());
			}
		}

		Fqn absoluteChildFqn = Fqn.fromRelativeFqn(this.nodeFqn, childFqn);
		logger.debug("**** absoluteChildFqn: "+absoluteChildFqn);

		Node node = new Node(this.cache, absoluteChildFqn);
		logger.debug("**** node: "+node);

		node.create();
		this.children.add(node);

		logger.debug("**** node: "+node);
		return node;
	}

	//public boolean hasChild(String childName) {
	//	return false;
	//}

	public boolean hasChild(Object childObject) {

		logger.debug("**** nodeFqn: "+this.nodeFqn);
		logger.debug("**** hasChild: "+childObject);
		//logger.debug("**** hasChild: "+childObject.getClass().getCanonicalName());
		//logger.debug("**** keySet: "+this.cache.keySet());
		logger.debug("**** this.children: "+this.children);

		for (Node child : this.children) {
			logger.debug("**** HAS CHILD: "+child);
			logger.debug("**** childFqn.size(): "+child.getNodeFqn().size());

			Object last = child.getFqn().getLastElement();
			logger.debug("**** last: "+last);
			logger.debug("**** last: "+last.getClass().getCanonicalName());

			if (last.equals(childObject)) {
				logger.debug("**** childObject was found");
				return true;
			}
		}

		/*
		for (Object key : this.cache.keySet()) {
			//logger.debug("key: "+key);
			if (key.equals(childObject)) {
				logger.debug("**** key "+key+" is "+childObject);
				return true;
			}
		}
		*/

		logger.debug("**** childObject did not find");
		return false;
	}

	public Node getChild(Object childName) {
		logger.debug("**** nodeFqn: "+this.nodeFqn);
		logger.debug("**** getChild: "+childName);
		if (hasChild(childName)) {

			Fqn childFqn = Fqn.fromRelativeElements(this.nodeFqn, childName);
			logger.debug("**** childFqn: "+childFqn);
			Node node = new Node(this.cache, childFqn);
			logger.debug("**** node: "+node);
			return node;

		} else
			return null;
	}

	//public boolean removeChild(String childName) {
	//	return false;
	//}

	public boolean removeChild(Object childObject) {
		return false;
	}


	// Children

	public Set<Node> getChildren() {
		logger.debug("**** nodeFqn: "+this.nodeFqn);
		Set<Node> res = this.children;

		for (Node child : res) {
			logger.debug("**** childFqn.size(): "+child.getNodeFqn().size());
			if (child.getNodeFqn().size() > 0) {
				for (int i = 0; i < child.getNodeFqn().size(); i++) {
					logger.debug("**** childFqn: ["+i+"]: "+child.getNodeFqn().get(i));
					logger.debug("**** childFqn: ["+i+"]: "+child.getNodeFqn().get(i).getClass().getCanonicalName());
				}
			}
		}

		/*
		for (Object key : this.cache.keySet()){
			//logger.debug("**** ALL: "+key);
			if (key instanceof String){
				if (((String)key).startsWith(nodeFqn.toString()) &&
						!key.equals(nodeFqn.toString())) {
					logger.debug("**** OBJECT key: "+key);
					Object obj = this.cache.get(key);
					logger.debug("**** OBJECT: "+obj);
					logger.debug("**** OBJECT: "+obj.getClass().getCanonicalName());

					Node node = new Node(this.cache, Fqn.fromString((String)key));
					Object last = node.getNodeFqn().getLastElement();
					logger.debug("**** last: "+last);

					logger.debug("**** childFqn.size(): "+node.getNodeFqn().size());
					if (node.getNodeFqn().size() > 0) {
						for (int i = 0; i < node.getNodeFqn().size(); i++) {
							logger.debug("**** childFqn: ["+i+"]: "+node.getNodeFqn().get(i));
							logger.debug("**** childFqn: ["+i+"]: "+node.getNodeFqn().get(i).getClass().getCanonicalName());
						}
					}

					res.add(node);
				}
			}
		}
		*/
		return res;
	}

	public void removeChildren() {
	}


	// Structure?

	public Node getParent() {
		return null;
	}









}
