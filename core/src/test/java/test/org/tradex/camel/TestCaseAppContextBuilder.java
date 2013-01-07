/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2007, Helios Development Group and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org. 
 *
 */
package test.org.tradex.camel;

import java.io.File;


import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.tradex.spring.ApplicationContextService;

/**
 * <p>Title: TestCaseAppContextBuilder</p>
 * <p>Description: A test fixture to locate a test case's spring.xml and launch an app context with it.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>test.org.tradex.camel.TestCaseAppContextBuilder</code></p>
 */

public class TestCaseAppContextBuilder {
	/** The root directory of resources */
	public static final String ROOT_RESOURCE_DIR = "./src/test/resources/";
	/** The default spring XML file name */
	public static final String DEFAULT_FILE_NAME = "spring.xml";
	
	/** Static class logger */
	protected static final Logger LOG = Logger.getLogger(TestCaseAppContextBuilder.class);
	
	
	/**
	 * Creates a new application context using the file found at <code>ROOT_RESOURCE_DIR + clazz.getPackageName + fileName</code>
	 * @param fileName The filename in the calculated directory
	 * @param clazz The clazz we're launching the app context for
	 * @return the app context
	 */
	public static GenericXmlApplicationContext buildFor(String fileName, Class<?> clazz) {
		if(clazz==null) throw new IllegalArgumentException("Passed class was null", new Throwable());		
		File springXml = new File(ROOT_RESOURCE_DIR + clazz.getPackage().getName().replace('.', '/') + "/" + fileName);
		if(!springXml.canRead()) {
			throw new RuntimeException("Failed to read Spring XML file at [" + springXml + "]", new Throwable());
		}
		return service(new GenericXmlApplicationContext(new FileSystemResource[]{new FileSystemResource(springXml.getAbsoluteFile())}));		
	}
	
	/**
	 * Creates a new application context using the file found at <code>ROOT_RESOURCE_DIR + clazz.getPackageName + DEFAULT_FILE_NAME</code>
	 * @param clazz The clazz we're launching the app context for
	 * @return the app context
	 */
	public static GenericXmlApplicationContext buildFor(Class<?> clazz) {
		return buildFor(DEFAULT_FILE_NAME, clazz);
	}
	
	/**
	 * Builds an application context reading the file from the passed path
	 * @param path The path of the app context xml
	 * @return the built app context
	 */
	public static GenericXmlApplicationContext buildFor(String path) {
		if(path==null) throw new IllegalArgumentException("Passed path was null", new Throwable());		
		File springXml = new File(path);
		if(!springXml.canRead()) {
			throw new RuntimeException("Failed to read Spring XML file at [" + springXml + "]", new Throwable());
		}		
		try {
			return service(new GenericXmlApplicationContext(new FileSystemResource[]{new FileSystemResource(springXml.getAbsoluteFile())}));
		} catch (Throwable t) {
			LOG.fatal("Failed to boot app context", t);
			throw new RuntimeException("Failed to boot app context", t);
		}
	}
	
	/**
	 * Registers the created app context as a JMX service
	 * @param ctx the created app context
	 * @return the created app context
	 */
	protected static GenericXmlApplicationContext service(GenericXmlApplicationContext ctx) {
		ApplicationContextService.register(ctx);
		return ctx;
	}

}
