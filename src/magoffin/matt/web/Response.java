/* ===================================================================
 * Response.java
 * 
 * Created Feb 3, 2015 6:26:32 PM
 * 
 * Copyright (c) 2015 Matt Magoffin.
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
 */

package magoffin.matt.web;

/**
 * A simple service response envelope object.
 *
 * @author matt
 * @version 1.0
 * @param <T>
 *        the object type
 */
public class Response<T> {

	private final Boolean success;
	private final String code;
	private final String message;
	private final T data;

	/**
	 * Construct a successful response with no data.
	 */
	public Response() {
		this(Boolean.TRUE, null, null, null);
	}

	/**
	 * Construct a successful response with just data.
	 * 
	 * @param data
	 *        the data
	 */
	public Response(T data) {
		this(Boolean.TRUE, null, null, data);
	}

	/**
	 * Constructor.
	 * 
	 * @param success
	 *        flag of success
	 * @param code
	 *        optional code, e.g. error code
	 * @param message
	 *        optional descriptive message
	 * @param data
	 *        optional data in the response
	 */
	public Response(Boolean success, String code, String message, T data) {
		super();
		this.success = success;
		this.code = code;
		this.message = message;
		this.data = data;
	}

	/**
	 * Helper method to construct instance using generic return type inference.
	 * If you import this static method, then in your code you can write
	 * {@code return response(myData)} instead of
	 * {@code new Response&lt;Object&gt;(myData)}.
	 * 
	 * @param data
	 *        the data
	 * @return the response
	 */
	public static <V> Response<V> response(V data) {
		return new Response<V>(data);
	}

	public Boolean getSuccess() {
		return success;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public T getData() {
		return data;
	}

}