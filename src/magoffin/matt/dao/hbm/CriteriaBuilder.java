/* ===================================================================
 * CriteriaBuilder.java
 * 
 * Created Oct 7, 2006 5:34:20 PM
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
 * $Id: CriteriaBuilder.java,v 1.1 2006/10/07 04:49:58 matt Exp $
 * ===================================================================
 */

package magoffin.matt.dao.hbm;

import org.hibernate.Criteria;

/**
 * API for building a Hibernate Criteria.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/10/07 04:49:58 $
 */
public interface CriteriaBuilder {

	/**
	 * Build up a Criteria instance.
	 * 
	 * @param criteria the criteria to start with
	 */
	void buildCriteria(Criteria criteria);
	
}
