/* ============================================================================
 * LuceneSearchResults.java
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
 * $Id: LuceneSearchResults.java,v 1.1 2006/07/10 04:22:34 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;

import java.util.List;
import java.util.Map;

/**
 * Low-level search  results API.
 * 
 * @author matt
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:22:34 $
 */
public interface LuceneSearchResults {

	/**
	 * Get the results.
	 * 
	 * <p>The results are Map instances where they key is the match 
	 * field and the value is an array of values for that field.</p>
	 * 
	 * @return List of results
	 */
	List<Map<String, String[]>> getResults();
	
	/**
	 * Get the total number of matches found.
	 * 
	 * @return total number of matches
	 */
	int getTotalMatches();

}
