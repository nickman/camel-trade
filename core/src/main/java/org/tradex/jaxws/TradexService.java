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
package org.tradex.jaxws;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Endpoint;
import org.apache.camel.ExchangePattern;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultExchange;
import org.apache.log4j.Logger;

/**
 * <p>Title: TradexService</p>
 * <p>Description: REST service for tradex</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.jaxws.TradexService</code></p>
 */
@Path("/tradex")
public class TradexService implements CamelContextAware {
	/** Template to send requests into the camel context */
	protected ProducerTemplate template = null;
	/** The camel context */
	protected CamelContext camelContext = null;
	
	/** Instance logger */
	protected final Logger log = Logger.getLogger(getClass());
	
	/**
	 * Says hello
	 * @return the hello say
	 */
	@GET
	@Produces("text/html")
	@Path("hello")
	public String hello(){
	    return "<h4>Hello World!</h4>";
	}
	
	/**
	 * Returns the system props in json format
	 * @return the system props in json format
	 */
	@GET
	@Produces("application/json")
	@Path("sysprops")
	public Response sysprops(){
		Properties p = System.getProperties();
		List<String> props = new ArrayList<String>(p.size());
		for(String key: p.stringPropertyNames()) {
			props.add(key + ":" + p.getProperty(key));
		}
		GenericEntity<List<String>> entity = new GenericEntity<List<String>>(props) { };
		return Response.ok().entity(entity).build();
	}
	
	/**
	 * Accepts a file name to process through a rest request
	 * @param fileName The file name to submit for processing
	 * @return the result of the file submit operation
	 */
	@GET
	@Produces("text/plain")
	@Path("submit/{fileName}")
	public String processFile(@PathParam("fileName") String fileName) {
		try {
			log.info("Processing submit file request for [" + fileName + "]");
			File file = new File(fileName);
			if(!file.exists()) throw new Exception("error:The file [" + fileName + "] does not exist");
			Map<String, Object> headers = new HashMap<String, Object>(2);
			headers.put("CamelFileName", file.getAbsolutePath());
			headers.put("CamelFileNameProduced", file.getAbsolutePath());						
			template.sendBodyAndHeaders("direct:TradeImportEntryPoint", headers);
			return "ok:processed file [" + fileName + "]";
		} catch (Exception ex) {
			ex.printStackTrace(System.err);
			return "error:" + ex;
		}
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
	 * @see org.apache.camel.CamelContextAware#setCamelContext(org.apache.camel.CamelContext)
	 */
	@Override
	public void setCamelContext(CamelContext camelContext) {
		this.camelContext = camelContext;
		template = camelContext.createProducerTemplate();
		template.setDefaultEndpointUri("direct:TradeImportEntryPoint");
	}
	

}
