/* ============================================================================
 * BasicIndexConfig.java
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
 * $Id: BasicIndexConfig.java,v 1.1 2006/07/10 04:22:34 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;

import magoffin.matt.lucene.LucenePlugin.LuceneIndexConfig;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.LogMergePolicy;

/**
 * Basic implementation of the LuceneIndexConfig API.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:22:34 $
 */
public class BasicIndexConfig implements LuceneIndexConfig {
	
	private int minMergeDocs = IndexWriter.DEFAULT_MAX_BUFFERED_DOCS;
	
	private int mergeFactor = LogMergePolicy.DEFAULT_MERGE_FACTOR;

	/**
	 * Default constructor.
	 */
	public BasicIndexConfig() {
		super();
	}
	
	/**
	 * Construct with specific merge parameters.
	 * @param minMergeDocs the min merge docs
	 * @param mergeFactor the merge factor
	 */
	public BasicIndexConfig(int minMergeDocs, int mergeFactor) {
		this.minMergeDocs = minMergeDocs;
		this.mergeFactor = mergeFactor;
	}

	@Override
	public int getMinMergeDocs() {
		return minMergeDocs;
	}

	@Override
	public int getMergeFactor() {
		return mergeFactor;
	}

	/**
	 * @param mergeFactor The mergeFactor to set.
	 */
	public void setMergeFactor(int mergeFactor) {
		this.mergeFactor = mergeFactor;
	}

	/**
	 * @param minMergeDocs The minMergeDocs to set.
	 */
	public void setMinMergeDocs(int minMergeDocs) {
		this.minMergeDocs = minMergeDocs;
	}

}
