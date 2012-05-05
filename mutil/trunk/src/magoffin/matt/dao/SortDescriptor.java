/* ===================================================================
 * SortDescriptor.java
 * 
 * Created May 4, 2012 5:05:57 PM
 * 
 * Copyright (c) 2012 Matt Magoffin.
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
 * $Id$
 * ===================================================================
 */

package magoffin.matt.dao;

import java.io.Serializable;

/**
 * API for a sorting specification.
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public interface SortDescriptor extends Serializable {

	/**
	 * Get the property name of the value to sort by.
	 * 
	 * @return the sort key
	 */
	String getSortKey();

	/**
	 * Return <em>true</em> if the sort should be in ascending order, otherwise
	 * the short should be in descending order.
	 * 
	 * @return <em>true</em> if the sort should be in ascending order
	 */
	boolean isAscending();

}
