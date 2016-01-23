package inc.morsecode.pagerduty.api;

import java.util.Iterator;

import inc.morsecode.core.ListResult;
import inc.morsecode.pagerduty.data.PDIncident;

public class IncidentList implements Iterable<PDIncident> {
	
	private ListResult<PDIncident> list;
	
	public IncidentList(ListResult<PDIncident> list) {
		this.list= list;
	}

	public ListResult<PDIncident> getList() {
		return list;
	}
	
	public PDIncident getByKey(String incidentKey) {
		if (incidentKey == null) {
			return null;
		}
		
		for (PDIncident i : list) {
			if (incidentKey.equals(i.getIncidentKey())) {
				return i;
			}
		}
		
		return null;
	}
	

	@Override
	public Iterator<PDIncident> iterator() {
		return list.iterator();
	}
	
}
