/* ============================================================================
 * LuceneSearchService.java
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
 * $Id: LuceneSearchService.java,v 1.17 2007/08/20 01:26:43 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import magoffin.matt.lucene.IndexEvent.EventType;
import magoffin.matt.util.BaseQueueThread;
import magoffin.matt.util.FastThreadSafeDateFormat;
import magoffin.matt.util.ThreadSafeDateFormat;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.StringUtils;

/**
 * Service for searching and indexing Lucene indicies.
 * 
 * <p>All index update operations happen within a separate thread and are 
 * queued in the order in which they are received. The <code>updateBufferSize</code>
 * property determines how many updates are buffered as to perform serveral 
 * updates as a batch. If this is set to a value greater than <code>1</code>, 
 * then updates requests will be buffered until <code>updateBufferSize</code>
 * updates are queued. At that them all updates in the buffer will be processed.
 * An exception to this buffering is a reindex operation, which will <em>not</em>
 * be buffered and instead will proceed immediately.</p>
 * 
 * <p>The configurable properties of this class are:</p>
 * 
 * <dl>
 *   <dt>baseIndexDirectoryPath</dt>
 *   <dd>The path where this class can manage Lucene index files. Note the 
 *   application must be able to read, write, and create new files here.</dd>
 *   
 *   <dt>indexTimeZone</dt>
 *   <dd>The time zone to translate all dates in the index to for consistency.
 *   Defaults to the local time zone.</dd>
 *   
 *   <dt>neverOptimize</dt>
 *   <dd>If <em>true</em> then never try to optimize. This useful during 
 *   testing. Defaults to <b>false</b>.</dd>
 *   
 *   <dt>optimizeTriggerCount</dt>
 *   <dd>The number of items to index before asking Lucene to optimize the 
 *   index for searching.</dd>
 *   
 *   <dt>updateBufferSize</dt>
 *   <dd>The number of items to buffer (for each index) before trying to 
 *   index them. Defaults to <code>1</code> so each item is indexed 
 *   immediately.</dd>
 *   
 *   <dt>updateBufferFlushMs</dt>
 *   <dd>The number of milliseconds between flushing the index queue buffers.
 *   Only useful if the <code>updateBufferSize</code> is greater than 1.
 *   Defaults to <code>0</code> (which disables the periodic flushing).</dd>
 *   
 *   <dt>plugins</dt>
 *   <dd>The list of {@link magoffin.matt.lucene.LucenePlugin} instances
 *   to use.</dd>
 *   
 *   <dt>batchMinMergeDocs</dt>
 *   <dd>The Lucene {@link IndexWriter#setMaxBufferedDocs(int)} value to use while
 *   performing batch index operations. Defaults to <b>500</b>.</dd>
 *   
 *   <dt>batchMergeFactor</dt>
 *   <dd>The Lucene {@link IndexWriter#setMergeFactor(int)} value to use while 
 *   performing batch index operations. Defaults to <b>50</b>.</dd>
 *   
 *   <dt>discardedIndexReaderMinCloseTime</dt>
 *   <dd>The minimum amount of milliseconds to hold on to discarded IndexReader instances
 *   before calling the {@link org.apache.lucene.index.IndexReader#close()} method
 *   on that object. Defaults to 60,000 (one minute).</dd>
 *   
 *   <dt>discardedIndexReaderProcessorMs</dt>
 *   <dd>The number of milliseconds between examining the discarded IndexReader buffer
 *   for IndexReader instances to close. Defaults to 180,000 (3 minutes).</dd>
 *   
 * </dl>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.17 $ $Date: 2007/08/20 01:26:43 $
 */
public class LuceneSearchService implements LuceneService {
	
	/** The default value for the <code>batchMinMergeDocs</code> property. */
	public static final int DEFALUT_BATCH_MIN_MERGE_DOCS = 500;
	
	/** The default value for the <code>batchMergeFactor</code> property. */
	public static final int DEFAULT_BATCH_MERGE_FACTOR = 50;
	
	/** The default value for the <code>discardedIndexReaderMinCloseTime</code> property. */
	public static final long DEFAULT_DISCARDED_INDEX_READER_MIN_CLOSE_TIME = 60000;
	
	/** The default value for the <code>discardedIndexReaderProcessorMs</code> property. */
	public static final long DEFAULT_DISCARDED_INDEX_READER_PROCESSOR_MS = 180000;
	
	private static final Long ZERO = new Long(0);
	
	private static final Pattern NON_ALPHANUMERIC = Pattern.compile("\\W");
	private static final Pattern LUCENE_RANGE = Pattern.compile("^\\[.+ TO .+\\]$");
	private static final Pattern LUCENE_SPECIAL = 
		Pattern.compile("(([\\+\\-\\!\\(\\)\\{\\}\\[\\]^\"~*?:\\\\]|&&|\\|\\|))");
	private static final Pattern LUCENE_ESCAPED_QUOTES_BUG = Pattern.compile("\"");
	private static final Pattern LUCENE_SPECIAL_WORD = Pattern.compile("(AND|OR|NOT)");
	private static final String LUCENE_BACKSLASH_BUG = "\\\\)";
	private static final String LUCENE_BACKSLASH_BUG_FIX = "\\\\ )";
	private static final int SECONDS_PER_HOUR = 3600;
	
	private static final boolean SHARED = true;
	private static final boolean NOT_SHARED = false;
	
	private static class LuceneSearchResultsImpl implements LuceneSearchResults {
		private int totalMatches;
		private List<Map<String, String[]>> results;

		public List<Map<String, String[]>> getResults() {
			return results;
		}

		public int getTotalMatches() {
			return totalMatches;
		}
		
	}
	
	/* The following are externally injected fields */
	
	private String idField = "id";
	private String defaultField = "Gtext";
	private String baseIndexDirectoryPath = null;
	private boolean neverOptimize = false;
	private int optimizeTriggerCount = 0;
	private int updateBufferSize = 1;
	private int batchMinMergeDocs = DEFALUT_BATCH_MIN_MERGE_DOCS;
	private int batchMergeFactor = DEFAULT_BATCH_MERGE_FACTOR;
	private long updateBufferFlushMs = 0;
	private TimeZone indexTimeZone = TimeZone.getDefault();	
	private long discardedIndexReaderMinCloseTime = DEFAULT_DISCARDED_INDEX_READER_MIN_CLOSE_TIME;
	private long discardedIndexReaderProcessorMs = DEFAULT_DISCARDED_INDEX_READER_PROCESSOR_MS;
	private boolean throwExceptionDuringInitialize = false;
	
	private List<LucenePlugin> plugins;
	private Set<IndexListener> indexEventListeners = new LinkedHashSet<IndexListener>();
	
	/* The following are internally initialized fields */
	
	private IndexQueueThread indexQueue = null;
	private File indexDirectory = null;
	private Map<String, IndexData> indexDataMap = new HashMap<String, IndexData>();
	private Timer indexQueueFlushTimer = null;
	
	private ThreadSafeDateFormat dayDateFormat = new FastThreadSafeDateFormat(
			INDEX_DATE_FORMAT_DAY_PATTERN, TimeZone.getDefault());
	private ThreadSafeDateFormat monthDateFormat = new FastThreadSafeDateFormat(
			INDEX_DATE_FORMAT_MONTH_PATTERN, TimeZone.getDefault());
	
	private List<DiscardedIndexReader> discardedIndexReaders = 
		Collections.synchronizedList(new LinkedList<DiscardedIndexReader>());
	private Timer discardedIndexReaderProcessorTimer = null;
	private boolean finished = false;

	private final Logger log = Logger.getLogger(LuceneSearchService.class);
	private final Logger traceLog = Logger.getLogger(LuceneSearchService.class.getName()+".TRACE");

