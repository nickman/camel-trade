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
package org.tradex.domain.trade;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.camel.Exchange;

/**
 * <p>Title: TradeCollection</p>
 * <p>Description: Container for a collection of trades</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.domain.trade.TradeCollection</code></p>
 */
@XmlRootElement(name="TradeCollection")
public class TradeCollection {
	/** The collection of trades */	
	protected final ArrayList<TradeCSV> trades;
	
	/** The default size of the collection */
	public static final int DEFAULT_SIZE = 64;
	
	/**
	 * Creates a new TradeCollection
	 * @param size The size of the collection
	 * @return the created collection
	 */
	public static TradeCollection newInstance(int size) {
		return new TradeCollection(size);
	}
	
	/**
	 * Creates a new TradeCollection
	 */
	public TradeCollection() {
		this(DEFAULT_SIZE);
	}
	
	/**
	 * Creates a new TradeCollection
	 * @param exchange The new exchange to create the collection from
	 * @return the created collection
	 */
	public static TradeCollection newInstance(Exchange exchange) {
		TradeCollection tc =  newInstance((Integer) exchange.getIn().getHeader("CamelTradeCount", 1));
		tc.addTrade(exchange.getIn().getBody(TradeCSV.class));
		exchange.getIn().setBody(tc);
		return tc;
	}
	
	
	
	
	/**
	 * Creates a new TradeColection
	 * @param The expected size of the collection
	 */
	private TradeCollection(int size) {
		trades = new ArrayList<TradeCSV>(size);
	}
	
	/**
	 * Adds a trade to the collection
	 * @param trade the trade to add
	 */
	public void addTrade(TradeCSV trade) {
		if(trade!=null) {
			trades.add(trade);
		}
	}
	
	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "TradeCollection:" + trades.size();
	}

	/**
	 * Returns a collection of trades
	 * @return a collection of trades
	 */
	public TradeCSV[] getTrades() {
		TradeCSV[] trds = new TradeCSV[trades.size()];
		return trades.toArray(trds);
	}
	
	/**
	 * Sets the trades for this collection
	 * @param trades a list of trades
	 */
	public void setTrades(List<TradeCSV> trades) {
		trades.addAll(trades);
	}

}
