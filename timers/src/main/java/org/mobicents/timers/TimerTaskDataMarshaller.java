package org.mobicents.timers;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * 
 * @author martins
 * 
 * @param <T>
 */
public abstract class TimerTaskDataMarshaller {
	
	private final static int NOT_PERIODIC_TIMER = 255;
	
	protected long readStartTime;
	protected long readPeriod;
	protected PeriodicScheduleStrategy readPeriodicScheduleStrategy;
	
	protected void writeStandardTimerTaskData(ObjectOutput output, TimerTaskData timerTaskData) throws IOException {
		output.writeLong(timerTaskData.getStartTime());
		output.writeLong(timerTaskData.getPeriod());
		if (timerTaskData.getPeriodicScheduleStrategy() != null) {
			output.write(timerTaskData.getPeriodicScheduleStrategy().ordinal());
		}
		else {
			output.write(NOT_PERIODIC_TIMER);
		}
	}
	
	protected void readStandardTimerTaskData(ObjectInput input) throws IOException {
		readStartTime = input.readLong();
		readPeriod = input.readLong();
		int ordinal = input.read();
		if (ordinal != NOT_PERIODIC_TIMER) {
			readPeriodicScheduleStrategy = PeriodicScheduleStrategy.values()[ordinal];
		}
		else {
			readPeriodicScheduleStrategy = null;
		}
	}
	
	/**
	 * 
	 * @param output
	 * @param object
	 * @throws IOException
	 */
	public abstract void writeTimerTaskData(ObjectOutput output, Object object)
			throws IOException;

	/**
	 * 
	 * @param input
	 * @param taskID
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public abstract Object readTimerTaskData(ObjectInput input) throws IOException,
			ClassNotFoundException;
	
}
