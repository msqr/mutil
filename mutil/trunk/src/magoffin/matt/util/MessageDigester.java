/* ===================================================================
 * MessageDigester.java
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
 * $Id: MessageDigester.java,v 1.1 2006/08/26 06:10:26 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Class to generate SHA or SSHA message digests.
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.1 $ $Date: 2006/08/26 06:10:26 $
 */
public final class MessageDigester {

	private static final String ALG = "SHA-1";

	private static final String hexits = "0123456789abcdef";

	private static byte[] concatenate(byte[] l, byte[] r) {
		byte[] b = new byte[l.length + r.length];

		System.arraycopy(l, 0, b, 0, l.length);
		System.arraycopy(r, 0, b, l.length, r.length);
		return b;
	}

	private static byte[] fromHex(String s) {
		if (s == null)
			return new byte[0];
		byte[] b = null;
		int j = 0;
		int nybble = 0;
		int i = 0;

		s = s.toLowerCase();
		b = new byte[(s.length() + 1) / 2];
		j = 0;
		nybble = -1;
		for (i = 0; i < s.length(); ++i) {
			int h = hexits.indexOf(s.charAt(i));

			if (h >= 0) {
				if (nybble < 0)
					nybble = h;
				else {
					b[j++] = (byte) ((nybble << 4) + h);
					nybble = -1;
				}
			}
		}
		if (nybble >= 0)
			b[j++] = (byte) (nybble << 4);
		if (j < b.length) {
			byte[] b2 = new byte[j];

			System.arraycopy(b, 0, b2, 0, j);
			b = b2;
		}
		return b;
	}

	/**
	 * Generate a digest from a String and array of bytes salt.
	 * 
	 * <p>
	 * If <var>salt</var> is <em>null</em> then no salt will be added to the
	 * generated digest.
	 * </p>
	 * 
	 * @param text
	 *            the message to digest
	 * @param salt
	 *            the bytes to use as salt (may be <em>null</em>)
	 * @return the digest
	 */
	public static String generateDigest(String text, byte[] salt) {
		MessageDigest msgDigest = null;
		if (salt == null)
			salt = new byte[0];

		try {
			msgDigest = MessageDigest.getInstance(ALG);
		} catch (NoSuchAlgorithmException e) {
			System.err.println("Digets algorithm '" + ALG + "' not available");
			return null;
		}

		if (msgDigest == null) {
			System.err.println(ALG + " algorithm not available");
			return null;
		}
		
		String label = null;
		byte[] pwhash = null;

		if (ALG.startsWith("SHA")) {
			label = (salt.length > 0) ? "{SSHA}" : "{SHA}";
		}
		msgDigest.reset();
		msgDigest.update(text.getBytes());
		msgDigest.update(salt);
		pwhash = msgDigest.digest();
		StringBuffer digest = new StringBuffer(label);
		digest.append(Base64.encode(concatenate(pwhash, salt)));
		return digest.toString();
	}

	/**
	 * Generate a digest from a String and some String salt.
	 * 
	 * @param text
	 *            the message to digest
	 * @param saltHex
	 *            treated as hex String of bytes
	 * @return the digest
	 */
	public static String generateDigest(String text, String saltHex) {
		return generateDigest(text, fromHex(saltHex));
	}

	/**
	 * Pass text on command line to digest.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("Pass arguments to SHA encrypt.");
			return;
		}

		byte[] salt = new byte[0];
		/*
		 * { (byte)0xc7, (byte)0x73, (byte)0x21, (byte)0x8c, (byte)0x7e,
		 * (byte)0xc8, (byte)0xee, (byte)0x99 };
		 */

		for (int i = 0; i < args.length; i++) {
			System.out.println(args[i] + " = " + generateDigest(args[i], salt));
		}

	}

}
