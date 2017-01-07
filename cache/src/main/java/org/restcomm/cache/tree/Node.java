package org.restcomm.cache.tree;

import org.apache.log4j.Logger;
import org.infinispan.Cache;

import java.util.HashSet;
import java.util.Set;


public class Node {
	// TODO 1: Node vs Node<K,V>? Data/Structure? Fqn? Keys/Values?
	// TODO 2: constructor, create, exists, put, set, remove, getKeys, getValues
	// TODO 3: Child: add, has, get, remove
	// TODO 4: Children: get, remove

	private static final Logger logger = Logger.getLogger(Node.class);
	
	private final static String DATA_SEPARATOR = "_/_";
	private final static String NODE = "node";

	//1.2
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
		logger.debug("@@@@ remove: "+this.getInfo());

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

	public void create() {
		this.cache.put(nodeFqn.toString(), NODE);
		this.cache.put(nodeFqn.toString() + DATA_SEPARATOR + "Node", this);
	}

	//public Object put(Object key, Object value){
	//	return this.cache.put(nodeFqn.toString() + DATA_SEPARATOR + key, value);
	//}

	//public Object get(Object key){
	//	return this.cache.get(nodeFqn.toString() + DATA_SEPARATOR + key);
	//}

	//public Object remove(Object key) {
	//	return this.cache.remove(nodeFqn.toString() + DATA_SEPARATOR + key);
	//}

	public Fqn getNodeFqn() {
		return nodeFqn;
	}

	public Fqn getFqn() {
		return nodeFqn;
	}
	
	//1.2
	public Object put(Object key, Object value) {
		this.cache.put(nodeFqn.toString() + DATA_SEPARATOR + key.toString() + KEY, key);
		return this.cache.put(nodeFqn.toString() + DATA_SEPARATOR + key.toString() + VALUE, value);
	}

	//1.2
	public Object getKey(Object key) {
		return this.cache.get(nodeFqn.toString() + DATA_SEPARATOR + key.toString() + KEY);
	}

	public Object get(Object key) {
		return this.cache.get(nodeFqn.toString() + DATA_SEPARATOR + key.toString() + VALUE);
	}

	public Object remove(Object key) {
		this.cache.remove(nodeFqn.toString() + DATA_SEPARATOR + key.toString() + KEY);
		return this.cache.remove(nodeFqn.toString() + DATA_SEPARATOR + key.toString() + VALUE);
	}


	// Sets: ChildNames (old), ChildrenNames, ChildrenKeys, ChildrenValues

	public Set<String> getNames() {
		Set<String> res = new HashSet<String>();

		String stringFqn = nodeFqn.toString();
		logger.debug("@@@@ getNames stringFqn: "+stringFqn);

		String stringKey;
		for (Object key: this.cache.keySet()) {
			if (key instanceof String) {
				stringKey = (String) key;
				logger.debug("@@@@ stringKey: " + stringKey);

				if (stringKey.startsWith(stringFqn) &&
						!stringKey.equals(stringFqn)) {
					//Object value = this.cache.get(key);
					//logger.debug("@@@@ FOUND value: " + value);
					res.add(stringKey);
				}
			}
		}

		return res;
	}

	public Set<Object> getKeys() {
		Set<Object> res = new HashSet<Object>();

		String stringFqn = nodeFqn.toString();
		logger.debug("@@@@ getKeys stringFqn: "+stringFqn);

		String stringKey;
		for (Object key: this.cache.keySet()) {
			if (key instanceof String) {
				stringKey = (String) key;
				logger.trace("@@@@ getKeys stringKey: " + stringKey);

				if (stringKey.startsWith(stringFqn) &&
						!stringKey.equals(stringFqn) &&
						stringKey.contains(KEY)) {

					Object okey = this.cache.get(key);
					logger.debug("@@@@ getKeys FOUND okey: " + okey);
					res.add(okey);
				}
			}
		}

		return res;
	}

