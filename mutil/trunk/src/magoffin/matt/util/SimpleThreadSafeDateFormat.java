/* ===================================================================
 * SimpleThreadSafeDateFormat.java
 * 
 * Created Mar 8, 2007 6:01:54 PM
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
 * $Id: SimpleThreadSafeDateFormat.java,v 1.2 2007/03/10 02:54:52 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * {@link ThreadSafeDateFormat} implementation using Java's SimpleDateFormat.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2007/03/10 02:54:52 $
 */
public class SimpleThreadSafeDateFormat implements ThreadSafeDateFormat {
	
	/** The default value for the <code>datePattern</code> property. */
	public static final String DEFAULT_DATE_PATTERN = "yyyy-MM-dd HH:mm:ssZZ";
	
	private String datePattern;
	private TimeZone timeZone;

	/**
	 * Default constructor.
	 */
	public SimpleThreadSafeDateFormat() {
		this(DEFAULT_DATE_PATTERN);
	}
	
	/**
	 * Construct with a date pattern and default time zone.
	 * 
	 * @param datePattern the date pattern
	 */
	public SimpleThreadSafeDateFormat(String datePattern) {
		this(datePattern, TimeZone.getDefault());
	}


	/**
	 * Construct with fields.
	 * 
	 * @param datePattern the date pattern
	 * @param timeZone the time zone
	 */
	public SimpleThreadSafeDateFormat(String datePattern, TimeZone timeZone) {
		super();
		this.datePattern = datePattern;
		this.timeZone = timeZone != null ? timeZone : TimeZone.getDefault();
	}


	/* (non-Javadoc)
	 * @see magoffin.matt.util.ThreadSafeDateFormat#getPattern()
	 */
	public String getPattern() {
		return datePattern;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.util.ThreadSafeDateFormat#format(java.util.Date)
	 */
	public String format(Date date) {
		SimpleDateFormat sdf = getSdfInstance();
		return sdf.format(date);
	}
	
	private SimpleDateFormat getSdfInstance() {
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		sdf.setTimeZone(timeZone);
		return sdf;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.util.ThreadSafeDateFormat#format(java.util.Date, java.util.TimeZone)
	 */
	public String format(Date date, TimeZone zone) {
		if ( zone == null ) {
			return format(date);
		}
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		sdf.setTimeZone(zone);
		return sdf.format(date);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.util.ThreadSafeDateFormat#format(java.util.Calendar)
	 */
	public String format(Calendar date) {
		SimpleDateFormat sdf = getSdfInstance();
		return sdf.format(date.getTime());
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.util.ThreadSafeDateFormat#format(java.util.Calendar, java.util.TimeZone)
	 */
	public String format(Calendar date, TimeZone zone) {
		if ( zone == null ) {
			return format(date);
		}
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		sdf.setTimeZone(zone);
		return sdf.format(date.getTime());
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.util.ThreadSafeDateFormat#getCalendarInstance()
	 */
	public Calendar getCalendarInstance() {
		return Calendar.getInstance(timeZone);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.util.ThreadSafeDateFormat#parseCalendar(java.lang.String)
	 */
	public Calendar parseCalendar(String dateStr) {
		Date d = parseDate(dateStr);
		Calendar cal = getCalendarInstance();
		cal.setTime(d);
		return cal;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.util.ThreadSafeDateFormat#parseCalendar(java.lang.String, java.util.TimeZone)
	 */
	public Calendar parseCalendar(String dateStr, TimeZone zone) {
		if ( zone == null ) {
			return parseCalendar(dateStr);
		}
		SimpleDateFormat sdf = new SimpleDateFormat(datePattern);
		sdf.setTimeZone(zone);
		try {
			Date d = sdf.parse(dateStr);
			Calendar cal = Calendar.getInstance(zone);
			cal.setTime(d);
			return cal;
		} catch (ParseException e) {
			throw new RuntimeException(dateStr);
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.util.ThreadSafeDateFormat#parseDate(java.lang.String)
	 */
	public Date parseDate(String dateStr) {
		SimpleDateFormat sdf = getSdfInstance();
		try {
			return sdf.parse(dateStr);
		} catch (ParseException e) {
			throw new RuntimeException(dateStr);
		}
	}
	
	/**
	 * @return the datePattern
	 */
	public String getDatePattern() {
		return datePattern;
	}
	
	/**
	 * @param datePattern the datePattern to set
	 */
	public void setDatePattern(String datePattern) {
		this.datePattern = datePattern;
	}
	
	/**
	 * @return the timeZone
	 */
	public TimeZone getTimeZone() {
		return timeZone;
	}
	
	/**
	 * @param timeZone the timeZone to set
	 */
	public void setTimeZone(TimeZone timeZone) {
		this.timeZone = timeZone;
	}

}
