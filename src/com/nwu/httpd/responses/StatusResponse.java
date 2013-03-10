/**       
 *	   Licensed to the Apache Software Foundation (ASF) under one
 *     or more contributor license agreements.  See the NOTICE file
 *     distributed with this work for additional information
 *     regarding copyright ownership.  The ASF licenses this file
 *     to you under the Apache License, Version 2.0 (the
 *     "License"); you may not use this file except in compliance
 *     with the License.  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing,
 *     software distributed under the License is distributed on an
 *     "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *     KIND, either express or implied.  See the License for the
 *     specific language governing permissions and limitations
 *     under the License.
 */
package com.nwu.httpd.responses;

import java.io.ByteArrayInputStream;
import java.lang.management.ManagementFactory;
import java.util.Properties;

import org.json.JSONException;
import org.json.JSONObject;

import com.nwu.httpd.Codes;
import com.nwu.httpd.HTTPd;
import com.nwu.httpd.Request;

/**
 * Status response.
 * 
 * This response was built for monitoring and testing proposes. It registers
 * itself on the URI '/Status' and provides status information about the httpd
 * server. 
 * 
 * Currently:
 * <ul>
 *  <li>TCP port - The current assigned TCP port.</li>
 *  <li>URI responses number - The current number of assigned URI responses registered.<li>
 * </ul>
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 *
 */
public class StatusResponse extends JsonResponse {
	//public static final String URI = "/Status";
	StringBuffer out;
	
	/**
	 * Builds a HTML header for displaying the status
	 * 
	 * @param out The StringBuffer to use.
	 */
	public void addHeader(StringBuffer out) {
		out.append("<html><title>Status</title><body>");
		 out.append("<table border=1 cellspacing=1>");	
	}
	
	/**
	 * Builds a HTML line for displaying status lines
	 * 
	 * @param out The StringBuffer to use.
	 * @param a1 The first column.
	 * @param a2 The second column.
	 */
	public void addLine(StringBuffer out, String a1, String a2) {
		out.append("<tr><td>" + a1 + "</td><td>" + a2 + "</td></tr>");
	}
	
	/**
	 * Builds a HTML footer for displaying the status
	 * 
	 * @param out The StringBuffer to use.
	 */
	public void addFooter(StringBuffer out) {
		 out.append("</table>");
		out.append("</body></html>");		
	}
	
	protected JSONObject respond2Properties(Properties p) {
		JSONObject props1 = new JSONObject();
		try {
			props1.put("TCP port", httpd.getMyTcpPort());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return props1;
	}
	
	protected JSONObject respond2URIResponsesNumber(Properties p) {
		JSONObject props1 = new JSONObject();
		try {
			props1.put("Size", HTTPd.getURIresponses().size());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return props1;		
	}
	
	protected JSONObject respond2ListOfResponses(Properties p) {
		JSONObject props1 = new JSONObject();
		try {
			for (String response : HTTPd.getURIresponses().keySet()) {
				props1.put(response, HTTPd.getURIresponses().get(response).getName());
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return props1;
	}
	
	protected JSONObject respond2ListOfResponsesHits(Properties p) {		
		JSONObject props1 = new JSONObject();
		try {
			for (String response : HTTPd.getURIhits().keySet()) {
				props1.put(response, HTTPd.getURIhits().get(response));
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return props1;
	}
	
	protected JSONObject respond2Memory(Properties p) {
		JSONObject props1 = new JSONObject();
		try {
			props1.put("Total memory", Runtime.getRuntime().totalMemory());
			props1.put("Free memory", Runtime.getRuntime().freeMemory());
			props1.put("Maximum memory", Runtime.getRuntime().maxMemory());
			props1.put("Number of threads", Thread.activeCount());
			props1.put("System load", ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage());
			props1.put("Uptime", ManagementFactory.getRuntimeMXBean().getUptime());
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return props1;
	}
	
	/**
	 * Creates the status response.
	 * 
	 * @param httpd The httpd server context.
	 * @param request The request.
	 */
	public StatusResponse(HTTPd httpd, String rUri, Properties props) {
		super(httpd, rUri, props);
		
		add("q", "properties", new JsonAnswer() { @Override public JSONObject answer(Properties p) { return respond2Properties(p); } });	
		add("q", "uriresponsesnumber", new JsonAnswer() { @Override public JSONObject answer(Properties p) { return respond2URIResponsesNumber(p); } });
		add("q", "listofresponses", new JsonAnswer() { @Override public JSONObject answer(Properties p) { return respond2ListOfResponses(p); } });
		add("q", "listofresponseshits",	new JsonAnswer() { @Override public JSONObject answer(Properties p) { return respond2ListOfResponsesHits(p); } });		
    	add("q", "memory", new JsonAnswer() { @Override public JSONObject answer(Properties p) { return respond2Memory(p); } });
	}
	
	/**
	 * Registers StatusResponse on the "/Status" URI
	 * 
	 */
//	public static void register(String URI) {
//		HTTPd.registerURIResponse(URI, StatusResponse.class);
//	}
	
	protected String listOfResponses() {
		String list = "";
		
		for (String response : HTTPd.getURIresponses().keySet()) {
			list += response + "<br>";
		}
		
		return list;
	}
	
	protected String listOfResponsesHits() {
		String list = "";
		
		for (String response : HTTPd.getURIhits().keySet()) {
			list += response + " - " + HTTPd.getURIhits().get(response) + "<br>";
		}
		
		return list;
	}
}
