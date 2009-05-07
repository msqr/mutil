/* ===================================================================
 * ClassnameListTag.java
 * 
 * Created May 8, 2009 8:50:12 AM
 * 
 * Copyright (c) 2009 Matt Magoffin.
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

package magoffin.matt.web.util;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.jstl.core.LoopTagSupport;

import magoffin.matt.util.ClassUtils;

/**
 * JSP looping tag for iterating over a list of class names within a specific set of packages.
 * 
 * <p>This tag will find all classes within the configured list of {@code packages}, optionally
 * limiting the returned set to those implementing any of the interfaces configured by the
 * {@code interfaces} property. Both {@code packages} and {@code interfaces} can accept
 * a single value or a comma-delimited list of values.</p>
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public class ClassnameListTag extends LoopTagSupport {
	
	private static final long serialVersionUID = 2635643444283875100L;

	private String interfaces;
	private String packages;
	
	private Set<String> classNames;
	private Iterator<String> itr;

	@Override
	protected boolean hasNext() throws JspTagException {
		return itr.hasNext();
	}

	@Override
	protected Object next() throws JspTagException {
		return itr.next();
	}

	@Override
	protected void prepare() throws JspTagException {
		String[] packageList = packages.split(",\\s*");
		Set<String> classNameSet = new LinkedHashSet<String>();
		Set<Class<?>> interfaceFilter = null;
		if ( interfaces != null && interfaces.trim().length() > 0 ) {
			interfaceFilter = new LinkedHashSet<Class<?>>();
			String[] interfaceList = interfaces.split(",\\s*");
			for ( String intf : interfaceList ) {
				try {
					interfaceFilter.add(
							Thread.currentThread().getContextClassLoader().loadClass(intf));
				} catch (ClassNotFoundException e) {
					throw new JspTagException(e);
				}
			}
		}
		for ( String pkg : packageList ) {
			try {
				ClassUtils.findClassNames(pkg, false, classNameSet, interfaceFilter);
			} catch (IOException e) {
				throw new JspTagException(e);
			}
		}
		
		// remove any interfaces from found class set
		if ( interfaceFilter != null ) {
			for ( Class<?> intf : interfaceFilter ) {
				if ( classNameSet.contains(intf.getName()) ) {
					classNameSet.remove(intf.getName());
				}
			}
		}
		
		this.classNames = classNameSet;
		this.itr = classNames.iterator();
	}

	/**
	 * @return the interfaces
	 */
	public String getInterfaces() {
		return interfaces;
	}

	/**
	 * @param interfaces the interfaces to set
	 */
	public void setInterfaces(String interfaces) {
		this.interfaces = interfaces;
	}

	/**
	 * @return the packages
	 */
	public String getPackages() {
		return packages;
	}

	/**
	 * @param packages the packages to set
	 */
	public void setPackages(String packages) {
		this.packages = packages;
	}

}
