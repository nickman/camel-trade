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
package org.tradex.camel.endpoint.file;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * <p>Title: FilePollerEndpoint</p>
 * <p>Description: A Java DSL based file poller endpoint builder implemented for additional configurability</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.camel.endpoint.file.FilePollerEndpoint</code></p>
 */

public class FilePollerEndpoint extends RouteBuilder implements BeanNameAware, ApplicationContextAware, InitializingBean, CamelContextAware {
	/** The injected camel context */
	protected CamelContext camelContext = null;
	/** The injected spring context */
	protected ApplicationContext applicationContext = null;
	/** The spring bean name */
	protected String beanName = null;
	/** The file poll delay */
	protected long delay = DEFAULT_DELAY;
	/** A map defining the file pollers to be implemented. The key is the directory to poll from, the value is the regex to match file names */
	protected final Map<String, String> filePollers = new ConcurrentHashMap<String, String>();
	/** The suffix appended to the file poller directories to create the fault directory where failed files are moved to */
	/** Indicates if Camel should delete the file after processing */
	protected boolean deleteFiles = true;
	/** Indicates if if an idempotent repository should be used */
	protected boolean useIdempotentRepo = true;
	
	/** The discovered bean name of the idempotent repository prototype */
	protected String idempotentFactoryName = null;
	
	
	/** Instance logger */
	protected final Logger log = Logger.getLogger(getClass());
	
	/** The default file polling delay */
	public static long DEFAULT_DELAY = 500;
	
	/**
	 * Creates a new FilePollerEndpoint
	 */
	public FilePollerEndpoint() {
	}
	
	/** A string template to define a file poller URI without an idempotent repository */
	public static final String FILE_POLLER_TEMPLATE = "file://%s?" + 
			"include=%s&" + 
			"delay=%s&" +
			"delete=%s";
	
	/** A string template to define a file poller URI using an idempotent repository */
	public static final String IDEMPOTENT_FILE_POLLER_TEMPLATE = FILE_POLLER_TEMPLATE + 
			"idempotentRepository=#%s" +
			"idempotent=true";
	
	
	/**
	 * {@inheritDoc}
	 * @see org.apache.camel.builder.RouteBuilder#configure()
	 */
	@Override
	public void configure() throws Exception {
		RouteDefinition rd = new RouteDefinition();
		StringBuilder filePollerMsg = new StringBuilder("\nFile Poller URIs");
		for(Map.Entry<String, String> entry: filePollers.entrySet()) {
			String uri = String.format(FILE_POLLER_TEMPLATE, entry.getKey(), entry.getValue(), delay);
			rd.from(uri);
			filePollerMsg.append("\n\t").append(uri);			
		}
		log.info(filePollerMsg);
		
	}
	

	/**
	 * {@inheritDoc}
	 * @see org.apache.camel.CamelContextAware#setCamelContext(org.apache.camel.CamelContext)
	 */
	@Override
	public void setCamelContext(CamelContext camelContext) {
		this.camelContext = camelContext;
		

	}

	/**
	 * {@inheritDoc}
	 * @see org.apache.camel.CamelContextAware#getCamelContext()
	 */
	@Override
	public CamelContext getCamelContext() {
		return camelContext;
	}



	/**
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		if(useIdempotentRepo) {
			// idempotentFactoryName
			String[] repoNames = applicationContext.getBeanNamesForType(IdempotentRepository.class, true, false);
			if(repoNames.length==0) {
				throw new Exception("No IdempotentRepository Bean Found", new Throwable());
			}
			for(String repoName : repoNames) {
				if(applicationContext.isPrototype(repoName)) {
					idempotentFactoryName = repoName;
					break;
				}
			}
			if(idempotentFactoryName==null) {
				idempotentFactoryName = repoNames[0];
			}
		}
	}



	/**
	 * {@inheritDoc}
	 * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}


	/**
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
	 */
	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

}
