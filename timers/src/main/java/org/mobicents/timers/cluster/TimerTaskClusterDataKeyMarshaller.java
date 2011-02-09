package org.mobicents.timers.cluster;

import org.mobicents.cluster.base.data.marshall.ExternalizableMarshaller;

/**
 * 
 * @author martins
 * 
 */
public class TimerTaskClusterDataKeyMarshaller extends
		ExternalizableMarshaller<TimerTaskClusterDataKey> {

	public static final Integer ID = 35001;

	@Override
	public Class<? extends TimerTaskClusterDataKey> getDataType() {
		return TimerTaskClusterDataKey.class;
	}

	@Override
	public Integer getMarshallerID() {
		return ID;
	}

	@Override
	public TimerTaskClusterDataKey getDataTypeInstance() {
		return new TimerTaskClusterDataKey();
	}

}
