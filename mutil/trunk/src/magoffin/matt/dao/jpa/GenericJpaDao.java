/* ===================================================================
 * GenericJpaDao.java
 * 
 * Created May 4, 2012 3:45:34 PM
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

import java.io.Serializable;
import javax.persistence.EntityManager;
import magoffin.matt.dao.GenericDao;
import magoffin.matt.dao.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GenericDao base implementation for JPA 2.
 *
 * @param <T> the domain objec type
 * @param <PK> the primary key type
 * @author matt
 * @version $Revision$ $Date$
 */
public abstract class GenericJpaDao<T, PK extends Serializable> implements
		GenericDao<T, PK> {

	private final EntityManager em;
	private final Class<T> type;
	
	/** A class logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor.
	 * 
	 * @param em the EntityManager
	 * @param type the entity type
	 */
	public GenericJpaDao(EntityManager em, Class<T> type) {
		this.em = em;
		this.type = type;
	}

	/**
	 * Get the primary key for a domain object.
	 * 
	 * <p>
	 * This method is only called if T does not implement {@link Identity}, in
	 * which case {@link Identity#getId()} is used automatically
	 * </p>
	 * 
	 * @param domainObject
	 *        the domain object
	 * @return the primary key, or <em>null</em> if not persistant
	 */
	protected PK getPrimaryKey(T domainObject) {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	private PK getPK(T domainObject) {
		return (domainObject instanceof Identity ? ((Identity<PK>) domainObject).getId()
				: getPrimaryKey(domainObject));
	}

	@Override
	public PK store(T domainObject) {
		PK pk = getPK(domainObject);
		if ( pk != null ) {
			domainObject = getEm().merge(domainObject);
		} else {
			getEm().persist(domainObject);
			pk = getPK(domainObject);
		}
		return pk;
	}

	@Override
	public T get(PK id) {
		if ( id == null ) {
			return null;
		}
		return getEm().find(type, id);
	}

	@Override
	public void delete(T domainObject) {
		getEm().remove(domainObject);
	}

	public EntityManager getEm() {
		return em;
	}

	public Class<T> getType() {
		return type;
	}

}
