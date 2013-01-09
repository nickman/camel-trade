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
package org.tradex.camel.enrich;

import java.util.Collections;

import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.tradex.domain.trade.ITrade;
import org.tradex.domain.trade.TradeImportBusinessException;

/**
 * <p>Title: TradeCommonCodeEnricher</p>
 * <p>Description: A simple enricher that adds the common code to a trade</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.camel.enrich.TradeCommonCodeEnricher</code></p>
 */

public class TradeCommonCodeEnricher implements InitializingBean, Processor {
	/** The data source for querying the ISIN DB */
	@Autowired(required=true)
	@Qualifier("Default")
	protected DataSource dataSource;
	/** A jdbc template for querying the ISIN DB */
	protected NamedParameterJdbcTemplate jdbcTemplate;
	/** Instance logger */
	protected final Logger log = Logger.getLogger(getClass());


	/**
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	}

	/**
	 * {@inheritDoc}
	 * @see org.apache.camel.Processor#process(org.apache.camel.Exchange)
	 */
	@Override
	public void process(Exchange exchange) throws Exception {
		ITrade trade = exchange.getIn().getBody(ITrade.class);
		String commonCode = jdbcTemplate.queryForObject("SELECT COMMON_CODE FROM ISIN WHERE ISIN = :isin", Collections.singletonMap("isin", trade.getIsin()), String.class);
		if(commonCode==null || commonCode.trim().isEmpty()) {
			throw new TradeImportBusinessException("Failed to get common code for trade [" + trade + "]");
		}
		trade.setCommonCode(commonCode);
	}

}
