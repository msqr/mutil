/* ===================================================================
 * GenericHibernateDao.java
 * 
 * Created Jul 2, 2006 9:36:26 AM
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
 * $Id: GenericHibernateDao.java,v 1.9 2007/01/28 00:09:36 matt Exp $
 * ===================================================================
 */

package magoffin.matt.dao.hbm;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import magoffin.matt.dao.GenericDao;
import magoffin.matt.dao.BatchableDao.BatchCallback;
import magoffin.matt.dao.BatchableDao.BatchCallbackResult;
import magoffin.matt.dao.BatchableDao.BatchOptions;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * GenericDao base implementation for Hibernate 3.
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl>
 *   <dt>updateMode</dt>
 *   <dd>The type of Hibernate update to perform. Defaults 
 *   to <code>UPDATE</code>.</dd>
 * </dl>
 * 
 * @param <T> the domain objec type
 * @param <PK> the primary key type
 * @author matt.magoffin
 * @version $Revision: 1.9 $ $Date: 2007/01/28 00:09:36 $
 */
public abstract class GenericHibernateDao<T, PK extends Serializable> 
extends HibernateDaoSupport implements GenericDao<T,PK> {

	/**
	 * Constants for the style of Hibernate update to perform .
	 */
	public enum UpdateMode {
		
		/** Always perform an update. */
		UPDATE,
		
		/** Perform merge if domain object is not a persistent object. */
		CONDITIONALLY_MERGE,
		
		/** Always perform merge. */
		MERGE;
	}
	
	/** The default batch flush count. */
	public static final int DEFAULT_BATCH_FLUSH_COUNT = 25;
	
	private Class<? extends T> type;
	private UpdateMode updateMode = UpdateMode.UPDATE;
	private int batchFlushCount = DEFAULT_BATCH_FLUSH_COUNT;
	
	/** A class logger. */
	protected final Logger log = Logger.getLogger(getClass());

	/**
	 * Constructor.
	 * @param type
	 */
	public GenericHibernateDao(Class<? extends T> type) {
		this.type = type;
	}
	
	/**
	 * Get the domain object class type.
	 * @return the type
	 */
	protected Class<? extends T> getType() {
		return this.type;
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
	@SuppressWarnings("unchecked")
	protected void update(T domainObject) {
		boolean merge = false;
		if ( updateMode == UpdateMode.MERGE ) {
			merge = true;
		} else if ( updateMode == UpdateMode.CONDITIONALLY_MERGE ) {
			T persistenObject = (T)getHibernateTemplate().get(type, 
					getPrimaryKey(domainObject));
			if ( persistenObject != domainObject ) {
				merge = true;
			}
		}
		if ( merge ) {
			getHibernateTemplate().merge(domainObject);
		} else {
			getHibernateTemplate().update(domainObject);
		}
	}
	
	/**
	 * Find a list of persistant objects by a named query.
	 * 
	 * @param queryName the name of the Hibernate query to execute
	 * @return the list of results, or an empty list if none found
	 */
	@SuppressWarnings("unchecked")
	protected List<T> findByNamedQuery(String queryName) {
		return getHibernateTemplate().findByNamedQuery(queryName);
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
	 * Find a list of persistant objects by a named query.
	 * 
	 * @param queryName the name of the Hibernate query to execute
	 * @param parameters the parameters to pass to the query
	 * @return the list of results, or an empty list if none found
	 */
	protected List<T> findByNamedQuery(final String queryName, 
			final Map<String, Object> parameters) {
		return findByNamedQuery(queryName, parameters, 0, 0);
	}
	
	/**
	 * Find a list of persistant objects by a named query with 
	 * pagination support.
	 * 
	 * @param queryName the name of the Hibernate query to execute
	 * @param parameters the parameters to pass to the query
	 * @param page the page, starting at 0 (zero), to return
	 * @param pageSize the number of results per page, or 0 for no limit
	 * @return the list of results, or an empty list if none found
	 */
	@SuppressWarnings("unchecked")
	protected List<T> findByNamedQuery(final String queryName, 
			final Map<String, Object> parameters, 
			final int page, final int pageSize) {
		return getHibernateTemplate().executeFind(new HibernateCallback() {
			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Query query = session.getNamedQuery(queryName);
				if ( parameters != null ) {
					for ( String parameterName : parameters.keySet() ) {
						query.setParameter(parameterName, parameters.get(parameterName));
					}
				}
				if ( pageSize > 0 ) {
					query.setFirstResult(pageSize * page); 
					query.setMaxResults(pageSize);
				}
				return query.list();
			}
		});
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
	
	/**
	 * Execute a batch callback using a named query.
	 * 
	 * @param queryName the named query name
	 * @param parameters the named parameters to pass to the query
	 * @param callback the callback
	 * @return the number of items processed
	 */
	protected Integer executeNamedQueryBatchCallback(final String queryName, 
			final Map<String, Object> parameters, final BatchCallback<T> callback) {
		Integer numProcessed = (Integer)getHibernateTemplate().execute(
			new HibernateCallback() {
				@SuppressWarnings("unchecked")
				public Object doInHibernate(Session session) 
				throws HibernateException, SQLException {
					Query q = session.getNamedQuery(queryName);
					if ( parameters != null ) {
						for ( String paramName : parameters.keySet() ) {
							q.setParameter(paramName, parameters.get(paramName));
						}
					}
					q.setCacheMode(CacheMode.IGNORE);
					ScrollableResults items = q.scroll(ScrollMode.FORWARD_ONLY);
					int count = 0;
					
					OUTER:
					while ( items.next() ) {
						T item = (T)items.get(0);
						BatchCallbackResult action = callback.handle(item);
						switch ( action ) {
							case DELETE:
								session.delete(item);
								break;
							
							case UPDATE:
							case UPDATE_STOP:
								store(item);
								if ( action == BatchCallbackResult.UPDATE_STOP ) {
									break OUTER;
								}
								break;
								
							case STOP:
								break OUTER;
								
							case CONTINUE:
								// nothing to do
								break;
						}
						if ( ++count % batchFlushCount == 0 ) {
							session.flush();
							session.clear();
						}
					}
					
					return count;
				}
			}, true);
		return numProcessed;
	}
	
	/**
	 * Execute a batch callback against a StatelessSession using a named query.
	 * 
	 * <p>The DELETE, UPDATE, and UPDATE_STOP {@link BatchCallbackResult}
	 * values are not supported in this operation, and will throw an 
	 * <code>UnsupportedOperationException</code> if returned by the 
	 * {@link BatchCallback} instance passed to this method.</p>
	 * 
	 * @param criteriaBuilder the criteria builder
	 * @param callback the callback
	 * @param options the options
	 * @return the number of items processed
	 */
	@SuppressWarnings("unchecked")
	protected Integer executeStatelessCriteriaBatchCallback(
			final CriteriaBuilder criteriaBuilder, 
			final BatchCallback<T> callback, 
			final BatchOptions options) {
		StatelessSession session = getHibernateTemplate().getSessionFactory()
			.openStatelessSession();
		Transaction tx = session.beginTransaction();
		try {
			Criteria criteria = session.createCriteria(getType());
			criteria.setFetchSize(options.getBatchSize());
			criteriaBuilder.buildCriteria(criteria);
			ScrollableResults items = criteria.scroll(
					ScrollMode.FORWARD_ONLY);
			int count = 0;
			
			OUTER:
			while ( items.next() ) {
				T item = (T)items.get(0);
				BatchCallbackResult action = callback.handle(item);
				switch ( action ) {
					case DELETE:
					case UPDATE:
					case UPDATE_STOP:
						throw new UnsupportedOperationException(
								"Action "+action +" not possible during "
								+options.getMode() +" mode batch processing");
						
					case STOP:
						break OUTER;
						
					case CONTINUE:
						// nothing to do
						break;
				}
			}			
			tx.commit();
			return count;
		} catch ( RuntimeException e ) {
			tx.rollback();
			throw e;
		} finally {
			if ( session != null ) {
				session.close();
			}
		}
	}
	
	/**
	 * Execute a batch callback against a normal Hibernate Session using a named query.
	 * 
	 * @param criteriaBuilder the criteria builder
	 * @param callback the callback
	 * @param options the options
	 * @return the number of items processed
	 */
	protected Integer executeLiveCriteriaBatchCallback(
			final CriteriaBuilder criteriaBuilder, 
			final BatchCallback<T> callback, 
			final BatchOptions options) {
		Integer numProcessed = (Integer)getHibernateTemplate().execute(
				new HibernateCallback() {
					@SuppressWarnings("unchecked")
					public Object doInHibernate(Session session) 
					throws HibernateException, SQLException {
						Criteria criteria = session.createCriteria(type);
						criteria.setFetchSize(options.getBatchSize());
						criteriaBuilder.buildCriteria(criteria);
						ScrollableResults items = criteria.scroll(
								ScrollMode.FORWARD_ONLY);
						int count = 0;
						
						OUTER:
						while ( items.next() ) {
							T item = (T)items.get(0);
							BatchCallbackResult action = callback.handle(item);
							switch ( action ) {
								case DELETE:
									session.delete(item);
									break;
								
								case UPDATE:
								case UPDATE_STOP:
									store(item);
									if ( action == BatchCallbackResult.UPDATE_STOP ) {
										break OUTER;
									}
									break;
									
								case STOP:
									break OUTER;
									
								case CONTINUE:
									// nothing to do
									break;
							}
							if ( ++count % 20 == 0 ) {
								session.flush();
								session.clear();
							}
						}
						
						return count;
					}
				}, true);
		return numProcessed;
	}

	/**
	 * @return the updateMode
	 */
	public UpdateMode getUpdateMode() {
		return updateMode;
	}
	
	/**
	 * @param updateMode the updateMode to set
	 */
	public void setUpdateMode(UpdateMode updateMode) {
		this.updateMode = updateMode;
	}
	
	/**
	 * @return the batchFlushCount
	 */
	protected int getBatchFlushCount() {
		return batchFlushCount;
	}
	
	/**
	 * @param batchFlushCount the batchFlushCount to set
	 */
	protected void setBatchFlushCount(int batchFlushCount) {
		this.batchFlushCount = batchFlushCount;
	}
	
}
