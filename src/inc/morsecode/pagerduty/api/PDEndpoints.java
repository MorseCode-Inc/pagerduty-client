package inc.morsecode.pagerduty.api;

import java.util.HashMap;
import java.util.Map;

import util.StrUtils;
import util.json.JsonObject;
import inc.morsecode.NDS;
import inc.morsecode.pagerduty.data.PDIncident;
import inc.morsecode.pagerduty.data.PDService;
import inc.morsecode.pagerduty.data.PDTriggerEvent;
import inc.morsecode.pagerduty.data.PDUser;

/**
 * 
 * &copy; MorseCode Incorporated 2015<br/>
 * =--------------------------------=<br/><pre>
 * Created: Aug 24, 2015
 * Project: probe-pager-duty-gateway
 *
 * Description:
 * 
 *    
   &lt;service_urls&gt;
   	&lt;event&gt;
   		x_trigger= https://events.pagerduty.com/generic/2010-04-15/create_event.json
   		x_trigger= https://$domain/redonkulize/passthrough/$event.service_key"+ event.getServiceKey();
   		trigger= https://redonkulizer-toy.herokuapp.com/redonkulize/passthrough/$event.service_key"+ event.getServiceKey();
   	&lt;/event&gt;
   	&lt;incident&gt;
   		api= https://$domain/api/v1/incidents
   		resolve= https://$domain/api/v1/incidents/$incident.id/resolve
   		ack= https://$domain/api/v1/incidents/$incident.id/acknowledge
   		reassign= incidents/$incident.id/reassign
   		snooze= incidents/$incident.id/snooze
   	&lt;/incident&gt;
   	&lt;service&gt;
   		api= /api/v1/services
   		create= /api/v1/services
   		update= /api/v1/services/$service.id
   		delete= /api/v1/services/$service.id
   		disable= /api/v1/services/$service.id/disable
   		enable= /api/v1/services/$service.id/enable
   	&lt;/service&gt;
   	
   &lt;/service_urls&gt;
 * 
 * </pre></br>
 * =--------------------------------=
 */
public class PDEndpoints extends NDS {

	
	private HashMap<String, String> cache= new HashMap<String, String>();
	
	public PDEndpoints() { super("service_urls"); }
	public PDEndpoints(String name) { super(name); }
	public PDEndpoints(Map<String, Object> map) { super(map); }
	public PDEndpoints(NDS nds) { super(nds); }
	public PDEndpoints(String name, Map<String, Object> map) { super(name, map); }
	public PDEndpoints(NDS nds, boolean reference) { super(nds, reference); }
	public PDEndpoints(String name, JsonObject json) { super(name, json); }

	
	public String getEventTriggerTemplate() {
		return get("event/trigger", "https://events.pagerduty.com/generic/2010-04-15/create_event.json");
	}
	
	public String getEventTrigger(PDClient client, PDTriggerEvent event) {
		String url= get("event/trigger", "https://events.pagerduty.com/generic/2010-04-15/create_event.json");
		// String url= "https://redonkulizer-toy.herokuapp.com/redonkulize/passthrough/"+ event.getServiceKey();
		
		url= prepareUrl(client, event, url, "event");
		
		return url;
	}
	
	/*
	public String getServiceApi(PDClient client) { 
		String url= get(SERVICE_API, "https://$domain/api/v1/services");
		return prepareUrl(client, url);
	}
	*/
	
	public String getServiceCreate(PDClient client) { 
		return prepareUrl(client, get("service/post:create", "http://$domain/api/v1/services"));
	}
	
	public String getService(PDClient client) {
		return prepareUrl(client, get("service/get:service", "http://$domain/api/v1/services/$incident.id"));
	}
	
	public String getServiceUpdate(PDClient client, PDService service) { 
		return prepareUrl(client, service, get("service/put:update", "http://$domain/api/v1/services/$service.id"), "service"); 
	}
	
	
	public String getServiceList(PDClient client) {
		String url= get("service/get:list", "https://$domain/api/v1/services");
		
		url= prepareUrl(client, url);
		
		return url;
	}
	
	
	
