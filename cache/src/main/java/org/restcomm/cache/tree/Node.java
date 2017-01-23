package org.restcomm.cache.tree;

import org.apache.log4j.Logger;
import org.infinispan.Cache;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Node {

	private static final Logger logger = Logger.getLogger(Node.class);
	
	private final static String DATA_SEPARATOR = "_/_";
	private final static String NODE = "node";
	private final static String KEY = "_key";
	private final static String VALUE = "_value";
	
	private final Cache cache;
	private final Fqn nodeFqn;
	private final Set<Node> children;

	public Node(Cache cache, Fqn nodeFqn) {
		this.cache = cache;
		this.nodeFqn = nodeFqn;
		this.children = new HashSet<Node>();
	}

	public Fqn getNodeFqn() {
		return nodeFqn;
	}

	public boolean exists() {
		return this.cache.get(nodeFqn.toString()) != null;
	}
	
	public void remove() {
		logger.trace("@@@@ remove: "+this.getInfo());

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
		logger.trace("@@@@ remove: "+this.getChildNames());
	}

	public void create() {
		this.cache.put(nodeFqn.toString(), NODE);
		this.cache.put(nodeFqn.toString() + DATA_SEPARATOR + "Node", this);
	}

	//public Object put(Object key, Object value){
	//	return this.cache.put(nodeFqn.toString() + DATA_SEPARATOR + key, value);
	//}
	//
	//public Object get(Object key){
	//	return this.cache.get(nodeFqn.toString() + DATA_SEPARATOR + key);
	//}
	//
	//public Object remove(Object key) {
	//	return this.cache.remove(nodeFqn.toString() + DATA_SEPARATOR + key);
	//}

	public Object put(Object key, Object value) {
		if (key == null) {
			return null;
		}

		if (value == null) {
			if (this.get(key) != null) {
				this.remove(key);
			}
			return null;
		}

		this.cache.put(nodeFqn.toString() + DATA_SEPARATOR + key.toString() + KEY, key);
		return this.cache.put(nodeFqn.toString() + DATA_SEPARATOR + key.toString() + VALUE, value);
	}

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

//	public Set<String> getNames() {
//		Set<String> res = new HashSet<String>();
//
//		String stringFqn = nodeFqn.toString();
//		logger.debug("@@@@ getNames stringFqn: "+stringFqn);
//
//		String stringKey;
//		for (Object key: this.cache.keySet()) {
//			if (key instanceof String) {
//				stringKey = (String) key;
//				logger.debug("@@@@ stringKey: " + stringKey);
//
//				if (stringKey.startsWith(stringFqn) &&
//						!stringKey.equals(stringFqn)) {
//					//Object value = this.cache.get(key);
//					//logger.debug("@@@@ FOUND value: " + value);
//					res.add(stringKey);
//				}
//			}
//		}
//
//		return res;
//	}

	public Set<Object> getKeys() {
		Set<Object> result = new HashSet<Object>();

		String stringFqn = nodeFqn.toString();
		logger.trace("@@@@ getKeys stringFqn: "+stringFqn);

		String stringKey;
		for (Object key: this.cache.keySet()) {
			if (key instanceof String) {
				stringKey = (String) key;
				logger.trace("@@@@ getKeys stringKey: " + stringKey);

				if (stringKey.startsWith(stringFqn) &&
						!stringKey.equals(stringFqn) &&
						stringKey.contains(KEY)) {

					Object okey = this.cache.get(key);
					logger.trace("@@@@ getKeys FOUND okey: " + okey);
					result.add(okey);
				}
			}
		}

		logger.debug("@@@@ getKeys result: "+result);
		return result;
	}

//	public Set<Object> getValues() {
//		Set<Object> res = new HashSet<Object>();
//
//		String stringFqn = nodeFqn.toString();
//		logger.debug("@@@@ getValues stringFqn: "+stringFqn);
//
//		String stringKey;
//		for (Object key: this.cache.keySet()) {
//			if (key instanceof String) {
//				stringKey = (String) key;
//				logger.debug("@@@@ getValues stringKey: " + stringKey);
//
//				if (stringKey.startsWith(stringFqn) &&
//						!stringKey.equals(stringFqn) &&
//						stringKey.contains(VALUE)) {
//
//					Object ovalue = this.cache.get(key);
//					logger.debug("@@@@ getValues FOUND ovalue: " + ovalue);
//					res.add(ovalue);
//				}
//			}
//		}
//
//		return res;
//	}

	public Set<String> getChildNames() {
		Set<String> result = new HashSet<String>();

		String stringFqn = nodeFqn.toString();
		logger.trace("@@@@ getChildNames stringFqn: "+stringFqn);

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
					logger.trace("@@@@ getChildNames name: " + name);
					result.add(name);
				}
			} else {
				logger.warn("@@@@ getChildNames [IS NOT STRING] key: " + key +
						", class: " + key.getClass().getCanonicalName());
			}
		}

		logger.debug("@@@@ getChildNames result: "+result);
		return result;
	}

	public Set<Object> getChildrenNames() {
		Set<Object> result = null;

		String stringFqn = nodeFqn.toString();
		logger.trace("@@@@ getChildrenNames stringFqn: "+stringFqn);

		String stringKey;
		for (Object key : this.cache.keySet()) {
			if (key instanceof String) {
				stringKey = (String)key;

				if (stringKey.startsWith(stringFqn + "/") &&
						!key.equals(stringFqn) &&
						cache.get(key).equals(NODE)) {

					if ((Fqn.fromString(stringKey).size() - nodeFqn.size() == 1)) {
						Object checkNode = this.cache.get(key + "_/_" + "Node");
						logger.trace("@@@@ getChildrenNames checkNode: " + checkNode);

						if (checkNode != null) {

							Fqn checkNodeFqn = ((Node) checkNode).getNodeFqn();

							if (checkNodeFqn.size() > 0) {
								for (int i = 0; i < checkNodeFqn.size(); i++) {
									logger.trace("@@@@ getChildrenNames childFqn: [" + i + "]: " + checkNodeFqn.get(i));
									logger.trace("@@@@ getChildrenNames childFqn: [" + i + "]: " + checkNodeFqn.get(i).getClass().getCanonicalName());
								}
							}

							Object last = checkNodeFqn.getLastElement();
							logger.trace("@@@@ getChildrenNames achElement: " + last);

							if (result == null) {
								result = new HashSet<Object>();
							}
							result.add(last);
						}
					}
				}
			} else {
				logger.warn("@@@@ getChildrenNames [IS NOT STRING] key: " + key +
						", class: " + key.getClass().getCanonicalName());
			}
		}

		logger.debug("@@@@ getChildrenNames result: "+result);
		return result != null ? result : Collections.emptySet();
	}

	//1.1 - String, 1.2 - Object
