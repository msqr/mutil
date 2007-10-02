/* ============================================================================
 * BaseQueueThread.java
 * 
 * Copyright (c) 2006 Matt Magoffin.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
 * 02111-1307 USA
 * ===================================================================
 * $Id: BaseQueueThread.java,v 1.2 2007/03/04 06:05:11 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

/**
 * Base thread class for managing a queue of objects.
 * 
 * @param <E> a queue item type
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2007/03/04 06:05:11 $
 */
public abstract class BaseQueueThread<E> implements Runnable {
	
	/** Class logger. */
	protected final Logger queueLog = Logger.getLogger(getClass());

	private BlockingQueue<E> queue;
	private E stopItem;
	private boolean keepGoing;
	private boolean started;

	/**
	 * Construct the queue thread.
	 *
	 * <p>This method will create a new queue for processing.</p>
	 * 
	 * @param stopItem the queue item that will be added to the queue to 
	 * signal the queue should stop processing items
	 */
	public BaseQueueThread(E stopItem) {
		this.queue = new LinkedBlockingQueue<E>();
		this.keepGoing = true;
		this.started = false;
		this.stopItem = stopItem;
	}
	
	/**
	 * Get the stop item.
	 * @return the stop item
	 */
	protected E getStopItem() {
		return stopItem;
	}
	
	/**
	 * Return a name for this thread.
	 *
	 * <p>This name is used in log statements.</p>
	 * @return name
	 */
	public abstract String getThreadName();
	
	/**
	 * Handle a new item from the queue.
	 *
	 * <p>This method is called by {@link #run()} after a new object has
	 * been added to the queue.</p>
	 *
	 * @param item the enqueued item
	 */
	protected abstract void handleItem(E item);
	
	/**
	 * Method called by {@link #run()} when leaving that method.
	 * <p>Extending classes may want to override this method to perform some
	 * last-minute cleanup.</p>
	 */
	protected void exiting() {
		// nothing here
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public final void run() {
		started = true;
		
		if ( queueLog.isInfoEnabled() ) {
			queueLog.info("Starting "+getThreadName() +" thread "
					+Integer.toHexString(hashCode()));
		}
	
		while (true) {
			try {
				E item = queue.take();
	
				if (item == this.stopItem && !keepGoing ) {
					break;
				}
	
				handleItem(item);
	
			} catch ( Exception e ) {
				queueLog.error("Unhandled exception in queue thread",e);
			}
		}
		
		if ( queueLog.isInfoEnabled() ) {
			queueLog.info("Exiting "+getThreadName() +" thread "
					+Integer.toHexString(hashCode()));
		}
		try {
			exiting();
		} catch ( Exception e ) {
			queueLog.warn("Exception while exiting",e);
		}

		synchronized (this) {
			notifyAll(); // we're done now
		}
	}
	
	/**
	 * Stop this thread.
	 *
	 * <p>The thread will finish processing any items in the queue,
	 * then exit.</p>
	 */
	public synchronized final void stop() {
		if ( !keepGoing ) {
			return; // already stopped
		}
		if ( !started ) {
			return; // never started
		}
		if ( queueLog.isInfoEnabled() ) {
			queueLog.info("Stopping "+getThreadName() +" thread "
					+Integer.toHexString(hashCode()));
		}
		keepGoing = false;
		queue.add(this.stopItem); // in case nothing in queue, this wakes the queue up
		try {
			wait();
		} catch ( InterruptedException e ) {
			queueLog.warn("Interrupted while waiting for queue to empty");
		}
		if ( queueLog.isInfoEnabled() ) {
			queueLog.info("Thread " +getThreadName() +" stopped.");
		}
	}
	
	/**
	 * Add an object to the queue.
	 * @param item the object to add
	 */
	public final void enqueue(E item) {
		if ( keepGoing && item != null ) {
			queue.add(item);
		}
	}
	
	/**
	 * Add a collection of items to the queue.
	 * @param items the items to add
	 */
	public final void enqueueAll(Collection<E> items) {
		if ( keepGoing && items != null ) {
			queue.addAll(items);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		stop();
	}

}
