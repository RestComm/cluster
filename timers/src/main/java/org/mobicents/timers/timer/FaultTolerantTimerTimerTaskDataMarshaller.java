package org.mobicents.timers.timer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.TimerTask;

import org.mobicents.timers.TimerTaskDataMarshaller;

/**
 * 
 * @author martins
 *
 */
public class FaultTolerantTimerTimerTaskDataMarshaller extends TimerTaskDataMarshaller {
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.timers.TimerTaskDataMarshaller#readTimerTaskData(java.io.ObjectInput)
	 */
	@Override
	public Object readTimerTaskData(ObjectInput input) throws IOException,
			ClassNotFoundException {
		// read the standard timer task datat fields
		super.readStandardTimerTaskData(input);
		// read the java.util timer task
		TimerTask timerTask = (TimerTask) input.readObject();
		// recreate the timer task data
		return new FaultTolerantTimerTimerTaskData(timerTask,readStartTime,readPeriod,readPeriodicScheduleStrategy);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.mobicents.timers.TimerTaskDataMarshaller#writeTimerTaskData(java.io.ObjectOutput, java.lang.Object)
	 */
	@Override
	public void writeTimerTaskData(ObjectOutput output, Object object)
			throws IOException {
		FaultTolerantTimerTimerTaskData timerTaskData = (FaultTolerantTimerTimerTaskData) object;
		// write the standard timer task data
		super.writeStandardTimerTaskData(output, timerTaskData);
		// write the java.util timer task
		output.writeObject(timerTaskData.getJavaUtilTimerTask());
	}
}
