/* ============================================================================
 * LuceneService.java
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
 * $Id: LuceneService.java,v 1.4 2007/03/10 02:54:52 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;


import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocCollector;

/**
 * API for Lucene plug-in implementation to use for 
 * accessing Lucene functionality.
 * 
 * @author matt.magoffin
 * @version $Revision: 1.4 $ $Date: 2007/03/10 02:54:52 $
 */
public interface LuceneService {
	
	/** The Lucene field/value delimiter. */
	char FIELD_DELIM = ':';
	
	/** The Lucene query parser boolean "and" operation. */
	String AND = "AND";
	
	/** The Lucene query parser boolean "or" operation. */
	String OR = "OR";

	/** The index date format for day-precision dates. */
	String INDEX_DATE_FORMAT_DAY_PATTERN = "yyyyMMdd";
	
	/** The index date format for month-precision dates .*/
	String INDEX_DATE_FORMAT_MONTH_PATTERN = "yyyyMM";
	
	/** Flag for synchronous searches on {@link #doIndexQueryOp(String, Query, boolean, IndexQueryOp)}. */
	boolean SYNCHRONOUS = true;

	/** Flag for asynchronous searches on {@link #doIndexQueryOp(String, Query, boolean, IndexQueryOp)}. */
	boolean ASYNCHRONOUS = false;
	
	/** 
	 * The value assigned to fields that need to be indexed but don't have 
	 * a value assigned to them (as Lucene can throw an exception on queries
	 * that require this term).
	 */
	String UNKNOWN_REQUIRED_TEXT_VALUE = "UNKNOWN";
	
	/** The value assigned to required terms that are not specified. */
	String UNSPECIFIED_REQUIRED_SEARCH_TERM = "UnspecifiedSearchTerm";
	
	/**
	 * API to perform an exclusive Lucene index write operation.
	 */
	public static interface IndexWriterOp {
		/** 
		 * Perform an exclusive IndexWriter operation.
		 * 
		 * @param type the String
		 * @param writer the IndexWriter
		 */
		void doWriterOp(String type, IndexWriter writer);
	}
	
	/**
	 * API to perform a Lucene index read operation.
	 */
	public static interface IndexReaderOp {
		/** 
		 * Perform an exclusive IndexReader operation.
		 * 
		 * @param type the String
		 * @param reader the IndexReader
		 */
		void doReaderOp(String type, IndexReader reader);
	}
	
	/**
	 * API to perform a Lucene index search operaion.
	 */
	public static interface IndexQueryOp {
		
		/**
		 * Execute a search query.
		 * @param type the String
		 * @param searcher the IndexSearcher
		 * @param query the Lucene query executed
		 * @param hits the query hits
		 * @throws IOException if an error occurs
		 */
		void doSearcherOp(String type, IndexSearcher searcher, 
				Query query, TopDocCollector hits) throws IOException;
	}
	
	/**
	 * API to perform a Lucene index search operaion.
	 */
	public static interface IndexSearcherOp {
		
		/**
		 * Perform an IndexSearcher operation.
		 * @param type the String
		 * @param searcher the IndexSearcher
		 * @throws IOException if an error occurs
		 */
		void doSearcherOp(String type, IndexSearcher searcher)
		throws IOException;
	}
	
	/**
	 * API for handling low-level search results.
	 * 
	 * @author matt
	 * @version $Revision: 1.4 $ $Date: 2007/03/10 02:54:52 $
	 */
	public static interface LuceneSearchResultHandler {
		
		/**
		 * Set the total number of matches found.
		 * @param numMatches the number of matches found
		 */
		void setTotalMatches(int numMatches);
		
		/**
		 * Process a single search result match.
		 * 
		 * @param match the match data, in the form of a Map
		 * @return <em>true</em> to continue processing, or <em>false</em> to stop
		 */
		boolean processMatch(Map<String, String[]> match);
		
	}
	
	/**
	 * Format a Date into an index-suitable date with day accuracy.
	 * @param date the date to format
	 * @return the formatted date string
	 */
	String formatDateToDay(Date date);
	
	/**
	 * Format a Date into an index-suitable date with day accuracy.
	 * @param date the date to format
	 * @param zone the time zone to format the date as
	 * @return the formatted date string
	 */
	String formatDateToDay(Date date, TimeZone zone);
	
