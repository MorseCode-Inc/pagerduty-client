package inc.morsecode.pagerduty.data;

import java.util.Map;

import util.kits.DateKit;

import inc.morsecode.NDS;
import inc.morsecode.NDSValue;
import inc.morsecode.nas.UIMAlarmMessage;
//import inc.morsecode.pagerduty.Probe;
import inc.morsecode.util.StrUtils;
import inc.morsecode.util.json.JsonObject;

public class PDTriggerEvent extends NDS {
	
	private static final String TAG2 = UIMAlarmMessage.OSUSER2;
	private static final String TAG1 = UIMAlarmMessage.OSUSER1;
	private static final String HOSTNAME = UIMAlarmMessage.HOSTNAME;
	private static final String SOURCE = UIMAlarmMessage.SOURCE;
	private static final String ROBOT = UIMAlarmMessage.ROBOT;
	private static final String ORIGIN = UIMAlarmMessage.ORIGIN;
	private static final String DOMAIN = UIMAlarmMessage.DOMAIN;

	private PDService service;
	
	public static final String SERVICE_KEY= "service_key";
	public static final String EVENT_TYPE= "event_type";
	public static final String DESCRIPTION= "description";
	public static final String INCIDENT_KEY= "incident_key";
	public static final String CLIENT= "client";
	public static final String CLIENT_URL= "client_url";
	public static final String DETAILS= "details";
	public static final String CONTEXTS= "contexts";

	public PDTriggerEvent(PDService service, UIMAlarmMessage alarm) {
		
		this.service= service;
		
		setEventType("trigger");
		setServiceKey(service.getServiceKey());
		// setIncidentKey(alarm.getAlarmNimid() +" "+ StrUtils.SHA(alarm.signature()) +":"+ alarm.getAlarmSuppKey());
		// setIncidentKey(StrUtils.SHA(alarm.getAlarmSuppKey() + alarm.getAlarmDomain() + alarm.getAlarmHub() + alarm.getAlarmRobot() + alarm.getAlarmSource() + alarm.getAlarmPrid()));
		setIncidentKey(alarm.getAlarmNimid());
		String message= alarm.getAlarmMessage();
		setDescription(message);
		setDetails(alarm);
		//setClient(Probe.PROBE_NAME +"/"+ Probe.PROBE_VERSION);
		setClient("pagerdutygtw/1.0");
		
	}
	
	public PDService getService() {
		return service;
	}
	
	
	/*
	public JsonObject toJson() {
		JsonObject json= toJson();
		return json;
	}
	*/

	
	public String getServiceKey() { return get(SERVICE_KEY); }
	
	public String getEventType() { return get(SERVICE_KEY); }
	public String getDescription() { return get(DESCRIPTION); }
	public String getIncidentKey() { return get(INCIDENT_KEY); }
	
	public String getClient() { return get(CLIENT); }
	public String getClientUrl() { return get(CLIENT_URL); }
	
	public NDS getDetails() { return seek(DETAILS, true); }
	public NDS getContexts() { return seek(CONTEXTS, true); }
	
	
	public void setServiceKey(String value) { set(SERVICE_KEY, value); }
	public void setEventType(String value) { set(EVENT_TYPE, value); }
	public void setDescription(String value) { set(DESCRIPTION, value); }
	public void setIncidentKey(String value) { set(INCIDENT_KEY, value); }
	public void setClient(String value) { set(CLIENT, value); }
	public void setClientUrl(String value) { set(CLIENT_URL, value); }
	
	public void setDetails(UIMAlarmMessage alarm) { 

		String from= "";
		String space= "";
		for (String key : new String[]{DOMAIN, ORIGIN, ROBOT, SOURCE, HOSTNAME}) {
			String value = alarm.getAlarmField(key, null);
			if (value == null) { continue; }
			from+= space + key +":"+ value;
			space= " ";
		}
		setDetail("From", from);
		
		String tags= "";
		space= "";
		for (String key : new String[]{TAG1, TAG2}) {
			String value = alarm.getAlarmField(key, "");
			if (value.length() == 0) { continue; }
			tags+= space + key +":"+ value;
			space= " ";
		}
		setDetail("Tags", tags);
		
		String severity= alarm.getAlarmSeverity();
		
		setDetail("Severity", severity);
		setDetail("Domain", alarm.getAlarmDomain());
		setDetail("Message", alarm.getAlarmMessage());
		setDetail("Probe", alarm.getAlarmPrid());
		setDetail("Robot", alarm.getAlarmRobot());
		setDetail("Hostname", alarm.getAlarmHostname());
		setDetail("Source", alarm.getAlarmSource());
	}
	
	public void setDetail(String key, String value) {
		set(DETAILS +"/"+ key, value);
	}
	
	public String toString() {
		return toJson().toString();
	}
	
	@Override
	public JsonObject toJson() {
		JsonObject json= new JsonObject();
		
		// required
		json.set("service_key", getServiceKey());
		json.set("event_type", "trigger");
		json.set("description", getDescription());
		
		// optional, but we will set the incident key to keep mapping back to nimsoft
		json.set("incident_key", getIncidentKey());
		
		// json.set("client", "pd_uim_gtw");
		
		// turn the alarm message into a JsonObject
		json.set("details", getDetails().toJson());
		
		if (!getContexts().isEmpty()) {
			json.set("contexts", getContexts().toJson());
		}
		return json;
		
	}
	
}
