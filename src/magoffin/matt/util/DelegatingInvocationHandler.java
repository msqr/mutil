/* ===================================================================
 * DelegatingInvocationHandler.java
 * 
 * Created Sep 23, 2004 4:39:12 PM
 * 
 * Copyright (c) 2004 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: DelegatingInvocationHandler.java,v 1.2 2007/07/12 09:09:55 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.List;

/**
 * InvocationHandler that passes all method calls to a delegate object.
 * 
 * <p>This class can be used to pass method calls from a parent interface 
 * to an extending one.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2007/07/12 09:09:55 $
 */
public class DelegatingInvocationHandler implements InvocationHandler {
	
	private final Object myDelegate;
	
	/**
	 * Delegate all method calls to another object.
	 * @param delegate the delegate
	 */
	public DelegatingInvocationHandler(Object delegate) {
		myDelegate = delegate;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
	throws Throwable {
		Method delegateMethod = myDelegate.getClass().getMethod(method.getName(),
				method.getParameterTypes());
		return delegateMethod.invoke(myDelegate,args);
	}
	
	/**
	 * Create a DelegatingInvocationHandler-proxied instance of an object.
	 * 
	 * @param delegate the delegate
	 * @param proxyInterface the proxy interface
	 * @return the proxy instance
	 */
	public static Object wrapObject( Object delegate, Class<?> proxyInterface) {
		Class<?>[] interfaces = delegate.getClass().getInterfaces();
		Class<?>[] proxyInterfaces = new Class[interfaces.length+1];
		proxyInterfaces[0] = proxyInterface;
		System.arraycopy(interfaces,0,proxyInterfaces,1,interfaces.length);
		Object proxy = Proxy.newProxyInstance(
				delegate.getClass().getClassLoader(),
				proxyInterfaces,
				new DelegatingInvocationHandler(delegate));
		return proxy;
	}

	/**
	 * Create proxied instances of all objects in one List and add the proxy
	 * objects to another List.
	 * 
	 * <p>This method creates a proxy object for each object in <code>originals</code>
	 * that implements all interfaces of that object in addition to the 
	 * <code>proxyInterface</code> interface. The proxy objects are then added to 
	 * the <code>proxies</code> List. Is this too devious?</p>
	 * 
	 * @param originals a list of original objects
	 * @param proxies a list to place all wrapped Recipe objects into
	 * @param proxyInterface the main interface for the proxy objects to implement
	 */
	public static void wrapObjects( List<?> originals, List<Object> proxies, Class<?> proxyInterface) {
		if ( originals == proxies ) {
			throw new IllegalArgumentException("The originals List can not be the same as the proxies List");
		}
		for ( Iterator<?> itr = originals.iterator(); itr.hasNext(); ) {
			Object delegate = itr.next();
			Object proxy = wrapObject(delegate, proxyInterface);
			proxies.add(proxy);
		}
	}

}
