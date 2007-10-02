/* ============================================================================
 * SearchCriteria.java
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
 * $Id: SearchCriteria.java,v 1.4 2007/03/07 03:45:00 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;


/**
 * Marker interface for a search criteria object.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.4 $ $Date: 2007/03/07 03:45:00 $
 */
public interface SearchCriteria {

	/**
	 * Get the maximum desired results, or unlimited results
	 * if less than 1.
	 * @return the max results
	 */
	int getMaxResults();
	
	/**
	 * Get a desired pagination size, or unlimited page size
	 * if less than 1.
	 * @return the page size
	 */
	int getPageSize();
	
	/**
	 * Get the desired page of results.
	 * @return the page
	 */
	int getPage();
	
	/**
	 * Return <em>true</em> if should return only a count,
	 * not the actual results.
	 * @return boolean
	 */
	boolean isCountOnly();
}
