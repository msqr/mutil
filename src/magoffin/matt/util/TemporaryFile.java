/* ===================================================================
 * TemporaryFile.java
 * 
 * Created Feb 15, 2005 6:51:40 PM
 * 
 * Copyright (c) 2005 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: TemporaryFile.java,v 1.2 2006/12/17 07:28:49 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * API for a temporary file instance.
 * 
 * <p>This API exists so we can use a JavaBeans PropertyEditor to edit
 * file properties.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2006/12/17 07:28:49 $
 */
public interface TemporaryFile {
	
	/**
	 * Get an InputStream to the temporary file.
	 * @return the InputStream to the contents of the file
	 * @throws IOException if there is an error opening the stream
	 */
	InputStream getInputStream() throws IOException;
	
	/**
	 * Get a name for the associated file.
	 * @return the file name (may be <em>null</em>)
	 */
	String getName();
	
	/**
	 * Get the content type.
	 * @return content type
	 */
	String getContentType();
	
	/**
	 * Get the uploaded file size.
	 * @return the file size, in bytes
	 */
	long getSize();
	
}
