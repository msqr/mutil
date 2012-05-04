/* ===================================================================
 * MultiDateFormat.java
 * 
 * Created Mar 8, 2007 5:51:04 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: MultiDateFormat.java,v 1.2 2007/03/10 02:54:52 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * {@link ThreadSafeDateFormat} implementation that delegates
 * to a number of other {@link ThreadSafeDateFormat} implementations,
 * using the first one that works.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl>
 *   <dt>delegates</dt>
 *   <dd>An array of {@link ThreadSafeDateFormat} implementations to 
 *   delegate all methods to. They will be tried in array order, and
 *   the first non-null value that one returns will be returned from 
 *   each method.</dd>
 * </dl>
 * 
 * <p>Note the {@link #getPattern()} method returns just the first
 * configured date format's pattern.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2007/03/10 02:54:52 $
 */
public class MultiDateFormat implements ThreadSafeDateFormat {

	private ThreadSafeDateFormat[] delegates = new ThreadSafeDateFormat[0];

	@Override
	public String getPattern() {
		return delegates[0].getPattern();
	}

	@Override
	public String format(Date date) {
		for ( ThreadSafeDateFormat f : delegates ) {
			try {
				String result = f.format(date);
				if ( result != null ) {
					return result;
				}
			} catch ( RuntimeException e ) {
				// ignore
			}
		}
		throw new RuntimeException("Could not format date with any delgate");
	}

	@Override
	public String format(Date date, TimeZone zone) {
		for ( ThreadSafeDateFormat f : delegates ) {
			try {
				String result = f.format(date, zone);
				if ( result != null ) {
					return result;
				}
			} catch ( RuntimeException e ) {
				// ignore
			}
		}
		throw new RuntimeException("Could not format date with any delgate");
	}

	@Override
	public String format(Calendar date) {
		for ( ThreadSafeDateFormat f : delegates ) {
			try {
				String result = f.format(date);
				if ( result != null ) {
					return result;
				}
			} catch ( RuntimeException e ) {
				// ignore
			}
		}
		throw new RuntimeException("Could not format date with any delgate");
	}

	@Override
	public String format(Calendar date, TimeZone zone) {
		for ( ThreadSafeDateFormat f : delegates ) {
			try {
				String result = f.format(date, zone);
				if ( result != null ) {
					return result;
				}
			} catch ( RuntimeException e ) {
				// ignore
			}
		}
		throw new RuntimeException("Could not format date with any delgate");
	}

	@Override
	public Calendar getCalendarInstance() {
		for ( ThreadSafeDateFormat f : delegates ) {
			try {
				Calendar cal = f.getCalendarInstance();
				if ( cal != null ) {
					return cal;
				}
			} catch ( RuntimeException e ) {
				// ignore
			}
		}
		throw new RuntimeException("Could not get Calendar from any delgate");
	}

	@Override
	public Calendar parseCalendar(String dateStr) {
		for ( ThreadSafeDateFormat f : delegates ) {
			try {
				Calendar result = f.parseCalendar(dateStr);
				if ( result != null ) {
					return result;
				}
			} catch ( RuntimeException e ) {
				// ignore
			}
		}
		throw new RuntimeException("Could not parse Calendar with any delgate");
	}

	@Override
	public Calendar parseCalendar(String dateStr, TimeZone zone) {
		for ( ThreadSafeDateFormat f : delegates ) {
			try {
				Calendar result = f.parseCalendar(dateStr, zone);
				if ( result != null ) {
					return result;
				}
			} catch ( RuntimeException e ) {
				// ignore
			}
		}
		throw new RuntimeException("Could not parse Calendar with any delgate");
	}

	@Override
	public Date parseDate(String dateStr) {
		for ( ThreadSafeDateFormat f : delegates ) {
			try {
				Date result = f.parseDate(dateStr);
				if ( result != null ) {
					return result;
				}
			} catch ( RuntimeException e ) {
				// ignore
			}
		}
		throw new RuntimeException("Could not parse Date with any delgate");
	}
	
	/**
	 * @return the delegates
	 */
	public ThreadSafeDateFormat[] getDelegates() {
		return delegates;
	}

	/**
	 * @param delegates the delegates to set
	 */
	public void setDelegates(ThreadSafeDateFormat[] delegates) {
		this.delegates = delegates;
	}

}