	/**
	 * Initialize this instance.
	 * 
	 * <p>This method must be called after all properties have been set 
	 * and before any non-property methods are called.</p>
	 */
	public synchronized void initialize() {
		if ( baseIndexDirectoryPath == null ) {
			if ( throwExceptionDuringInitialize ) {
				throw new RuntimeException("Property baseIndexDirectoryPath not configured");
			}
			log.warn("Property baseIndexDirectoryPath not configured");
		}

		File tmpFile = new File(baseIndexDirectoryPath);
		if ( !tmpFile.exists() ) {
			if ( log.isInfoEnabled() ) {
				log.info("Creating Lucene index directory " +tmpFile.getAbsolutePath());
			}
			if ( !tmpFile.mkdirs() ) {
				if ( throwExceptionDuringInitialize ) {
					throw new RuntimeException("Unable to create Lucene index directory " 
						+tmpFile.getAbsolutePath());
				}
				log.warn("Unable to create Lucene index directory " 
					+tmpFile.getAbsolutePath());
			}
		}
		if ( !tmpFile.isDirectory() ) {
			if ( throwExceptionDuringInitialize ) {
				throw new RuntimeException("Lucene index directory is not a directory: "
					+tmpFile.getAbsolutePath());
			}
			log.warn("Lucene index directory is not a directory: "
				+tmpFile.getAbsolutePath());
		}
		indexDirectory = tmpFile;
		
		// add shutdown hook to try to close Lucene indicies properly when shutdown
		Thread shutdownHook = new Thread() {
			@Override
			public void run() {
				LuceneSearchService.this.finish();
			}
		};
		Runtime.getRuntime().addShutdownHook(shutdownHook);
		
		// initialize plug-ins
		for ( LucenePlugin plugin : plugins ) {
			setupAndVerifyIndex(plugin);
		}
		
		indexQueue = new IndexQueueThread();
		if ( this.updateBufferSize > 0 ) {
			Thread t = new Thread(indexQueue);
			t.setName(indexQueue.getThreadName());
			// t.setPriority(Thread.MIN_PRIORITY);
			t.setDaemon(false);
			t.start();
			
			// setup flush timer if appropriate
			if ( this.updateBufferFlushMs > 0 && this.updateBufferSize > 1 ) {
				this.indexQueueFlushTimer = new Timer(true); // make a daemon
				this.indexQueueFlushTimer.schedule(
						new IndexQueueFlushTask(),
						new Date(), // start now
						this.updateBufferFlushMs);
			}
		}
		
		// setup discarded reader processor thread
		this.discardedIndexReaderProcessorTimer = new Timer(true);
		this.discardedIndexReaderProcessorTimer.schedule(
				new CloseDiscardedIndexReaderTask(),
				new Date(),
				this.discardedIndexReaderProcessorMs);

		if ( this.indexTimeZone != null && log.isInfoEnabled() ) {
			log.info("Index using [" +this.indexTimeZone.getDisplayName() 
					+"] time zone for all date operations.");
		}
		finished = false;
	}

	/**
	 * Verify the IndexData for an index.
	 * @param plugin the index to verify
	 * @throws RuntimeException if an error occurs
	 */
	private void setupAndVerifyIndex(LucenePlugin plugin) {
		String type = plugin.getIndexType();
		File indexDir = new File(indexDirectory, type.toString());
		
		if ( !indexDir.exists() && !indexDir.mkdirs() ) {
			if ( throwExceptionDuringInitialize ) {
				throw new RuntimeException("Unable to create Lucene index directory [" 
					+indexDir.getAbsolutePath() +"]");
			}
			log.warn("Unable to create Lucene index directory [" 
					+indexDir.getAbsolutePath() +"]");
			return;
		}
		try {
			if ( !indexDataMap.containsKey(type) ) {
				Directory dir = FSDirectory.getDirectory(indexDir);
				IndexData data = new IndexData(dir, type, plugin);
				indexDataMap.put(type, data);
			} else {
				IndexData data = indexDataMap.get(type);
				data.dir = FSDirectory.getDirectory(indexDir);
				data.type = type;
				data.plugin = plugin;
			}
			IndexData indexData = indexDataMap.get(type);
			// indexData.indexDirectory = indexDir;
			if ( plugin.getAnalyzer() == null ) {
				throw new RuntimeException("Analyzer not configured for index [" 
						+type +"]");
			}

			indexData.plugin = plugin;
			indexData.config = plugin.init(this, Collections.unmodifiableSet(
					this.indexEventListeners));
			if ( indexData.config == null ) {
				throw new RuntimeException("LuceneIndexConfig for plugin ["
						+plugin +"] is null");
			}
			
			if ( !IndexReader.indexExists(indexData.dir) ) {
				IndexResults results = plugin.reindex();
				if ( results != null && results.getErrors().size() > 0 ) {
					String msg = results.getNumIndexed() +" leads indexed OK, " 
						+results.getErrors().size() +" leads could not be indexed.\n"
							+"Lead IDs that failed to be indexed:\n";
					for ( Map.Entry<? extends Serializable, String> me 
							: results.getErrors().entrySet() ) {
						msg += "\n" +me.getKey() +": " +me.getValue();
					}
					log.error(msg);
				}
			}

		} catch ( IOException e ) {
			if ( throwExceptionDuringInitialize ) {
				throw new RuntimeException("Unable to verify existance of Lucene index [" 
					+type +"] at [" +indexDir.getAbsolutePath() +"]", e);
			}
			log.warn("Unable to verify existance of Lucene index [" 
					+type +"] at [" +indexDir.getAbsolutePath() +"]", e);
		}
	}
	
	/**
	 * Call when instance is no longer needed to cleanly shut down the 
	 * Lucene indicies and buffers.
	 */
	public synchronized void finish() {
		if ( finished ) return;
		
		if ( indexQueueFlushTimer != null ) {
			indexQueueFlushTimer.cancel();
		}
		if ( indexQueue != null ) {
			if ( log.isInfoEnabled() ) {
				log.info("Stopping " +indexQueue.getThreadName());
			}
			indexQueue.stop();
			indexQueue = null;
		}
		
		finished = true;
		log.info("LuceneSearchService.finish() complete.");
	}
	
	/**
	 * Flush a single index's update queue.
	 * @param type the index queue to flush
	 */
	protected synchronized void flush(String type) {
		if ( indexQueue != null ) {
			indexQueue.flush(type);
		}
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		finish();
	}
	
