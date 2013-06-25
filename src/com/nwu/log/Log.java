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
package com.nwu.log;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * General logging using log4j
 * 
 * @author Nuno Aguiar <nuno@aguiar.name>
 * 
 */
public class Log {
	/**
	 * Classifies the type of log message
	 * 
	 * @author Nuno Aguiar <nuno@aguiar.name>
	 */
	public enum Type {
		INFO, ERROR, DEBUG, OFF
	};

	/**
	 * Log4J default message pattern
	 */
	protected static String LOG4J_DEFAULT_PATTERN = "%d{dd MMM yyyy HH:mm:ss,SSS};[%p]; %m%n";

	protected String logName;
	protected Logger lOG;

	/**
	 * Creates a instance to log for loggerName. Log4j is configured to output
	 * to console using LOG4J_DEFAULT_PATTERN. The log level is set to INFO.
	 * 
	 * @param loggerName
	 *            The log4j Logger name
	 */
	public Log(String loggerName) {
		logName = loggerName;
		lOG = Logger.getLogger(loggerName);
		BasicConfigurator.configure(new ConsoleAppender(new PatternLayout(
				LOG4J_DEFAULT_PATTERN)));
		lOG.setLevel(getOutputType2Level(Type.INFO));
	}

	/**
	 * Creates a instance to log for loggerName. Log4j is configured to output
	 * to console using LOG4J_DEFAULT_PATTERN.
	 * 
	 * @param loggerName
	 *            The log4j Logger name
	 * @param type
	 *            The log level to use
	 */
	public Log(String loggerName, Type type) {
		logName = loggerName;
		lOG = Logger.getLogger(loggerName);
		BasicConfigurator.configure(new ConsoleAppender(new PatternLayout(
				LOG4J_DEFAULT_PATTERN)));
		lOG.setLevel(getOutputType2Level(type));
	}

	/**
	 * Creates a instance to log for loggerName without configuring Log4j
	 * Use only for overridden the default behavior
	 * 
	 * @param loggerName
	 *            The log4j Logger name
	 * @param type
	 *            The log level to use
	 */
	public Log() {
		
	}
	
	/**
	 * Log a generic message.
	 * 
	 * @param type Type of message (type OFF won't actually do anything)
	 * @param message The message to log
	 */
	public void log(Type type, String message) {
		switch (type) {
		case DEBUG:
			lOG.debug(message);
			break;
		case ERROR:
			lOG.error(message);
			break;
		case INFO:
			lOG.info(message);
			break;
		}
	}
	
	/**
	 * Log a thread message.
	 * 
	 * @param type Type of message (type OFF won't actually do anything)
	 * @param id The thread id
	 * @param message The message to log
	 */
	public void log(Type type, long id, String message) {
		switch (type) {
		case DEBUG:
			lOG.debug(id + "|" + message);
			break;
		case ERROR:
			lOG.error(id + "|" + message);
			break;
		case INFO:
			lOG.info(id + "|" + message);
			break;
		}
	}

	/**
	 * Convert Log type to log4j levels
	 * 
	 * @param type
	 *            The Log type
	 * @return The log4j Level correspondent.
	 */
	public static Level getOutputType2Level(Type type) {
		switch (type) {
		case DEBUG:
			return Level.DEBUG;
		case ERROR:
			return Level.ERROR;
		case INFO:
			return Level.INFO;
		case OFF:
			return Level.OFF;
		}

		return null;
	}
}