//	public Set<Object> getChildKeys() {
//		Set<Object> res = new HashSet<Object>();
//
//		String stringFqn = nodeFqn.toString();
//		logger.debug("@@@@ getChildKeys stringFqn: "+stringFqn);
//
//		String stringKey;
//		for (Object key : this.cache.keySet()) {
//			if (key instanceof String) {
//				stringKey = (String)key;
//
//				Fqn fqnKey = Fqn.fromString(stringKey);
//
//				// FIXME: if stringFqn is "/ac", then "/aci-names" includes too
//				if (stringKey.startsWith(stringFqn + "/") &&
//						!stringKey.equals(stringFqn)) {
//					if (stringKey.contains(KEY)) {
//						Object objectKey = this.cache.get(key);
//						if (objectKey != null) {
//							logger.debug("@@@@ getChildKeys cacheKey: " + key);
//
//							logger.trace("@@@@ getChildKeys fqnKey: " + fqnKey);
//							logger.trace("@@@@ getChildKeys fqnKey: " + fqnKey.getLastElement());
//							logger.trace("@@@@ getChildKeys fqnKey: " + fqnKey.getLastElement().getClass().getCanonicalName());
//
//							logger.debug("@@@@ getChildKeys objectKey: " + objectKey + ", " +
//									objectKey.getClass().getCanonicalName());
//							//logger.debug("@@@@ getChildKeys stringKey: " + stringKey + ", " +
//							//		stringKey.getClass().getCanonicalName());
//							res.add(objectKey);
//						}
//					}
//				}
//			} else {
//				logger.warn("@@@@ getChildKeys [IS NOT STRING] cacheKey: " + key +
//						", class: " + key.getClass().getCanonicalName());
//			}
//		}
//		return res;
//	}

