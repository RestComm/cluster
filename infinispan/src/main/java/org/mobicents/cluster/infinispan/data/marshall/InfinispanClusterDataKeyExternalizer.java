package org.mobicents.cluster.infinispan.data.marshall;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

import org.infinispan.marshall.Externalizer;
import org.mobicents.cluster.infinispan.data.InfinispanClusterDataKey;

/**
 * Marshalls cluster data keys in Infinispan.
 * 
 * @author martins
 * @param <T>
 * 
 */
public class InfinispanClusterDataKeyExternalizer implements
		Externalizer<InfinispanClusterDataKey> {

	private static final Integer ID = 5432;

	private final Set<Class<? extends InfinispanClusterDataKey>> typeClasses;

	public InfinispanClusterDataKeyExternalizer() {
		typeClasses = new HashSet<Class<? extends InfinispanClusterDataKey>>();
		typeClasses.add(InfinispanClusterDataKey.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.infinispan.marshall.Externalizer#readObject(java.io.ObjectInput)
	 */
	@Override
	public InfinispanClusterDataKey readObject(ObjectInput objectInput)
			throws IOException, ClassNotFoundException {
		InfinispanClusterDataKey key = new InfinispanClusterDataKey();
		key.readExternal(objectInput);
		return key;		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.infinispan.marshall.Externalizer#writeObject(java.io.ObjectOutput,
	 * java.lang.Object)
	 */
	@Override
	public void writeObject(ObjectOutput objectOutput,
			InfinispanClusterDataKey infinispanKey) throws IOException {
		infinispanKey.writeExternal(objectOutput);
	}

	@Override
	public Integer getId() {
		return ID;
	}

	@Override
	public Set<Class<? extends InfinispanClusterDataKey>> getTypeClasses() {
		return typeClasses;
	}

}
