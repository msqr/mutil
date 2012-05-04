/* ===================================================================
 * Hibernate3EhCacheProvider.java
 * 
 * Created Apr 18, 2007 4:36:36 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: Hibernate3EhCacheProvider.java,v 1.2 2007/09/07 08:33:54 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.net.URL;
import java.util.Properties;
import net.sf.ehcache.CacheManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cache.Cache;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.CacheProvider;
import org.hibernate.cache.EhCache;
import org.hibernate.cache.EhCacheProvider;
import org.hibernate.cache.Timestamper;
import org.hibernate.cfg.Environment;
import org.hibernate.util.ConfigHelper;
import org.hibernate.util.StringHelper;

/**
 * Replacement for Hibernate's EhCacheProvier to better integrate with Spring.
 * 
 * <p>Source copied from Hibernate's {@link org.hibernate.cache.EhCacheProvider}
 * class and alterted to use {@link CacheManager#create()} in place of constructors.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2007/09/07 08:33:54 $
 */
@SuppressWarnings("deprecation")
public class Hibernate3EhCacheProvider implements CacheProvider {

	private static final Log log = LogFactory.getLog(EhCacheProvider.class);

	private CacheManager manager;

	/**
	 * Builds a Cache.
	 * <p>
	 * Even though this method provides properties, they are not used.
	 * Properties for EHCache are specified in the ehcache.xml file.
	 * Configuration will be read from ehcache.xml for a cache declaration where
	 * the name attribute matches the name parameter in this builder.
	 * 
	 * @param name
	 *            the name of the cache. Must match a cache configured in
	 *            ehcache.xml
	 * @param properties
	 *            not used
	 * @return a newly built cache will be built and initialised
	 * @throws CacheException
	 *             inter alia, if a cache of the same name already exists
	 */
	@Override
	public Cache buildCache(String name, Properties properties)
			throws CacheException {
		try {
			net.sf.ehcache.Cache cache = manager.getCache(name);
			if (cache == null) {
				log.warn("Could not find configuration [" + name
						+ "]; using defaults.");
				manager.addCache(name);
				cache = manager.getCache(name);
				log.debug("started EHCache region: " + name);
			}
			return new EhCache(cache);
		} catch (net.sf.ehcache.CacheException e) {
			throw new CacheException(e);
		}
	}

	@Override
	public long nextTimestamp() {
		return Timestamper.next();
	}

	/**
	 * Callback to perform any necessary initialization of the underlying cache
	 * implementation during SessionFactory construction.
	 * 
	 * @param properties
	 *            current configuration settings.
	 */
	@Override
	public void start(Properties properties) throws CacheException {
		if (manager != null) {
			log
					.warn("Attempt to restart an already started EhCacheProvider. Use sessionFactory.close() "
							+ " between repeated calls to buildSessionFactory. Using previously created EhCacheProvider."
							+ " If this behaviour is required, consider using net.sf.ehcache.hibernate.SingletonEhCacheProvider.");
			return;
		}
		try {
			String configurationResourceName = null;
			if (properties != null) {
				configurationResourceName = (String) properties
						.get(Environment.CACHE_PROVIDER_CONFIG);
			}
			if (StringHelper.isEmpty(configurationResourceName)) {
				manager = CacheManager.create();
			} else {
				URL url = loadResource(configurationResourceName);
				manager = CacheManager.create(url);
			}
		} catch (net.sf.ehcache.CacheException e) {
			// yukky! Don't you have subclasses for that!
			// race conditions can happen here
			if (e.getMessage().startsWith(
					"Cannot parseConfiguration CacheManager. Attempt to create a new instance of "
							+ "CacheManager using the diskStorePath")) {
				throw new CacheException(
						"Attempt to restart an already started EhCacheProvider. Use sessionFactory.close() "
								+ " between repeated calls to buildSessionFactory. Consider using net.sf.ehcache.hibernate.SingletonEhCacheProvider.",
						e);
			}
			throw e;
		}
	}

	private URL loadResource(String configurationResourceName) {
		URL url = ConfigHelper.locateConfig(configurationResourceName);
		if (log.isDebugEnabled()) {
			log.debug("Creating EhCacheProvider from a specified resource: "
					+ configurationResourceName + " Resolved to URL: " + url);
		}
		return url;
	}

	/**
	 * Callback to perform any necessary cleanup of the underlying cache
	 * implementation during SessionFactory.close().
	 */
	@Override
	public void stop() {
		if (manager != null) {
			manager.shutdown();
			manager = null;
		}
	}

	@Override
	public boolean isMinimalPutsEnabledByDefault() {
		return false;
	}

}
