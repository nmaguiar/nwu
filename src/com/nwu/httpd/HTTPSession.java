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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;

import com.nwu.httpd.NanoHTTPD.Method;
import com.nwu.httpd.NanoHTTPD.Response;
import com.nwu.log.Log;
import com.nwu.log.Log.Type;

/**
 * HTTPSession represents a httpd helper to serve requests.
 * 
 * <i>Note: Code for customizing NanoHTTPd</i>
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 *
 */
public class HTTPSession {
	/**
	 * The main run method
	 * 
	 */

	public static com.nwu.httpd.NanoHTTPD.Response serve(HTTPd httpd, Log log, String uri, Method method, Map<String, String> headers, Map<String, String> parms,
            Map<String, String> files) {
		String registeredUri = uri;
		
		if (uri.indexOf('/', 1) > -1) {
			registeredUri = registeredUri.substring(0, uri.indexOf('/', 1));
		}
		
		if (!HTTPd.getURIresponses().containsKey(registeredUri)) {
			registeredUri = HTTPd.getDefaultResponse();
		}

		if (HTTPd.getURIresponses().containsKey(registeredUri)) {
			com.nwu.httpd.responses.Response response = null;
			
			log.log(Type.DEBUG, 0, "Using response class '" + HTTPd.getURIresponses().get(registeredUri).getName() + "' for URI = '" + registeredUri + "'"); 

			try {
				Constructor c = HTTPd.getURIresponse(registeredUri).getDeclaredConstructor(HTTPd.class, String.class, Map.class);
				response = (com.nwu.httpd.responses.Response) c.newInstance(httpd, registeredUri, HTTPd.getURIProps(registeredUri));
				response.execute(new Request(uri, method, headers, parms, files));
			} catch (InstantiationException | IllegalAccessException | SecurityException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace();
			}
			
			return response.getResponse();
		} else {
			//return serveFile( uri, header, new File("."), true );
			return null; // TODO: Need to change
		}

	}	
}