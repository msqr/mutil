/* ===================================================================
 * BasicSearchResults.java
 * 
 * Created Sep 5, 2006 1:00:05 PM
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
 * $Id: BasicSearchResults.java,v 1.2 2007/03/07 03:45:00 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;

import java.util.Collections;
import java.util.List;

import magoffin.matt.lucene.SearchMatch;
import magoffin.matt.lucene.SearchResults;

/**
 * Basic implementation of SearchResults.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2007/03/07 03:45:00 $
 */
public class BasicSearchResults implements SearchResults {
	
	private List<SearchMatch> matches;
	private int totalMatches;
	
	/**
	 * Default constructor.
	 */
	public BasicSearchResults() {
		matches = Collections.emptyList();
		totalMatches = 0;
	}

	/**
	 * Constructor.
	 * 
	 * @param matches the matches
	 * @param totalMatches the total matches
	 */
	public BasicSearchResults(List<SearchMatch> matches, int totalMatches) {
		this.matches = matches;
		this.totalMatches = totalMatches;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.SearchResults#getMatches()
	 */
	public List<SearchMatch> getMatches() {
		return matches;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.SearchResults#getTotalMatches()
	 */
	public int getTotalMatches() {
		return totalMatches;
	}
	
	/**
	 * @param matches the matches to set
	 */
	public void setMatches(List<SearchMatch> matches) {
		this.matches = matches;
	}
	
	/**
	 * @param totalMatches the totalMatches to set
	 */
	public void setTotalMatches(int totalMatches) {
		this.totalMatches = totalMatches;
	}

}
