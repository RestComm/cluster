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

package org.mobicents.timers.cache;

import java.util.Collections;
import java.util.Set;

import org.mobicents.cache.CacheData;
import org.mobicents.cluster.MobicentsCluster;

import org.infinispan.tree.Node;
import org.infinispan.tree.Fqn;

/**
 * 
 * Proxy object for timer facility entity data management through Infinispan Cache
 * 
 * @author martins
 * @author András Kőkuti
 * 
 */

public class FaultTolerantSchedulerCacheData extends CacheData {
			
	/**
	 * 
	 * @param txManager 
	 * @param txManager
	 */
	//@SuppressWarnings("unchecked")
	public FaultTolerantSchedulerCacheData(Fqn baseFqn, MobicentsCluster cluster) {
		super(baseFqn,cluster.getMobicentsCache());
	}

	public Set<?> getTaskIDs() {
		final Node<?,?> node = getNode();
		if (!node.getChildren().isEmpty()) {
			return node.getChildrenNames();			
		}
		else {
			return Collections.EMPTY_SET;
		}
	}
	
}
