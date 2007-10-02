/* ============================================================================
 * ThreadSafeDateFormat.java
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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
 * ============================================================================
 * $Id: ThreadSafeDateFormat.java,v 1.2 2007/03/10 02:54:52 matt Exp $
 * ============================================================================
 */

package magoffin.matt.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Simple API for date formatting/parsing, to be used in a multi-threaded 
 * environment.
 * 
 * <p>This API is designed to be used as a pre-configured instance class, 
 * such as a Spring bean or a static field constant. The locale and time 
 * zone configuration that the dates use are determined at configuration 
 * time and are not changeable at runtime.</p>
 * 
 * @author matt
 * @version $Revision: 1.2 $ $Date: 2007/03/10 02:54:52 $
 */
public interface ThreadSafeDateFormat {
	
	/**
	 * Get the pattern used by this date format.
	 * @return pattern
	 */
	String getPattern();

	/**
	 * Format a Date.
	 * 
	 * @param date the date to format
	 * @return the formatted date
	 */
	String format(Date date);

	/**
	 * Format a Date with a time zone.
	 * 
	 * @param date the date to format
	 * @param zone the time zone to format the date string as
	 * @return the formatted date
	 */
	String format(Date date, TimeZone zone);

	/**
	 * Format a Calendar.
	 * 
	 * @param date the date to format
	 * @return the formatted date
	 */
	String format(Calendar date);

	/**
	 * Format a Calendar with a time zone.
	 * 
	 * @param date the date to format
	 * @param zone the time zone to format the date string as
	 * @return the formatted date
	 */
	String format(Calendar date, TimeZone zone);

	/**
	 * Parse a string into a Date.
	 * 
	 * @param dateStr the date string
	 * @return the Date
	 */
	Date parseDate(String dateStr);

	/**
	 * Parse a string into a Calendar.
	 * 
	 * @param dateStr the date string
	 * @return the Calendar
	 */
	Calendar parseCalendar(String dateStr);
	
	/**
	 * Parse a string into a Calendar, using an arbitrary TimeZone.
	 * 
	 * @param dateStr the date string
	 * @param zone the time zone to treat the date string as
	 * @return the Calendar
	 */
	Calendar parseCalendar(String dateStr, TimeZone zone);
	
	/**
	 * Get a Calendar instance, configured to the proper time zone.
	 * @return Calendar instance
	 */
	Calendar getCalendarInstance();
	
}
