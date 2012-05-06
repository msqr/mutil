/* ===================================================================
 * SearchResults.java
 * 
 * Created May 7, 2012 10:08:08 AM
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
 * API for search results.
 * 
 * @param <T>
 *        the domain object type
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public interface SearchResults<T extends SearchResult<?>> extends Serializable {

	/**
	 * Get the total number of results that matched the search criteria.
	 * 
	 * <p>
	 * This is the overall number of matches for the query. If not known, this
	 * value should return <em>null</em>.
	 * </p>
	 * 
	 * @return the total number of matches
	 */
	Integer getTotalResultCount();

	/**
	 * Get the number of results returned, which should equal the number of
	 * objects returned by the {@link #getResults()} iterator.
	 * 
	 * @return the number of returned results
	 */
	int getReturnedResultCount();

	/**
	 * Get the returned results starting offset.
	 * 
	 * @return the returned results starting offset
	 */
	Integer getStartingOffset();

	/**
	 * Get the maximum results returned at one time.
	 * 
	 * @return the maximum number of results returned at one time
	 */
	Integer getMaximumResultCount();

	/**
	 * Get the result objects, as an {@link Iterable}.
	 * 
	 * @return the results iterable
	 */
	Iterable<T> getResults();

}
