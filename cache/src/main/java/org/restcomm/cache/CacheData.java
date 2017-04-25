/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.restcomm.cache;

import org.apache.log4j.Logger;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;

import java.util.Map;

/**
 * Common base proxy for runtime cached data. 
 * @author martins
 *
 */
public class CacheData {

	private static final Logger logger = Logger.getLogger(CacheData.class);
	
	@SuppressWarnings("unchecked")
	private Node node;
	@SuppressWarnings("unchecked")
	private final Fqn nodeFqn;
	
	private boolean isRemoved;
	private final MobicentsCache mobicentsCache;
	
	private final static boolean doTraceLogs = logger.isTraceEnabled();  

	/*
	@SuppressWarnings("unchecked")
	public CacheData(Fqn nodeFqn, MobicentsCache mobicentsCache) {		
		this.nodeFqn = nodeFqn;	
		this.mobicentsCache = mobicentsCache;
		this.node = mobicentsCache.getJBossCache().getRoot().getChild(nodeFqn);
		if (doTraceLogs) {
			logger.trace("cache node "+nodeFqn+" retrieved, result = "+this.node);
		}
	}
	*/

	@SuppressWarnings("unchecked")
	public CacheData(FqnWrapper nodeFqnWrapper, MobicentsCache mobicentsCache) {
		this.nodeFqn = nodeFqnWrapper.getFqn();
		this.mobicentsCache = mobicentsCache;
		this.node = mobicentsCache.getJBossCache().getRoot().getChild(nodeFqn);
		if (doTraceLogs) {
			logger.trace("cache node "+nodeFqn+" retrieved, result = "+this.node);
		}
	}
	
	/**
	 * Verifies if node where data is stored exists in cache
	 * @return
	 */
	public boolean exists() {
		return node != null;
	}

	/**
	 * Creates node to hold data in cache
	 */
	public boolean create() {
		if (!exists()) {
			node = mobicentsCache.getJBossCache().getRoot().addChild(nodeFqn);
			if (doTraceLogs) {
				logger.trace("created cache node "+nodeFqn);
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
			isRemoved = true;
			node.getParent().removeChild(nodeFqn.getLastElement());
			if (doTraceLogs) {
				logger.trace("removed cache node "+nodeFqn);
			}	
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
	@SuppressWarnings("unchecked")
	protected Node getNode() {
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
	@SuppressWarnings("unchecked")
	public Fqn getNodeFqn() {
		return nodeFqn;
	}

	public FqnWrapper getNodeFqnWrapper() {
		return new FqnWrapper(nodeFqn);
	}

	public Object getNodeFqnLastElement() {
		return nodeFqn.getLastElement();
	}

	public Object putNodeValue(Object key, Object value) {
		return getNode().put(key, value);
	}

	public Object getNodeValue(Object key) {
		return getNode().get(key);
	}

	public Object removeNodeValue(Object key) {
		return getNode().remove(key);
	}

	public Object addChildNode(FqnWrapper fqnWrapper) {
		final Node childNode = getNode().addChild(fqnWrapper.getFqn());
		return (Object) childNode;
	}

	public Object putChildNodeValue(FqnWrapper fqnWrapper, Object key, Object value) {
		final Node childNode = getNode().getChild(fqnWrapper.getFqn());
		if (childNode != null) {
			return childNode.put(key, value);
		}
		return null;
	}

	public Object getChildNode(String child) {
		final Node childNode = getNode().getChild(child);
		return (Object) childNode;
	}

	public Map<String, Object> getChildNodeData(String child) {
		final Node childNode = getNode().getChild(child);
		return childNode.getData();
	}

	public Object getChildNodeValue(String child, Object key) {
		final Node childNode = getNode().getChild(child);
		return childNode.get(key);
	}

	public boolean removeChildNode(String child) {
		return getNode().removeChild(child);
	}

}