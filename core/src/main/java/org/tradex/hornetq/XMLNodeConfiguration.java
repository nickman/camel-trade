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
package org.tradex.hornetq;

import org.hornetq.core.config.impl.ConfigurationImpl;
import org.hornetq.core.deployers.impl.FileConfigurationParser;
import org.hornetq.utils.XMLUtil;
import org.w3c.dom.Element;

/**
 * <p>Title: XMLNodeConfiguration</p>
 * <p>Description: HornetQ configuration implementation using a passed string which is parsed into an XML element. </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.hornetq.XMLNodeConfiguration</code></p>
 */

public class XMLNodeConfiguration extends ConfigurationImpl {
	/** The XML configuration string */
	protected String configurationXml = null;
	/** The start indicator */
	protected boolean started = false;
	/**  */
	private static final long serialVersionUID = -4629739854063622027L;

	/**
	 * Returns the XML configuration string
	 * @return the XML configuration string
	 */
	public String getConfigurationXml()  {
		return configurationXml;
	}

	/**
	 * Sets the XML configuration string
	 * @param configurationXml the XML configuration string
	 */
	public void setConfigurationXml(final String configurationXml) {
		this.configurationXml = configurationXml;
	}	
	
	
	/**
	 * Parses the XML configuration
	 * @throws Exception thrown on any configuration exception
	 */
	public void start() throws Exception {	      
		configurationXml = XMLUtil.replaceSystemProps(configurationXml);
	    Element e = org.hornetq.utils.XMLUtil.stringToElement(configurationXml);
	    FileConfigurationParser parser = new FileConfigurationParser();
	    parser.setValidateAIO(true);
	    parser.parseMainConfig(e, this);
		started = true;
	}

	/**
	 * Marks this configuration as stopped 
	 */
	public void stop() {		
		started = false;
	}
}