//	public Set<Object> getChildValues() {
//		Set<Object> res = new HashSet<Object>();
//
//		String stringFqn = nodeFqn.toString();
//		logger.debug("@@@@ getChildValues stringFqn: "+stringFqn);
//
//		String stringKey;
//		for (Object key : this.cache.keySet()) {
//			if (key instanceof String){
//				stringKey = (String)key;
//				if (stringKey.startsWith(stringFqn + "/") &&
//						!key.equals(stringFqn)) {
//					if (stringKey.contains(VALUE)) {
//						Object objectKey = this.cache.get(key);
//						logger.debug("@@@@ getChildValues cacheKey: " + key);
//						logger.debug("@@@@ getChildValues objectKey: " + objectKey + ", " +
//								objectKey.getClass().getCanonicalName());
//						res.add(objectKey);
//					}
//				}
//			} else {
//				logger.warn("@@@@ getChildValues [IS NOT STRING] cacheKey: " + key +
//						", class: " + key.getClass().getCanonicalName());
//			}
//		}
//		return res;
//	}


	// Child: add, has, get, remove

	public Node addChild(Fqn childFqn) {
		logger.trace("@@@@ ADD CHILD: "+childFqn+"["+childFqn.size()+"] FOR nodeFqn: "+this.nodeFqn);
		logger.trace("@@@@ addChild: "+this.getInfo());

		if (childFqn.size() > 0) {
			for (int i = 0; i < childFqn.size(); i++) {
				logger.trace("@@@@ childFqn: ["+i+"]: "+childFqn.get(i)+
						", class: "+childFqn.get(i).getClass().getCanonicalName());
			}
		}

		Fqn absoluteChildFqn = Fqn.fromRelativeFqn(this.nodeFqn, childFqn);
		logger.trace("@@@@ absoluteChildFqn: "+absoluteChildFqn);

		Node node = new Node(this.cache, absoluteChildFqn);
		node.create();
		this.children.add(node);

		logger.debug("@@@@ addChild: "+this.getInfo());
		return node;
	}

	//public boolean hasChild(String childName) {
	//	return false;
	//}

	public boolean hasChild(Object childObject) {
		logger.trace("@@@@ HAS CHILD: "+childObject+
				" CLASS: "+childObject.getClass().getCanonicalName()+
				" FOR nodeFqn: "+this.nodeFqn);
		logger.trace("@@@@ hasChild: "+this.getInfo());

		if (childObject instanceof Fqn) {
			logger.trace("@@@@ childObject if Fqn");
			childObject = ((Fqn) childObject).getLastElementAsString();
			logger.trace("@@@@ update childObject: "+childObject);
		}

		for (Node child : this.children) {
			logger.trace("@@@@ TEST CHILD: "+child);
			logger.trace("@@@@ childFqn.size(): "+child.getNodeFqn().size());

			// last?!
			if (child.getNodeFqn().size() > 0) {
				for (int i = 0; i < child.getNodeFqn().size(); i++) {
					logger.trace("@@@@ childFqn: ["+i+"]: "+child.getNodeFqn().get(i)+
							", class: "+child.getNodeFqn().get(i).getClass().getCanonicalName());
				}
			}

			Object last = child.getNodeFqn().getLastElement();
			logger.trace("@@@@ last: "+last);
			if (last != null) {
				logger.trace("@@@@ last: " + last.getClass().getCanonicalName());
				if (last.equals(childObject)) {
					logger.trace("@@@@ childObject was found");
					return true;
				}
			}
		}

		logger.trace("@@@@ childObject did not find");
		return false;
	}

	public Node getChild(Object childObject) {
		logger.trace("@@@@ GET CHILD: "+childObject+" FOR nodeFqn: "+this.nodeFqn);
		logger.trace("@@@@ getChild: "+this.getInfo());

		if (childObject instanceof Fqn) {
			logger.trace("@@@@ childObject if Fqn");
			childObject = ((Fqn) childObject).getLastElementAsString();
			logger.trace("@@@@ update childObject: "+childObject);
		}

		//// CHILDREN
		if (hasChild(childObject)) {
			Fqn childFqn = Fqn.fromRelativeElements(this.nodeFqn, childObject);
			logger.trace("@@@@ childFqn: "+childFqn);

			// TODO: Refactoring to more quickly

			for (Node child : this.children) {
				logger.trace("@@@@ TEST CHILD: "+child);
				logger.trace("@@@@ childFqn.size(): "+child.getNodeFqn().size());

				Object last = child.getNodeFqn().getLastElement();
				logger.trace("@@@@ last: "+last);
				if (last != null) {
					logger.trace("@@@@ last: " + last.getClass().getCanonicalName());
					if (last.equals(childObject)) {
						logger.trace("@@@@ childObject was found");
						return child;
					}
				}
			}
		}

		//// getChildrenNames
		Set<Object> result = this.getChildrenNames();
		logger.trace("@@@@ childObject did not find, but we have second chance: "+result);
		for (Object object : result) {
			if (object.equals(childObject)) {
				logger.trace("@@@@ childObject we have second chance: " + object);

				String searchKey = this.nodeFqn.toString() + "/" + object.toString();
				logger.trace("@@@@ getChild startsWith: " + searchKey);

				Object objectNode = this.cache.get(searchKey + "_/_Node");
				logger.trace("@@@@ getChild objectNode: " + objectNode);
				return (objectNode != null)
						? ((objectNode instanceof Node) ? (Node) objectNode : null)
						: null;
			}
		}

		////
		String searchKey = this.nodeFqn.toString() + "/" + childObject.toString();
		for (Object key: this.cache.keySet()) {
			if (key instanceof String) {
				//logger.trace("@@@@ getChild key: " + key);
				if (((String)key).startsWith(searchKey)) {
					logger.trace("@@@@ childObject did not find, but we have third chance.");
					Object checkNode = this.cache.get(searchKey + DATA_SEPARATOR + "Node");
					if (checkNode == null) {
						return this.addChild(Fqn.fromString((String)childObject));
					}
					return (Node)checkNode;
				}
			}
		}

		logger.trace("@@@@ childObject did not find");
		return null;
	}

	//public boolean removeChild(String childName) {
	//	return false;
	//}

	public boolean removeChild(Object childObject) {
		logger.trace("@@@@ REMOVE CHILD: "+childObject+" FOR nodeFqn: "+this.nodeFqn);
		//logger.debug("@@@@ this.children: "+this.children);
		logger.trace("@@@@ removeChild: "+this.getInfo());

		if (childObject instanceof Fqn) {
			logger.trace("@@@@ childObject if Fqn");
			childObject = ((Fqn) childObject).getLastElementAsString();
			logger.trace("@@@@ update childObject: "+childObject);
		}

		if (hasChild(childObject)) {
			for (Node child : this.children) {
				logger.trace("@@@@ TEST CHILD: "+child);
				logger.trace("@@@@ childFqn.size(): "+child.getNodeFqn().size());

				// last?!
				/*
				if (child.getNodeFqn().size() > 0) {
					for (int i = 0; i < child.getNodeFqn().size(); i++) {
						logger.trace("@@@@ childFqn: ["+i+"]: "+child.getNodeFqn().get(i)+
								", class: "+child.getNodeFqn().get(i).getClass().getCanonicalName());
					}
				}
				*/

				Object last = child.getNodeFqn().getLastElement();
				logger.trace("@@@@ last: "+last);
				if (last != null) {
					logger.trace("@@@@ last: " + last.getClass().getCanonicalName());
					if (last.equals(childObject)) {
						logger.trace("@@@@ childObject was found");
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
		logger.debug("@@@@ getChildren: "+this.getInfo());

		Set<Node> res = this.children;
		for (Node child : res) {
			logger.trace("@@@@ getChildren child: "+child.toString());
			logger.trace("@@@@ getChildren childFqn.size(): "+child.getNodeFqn().size());
			if (child.getNodeFqn().size() > 0) {
				for (int i = 0; i < child.getNodeFqn().size(); i++) {
					logger.trace("@@@@ getChildren childFqn: ["+i+"]: "+child.getNodeFqn().get(i));
					logger.trace("@@@@ getChildren childFqn: ["+i+"]: "+child.getNodeFqn().get(i).getClass().getCanonicalName());
				}
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
