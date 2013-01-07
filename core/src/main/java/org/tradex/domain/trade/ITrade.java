package org.tradex.domain.trade;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>Title: ITrade</p>
 * <p>Description: Defines a trade</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>test.org.tradex.domain.ITrade</code></p>
 */
public interface ITrade {
	/**
	 * Returns the trade Id
	 * @return the trade Id
	 */
	public long getTradeId();
	/**
	 * Returns the trade security ISIN
	 * @return the trade security ISIN
	 */
	public String getIsin();
	/**
	 * Returns the trade business date
	 * @return the trade business date
	 */
	public Date getBusinessDay();
	/**
	 * Returns the trade send time
	 * @return the trade send time
	 */
	public Date getSendingTime();
	/**
	 * Returns the trade type
	 * @return the trade type
	 */
	public String getType();
	/**
	 * Returns the trade order Id
	 * @return the trade order Id
	 */
	public String getOrderId();
	/**
	 * Returns the trade order price
	 * @return the trade order price
	 */
	public BigDecimal getPrice();
	
	
	
}
