/* ===================================================================
 * ClassUtils.java
 * 
 * Created May 8, 2009 9:19:25 AM
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

package magoffin.matt.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Utility methods dealing with classes.
 * 
 * <p>
 * Adapted from the MMM project's ReflectionUtilImpl (http://m-m-m.sourceforge.net/).
 * </p>
 * 
 * @author matt
 * @version $Revision$ $Date$
 */
public class ClassUtils {

	/**
	 * This method checks and transforms the filename of a potential
	 * {@link Class} given by <code>fileName</code>.
	 * 
	 * @param fileName
	 *            is the filename.
	 * @return the according Java {@link Class#getName() class-name} for the
	 *         given <code>fileName</code> if it is a class-file that is no
	 *         anonymous {@link Class}, else <code>null</code>.
	 */
	private static String fixClassName(String fileName) {

		if (fileName.endsWith(".class")) {
			// remove extension (".class".length() == 6)
			String nameWithoutExtension = fileName.substring(0, fileName
					.length() - 6);
			// handle inner classes...
			/*
			 * int lastDollar = nameWithoutExtension.lastIndexOf('$'); if
			 * (lastDollar > 0) { char innerClassStart =
			 * nameWithoutExtension.charAt(lastDollar + 1); if ((innerClassStart
			 * >= '0') && (innerClassStart <= '9')) { // ignore anonymous class
			 * } else { return nameWithoutExtension.replace('$', '.'); } } else
			 * { return nameWithoutExtension; }
			 */
			return nameWithoutExtension;
		}
		return null;
	}

	/**
	 * This method finds the recursively scans the given
	 * <code>packageDirectory</code> for {@link Class} files and adds their
	 * according Java names to the given <code>classSet</code>.
	 * 
	 * @param packageDirectory
	 *            is the directory representing the {@link Package}.
	 * @param classSet
	 *            is where to add the Java {@link Class}-names to.
	 * @param qualifiedNameBuilder
	 *            is a {@link StringBuilder} containing the qualified prefix
	 *            (the {@link Package} with a trailing dot).
	 * @param qualifiedNamePrefixLength
	 *            the length of the prefix used to rest the string-builder after
	 *            reuse.
	 * @param interfaceFilter
	 *            an optional Set of Class objects that each found class name
	 *            must implement in order to be added to {@code classSet}
	 */
	private static void findClassNamesRecursive(File packageDirectory,
			Set<String> classSet, StringBuilder qualifiedNameBuilder,
			int qualifiedNamePrefixLength, Set<Class<?>> interfaceFilter) {

		for (File childFile : packageDirectory.listFiles()) {
			String fileName = childFile.getName();
			if (childFile.isDirectory()) {
				qualifiedNameBuilder.setLength(qualifiedNamePrefixLength);
				StringBuilder subBuilder = new StringBuilder(
						qualifiedNameBuilder);
				subBuilder.append(fileName);
				subBuilder.append('.');
				findClassNamesRecursive(childFile, classSet, subBuilder,
						subBuilder.length(), interfaceFilter);
			} else {
				String simpleClassName = fixClassName(fileName);
				if (simpleClassName != null) {
					qualifiedNameBuilder.setLength(qualifiedNamePrefixLength);
					qualifiedNameBuilder.append(simpleClassName);
					addClass(qualifiedNameBuilder.toString(), classSet, interfaceFilter);
					classSet.add(qualifiedNameBuilder.toString());
				}
			}
		}
	}

