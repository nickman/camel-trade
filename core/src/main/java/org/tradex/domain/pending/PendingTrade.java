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
package org.tradex.domain.pending;


/**
 * <p>Title: PendingTrade</p>
 * <p>Description: Represents a pending trade.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.domain.pending.PendingTrade</code></p>
 */

public class PendingTrade {
	/** The trade request xml */
	protected String tradeXml = null;
	/** The trade order Id */	
	protected String orderId = null;
	
	/**
	 * Creates a new PendingTrade
	 */
	public PendingTrade() {
		/* No Op */
	}
	
	

	/**
	 * Creates a new PendingTrade
	 * @param tradeXml The trade request xml 
	 * @param orderId The trade order Id
	 */
	public PendingTrade(String tradeXml, String orderId) {
		super();
		this.tradeXml = tradeXml;
		this.orderId = orderId;
	}



	/**
	 * Returns the request Xml
	 * @return the request Xml
	 */
	public String getTradeXml() {
		return tradeXml;
	}

	/**
	 * Sets the request Xml
	 * @param tradeXml the request Xml
	 */
	public void setTradeXml(String tradeXml) {
		this.tradeXml = tradeXml;
	}

	/**
	 * Returns the trade identifier
	 * @return the trade identifier
	 */
	public String getOrderId() {
		return orderId;
	}

	/**
	 * Sets the trade identifier
	 * @param orderId the trade identifier
	 */
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

}
