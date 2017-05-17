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
import org.infinispan.Cache;
import org.infinispan.interceptors.CacheMgmtInterceptor;
import org.infinispan.interceptors.TxInterceptor;
import org.infinispan.interceptors.base.CommandInterceptor;
import org.infinispan.tree.Fqn;
import org.infinispan.tree.Node;

import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Common base proxy for runtime cached data. 
 * @author martins
 * @author András Kőkuti
 *
 */
public class CacheData {

	//private static final String IS_REMOVED_CACHE_NODE_MAP_KEY = "isremoved";
	
	private static final Logger logger = Logger.getLogger(CacheData.class);
	
	
	@SuppressWarnings("rawtypes")
	private Node node;
	
	private final Fqn nodeFqn;
	
	
	
	private boolean isRemoved;
	private final MobicentsCache mobicentsCache;
	
	private final static boolean doTraceLogs = logger.isTraceEnabled();  

	/*
	public CacheData(Fqn nodeFqn, MobicentsCache mobicentsCache) {		
		this.nodeFqn = nodeFqn;	
		this.mobicentsCache = mobicentsCache;
		this.node = mobicentsCache.getJBossCache().getRoot().getChild(nodeFqn);
		if (doTraceLogs) {
			logger.trace("cache node "+nodeFqn+" retrieved, result = "+this.node);
		}
		logger.info("cache node "+nodeFqn+" retrieved, result = "+this.node);
	}
	*/

	public CacheData(FqnWrapper nodeFqnWrapper, MobicentsCache mobicentsCache) {
		this.nodeFqn = nodeFqnWrapper.getFqn();
		this.mobicentsCache = mobicentsCache;

		//TxInterceptor txInterceptor = null;
		//boolean excludeTxInterceptor = isNotValidTransaction();
		//if (excludeTxInterceptor) {
		//	txInterceptor = excludeTxInterceptor();
		//}
		blockTxInterceptorIfTxNotValid();

		this.node = mobicentsCache.getJBossCache().getRoot().getChild(nodeFqn);

		//if (excludeTxInterceptor & txInterceptor != null) {
		//	this.getMobicentsCache().getJBossCache().getCache()
		//			.getAdvancedCache().addInterceptorAfter(txInterceptor, CacheMgmtInterceptor.class);
		//}
		unblockTxInterceptorIfTxNotValid();

		/*
		if (this.node == null) {
			Fqn lastFqn = Fqn.fromElements(this.nodeFqn.getLastElement());
			if (doTraceLogs) {
				logger.trace("cache node for " + nodeFqn + " is null, try to get node for " + lastFqn);
			}
			//this.node = mobicentsCache.getJBossCache().getRoot().getChild(lastFqn);
			//if (doTraceLogs) {
			//	logger.trace("cache node " + lastFqn + " retrieved, result = " + this.node);
			//}
		}
		*/

		if (doTraceLogs) {
			logger.trace("cache node " + nodeFqn + " retrieved, result = " + this.node);
		}
	}
	

	
	/**
	 * Verifies if node where data is stored exists in cache
	 * @return
	 */
	public boolean exists() {
		//return (node != null && (node.get(IS_REMOVED_CACHE_NODE_MAP_KEY) == null || (Boolean)node.get(IS_REMOVED_CACHE_NODE_MAP_KEY) == false)) ;
		return node != null;
	}

