package org.mobicents.timers.timer;

import org.mobicents.cluster.base.data.marshall.ExternalizableMarshaller;

/**
 * 
 * @author martins
 * 
 */
public class FaultTolerantTimerTimerTaskDataMarshaller extends
		ExternalizableMarshaller<FaultTolerantTimerTimerTaskData> {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.mobicents.timers.cluster.TimerTaskDataMarshaller#getDataType()
	 */
	@Override
	public Class<? extends FaultTolerantTimerTimerTaskData> getDataType() {
		return FaultTolerantTimerTimerTaskData.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.mobicents.timers.cluster.TimerTaskDataMarshaller#getDataTypeInstance
	 * ()
	 */
	@Override
	public FaultTolerantTimerTimerTaskData getDataTypeInstance() {
		return new FaultTolerantTimerTimerTaskData();
	}

	private static Integer ID = 35003;
	
	@Override
	public Integer getMarshallerID() {
		return ID;
	}

}