	/**
	 * Find all available class names within a given package.
	 * 
	 * <p>Use the {@code interfaceFilter} to filter out all classes that do not
	 * implement one of the provided interfaces.</p>
	 * 
	 * @param packageName the package name to search in
	 * @param includeSubPackages <em>true</em> to recursively search sub-packages
	 * @param classSet a Set to add all found class names to
	 * @param interfaceFilter an optional filter of Class objects each found class
	 * name must implement
	 * @throws IOException if any IO error occurs
	 */
	public static void findClassNames(String packageName, boolean includeSubPackages,
			Set<String> classSet, Set<Class<?>> interfaceFilter) throws IOException {
		ClassLoader classLoader = Thread.currentThread()
				.getContextClassLoader();
		String path = packageName.replace('.', '/');
		String pathWithPrefix = path + '/';
		Enumeration<URL> urls = classLoader.getResources(path);
		StringBuilder qualifiedNameBuilder = new StringBuilder(packageName);
		qualifiedNameBuilder.append('.');
		int qualifiedNamePrefixLength = qualifiedNameBuilder.length();
		while (urls.hasMoreElements()) {
			URL packageUrl = urls.nextElement();
			String urlString = URLDecoder.decode(packageUrl.getFile(), "UTF-8");
			String protocol = packageUrl.getProtocol().toLowerCase();
			if ("file".equals(protocol)) {
				File packageDirectory = new File(urlString);
				if (packageDirectory.isDirectory()) {
					if (includeSubPackages) {
						findClassNamesRecursive(packageDirectory, classSet,
								qualifiedNameBuilder, qualifiedNamePrefixLength,
								interfaceFilter);
					} else {
						for (String fileName : packageDirectory.list()) {
							String simpleClassName = fixClassName(fileName);
							if (simpleClassName != null) {
								qualifiedNameBuilder
										.setLength(qualifiedNamePrefixLength);
								qualifiedNameBuilder.append(simpleClassName);
								addClass(qualifiedNameBuilder.toString(), classSet, interfaceFilter);
							}
						}
					}
				}
			} else if ("jar".equals(protocol)) {
				// somehow the connection has no close method and can NOT be
				// disposed
				JarURLConnection connection = (JarURLConnection) packageUrl
						.openConnection();
				JarFile jarFile = connection.getJarFile();
				Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
				while (jarEntryEnumeration.hasMoreElements()) {
					JarEntry jarEntry = jarEntryEnumeration.nextElement();
					String absoluteFileName = jarEntry.getName();
					if (absoluteFileName.endsWith(".class")) {
						if (absoluteFileName.startsWith("/")) {
							absoluteFileName = absoluteFileName.substring(1);
						}
						// special treatment for WAR files...
						// "WEB-INF/lib/" entries should be opened directly in
						// contained jar
						if (absoluteFileName.startsWith("WEB-INF/classes/")) {
							// "WEB-INF/classes/".length() == 16
							absoluteFileName = absoluteFileName.substring(16);
						}
						boolean accept = true;
						if (absoluteFileName.startsWith(pathWithPrefix)) {
							String qualifiedName = absoluteFileName.replace(
									'/', '.');
							if (!includeSubPackages) {
								int index = absoluteFileName.indexOf('/',
										qualifiedNamePrefixLength + 1);
								if (index != -1) {
									accept = false;
								}
							}
							if (accept) {
								String className = fixClassName(qualifiedName);
								if (className != null) {
									addClass(className, classSet, interfaceFilter);
								}
							}
						}
					}
				}
			} else {
				// TODO: unknown protocol - log this?
			}
		}
	}

	/**
	 * Add {@code className} to the {@code classSet} if it implements any of the classes 
	 * specified in {@code interfaceFilter}.
	 * 
	 * <p>If {@code interfaceFilter} is not provided, this method simply adds {@code className}
	 * to {@code classSet}. If {@code interfaceFilter} is provided, the {@code className} class
	 * is loaded using the current thread's context ClassLoader, and if that Class is assignable
	 * by any of the Class objects in {@code interfaceFilter} it will be added to the 
	 * {@code classSet}; otherwise {@code className} will not be added.</p>
	 * 
	 * @param className a fully qualified class name
	 * @param classSet a Set of class names to add {@code className} to
	 * @param interfaceFilter if non-null, a Set of interfaces to filter by
	 */
	private static void addClass(String className, Set<String> classSet, Set<Class<?>> interfaceFilter) {
		if ( classSet.contains(className) ) {
			return;
		}
		if ( interfaceFilter == null || interfaceFilter.size() < 1 ) {
			classSet.add(className);
		}
		Class<?> clazz;
		try {
			clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
		} catch ( ClassNotFoundException e ) {
			// ignore this class
			return;
		}
		for ( Class<?> intf : interfaceFilter ) {
			if ( intf.isAssignableFrom(clazz) ) {
				classSet.add(className);
			}
		}
	}

}
