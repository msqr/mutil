/* ===================================================================
 * BaseEntityt.java
 * 
 * Created May 5, 2012 7:22:04 PM
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

package magoffin.matt.dao.jpa;

import javax.persistence.MappedSuperclass;
import magoffin.matt.dao.Entity;
import magoffin.matt.dao.Identity;
import magoffin.matt.dao.SearchResult;

/**
 * Base entity class for Long primary key values.
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
@MappedSuperclass
public abstract class BaseEntityLong implements Entity<Long>, SearchResult<Long> {

	@Override
	public int compareTo(Identity<Long> arg0) {
		Long otherId = arg0.getId();
		if ( otherId == null ) {
			return 1;
		}
		if ( getId() == null ) {
			return -1;
		}
		return getId().compareTo(otherId);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if ( this == obj ) {
			return true;
		}
		if ( obj == null ) {
			return false;
		}
		if ( getClass() != obj.getClass() ) {
			return false;
		}
		BaseEntityLong other = (BaseEntityLong) obj;
		if ( getId() == null ) {
			if ( other.getId() != null ) {
				return false;
			}
		} else if ( !getId().equals(other.getId()) ) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "{id=" + getId() + '}';
	}

}