	public Set<Object> getValues() {
		Set<Object> res = new HashSet<Object>();

		String stringFqn = nodeFqn.toString();
		logger.debug("@@@@ getValues stringFqn: "+stringFqn);

		String stringKey;
		for (Object key: this.cache.keySet()) {
			if (key instanceof String) {
				stringKey = (String) key;
				logger.debug("@@@@ getValues stringKey: " + stringKey);

				if (stringKey.startsWith(stringFqn) &&
						!stringKey.equals(stringFqn) &&
						stringKey.contains(VALUE)) {

					Object ovalue = this.cache.get(key);
					logger.debug("@@@@ getValues FOUND ovalue: " + ovalue);
					res.add(ovalue);
				}
			}
		}

		return res;
	}

	public Set<String> getChildNames() {
		Set<String> res = new HashSet<String>();

		String stringFqn = nodeFqn.toString();
		logger.debug("@@@@ getChildNames stringFqn: "+stringFqn);

		String stringKey;
		for (Object key : this.cache.keySet()) {
			//logger.trace("@@@@ getChildNames key: " + key);
			if (key instanceof String) {
				stringKey = (String)key;

				//if (stringKey.startsWith(stringFqn) &&
				//		!key.equals(stringFqn) &&
				//		cache.get(key).equals(NODE)) {
				if (stringKey.startsWith(stringFqn + "/") &&
						!key.equals(stringFqn) &&
						cache.get(key).equals(NODE)) {

					logger.trace("@@@@ getChildNames stringKey: " + stringKey);
					//Separator as well!
					String name = stringKey.substring(stringFqn.length()+1);
					logger.debug("@@@@ getChildNames name: " + name);
					res.add(name);
				}
			} else {
				logger.warn("@@@@ getChildNames [IS NOT STRING] key: " + key +
						", class: " + key.getClass().getCanonicalName());
			}
		}
		return res;
	}

	//1.1 - String, 1.2 - Object
	public Set<Object> getChildKeys() {
		Set<Object> res = new HashSet<Object>();

		String stringFqn = nodeFqn.toString();
		logger.debug("@@@@ getChildKeys stringFqn: "+stringFqn);

		String stringKey;
		for (Object key : this.cache.keySet()) {
			if (key instanceof String) {
				stringKey = (String)key;

				Fqn fqnKey = Fqn.fromString(stringKey);

				// FIXME: if stringFqn is "/ac", then "/aci-names" includes too
				if (stringKey.startsWith(stringFqn + "/") &&
						!stringKey.equals(stringFqn)) {
					if (stringKey.contains(KEY)) {
						Object objectKey = this.cache.get(key);
						logger.debug("@@@@ getChildKeys cacheKey: " + key);

						logger.trace("@@@@ getChildKeys fqnKey: " + fqnKey);
						logger.trace("@@@@ getChildKeys fqnKey: " + fqnKey.getLastElement());
						logger.trace("@@@@ getChildKeys fqnKey: " + fqnKey.getLastElement().getClass().getCanonicalName());

						logger.debug("@@@@ getChildKeys objectKey: " + objectKey + ", " +
								objectKey.getClass().getCanonicalName());
						//logger.debug("@@@@ getChildKeys stringKey: " + stringKey + ", " +
						//		stringKey.getClass().getCanonicalName());
						res.add(objectKey);

					}
				}
			} else {
				logger.warn("@@@@ getChildKeys [IS NOT STRING] cacheKey: " + key +
						", class: " + key.getClass().getCanonicalName());
			}
		}
		return res;
	}

	public Set<Object> getChildValues() {
		Set<Object> res = new HashSet<Object>();

		String stringFqn = nodeFqn.toString();
		logger.debug("@@@@ getChildValues stringFqn: "+stringFqn);

		String stringKey;
		for (Object key : this.cache.keySet()) {
			if (key instanceof String){
				stringKey = (String)key;
				if (stringKey.startsWith(stringFqn + "/") &&
						!key.equals(stringFqn)) {
					if (stringKey.contains(VALUE)) {
						Object objectKey = this.cache.get(key);
						logger.debug("@@@@ getChildValues cacheKey: " + key);
						logger.debug("@@@@ getChildValues objectKey: " + objectKey + ", " +
								objectKey.getClass().getCanonicalName());
						res.add(objectKey);
					}
				}
			} else {
				logger.warn("@@@@ getChildValues [IS NOT STRING] cacheKey: " + key +
						", class: " + key.getClass().getCanonicalName());
			}
		}
		return res;
	}


