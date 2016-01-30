package inc.morsecode.pagerduty.api;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.StringTokenizer;


import inc.morsecode.NDS;
import inc.morsecode.etc.ArrayUtils;
import inc.morsecode.pagerduty.PDException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;

import util.json.JsonObject;
import util.json.ex.MalformedJsonException;
import util.kits.JsonParser;


public class PDClient {

	private NDS data= new NDS("pagerduty_client");
	private NDS services;
	private HttpClient restlet;
	
	private PagerDutyIncidentsAPI incidents;
	private PagerDutyServicesAPI servicesApi;
	private PagerDutyUsersAPI usersApi;
	private PDEndpoints urls;
	private boolean debugging= false;
	
	public PDClient(String subdomain, String apiKey, PDEndpoints urls, String userid) {
		this(subdomain, "pagerduty.com", apiKey, urls, userid);
	}
	
	public PDClient(String subdomain, String domain, String apiKey, PDEndpoints urls, String userid) {
		this.services= data.seek("services", true);
		this.urls= urls;
		data.set("subdomain", subdomain);
		data.set("domain", domain);
		data.set("auth/api_key", apiKey);
		data.set("auth/user_id", userid);
		
		this.restlet= HttpClients.createDefault();
		
	}

	public PDEndpoints urls() {
		return urls;
	}
	
	public String getSubdomain() { return data.get("subdomain", "events"); }
	
	public String getUserid() { return data.get("auth/user_id");  }
	
	public String getDomain() { return getSubdomain() +"."+ getTopLevelDomain(); }
	
	public String getTopLevelDomain() {
		return data.get("domain", "pagerduty.com");
	}
	
	public String getBaseUrl(String protocol, String apiVer) {
		return (protocol +"://"+ getDomain()); // +"/api/"+ apiVer);
	}
	
	public String getApiToken() {
		return data.get("auth/api_key", (String)null);
	}
	
	public void addService(String name, String token) {
		NDS service= services.seek(name, true);
		service.set("name", name);
		service.set("token", token);
	}
	
	public String getServiceToken(String name) {
		return services.get(name +"/token", (String) null);
	}
	
    public HttpGet get(String uri, NDS params, boolean auth) {
    	return (HttpGet)this.http("get", uri, null, params, auth);
    }
	
    public HttpRequest buildPostRequest(String uri, JsonObject data, NDS params, boolean auth) {
    	return this.http("post", uri, data, params, auth);
    }
    
    public HttpRequest delete(String uri, JsonObject data, NDS params, boolean auth) {
    	return this.http("delete", uri, data, params, auth);
    }

    public HttpRequest put(String uri, JsonObject data, NDS params, boolean auth) {
    	return this.http("put", uri, data, params, auth);
    }
    
