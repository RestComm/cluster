/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2016, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package org.restcomm.cache;

import org.apache.log4j.Logger;
import org.restcomm.cache.tree.Fqn;
import org.restcomm.cache.tree.Node;


/**
 * Common base proxy for runtime cached data. 
 * @author martins
 * @author András Kőkuti
 *
 */
public class CacheData {

	private static final Logger logger = Logger.getLogger(CacheData.class);


	private final MobicentsCache mobicentsCache;
	
	@SuppressWarnings("rawtypes")
	private Node node;

	//private final Fqn nodeFqn;

	private boolean isRemoved;

	//private static final String IS_REMOVED_CACHE_NODE_MAP_KEY = "isremoved";

	private final static boolean doTraceLogs = logger.isTraceEnabled();  
	
	public CacheData(Fqn nodeFqn, MobicentsCache mobicentsCache) {
		this.mobicentsCache = mobicentsCache;
		//this.nodeFqn = nodeFqn;
		//this.node = mobicentsCache.getJBossCache().getRoot().getChild(nodeFqn);

		logger.debug("@@@@ CacheData: nodeFqn: " + nodeFqn);

		// TODO: How to find node for nodeFqn:
		// 1: FOR
		Node foundNode = null;
		for (Object key: this.mobicentsCache.getJBossCache().keySet()) {
			logger.trace("@@@@ key: "+key);
			logger.trace("@@@@ key: "+key.getClass().getCanonicalName());
			Object value = this.mobicentsCache.getJBossCache().get(key);
			logger.trace("@@@@ value: "+value);
			Object checkNode1 = this.mobicentsCache.getJBossCache().get(key + "_/_" + "Node");
			logger.trace("@@@@ checkNode1: "+checkNode1);

			String stringKey = key.toString();
			if (nodeFqn.toString().equals(stringKey)) {
				logger.info("@@@@ FOUND stringKey: "+stringKey);
				logger.info("@@@@ FOUND-1 checkNode1: "+checkNode1);
				if (checkNode1 instanceof Node) {
					foundNode = (Node) checkNode1;
				}
			}
		}

		// 2:
		if (this.mobicentsCache.getJBossCache().keySet()
				.contains(nodeFqn.toString())) {
			Object checkNode2 = this.mobicentsCache.getJBossCache().get(nodeFqn.toString() + "_/_" + "Node");
			logger.info("@@@@ FOUND-2 checkNode2: "+checkNode2);
			//if (checkNode2 instanceof Node) {
			//	foundNode = (Node) checkNode2;
			//}
		}

		if (foundNode == null) {
			this.node = new Node(this.mobicentsCache.getJBossCache(), nodeFqn);
			logger.debug("@@@@ new Node: nodeFqn: " + nodeFqn);
			logger.trace("@@@@ new Node: this.node.getChildren(): " + this.node.getChildren());
			logger.trace("@@@@ new Node: this.node.getChildNames(): " + this.node.getChildNames());
		} else {
			this.node = foundNode;
			logger.debug("@@@@ found Node: nodeFqn: " + nodeFqn);
			logger.debug("@@@@ found Node: this.node.getChildren(): " + this.node.getChildren());
			logger.debug("@@@@ found Node: this.node.getChildNames(): " + this.node.getChildNames());
		}
		logger.debug("@@@@ CacheData: node: " + this.node);

		//this.node = this.rootNode.getChild(nodeFqn);
		
		//if (doTraceLogs) {
		//	logger.trace("cache node "+nodeFqn+" retrieved, result = "+this.node);
		//}
		logger.info("cache node "+nodeFqn+" retrieved, result = "+this.node);
	}
	

	
	/**
	 * Verifies if node where data is stored exists in cache
	 * @return
	 */
	public boolean exists() {
		return this.node.exists();
		
		
	}

	/**
	 * Creates node to hold data in cache
	 */
	public boolean create() {
		if (!exists()) {
			//node = mobicentsCache.getJBossCache().getRoot().addChild(nodeFqn);
			//node.put(IS_REMOVED_CACHE_NODE_MAP_KEY, false);
			
			this.node.create();

			logger.debug("@@@@ create this.node: "+this.node);
			logger.debug("@@@@ create this.node.getFqn(): "+this.node.getFqn());
			logger.debug("@@@@ create this.node.getFqn().getParent(): "+this.node.getFqn().getParent());

			//Node rootNode = new Node(mobicentsCache.getJBossCache(), this.node.getFqn().getParent());
			//rootNode.addChild(node.getFqn());
			
			//if (doTraceLogs) {
			//	logger.trace("created cache node "+this.node);
			//}
			logger.debug("created cache node "+this.node);
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Returns true if it was requested to remove the data from cache
	 * @return
	 */
	public boolean isRemoved() {
		return isRemoved;
	}
	
	/**
	 * Removes node that holds data in cache
	 */
	public boolean remove() {
		if (exists() && !isRemoved()) {
			logger.info("removing cache node "+this.node);

			isRemoved = true;

			/*
			node.clearData();
			node.put(IS_REMOVED_CACHE_NODE_MAP_KEY, true);
			if (doTraceLogs) {
				logger.trace("removed cache node "+ node);
			}
			
			node = null;
			return true;
			*/

			this.node.remove();
			
			//if (doTraceLogs) {
			//	logger.trace("removed cache node "+ node);
			//}

			logger.info("removed cache node "+this.node);
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * 
	 * Retrieves the cache {@link Node} which holds the data in cache
	 * 
	 * Throws {@link IllegalStateException} if remove() was invoked
	 */
	
	public Node getNode() {
		if (isRemoved()) {
			throw new IllegalStateException();
		}

		logger.trace("@@@@ getNode: this.node.getChildren(): "+this.node.getChildren());
		logger.trace("@@@@ getNode: this.node.getChildNames(): "+this.node.getChildNames());

		return node;
	}
	
	/**
	 * 
	 * @return
	 */
	public MobicentsCache getMobicentsCache() {
		return mobicentsCache;
	}
	
	/**
	 * Retrieves the node fqn
	 * @return the nodeFqn
	 */
	
	public Fqn getNodeFqn() {
		return this.node.getNodeFqn();
	}
}