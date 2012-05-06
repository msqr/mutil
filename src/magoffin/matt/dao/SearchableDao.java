/* ===================================================================
 * SearchableDao.java
 * 
 * Created May 7, 2012 10:04:41 AM
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
 * API for searachable DAO.
 * 
 * @param <T>
 *        the domain objec type
 * @param <PK>
 *        the primary key type
 * @param <R>
 *        the search result type
 * @author matt
 * @version $Revision$ $Date$
 */
public interface SearchableDao<T extends Entity<PK>, R extends SearchResult<PK>, PK extends Serializable> {

	/**
	 * Search by criteria.
	 * 
	 * @param criteria
	 *        the criteria to search for
	 * @return the search results, never <em>null</em>
	 */
	SearchResults<R> findByCriteria(SearchCriteria criteria);

}