	// Child: add, has, get, remove

	public Node addChild(Fqn childFqn) {
		logger.debug("@@@@ ADD CHILD: "+childFqn+"["+childFqn.size()+"] FOR nodeFqn: "+this.nodeFqn);
		if (childFqn.size() > 0) {
			for (int i = 0; i < childFqn.size(); i++) {
				logger.trace("@@@@ childFqn: ["+i+"]: "+childFqn.get(i)+
						", class: "+childFqn.get(i).getClass().getCanonicalName());
			}
		}
		logger.debug("@@@@ addChild: "+this.getInfo());

		Fqn absoluteChildFqn = Fqn.fromRelativeFqn(this.nodeFqn, childFqn);
		logger.debug("@@@@ absoluteChildFqn: "+absoluteChildFqn);

		Node node = new Node(this.cache, absoluteChildFqn);
		logger.trace("@@@@ node: "+node);

		node.create();
		this.children.add(node);

		logger.debug("@@@@ node: "+node);
		logger.debug("@@@@ children: "+children);
		return node;
	}

	//public boolean hasChild(String childName) {
	//	return false;
	//}

	public boolean hasChild(Object childObject) {
		logger.debug("@@@@ HAS CHILD: "+childObject+
				" CLASS: "+childObject.getClass().getCanonicalName()+
				" FOR nodeFqn: "+this.nodeFqn);
		//logger.debug("@@@@ this.children: "+this.children);
		logger.debug("@@@@ hasChild: "+this.getInfo());

		if (childObject instanceof Fqn) {
			logger.debug("@@@@ childObject if Fqn");
			childObject = ((Fqn) childObject).getLastElementAsString();
			logger.debug("@@@@ update childObject: "+childObject);
		}

		for (Node child : this.children) {
			logger.debug("@@@@ TEST CHILD: "+child);
			logger.debug("@@@@ childFqn.size(): "+child.getNodeFqn().size());

			// last?!
			if (child.getNodeFqn().size() > 0) {
				for (int i = 0; i < child.getNodeFqn().size(); i++) {
					logger.trace("@@@@ childFqn: ["+i+"]: "+child.getNodeFqn().get(i)+
							", class: "+child.getNodeFqn().get(i).getClass().getCanonicalName());
				}
			}

			Object last = child.getFqn().getLastElement();
			logger.debug("@@@@ last: "+last);
			if (last != null) {
				logger.debug("@@@@ last: " + last.getClass().getCanonicalName());
				if (last.equals(childObject)) {
					logger.debug("@@@@ childObject was found");
					return true;
				}
			}
		}

		logger.debug("@@@@ childObject did not find");
		return false;
	}

	public Node getChild(Object childObject) {
		logger.debug("@@@@ GET CHILD: "+childObject+" FOR nodeFqn: "+this.nodeFqn);
		//logger.debug("@@@@ this.children: "+this.children);
		logger.debug("@@@@ getChild: "+this.getInfo());

		if (childObject instanceof Fqn) {
			logger.debug("@@@@ childObject if Fqn");
			childObject = ((Fqn) childObject).getLastElementAsString();
			logger.debug("@@@@ update childObject: "+childObject);
		}

		if (hasChild(childObject)) {
			Fqn childFqn = Fqn.fromRelativeElements(this.nodeFqn, childObject);
			logger.debug("@@@@ childFqn: "+childFqn);

			// TODO: Refactoring to more quickly
			for (Node child : this.children) {
				logger.debug("@@@@ TEST CHILD: "+child);
				logger.debug("@@@@ childFqn.size(): "+child.getNodeFqn().size());

				// last?!
				/*
				if (child.getNodeFqn().size() > 0) {
					for (int i = 0; i < child.getNodeFqn().size(); i++) {
						logger.trace("@@@@ childFqn: ["+i+"]: "+child.getNodeFqn().get(i)+
								", class: "+child.getNodeFqn().get(i).getClass().getCanonicalName());
					}
				}
				*/

				Object last = child.getFqn().getLastElement();
				logger.debug("@@@@ last: "+last);
				if (last != null) {
					logger.debug("@@@@ last: " + last.getClass().getCanonicalName());
					if (last.equals(childObject)) {
						logger.debug("@@@@ childObject was found");
						return child;
					}
				}
			}

		}

		return null;
	}

