/**
 * Helios, OpenSource Monitoring
 * Brought to you by the Helios Development Group
 *
 * Copyright 2013, Helios Development Group and individual contributors
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
package org.tradex.camel.aggregator;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.processor.aggregate.AggregationStrategy;
import org.tradex.domain.trade.ITrade;
import org.tradex.domain.trade.TradeCSV;
import org.tradex.domain.trade.TradeCollection;

/**
 * <p>Title: TradeImportAggregator</p>
 * <p>Description: Aggregates streaming trade instances</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.camel.aggregator.TradeImportAggregator</code></p>
 */

public class TradeImportAggregator implements AggregationStrategy {

	/**
	 * {@inheritDoc}
	 * @see org.apache.camel.processor.aggregate.AggregationStrategy#aggregate(org.apache.camel.Exchange, org.apache.camel.Exchange)
	 */
	@Override
	public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
		Message newIn = newExchange.getIn();   		   
		ITrade newTrade = newIn.getBody(ITrade.class);
		TradeCollection oldBody =  null;
		if(oldExchange!=null) {
			oldBody = oldExchange.getIn().getBody(TradeCollection.class);
			oldBody.addTrade((TradeCSV)newTrade);
			newIn.setBody(oldBody);
		} else {
			oldBody = TradeCollection.newInstance(newExchange); 
		}				
//		if(complete(newExchange)) {
//			newIn.setBody(oldBody.getTrades());
//		} else {
//			newIn.setBody(oldBody);
//		}
		newIn.setBody(oldBody);
		return newExchange;
	}
	
	private boolean complete(Exchange ex) {
		Boolean complete = ex.getProperty("CamelSplitComplete", Boolean.class);		
		return complete==null ? false : complete;
	}

}
