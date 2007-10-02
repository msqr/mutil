/* ===================================================================
 * DataEncryption.java
 * 
 * Copyright (c) 2004 Matt Magoffin.
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
 * $Id: DataEncryption.java,v 1.2 2007/08/20 01:25:30 matt Exp $
 * ===================================================================
 */

package magoffin.matt.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Utility class to encrypt/decrypt data.
 * 
 * <p>
 * The {@link #encrypt(String)} and {@link #decrypt(String)} methods will use
 * cipher and salt defined by the various fields passed to the constructor of
 * this class. The String returned from {@link #encrypt(String)} will be Base64
 * encoded, and the String passed to {@link #encrypt(String)} should also be
 * Base64 encoded.
 * </p>
 * 
 * @author Matt Magoffin (spamsqr@msqr.us)
 * @version $Revision: 1.2 $ $Date: 2007/08/20 01:25:30 $
 */
public final class DataEncryption {

	private final Log log = LogFactory.getLog(DataEncryption.class);

	private static final byte[] DEFAULT_SALT = { (byte) 0x21, (byte) 0xda,
			(byte) 0xb5, (byte) 0x0e, (byte) 0xab, (byte) 0xd7, (byte) 0x8c,
			(byte) 0xae };

	private static final String DEFAULT_KEY = "498dlij3^@T$jl20l;ashe sfhd f=({*Y_@(JIOH";

	private SecretKey secretKey;

	private Cipher cipherEnc;

	private Cipher cipherDec;

	/**
	 * Construct a new ApplicationDataEncryption instance.
	 * 
	 * <p>
	 * If <var>useSalt</var> is <em>true</em> then use a JCE cipher like
	 * <code>PBEWithHmacSHA1AndDESede</code>. Otherwise use a JCE cipher like
	 * <code>Blowfish</code>.
	 * </p>
	 * 
	 * @param provider
	 *            the JCE provider
	 * @param jceSecretKey
	 *            the JCE secret key type
	 * @param jceCipher
	 *            the JCE cipher type
	 * @param usePassword
	 *            if <em>true</em> then use a default key and salt
	 * @throws RuntimeException 
	 *            if any JCE exception occurs
	 */
	public DataEncryption(String provider, String jceSecretKey,
			String jceCipher, boolean usePassword) {
		this(provider, jceSecretKey, jceCipher, DEFAULT_KEY,
				usePassword ? DEFAULT_SALT : null);
	}

	/**
	 * Construct a new ApplicationDataEncryption instance.
	 * 
	 * <p>
	 * If <var>useSalt</var> is <em>true</em> then use a JCE cipher like
	 * <code>PBEWithHmacSHA1AndDESede</code>. Otherwise use a JCE cipher like
	 * <code>Blowfish</code>.
	 * </p>
	 * 
	 * @param provider
	 *            the JCE provider
	 * @param jceSecretKey
	 *            the JCE secret key type (eg. PBEWithMD5AndDES)
	 * @param jceCipher
	 *            the JCE cipher type (eg. PBEWithMD5AndDES)
	 * @param key
	 *            the key to encrypt the data with
	 * @param salt
	 *            the salt to use, or <em>null</em> to not use a password
	 * @throws RuntimeException
	 *             if any JCE exception occurs
	 */
	public DataEncryption(String provider, String jceSecretKey,
			String jceCipher, String key, byte[] salt) {
		try {
			if (key != null && salt != null && salt.length > 0) {
				if (log.isDebugEnabled()) {
					log.debug("Getting secret key instance for " + jceSecretKey
							+ " (with salt)...");
				}

				PBEKeySpec pbeKeySpec = new PBEKeySpec(key.toCharArray());
				PBEParameterSpec pbeParamSpec = new PBEParameterSpec(salt, 20);
				SecretKeyFactory keyFac = provider != null ? SecretKeyFactory
						.getInstance(jceCipher, provider) : SecretKeyFactory
						.getInstance(jceCipher);

				secretKey = keyFac.generateSecret(pbeKeySpec);

				if (log.isDebugEnabled()) {
					log.debug("Getting cipher instance for " + jceCipher
							+ "...");
				}

				if (provider != null) {
					cipherEnc = Cipher.getInstance(jceCipher, provider);
					cipherDec = Cipher.getInstance(jceCipher, provider);
				} else {
					cipherEnc = Cipher.getInstance(jceCipher);
					cipherDec = Cipher.getInstance(jceCipher);
				}

				log
						.debug("Initializing cipher instances for encryption/decryption...");

				cipherEnc.init(Cipher.ENCRYPT_MODE, secretKey, pbeParamSpec);
				cipherDec.init(Cipher.DECRYPT_MODE, secretKey, pbeParamSpec);

			} else {
				if (log.isDebugEnabled()) {
					log.debug("Getting key instance for " + jceSecretKey
							+ "...");
				}

				KeyGenerator keygen = provider != null ? KeyGenerator
						.getInstance(jceSecretKey, provider) : KeyGenerator
						.getInstance(jceSecretKey);

				secretKey = keygen.generateKey();

				if (log.isDebugEnabled()) {
					log.debug("Getting cipher instance for " + jceCipher
							+ "...");
				}

				if (provider != null) {
					cipherEnc = Cipher.getInstance(jceCipher, provider);
					cipherDec = Cipher.getInstance(jceCipher, provider);
				} else {
					cipherEnc = Cipher.getInstance(jceCipher);
					cipherDec = Cipher.getInstance(jceCipher);
				}

				log
						.debug("Initializing cipher instances for encryption/decryption...");

				cipherEnc.init(Cipher.ENCRYPT_MODE, secretKey);
				cipherDec.init(Cipher.DECRYPT_MODE, secretKey);
			}
			log.debug("Finished initializing DataEncryption instance.");

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("JCE exception", e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException("JCE exception", e);
		} catch (InvalidKeyException e) {
			throw new RuntimeException("JCE exception", e);
		} catch (InvalidAlgorithmParameterException e) {
			throw new RuntimeException("JCE exception", e);
		} catch (InvalidKeySpecException e) {
			throw new RuntimeException("JCE exception", e);
		} catch (NoSuchProviderException e) {
			throw new RuntimeException("JCE exception", e);
		}
	}

	/**
	 * Decrypt a Base64-encoded encrypted string (previously enctyped
	 * with the same and instance of DataEntyption configured with 
	 * the same encryption properties).
	 * 
	 * @param encryptedData the enctypted string
	 * @return the decrypted string
	 */
	public synchronized String decrypt(String encryptedData) {
		if (log.isDebugEnabled()) {
			log.debug("Decrypting data: " + encryptedData);
		}
		try {
			byte[] ciphertext = Base64.decode(encryptedData.toCharArray());
			byte[] text = cipherDec.doFinal(ciphertext);
			String result = new String(text);
			return result;
		} catch (Exception e) {
			throw new RuntimeException("JCE exception", e);
		}
	}

	/**
	 * Encrypt a String into a Base64 encoded String.
	 * @param data the string to encrypt
	 * @return the Base64-encoded encrypted value
	 */
	public synchronized String encrypt(String data) {
		if (log.isDebugEnabled()) {
			log.debug("Encrypting data: " + data);
		}
		try {
			byte[] ciphertext = cipherEnc.doFinal(data.getBytes());
			String result = String.valueOf(Base64.encode(ciphertext));
			if (log.isDebugEnabled()) {
				log.debug("Encrypted data '" + data + "' = " + result);
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException("JCE exception", e);
		}
	}

}
