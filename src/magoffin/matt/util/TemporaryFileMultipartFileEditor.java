/* ===================================================================
 * TemporaryFileMultipartFileEditor.java
 * 
 * Created Feb 15, 2005 6:53:16 PM
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
 * $Id: TemporaryFileMultipartFileEditor.java,v 1.3 2007/09/09 10:38:05 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.beans.PropertyEditorSupport;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

/**
 * Custom PropertyEditor for converting MultipartFiles to TemporaryFile instances.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.3 $ $Date: 2007/09/09 10:38:05 $
 */
public class TemporaryFileMultipartFileEditor extends PropertyEditorSupport {
	
	private boolean allowEmpty = false;
	
	/**
	 * Default constructor.
	 */
	public TemporaryFileMultipartFileEditor() {
		super();
	}
    
	/**
	 * Constsruct with settings.
	 * @param allowEmpty if <em>false</em> then throw an 
	 * IllegalArgumentException if the multipart file is empty
	 */
	public TemporaryFileMultipartFileEditor(boolean allowEmpty) {
		this.allowEmpty = allowEmpty;
	}
	
    @Override
	public void setValue(Object value) {
		if (value instanceof MultipartFile) {
			final MultipartFile multipartFile = (MultipartFile)value;
			if ( !allowEmpty && (multipartFile == null || multipartFile.isEmpty()) ) {
				throw new IllegalArgumentException("MultipartFile may not be null");
			}
			super.setValue(new TemporaryFile() {
				public InputStream getInputStream() throws IOException {
					return multipartFile.getInputStream();
				}
				public String getName() {
					return multipartFile.getOriginalFilename();
				}
				public String getContentType() {
					return multipartFile.getContentType();
				}
				public long getSize() {
					return multipartFile.getSize();
				}
			});
		}
	}

}
