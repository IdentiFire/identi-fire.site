package com.identifire.rest;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.identifire.db.access.BaseDBManager;
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
