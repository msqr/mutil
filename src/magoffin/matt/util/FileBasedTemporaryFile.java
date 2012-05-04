/* ===================================================================
 * FileBasedTemporaryFile.java
 * 
 * Created Dec 2, 2007 5:12:52 PM
 * 
 * Copyright (c) 2007 Matt Magoffin.
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * {@link TemporaryFile} wrapper around a simple {@link File} object.
 *
 * @author matt
 * @version $Revision$ $Date$
 */
public class FileBasedTemporaryFile implements TemporaryFile {
	
	private final String contentType;
	private final File file;
	
	/**
	 * Construct from a File.
	 * 
	 * @param file the file
	 * @param contentType the MIME type
	 */
	public FileBasedTemporaryFile(File file, String contentType) {
		this.file = file;
		this.contentType = contentType;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new BufferedInputStream(new FileInputStream(file));
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public long getSize() {
		return file.length();
	}

}
