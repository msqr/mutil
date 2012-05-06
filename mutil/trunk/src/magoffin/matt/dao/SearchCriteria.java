/* ===================================================================
 * SearchCriteria.java
 * 
 * Created May 7, 2012 10:06:46 AM
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
import java.util.List;
import java.util.Map;

/**
 * API for a search criteria.
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public interface SearchCriteria extends Serializable {

	/**
	 * Get a map of search filters, where keys represent search keys and values
	 * represent their associated search criteria.
	 * 
	 * @return map of search filters
	 */
	Map<String, ?> getSearchFilters();

	/**
	 * Get a list of orderings to apply to the search results.
	 * 
	 * @return list of search descriptors
	 */
	List<SortDescriptor> getSortDescriptors();

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

}
