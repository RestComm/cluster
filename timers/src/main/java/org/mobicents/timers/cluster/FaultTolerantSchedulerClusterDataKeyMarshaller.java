package org.mobicents.timers.cluster;

import org.mobicents.cluster.base.data.marshall.ExternalizableMarshaller;

/**
 * 
 * @author martins
 * 
 */
public class FaultTolerantSchedulerClusterDataKeyMarshaller extends
		ExternalizableMarshaller<FaultTolerantSchedulerClusterDataKey> {

	public static final Integer ID = 35000;

	@Override
	public Class<? extends FaultTolerantSchedulerClusterDataKey> getDataType() {
		return FaultTolerantSchedulerClusterDataKey.class;
	}

	@Override
	public FaultTolerantSchedulerClusterDataKey getDataTypeInstance() {
		return new FaultTolerantSchedulerClusterDataKey();
	}

	@Override
	public Integer getMarshallerID() {
		return ID;
	}
}
