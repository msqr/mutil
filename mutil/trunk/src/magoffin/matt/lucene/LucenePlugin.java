/* ============================================================================
 * LucenePlugin.java
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
 * $Id: LucenePlugin.java,v 1.3 2007/07/12 09:09:55 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;

import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

/**
 * API for handling Lucene index operations.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.3 $ $Date: 2007/07/12 09:09:55 $
 */
public interface LucenePlugin {
	
	/**
	 * Lucene index configuration API for plugins.
	 */
	public interface LuceneIndexConfig {
		
		/**
		 * Get the minimum merge docs (max buffered docs).
		 * @return min merge docs
		 */
		int getMinMergeDocs();
		
		/**
		 * Get the desired merge factor.
		 * @return merge factor
		 */
		int getMergeFactor();
		
	}
	
	/**
	 * Initialize the plugin.
	 * @param luceneService the host LuceneService
	 * @param indexEventListenersSet a read-only list of IndexListener objects
	 * to receive index events
	 * @return the index configuration parameters
	 */
	LuceneIndexConfig init(LuceneService luceneService, 
			Set<IndexListener> indexEventListenersSet);
	
	/**
	 * Get the String supported by the plugin.
	 * @return the index type
	 */
	String getIndexType();
	
	/**
	 * Get the Lucene Analyzer used to parse query strings for this index.
	 * @return the analyzer
	 */
	Analyzer getAnalyzer();

	/**
	 * Reindex the entire set of data.
	 * @return the index results
	 */
	IndexResults reindex();
	
	/**
	 * Reindex a set of data that matches a criteria.
	 * @param criteria the criteria of the data to reindex.
	 * @return the index results
	 */
	IndexResults reindex(SearchCriteria criteria);
	
	/**
	 * Index a single domain object by it's ID.
	 * @param objectId the unique ID of the domain object to index
	 * @param writer the IndexWriter to use
	 */
	void index(Object objectId, IndexWriter writer);
	
	/**
	 * Index a single domain object.
	 * @param object the domain object to index
	 * @param writer the IndexWriter to use
	 */
	void indexObject(Object object, IndexWriter writer);
	
	/**
	 * Get the domain object ID from a domain object.
	 * @param object the domain object
	 * @return the ID, or <em>null</em> if not available
	 */
	Object getIdForObject(Object object);
	
	/**
	 * Build a SearchMatch object from a Document object.
	 * 
	 * @param doc the Lucene Document
	 * @return a SearchMatch object
	 */
	SearchMatch build(Document doc);
	
	/**
	 * Index a set of data.
	 * @param data the set of data to index
	 */
	void index(Iterable<?> data);
	
	/**
	 * Perform a search against the index and return the results.
	 * @param criteria the search criteria
	 * @return the search results (should never be <em>null</em>)
	 */
	SearchResults find(SearchCriteria criteria);
	
	/**
	 * Perform a generalized search against the index and return the results.
	 * @param criteria the search criteria
	 * @return the search results (should never be <em>null</em>)
	 */
	List<SearchMatch> search(SearchCriteria criteria);
	
	/**
	 * Get the native query for a given criteria.
	 * @param criteria the criteria
	 * @return the native query
	 */
	Object getNativeQuery(SearchCriteria criteria);
}
