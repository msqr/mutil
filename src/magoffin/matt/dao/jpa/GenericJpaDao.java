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
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import magoffin.matt.dao.GenericDao;
import magoffin.matt.dao.Identity;
import magoffin.matt.dao.SortDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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

	private final Class<T> type;
	private EntityManager em;
	private boolean refreshOnPersist = false;
	
	/** A class logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Constructor.
	 * 
	 * @param type the entity type
	 */
	public GenericJpaDao(Class<T> type) {
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

	/**
	 * Callback for extending classes to perform some action on an entity before
	 * it is persisted.
	 * 
	 * @param domainObject
	 *        the entity being persisted
	 */
	protected void prePersist(T domainObject) {
		// extending classes can override
	}

	/**
	 * Callback for extending classes to perform some action on an entity before
	 * it is updated.
	 * 
	 * @param domainObject
	 *        the entity being persisted
	 */
	protected void preUpdate(T domainObject) {
		// extending classes can override
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public PK store(T domainObject) {
		PK pk = getPK(domainObject);
		if ( pk != null ) {
			preUpdate(domainObject);
			domainObject = getEm().merge(domainObject);
		} else {
			prePersist(domainObject);
			getEm().persist(domainObject);
			if ( refreshOnPersist ) {
				getEm().refresh(domainObject);
			}
			pk = getPK(domainObject);
		}
		return pk;
	}

	@Override
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public T get(PK id) {
		if ( id == null ) {
			return null;
		}
		return getEm().find(type, id);
	}

	@Override
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public void delete(T domainObject) {
		getEm().remove(domainObject);
	}


	/**
	 * Find all of the supported entity.
	 * 
	 * @param sortDescriptors
	 *        optional sort descriptors
	 * @return list of results (never <em>null</em>)
	 */
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	protected List<T> getAll(List<SortDescriptor> sortDescriptors) {
		CriteriaBuilder cb = getEm().getCriteriaBuilder();
		CriteriaQuery<T> q = cb.createQuery(type);
		Root<T> root = q.from(type);
		q.select(root);

		applySortDescriptors(sortDescriptors, cb, q, root);

		TypedQuery<T> query = getEm().createQuery(q);
		return query.getResultList();
	}

	/**
	 * Apply a list of sort descriptors to a criteria query.
	 * 
	 * @param sortDescriptors
	 *        the sort descriptors (<em>null</em> allowed)
	 * @param cb
	 *        the criteria builder
	 * @param criteria
	 *        the criteria query
	 * @param root
	 *        the query root the sort is applied against
	 */
	protected void applySortDescriptors(List<SortDescriptor> sortDescriptors, CriteriaBuilder cb,
			CriteriaQuery<T> criteria, Root<T> root) {
		if ( sortDescriptors != null && sortDescriptors.size() > 0 ) {
			List<Order> ordering = new ArrayList<Order>(sortDescriptors.size());
			for ( SortDescriptor sort : sortDescriptors ) {
				Path<?> path = root.get(sort.getSortKey());
				Order order = (sort.isAscending() ? cb.asc(path) : cb.desc(path));
				ordering.add(order);
			}
			criteria.orderBy(ordering);
		}
	}

	protected void setEm(EntityManager em) {
		this.em = em;
	}

	public EntityManager getEm() {
		return em;
	}

	public Class<T> getType() {
		return type;
	}

	public boolean isRefreshOnPersist() {
		return refreshOnPersist;
	}

	public void setRefreshOnPersist(boolean refreshOnPersist) {
		this.refreshOnPersist = refreshOnPersist;
	}

}
