package com.identifire.rest;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.identifire.common.log.HLogger;
import com.identifire.model.FEmails;
import com.identifire.model.FSchema;

@Path("/actions")
public class Register extends AbstractTableManager {

	private static final String EMAIL_ALREADY_EXISTS = "Email Already Exists";
	private static final String EMAIL_INVALIDS = "Email invalids";
	private static final String EMAIL = "email";
	@SuppressWarnings("unused")
	private HLogger logger = new HLogger(Register.class.getName());

	@POST
	@Produces(MediaType.APPLICATION_XML)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/registerme")
	public Response registerClient(String requestBody) {
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
			if (!isEmailValid(mail)) {
				Exception e = new Exception(EMAIL_INVALIDS);
				return getTableManager().respondWithError(e);
			}
			if (isEmailExists(mail)) {
				Exception e = new Exception(EMAIL_ALREADY_EXISTS);
				return getTableManager().respondWithError(e);
			}
			jsonobject.put(EMAIL, mail);
			Response response = getTableManager().createNew(FSchema.SCHEMA, FEmails.TABLE, jsonobject);
			return response;

		} catch (Exception e) {
			return getTableManager().respondWithError(e);
		}
	}

	public static void main(String[] args) {
		String rb = "data|U2FsdGVkX181rzoEhUb0PzbrB4qbaFTH8G7voOMi1Se9gIK8bwzt1AB4/xx6AtiX|,session_id%'aaaa-bbbb-cccc-dddd'";
		Register register = new Register();
		register.registerClient(rb);
	}
}
