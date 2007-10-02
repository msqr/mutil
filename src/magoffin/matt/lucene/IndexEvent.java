/* ===================================================================
 * IndexEvent.java
 * 
 * Created May 29, 2006 7:07:58 PM
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
 * $Id: IndexEvent.java,v 1.2 2007/03/05 05:41:02 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;

import java.util.EventObject;

/**
 * An event object for index events.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2007/03/05 05:41:02 $
 */
public class IndexEvent extends EventObject {

	private static final long serialVersionUID = -8706292811871468025L;

	/**
	 * A type of index event.
	 */
	public static enum EventType {
		
		/** Query. */
		QUERY,
		
		/** Delete. */
		DELETE,
		
		/** Update. */
		UPDATE;
		
	}
	
	private final EventType type;
	private final String indexType;
	
	/**
	 * Construct an IndexEvent.
	 * 
	 * @param source the source of the event
	 * @param type the type of index event
	 * @param indexType the index type the event is for
	 */
	public IndexEvent(Object source, EventType type, String indexType) {
		super(source);
		if ( type == null ) {
			throw new IllegalArgumentException("The EventType parameter is required");
		}
		if ( indexType == null ) {
			throw new IllegalArgumentException("The indexType parameter is required");
		}
		this.type = type;
		this.indexType = indexType;
	}
	
	/**
	 * @return the indexType
	 */
	public String getIndexType() {
		return indexType;
	}

	/**
	 * @return the type
	 */
	public EventType getType() {
		return type;
	}

}
