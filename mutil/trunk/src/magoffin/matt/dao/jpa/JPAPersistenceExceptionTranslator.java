/* ===================================================================
 * JPAPersistenceExceptionTranslator.java
 * 
 * Created May 4, 2012 5:26:12 PM
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

import java.sql.SQLIntegrityConstraintViolationException;
import javax.persistence.PersistenceException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.support.PersistenceExceptionTranslator;

/**
 * Helper {@link PersistenceExceptionTranslator} for JPA constraint violations.
 * 
 * <p>
 * Found that EclipseLink constraint violations were not getting translated into
 * Spring {@link org.springframework.dao.DataAccessException} exceptions. This
 * translator serves to fill in that gap.
 * </p>
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public class JPAPersistenceExceptionTranslator implements PersistenceExceptionTranslator {

	@SuppressWarnings("unchecked")
	private <T extends Throwable> T findCause(Throwable t, Class<T> type) {
		Throwable root = t;
		while ( !(type.isAssignableFrom(root.getClass())) && root.getCause() != null ) {
			root = root.getCause();
		}
		return (type.isAssignableFrom(root.getClass()) ? (T) root : null);
	}

	@Override
	public DataAccessException translateExceptionIfPossible(RuntimeException ex) {
		if ( PersistenceException.class.equals(ex.getClass()) ) {
			SQLIntegrityConstraintViolationException root = findCause(ex,
					SQLIntegrityConstraintViolationException.class);
			if ( root != null ) {
				return new DataIntegrityViolationException(root.getMessage(), root);
			}
		}
		return null;
	}

}