	/**
	 * Format a Date into an index-suitable date with month accuracy.
	 * @param date the date to format
	 * @return the formatted date string
	 */
	String formatDateToMonth(Date date);
	
	/**
	 * Format a Date into an index-suitable date with month accuracy.
	 * @param date the date to format
	 * @param zone the time zone to format the date as
	 * @return the formatted date string
	 */
	String formatDateToMonth(Date date, TimeZone zone);
	
	/**
	 * Parse a date string in the form returned by {@link #formatDateToDay(Date)}
	 * or {@link #formatDateToMonth(Date)}.
	 * @param dateStr the string to parse
	 * @return the parsed date
	 */
	Date parseDate(String dateStr);
	
	/**
	 * Parse a date string in the form returned by {@link #formatDateToDay(Date)}
	 * or {@link #formatDateToMonth(Date)}.
	 * @param dateStr the string to parse
	 * @param zone the time zone to parse with
	 * @return the parsed date
	 */
	Date parseDate(String dateStr, TimeZone zone);
	
	/**
	 * Get the TimeZone used by the index.
	 * @return TimeZone
	 */
	TimeZone getIndexTimeZone();
	
	/**
	 * Index an object based on that object's unique ID.
	 * 
	 * @param type the index type
	 * @param objectId the object ID
	 */
	void indexObjectById(String type, Object objectId);
	
	/**
	 * Index an object.
	 * 
	 * @param type the index type
	 * @param object the object to index
	 */
	void indexObject(String type, Object object);
	
	/**
	 * Delete an object from the index based on it's ID.
	 * @param type the index type
	 * @param objectId the object ID
	 */
	void deleteObjectById(String type, Object objectId);
	
	/**
	 * Reindex an entire index type.
	 * @param type the index type
	 * @return the status callback
	 */
	IndexStatusCallback reindex(String type);
	
	/**
	 * Perform a write operation on a Lucene index.
	 * @param type the index
	 * @param create if <em>true</em> then erase any current index files
	 * @param optimize if <em>true</em> then increment the index's optimize 
	 * count and optimize if surpassing that count
	 * @param optimizeOnFinish if <em>true</em> then optimize when finished (and treat
	 * the write operation as a batch operation)
	 * @param writeOp the callback implementation to handle the indexing operation
	 */
	void doIndexWriterOp(String type, boolean create, boolean optimize, 
			boolean optimizeOnFinish, IndexWriterOp writeOp);

	/**
	 * Perform a query operation on a Lucene index.
	 * @param type the index
	 * @param query the query
	 * @param synchronous if <em>true</em> then perform the search operation 
	 * in FIFO order with all other search update operations on the 
	 * specified index
	 * @param queryOp the search callback implementation
	 */
	void doIndexQueryOp(String type, Query query, 
			boolean synchronous, IndexQueryOp queryOp );

	/**
	 * Perform a search operation on a Lucene index.
	 * @param type the index to perform the search operation on
	 * @param searcherOp the search callback implementation
	 */
	void doIndexSearcherOp(String type, IndexSearcherOp searcherOp);

	/**
	 * Perform an exclusive read operation (delete) on a Lucene index.
	 * @param type the index
	 * @param readerOp the reader callback implementation
	 */
	void doIndexReaderOp(String type, IndexReaderOp readerOp);
	
	/**
	 * Perform an exclusive read operation (delete) and write operation on a Lucene index.
	 * @param type the index
	 * @param readerOp the reader callback implementation
	 * @param create if <em>true</em> then erase any current index files
	 * @param optimize if <em>true</em> then increment the index's optimize 
	 * count and optimize if surpassing that count
	 * @param optimizeOnFinish if <em>true</em> then optimize when finished (and treat
	 * the write operation as a batch operation)
	 * @param writeOp the callback implementation to handle the indexing operation
	 */
	void doIndexUpdateOp(String type, IndexReaderOp readerOp, boolean create, 
			boolean optimize, boolean optimizeOnFinish, IndexWriterOp writeOp);
	
	/**
	 * Append a search term to a query buffer.
	 * @param buf the query buffer
	 * @param field the index field
	 * @param value the index field value
	 * @param required is the field required?
	 * @param prohibited is the field prohibited?
	 */
	void appendTerm(StringBuilder buf, String field, 
			String value, boolean required, boolean prohibited);
	