	/**
	 * Creates node to hold data in cache
	 */
	public boolean create() {
		if (!exists()) {
			node = mobicentsCache.getJBossCache().getRoot().addChild(nodeFqn);
			//node.put(IS_REMOVED_CACHE_NODE_MAP_KEY, false);
			if (doTraceLogs) {
				logger.trace("created cache node "+ node);
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

			blockTxInterceptorIfTxNotValid();

			node.clearData();
			//node.put(IS_REMOVED_CACHE_NODE_MAP_KEY, true);
			node.getParent().removeChild(nodeFqn.getLastElement());

			if (doTraceLogs) {
				logger.trace("removed cache node "+ node);

				/*
				logger.trace("removed cache node "+ node.getChildrenNames());
				logger.trace("removed cache node "+ node.getParent());
				if (node.getParent() != null) {
					logger.trace("removed cache node " + node.getParent().getChildrenNames());

					logger.trace("removed cache node "+ mobicentsCache.getJBossCache().getRoot());
					logger.trace("removed cache node " + mobicentsCache.getJBossCache().getRoot().getChildrenNames());

					Node test = mobicentsCache.getJBossCache().getRoot().getChild(nodeFqn.getLastElement());
					if (test != null) {
						logger.trace("WE SHOULD TO REMOVE IT FROM ROOT! "+nodeFqn.getLastElement());
						mobicentsCache.getJBossCache().getRoot().removeChild(nodeFqn.getLastElement());
					}

					logger.trace("removed cache node " + mobicentsCache.getJBossCache().getRoot().getChildrenNames());
				}
				*/

			}

			unblockTxInterceptorIfTxNotValid();
			
			//node = null;
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
	@SuppressWarnings({ "rawtypes" })
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
		Object result = null;
		blockTxInterceptorIfTxNotValid();

		result = getNode().get(key);

		unblockTxInterceptorIfTxNotValid();
		return result;
	}

	public Set getNodeChildrenNames() {
		Set result = null;
		blockTxInterceptorIfTxNotValid();

		final Node node = getNode();
		if (node != null) {
			result = node.getChildrenNames();
		} else {
			result = Collections.emptySet();
		}

		unblockTxInterceptorIfTxNotValid();
		return result;
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

	public Object putChildNodeValue(String child, boolean createIfNotExists, Object key, Object value) {
		Object result = null;
		blockTxInterceptorIfTxNotValid();

		Node childNode = getNode().getChild(child);
		if (childNode == null && createIfNotExists) {
			childNode = (Node) addChildNode(FqnWrapper.fromElementsWrapper(child));
		}
		if (childNode != null) {
			result = childNode.put(key, value);
		}

		unblockTxInterceptorIfTxNotValid();
		return result;
	}

	public boolean hasChildNode(Object child) {
		boolean result = false;
		blockTxInterceptorIfTxNotValid();

		result = getNode().hasChild(child);

		unblockTxInterceptorIfTxNotValid();
		return result;
	}

	public Object getChildNode(Object child) {
		Object result = null;
		blockTxInterceptorIfTxNotValid();

		result = (Object) getNode().getChild(child);

		unblockTxInterceptorIfTxNotValid();
		return result;
	}

	public Map<String, Object> getChildNodeData(String child) {
		final Node childNode = getNode().getChild(child);
		return childNode.getData();
	}

	public Set getChildNodeChildrenNames(Object child) {
		Set result = null;
		blockTxInterceptorIfTxNotValid();

		final Node childNode = getNode().getChild(child);
		if (childNode != null) {
			result = childNode.getChildrenNames();
		} else {
			result = Collections.emptySet();
		}

		unblockTxInterceptorIfTxNotValid();
		return result;
	}

	public Object getChildNodeValue(String child, Object key) {
		Object result = null;
		blockTxInterceptorIfTxNotValid();

		final Node childNode = getNode().getChild(child);
		if (childNode != null) {
			result = childNode.get(key);
		}

		unblockTxInterceptorIfTxNotValid();
		return result;
	}

	public boolean removeChildNode(String child) {
		boolean result = false;
		blockTxInterceptorIfTxNotValid();

		result = getNode().removeChild(child);

		unblockTxInterceptorIfTxNotValid();
		return result;
	}

	//

	private boolean isNotValidTransaction() {
		Transaction tx = null;
		int status = 0;
		//boolean isRollbackOnly = false;
		try {
			tx = this.getMobicentsCache().getTxManager().getTransaction();
			if (tx != null) {
				status = tx.getStatus();
				//isRollbackOnly = (status == Status.STATUS_MARKED_ROLLBACK);
				//logger.warn("**** TEST: isRollbackOnly: "+isRollbackOnly);
			}
		} catch (SystemException e) {
			return false;
		}

		return isNotValid(status);
	}

	private boolean isNotValid(int status) {
		return status != Status.STATUS_ACTIVE
				&& status != Status.STATUS_PREPARING
				&& status != Status.STATUS_COMMITTING;
	}

	private TxInterceptor txInterceptor = null;

	private void blockTxInterceptorIfTxNotValid() {
		if (isNotValidTransaction()) {
			Cache cache = this.getMobicentsCache().getJBossCache().getCache();
			Iterator<Object> iter = cache.getAdvancedCache().getInterceptorChain().iterator();
			while (iter.hasNext()) {
				CommandInterceptor ci = (CommandInterceptor) iter.next();
				if (ci instanceof TxInterceptor) {
					txInterceptor = (TxInterceptor) ci;
					cache.getAdvancedCache().removeInterceptor(TxInterceptor.class);
					break;
				}
			}
		}
	}

	private void unblockTxInterceptorIfTxNotValid() {
		if (isNotValidTransaction() & txInterceptor != null) {
			this.getMobicentsCache().getJBossCache().getCache()
					.getAdvancedCache().addInterceptorAfter(txInterceptor, CacheMgmtInterceptor.class);
		}
	}

}