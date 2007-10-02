/* ===================================================================
 * GenericDao.java
 * 
 * Created Jul 2, 2006 9:29:38 AM
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
 * $Id: GenericDao.java,v 1.1 2006/07/10 04:22:35 matt Exp $
 * ===================================================================
 */

package magoffin.matt.dao;

import java.io.Serializable;

/**
 * Generic DAO API.
 * 
 * <p>Based in part on 
 * http://www-128.ibm.com/developerworks/java/library/j-genericdao.html.</p>
 * 
 * @param <T> the domain objec type
 * @param <PK> the primary key type
 * @author matt.magoffin
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:22:35 $
 */
public interface GenericDao <T, PK extends Serializable> {

    /**
     * Persist the domainObject object into database, 
     * creating or updating as appropriate.
     * 
     * @param domainObject the domain object so store
     * @return the primary key of the stored object
     */
    PK store(T domainObject);

    /** 
     * Get a persisted domain object by its primary key.
     * @param id the primary key to retrieve
     * @return the domain object
     */
    T get(PK id);

    /** 
     * Remove an object from persistent storage in the database.
     * @param domainObject the domain object to delete
     */
    void delete(T domainObject);
}
