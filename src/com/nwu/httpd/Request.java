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

import java.util.Properties;

public class Request {
	String uri;
	MethodType method;
	Properties header;
	Properties params;
	
	public enum MethodType {
		GET,
		POST,
		UPDATE,
		DELETE,
		HEAD
	}
	
	public MethodType strMethod2Type(String method) {
		MethodType mtype = null;
		
		if (method.equalsIgnoreCase(MethodType.GET.toString())) {
			return MethodType.GET;
		} else if (method.equalsIgnoreCase(MethodType.POST.toString())) {
			return MethodType.POST;
		} else if (method.equalsIgnoreCase(MethodType.UPDATE.toString())) {
			return MethodType.UPDATE;
		} else if (method.equalsIgnoreCase(MethodType.DELETE.toString())) {
			return MethodType.DELETE;
		} else if (method.equalsIgnoreCase(MethodType.HEAD.toString())) {
			return MethodType.HEAD;
		}
		
		return mtype;
	}
	
	public Request(String uri, String method, Properties header, Properties params) {
		this.uri = uri;
		this.method = strMethod2Type(method);
		this.header = header;
		this.params = params;
	}
	
	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public MethodType getMethod() {
		return method;
	}

	public void setMethod(MethodType method) {
		this.method = method;
	}

	public Properties getHeader() {
		return header;
	}

	public void setHeader(Properties header) {
		this.header = header;
	}

	public Properties getParams() {
		return params;
	}

	public void setParams(Properties params) {
		this.params = params;
	}

}
