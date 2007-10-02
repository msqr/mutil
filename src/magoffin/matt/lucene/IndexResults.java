/* ============================================================================
 * IndexResults.java
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
 * $Id: IndexResults.java,v 1.1 2006/07/10 04:22:34 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;

import java.io.Serializable;
import java.util.Map;

/**
 * Results status object for index operations.
 * 
 * @author matt
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:22:34 $
 */
public interface IndexResults {

	/**
	 * Get the total number of items processed during indexing.
	 * @return the number of items processed
	 */
	int getNumProcessed();
	
	/**
	 * Get the total number of items actually indexed during processing.
	 * @return the number of items indexed
	 */
	int getNumIndexed();
	
	/**
	 * Return true if the indexing is complete.
	 * @return boolean
	 */
	boolean isFinished();
	
	/**
	 * Get a Map of item IDs for the items and error messages for
	 * items that failed to be indexed.
	 * @return Map of item IDs to error messages
	 */
	Map<? extends Serializable, String> getErrors();
	
}