	/**
	 * Append a group of search terms to a query buffer.
	 * @param buf the query buffer
	 * @param field the index field
	 * @param input the list of query terms
	 * @param booleanOp the boolean operation to join the search terms with
	 * (i.e. {@link #AND} or {@link #OR}).
	 * @param required is the group required?
	 */
	void appendTerms(StringBuilder buf, String field, 
			List<?> input, String booleanOp, boolean required);
	
	/**
	 * Utilty for working with query template strings.
	 * @param mergeData the Map of data to merge with the query string
	 * @param field the index field
	 * @param value the index field value (may be a List or single object)
	 * @param joinOp the boolean operation to join the search terms with 
	 * in case <code>value</code> is a List (i.e. {@link #AND} or {@link #OR})
	 * @param matchPattern if non-null then convert <em>value</em> to <em>null</em>
	 * if it does not contain the specified pattern
	 */
	void mergeMagic(Map<String, String> mergeData, String field, 
			Object value, String joinOp, Pattern matchPattern);
	
	/**
	 * Add a series of non-required TermQuery objects to a BooleanQuery, 
	 * from tokenizing a string with the Analyzer used by the index type.
	 * @param rootQuery the root boolean query
	 * @param query the query to tokenize
	 * @param field the field this query is searching
	 * @param type the index type
	 */
	void addTokenizedTermQuery(BooleanQuery rootQuery, String query, 
			String field, String type);
	
	/**
	 * Add a series of non-required FuzzyQuery objects to a BooleanQuery, 
	 * from tokenizing a string with the Analyzer used by the index type.
	 * @param rootQuery the root boolean query
	 * @param query the query to tokenize
	 * @param field the field this query is searching
	 * @param type the index type
	 */
	void addTokenizedFuzzyQuery(BooleanQuery rootQuery, String query, 
			String field, String type);
	
	/**
	 * Parse a query string into a Lucene {@link Query} object.
	 * @param indexType the index
	 * @param query the query string
	 * @return the Lucene Query
	 * @throws RuntimeException if unable to parse the query string
	 */
	Query parseQuery(String indexType, String query);
	
	/**
	 * Get all terms (i.e. values) stored in an index for a given field.
	 * @param index the index
	 * @param field the field
	 * @return the terms
	 */
	Set<String> getFieldTerms(String index, String field);
	
	/**
	 * Low-level search method for executing a raw query against an index.
	 * 
	 * @param index the index type to search against
	 * @param query the query
	 * @param maxResults the maximum number of results to return
	 * @param pageSize if greater than 0, limite results to this many
	 * @param page the page of results to display if <code>pageSize</code> is specified
	 * @return list of results, converted to Map ojbects
	 */
	LuceneSearchResults search(String index, String query,  
			int maxResults, int pageSize, int page);
	
	/**
	 * Low-level search method for executing a raw query against an index, 
	 * using a callback API for processing the results.
	 * 
	 * @param index the index type to search against
	 * @param query the query
	 * @param handler the result handler
	 */
	void search(String index, String query,  LuceneSearchResultHandler handler);
	
	/**
	 * Perform a search against an index, returning a list of search result
	 * objects as defined by the index plugin supporting that index.
	 * @param index the index to search
	 * @param criteria the search criteria
	 * @return the search results
	 */
	SearchResults find(String index, SearchCriteria criteria);
	
	/**
	 * Build a List of SearchMatch objects from an existing Hits object.
	 * @param index the index the hits are from
	 * @param hits the Lucene hits
	 * @param start the starting hits index to build from
	 * @param end the ending hits index to build to
	 * @return the List of SearchMatch objects
	 */
	List<SearchMatch> build(String index, TopDocCollector hits, int start, int end);
	
	/**
	 * Add an EventListener for index operations.
	 * 
	 * <p>The events published by implementations of this class will all
	 * derive from {@link IndexEvent}.</p>
	 * 
	 * @param listener the listener
	 */
	void addIndexEventListener(IndexListener listener);
	
	/**
	 * Remove an EventListener for index operations.
	 * @param listener the listener
	 */
	void removeIndexEventListener(IndexListener listener);
	
}
