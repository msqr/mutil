/* ===================================================================
 * BasicBatchResult.java
 * 
 * Created Oct 7, 2006 11:42:31 AM
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
 * $Id: BasicBatchResult.java,v 1.1 2006/10/07 04:49:58 matt Exp $
 * ===================================================================
 */

package magoffin.matt.dao;

import magoffin.matt.dao.BatchableDao.BatchResult;

/**
 * Basic implementation of {@link BatchResult}.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/10/07 04:49:58 $
 */
public class BasicBatchResult implements BatchResult {
	
	private int numProcessed;
	
	/**
	 * Default constructor.
	 */
	public BasicBatchResult() {
		this(0);
	}
	
	/**
	 * Construct with values.
	 * @param numProcessed the number of items processed
	 */
	public BasicBatchResult(int numProcessed) {
		this.numProcessed = numProcessed;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.dao.BatchableDao.BatchResult#numProcessed()
	 */
	public int numProcessed() {
		return numProcessed;
	}
	
	/**
	 * @return the numProcessed
	 */
	public int getNumProcessed() {
		return numProcessed;
	}
	
	/**
	 * @param numProcessed the numProcessed to set
	 */
	public void setNumProcessed(int numProcessed) {
		this.numProcessed = numProcessed;
	}

}
