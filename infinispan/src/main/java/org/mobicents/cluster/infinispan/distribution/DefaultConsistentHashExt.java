package org.mobicents.cluster.infinispan.distribution;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.infinispan.distribution.ch.DefaultConsistentHash;
import org.infinispan.marshall.Marshalls;
import org.infinispan.remoting.transport.Address;
import org.mobicents.cluster.data.ClusterDataKey;
import org.mobicents.cluster.data.DependentClusterDataKey;
import org.mobicents.cluster.infinispan.data.InfinispanClusterDataKey;
import org.mobicents.cluster.infinispan.data.marshall.ExternalizerIds;

/**
 * Extension of Infinispan's class that handles consistent hashing. The
 * extension is aware that some objects must be colocated.
 * 
 * @author martins
 * 
 */
public class DefaultConsistentHashExt extends DefaultConsistentHash {

	private static final Class<?> InfinispanClusterDataKey_TYPE = InfinispanClusterDataKey.class;

	/**
	 * Retrieves the object to be used when calculating the consistent hash.
	 * 
	 * @param object
	 * @return
	 * @throws NullPointerException
	 */
	protected Object getHashObject(Object object) throws NullPointerException {
		if (object.getClass() == InfinispanClusterDataKey_TYPE) {
			// all types of the InfinispanClusterDataKey hash to wrapped key
			final ClusterDataKey clusterDataKey = ((InfinispanClusterDataKey) object)
					.getKey();
			if (clusterDataKey instanceof DependentClusterDataKey) {
				// defer hash to the other key
				return ((DependentClusterDataKey) clusterDataKey).dependsOn();
			} else {
				return clusterDataKey;
			}
		}
		return object;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.infinispan.distribution.ch.DefaultConsistentHash#locate(java.lang
	 * .Object, int)
	 */
	@Override
	public List<Address> locate(Object object, int replCount) {
		return super.locate(getHashObject(object), replCount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.infinispan.distribution.ch.DefaultConsistentHash#isKeyLocalToAddress
	 * (org.infinispan.remoting.transport.Address, java.lang.Object, int)
	 */
	@Override
	public boolean isKeyLocalToAddress(Address target, Object object,
			int replCount) {
		return super.isKeyLocalToAddress(target, getHashObject(object),
				replCount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.infinispan.distribution.ch.DefaultConsistentHash#toString()
	 */
	@Override
	public String toString() {
		return super.toString();
	}

	/**
	 * Infinispan Externalizer
	 * 
	 * @author martins
	 * 
	 */
	@Marshalls(typeClasses = DefaultConsistentHashExt.class, id = ExternalizerIds.DefaultConsistentHashExt)
	public static class Externalizer implements
			org.infinispan.marshall.Externalizer<DefaultConsistentHashExt> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.infinispan.marshall.Externalizer#writeObject(java.io.ObjectOutput
		 * , java.lang.Object)
		 */
		@Override
		public void writeObject(ObjectOutput output,
				DefaultConsistentHashExt dch) throws IOException {
			output.writeObject(dch.addresses);
			output.writeObject(dch.positions);
			output.writeObject(dch.addressToHashIds);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.infinispan.marshall.Externalizer#readObject(java.io.ObjectInput)
		 */
		@SuppressWarnings("unchecked")
		@Override
		public DefaultConsistentHashExt readObject(ObjectInput unmarshaller)
				throws IOException, ClassNotFoundException {
			DefaultConsistentHashExt dch = new DefaultConsistentHashExt();
			dch.addresses = (ArrayList<Address>) unmarshaller.readObject();
			dch.positions = (SortedMap<Integer, Address>) unmarshaller
					.readObject();
			dch.addressToHashIds = (Map<Address, Integer>) unmarshaller
					.readObject();
			return dch;
		}
	}
}
