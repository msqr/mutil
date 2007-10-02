/* ===================================================================
 * TimeZoneEditor.java
 * 
 * Created May 17, 2007 1:47:00 PM
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
 * $Id: TimeZoneEditor.java,v 1.1 2007/05/17 03:56:10 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.beans.PropertyEditorSupport;
import java.util.TimeZone;

import org.springframework.util.StringUtils;

/**
 * Property editor for TimeZone instances.
 * 
 * <p>This sets/gets TimeZone instances by their ID values, as defined by
 * {@link TimeZone#getID()}.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2007/05/17 03:56:10 $
 */
public class TimeZoneEditor extends PropertyEditorSupport {

	private boolean allowEmpty;
	
	/**
	 * Construct a TimeZoneEditor instance.
	 * @param allowEmpty if <em>true</em> allow null values
	 */
	public TimeZoneEditor(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
	}
	
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		if (this.allowEmpty && !StringUtils.hasText(text)) {
			// Treat empty String as null value.
			setValue(null);
		} else {
			TimeZone zone = StringUtils.hasText(text)
				? TimeZone.getTimeZone(text)
				: TimeZone.getDefault();
			setValue(zone);
		}
	}

	@Override
	public String getAsText() {
		TimeZone value = (TimeZone)getValue();
		return (value != null ? value.getID() : "");
	}

}
