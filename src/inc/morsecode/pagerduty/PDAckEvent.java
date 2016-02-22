package inc.morsecode.pagerduty;

import inc.morsecode.pagerduty.data.PDService;
import inc.morsecode.util.json.JsonObject;

public class PDAckEvent {
	
	private PDService service;
	private String incidentKey;
	private String description;
	private JsonObject details;

	public PDAckEvent(PDService service, String incidentKey, String description, JsonObject details) {
		this.service= service;
		this.incidentKey= incidentKey;
		this.description= description;
		this.details= details;
	}
	 
	 
	public PDService getService() {
		return service;
	}
	
	public JsonObject toJson() {
		JsonObject json= service.toJson();
		
		json.set("incident_key", incidentKey);
		json.set("description", description);
		json.set("details", details);
		
		return json;
	}

}
