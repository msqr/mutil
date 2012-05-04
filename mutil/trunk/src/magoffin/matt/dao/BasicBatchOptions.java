/* ===================================================================
 * BasicBatchOptions.java
 * 
 * Created Oct 10, 2006 8:06:19 AM
 * 
 * Copyright (c) 2006 Matt Magoffin (spamsqr@msqr.us)
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
 * $Id: BasicBatchOptions.java,v 1.2 2006/10/10 04:18:14 matt Exp $
 * ===================================================================
 */

package magoffin.matt.dao;

import java.util.LinkedHashMap;
import java.util.Map;
import magoffin.matt.dao.BatchableDao.BatchMode;
import magoffin.matt.dao.BatchableDao.BatchOptions;

/**
 * Basic implementation of {@link BatchOptions}.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2006/10/10 04:18:14 $
 */
public class BasicBatchOptions implements BatchOptions {
	
	/** The default batch size value. */
	public static final int DEFAULT_BATCH_SIZE = 100;
	
	private String name = null;
	private int batchSize = DEFAULT_BATCH_SIZE;
	private BatchMode mode = BatchMode.LIVE;
	private Map<String, Object> parameters = new LinkedHashMap<String, Object>();

	/**
	 * Default constructor.
	 */
	public BasicBatchOptions() {
		super();
	}

	/**
	 * Construct with a name.
	 * 
	 * @param name the name
	 */
	public BasicBatchOptions(String name) {
		super();
		this.name = name;
	}

	/**
	 * Construct with a mode.
	 * 
	 * @param name the name
	 * @param mode the mode
	 */
	public BasicBatchOptions(String name, BatchMode mode) {
		super();
		this.name = name;
		this.mode = mode;
	}

	/**
	 * Construct with a mode and parameters.
	 * 
	 * @param name the name
	 * @param mode the mode
	 * @param parameters the parameters
	 */
	public BasicBatchOptions(String name, BatchMode mode, Map<String, Object> parameters) {
		super();
		this.name = name;
		this.mode = mode;
		this.parameters = parameters;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public int getBatchSize() {
		return batchSize;
	}

	@Override
	public BatchMode getMode() {
		return mode;
	}

	@Override
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @param batchSize the batchSize to set
	 */
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	
	/**
	 * @param mode the mode to set
	 */
	public void setMode(BatchMode mode) {
		this.mode = mode;
	}
	
	/**
	 * @param parameters the parameters to set
	 */
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}

}
