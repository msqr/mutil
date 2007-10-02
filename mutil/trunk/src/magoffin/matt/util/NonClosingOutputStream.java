/* ===================================================================
 * NonClosingOutputStream.java
 * 
 * Created Dec 24, 2006 6:32:28 PM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: NonClosingOutputStream.java,v 1.1 2006/12/24 05:36:25 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.io.IOException;
import java.io.OutputStream;


/**
 * InputStream that does not close the stream when {@link #close()} is called.
 * 
 * <p>This can be useful in dealing with zip streams, where you don't want 
 * the stream closed after reading in an entry but some library calls automatically
 * close the input stream.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/12/24 05:36:25 $
 */
public class NonClosingOutputStream extends OutputStream {
	
	private OutputStream delegate;
	
	/**
	 * Construct.
	 * @param delegate the OutputStream to wrap
	 */
	public NonClosingOutputStream(OutputStream delegate) {
		this.delegate = delegate;
	}

	@Override
	public void close() throws IOException {
		// ignore
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	@Override
	public void flush() throws IOException {
		delegate.flush();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	@Override
	public void write(byte[] arg0, int arg1, int arg2) throws IOException {
		delegate.write(arg0, arg1, arg2);
	}

	@Override
	public void write(byte[] arg0) throws IOException {
		delegate.write(arg0);
	}

	@Override
	public void write(int arg0) throws IOException {
		delegate.write(arg0);
	}

}
