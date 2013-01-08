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
package org.tradex.validation.trade;

import java.util.Collections;

import javax.sql.DataSource;

import org.apache.camel.Exchange;
import org.apache.camel.Handler;
import org.apache.camel.Message;
import org.apache.camel.api.management.ManagedAttribute;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.tradex.domain.trade.ITrade;
import org.tradex.domain.trade.TradeImportBusinessException;
/**
 * <p>Title: TradeImportValidation</p>
 * <p>Description: Validation bean for trade imports</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.validation.trade.TradeImportValidation</code></p>
 */

public class TradeImportValidation implements InitializingBean {
	/** The data source for querying the ISIN DB */
	@Autowired(required=true)
	@Qualifier("Default")
	protected DataSource dataSource;
	/** A jdbc template for querying the ISIN DB */
	protected NamedParameterJdbcTemplate jdbcTemplate;
	/** The maximum number of retries for a business validation failed trade */
	protected int maxRetries = 5;
	/** Instance logger */
	protected final Logger log = Logger.getLogger(getClass());
	
	/**
	 * <p>Validates various bits of the passed trade.</p>
	 * <p>If validation fails, the exchange's exception set in order to setup a <b>recoverable</b> error.
	 * For example, an ISIN may be invalid because it has not been registered in the ISIN database yet,
	 * but once it is, the trade import will succeed on retry.</p>
	 * <p>Once the trade has failed {@link #maxRetries} times, the exchange will have its <b>fault</b> flag set,
	 * indicating an unrecoverable error.</p>
	 * @param tradeExchange the exchange containing the trade to validate
	 */
	@Handler
	public void validateTrade(Exchange tradeExchange) {
		ITrade trade = tradeExchange.getIn().getBody(ITrade.class);
		try {
			if(1>jdbcTemplate.queryForInt("SELECT COUNT(*) FROM ISIN WHERE ISIN = :isin", Collections.singletonMap("isin", trade.getIsin()))) {
				throw new Exception("Invalid ISIN [" + trade.getIsin() + "]");				
			}					
		} catch (Exception ex) {
			TradeImportBusinessException tibe = new TradeImportBusinessException("Failed to validate Trade [" + trade + "]", ex); 			
			int retries = getRetryCount(tradeExchange);
			log.warn("\n\t======================\n\tFailed trade retry count: [" + retries + "]\n\t======================\n");
			tradeExchange.setException(tibe);
//			if(retries>=maxRetries) {
//				Message fault = tradeExchange.getOut();
//				fault.setFault(true);				
//				fault.setBody(tibe);
//			} else {
//				tradeExchange.setException(tibe);
//			}
		}
	}
	
	/**
	 * Increments the retry count of the passed trade exchange and returns the new count
	 * @param tradeExchange The exchange
	 * @return The latest retry count
	 */
	private int getRetryCount(Exchange tradeExchange) {
		int retries = tradeExchange.getProperty(Exchange.REDELIVERY_COUNTER, 0, Integer.class);
		retries++;
		tradeExchange.setProperty(Exchange.REDELIVERY_COUNTER, retries);
		if(retries>=maxRetries) {
			tradeExchange.setProperty(Exchange.REDELIVERY_EXHAUSTED, true);
		}
		return retries;
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		
	}

	/**
	 * Returns the maximum number of retries for a business validation failed trade 
	 * @return the maximum number of retries for a business validation failed trade
	 */
	@ManagedAttribute(description="The maximum number of retries for a business validation failed trade")
	public int getMaxRetries() {
		return maxRetries;
	}

	/**
	 * Sets the maximum number of retries for a business validation failed trade
	 * @param maxRetries the maximum number of retries for a business validation failed trade
	 */
	@ManagedAttribute(description="The maximum number of retries for a business validation failed trade")
	public void setMaxRetries(int maxRetries) {
		this.maxRetries = maxRetries;
	}
}