	public String getIncidentApi(PDClient client) { 
		String url= get("incident/api", "https://$domain/api/v1/incidents"); 
		url= prepareUrl(client, url);
		return url;
	}
	
	public String getIncidentCount(PDClient client) { 
		String url= get("incident/get:count", "http://$domain/api/v1/incidents/count"); 
		return prepareUrl(client, url);
	}
	
	public String getIncidentResolve(PDClient client, PDIncident incident) { 
		String url= get("incident/put:resolve", "/api/v1/$incident.id/resolve"); 
		return prepareUrl(client, incident, url, "incident"); 
	}
	
	public String getIncidentUpdate(PDClient client) { 
		String url= get("incident/put:update", "/api/v1/incidents"); 
		return prepareUrl(client, url);
	}
	
	public String getIncidentAck(PDClient client, PDIncident incident) { 
		String url= get("incident/put:ack", "/api/v1/$incident.id/acknowledge");
		return prepareUrl(client, incident, url, "incident");
	}
	
	public String getIncidentReassign(PDClient client, PDIncident incident) { 
		String url= get("incident/put:reassign", "/api/v1/$incident.id/reassign");
		return prepareUrl(client, incident, url, "incident");
	}
	
	public String getIncidentSnooze(PDClient client, PDIncident incident) { 
		String url= get("incident/put:snooze", "/api/v1/$incident.id/snooze");
		url= prepareUrl(client, incident, url, "incident");
		return url;
	}
	
	public String getIncidentList(PDClient client) { 
		String url= get("incident/get:list", "/api/v1/incidents");
		return prepareUrl(client, url);
	}
	
	public String getIncident(PDClient client, String incidentId) { 
		return getIncident(client, new PDIncident(incidentId));
	}
	
	public String getIncident(PDClient client, PDIncident incident) { 
		String url= get("incident/get:incident", "/api/v1/incidents/$incident.id"); 
		String object = "incident";
		return prepareUrl(client, incident, url, object);
	}
	
	public String getUser(PDClient client, PDUser user) { 
		String url= get("user/get:user", "/api/v1/users/$user.id");
		return prepareUrl(client, user, url, "user");
	}
	
	public String getUserOnCall(PDClient client, PDUser user) { 
		String url= get("user/get:oncall", "/api/v1/users/$user.id/on_call");
		return prepareUrl(client, user, url, "user");
	}
	
	public String getUserCreate(PDClient client, PDUser user) { 
		String url= get("user/post:create", "/api/v1/users");
		return prepareUrl(client, user, url, "user");
	}
	
	public String getUserList(PDClient client) { 
		String url= get("user/get:list", "/api/v1/users");
		return prepareUrl(client, url);
	}
	
	public String getUserUpdate(PDClient client, PDUser user) { 
		String url= get("user/put:update", "/api/v1/users/$user.id");
		return prepareUrl(client, user, url, "user");
	}
	
	public String getUserDelete(PDClient client, PDUser user) { 
		String url= get("user/delete:user", "/api/v1/users/$user.id");
		return prepareUrl(client, user, url, "user");
	}
	
	public String prepareUrl(PDClient client, NDS data, String url, String object) {
		NDS vars= new NDS();
		
		for (String key : data.keys()) {
			String value= data.get(key);
			vars.set("\\$"+ object +"."+ key, value);
		}
		
		url= prepareUrl(client, url);
		
		return substitute(url, vars);
	}
	
	public String prepareUrl(PDClient client, String url) {
		
		String hash= StrUtils.SHA(client +":"+ url);
		String cached= cache.get(hash);
		if (cached != null) {
			return cached;
		}
		
		NDS vars= new NDS();
		
		vars.set("\\$domain", client.getDomain());
		vars.set("\\$subdomain", client.getSubdomain());
		vars.set("\\$tld", client.getTopLevelDomain());
		url= substitute(url, vars);
		if (cache.size() > 5000) { cache.clear(); }
		cache.put(hash, url);
		return url;
	}
	
	public String substitute(String url, NDS vars) {
		for (String key : vars.keys()) {
			url= url.replaceAll(key, vars.get(key));
		}
		
		return url;
	}
}
