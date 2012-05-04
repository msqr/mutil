/* ===================================================================
 * IndexUpdateTracker.java
 * 
 * Created Mar 5, 2007 5:59:10 PM
 * 
 * Copyright (c) 2007 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: IndexUpdateTracker.java,v 1.1 2007/03/05 05:41:02 matt Exp $
 * ===================================================================
 */

package magoffin.matt.lucene;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

/**
 * IndexListener that listens for updates and keeps track of all of them.
 * 
 * <p>This is useful for unit tests that want to delete all 
 * updates after the test runs.</p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2007/03/05 05:41:02 $
 */
public class IndexUpdateTracker implements IndexListener {
	
	private final Map<String, List<Object>> updateMap = new HashMap<String, List<Object>>();
	
	private final Logger log = Logger.getLogger(IndexUpdateTracker.class);

	@Override
	public void onIndexEvent(IndexEvent event) {
		if ( IndexEvent.EventType.UPDATE == event.getType() ) {
			List<Object> typeList = updateMap.get(event.getIndexType());
			if ( typeList == null ) {
				typeList = new LinkedList<Object>();
				updateMap.put(event.getIndexType(), typeList);
			}
			typeList.add(event.getSource());
		}
	}

	/**
	 * Delete all updated objects.
	 * @param lucene the LuceneService to delete from
	 */
	public void cleanUp(LuceneService lucene) {
		for ( String indexType : updateMap.keySet() ) {
			List<Object> list = updateMap.get(indexType);
			for ( Object o : list ) {
				try {
					lucene.deleteObjectById(indexType, o);
				} catch ( Exception e ) {
					if ( log.isDebugEnabled() ) {
						log.debug("Unable to delete by object ID for ID [" +o +"]");
					}
				}
			}
		}
	}
}
