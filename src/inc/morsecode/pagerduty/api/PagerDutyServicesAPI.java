package inc.morsecode.pagerduty.api;

import inc.morsecode.NDS;
import inc.morsecode.core.ListResult;
import inc.morsecode.nas.UIMAlarmMessage;
import inc.morsecode.pagerduty.data.PDService;
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

public class PagerDutyServicesAPI {
	
	public static final String GET = "GET";
	private PDClient client;
	private static final String CONTEXT= "services";
	
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
	
	
	public PagerDutyServicesAPI(PDClient client) {
		this.client= client;
	}

	public List<PDService> listServices() throws IOException, MalformedJsonException {
		
		ListResult<PDService> all= listServices(0, 49);
		
		int total= all.getCount();
		
		if (total < 50) {
			return all;
		} else {
			
			// break requests into chunks
			int chunksize= 25;
			
			for (int i= 49; i < total; i+= chunksize) {
				all.addAll(listServices(i, chunksize));
			}
			
		}
		
		return all;
		
	}

	public ListResult<PDService> listServices(int offset, int limit) throws IOException, MalformedJsonException {
		
		NDS params= new NDS();
		params.set("offset", offset);
		params.set("limit", limit);
		
		String url= client.urls().getServiceList(client);
		
		JsonObject data= client.call(GET, url, null, params);
		
		JsonArray array= (JsonArray)data.get("services", new JsonArray());
		ListResult<PDService> services= new ListResult<PDService>(data.get("total", 0));
		
		for(JsonValue obj : array) {
			if (obj instanceof JsonObject) {
				PDService service= new PDService(((JsonObject) obj).get("name", "invalid"), ((JsonObject) obj));
				services.add(service);
			}
		}
		
		return services;
	}
	
}