    public HttpRequest post(String uri, JsonObject data, NDS params, boolean auth) {
    	return this.http("post", uri, data, params, auth);
    }
    
    
    private HttpRequest http(String method, String uri, JsonObject data, NDS params, boolean auth) {
    	HttpRequest request= null;
    	if (uri == null) {
    		throw new RuntimeException("URI cannot be null, missing required argument to http(method, uri, data, params).");
    	}
    	
    	uri= uri.trim();
    	
    	if (!uri.toLowerCase().startsWith("http")) { 
    		if (!uri.startsWith("/")) { uri= "/"+ uri; }
    		uri= getBaseUrl("https", "v1") + uri;
    	}
    	
    	if (params != null && params.size() > 0) {
    		String delim= "?";
    		for (String key : params.keys()) {
    			try {
					String string = params.get(key);
					if ("".equals(string) || null == string) { continue; }
					uri+= delim + URLEncoder.encode(key, "UTF-8") +"="+ URLEncoder.encode(string, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
    			delim="&";
    		}
    		
    	}
		HttpEntity entity= null;
    	
    	if ("get".equalsIgnoreCase(method)) {
    		request= new HttpGet(uri);
    		HttpGet getUrl= (HttpGet)request;
    		
    		// System.err.println("uri: "+ uri);
    		// System.err.println("URL: "+ ((HttpUriRequest) getUrl).getURI());
    		
    	} else if ("delete".equalsIgnoreCase(method)) {
    		request= new HttpDelete(uri);
    	
    	} else {
			entity= entity(data);
			
			if ("put".equalsIgnoreCase(method)) {
			
				request= new HttpPut(uri);
				if (data != null) {
					((HttpPut)request).setEntity(entity);
					request.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
				}
			
			} else if ("post".equalsIgnoreCase(method)) {
				request= new HttpPost(uri);
				if (data != null) {
					((HttpPost)request).setEntity(entity);
					request.setHeader("Content-Type", ContentType.APPLICATION_JSON.getMimeType());
				}
			} else {
				throw new RuntimeException("Unsupported HTTP Method: "+ method +" "+ uri);
			}
		}
    	
    	if (auth) {
    		request.setHeader("Authorization", "Token token="+ getApiToken());
    	}
		
    	return request;
    }

	public StringEntity entity(JsonObject data) {
		if (data == null) { return null; }
		return new StringEntity(data.toString(), ContentType.APPLICATION_JSON);
	}
    
    private HttpResponse execute(HttpUriRequest request) {
    	try {
            
            try {
            	HttpResponse response= restlet.execute(request); // , responseHandler);
            	return response;
            } catch (ClientProtocolException x) {
            	System.out.println("Client Error: "+ x.getMessage() +" "+ x.getCause());
            }
    		
    	} catch (IllegalArgumentException iax) {
    		System.err.println("ERROR Illegal Argument:\n"+ iax);
    	} catch (NullPointerException npx) {
    		System.err.println("ERROR NULL Reference:\n"+ npx);
    	} catch (Throwable error) {
    		System.err.println("ERROR:\n"+ error);
    		error.printStackTrace();
    	} finally {
    		System.out.flush();
    		System.err.flush();
    	}
    	
    	return null;
    }
    
	public JsonObject call(String httpMethod, String uri, JsonObject data, NDS params) throws IOException, MalformedJsonException {
		return call(httpMethod, uri, data, params, true);
	}
	public JsonObject call(String httpMethod, String uri, JsonObject data, NDS params, boolean auth) throws IOException, MalformedJsonException {
		
		HttpRequest request= null;
		
		if ("put".equalsIgnoreCase(httpMethod)) {
			request= put(uri, data, params, auth);
		} else if ("get".equalsIgnoreCase(httpMethod)) {
			request= get(uri, params, auth);
		} else if ("delete".equalsIgnoreCase(httpMethod)) {
			request= delete(uri, data, params, auth);
		} else if ("post".equalsIgnoreCase(httpMethod)) {
			request= post(uri, data, params, auth);
		} else {
			// unsupported method
			throw new RuntimeException("Unsupported HTTP Method: "+ httpMethod +".  Denying access to "+ uri +" {"+ data.toString().replaceAll("\r\n\t",  " ") +"}");
		}
		
		System.out.println("HTTPClient\t"+ request.getRequestLine());
		for (Header header : request.getAllHeaders()) {
			System.out.println("HTTPClient\t"+ header);
		}
		
		// send it
		HttpResponse response= execute((HttpUriRequest)request);
		
		if (response != null) {
			
			String message= response.getStatusLine().getReasonPhrase();
			int code= response.getStatusLine().getStatusCode();
			
			System.out.println(code + " "+ message);
			
			HttpEntity entity = response.getEntity();
			
			if (entity != null) {
				String string= read(entity);
				
				if ("".equals(string)) {
					JsonObject json= new JsonObject();
					
					json.set("status", code);
					json.set("message", message);
					
					return json;
					
				} else {
					JsonObject json= null;
					
					try {
						json= JsonParser.parse(string);
					} catch (MalformedJsonException mfx) {
						
						if (string.startsWith("Responses from PD were:") && string.contains("\n")) {
							StringTokenizer tokenizer= new StringTokenizer(string, "\n");
							tokenizer.nextToken();
							string= tokenizer.nextToken();
							string= string.replaceAll(".* content\\(", "");
							string= string.replaceAll("\\)*$", "");
							json= JsonParser.parse(string);
						} else {
							System.err.println("Client Error, Malformed JSON Response from PagerDuty server: "+ mfx.getMessage());
							System.err.println(string);
							// throw new RuntimeException("Client Error, Malformed JSON Response from server: "+ mfx.getMessage() +"\n"+ string);
							throw mfx;
						}
					}
					
					/*
					 * HTTPClient	GET https://morsecode-incorporated.pagerduty.com/api/v1/incidents/1 HTTP/1.1
						HTTPClient	Authorization: Token token=PnKQyzNjQEjsRfodeTwa
						Incident ID: 1
						Incident Service Information:
						<service>
						</service>
						
						Incident JSON:
						{
		 					"error":{
		 						"message":"Your account is expired and cannot use the API."
		 						, "code":2012
		 					}
						}
					 */
					
					if (json == null) {
						throw new RuntimeException("Client Error, Malformed JSON Response from: "+ request +"");
					}
					
					JsonObject error= json.getObject("error", null);
					
					if (error != null) {
						// there is an error object in the response data
						int errorCode= error.get("code", 0);
						String errorMessage= error.get("message", "no message");
						
						switch (errorCode) {
						case 0:
							// probably not an error... 
							break;
						case 2012:
							if ("Your account is expired and cannot use the API.".equalsIgnoreCase(errorMessage)) {
								// we know EXACTLY what this error is, and we should handle it gracefully.
								// throw new PagerDutyApiException(error, errorCode, errorMessage);
								errorMessage+= " Check your user account information [subdomain="+ getSubdomain() +" token="+ getApiToken() +"]";
							}
							throw new PDException("ERR("+ errorCode +"): "+ errorMessage);
							
						default:
							throw new PDException("ERR("+ errorCode +"): "+ errorMessage);
						
						}
					}
					
					
					return json;
				}
			}
		}
		
		return null;
	}

	public String read(HttpEntity entity) throws IOException {
		ByteArrayOutputStream baos= new ByteArrayOutputStream();
		
		byte[] buffer= new byte[2048];
		
		//entity.writeTo(baos);
		InputStream content = entity.getContent();
		while (content.available() > 0) {
			int k= content.read(buffer);
			if (k <= 0) { break; }
			baos.write(buffer, 0, k);
		}
		
		content.close();
		baos.close();
		String string = baos.toString().trim();
		return string;
	}
	
	public PDClient newInstance() {
		return new PDClient(getSubdomain(), getTopLevelDomain(), getApiToken(), urls, getUserid());
	}

	public PagerDutyUsersAPI users() {
		if (this.usersApi == null) {
			this.usersApi= new PagerDutyUsersAPI(this);
		}
		return this.usersApi;
	}
	
	public PagerDutyServicesAPI services() {
		if (this.servicesApi == null) {
			this.servicesApi= new PagerDutyServicesAPI(this);
		}
		return this.servicesApi;
	}
	
	public PagerDutyIncidentsAPI incidents() {
		if (this.incidents == null) {
			this.incidents= new PagerDutyIncidentsAPI(this);
		}
		return this.incidents;
	}
	
	
	public void enableDebugging() { this.setDebugging(true); }
	public void disableDebugging() { this.setDebugging(false); }
	public void setDebugging(boolean debugging) { this.debugging = debugging; }
	
}
