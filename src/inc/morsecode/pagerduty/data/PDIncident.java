package inc.morsecode.pagerduty.data;

/**
 * 
 * @author bcmorse
 * 
 * Reference Material:
 * https://developer.pagerduty.com/documentation/integration/events/trigger
 */

import inc.morsecode.NDS;
import inc.morsecode.pagerduty.PDConstants.EventType;
import inc.morsecode.util.json.JsonArray;
import inc.morsecode.util.json.JsonObject;

/**
 * 
 * &copy; MorseCode Incorporated 2015<br/>
 * =--------------------------------=<br/><pre>
 * Created: Aug 8, 2015
 * Project: probe-pager-duty-gateway
 *
 * Description:
 * 
 * </pre></br>
 * =--------------------------------=
 */


/**
 * 
 * 
 * &copy; MorseCode Incorporated 2015<br/>
 * =--------------------------------=<br/><pre>
 * Created: Aug 14, 2015
 * Project: probe-pager-duty-gateway
 * 
 *
 * Description:
 * 
 * </pre>
 * =--------------------------------=
 * @author nwhitney
 *
 */
public class PDIncident extends NDS {
	

	 /**
	  * <pre>
	  * 
	  * </pre>
	  * @param name
	  * @param json
	  */
	public PDIncident(JsonObject json) {
		super(json.get("id", "incident"), json);
	}
	
	public PDIncident(String id) {
		super(id);
		setId(id);
	}

	public void setId(String id) {
		set("id", id);
	}

	public String getIncidentNumber() { return get("incident_number"); }
	public String getStatus() { return get("status"); }
	
	public NDS getPendingActions() { return seek("pending_actions", true); }
	
	public String getCreatedOn() { return get("created_on"); }
	public String getHtmlUrl() { return get("html_url"); }
	public String getTriggerDetailsHtmlUrl() { return get("trigger_details_html_url"); }
	
	// possibly could return a PDService object
	// that will take a little work to do 
	public NDS getService() { return seek("service", true); }
	
	public NDS getEscalationPolicy() { return seek("escalation_policy", true); }
	public NDS getTeams() { return seek("teams", true); }
	
	
	
	/**
	 *
	 * 	  "assigned_to": [
				    {
				      "at": "2012-12-22T00:35:21Z",
				      "object": {
				        "id": "PPI9KUT",
				        "name": "Alan Kay",
				        "email": "alan@pagerduty.com",
				        "html_url": "https://acme.pagerduty.com/users/PPI9KUT",
				        "type": "user"
				      }
				    }
				  ]
				  
	 * @return
	 */
	// public IncidentAssignedTo getAssignedTo() { return new IncidentAssignedTo(seek("assigned_to", true)); }
	
	
	
	public PDUser getAssignedToUser() { 
		return new PDUser(seek("assigned_to_user", true)); 
	}
	
	public String getIncidentKey() { return get("incident_key"); }
	
	public String getDateRange() { return get("date_range"); }
	public String getDateRangeSince() { return get("since"); }
	public String getDateRangeUntil() { return get("until"); }
	
	public String getFields() { return get("fields"); }
	// Defaults to UTC. https://developer.pagerduty.com/documentation/rest/types#timezone
	public String getTimeZone() { return get("time_zone"); }
	public String getSortBy() { return get("sort_by"); }
	
	
	
	public NDS getAcknowledgers() { return seek("acknowledgers", true); }
	public NDS getLastStatusChangeOn() { return seek("last_status_change_on", true); }
	public NDS getLastStatusChangeBy() { return seek("last_status_change_by", true); }
	public NDS getTriggerSummaryData() { return seek("trigger_summary_data", true); }
	
	
	
	@Override
	public JsonObject toJson() {
		JsonObject json= super.toJson();
		
		return json;
	}

	public String getId() {
		return this.get("id");
	}

	public boolean isResolved() {
		String status= getStatus();
		return "resolved".equals(status);
	}
	
	
	public PDUser getResolvedBy() {
		if (!isResolved()) { return null; }
		return new PDUser(seek("resolved_by_user"));
	}

	
}
