/* ===================================================================
 * BaseAnalyzer.java
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
 * $Id: BaseAnalyzer.java,v 1.1 2006/07/10 04:22:34 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;

/**
 * Base class for Lucene Analyzer implementations.
 * 
 * <p>The default case for fields is to apply the following:</p>
 * 
 * <ol>
 *   <li>StandardTokenizer</li>
 *   <li>StandardFilter</li>
 *   <li>LowerCaseFilter</li>
 * </ol>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:22:34 $
 */
public class BaseAnalyzer extends Analyzer {
	
	/** The field key for general text. */
	public static final char FIELD_GENERAL_TEXT = 'G';
	
	@Override
	public TokenStream tokenStream(String field, Reader reader) {
		char fieldChar = field.charAt(0);
		TokenStream result = null;
		switch (fieldChar) {
			case FIELD_GENERAL_TEXT:
				result = new StandardTokenizer(reader);
				result = new StandardFilter(result);
				result = new LowerCaseFilter(result);
				result = new RegexpSplitFilter(result, "[@.]"); // tokenize emails
				result = new StopFilter(result, StopAnalyzer.ENGLISH_STOP_WORDS);
				// result = new PorterStemFilter(result);
				break;
				
			default:
				result = new StandardTokenizer(reader);
				result = new StandardFilter(result);
				result = new LowerCaseFilter(result);
				break;
		}
		return result;
	}

}
