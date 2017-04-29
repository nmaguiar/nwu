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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import com.nwu.log.Log;
import com.nwu.log.Log.Type;

public class WSd extends NanoWSD {
	protected Log log;
	protected WSd httpd;
	protected int myTcpPort;
	protected Class<WebSocket> webSocket;
	
	public WSd(Log aLog, int port, Class<WebSocket> ws) throws IOException {
		super(port);
		super.LOG = aLog.getLogLogger();
		super.start();
		this.log = aLog;
		this.httpd = this;
		this.webSocket = ws;
		myTcpPort = port;
		log.log(Type.DEBUG, "WSServerSocket created for port: " + myTcpPort);
	}
	
	public WSd(Log aLog, String hostname, int port, Class<WebSocket> ws) throws IOException {
		super(hostname, port);
		super.LOG = aLog.getLogLogger();
		super.start();
		this.log = aLog;
		this.httpd = this;
		this.webSocket = ws;
		myTcpPort = port;
		log.log(Type.DEBUG, "WSServerSocket created for TCP hostname: " + hostname + "; port: " + myTcpPort);
	}

	@Override
	protected WebSocket openWebSocket(IHTTPSession handshake) {
		try {
			return webSocket.getDeclaredConstructor(WebSocket.class, IHTTPSession.class).newInstance(this, handshake);
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | SecurityException | NoSuchMethodException e) {
			return null;
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
