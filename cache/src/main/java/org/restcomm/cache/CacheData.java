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
	private boolean isRemoved;

	public CacheData(Fqn nodeFqn, MobicentsCache mobicentsCache) {
		this.mobicentsCache = mobicentsCache;

		logger.debug("@@@@ CacheData: nodeFqn: " + nodeFqn);
		Node foundNode = null;
		if (this.mobicentsCache.getJBossCache().keySet()
				.contains(nodeFqn.toString())) {
			Object checkNode = this.mobicentsCache.getJBossCache().get(nodeFqn.toString() + "_/_" + "Node");
			logger.info("@@@@ FOUND checkNode: "+checkNode);
			if (checkNode instanceof Node) {
				foundNode = (Node) checkNode;
			}
		}

		if (foundNode == null) {
			this.node = new Node(this.mobicentsCache.getJBossCache(), nodeFqn);
			logger.debug("@@@@ new Node");
		} else {
			this.node = foundNode;
			logger.debug("@@@@ found Node");
			logger.trace("@@@@ found Node: this.node.getChildren(): " + this.node.getChildren());
			logger.trace("@@@@ found Node: this.node.getChildNames(): " + this.node.getChildNames());
		}

		if (logger.isDebugEnabled()) {
			logger.debug("cache node "+nodeFqn+" retrieved, result = "+this.node);
		}
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
			this.node.create();

			if (logger.isDebugEnabled()) {
				logger.debug("created cache node "+this.node);
			}
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
			if (logger.isDebugEnabled()) {
				logger.debug("removing cache node "+this.node);
			}

			isRemoved = true;
			this.node.remove();

			if (logger.isDebugEnabled()) {
				logger.debug("removed cache node "+this.node);
			}
			return true;
		} else {
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