package com.identifire.rest;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.SQLException;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.identifire.db.access.BaseDBManager;
import com.identifire.env.FireEnv;
import com.identifire.model.FEmails;
import com.identifire.model.FSchema;
import com.identifire.model.HRecord;
import com.identifire.model.ICommonStrings;

public class AbstractTableManager extends BaseDBManager {

	/**
	 * 
	 * @return
	 */
	protected static com.identifire.db.access.TableManager getTableManager() {
		return com.identifire.db.access.TableManager.getInstance();
	}

	/**
	 * 
	 * @param requestBody
	 * @return
	 * @throws SQLException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String decryptRequest(Object requestBody) throws SQLException, NoSuchAlgorithmException, InvalidKeyException,
			InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		if (FireEnv.getInstance().isTestMode()) {
			return decryptRequest(requestBody, true);
		}
		JsonObjectBuilder builder = parse(requestBody);
		JsonObject json0 = builder.build();
		String session_id = json0.getString(SESSION_ID);
		if (session_id.equals("'default'")) {
			session_id = getDefaultKey();
		}
		session_id = session_id.replaceAll("'", "");
		String secret = session_id.replaceAll("-", "");
		String cipherText = json0.getString(DATA);

		return fcencrypt(cipherText, secret);
	}

	protected SecretKey getSecretKey(String text, String salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		KeySpec spec = new PBEKeySpec(text.toCharArray(), salt.getBytes(), 65536, 256);
		SecretKey secret = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
		return secret;
	}

	public static IvParameterSpec generateIv() {
		byte[] iv = new byte[16];
		new SecureRandom().nextBytes(iv);
		return new IvParameterSpec(iv);
	}
	
	public static SecretKey generateKey(int n) throws NoSuchAlgorithmException {
	    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
	    keyGenerator.init(n);
	    SecretKey key = keyGenerator.generateKey();
	    return key;
	}
	
	public static SecretKey getKeyFromPassword(String password, String salt)
		    throws NoSuchAlgorithmException, InvalidKeySpecException {
		    
		    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		    KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
		    SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
		        .getEncoded(), "AES");
		    return secret;
	}


	protected String doEncrypt(String algorithm, String input, SecretKey key, IvParameterSpec iv)
			throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
			InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.ENCRYPT_MODE, key, iv);
		byte[] cipherText = cipher.doFinal(input.getBytes());
		return Base64.getEncoder().encodeToString(cipherText);
	}

	protected String doDecrypt(String algorithm, String cipherText, SecretKey key, IvParameterSpec iv)
			throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
			InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

		Cipher cipher = Cipher.getInstance(algorithm);
		cipher.init(Cipher.DECRYPT_MODE, key, iv);
		byte[] plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText));
		return new String(plainText);
	}

	protected String getDefaultKey() {
		return "5aef422ab63d4b4f";
	}

	protected String getSecretKey() {
		return "0d46a6e0b888494c";
	}

	/**
	 * 
	 * @param mail
	 * @return
	 * @throws Exception
	 */
	protected boolean isEmailExists(String mail) throws Exception {

		HRecord hr = new HRecord(FSchema.SCHEMA, FEmails.TABLE);
		String where = String.format(ICommonStrings.WHERE_STR, FEmails.EMAIL, mail);
		hr.query(where);
		boolean isExists = hr.next();
		hr.close();
		return isExists;
	}

	/**
	 * 
	 * @param mail
	 * @return
	 */
	protected boolean isEmailValid(String mail) {
		boolean result = true;
		try {
			InternetAddress emailAddr = new InternetAddress(mail);
			emailAddr.validate();
		} catch (AddressException ex) {
			result = false;
		}
		return result;
	}
}