	private synchronized void processDiscardedIndexReaders() {
		if ( discardedIndexReaders != null ) {
			synchronized ( discardedIndexReaders ) {
				if ( log.isDebugEnabled() ) {
					log.debug("Processing discarded IndexReader buffer (contains " 
							+this.discardedIndexReaders.size() +" items)");
				}
				for ( Iterator<DiscardedIndexReader> itr = discardedIndexReaders.iterator(); 
						itr.hasNext(); ) {
					DiscardedIndexReader discarded = itr.next();
					int count = discarded.readerCount.get();
					long age = System.currentTimeMillis() - discarded.discardTime;
					if ( age > discardedIndexReaderMinCloseTime && count < 1 ) {
						// ok to close reader now
						if ( log.isInfoEnabled() ) {
							log.info("Closing IndexReader " +discarded.reader 
									+" for index [" +discarded.indexType 
									+"], is " +age +"ms old and readerCount < 1");
						}
						try {
							closeIndexReader(discarded.reader, discarded.indexType);
						} catch ( Exception e ) {
							log.error("Error closing discarded IndexReader ["
									+discarded.reader +"] for index ["
									+discarded.indexType +"]", 
									e.getCause() != null ? e.getCause() : e);
						}
						itr.remove();
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#indexObject(java.lang.String, java.lang.Object)
	 */
	public void indexObject(String type, Object object) {
		if ( indexQueue == null ) {
			log.warn("Unable to index object [" +object 
					+"] from index [" +type 
					+"], LuceneSearchService is shut down");
			return;
		}
		IndexQueueThreadCommand command = new IndexQueueThreadCommand(object,
				type, IndexQueueThreadCommand.Operation.UPDATE, false);
		if ( this.updateBufferSize < 1 ) {
			command.callingThread = true;
			synchronized ( this ) {
				// synchronized for luceneSearchService.finish() method,
				// which sets indexQueue to null
				indexQueue.handleItem(command);
			}
		} else {
			indexQueue.enqueue(command);
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#indexObjectById(java.lang.String, java.lang.Long)
	 */
	public void indexObjectById(String type, Object objectId) {
		if ( indexQueue == null ) {
			log.warn("Unable to index objectId [" +objectId 
					+"] from index [" +type 
					+"], LuceneSearchService is shut down");
			return;
		}
		IndexQueueThreadCommand command = new IndexQueueThreadCommand(objectId,
				type, IndexQueueThreadCommand.Operation.UPDATE, true);
		if ( this.updateBufferSize < 1 ) {
			command.callingThread = true;
			synchronized ( this ) {
				// synchronized for luceneSearchService.finish() method,
				// which sets indexQueue to null
				indexQueue.handleItem(command);
			}
		} else {
			indexQueue.enqueue(command);
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#deleteObjectById(java.lang.String, java.lang.Object)
	 */
	public void deleteObjectById(final String type, final Object objectId) {
		if ( indexQueue == null ) {
			log.warn("Unable to delete by objectId [" +objectId 
					+"] from index [" +type 
					+"], LuceneSearchService is shut down");
			return;
		}
		IndexQueueThreadCommand command = new IndexQueueThreadCommand(objectId,
				type, IndexQueueThreadCommand.Operation.DELETE, true);
		if ( this.updateBufferSize < 1 ) {
			command.callingThread = true;
			synchronized ( this ) {
				// synchronized for luceneSearchService.finish() method,
				// which sets indexQueue to null
				indexQueue.handleItem(command);
			}
		} else {
			indexQueue.enqueue(command);
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#reindex(java.lang.String)
	 */
	public IndexStatusCallback reindex(final String type) {
		if ( indexQueue == null ) {
			log.warn("Unable to reindex index [" +type 
					+"], LuceneSearchService is shut down");
			return null;
		}
		final IndexData indexData = indexDataMap.get(type);
		if ( this.updateBufferSize < 1 ) {
			LuceneIndexStatusCallback callback = new LuceneIndexStatusCallback() {
				@Override
				public void go() {
					setIndexResults(indexData.plugin.reindex());
				}
			};
			callback.go();
			return callback;
		}
		LuceneIndexStatusCallback callback = new LuceneIndexStatusCallback() {
			@Override
			public void go() {
				setIndexResults(indexData.plugin.reindex());
			}
		};
		IndexQueueThreadCommand command = new IndexQueueThreadCommand(
				ZERO, type, IndexQueueThreadCommand.Operation.REINDEX, true);
		command.statusCallback = callback;
		indexQueue.enqueue(command);
		return callback;
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#find(java.lang.String, magoffin.matt.lucene.SearchCriteria)
	 */
	public SearchResults find(String index, SearchCriteria criteria) {
		IndexData indexData = this.indexDataMap.get(index);
		if ( criteria.isCountOnly() ) {
			Object o = indexData.plugin.getNativeQuery(criteria);
			if ( o instanceof Query ) {
				final BasicSearchResults results = new BasicSearchResults();
				doIndexQueryOp(index, (Query)o, false, new IndexQueryOp() {
					public void doSearcherOp(String type, IndexSearcher searcher, 
							Query query, Hits hits) throws IOException {
						results.setTotalMatches(hits.length());
					}
				});
				return results;
			}
			// fall back to normal query here
		}
		return indexData.plugin.find(criteria);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#build(java.lang.String, org.apache.lucene.search.Hits, int, int)
	 */
	public List<SearchMatch> build(String index, Hits hits, int start, int end) {
		LucenePlugin plugin = this.indexDataMap.get(index).plugin;
		int length = end > start ? end - start : 0;
		int hitLength = hits.length();
		List<SearchMatch> searchMatches = new ArrayList<SearchMatch>(length);
		try {
			for ( int i = start; i < end && i < hitLength; i++ ) {
				searchMatches.add(plugin.build(hits.doc(i)));
			}
			return searchMatches;
		} catch ( IOException e ) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#search(magoffin.matt.lucene.LuceneService.String, java.lang.String, magoffin.matt.lucene.LuceneService.LuceneSearchResultHandler)
	 */
	public void search(String index, String query, final LuceneSearchResultHandler handler) {
		Query luceneQuery = parseQuery(index, query);
		doIndexQueryOp(index, luceneQuery, ASYNCHRONOUS, new IndexQueryOp() {
			@SuppressWarnings("unchecked")
			public void doSearcherOp(String indexType, IndexSearcher searcher, 
					Query myQuery, Hits hits) throws IOException {
				int numHits = hits == null ? 0 : hits.length();
				handler.setTotalMatches(numHits);
				for ( int i = 0; i < numHits; i++ ) {
					Document doc = hits.doc(i);
					List<Field> fields = doc.getFields();
					
					Map<String, String[]> match = new LinkedHashMap<String, String[]>();
					for ( Field field : fields ) {
						match.put(field.name(), doc.getValues(field.name()));
					}
					
					if ( !handler.processMatch(match) ) {
						break;
					}
				}
			}
		});
	}

	public LuceneSearchResults search(String type, String query,  
			final int maxResults, final int pageSize, final int page) {
		Query luceneQuery = parseQuery(type, query);
		final LuceneSearchResultsImpl results = new LuceneSearchResultsImpl();
		doIndexQueryOp(type, luceneQuery, ASYNCHRONOUS, new IndexQueryOp() {
			@SuppressWarnings({ "unchecked" })
			public void doSearcherOp(String indexType, IndexSearcher searcher, 
					Query myQuery, Hits hits) throws IOException {
				int numHits = hits == null ? 0 : hits.length();
				results.totalMatches = numHits;
				if ( numHits > 0 ) {
					Set<String> seenFieldNames = new HashSet<String>();
					results.results = new LinkedList<Map<String, String[]>>();
					int start = 0;
					int max = -1;
					if ( pageSize > 0 ) {
						start = pageSize * (page - 1);
						max = pageSize;
					}
					int maxr = maxResults < 1 ? numHits : maxResults;
					for ( int i = start; i < numHits && i < maxr && ((max--) != 0); i++ ) {
						Document doc = hits.doc(i);
						List<Field> fields = doc.getFields();
						
						// use a TreeMap to keep keys sorted
						Map<String, String[]> data = new TreeMap<String, String[]>();
						for( Field field : fields ) {
							data.put(field.name(), doc.getValues(field.name()));
						}
						
						Set<String> fieldSet = new HashSet<String>();
						fieldSet.addAll(data.keySet());
						
						// see if doc was missing any seen fields...
						Collection<String> fill = CollectionUtils.subtract(
								seenFieldNames, fieldSet);
						if ( fill.size() > 0 ) {
							for ( String fieldName : fill ) {
								data.put(fieldName, null);
							}
						}
						
						// see if any fields we have not seen yet...
						Collection<String> missing = CollectionUtils.subtract(
								fieldSet, seenFieldNames);
						
						// any keys in 'missing' need to be added to all previous
						// results so they all have same keys
						if ( missing.size() > 0 ) {
							for ( Map<String, String[]> map : results.results ) {
								for ( Iterator<String> itr = missing.iterator(); itr.hasNext(); ) {
									map.put(itr.next(), null);
								}
							}
							seenFieldNames.addAll(missing);
						}
						results.results.add(data);
					}
				}
			}			
		});
		return results;
	}
	
	/**
	 * Add a series of non-required TermQuery objects to a BooleanQuery, 
	 * from tokenizing a string with the Analyzer used by the index type.
	 * @param rootQuery the root boolean query
	 * @param query the query to tokenize
	 * @param field the field this query is searching
	 * @param type the index type
	 */
	public void addTokenizedTermQuery(BooleanQuery rootQuery, String query, 
			String field, String type) {
		StringReader reader = new StringReader(query);
		IndexData data = indexDataMap.get(type);
		TokenStream stream = data.plugin.getAnalyzer().tokenStream(
				field, reader);
		try {
			while ( true ) {
				Token token = stream.next();
				if (token == null) {
					break;
				}
				Query q = new TermQuery(new Term(field, token.termText()));
				rootQuery.add(q, Occur.SHOULD);
			}
		} catch ( IOException e ) {
			throw new RuntimeException("Unable to tokenize query string", e);
		}
	}
	
	/**
	 * Add a series of non-required FuzzyQuery objects to a BooleanQuery, 
	 * from tokenizing a string with the Analyzer used by the index type.
	 * @param rootQuery the root boolean query
	 * @param query the query to tokenize
	 * @param field the field this query is searching
	 * @param type the index type
	 */
	public void addTokenizedFuzzyQuery(BooleanQuery rootQuery, String query, 
			String field, String type) {
		StringReader reader = new StringReader(query);
		IndexData data = indexDataMap.get(type);
		TokenStream stream = data.plugin.getAnalyzer().tokenStream(
				field, reader);
		try {
			while ( true ) {
				Token token = stream.next();
				if (token == null) {
					break;
				}
				Query q = new FuzzyQuery(new Term(field, token.termText()));
				rootQuery.add(q, Occur.SHOULD);
			}
		} catch ( IOException e ) {
			throw new RuntimeException("Unable to tokenize query string", e);
		}
	}
	
	public void mergeMagic(Map<String,String> mergeData, String field, Object value, String joinOp, Pattern matchPattern) {
		if ( value == null || !StringUtils.hasText(value.toString()) ) {
			return;
		}
		StringBuilder tmp = new StringBuilder();
		if ( value instanceof List ) {
			List<?> listValue = (List<?>)value;
			if ( matchPattern != null ) {
				List<String> matchList = new LinkedList<String>();
				for ( Object o : listValue ) {
					String oStr = o == null ? null : o.toString();
					if ( oStr != null && !matchPattern.matcher(oStr).find() ) {
						// didn't match, so convert to null
						oStr = null;
					}
					matchList.add(oStr);
				}
				appendTerms(tmp, field, matchList, joinOp, false);
			} else {
				appendTerms(tmp, field, listValue, joinOp, false);
			}
		} else {
			String valueStr = value.toString();
			if ( matchPattern != null ) {
				if ( !matchPattern.matcher(valueStr).find() ) {
					// convert to NULL
					valueStr = null;
				}
			}
			appendTerm(tmp, field, valueStr, false, false);
		}
		if ( tmp.length() > 1 ) {
			mergeData.put(field, tmp.toString());
		}
	}

	public Query parseQuery(String indexType, String query) {
		if ( traceLog.isDebugEnabled() ) {
			traceLog.debug("Parsing Lucene query string [" +query +"]");
		}
		// check for bug found in Lucene QueryParser...
		if ( query.indexOf(LUCENE_BACKSLASH_BUG) >= 0 ) {
			query = query.replace(LUCENE_BACKSLASH_BUG, LUCENE_BACKSLASH_BUG_FIX);
			if ( log.isDebugEnabled() ) {
				log.debug("Corrected query for Lucene \"\\)\" bug: " +query);
			}
		}
		try {
			return new QueryParser(this.defaultField,
					indexDataMap.get(indexType).plugin.getAnalyzer()).parse(query);
		} catch (ParseException e) {
			throw new RuntimeException("Unable to parse Lucene query [" +query +"]", e);
		}
	}
	
	public void appendTerm(StringBuilder buf, String field, 
			String value, boolean required, boolean prohibited) {
		if ( !StringUtils.hasText(value) ) {
			// term value is empty, do not append anything
			if ( log.isDebugEnabled() ) {
				log.debug("Not appending search term for field [" +field 
						+"] since value is empty");
			}
			return;
		}
		if ( buf.length() > 0 ) {
			buf.append(' ');
		}
		if ( required ) {
			buf.append('+');
		} else if ( prohibited ) {
			buf.append('-');
		}
		buf.append(field).append(FIELD_DELIM);

		appendTermValue(buf, value);
	}
	
	private void appendTermValue(StringBuilder buf, String value) {
		// we use the LUCENE_RANGE regexp to check if value is already a lucene range, 
		// which we don't want to enclose in quotes. Otherwise, if special characters
		// are in the value, we'll enclose the value in quotes
		
		Matcher specialWordMatcher = LUCENE_SPECIAL_WORD.matcher(value);
		
		if ( !LUCENE_RANGE.matcher(value).matches() && (NON_ALPHANUMERIC.matcher(value).find() 
				|| specialWordMatcher.matches()) ) {
			// begin SCR 45: work around for Lucene QueryParser bug that can't parse nested quotes
			Matcher quotes = LUCENE_ESCAPED_QUOTES_BUG.matcher(value);
			if ( quotes.find() ) {
				// remove the quotes
				value = quotes.replaceAll("");
				
				// if we removed the quotes and length is now 0, 
				// reset back to empty quote string to prevent empty search
				if ( value.length() < 1 ) {
					value = "\"\"";
				}
			}
			// end SCR 45
			
			Matcher special = LUCENE_SPECIAL.matcher(value);
			if ( special.find() ) {
				// contains Lucene-reserved character, escape with \
				value = special.replaceAll("\\\\$1");
			}
			if ( value.indexOf(" ") >= 0 || specialWordMatcher.matches() ) {
				buf.append('"').append(value).append('"');
			} else {
				buf.append(value);
			}
		} else {
			buf.append(value);
		}
	}
	
	public void appendTerms(StringBuilder buf, String field, List<?> input, String booleanOp, boolean required) {
		if ( input.size() < 1 ) {
			return; // skip
		}
		List<String> values = new ArrayList<String>(input.size());
		for ( Object o : input ) {
			if ( o != null ) {
				String str = o.toString().trim();
				if ( StringUtils.hasText(str) ) {
					values.add(str);
				}
			}
		}
		if ( values.size() < 1 ) {
			return; // skip
		}
		if ( values.size() == 1 ) {
			appendTerm(buf, field, values.get(0).toString(), required, false);
			return;
		}
		if ( buf.length() > 0 ) {
			buf.append(' ');
		}
		if ( required ) {
			buf.append('+');
		}
		buf.append('(');
		int i = 0;
		for ( Iterator<?> itr = values.iterator(); itr.hasNext(); i++ ) {
			String term = itr.next().toString();
			if ( i > 0 ) {
				buf.append(' ').append(booleanOp).append(' ');
			}
			buf.append(field).append(FIELD_DELIM);
			appendTermValue(buf, term);
		}
		buf.append(')');
	}

	/**
	 * Delete a Document from the index.
	 * 
	 * <p>Check out <a 
	 * href="http://nagoya.apache.org/eyebrowse/ReadMsg?listName=lucene-user@jakarta.apache.org&msgId=1190557"
	 * >this post</a> for info on how this is done.
	 * </p>
	 * 
	 * @param type the index type
	 * @param reader the index to delete from
	 * @param id the ID of the Document to delete, using the <code>idField</code> field
	 * @return the number of items deleted
	 */
	protected int deleteFromIndex(String type, IndexReader reader, Object id) {
		if ( id == null ) {
			throw new IllegalArgumentException("Null ID passed to deleteFromIndex");
		}
		try {
			Term idTerm = new Term(idField, id.toString());
			if ( reader.docFreq(idTerm) > 0 ) {
				int result = reader.deleteDocuments(idTerm);
				if ( traceLog.isInfoEnabled() ) {
					traceLog.info(TraceOp.DELETE +"Deleted " +result 
							+" Document for ID " +id +" from reader "
							+reader +" (" +reader.directory().toString() +")");
				}
				LuceneServiceUtils.publishIndexEvent(new IndexEvent(id,
						EventType.DELETE, type), this.indexEventListeners);
				return result;
			}
		} catch ( IOException e ) {
			throw new RuntimeException("IOException deleting Document from Lucene index", e);
		}
		return 0;
	}
	
	private abstract static class LuceneIndexStatusCallback implements IndexStatusCallback {

		private Logger log = Logger.getLogger(getClass());
		
		private boolean done = false;
		private Throwable throwable = null;
		private IndexResults indexResults = null;
		
		/** Perform the index operation. */
		public abstract void go();
		
		/**
		 * Set the index results.
		 * @param indexResults the index results
		 */
		protected void setIndexResults(IndexResults indexResults) {
			this.indexResults = indexResults;
		}
		
		public IndexResults getIndexResults() {
			return indexResults;
		}

		public void waitUntilDone() {
			synchronized ( this ) {
				try {
					while ( !done ) {
						wait();
					}
				} catch (InterruptedException e) {
					log.warn("Interrupted while waiting for index to complete", e);
				}
			}
			if ( throwable != null ) {
				if ( log.isDebugEnabled() ) {
					log.debug("Encountered error " +throwable.getClass().getName() 
							+" during callback processing");
				}
				if ( throwable instanceof RuntimeException ) {
					throw ((RuntimeException)throwable);
				}
				throw new RuntimeException(throwable);
			}
		}

	}
	
	private void optimizeIndex(IndexData data, IndexWriter writer) throws IOException {
		if ( neverOptimize ) {
			return;
		}
		synchronized ( data.dir ) {
			while (data.readerCount.get() > 0 ) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.warn("Interrupted waiting for readers to finish", e);
				}
			}
			discardIndexReader(data);
			synchronized ( discardedIndexReaders ) {
				for ( DiscardedIndexReader dir : discardedIndexReaders ) {
					try {
						closeIndexReader(dir.reader, data.type);
					} catch ( RuntimeException e ) {
						if ( e.getCause() != null && e.getCause() instanceof IOException ) {
							// we'll ignore this one
						} else {
							throw e;
						}
					}
				}
			}
			if ( traceLog.isInfoEnabled() ) {
				traceLog.info(TraceOp.UPDATE +"Optimizing Lucene index [" 
						+data.type +"] ...");
			}
			writer.optimize();
			if ( traceLog.isInfoEnabled() ) {
				traceLog.info(TraceOp.UPDATE +"Optimizing Lucene index [" 
						+data.type +"] complete.");
			}
		}
	}

	/** Internal enum for trace log. */
	private enum TraceOp {
		/** Concurrency. */
		CONCURRENCY,
		
		/** Query. */
		QUERY,
		
		/** Delete. */
		DELETE,
		
		/** Update. */
		UPDATE,
		
		/** Error. */
		ERROR;
		
		@Override
		public String toString() {
			switch ( this ) {
				case CONCURRENCY: return "CON ";
				case QUERY: return "QUE ";
				case DELETE: return "DEL ";
				case UPDATE: return "UPD ";
				case ERROR: return "!!! ";
				default: throw new AssertionError(this);
			}
		}
		
	}
	
	public void doIndexQueryOp(final String type, final Query query, 
			final boolean synchronous, final IndexQueryOp queryOp ) {
		if ( query == null ) {
			return;
		}
		final IndexData data = indexDataMap.get(type);
		if ( synchronous && indexQueue != null ) {
			LuceneIndexStatusCallback callback = new LuceneIndexStatusCallback() {
				@Override
				public void go() {
					executeIndexSearcherOp(type, query, queryOp, data);
				}
			};
			IndexQueueThreadCommand command = new IndexQueueThreadCommand(
					ZERO, type, IndexQueueThreadCommand.Operation.CALLBACK, true);
			command.statusCallback = callback;
			indexQueue.enqueue(command);
			callback.waitUntilDone();
		} else {
			executeIndexSearcherOp(type, query, queryOp, data);
		}
	}

	public void doIndexSearcherOp(String type, IndexSearcherOp searcherOp) {
		final IndexData data = indexDataMap.get(type);
		IndexSearcher searcher = getIndexSearcher(data);
		AtomicInteger readerCount = data.readerCount;
		readerCount.incrementAndGet();
		data.queryCount.incrementAndGet();
		try {
			searcherOp.doSearcherOp(type, searcher);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			readerCount.decrementAndGet();
		}
	}

	private void executeIndexSearcherOp(String type, Query query, 
			IndexQueryOp queryOp, IndexData data) {
		IndexSearcher searcher = null;
		AtomicInteger readerCount = data.readerCount;
		readerCount.incrementAndGet();
		data.queryCount.incrementAndGet();
		try {
			searcher = getIndexSearcher(data);
			long start = System.currentTimeMillis();
			Hits hits = searcher.search(query);
			long time = System.currentTimeMillis() - start;
			if ( log.isDebugEnabled() ) {
				log.debug("Lucene query [" +query
						+"] returned " +hits.length() +" in " +time +"ms");
			}
			if ( traceLog.isDebugEnabled() ) {
				traceLog.debug(TraceOp.QUERY +"Lucene query [" +query
						+"] returned " +hits.length() +" in " +time +"ms");
			}
			queryOp.doSearcherOp(type, searcher, query, hits);
		} catch ( Exception e ) {
			log.error("Lucene exception during search on [" +type +"]", e);
			throw new RuntimeException("Exception searching index [" +type +"]", e);
		} finally {
			readerCount.decrementAndGet();
		}
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#doIndexUpdateOp(magoffin.matt.lucene.LuceneService.String, magoffin.matt.lucene.LuceneService.IndexReaderOp, boolean, boolean, boolean, magoffin.matt.lucene.LuceneService.IndexWriterOp)
	 */
	public void doIndexUpdateOp(String type, IndexReaderOp readerOp, boolean create, 
			boolean optimize, boolean optimizeOnFinish, IndexWriterOp writeOp) {
		IndexData data = indexDataMap.get(type);
		IndexReader reader = null;
		IndexWriter writer = null;
		Lock lock = data.writeLock;
		lock.lock();
		try {
			// perform delete op
			try {
				reader = getIndexReader(data, NOT_SHARED);
				readerOp.doReaderOp(type, reader);
			} finally {
				try {
					closeIndexReader(reader, type);
				} catch ( Exception e ) {
					traceLog.warn(TraceOp.ERROR +"Unable to close index reader", e);
				}
				reader = null;
			}

			// perform update op
			writer = new IndexWriter(data.dir,
					data.plugin.getAnalyzer(), create);
			if ( traceLog.isInfoEnabled() ) {
				traceLog.info(TraceOp.CONCURRENCY +"Created new IndexWriter " 
						+writer +" for index [" +type +"]");
			}
			writer.setUseCompoundFile(true); // to minimize the number of files kept open
			if ( optimizeOnFinish ) {
				// treat as batch
				writer.setMaxBufferedDocs(this.batchMinMergeDocs);
				writer.setMergeFactor(this.batchMergeFactor);
			} else {
				writer.setMaxBufferedDocs(data.config.getMinMergeDocs());
				writer.setMergeFactor(data.config.getMergeFactor());
			}
			writeOp.doWriterOp(type, writer);
			if ( optimize && !optimizeOnFinish && optimizeTriggerCount > 0 ) {
				data.updateCount++;
				if ( data.updateCount > optimizeTriggerCount ) {
					try {
						optimizeIndex(data, writer);
					} catch ( IOException e ) {
						throw new RuntimeException("IOException optimizing index [" 
								+type +"]", e);
					}
					data.updateCount = 0;
				}
			}
		} catch ( Exception e ) {
			log.error("Lucene exception during index update operation on [" +type +"]", e);
			throw new RuntimeException("Exception during IndexReader operation on index [" 
					+type +"]", e);	
		} finally {
			if ( writer != null ) {
				if ( optimizeOnFinish ) {
					try {
						optimizeIndex(data, writer);
					} catch ( Exception e ) {
						traceLog.warn(TraceOp.ERROR +"Unable to optimize Lucene index ["
								+type +"]", e);
					}
				}
				
				if ( traceLog.isInfoEnabled() ) {
					traceLog.info(TraceOp.CONCURRENCY +"Closing IndexWriter " +writer
							+" for index [" +type +"]");
				}
				try {
					writer.close();
				} catch (Exception e) {
					traceLog.warn(TraceOp.ERROR +"Unable to close Lucene index writer", e);
				}
			}
			
			discardIndexReader(data);
			lock.unlock();
		}
	}

	private void discardIndexReader(IndexData data) {
		synchronized ( data.dir ) {
			if ( data.reader != null ) {
				// move IndexReader over to discarded buffer, for closing later
				DiscardedIndexReader discardedReader = new DiscardedIndexReader(
						data.readerCount, data.reader, data.type);
				this.discardedIndexReaders.add(discardedReader);
				
				if ( traceLog.isInfoEnabled() ) {
					traceLog.info(TraceOp.CONCURRENCY +"Discarding IndexReader [" 
							+data.reader +"] for index [" +data.type +"]");
				}
				
				data.reader = null;
				data.readerCount = new AtomicInteger(0);
				data.searcher = null;
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#doIndexReaderOp(magoffin.matt.lucene.LuceneService.String, magoffin.matt.lucene.LuceneService.IndexReaderOp)
	 */
	public void doIndexReaderOp(String type, IndexReaderOp readerOp) {
		IndexData data = indexDataMap.get(type);
		IndexReader reader = null;
		Lock lock = data.writeLock;
		lock.lock();
		try {
			reader = getIndexReader(data, NOT_SHARED);
			readerOp.doReaderOp(type, reader);
		} catch ( Exception e ) {
			log.error("Lucene exception during IndexReader operation on [" +type +"]", e);
			throw new RuntimeException("Exception during IndexReader operation on index [" 
					+type +"]", e);	
		} finally {
			try {
				closeIndexReader(reader, type);
			} catch ( Exception e ) {
				traceLog.warn(TraceOp.ERROR +"Unable to close index reader", e);
			}
			lock.unlock();
		}
	}
	
	
	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#doIndexWriterOp(magoffin.matt.lucene.LuceneService.String, boolean, boolean, boolean, magoffin.matt.lucene.LuceneService.IndexWriterOp)
	 */
	public void doIndexWriterOp(String type, boolean create, boolean optimize, 
			boolean optimizeOnFinish, IndexWriterOp writeOp) {
		IndexData data = indexDataMap.get(type);
		IndexWriter writer = null;
		Lock lock = data.writeLock;
		lock.lock();
		try {
			writer = new IndexWriter(data.dir,
					data.plugin.getAnalyzer(), create);
			if ( traceLog.isInfoEnabled() ) {
				traceLog.info(TraceOp.CONCURRENCY +"Created new IndexWriter " 
						+writer +" for index [" +type +"]");
			}
			writer.setUseCompoundFile(true); // to minimize the number of files kept open
			if ( optimizeOnFinish ) {
				// treat as batch
				writer.setMaxBufferedDocs(this.batchMinMergeDocs);
				writer.setMergeFactor(this.batchMergeFactor);
			} else {
				writer.setMaxBufferedDocs(data.config.getMinMergeDocs());
				writer.setMergeFactor(data.config.getMergeFactor());
			}
			writeOp.doWriterOp(type, writer);
			if ( optimize && !optimizeOnFinish && optimizeTriggerCount > 0 ) {
				data.updateCount++;
				if ( data.updateCount > optimizeTriggerCount ) {
					try {
						optimizeIndex(data, writer);
					} catch ( IOException e ) {
						throw new RuntimeException("IOException optimizing index [" 
								+type +"]", e);
					}
					data.updateCount = 0;
				}
			}
		} catch ( Exception e ) {
			log.error("Lucene exception during IndexWriter operation on [" 
					+type +",create=" +create +"]", e);
			throw new RuntimeException("Exception during IndexWriter operation on index [" 
					+type +",create=" +create +"]", e);	
		} finally {
			if ( writer != null ) {
				if ( optimizeOnFinish ) {
					try {
						optimizeIndex(data, writer);
					} catch ( Exception e ) {
						traceLog.warn(TraceOp.ERROR +"Unable to optimize Lucene index ["
								+type +"]", e);
					}
				}
				
				if ( traceLog.isInfoEnabled() ) {
					traceLog.info(TraceOp.CONCURRENCY +"Closing IndexWriter " +writer
							+" for index [" +type +"]");
				}
				try {
					writer.close();
				} catch (Exception e) {
					traceLog.warn(TraceOp.ERROR +"Unable to close Lucene index writer", e);
				}
			}
			discardIndexReader(data);
			lock.unlock();
		}
	}
	
	public Set<String> getFieldTerms(final String index, final String field) {
		final Set<String> results = new TreeSet<String>();
		IndexData data = indexDataMap.get(index);
		AtomicInteger readerCount = data.readerCount;
		TermEnum terms = null;
		readerCount.incrementAndGet();
		data.queryCount.incrementAndGet();
		try {
			terms = getIndexReader(data, SHARED).terms(new Term(field, ""));
			while ( terms.term() != null && field.equals(terms.term().field()) ) {
				String aTerm = terms.term().text();
				results.add(aTerm);
				if ( !terms.next() ) {
					break;
				}
			}
		} catch ( IOException e ) {
			throw new RuntimeException("Unable to get index terms on index [" 
					+index +"] for field [" +field +"]", e);
		} finally {
			try {
				terms.close();
			} catch (IOException e) {
				throw new RuntimeException("Error closing TermEnum while getting terms on index [" 
						+index +"] for field [" +field +"]", e);
			}
			readerCount.decrementAndGet();
		}
		return results;
	}
	
	private IndexReader getIndexReader(IndexData data, boolean shared) {
		if ( shared ) {
			synchronized ( data.dir ) {
				if ( data.reader == null ) {
					if ( data.reader != null && traceLog.isInfoEnabled() ) {
						traceLog.info(TraceOp.CONCURRENCY +"Replacing IndexReader "
								+data.reader +" [" +data.type +"]");
					}
					try {
						data.reader = IndexReader.open(data.dir);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
					if ( traceLog.isInfoEnabled() ) {
						traceLog.info(TraceOp.CONCURRENCY +"Cached new IndexReader " 
								+data.reader +" [" +data.type +"]");
					}
				}
				return data.reader;
			}
		}
		try {
			IndexReader reader = IndexReader.open(data.dir);
			if ( traceLog.isInfoEnabled() ) {
				traceLog.info(TraceOp.CONCURRENCY +"Created non-cached IndexReader " 
						+reader +" [" +data.type +"]");
			}
			return reader;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}	
	}

	private void closeIndexReader(IndexReader reader, String type)  {
		if ( reader != null ) {
			if ( traceLog.isInfoEnabled() ) {
				traceLog.info(TraceOp.CONCURRENCY +"Closing Lucene IndexReader " 
						+reader +" [" +type +"]");
			}
			try {
				reader.close();
			} catch (IOException e) {
				
				throw new RuntimeException("Unable to close Lucene index reader", e);
			}
		}
	}

	private IndexSearcher getIndexSearcher(IndexData data) {
		synchronized ( data.dir ) {
			if ( data.searcher == null ) {
				IndexReader reader = getIndexReader(data, SHARED);
				if ( data.searcher != null && traceLog.isInfoEnabled() ) {
					traceLog.info(TraceOp.CONCURRENCY +"Replacing IndexSearcher "
							+data.searcher +" for index [" +data.type +"]");
				}
				data.searcher = new IndexSearcher(reader);
				if ( traceLog.isInfoEnabled() ) {
					traceLog.info(TraceOp.CONCURRENCY 
							+"Cached new IndexSearcher " +data.searcher 
							+" for index [" +data.type +"]");
				}
			}
			return data.searcher;
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#addIndexEventListener(java.util.EventListener)
	 */
	public synchronized void addIndexEventListener(IndexListener listener) {
		this.indexEventListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#removeIndexEventListener(java.util.EventListener)
	 */
	public synchronized void removeIndexEventListener(IndexListener listener) {
		if ( this.indexEventListeners == null ) return;
		for ( Iterator<IndexListener> itr = this.indexEventListeners.iterator(); itr.hasNext(); ) {
			IndexListener oneListener = itr.next();
			if ( listener.equals(oneListener) ) {
				itr.remove();
			}
		}
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#formatDateToDay(java.util.Date)
	 */
	public String formatDateToDay(Date date) {
		return dayDateFormat.format(date);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#formatDateToDay(java.util.Date, java.util.TimeZone)
	 */
	public String formatDateToDay(Date date, TimeZone zone) {
		return dayDateFormat.format(date, zone);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#formatDateToMonth(java.util.Date)
	 */
	public String formatDateToMonth(Date date) {
		return monthDateFormat.format(date);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#formatDateToMonth(java.util.Date, java.util.TimeZone)
	 */
	public String formatDateToMonth(Date date, TimeZone zone) {
		return monthDateFormat.format(date, zone);
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#parseDate(java.lang.String, java.util.TimeZone)
	 */
	public Date parseDate(String dateStr, TimeZone zone) {
		Calendar result = null;
		try {
			result = dayDateFormat.parseCalendar(dateStr, zone);
		} catch ( RuntimeException e ) {
			// ignore this
		}
		if ( result == null ) {
			try {
				result = monthDateFormat.parseCalendar(dateStr, zone);
			} catch ( RuntimeException e2 ) {
				// ignore this
			}
			if ( result == null ) {
				if ( log.isDebugEnabled() ) {
					log.debug("Unable to parse date string [" +dateStr +"] with either ["
							+dayDateFormat.getPattern() +"] or [" 
							+monthDateFormat.getPattern() +"] patterns");
				}				
			}
		}
		return result == null ? null : result.getTime();
	}

	/* (non-Javadoc)
	 * @see magoffin.matt.lucene.LuceneService#parseDate(java.lang.String)
	 */
	public Date parseDate(String dateStr) {
		Date result = null;
		try {
			result = dayDateFormat.parseDate(dateStr);
		} catch ( RuntimeException e ) {
			// ignore this
		}
		if ( result == null ) {
			try {
				result = monthDateFormat.parseDate(dateStr);
			} catch ( RuntimeException e2 ) {
				// ignore this
			}
			if ( result == null ) {
				if ( log.isDebugEnabled() ) {
					log.debug("Unable to parse date string [" +dateStr +"] with either ["
							+dayDateFormat.getPattern() +"] or [" 
							+monthDateFormat.getPattern() +"] patterns");
				}				
			}
		}
		return result;
	}

	/** A command type for the IndexQueue. */
	private static class IndexQueueThreadCommand {
		/** The index operation to perform. */
		private static enum Operation {
			/** Delete an item from the index. */
			DELETE, 
			
			/** Update an item in the index. */
			UPDATE, 
			
			/** Reindex the entire table. */
			REINDEX, 
			
			/** No operation. */
			CALLBACK,
		}
		
		private Object item;
		private IndexQueueThreadCommand.Operation op;
		private String type;
		private IndexStatusCallback statusCallback = null;
		private boolean indexById = true;
		private boolean callingThread = false;
		
		/**
		 * Construct with an item ID, type, and mode.
		 * 
		 * @param item the object, or object ID of the item to index
		 * @param type the type of object to index
		 * @param op the index operation to perform
		 * @param indexById if <em>true</em> then <code>object</code> is treated
		 * as an object ID, otherwise it is treated as the object itself
		 */
		public IndexQueueThreadCommand(Object item, String type, 
				IndexQueueThreadCommand.Operation op, boolean indexById) {
			this.item = item;
			this.type = type;
			this.op = op;
			this.indexById = indexById;
		}
		
		@Override
		public String toString() {
			return "IndexQueueThreadCommand{item=" 
				+item +",type=" +type +",op=" +op +",byId=" +indexById
				+",callingThread=" +callingThread +"}";
		}
	}
	
	/**
	 * Timer task to periodically flush index queues.
	 */
	private class IndexQueueFlushTask extends TimerTask {
		@Override
		public void run() {
			indexQueue.flush();
		}
	}
	
	/**
	 * Timer task to periodically close discarded IndexReader objects.
	 */
	private class CloseDiscardedIndexReaderTask extends TimerTask {
		@Override
		public void run() {
			processDiscardedIndexReaders();
		}
	}
	
	/**
	 * An interceptor that will cause indexing to occur in the calling thread,
	 * instead of the index queue thread.
	 * 
	 * <p>This can be used in certain situations where the calling thread has 
	 * specific resources that are necessary for indexing, for example during 
	 * a transaction where the data to be indexed is only available in the 
	 * transaction.</p>
	 */
	public static class CallingThreadService implements MethodInterceptor {

		private LuceneSearchService luceneSearchService;
		
		/* (non-Javadoc)
		 * @see org.aopalliance.intercept.MethodInterceptor#invoke(org.aopalliance.intercept.MethodInvocation)
		 */
		public Object invoke(MethodInvocation invocation) throws Throwable {
			String methodName = invocation.getMethod().getName();
			if ( methodName.startsWith("indexObject") ) {
				final String type = (String)invocation.getArguments()[0];
				final Object obj = invocation.getArguments()[1];
				IndexQueueThreadCommand command = new IndexQueueThreadCommand(obj,
						type, IndexQueueThreadCommand.Operation.UPDATE, 
						methodName.endsWith("ById"));
				command.callingThread = true;
				synchronized ( luceneSearchService ) {
					// synchronized for luceneSearchService.finish() method,
					// which sets indexQueue to null
					if ( luceneSearchService.indexQueue != null ) {
						luceneSearchService.indexQueue.handleItem(command);
					} else {
						luceneSearchService.log.warn(
								"Unable to index object [" +obj 
								+"] from index [" +type 
								+"], LuceneSearchService is shut down");
					}
				}
				return null;
			}
			return invocation.proceed();
		}
		
		/**
		 * @return the luceneSearchService
		 */
		public LuceneSearchService getLuceneSearchService() {
			return luceneSearchService;
		}
		
		/**
		 * @param luceneSearchService the luceneSearchService to set
		 */
		public void setLuceneSearchService(LuceneSearchService luceneSearchService) {
			this.luceneSearchService = luceneSearchService;
		}

	}

	private final class IndexQueueThread extends BaseQueueThread<IndexQueueThreadCommand> {
		
		private Map<String, Queue<IndexQueueThreadCommand>> bufferMap = 
			new HashMap<String, Queue<IndexQueueThreadCommand>>();
		
		private ExecutorService callbackExecutor = Executors.newCachedThreadPool();
		
		private IndexQueueThread() {
			// call super constructor with "stop" item
			super(new IndexQueueThreadCommand(Long.MIN_VALUE,
					null, IndexQueueThreadCommand.Operation.UPDATE, true));
			for ( LucenePlugin plugin : plugins ) {
				bufferMap.put(plugin.getIndexType(), 
						new ConcurrentLinkedQueue<IndexQueueThreadCommand>());
			}
		}
		
		@Override
		public String getThreadName() {
			return "LuceneSearchServiceIndexQueueThread";
		}
		
		@Override
		protected void handleItem(final IndexQueueThreadCommand command) {
			switch ( command.op ) {
				case REINDEX:
				case CALLBACK:
					callbackExecutor.execute(new Runnable() {
						public void run() {
							if ( traceLog.isInfoEnabled() ) {
								traceLog.info(TraceOp.CONCURRENCY +"Processing " +command.op
										+" op on index [" +command.type +"]");
							}
							LuceneIndexStatusCallback callback = 
								(LuceneIndexStatusCallback)command.statusCallback;
							try {
								callback.go();
							} catch ( Throwable t ) {
								if ( log.isDebugEnabled() ) {
									log.debug("Exception during callback processing: " 
											+t.getMessage());
								}
								callback.throwable = t;
							} finally {
								if ( traceLog.isInfoEnabled() ) {
									traceLog.info(TraceOp.CONCURRENCY +"Completed " +command.op
											+" op on index [" +command.type +"]");
								}
								synchronized ( callback ) {
									callback.done = true;
									callback.notify();
								}
							}
						}					
					});
					return;
					
				case DELETE:
				case UPDATE:
					// add update to internal buffer, then process buffer if reached capacity
					if ( log.isDebugEnabled() ) {
						log.debug("Buffering update to index type " +command.type +": " +command);
					}
					Queue<IndexQueueThreadCommand> queue = bufferMap.get(command.type);
					queue.add(command);
					
					// the following test tests for simple case first because calls 
					// to ConcurrentLinkedQueue.size() are not linear so we try to avoid that
					if ( command.callingThread || updateBufferSize < 2 || bufferMap.get(command.type).size() 
							>= updateBufferSize ) {
						processBufferedUpdates(queue, command.type);
					}
					return;
					
				default:
					// nothing
			}
		}
		
		/**
		 * Process an index Queue.
		 * 
		 * <p>Ad items are processed they are removed from the Queue.</p>
		 * 
		 * @param queue the queue to process
		 * @param indexType the index type
		 */
		private void processBufferedUpdates(final Queue<IndexQueueThreadCommand> queue, 
				String indexType) {
			if ( queue.isEmpty() ) {
				return;
			}
			
			// index all items in update buffer by first deleting them 
			// and then (if update operation) adding them to the index
			
			if ( log.isDebugEnabled() ) {
				log.debug("Processing " +queue.size() +" updates for index " +indexType);
			}
			final Map<Object, IndexQueueThreadCommand> toUpdate 
				= new LinkedHashMap<Object, IndexQueueThreadCommand>();

			doIndexReaderOp(indexType, new IndexReaderOp() {
				public void doReaderOp(String type, IndexReader reader) {
					while ( !queue.isEmpty() ) {
						IndexQueueThreadCommand command = queue.remove();					
						if ( command.op == IndexQueueThreadCommand.Operation.UPDATE ) {
							// handle later so delete / update within same lock
							Object itemId = command.item;
							if ( !command.indexById ) {
								IndexData indexData = indexDataMap.get(type);
								itemId = indexData.plugin.getIdForObject(command.item);
							}
							toUpdate.put(itemId, command);
						} else {
							handleDelete(type, reader, command);
							LuceneServiceUtils.publishIndexEvent(
									new IndexEvent(command.item, EventType.DELETE, type), 
									indexEventListeners);
						}
					}
				}
			});

			if ( toUpdate.size() > 0 ) {
				doIndexUpdateOp(indexType, new IndexReaderOp() {
					public void doReaderOp(String type, IndexReader reader) {
						// process index deletes
						for ( IndexQueueThreadCommand command : toUpdate.values() ) {
							handleDelete(type, reader, command);
						}
					}				
				}, false, true, false, new IndexWriterOp() {
					public void doWriterOp(String type, IndexWriter writer) {
						// process index updates
						IndexData indexData = indexDataMap.get(type);
						for ( IndexQueueThreadCommand command : toUpdate.values() ) {
							try {
								if ( command.indexById ) {
									indexData.plugin.index(command.item, writer);
								} else {
									indexData.plugin.indexObject(command.item, writer);
								}
								LuceneServiceUtils.publishIndexEvent(
										new IndexEvent(command.item, EventType.UPDATE, type), 
										indexEventListeners);
							} catch ( ObjectRetrievalFailureException e ) {
								log.warn("Unable to load object type [" +command.type +"] by key ["
										+command.item +"] for indexing");
							}
						}
					}
				});
			}
		}
		
		@Override
		protected void exiting() {
			try {
				flush();
			} catch ( Throwable t ) {
				log.error("Exception flushing queue!", t);
			} finally {
				if ( log.isInfoEnabled() ) {
					log.info("Shutting down callback ExecutorService [" +callbackExecutor +"]");
				}
				callbackExecutor.shutdown();
				if ( log.isInfoEnabled() ) {
					log.info("Waiting for termination of ExecutorService [" +callbackExecutor +"]");
				}
				try {
					callbackExecutor.awaitTermination(SECONDS_PER_HOUR, TimeUnit.SECONDS);
				} catch ( InterruptedException e ) {
					log.warn("Interrupted waiting for termination of ExecutorService [" 
							+callbackExecutor +"]");
				}
			}
		}
		
		private void flush(String type) {
			if ( log.isDebugEnabled() ) {
				log.debug("Flushing index update buffer [" +type +"]");
			}
			if ( traceLog.isInfoEnabled() ) {
				traceLog.info(TraceOp.UPDATE +"Flushing index update buffer [" +type +"]");
			}
			processBufferedUpdates(bufferMap.get(type), type);
		}
		
		private void flush() {
			for ( LucenePlugin plugin : plugins ) {
				flush(plugin.getIndexType());
			}
		}

		private void handleDelete(String type, IndexReader reader, IndexQueueThreadCommand command) {
			if ( command.indexById ) {
				deleteFromIndex(type, reader, command.item);
			} else {
				IndexData indexData = indexDataMap.get(type);
				Object id = indexData.plugin.getIdForObject(command.item);
				if ( id != null ) {
					deleteFromIndex(type, reader, id);
				}
			}
		}
	}
	
	/**
	 * An internal struct to manage all objects related to a single 
	 * Lucene index.
	 * 
	 * <p>This makes it easier to manage more than one index at a 
	 * time.</p>
	 */
	private static final class IndexData {
		private String type;
		private LucenePlugin plugin;
		private LucenePlugin.LuceneIndexConfig config;
		private IndexSearcher searcher;
		private IndexReader reader;
		private Directory dir;
		private int updateCount;
		private AtomicInteger queryCount; // may need to use AtomicLong?
		private AtomicInteger readerCount;
		private Lock writeLock;

		private IndexData() {
			this(null, null, null);
		}
		private IndexData(Directory dir, String type, 
				LucenePlugin plugin) {
			this.updateCount = 0;
			this.plugin = plugin;
			this.type = type;
			this.dir = dir;
			this.writeLock = new ReentrantLock();
			this.readerCount = new AtomicInteger(0);
			this.queryCount = new AtomicInteger(0);
		}
	}
	
	private static final class DiscardedIndexReader {
		private DiscardedIndexReader(AtomicInteger readerCount, IndexReader reader, 
				String indexType) {
			this.discardTime = System.currentTimeMillis();
			this.readerCount = readerCount;
			this.reader = reader;
			this.indexType = indexType;
		}
		private long discardTime;
		private AtomicInteger readerCount;
		private IndexReader reader;
		private String indexType;
	}
	
	/**
	 * Get the LucenePlugin configured for a given String.
	 * 
	 * <p>This method can be used by extending classes.</p>
	 * 
	 * @param type the type
	 * @return the LucenePlugin
	 */
	protected LucenePlugin getPluginForString(String type) {
		IndexData indexData = indexDataMap.get(type);
		return indexData.plugin;
	}

	/* JMX friendly methods below. */
	
	/**
	 * Get a status string of the search service.
	 * @return status description
	 */
	public String getStatusDescription() {
		StringBuilder buf = new StringBuilder();
		
		buf.append("IndexQueue:             ").append(indexQueue.getThreadName()).append("\n");	
		buf.append("Discarded IndexReaders: ").append(
				this.discardedIndexReaders.size()).append("\n");
		
		buf.append("\nConfigured indicies:\n");
		for ( IndexData data : indexDataMap.values() ) {
			buf.append("\n").append(data.type).append("\n");
			
			buf.append("    ").append("Directory:        ").append(data.dir).append("\n");
			buf.append("    ").append("Plugin:           ").append(
					data.plugin.getClass().getName()).append("\n");

			buf.append("    ").append("MergeFactor:      ").append(
					data.config.getMergeFactor()).append("\n");
			buf.append("    ").append("MinMergeDocs:     ").append(
					data.config.getMinMergeDocs()).append("\n");
			

			
			buf.append("    ").append("IndexReader:      ").append(data.reader).append("\n");
			buf.append("    ").append("IndexSearcher:    ").append(data.searcher).append("\n");
			buf.append("    ").append("Curr read ops:    ").append(
					data.readerCount.get()).append("\n");
			buf.append("    ").append("Update count:     ").append(data.updateCount).append("\n");
			buf.append("    ").append("Query count:      ").append(data.queryCount).append("\n");
			buf.append("    ").append("Index queue size: ").append(
					indexQueue.bufferMap.get(data.type).size()).append("\n");
			
			Lock writeLock = data.writeLock;
			String avail = "locked";
			if ( writeLock.tryLock() ) {
				writeLock.unlock();
				avail = "available";
			}
			buf.append("    ").append("Write lock:       ").append(avail).append("\n");
		}
		return buf.toString();
	}

	/* Injection methods below. */
	
	/**
	 * @return Returns the baseIndexDirectoryPath.
	 */
	public String getBaseIndexDirectoryPath() {
		return baseIndexDirectoryPath;
	}
	
	/**
	 * @param baseIndexDirectoryPath The baseIndexDirectoryPath to set.
	 */
	public void setBaseIndexDirectoryPath(String baseIndexDirectoryPath) {
		this.baseIndexDirectoryPath = baseIndexDirectoryPath;
	}
	
	/**
	 * @return Returns the optimizeTriggerCount.
	 */
	public int getOptimizeTriggerCount() {
		return optimizeTriggerCount;
	}
	
	/**
	 * @param optimizeTriggerCount The optimizeTriggerCount to set.
	 */
	public void setOptimizeTriggerCount(int optimizeTriggerCount) {
		this.optimizeTriggerCount = optimizeTriggerCount;
	}
	
	/**
	 * @return Returns the updateBufferFlushMs.
	 */
	public long getUpdateBufferFlushMs() {
		return updateBufferFlushMs;
	}
	
	/**
	 * @param updateBufferFlushMs The updateBufferFlushMs to set.
	 */
	public void setUpdateBufferFlushMs(long updateBufferFlushMs) {
		this.updateBufferFlushMs = updateBufferFlushMs;
	}
	
	/**
	 * @return Returns the updateBufferSize.
	 */
	public int getUpdateBufferSize() {
		return updateBufferSize;
	}
	
	/**
	 * @param updateBufferSize The updateBufferSize to set.
	 */
	public void setUpdateBufferSize(int updateBufferSize) {
		this.updateBufferSize = updateBufferSize;
	}
	
	/**
	 * @return Returns the batchMergeFactor.
	 */
	public int getBatchMergeFactor() {
		return batchMergeFactor;
	}
	
	/**
	 * @param batchMergeFactor The batchMergeFactor to set.
	 */
	public void setBatchMergeFactor(int batchMergeFactor) {
		this.batchMergeFactor = batchMergeFactor;
	}
	
	/**
	 * @return Returns the batchMinMergeDocs.
	 */
	public int getBatchMinMergeDocs() {
		return batchMinMergeDocs;
	}
	
	/**
	 * @param batchMinMergeDocs The batchMinMergeDocs to set.
	 */
	public void setBatchMinMergeDocs(int batchMinMergeDocs) {
		this.batchMinMergeDocs = batchMinMergeDocs;
	}
	
	/**
	 * @return Returns the neverOptimize.
	 */
	public boolean isNeverOptimize() {
		return neverOptimize;
	}
	
	/**
	 * @param neverOptimize The neverOptimize to set.
	 */
	public void setNeverOptimize(boolean neverOptimize) {
		this.neverOptimize = neverOptimize;
	}
	
	/**
	 * @return Returns the plugins.
	 */
	public List<LucenePlugin> getPlugins() {
		return plugins;
	}
	
	/**
	 * @param plugins The plugins to set.
	 */
	public void setPlugins(List<LucenePlugin> plugins) {
		this.plugins = plugins;
	}
	
	/**
	 * @return Returns the indexTimeZone.
	 */
	public TimeZone getIndexTimeZone() {
		return indexTimeZone;
	}
	
	/**
	 * @param indexTimeZone The indexTimeZone to set.
	 */
	public void setIndexTimeZone(TimeZone indexTimeZone) {
		if ( indexTimeZone != this.indexTimeZone) {
			// update date formats, too
			this.dayDateFormat = new FastThreadSafeDateFormat(
					this.dayDateFormat == null 
						? INDEX_DATE_FORMAT_DAY_PATTERN
						: this.dayDateFormat.getPattern(), indexTimeZone);
			this.monthDateFormat = new FastThreadSafeDateFormat(
					this.monthDateFormat == null
						? INDEX_DATE_FORMAT_MONTH_PATTERN
						: this.monthDateFormat.getPattern(), indexTimeZone);
		}
		this.indexTimeZone = indexTimeZone;
	}
	
	/**
	 * @return Returns the minDiscardedIndexReaderCloseTime.
	 */
	public long getDiscardedIndexReaderMinCloseTime() {
		return discardedIndexReaderMinCloseTime;
	}
	
	/**
	 * @param minDiscardedIndexReaderCloseTime The minDiscardedIndexReaderCloseTime to set.
	 */
	public void setDiscardedIndexReaderMinCloseTime(
			long minDiscardedIndexReaderCloseTime) {
		this.discardedIndexReaderMinCloseTime = minDiscardedIndexReaderCloseTime;
	}
	
	/**
	 * @return Returns the discardedIndexReaderProcessorMs.
	 */
	public long getDiscardedIndexReaderProcessorMs() {
		return discardedIndexReaderProcessorMs;
	}
	
	/**
	 * @param discardedIndexReaderProcessorMs The discardedIndexReaderProcessorMs to set.
	 */
	public void setDiscardedIndexReaderProcessorMs(
			long discardedIndexReaderProcessorMs) {
		this.discardedIndexReaderProcessorMs = discardedIndexReaderProcessorMs;
	}

	/**
	 * @return Returns the defaultField.
	 */
	public String getDefaultField() {
		return defaultField;
	}

	/**
	 * @param defaultField The defaultField to set.
	 */
	public void setDefaultField(String defaultField) {
		this.defaultField = defaultField;
	}

	/**
	 * @return Returns the idField.
	 */
	public String getIdField() {
		return idField;
	}

	/**
	 * @param idField The idField to set.
	 */
	public void setIdField(String idField) {
		this.idField = idField;
	}

	/**
	 * @return Returns the throwExceptionDuringInitialize
	 */
	public boolean isThrowExceptionDuringInitialize() {
		return throwExceptionDuringInitialize;
	}

	/**
	 * @param throwExceptionDuringInitialize the throwExceptionDuringInitialize to set.
	 */
	public void setThrowExceptionDuringInitialize(
			boolean throwExceptionDuringInitialize) {
		this.throwExceptionDuringInitialize = throwExceptionDuringInitialize;
	}
	
	/**
	 * @return the dayDateFormat
	 */
	public ThreadSafeDateFormat getDayDateFormat() {
		return dayDateFormat;
	}
	
	/**
	 * @param dayDateFormat the dayDateFormat to set
	 */
	public void setDayDateFormat(ThreadSafeDateFormat dayDateFormat) {
		this.dayDateFormat = dayDateFormat;
	}
	
	/**
	 * @return the monthDateFormat
	 */
	public ThreadSafeDateFormat getMonthDateFormat() {
		return monthDateFormat;
	}
	
	/**
	 * @param monthDateFormat the monthDateFormat to set
	 */
	public void setMonthDateFormat(ThreadSafeDateFormat monthDateFormat) {
		this.monthDateFormat = monthDateFormat;
	}

}
