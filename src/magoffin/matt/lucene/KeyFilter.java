/* ============================================================================
 * KeyFilter.java
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
 * $Id: KeyFilter.java,v 1.1 2006/07/10 04:22:34 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;

import java.io.IOException;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

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
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:22:34 $
 */
public class KeyFilter extends TokenFilter {

	/** The default maximum length of the resulting key string. */
	public static final int DEFAULT_MAX_LENGTH = 3;
	
	/* Configurable fields. */
	
	private int maxLength = DEFAULT_MAX_LENGTH;
	
	/* Internal fields. */
	
	/**
	 * Construct a KeyFilter.
	 * @param input the TokenStream to use as input
	 */
	public KeyFilter(TokenStream input) {
		super(input);
	}

	/**
	 * Construct a KeyFilter.
	 * @param input the TokenStream to use as input
	 * @param maxLength the max length value
	 */
	public KeyFilter(TokenStream input, int maxLength) {
		super(input);
		this.maxLength = maxLength;
	}

	/* (non-Javadoc)
	 * @see org.apache.lucene.analysis.TokenStream#next()
	 */
	@Override
	public Token next() throws IOException {
		Token token = input.next();
		
		if ( token == null ) {
			return null;
		}
		
		String key = token.termText();
		if ( key.length() > maxLength ) {
			key = key.substring(0, maxLength);
		}
		
		return new Token(key, token.startOffset(), token.startOffset()+key.length());
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
		this.maxLength = maxLength;
	}
	
}
