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
 * $Id: FastThreadSafeDateFormat.java,v 1.1 2007/03/10 02:54:52 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.time.FastDateFormat;

/**
 * Extension of {@link SimpleThreadSafeDateFormat} that uses Apache's
 * {@link FastDateFormat} for formatting dates.
 * 
 * <p>Parsing of dates is handled by {@link SimpleThreadSafeDateFormat}, 
 * i.e. via Java's SimpleDateFormat.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2007/03/10 02:54:52 $
 */
public class FastThreadSafeDateFormat extends SimpleThreadSafeDateFormat {
	
	private FastDateFormat fastDate;
	
	/**
	 * Default constructor.
	 */
	public FastThreadSafeDateFormat() {
		this(DEFAULT_DATE_PATTERN);
	}
	
	/**
	 * Construct with a date pattern and default time zone.
	 * 
	 * @param datePattern the date pattern
	 */
	public FastThreadSafeDateFormat(String datePattern) {
		this(datePattern, TimeZone.getDefault());
	}


	/**
	 * Construct with fields.
	 * 
	 * @param datePattern the date pattern
	 * @param timeZone the time zone
	 */
	public FastThreadSafeDateFormat(String datePattern, TimeZone timeZone) {
		super(datePattern, timeZone);
		fastDate = FastDateFormat.getInstance(datePattern, timeZone);
	}

	@Override
	public String format(Date date) {
		return fastDate.format(date);
	}
	
	@Override
	public String format(Calendar date) {
		return fastDate.format(date);
	}

	@Override
	public String format(Calendar date, TimeZone zone) {
		FastDateFormat fdf = FastDateFormat.getInstance(getDatePattern(), zone);
		return fdf.format(date);
	}

	@Override
	public String format(Date date, TimeZone zone) {
		FastDateFormat fdf = FastDateFormat.getInstance(getDatePattern(), zone);
		return fdf.format(date);
	}

}
