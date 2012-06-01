/* ===================================================================
 * JPASchemaGenerator.java
 * 
 * Created May 3, 2012 4:29:24 PM
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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

/**
 * Generate the JPA schema by instantiating the EntityManager, which
 * we assume is configured to emit the database DDL on startup.
 * 
 * <p>This class looks for a Spring configuration file either passed as the 
 * command line argument, or by default {@code classpath:META-INF/ddl-gen.xml}.
 * The Spring configuration must define a {@link EntityManagerFactory} bean.
 * This class will use that bean to create an {@link EntityManager} instance.
 * Using the JPA properties specific to the JPA provider, the EntityManagerFactory
 * should be configured to emit the JPA model as a DDL script.</p>
 *
 * @author matt
 * @version $Revision$ $Date$
 */
public class JPASchemaGenerator {

	/** The prefix to use for a classpath Spring configuration file. */
	public static final String CLASSPATH_CONFIG_PREFIX = "classpath:";
	
	/** The default location of the Spring configuration file. */
	public static final String DEFAULT_CONFIG_LOCATION = "file:lib/ddl-gen.xml";
	
	private static final Logger LOG = LoggerFactory.getLogger(JPASchemaGenerator.class);
	
	private ApplicationContext appContext;

	/**
	 * Default constructor.
	 * 
	 * @param configLocation the spring configuration
	 */
	public JPASchemaGenerator(String configLocation) {
		super();
		loadSpring(configLocation);
	}

	private void loadSpring(String configLocation) {
		if ( configLocation == null ) {
			configLocation = DEFAULT_CONFIG_LOCATION;
		}
		LOG.info("Loading application config from [{}]...", configLocation);
		if ( configLocation.startsWith(CLASSPATH_CONFIG_PREFIX) ) {
			appContext = new ClassPathXmlApplicationContext(
					configLocation.substring(CLASSPATH_CONFIG_PREFIX.length()));
		} else {
			appContext = new FileSystemXmlApplicationContext(configLocation);
		}
		LOG.debug("Loaded Spring config [{}]", configLocation);
	}
	
	/**
	 * Output the schema.
	 */
	public void outputSchema() {
		EntityManagerFactory emf = appContext.getBean(EntityManagerFactory.class);
		EntityManager em = emf.createEntityManager(); // assume this generates the DDL
		em.close();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String configLoc = (args.length > 0 ? args[0] : null);
		JPASchemaGenerator gen = new JPASchemaGenerator(configLoc);
		gen.outputSchema();
	}

}
