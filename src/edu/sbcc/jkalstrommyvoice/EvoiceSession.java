package edu.sbcc.jkalstrommyvoice;

import java.io.*;
import java.util.*;

import org.apache.http.*;
import org.apache.http.client.methods.*;
import org.apache.http.cookie.*;
import org.apache.http.impl.client.*;

import android.content.*;

import com.fasterxml.jackson.databind.*;

/**
 * Singleton for httpclient object; after login it contains authentication cookies from evoice login
 */
public class EvoiceSession extends DefaultHttpClient {
	private static EvoiceSession instance = null;
	private Context context;

	private JsonNode apiResults = null;
	private int status = 1;
	private String errorMessage = "";

	public static EvoiceSession getInstance(Context context) {
		if (instance == null) {
			instance = new EvoiceSession();
			instance.context = context;
		}
		return instance;
	}

	/*
	 * Makes a request to URL, returns status code For other results, use getErrorMessage() and getApiResults()
	 */
	public int sendRequest(String url) {
		InputStream content = sendRequestRaw(url);
		if (content == null) {
			return -1;
		}
		apiResults = parseJSON(content);
		return 1;
	}

	public InputStream sendRequestRaw(String url) {
		errorMessage = "";

		// Prefix with base URL, e.g. http://hostname/context-root/
		// Then make (blocking) request
		url = context.getResources().getString(R.string.base_url) + url;
		InputStream content = null;
		HttpResponse response = null;
		try {
			response = instance.execute(new HttpGet(url));
			content = response.getEntity().getContent();
		} catch (Exception e) {
			errorMessage = "Cannot connect: " + e.getLocalizedMessage();
		}
		return content;
	}

	// Parse JSON response
	private JsonNode parseJSON(InputStream content) {
		if (status != 1) {
			return null;
		}

		ObjectMapper m = new ObjectMapper();
		// can either use mapper.readTree(source), or mapper.readValue(source, JsonNode.class);
		try {
			JsonNode rootNode = m.readTree(content);
			// error : {"api_results":{"returncode":"-7","errordescription":"Password not valid."}}
			// success: {"api_results":{"returncode":"1",...}}
			// But mostly what we care about from a successful login is the COOKIES; we preserve them
			// by reusing the HTTP connection
			JsonNode returncode = rootNode.findValue("returncode");
			if (returncode == null) {
				status = -1;
				errorMessage = "Invalid JSON response";
				return null;
			}
			status = returncode.asInt();
			if (status != 1) {
				JsonNode message = rootNode.findValue("errordescription");
				errorMessage = message != null ? message.asText() : "JSON status = " + status;
			}
			JsonNode apiResultsNode = rootNode.findValue("api_results");
			return apiResultsNode != null ? apiResultsNode : rootNode;
		} catch (Exception e) {
			status = -1;
			errorMessage = "Server error: " + e.getLocalizedMessage();
		}
		return null;
	}

	public JsonNode getApiResults() {
		return apiResults;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public Map<String, String> getCookieHeaders() {
		HashMap<String, String> headers = new HashMap<String, String>();
		String value = "mobile=\"true\"; ";
		for (Cookie cookie : getCookieStore().getCookies()) {
			value += cookie.getName() + "=\"" + cookie.getValue() + "\"; ";
		}
		headers.put("Cookie", value);
		return headers;
	}

}
