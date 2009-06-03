/* ============================================================================
 * RegexpSplitFilter.java
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
 * $Id: RegexpSplitFilter.java,v 1.1 2006/07/10 04:22:34 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;

/**
 * Filter for splitting an individual token into multiple tokens, split
 * on a regular expression.
 * 
 * <p>This can be used to tokenize email addresses as words, for example.</p>
 * 
 * @author matt.magoffin
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:22:34 $
 */
public class RegexpSplitFilter extends TokenFilter {
	
	private Pattern pattern = Pattern.compile("[@.]");
	private int start = 0;
	private Queue<String> splitQueue = new LinkedList<String>();

	/**
	 * @param input the token stream input
	 */
	public RegexpSplitFilter(TokenStream input) {
		super(input);
	}
	
	/**
	 * Construct with a custom regexp.
	 * @param input the token stream input
	 * @param regexp the regular expression
	 */
	public RegexpSplitFilter(TokenStream input, String regexp) {
		super(input);
		this.pattern = Pattern.compile(regexp);
	}

	@Override
	public Token next() throws IOException {
		if ( splitQueue.size() > 0 ) {
			String next = splitQueue.remove();
			this.start++;
			char[] nextChar = next.toCharArray();
			Token t = new Token(nextChar, 0, nextChar.length, this.start, this.start + next.length());
			this.start += next.length();
			return t;
		}

		Token reusableToken = new Token();
	    Token t = input.next(reusableToken);
	    if (t == null) {
	    	return null;
	    }
	    
	    String text = t.term();
	    Matcher matcher = pattern.matcher(text);
	    if ( !matcher.find() ) {
	    	return t;
	    }
	    
	    // found pattern
	    String[] split = pattern.split(text);
	    for ( int i = 1; i < split.length; i++ ) {
	    	splitQueue.offer(split[i]);
	    }
	    this.start = t.startOffset() + split[0].length();
	    char[] splitChar = split[0].toCharArray();
	    return new Token(splitChar, 0, splitChar.length, t.startOffset(), this.start);
	}

}
