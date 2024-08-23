package com.vomaksh.hnpocket.server;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.HeaderElement;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.ClientProtocolException;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;

import java.io.IOException;

/**
 * Handles HTML response of a request to login to HN and retrieving its user token.
 * @author manuelmaly
 */
public class GetHNUserTokenResponseHandler implements ResponseHandler<String> {

    private IAPICommand<String> mCommand;
    
    public GetHNUserTokenResponseHandler(IAPICommand<String> command, HttpClient client) {
        mCommand = command;
    }
    
    @Override
    public String handleResponse(HttpResponse response)
            throws ClientProtocolException, IOException {
    	String responseString = null;
    	String redirectToLocation = null;

      Header[] headers = response.getHeaders("Location");
        if (headers.length > 0) {
            HeaderElement[] headerElements = headers[0].getElements();
            if (headerElements.length > 0) {
            	redirectToLocation = headerElements[0].getName();
            }
        }
    	
        if (redirectToLocation != null && redirectToLocation.equals("news")) {
        	responseString = getUserID(response);
        }
        
        mCommand.responseHandlingFinished(responseString, response.getStatusLine().getStatusCode());
        return responseString;
    }
    
    private String getUserID(HttpResponse response) {
    	for (Header header : response.getHeaders("Set-Cookie")) {
    		HeaderElement[] elements = header.getElements();
    		if (elements.length > 0 && elements[0].getName().equals("user")) {
    			return elements[0].getValue();
    		}
    	}
    	return null;
    }
}
