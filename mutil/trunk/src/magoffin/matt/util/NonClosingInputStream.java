/* ===================================================================
 * NonClosingInputStream.java
 * 
 * Created Dec 24, 2006 6:33:18 PM
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
 * $Id: NonClosingInputStream.java,v 1.1 2006/12/24 05:36:25 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.io.IOException;
import java.io.InputStream;

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
public class NonClosingInputStream extends InputStream {

	private InputStream delegate;

	/**
	 * Construct.
	 * @param delegate the InputStream to wrap
	 */
	public NonClosingInputStream(InputStream delegate) {
		this.delegate = delegate;
	}

	@Override
	public void close() throws IOException {
		// ignore
	}

	@Override
	public int read() throws IOException {
		return delegate.read();
	}

	@Override
	public int available() throws IOException {
		return delegate.available();
	}

	@Override
	public boolean equals(Object obj) {
		return delegate.equals(obj);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public void mark(int readlimit) {
		delegate.mark(readlimit);
	}

	@Override
	public boolean markSupported() {
		return delegate.markSupported();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		return delegate.read(b, off, len);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return delegate.read(b);
	}

	@Override
	public void reset() throws IOException {
		delegate.reset();
	}

	@Override
	public long skip(long n) throws IOException {
		return delegate.skip(n);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}
	
}
