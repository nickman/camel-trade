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

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.management.ObjectName;
import javax.sql.DataSource;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.file.FileEndpoint;
import org.apache.camel.impl.DefaultRoute;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;
import org.tradex.jmx.JMXHelper;

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
	/** The idempotent repository data source */
	protected DataSource dataSource = null;
	
	/** The file poll delay */
	protected long delay = DEFAULT_DELAY;
	/** A map defining the file pollers to be implemented. The key is the directory to poll from, the value is the regex to match file names */
	protected final Map<String, String> filePollers = new ConcurrentHashMap<String, String>();
	/** The idempotent repositories created for each directory */
	protected final Map<String, IdempotentRepository<?>> idempotentRepositories = new ConcurrentHashMap<String, IdempotentRepository<?>>();
	/** The suffix appended to the file poller directories to create the fault directory where failed files are moved to */
	/** Indicates if Camel should delete the file after processing */
	protected boolean deleteFiles = true;
	/** Indicates if if an idempotent repository should be used */
	protected boolean useIdempotentRepo = true;
	/** The uri of the endpoint that polled file exchanges should be forwarded to */
	protected String targetEndpointUri = null;
	/** The uri of the endpoint that successfully completed polled file exchanges should be forwarded to */
	protected String completionEndpointUri = null;
	/** The uri of the endpoint that polled file exchanges that resulted in an exception should be forwarded to */
	protected String exceptionEndpointUri = null;
	/** The throwable classes that will trigger this route's exception handler. Defaults to <b><code>{ {@link Throwable} }</code></b> */
	@SuppressWarnings("unchecked")
	protected Class<? extends Throwable>[] exceptionHandlerTriggers = new Class[]{Throwable.class};
	
	
	
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
	public static final String FILE_POLLER_TEMPLATE = "file:%s?" + 
			"include=%s&" + 
			"delay=%s&" +
			"delete=%s";
	
	/** A string template to define a file poller URI using an idempotent repository */
	public static final String IDEMPOTENT_FILE_POLLER_TEMPLATE = FILE_POLLER_TEMPLATE + "&" + 
			"idempotentRepository=#%s&" +
			"idempotent=true";
	
	
	/**
	 * {@inheritDoc}
	 * @see org.apache.camel.builder.RouteBuilder#configure()
	 */
	@Override
	public void configure() throws Exception {
		errorHandler(deadLetterChannel(exceptionEndpointUri).log("WTF ????"));
		StringBuilder filePollerMsg = new StringBuilder("\nFile Poller URIs");
		for(Map.Entry<String, String> entry: filePollers.entrySet()) {
			String uri = null;
			if(useIdempotentRepo) {
				String repoName = buildIdempotentRepository(entry.getKey());
				uri = String.format(IDEMPOTENT_FILE_POLLER_TEMPLATE, entry.getKey(), entry.getValue(), delay, deleteFiles, repoName);
			} else {
				uri = String.format(FILE_POLLER_TEMPLATE, entry.getKey(), entry.getValue(), delay, deleteFiles);
			}
			
			
			
			from(uri).routeId("FilePollerRoute")
				.id("File Poller [" + entry.getKey() + "]")
				.to(targetEndpointUri)
					.id(beanName + "TargetEndpoint");
			
//			.onCompletion()
//				.id(beanName + "CompletionHandler")
//				.to(completionEndpointUri)
//					.id(beanName + "CompletionEndpoint")
//			
//			.onException(exceptionHandlerTriggers)
//				.id(beanName + "ExceptionHandler")
//				.to(exceptionEndpointUri)
//					.id(beanName + "ExceptionEndpoint");
			
			filePollerMsg.append("\n\t").append(uri);			
		}
		log.info(filePollerMsg);
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		camelContext.addRoutes(this);
	}
	
	
	/**
	 * Builds and registers an idempotent repository for the passed directory name
	 * @param directoryName The directory name files are being polled from
	 * @return The bean name of the idempotent repository
	 */
	protected String buildIdempotentRepository(String directoryName) {
		String idempotentFactoryName = null;
		String[] repoNames = applicationContext.getBeanNamesForType(IdempotentRepository.class, true, false);
		if(repoNames.length==0) {
			throw new RuntimeException("No IdempotentRepository Bean Found", new Throwable());
		}
		for(String repoName : repoNames) {
			if(applicationContext.isPrototype(repoName)) {
				idempotentFactoryName = repoName;
				break;
			}
		}
		if(idempotentFactoryName==null) {
			throw new RuntimeException("No IdempotentRepository Prototype Bean Found", new Throwable());
		}
		String generatedId = directoryName.replace('/', '_').replace('\\', '_').replace(':', '_');
		ObjectName on = JMXHelper.objectName("org.tradex.idempotent:service=IdempotentRepository,name=" + generatedId);
		IdempotentRepository<?> repository = (IdempotentRepository<?>) applicationContext.getBean(idempotentFactoryName, dataSource, generatedId, on);
		((GenericApplicationContext)applicationContext).getBeanFactory().registerSingleton(generatedId, repository);
		//((GenericApplicationContext)applicationContext).getBeanFactory().configureBean(repository, generatedId);
		
		
		idempotentRepositories.put(directoryName, repository);
		log.info("Created and registered Idempotent Repository [" + generatedId + "]");
		return generatedId;
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

	/**
	 * Returns the file poll delay in ms.
	 * @return the file poll delay in ms.
	 */
	public long getDelay() {
		return delay;
	}

	/**
	 * Sets the file poll delay in ms.
	 * @param delay the file poll delay in ms.
	 */
	public void setDelay(long delay) {
		this.delay = delay;
	}

	/**
	 * Indicates if the file pollers should use an idempotent repository
	 * @return true if the file pollers should use an idempotent repository, false otherwise
	 */
	public boolean isUseIdempotentRepo() {
		return useIdempotentRepo;
	}

	/**
	 * Sets if the file pollers should use an idempotent repository
	 * @param useIdempotentRepo true to use an idempotent repository, false otherwise
	 */
	public void setUseIdempotentRepo(boolean useIdempotentRepo) {
		this.useIdempotentRepo = useIdempotentRepo;
	}

	/**
	 * Returns the URI of the endpoint that polled files should be forwarded to
	 * @return the URI of the endpoint that polled files should be forwarded to
	 */
	public String getTargetEndpointUri() {
		return targetEndpointUri;
	}

	/**
	 * Sets the URI of the endpoint that polled files should be forwarded to
	 * @param targetEndpointUri the URI of the endpoint that polled files should be forwarded to
	 */
	public void setTargetEndpointUri(String targetEndpointUri) {
		this.targetEndpointUri = targetEndpointUri;
	}

	/**
	 * Returns a map of the configured file pollers
	 * @return a map of the configured file pollers
	 */
	public Map<String, String> getFilePollers() {
		return filePollers;
	}
	
	/**
	 * Adds the passed map to this bean's file poller map
	 * @param pollers A map of file pollers where the key is the directory and the value is the file name pattern
	 */
	public void setFilePollers(Map<String, String> pollers) {
		filePollers.putAll(pollers);
	}

	/**
	 * Sets the data source bean name to use if idempotent repositories are being used
	 * @param dataSource the data source bean name to use if idempotent repositories are being used
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Sets the deletion policy for polled files
	 * @param deleteFiles true to delete polled files on completion, false otherwise
	 */
	public void setDeleteFiles(boolean deleteFiles) {
		this.deleteFiles = deleteFiles;
	}
	
	/**
	 * Indicates if complete polled files are deleted
	 * @return true if complete polled files are deleted, false otherwise
	 */
	public boolean isDeleteFiles() {
		return deleteFiles;
	}

	/**
	 * Returns the uri of the endpoint that polled file exchanges that resulted in an exception should be forwarded to 
	 * @return the uri of the endpoint that polled file exchanges that resulted in an exception should be forwarded to 
	 */
	public String getCompletionEndpointUri() {
		return completionEndpointUri;
	}

	/**
	 * Sets the uri of the endpoint that polled file exchanges that resulted in an exception should be forwarded to
	 * @param completionEndpointUri the uri of the endpoint that polled file exchanges that resulted in an exception should be forwarded to
	 */
	public void setCompletionEndpointUri(String completionEndpointUri) {
		this.completionEndpointUri = completionEndpointUri;
	}

	/**
	 * Returns the uri of the endpoint that successful polled file exchanges should be forwarded to
	 * @return the uri of the endpoint that successful polled file exchanges should be forwarded to
	 */
	public String getExceptionEndpointUri() {
		return exceptionEndpointUri;
	}

	/**
	 * Sets the uri of the endpoint that successful polled file exchanges should be forwarded to
	 * @param exceptionEndpointUri the uri of the endpoint that successful polled file exchanges should be forwarded to
	 */
	public void setExceptionEndpointUri(String exceptionEndpointUri) {
		this.exceptionEndpointUri = exceptionEndpointUri;
	}

	/**
	 * Returns an array of the throwable classes that will trigger this route's exception handler. Defaults to <b><code>{ {@link Throwable} }</code></b>
	 * @return the throwable classes that will trigger this route's exception handler. Defaults to <b><code>{ {@link Throwable} }</code></b>
	 */
	public Class<? extends Throwable>[] getExceptionHandlerTriggers() {
		return exceptionHandlerTriggers;
	}

	/**
	 * Sets the array of the throwable classes that will trigger this route's exception handler. Defaults to <b><code>{ {@link Throwable} }</code></b>
	 * @param exceptionHandlerTriggers the throwable classes that will trigger this route's exception handler. Defaults to <b><code>{ {@link Throwable} }</code></b>
	 */
	public void setExceptionHandlerTriggers(Class<? extends Throwable>[] exceptionHandlerTriggers) {
		this.exceptionHandlerTriggers = exceptionHandlerTriggers;
	}

}
