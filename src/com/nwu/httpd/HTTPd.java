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
package com.nwu.httpd;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Properties;

import com.nwu.log.Log;
import com.nwu.log.Log.Type;

/**
 * The HTTPd server. Creates a thread to run the httpd server.
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 * 
 */
public class HTTPd {
	protected int myTcpPort;
	protected final ServerSocket myServerSocket;
	protected Thread myThread;
	protected HTTPd httpd;
	protected Log log;
	protected static String defaultResponse = null;

	@SuppressWarnings("rawtypes")
	protected static HashMap<String, Class> URIresponses = new HashMap<String, Class>();
	protected static HashMap<String, Long> URIhits = new HashMap<String, Long>();
	protected static HashMap<String, Properties> URIProps = new HashMap<String, Properties>();
	
	public static String getDefaultResponse() {
		return defaultResponse;
	}

	public static void setDefaultResponse(String defaultResponse) {
		HTTPd.defaultResponse = defaultResponse;
	}
	
	/**
	 * Creates the thread launching the httpd server on the corresponding port.
	 * 
	 * @param port
	 *            The port to assign to this server thread.
	 * @throws IOException
	 *             Exception in case the server is unable to assigned to the
	 *             corresponding port.
	 */
	public HTTPd(Log aLog, int port) throws IOException {
		this.log = aLog;
		this.httpd = this;
		myTcpPort = port;
		myServerSocket = new ServerSocket(myTcpPort);
		log.log(Type.DEBUG, "ServerSocket created for TCP port: " + myTcpPort);
		
		myThread = new Thread(new Runnable() {
			public void run() {
				try {
					while (true)
						new HTTPSession(httpd, myServerSocket.accept());
				} catch (IOException ioe) {
				}
			}
		});
		myThread.setDaemon(true);
		myThread.start();
	}

	/**
	 * Registers a response class to answer requests on a given URI for this
	 * server thread.
	 * 
	 * (Note: will not add anything if the class provided isn't a subclass of
	 * com.nwu.httpd.responses.Response class)
	 * 
	 * @param URI
	 *            The URI to register.
	 * @param aClass
	 *            The response class (com.nwu.httpd.responses.Response) to
	 *            register.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void registerURIResponse(String URI, Class aClass, Properties props) {
		if (aClass != null
				&& aClass.asSubclass(com.nwu.httpd.responses.Response.class) != null) {
			URIresponses.put(URI, aClass);
			URIProps.put(URI, props);
			URIhits.put(URI, new Long(0));
		}
	}

	File myFileDir;

	/**
	 * Return the current registered URI response objects. If none is found
	 * it will return null.
	 * 
	 * @return A HashMap where the key is the URI and the value the 
	 * com.nwu.httpd.responses.Response class.
	 */
	public static HashMap<String, Class> getURIresponses() {
		return URIresponses;
	}
	
	/**
	 * Return the current registered URI properties. If none is found
	 * it will return null.
	 * 
	 * @return A HashMap where the key is the URI and the value are 
	 * Properties.
	 * 
	 * @see java.util.Properties
	 */
	public static HashMap<String, Properties> getURIproperties() {
		return URIProps;
	}
	
	/**
	 * Returns a registered Response Class given an URI
	 * 
	 * @param URI The URI to lookup the corresponding registered class
	 * @return A sub-class of com.nwu.httpd.responses.Response
	 */
	public static Class getURIresponse(String URI) {
		//if (URI.equals("")) URI = "/";
		URI.replaceFirst("/+", "/");
		
		if (getURIresponses().containsKey(URI)) {
			URIhits.put(URI, URIhits.get(URI) + 1);
			return getURIresponses().get(URI);
		}
		
		return null;
	}
	
	/**
	 * Returns the properties for the given URI
	 * 
	 * @param URI The URI to lookup the corresponding properties
	 * @return The properties or null if not found
	 */
	public static Properties getURIProps(String URI) {
		if (URI.equals("")) URI = "/";
		
		if(getURIproperties().containsKey(URI)) {
			return getURIproperties().get(URI);
		}
		
		return null;
	}
	
	/**
	 * Return the current hit counters for each URI response object
	 * 
	 * @return A HashMap where the key is the URI and the value the hit counter
	 */
	public static HashMap<String, Long> getURIhits() {
		return URIhits;
	}

	/**
	 * Stops the server.
	 */
	public void stop() {
		try {
			myServerSocket.close();
			myThread.join();
		} catch (IOException ioe) {
		} catch (InterruptedException e) {
		}
	}

	/**
	 * Provides the current assigned TCP port.
	 * 
	 * @return the port number.
	 */
	public int getMyTcpPort() {
		return myTcpPort;
	}
	
	/**
	 * Obtain the httpd log
	 * 
	 * @return A current Log object
	 */
	public Log getLog() {
		return log;
	}

}
