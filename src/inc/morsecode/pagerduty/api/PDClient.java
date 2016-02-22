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
import inc.morsecode.util.json.JsonObject;

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

import util.json.ex.MalformedJsonException;
import util.kits.JsonParser;


public class PDClient {

	private static final String UTF_8 = "UTF-8";
	
	private static final String HTTP_PUT = "put";
	private static final String HTTP_DELETE = "delete";
	private static final String HTTP_POST = "post";
	private static final String HTTP_GET = "get";
	private static final String HTTP_HDR_CONTENT_TYPE = "Content-Type";
	private static final String HTTP_HDR_AUTHORIZATION = "Authorization";
	
	private static final String KEY_STATUS = "status";
	private static final String KEY_ERROR = "error";
	private static final String KEY_CODE = "code";
	private static final String KEY_MESSAGE = "message";
	private static final String KEY_TOKEN = "token";
	private static final String KEY_NAME = "name";
	private static final String KEY_AUTH_USER_ID = "auth/user_id";
	private static final String KEY_AUTH_API_KEY = "auth/api_key";
	private static final String KEY_DOMAIN = "domain";
	private static final String KEY_SUBDOMAIN = "subdomain";
	private static final String KEY_SERVICES = "services";
	
	private static final String PAGERDUTY_TLD = "pagerduty.com";
	private static final String DEFAULT_SUBDOMAIN = "events";
	
	private NDS data= new NDS(PDClient.class.getSimpleName());
	private NDS services;
	private HttpClient restlet;
	
	private PagerDutyIncidentsAPI incidents;
	private PagerDutyServicesAPI servicesApi;
	private PagerDutyUsersAPI usersApi;
	private PDEndpoints urls;
	private boolean debugging= false;
	
	public PDClient(String subdomain, String apiKey, PDEndpoints urls, String userid) {
		this(subdomain, PAGERDUTY_TLD, apiKey, urls, userid);
	}
	
	public PDClient(String subdomain, String domain, String apiKey, PDEndpoints urls, String userid) {
		this.services= data.seek(KEY_SERVICES, true);
		this.urls= urls;
		data.set(KEY_SUBDOMAIN, subdomain);
		data.set(KEY_DOMAIN, domain);
		data.set(KEY_AUTH_API_KEY, apiKey);
		data.set(KEY_AUTH_USER_ID, userid);
		
		this.restlet= HttpClients.createDefault();
		
	}

	public PDEndpoints urls() {
		return urls;
	}
	
	public String getSubdomain() { return data.get(KEY_SUBDOMAIN, DEFAULT_SUBDOMAIN); }
	
	public String getUserid() { return data.get(KEY_AUTH_USER_ID);  }
	
	public String getDomain() { return getSubdomain() +"."+ getTopLevelDomain(); }
	
	public String getTopLevelDomain() {
		return data.get(KEY_DOMAIN, PAGERDUTY_TLD);
	}
	
	public String getBaseUrl(String protocol, String apiVer) {
		return (protocol +"://"+ getDomain()); // +"/api/"+ apiVer);
	}
	
	public String getApiToken() {
		return data.get(KEY_AUTH_API_KEY, (String)null);
	}
	
	public void addService(String name, String token) {
		NDS service= services.seek(name, true);
		service.set(KEY_NAME, name);
		service.set(KEY_TOKEN, token);
	}
	
	public String getServiceToken(String name) {
		return services.get(name +"/"+ KEY_TOKEN, (String) null);
	}
	
    public HttpGet get(String uri, NDS params, boolean auth) {
    	return (HttpGet)this.http(HTTP_GET, uri, null, params, auth);
    }
	
    public HttpRequest buildPostRequest(String uri, JsonObject data, NDS params, boolean auth) {
    	return this.http(HTTP_POST, uri, data, params, auth);
    }
    
    public HttpRequest delete(String uri, JsonObject data, NDS params, boolean auth) {
    	return this.http(HTTP_DELETE, uri, data, params, auth);
    }

    public HttpRequest put(String uri, JsonObject data, NDS params, boolean auth) {
    	return this.http(HTTP_PUT, uri, data, params, auth);
    }
    
    public HttpRequest post(String uri, JsonObject data, NDS params, boolean auth) {
    	return this.http(HTTP_POST, uri, data, params, auth);
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
					uri+= delim + URLEncoder.encode(key, UTF_8) +"="+ URLEncoder.encode(string, UTF_8);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
    			delim="&";
    		}
    		
    	}
		HttpEntity entity= null;
    	
    	if (HTTP_GET.equalsIgnoreCase(method)) {
    		request= new HttpGet(uri);
    		// HttpGet getUrl= (HttpGet)request;
    		
    		// System.err.println("uri: "+ uri);
    		// System.err.println("URL: "+ ((HttpUriRequest) getUrl).getURI());
    		
    	} else if (HTTP_DELETE.equalsIgnoreCase(method)) {
    		request= new HttpDelete(uri);
    	
    	} else {
			entity= entity(data);
			
			if (HTTP_PUT.equalsIgnoreCase(method)) {
			
				request= new HttpPut(uri);
				if (data != null) {
					((HttpPut)request).setEntity(entity);
					request.setHeader(HTTP_HDR_CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
				}
			
			} else if (HTTP_POST.equalsIgnoreCase(method)) {
				request= new HttpPost(uri);
				if (data != null) {
					((HttpPost)request).setEntity(entity);
					request.setHeader(HTTP_HDR_CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
				}
			} else {
				throw new RuntimeException("Unsupported HTTP Method: "+ method +" "+ uri);
			}
		}
    	
    	if (auth) {
    		request.setHeader(HTTP_HDR_AUTHORIZATION, "Token token="+ getApiToken());
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
		
		if (HTTP_PUT.equalsIgnoreCase(httpMethod)) {
			request= put(uri, data, params, auth);
		} else if (HTTP_GET.equalsIgnoreCase(httpMethod)) {
			request= get(uri, params, auth);
		} else if (HTTP_DELETE.equalsIgnoreCase(httpMethod)) {
			request= delete(uri, data, params, auth);
		} else if (HTTP_POST.equalsIgnoreCase(httpMethod)) {
			request= post(uri, data, params, auth);
		} else {
			// unsupported method
			throw new RuntimeException("Unsupported HTTP Method: "+ httpMethod +".  Denying access to "+ uri +" {"+ data.toString().replaceAll("\r\n\t",  " ") +"}");
		}
		
		System.out.println("HTTPClient\t"+ request.getRequestLine());
		
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
					
					json.set(KEY_STATUS, code);
					json.set(KEY_MESSAGE, message);
					
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
							if (debugging) {
								System.err.println(string);
							}
							// throw new RuntimeException("Client Error, Malformed JSON Response from server: "+ mfx.getMessage() +"\n"+ string);
							throw mfx;
						}
					}
					
					
					if (json == null) {
						throw new RuntimeException("Client Error, Malformed JSON Response, 'null' from: "+ request +"");
					}
					
					JsonObject error= json.getObject(KEY_ERROR, null);
					
					if (error != null) {
						// there is an error object in the response data
						int errorCode= error.get(KEY_CODE, 0);
						String errorMessage= error.get(KEY_MESSAGE, "no message");
						
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

	protected String read(HttpEntity entity) throws IOException {
		ByteArrayOutputStream baos= new ByteArrayOutputStream();
		
		byte[] buffer= new byte[2048];
		
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
