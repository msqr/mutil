/* ============================================================================
 * IndexStatusCallback.java
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
 * $Id: IndexStatusCallback.java,v 1.1 2006/07/10 04:58:06 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;


/**
 * API for obtaining the status of an asynchronous indexing process.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:58:06 $
 */
public interface IndexStatusCallback {

	/**
	 * This method will block until the indexing operation is complete.
	 */
	void waitUntilDone();
	
	/**
	 * Get the indexing results.
	 * @return index results
	 */
	IndexResults getIndexResults();
	
}
