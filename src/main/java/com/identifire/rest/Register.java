package com.identifire.rest;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.identifire.common.log.HLogger;
import com.identifire.connection.AWSSiteDBConnection;
import com.identifire.connection.DBConnection;
import com.identifire.model.FEmails;
import com.identifire.model.FSchema;

@Path("/actions")
public class Register extends AbstractTableManager {
	
	private static final String EMAIL_ALREADY_EXISTS = "Email Already Exists";
	private static final String EMAIL_INVALIDS = "Email invalids";
	private static final String EMAIL = "email";
	
	private static final String RDS_HOSTNAME 	= "RDS_HOSTNAME";
	
	
	@SuppressWarnings("unused")
	private static HLogger logger = new HLogger(Register.class.getName());
	
	@GET
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/stat")
	public Response getStat() {
		DBConnection.getDBConnection(new AWSSiteDBConnection());
		JSONObject json = new JSONObject();
		json.put("status", "OK");
		return Response.status(Response.Status.OK).entity("Exception Connection ").build();		 	 
	}
	
	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/registerme")
	public Response registerClient(String requestBody) {
		DBConnection.getDBConnection(new AWSSiteDBConnection());
		try {
			testSessionId(requestBody);
			String content = decryptRequest(requestBody);
			JsonObjectBuilder builder = this.parse(content);
			JsonObject json = builder.build();
			JSONObject jsonobject = new JSONObject();
			if (!json.containsKey(EMAIL) || json.getString(EMAIL, "").isEmpty()) {
				Exception e = new Exception();
				return getTableManager().respondWithError(e);
			}
			
			String mail = json.getString(EMAIL);
			/*logger.debug(mail);
			String salt = "12345678";
			String password = getSecretKey();
		    SecretKey key = getKeyFromPassword(password,salt);
		    IvParameterSpec ivParameterSpec = generateIv();
		    String algorithm = "AES/CBC/PKCS5Padding";
		    String cipherText = doEncrypt(algorithm, mail, key, ivParameterSpec);
		    logger.debug(cipherText);
		    String plainText = doDecrypt(algorithm, cipherText, key, ivParameterSpec);
		    logger.debug(plainText);*/
		 
			
			if (!isEmailValid(mail)) {
				Exception e = new Exception(EMAIL_INVALIDS);
				return getTableManager().respondWithError(e);
			}
			if (isEmailExists(mail)) {
				Exception e = new Exception(EMAIL_ALREADY_EXISTS);
				return getTableManager().respondWithError(e);
			}
			
			jsonobject.put(EMAIL, mail);
			//jsonobject.put(IV, new String(ivParameterSpec.getIV()));
			//jsonobject.put("skey", new String(key.getEncoded()));
			Response response = getTableManager().createNew(FSchema.SCHEMA, FEmails.TABLE, jsonobject);
			return response;

		} catch (Exception e) {
			return getTableManager().respondWithError(e);
		}
	}

	public static void main(String[] args) {
		String rb = "data|U2FsdGVkX1+kusM0M3rUggXlt/Wd/aVRKev1GFQWtaFzQJQCVkUBE6ne12knzgHw|,session_id%'default'";
		Register register = new Register();
		register.getStat();
		//register.registerClient(rb);
	}
}
