package inc.morsecode.pagerduty.api;

import inc.morsecode.NDS;
import inc.morsecode.core.ListResult;
import inc.morsecode.nas.UIMAlarmMessage;
import inc.morsecode.pagerduty.data.PDUser;
import inc.morsecode.util.json.JsonArray;
import inc.morsecode.util.json.JsonObject;
import inc.morsecode.util.json.JsonValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;

import util.json.ex.MalformedJsonException;

public class PagerDutyUsersAPI {
	
	public static final String GET = "GET";
	private PDClient client;
	private static final String CONTEXT= "users";
	
	public enum Endpoints {
		LIST("GET", CONTEXT)
		, GET("GET", CONTEXT)
		, NEW("POST", CONTEXT)
		, UPDATE("PUT", CONTEXT)
		, DELETE("DELETE", CONTEXT)
		, DISABLE("PUT", CONTEXT)
		, ENABLE("PUT", CONTEXT)
		, REGENERATE("POST", CONTEXT)
		;
		
		private String httpMethod;
		private String context;
		
		Endpoints(String httpMethod, String context) {
			this.httpMethod= httpMethod;
			this.context= context;
		}
		
		public String getContext() { return context; }
		public String getHttpMethod() { return httpMethod; }
	};
	
	
	public PagerDutyUsersAPI(PDClient client) {
		this.client= client;
	}

	public List<PDUser> listUsers() throws IOException, MalformedJsonException {
		
		ListResult<PDUser> all= listUsers(0, 49);
		
		int total= all.getCount();
		
		if (total < 50) {
			return all;
		} else {
			
			// break requests into chunks
			int chunksize= 25;
			
			for (int i= 0; i < total; i+= chunksize) {
				all.addAll(listUsers(i, chunksize));
			}
			
		}
		
		return all;
		
	}

	public ListResult<PDUser> listUsers(int offset, int limit) throws IOException, MalformedJsonException {
		
		NDS params= new NDS();
		params.set("offset", offset);
		params.set("limit", limit);
		
		String url= client.urls().getUserList(client);
		
		JsonObject data= client.call(GET, url, null, params);
		
		JsonArray array= (JsonArray)data.get("users", new JsonArray());
		ListResult<PDUser> users= new ListResult<PDUser>(data.get("total", 0));
		
		for(JsonValue obj : array) {
			if (obj instanceof JsonObject) {
				PDUser user= new PDUser(((JsonObject) obj).get("name", "invalid"), ((JsonObject) obj));
				users.add(user);
			}
		}
		
		return users;
	}
	
	public PDUser getUser(String id) throws IOException, MalformedJsonException {
		
		String url= client.urls().getUser(client, new PDUser(id));
		
		JsonObject json= client.call("GET", url, null, null);
		
		JsonObject userInfo= json.getObject("user");
		
		
		if (userInfo == null) {
			System.err.println("Error getting User Information for user="+ id);
			// System.err.println(json);
			return null;
		}
		
		PDUser user= new PDUser(id, userInfo);
		
		return user;
		
	}
	
}
