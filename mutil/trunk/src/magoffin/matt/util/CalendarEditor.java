/* ===================================================================
 * CalendarEditor.java
 * 
 * Created Nov 25, 2006 9:13:40 PM
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
 * ===================================================================
 * $Id: CalendarEditor.java,v 1.5 2007/06/23 07:19:16 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.beans.PropertyEditorSupport;
import java.util.Calendar;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

/**
 * Property editor for Calendar instances.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.5 $ $Date: 2007/06/23 07:19:16 $
 */
public class CalendarEditor extends PropertyEditorSupport {

	private static final Logger LOG = Logger.getLogger(CalendarEditor.class);
	
	private ThreadSafeDateFormat dateFormat;
	private TimeZone zone;
	private boolean allowEmpty;
	
	/**
	 * Construct a new CalendarEditor instance.
	 * 
	 * @param dateFormat the date format for parsing date strings
	 * @param zone an optional TimeZone to use for parsing dates
	 * @param allowEmpty if <em>true</em> then empty strings are allowed
	 */
	public CalendarEditor(ThreadSafeDateFormat dateFormat, TimeZone zone, boolean allowEmpty) {
		this.dateFormat = dateFormat;
		this.zone = zone;
		this.allowEmpty = allowEmpty;
	}

	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (this.allowEmpty && !StringUtils.hasText(text)) {
			// Treat empty String as null value.
			setValue(null);
		} else {
			try {
				Calendar cal = this.zone == null 
					? dateFormat.parseCalendar(text)
					: dateFormat.parseCalendar(text, this.zone);
				setValue(cal);
			} catch ( RuntimeException e ) {
				if ( LOG.isDebugEnabled() ) {
					LOG.debug("Exception parsing date [" +text +"]: "
							+e.getMessage());
				}
				setValue(null);
			}
		}
	}

	@Override
	public String getAsText() {
		Calendar value = (Calendar)getValue();
		return (value != null ? this.dateFormat.format(value) : "");
	}

}
