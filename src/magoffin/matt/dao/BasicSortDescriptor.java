/* ===================================================================
 * BasicSortDescriptor.java
 * 
 * Created May 4, 2012 5:09:33 PM
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

/**
 * Basic implementation of {@link SortDescriptor}.
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public class BasicSortDescriptor implements SortDescriptor {

	private static final long serialVersionUID = 3862928227009604323L;

	private final String sortKey;
	private final boolean ascending;

	/**
	 * Construct with a sort key, in ascending order.
	 * 
	 * @param sortKey
	 *        the sort key
	 */
	public BasicSortDescriptor(String sortKey) {
		this(sortKey, true);
	}

	/**
	 * Construct with values.
	 * 
	 * @param sortKey
	 *        the sort key
	 * @param ascending
	 *        <em>true</em> for ascending, <em>false</em> for descending
	 */
	public BasicSortDescriptor(String sortKey, boolean ascending) {
		super();
		this.sortKey = sortKey;
		this.ascending = ascending;
	}

	@Override
	public String getSortKey() {
		return sortKey;
	}

	@Override
	public boolean isAscending() {
		return ascending;
	}

	@Override
	public String toString() {
		return "BasicSortDescriptor{sortKey" + (ascending ? " ASC" : " DESC") + '}';
	}

}
