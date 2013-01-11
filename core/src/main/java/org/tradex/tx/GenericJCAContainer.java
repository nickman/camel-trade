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
package org.tradex.tx;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.XATerminator;
import javax.resource.spi.work.WorkManager;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jencks.JCAConnector;
import org.jencks.JCAContainer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jca.support.SimpleBootstrapContext;

/**
 * <p>Title: GenericJCAContainer</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.tx.GenericJCAContainer</code></p>
 */

public class GenericJCAContainer implements InitializingBean, DisposableBean, ApplicationContextAware { 
    private static final Log log = LogFactory.getLog(GenericJCAContainer.class);
    private BootstrapContext bootstrapContext;
    private ResourceAdapter resourceAdapter;
    private ApplicationContext applicationContext;
    private XATerminator xaTerminator;
    private boolean lazyLoad = false;

    // optional - used to create bootstrap context when not specified
    private TransactionManager transactionManager;
    private WorkManager workManager;
    // optional - used to create work manager when bootstrap context and work manager are not specified
    private int threadPoolSize;

    public GenericJCAContainer() {
    }

    public JCAConnector addConnector() {
        return new JCAConnector(getBootstrapContext(), getResourceAdapter());
    }

    public void afterPropertiesSet() throws Exception {
    	bootstrapContext = new SimpleBootstrapContext(workManager, xaTerminator);
        resourceAdapter.start(bootstrapContext);
        // now lets start all of the JCAConnector instances
        if (!lazyLoad) {
            if (applicationContext == null) {
                throw new IllegalArgumentException("applicationContext should have been set by Spring");
            }
            applicationContext.getBeansOfType(JCAConnector.class);
        }

        String version = null;
        Package aPackage = Package.getPackage("org.jencks");
        if (aPackage != null) {
            version = aPackage.getImplementationVersion();
        }

        log.info("Jencks JCA Container (http://jencks.org/) has started running version: " + version);
    }

    public void destroy() throws Exception {
        if (resourceAdapter != null) {
            resourceAdapter.stop();
        }
        workManager = null;
    }

    // Properties
    //-------------------------------------------------------------------------
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    public void setResourceAdapter(ResourceAdapter resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
    }

    public BootstrapContext getBootstrapContext() {
        return bootstrapContext;
    }

    public void setBootstrapContext(BootstrapContext bootstrapContext) {
        this.bootstrapContext = bootstrapContext;
    }

    public boolean isLazyLoad() {
        return lazyLoad;
    }

    public void setLazyLoad(boolean lazyLoad) {
        this.lazyLoad = lazyLoad;
    }



    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public WorkManager getWorkManager() {
        return workManager;
    }

    public void setWorkManager(WorkManager workManager) {
        this.workManager = workManager;
    }

    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

	public void setXaTerminator(XATerminator xaTerminator) {
		this.xaTerminator = xaTerminator;
	}

}
