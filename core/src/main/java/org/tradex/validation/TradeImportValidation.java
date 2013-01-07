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
package org.tradex.validation;

import java.util.Collections;

import javax.sql.DataSource;

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.tradex.domain.trade.ITrade;
/**
 * <p>Title: TradeImportValidation</p>
 * <p>Description: Validation bean for trade imports</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.validation.TradeImportValidation</code></p>
 */

public class TradeImportValidation implements InitializingBean {
	/** The data source for querying the ISIN DB */
	@Autowired(required=true)
	@Qualifier("Default")
	protected DataSource dataSource;
	/** A jdbc template for querying the ISIN DB */
	protected NamedParameterJdbcTemplate jdbcTemplate;
	
	/**
	 * Validates various bits of the passed trade
	 * @param trade the trade to validate
	 */
	@Handler
	public void validateTrade(@Body ITrade trade) {
		try {
			if(1>jdbcTemplate.queryForInt("SELECT COUNT(*) FROM ISIN WHERE ISIN = :isin", Collections.singletonMap("isin", trade.getIsin()))) {
				throw new Exception("Invalid ISIN [" + trade.getIsin() + "]");				
			}					
		} catch (Exception ex) {
			throw new RuntimeException("Failed to validate Trade [" + trade + "]", ex);
		}
		
		
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		
	}
}
