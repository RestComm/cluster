package org.mobicents.cluster.example.as5;

import java.io.Serializable;
import java.util.TimerTask;

/**
 * 
 * @author <a href="mailto:baranowb@gmail.com">Bartosz Baranowski</a>
 */
public class SerTimerTask extends TimerTask implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/* (non-Javadoc)
	 * @see org.mobicents.slee.core.timers.TimerTask#run()
	 */
	@Override
	public void run() {
		System.out.println("------------------------ TIMER RUN COMPLETED ------------------------");		
	}
	
}
