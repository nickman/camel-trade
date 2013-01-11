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

import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.TransactionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jencks.DefaultEndpointFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * <p>Title: GenericJCAConnector</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.tx.GenericJCAConnector</code></p>
 */

public class GenericJCAConnector implements InitializingBean, DisposableBean, BeanFactoryAware, BeanNameAware { 
    private static final transient Log log = LogFactory.getLog(GenericJCAConnector.class);

    private ActivationSpec activationSpec;
    private BootstrapContext bootstrapContext;
    private MessageEndpointFactory endpointFactory;
    private ResourceAdapter resourceAdapter;
    private String ref;
    private TransactionManager transactionManager;
    private BeanFactory beanFactory;
    private String name;
    private GenericJCAContainer jcaContainer;
    private String acknowledgeType = "SESSION_TRANSACTED";
    
    public GenericJCAConnector() {
    }

    public GenericJCAConnector(BootstrapContext bootstrapContext, ResourceAdapter resourceAdapter) {
        this.bootstrapContext = bootstrapContext;
        this.resourceAdapter = resourceAdapter;
    }


    public void afterPropertiesSet() throws Exception {
        if (activationSpec == null) {
            throw new IllegalArgumentException("activationSpec must be set");
        }

        if (resourceAdapter == null) {
            resourceAdapter = activationSpec.getResourceAdapter();
        }
        if (jcaContainer != null) {
            start();
        }
    }
    

    public void destroy() throws Exception {
        if (resourceAdapter != null && activationSpec != null) {
            resourceAdapter.endpointDeactivation(endpointFactory, activationSpec);
        }
    }
    

    public void start() throws Exception {
        if (resourceAdapter == null && jcaContainer != null) {
            resourceAdapter = jcaContainer.getResourceAdapter();
        }
        if (resourceAdapter == null) {
            throw new IllegalArgumentException("resourceAdapter property must be set on the activationSpec object");
        }
        if (activationSpec.getResourceAdapter() == null) {
            activationSpec.setResourceAdapter(resourceAdapter);
        }

        if (bootstrapContext == null && jcaContainer != null) {
            bootstrapContext = jcaContainer.getBootstrapContext();
        }
        if (bootstrapContext == null) {
            throw new IllegalArgumentException("bootstrapContext must be set");
        }
        if (endpointFactory == null) {
            if (ref == null) {
                throw new IllegalArgumentException("either the endpointFactory or ref properties must be set");
            }
            
            DefaultEndpointFactory defaultEF = new DefaultEndpointFactory(beanFactory, ref);
            defaultEF.setAcknowledgeType(acknowledgeType);
            defaultEF.setTransactionManager(transactionManager);
            defaultEF.setName(name);
            this.endpointFactory = defaultEF;
        }
        log.info("Activating endpoint for activationSpec: " + activationSpec + " using endpointFactory: " + endpointFactory);
        resourceAdapter.endpointActivation(endpointFactory, activationSpec);
    }


    // Properties
    // -------------------------------------------------------------------------

    public String getName() {
        return name;
    }

    public void setBeanName(String name) {
        this.name = name;
    }

    public ActivationSpec getActivationSpec() {
        return activationSpec;
    }

    public void setActivationSpec(ActivationSpec activationSpec) {
        this.activationSpec = activationSpec;
    }

    /**
     * Returns the name of the MessageListener POJO in Spring
     */
    public String getRef() {
        return ref;
    }

    /**
     * Sets the name of the MessageListener POJO in Spring
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    public MessageEndpointFactory getEndpointFactory() {
        return endpointFactory;
    }

    public void setEndpointFactory(MessageEndpointFactory endpointFactory) {
        this.endpointFactory = endpointFactory;
    }

    public BootstrapContext getBootstrapContext() {
        return bootstrapContext;
    }

    public void setBootstrapContext(BootstrapContext bootstrapContext) {
        this.bootstrapContext = bootstrapContext;
    }

    public ResourceAdapter getResourceAdapter() {
        return resourceAdapter;
    }

    public void setResourceAdapter(ResourceAdapter resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
    }

    public TransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public GenericJCAContainer getJcaContainer() {
        return jcaContainer;
    }

    public void setJcaContainer(GenericJCAContainer jcaConnector) {
        this.jcaContainer = jcaConnector;
    }

	public String getAcknowledgeType() {
		return acknowledgeType;
	}

	public void setAcknowledgeType(String acknowledgeTpe) {
		this.acknowledgeType = acknowledgeTpe;
    }


}
