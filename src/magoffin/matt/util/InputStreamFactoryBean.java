/* ===================================================================
 * InputStreamFactoryBean.java
 * 
 * Created Jun 28, 2006 9:45:49 PM
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
 * $Id: InputStreamFactoryBean.java,v 1.2 2007/07/12 09:09:55 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.io.InputStream;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.core.io.Resource;

/**
 * Factory bean for InputStream instances.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.2 $ $Date: 2007/07/12 09:09:55 $
 */
public class InputStreamFactoryBean implements FactoryBean {
	
	private Resource resource;

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObject()
	 */
	public Object getObject() throws Exception {
		return resource.getInputStream();
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#getObjectType()
	 */
	@SuppressWarnings("unchecked")
	public Class getObjectType() {
		return InputStream.class;
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.FactoryBean#isSingleton()
	 */
	public boolean isSingleton() {
		return false;
	}

	/**
	 * @return Returns the resource.
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @param resource The resource to set.
	 */
	public void setResource(Resource resource) {
		this.resource = resource;
	}

}
