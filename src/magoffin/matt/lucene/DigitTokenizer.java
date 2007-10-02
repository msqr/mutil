/* ============================================================================
 * DigitTokenizer.java
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
 * $Id: DigitTokenizer.java,v 1.1 2006/07/10 04:22:34 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;
import org.springframework.util.FileCopyUtils;

/**
 * Filter a stream into a single token of only digits.
 * 
 * <p>This class is not thread-safe. It is also geared for small 
 * input streams, as it uses a regular expression to replace non-digits 
 * with an empty string.</p>
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl>
 *   <dt>maxLength</dt>
 *   <dd>The maximum number of digits to output. When trimming, numbers
 *   are trimmed from the <em>left</em>. Defaults to <code>-1</code> 
 *   (for no maximum).</dd>
 * </dl>
 * 
 * @author matt.magoffin
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:22:34 $
 */
public class DigitTokenizer extends Tokenizer {
	
	private boolean complete = false;
	private int maxLength = -1;
	
	/**
	 * Constructor with a Reader.
	 * @param input the input reader
	 */
	public DigitTokenizer(Reader input) {
		super(input);
	}
	
	/**
	 * Construct with a max length.
	 * @param input the input reader
	 * @param maxLength the max length
	 */
	public DigitTokenizer(Reader input, int maxLength) {
		super(input);
		setMaxLength(maxLength);
	}

	@Override
	public Token next() throws IOException {
		if ( complete ) {
			return null;
		}
		
		// read in entire string
		StringWriter out = new StringWriter();
		FileCopyUtils.copy(this.input, out);
		int end = out.getBuffer().length();
		String numbers = out.toString().replaceAll("\\D", "");
		
		if ( maxLength > 0 && numbers.length() > maxLength ) {
			numbers = numbers.substring(numbers.length() - maxLength);
		}
		complete = true;
		return new Token(numbers, 0, end);
	}

	
	/**
	 * @return Returns the maxLength.
	 */
	public int getMaxLength() {
		return maxLength;
	}

	
	/**
	 * @param maxLength The maxLength to set.
	 */
	public void setMaxLength(int maxLength) {
		this.maxLength = maxLength < 1 ? -1 : maxLength;
	}

}
