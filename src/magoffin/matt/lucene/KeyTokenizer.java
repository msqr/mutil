/* ===================================================================
 * KeyTokenizer.java
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
 * $Id: KeyTokenizer.java,v 1.1 2006/07/10 04:22:34 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;

/**
 * Filter a stream into a single token for use as a single key.
 * 
 * <p>This class is not thread-safe.</p>
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl>
 *   <dt>maxLength</dt>
 *   <dd>The maximum length of the generated keys. Defaults to <b>3</b>.</dd>
 *   
 *   <dt>trim</dt>
 *   <dd>If <em>true</em> then trim whitespace from input before generating key.
 *   Otherwise leave the input as-is. Defaults to <em>true</em>.</dd>
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:22:34 $
 */

public final class KeyTokenizer extends Tokenizer {
	
	/** The default maximum length of the resulting key string. */
	public static final int DEFAULT_MAX_LENGTH = 3;
	
	/* Configurable fields. */
	
	private int maxLength = DEFAULT_MAX_LENGTH;
	private boolean trim = true;
	
	/* Internal fields. */
	
	private char[] buffer = new char[maxLength];
	private boolean complete = false;
	
	/**
	 * Construct a KeyTokenizer.
	 * @param input the reader to tokenize
	 */
	public KeyTokenizer(Reader input) {
		super(input);
	}
	
	/**
	 * Construct with a maximum length.
	 * @param input the reader to tokenize
	 * @param maxLength the maximum length of the key to generate
	 */
	public KeyTokenizer(Reader input, int maxLength) {
		super(input);
		setMaxLength(maxLength);
	}

	@Override
	public Token next() throws IOException {
		if ( complete ) {
			return null;
		}
		
		int numRead = input.read(buffer);
		String key = "";
		if ( numRead > 0 ) {
			if ( !trim ) {
				key = new String(buffer, 0, numRead);
			} else {
				if ( buffer.length == 1 ) {
					while ( Character.isWhitespace(buffer[0]) ) {
						numRead = input.read(buffer);
						if ( numRead < 1 ) {
							break;
						}
					}
				} else {
					if ( numRead < buffer.length ) {
						Arrays.fill(buffer, numRead, buffer.length, ' ');
					}
					int i = 0;
					while ( i < buffer.length && numRead > 0 ) {
						int start = i;
						for ( ; i < buffer.length && Character.isWhitespace(buffer[i]); i++ ) {
							// skip
						}
						if ( i > start ) {
							// found whitespace at beginning, so discard them and 
							// read more from stream
							System.arraycopy(buffer, i, buffer, start, buffer.length - i);
							numRead = input.read(buffer, buffer.length - i, i);
						} else {
							break;
						}
					}
				}
				key = new String(buffer).trim();
				if ( key.length() < 1 ) {
					return null;
				}
			}
		}
		complete = true;
		return new Token(key, 0, numRead - 1);
	}
	
	/* Injector methods below. */
	
	/**
	 * Get the maxLength.
	 * @return the maxLength
	 */
	public int getMaxLength() {
		return maxLength;
	}
	
	/**
	 * Set the maxLength.
	 * @param maxLength the maxLength to set
	 */
	public void setMaxLength(int maxLength) {
		if ( maxLength != this.maxLength ) {
			this.buffer = new char[maxLength];
			this.maxLength = maxLength;
		}
	}
	
	/**
	 * @return Returns the trim.
	 */
	public boolean isTrim() {
		return trim;
	}
	
	/**
	 * @param trim The trim to set.
	 */
	public void setTrim(boolean trim) {
		this.trim = trim;
	}
	
}
