/* ===================================================================
 * Base64.java
 * 
 * Copyright (c) 2002 Matt Magoffin.
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
 * $Id: Base64.java,v 1.1 2006/07/10 04:22:35 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

/**
 * Class to encode arbitrary bytes into Base64-encoded ASCII values.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/07/10 04:22:35 $
 */
public final class Base64 {

	private static char alphabet[];

	private static byte codes[];

	static {
		alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/="
				.toCharArray();
		codes = new byte[256];
		int i = 0;
		for (i = 0; i < 256; i++)
			codes[i] = -1;
		for (i = 65; i <= 90; i++)
			codes[i] = (byte) (i - 65);
		for (i = 97; i <= 122; i++)
			codes[i] = (byte) (26 + i - 97);
		for (i = 48; i <= 57; i++)
			codes[i] = (byte) (52 + i - 48);
		codes[43] = 62;
		codes[47] = 63;
	}

	/**
	 * Decode a character array of Base64-encoded data.
	 * @param data the Base64 character data
	 * @return the decoded data
	 */
	public static byte[] decode(char data[]) {
		int tempLen = data.length;
		for (int ix = 0; ix < data.length; ix++)
			if (data[ix] > 255 || codes[data[ix]] < 0)
				tempLen--;
		int len = tempLen / 4 * 3;
		if (tempLen % 4 == 3)
			len += 2;
		if (tempLen % 4 == 2)
			len++;
		byte out[] = new byte[len];
		int shift = 0;
		int accum = 0;
		int index = 0;
		for (int ix = 0; ix < data.length; ix++) {
			int value = (data[ix] > 255) ? -1 : codes[data[ix]];
			if (value >= 0) {
				accum <<= 6;
				shift += 6;
				accum |= value;
				if (shift >= 8) {
					shift -= 8;
					out[index++] = (byte) (accum >> shift & 255);
				}
			}
		}
		if (index != out.length) {
			throw new RuntimeException(index + " instead of " + out.length + ")");
		}
		return out;
	}

	/**
	 * Encode arbitrary data as Base64 ASCI character data.
	 * @param data the bytes to encode
	 * @return the Base64 encoded value
	 */
	public static char[] encode(byte data[]) {
		char out[] = new char[(data.length + 2) / 3 * 4];
		int i = 0;
		int index = 0;
		while (i < data.length) {
			boolean quad = false;
			boolean trip = false;
			int val = 255 & data[i];
			val <<= 8;
			if (i + 1 < data.length) {
				val |= 255 & data[i + 1];
				trip = true;
			}
			val <<= 8;
			if (i + 2 < data.length) {
				val |= 255 & data[i + 2];
				quad = true;
			}
			out[index + 3] = alphabet[quad ? (val & 63) : 64];
			val >>= 6;
			out[index + 2] = alphabet[trip ? (val & 63) : 64];
			val >>= 6;
			out[index + 1] = alphabet[val & 63];
			val >>= 6;
			out[index] = alphabet[val & 63];
			i += 3;
			index += 4;
		}
		return out;
	}

}
