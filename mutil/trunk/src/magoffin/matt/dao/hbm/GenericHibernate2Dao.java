/* ===================================================================
 * GenericHibernate2Dao.java
 * 
 * Created Aug 28, 2006 2:29:23 PM
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
 * $Id: GenericHibernate2Dao.java,v 1.1 2006/08/28 05:57:22 matt Exp $
 * ===================================================================
 */

package magoffin.matt.dao.hbm;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import magoffin.matt.dao.GenericDao;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;

import org.apache.log4j.Logger;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate.HibernateCallback;
import org.springframework.orm.hibernate.support.HibernateDaoSupport;


/**
 * GenericDao base implementation for Hibernate 2.
 * 
 * @param <T> the domain objec type
 * @param <PK> the primary key type
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/08/28 05:57:22 $
 */
public abstract class GenericHibernate2Dao<T, PK extends Serializable> 
extends HibernateDaoSupport implements GenericDao<T,PK> {

	private Class<? extends T> type;
	
	/** A class logger. */
	protected final Logger log = Logger.getLogger(getClass());

	/**
	 * Constructor.
	 * @param type
	 */
	public GenericHibernate2Dao(Class<? extends T> type) {
		this.type = type;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.dao.GenericDao#delete(T)
	 */
	public void delete(T domainObject) {
		getHibernateTemplate().delete(domainObject);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.dao.GenericDao#get(PK)
	 */
	@SuppressWarnings("unchecked")
	public T get(PK id) {
		try {
			return (T)getHibernateTemplate().load(type,id);
		} catch ( ObjectRetrievalFailureException e ) {
			log.warn("Object not found by primary key [" 
					+id +"]: " +e.getMessage());
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.dao.GenericDao#store(T)
	 */
	public final PK store(T domainObject) {
		PK primaryKey = getPrimaryKey(domainObject);
		if ( primaryKey == null ) {
			return save(domainObject);
		}
		update(domainObject);
		return primaryKey;
	}
	
	/**
	 * Get the primary  key for a domain object.
	 * @param domainObject the domain object
	 * @return the primary key, or <em>null</em> if not persistant
	 */
	protected abstract PK getPrimaryKey(T domainObject);
	
	/**
	 * Persist a new domain object.
	 * @param domainObject the domain object to persist
	 * @return the domain object's primary key
	 */
	@SuppressWarnings("unchecked")
	protected PK save(T domainObject) {
		return (PK)getHibernateTemplate().save(domainObject);
	}
	
	/**
	 * Update a persisted domain object.
	 * @param domainObject the domain object to update
	 */
	protected void update(T domainObject) {
		getHibernateTemplate().update(domainObject);
	}
	
	/**
	 * Find a list of persistant objects by a named query.
	 * 
	 * @param queryName the name of the Hibernate query to execute
	 * @param parameters the parameters to pass to the query
	 * @return the list of results, or an empty list if none found
	 */
	@SuppressWarnings("unchecked")
	protected List<T> findByNamedQuery(String queryName, Object[] parameters) {
		return getHibernateTemplate().findByNamedQuery(queryName, 
				parameters);
	}
	
	/**
	 * Find a list of persistant objects by a named query with 
	 * pagination support.
	 * 
	 * @param queryName the name of the Hibernate query to execute
	 * @param parameters the parameters to pass to the query
	 * @param page the page, starting at 0 (zero), to return
	 * @param pageSize the number of results per page
	 * @return the list of results, or an empty list if none found
	 */
	@SuppressWarnings("unchecked")
	protected List<T> findByNamedQuery(final String queryName, final Object[] parameters, 
			final int page, final int pageSize) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.getNamedQuery(queryName);
				if ( parameters != null ) {
					for ( int i = 0; i < parameters.length; i++ ) {
						query.setParameter(i, parameters[i]);
					}
				}
				query.setFirstResult(pageSize * page); 
				query.setMaxResults(pageSize); 
				return query.list();
			}
		});
	}
	
}