	//public boolean removeChild(String childName) {
	//	return false;
	//}

	public boolean removeChild(Object childObject) {
		logger.debug("@@@@ REMOVE CHILD: "+childObject+" FOR nodeFqn: "+this.nodeFqn);
		//logger.debug("@@@@ this.children: "+this.children);
		logger.debug("@@@@ removeChild: "+this.getInfo());

		if (childObject instanceof Fqn) {
			logger.debug("@@@@ childObject if Fqn");
			childObject = ((Fqn) childObject).getLastElementAsString();
			logger.debug("@@@@ update childObject: "+childObject);
		}

		if (hasChild(childObject)) {
			for (Node child : this.children) {
				logger.debug("@@@@ TEST CHILD: "+child);
				logger.debug("@@@@ childFqn.size(): "+child.getNodeFqn().size());

				// last?!
				/*
				if (child.getNodeFqn().size() > 0) {
					for (int i = 0; i < child.getNodeFqn().size(); i++) {
						logger.trace("@@@@ childFqn: ["+i+"]: "+child.getNodeFqn().get(i)+
								", class: "+child.getNodeFqn().get(i).getClass().getCanonicalName());
					}
				}
				*/

				Object last = child.getFqn().getLastElement();
				logger.debug("@@@@ last: "+last);
				if (last != null) {
					logger.debug("@@@@ last: " + last.getClass().getCanonicalName());
					if (last.equals(childObject)) {
						logger.debug("@@@@ childObject was found");
						this.children.remove(child);
						return true;
					}
				}
			}
		}

		return false;
	}

	// Structure & Children

	//public Node getParent() {
	//	return null;
	//}

	public Set<Node> getChildren() {
		//logger.debug("@@@@ getChildren nodeFqn: "+this.nodeFqn);
		//logger.debug("@@@@ getChildren this.children: "+this.children);
		logger.debug("@@@@ getChildren: "+this.getInfo());

		Set<Node> res = this.children;
		for (Node child : res) {
			logger.debug("@@@@ getChildren child: "+child.toString());
			logger.debug("@@@@ getChildren childFqn.size(): "+child.getNodeFqn().size());
			if (child.getNodeFqn().size() > 0) {
				for (int i = 0; i < child.getNodeFqn().size(); i++) {
					logger.debug("@@@@ getChildren childFqn: ["+i+"]: "+child.getNodeFqn().get(i));
					logger.debug("@@@@ getChildren childFqn: ["+i+"]: "+child.getNodeFqn().get(i).getClass().getCanonicalName());
				}
			}
		}

		return res;
	}

	public Set<Object> getChildrenNames() {
		Set<Object> res = new HashSet<Object>();

		String stringFqn = nodeFqn.toString();
		logger.debug("@@@@ getChildrenNames stringFqn: "+stringFqn);

		if (nodeFqn.size() > 0) {
			for (int i = 0; i < nodeFqn.size(); i++) {
				logger.debug("@@@@ getChildrenNames childFqn: ["+i+"]: "+nodeFqn.get(i));
				logger.debug("@@@@ getChildrenNames childFqn: ["+i+"]: "+nodeFqn.get(i).getClass().getCanonicalName());
			}
		}

		return res;
	}

	public void removeChildren() {
		logger.debug("@@@@ removeChildren: "+this.getInfo());
		for (Node child : this.children) {
			logger.debug("@@@@ removeChildren child: "+child.toString());
			child.removeChildren();
			child.remove();
		}
	}

	public String getInfo() {
		return "[object="+this.toString()+", fqn="+this.nodeFqn+", children="+this.children.toString()+"]";
	}

}
