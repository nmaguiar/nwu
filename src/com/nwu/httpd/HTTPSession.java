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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.TimeZone;

import com.nwu.httpd.NanoHTTPD.Response;
import com.nwu.log.Log;
import com.nwu.log.Log.Type;

/**
 * HTTPSession represents a httpd thread able to serve requests.
 * 
 * <i>Note: Code largely based on NanoHTTPd</i>
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 *
 */
public class HTTPSession implements Runnable {
	protected HTTPd httpd;
	protected Log log;
	protected long threadId;
	
	/**
	 * GMT date formatter
	 */
	private static java.text.SimpleDateFormat gmtFrmt;
	static
	{
		gmtFrmt = new java.text.SimpleDateFormat( "E, d MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		gmtFrmt.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * Creates an instance given the httpd context and the server socket
	 * 
	 * @param httpd The httpd context
	 * @param s A server socket
	 */
	public HTTPSession(HTTPd httpd, Socket s) {
		this.httpd = httpd;
		this.log = httpd.getLog();
		
		mySocket = s;
		Thread t = new Thread( this );
		t.setDaemon( true );
		threadId = t.getId();
		log.log(Type.DEBUG, threadId, "Creating a HTTPSession thread (id = " + t.getId() + "; activeThreads = " + t.activeCount() + ") and starting it.");
		t.start();
	}

	/**
	 * The main run method
	 * 
	 */
	public void run() {
		long startTime = System.currentTimeMillis();
		
		try {
			// Gets data from socket
			InputStream is = mySocket.getInputStream();
			if (is == null) return;
			BufferedReader in = new BufferedReader(new InputStreamReader(is));

			// Read the request line
			String inLine = in.readLine();
			if (inLine == null) return;
			StringTokenizer st = new StringTokenizer(inLine);
			if (!st.hasMoreTokens())
				sendError( Codes.HTTP_BADREQUEST, "BAD REQUEST: Syntax error." );

			String method = st.nextToken();

			if ( !st.hasMoreTokens())
				sendError( Codes.HTTP_BADREQUEST, "BAD REQUEST: Missing URI." );

			String uri = st.nextToken();

			// Decode parameters from the URI
			Properties parms = new Properties();
			int qmi = uri.indexOf('?');
			if (qmi >= 0) {
				decodeParms(uri.substring(qmi + 1), parms);
				uri = decodePercent(uri.substring(0, qmi));
			} else {
				uri = decodePercent(uri);
			}
			
			// If there's another token, it's protocol version,
			// followed by HTTP headers. Ignore version but parse headers.
			// NOTE: this now forces header names uppercase since they are
			// case insensitive and vary by client.
			Properties header = new Properties();
			if ( st.hasMoreTokens()) {
				String line = in.readLine();
				while (line.trim().length() > 0) {
					int p = line.indexOf(':');
					header.put(line.substring(0, p).trim().toLowerCase(), line.substring(p + 1).trim());
					line = in.readLine();
				}
			}

			// If the method is POST, there may be parameters
			// in data section, too, read it:
			if (method.equalsIgnoreCase("POST")) {
				long size = 0x7FFFFFFFFFFFFFFFl;
				String contentLength = header.getProperty("content-length");
				
				if (contentLength != null) {
					try { size = Integer.parseInt(contentLength); }
					catch (NumberFormatException ex) {}
				}
				
				String postLine = "";
				char buf[] = new char[512];
				int read = in.read(buf);
				while (read >= 0 && size > 0 && !postLine.endsWith("\r\n"))	{
					size -= read;
					postLine += String.valueOf(buf, 0, read);
					if ( size > 0 )
						read = in.read(buf);
				}
				postLine = postLine.trim();
				decodeParms(postLine, parms);
			}

			// Ok, now do the serve()
			com.nwu.httpd.responses.Response r = serve(uri, method, header, parms);
			
			if ( r == null ) {
				if (httpd.getDefaultResponse() != null) {
					sendErrorHTML(Codes.HTTP_OK, "<html>" +
							"<meta http-equiv=\"refresh\" content=\"0; url=" + httpd.getDefaultResponse() + "\">" +
				            "<body><a href=\"" + httpd.getDefaultResponse() + "\">" +
							httpd.getDefaultResponse()  + "</a></body></html>");
				} else {
					sendError( Codes.HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: Serve() returned a null response." );
				}
			} else {
				if (method.equals(Request.MethodType.HEAD)) {
					sendResponse(r.getStatus(), r.getMimeType(), r.getHeader(), null);
				} else {
					sendResponse(r.getStatus(), r.getMimeType(), r.getHeader(), r.getData());
				}
			}

			in.close();
			// connection=keep-alive
			if ((header.contains("connection")) && (header.get("connection").equals("keep-alive"))) {
				httpd.log.log(Type.DEBUG, "Connection keep-alive request. Connection lasted for " + (System.currentTimeMillis() - startTime));
			}
			
		} catch ( IOException ioe )	{
			try
			{
				sendError( Codes.HTTP_INTERNALERROR, "SERVER INTERNAL ERROR: IOException: " + ioe.getMessage());
			}
			catch ( Throwable t ) {}
		} catch ( InterruptedException ie )	{
			// Thrown by sendError, ignore and exit the thread.
		}
	}

	/**
	 * Decodes the percent encoding scheme. <br/>
	 * For example: "an+example%20string" -> "an example string"
	 */
	private String decodePercent( String str ) throws InterruptedException
	{
		try
		{
			StringBuffer sb = new StringBuffer();
			for( int i=0; i<str.length(); i++ )
			{
				char c = str.charAt( i );
				switch ( c )
				{
				case '+':
					sb.append( ' ' );
					break;
				case '%':
					sb.append((char)Integer.parseInt( str.substring(i+1,i+3), 16 ));
					i += 2;
					break;
				default:
					sb.append( c );
					break;
				}
			}
			return new String( sb.toString().getBytes());
		}
		catch( Exception e )
		{
			sendError( Codes.HTTP_BADREQUEST, "BAD REQUEST: Bad percent-encoding." );
			return null;
		}
	}

	/**
	 * Decodes parameters in percent-encoded URI-format
	 * ( e.g. "name=Jack%20Daniels&pass=Single%20Malt" ) and
	 * adds them to given Properties. NOTE: this doesn't support multiple
	 * identical keys due to the simplicity of Properties -- if you need multiples,
	 * you might want to replace the Properties with a Hastable of Vectors or such.
	 */
	private void decodeParms( String parms, Properties p )
	throws InterruptedException
	{
		if ( parms == null )
			return;

		StringTokenizer st = new StringTokenizer( parms, "&" );
		while ( st.hasMoreTokens())
		{
			String e = st.nextToken();
			int sep = e.indexOf( '=' );
			if ( sep >= 0 )
				p.put( decodePercent( e.substring( 0, sep )).trim(),
						decodePercent( e.substring( sep+1 )));
		}
	}

	/**
	 * Returns an error message as a HTTP response and
	 * throws InterruptedException to stop furhter request processing.
	 */
	private void sendError( String status, String msg ) throws InterruptedException
	{
		sendResponse( status, Codes.MIME_PLAINTEXT, null, new ByteArrayInputStream( msg.getBytes()));
		throw new InterruptedException();
	}
	
	private void sendErrorHTML( String status, String msg ) throws InterruptedException
	{
		sendResponse( status, Codes.MIME_HTML, null, new ByteArrayInputStream( msg.getBytes()));
		throw new InterruptedException();
	}

	/**
	 * Sends given response to the socket.
	 */
	private void sendResponse( String status, String mime, Properties header, InputStream data )
	{
		try
		{
			if ( status == null )
				throw new Error( "sendResponse(): Status can't be null." );

			OutputStream out = mySocket.getOutputStream();
			PrintWriter pw = new PrintWriter( out );
			pw.print("HTTP/1.0 " + status + " \r\n");

			if ( mime != null )
				pw.print("Content-Type: " + mime + "\r\n");

			if ( header == null || header.getProperty( "Date" ) == null )
				pw.print( "Date: " + gmtFrmt.format( new Date()) + "\r\n");

			if ( header != null )
			{
				Enumeration e = header.keys();
				while ( e.hasMoreElements())
				{
					String key = (String)e.nextElement();
					String value = header.getProperty( key );
					pw.print( key + ": " + value + "\r\n");
				}
			}

			pw.print("\r\n");
			pw.flush();

			if (data != null) {
				byte[] buff = new byte[2048];
				while (true)
				{
					int read = data.read( buff, 0, 2048 );
					if (read <= 0)
						break;
					out.write( buff, 0, read );
				}
			}
			
			out.flush();
			out.close();
			if ( data != null )
				data.close();
		}
		catch( IOException ioe )
		{
			// Couldn't write? No can do.
			try { mySocket.close(); } catch( Throwable t ) {}
		}
	}

	private Socket mySocket;

	public com.nwu.httpd.responses.Response serve( String uri, String method, Properties header, Properties parms ) {
		String registeredUri = uri;
		
		if (uri.indexOf('/', 1) > -1) {
			registeredUri = registeredUri.substring(0, uri.indexOf('/', 1));
		}
		
		if (!HTTPd.getURIresponses().containsKey(registeredUri)) {
			registeredUri = HTTPd.getDefaultResponse();
		}

		if (HTTPd.getURIresponses().containsKey(registeredUri)) {
			com.nwu.httpd.responses.Response response = null;
			
			log.log(Type.DEBUG, threadId, "Using response class '" + HTTPd.getURIresponses().get(registeredUri).getName() + "' for URI = '" + registeredUri + "'"); 

			try {
				//Constructor c = HTTPd.getURIresponses().get(registeredUri).getDeclaredConstructor(HTTPd.class, Request.class);
				Constructor c = HTTPd.getURIresponse(registeredUri).getDeclaredConstructor(HTTPd.class, String.class, Properties.class);
				response = (com.nwu.httpd.responses.Response) c.newInstance(httpd, registeredUri, HTTPd.getURIProps(registeredUri));
				response.execute(new Request(uri, method, header, parms));
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return response;
		} else {
			//return serveFile( uri, header, new File("."), true );
			return null; // TODO: Need to change
		}

	}
	
}
