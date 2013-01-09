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
	
	/**
	 * Returns the security common code
	 * @return the security common code
	 */
	public String getCommonCode();
	
	/**
	 * Sets the security common code
	 * @param commonCode the security common code
	 */
	public void setCommonCode(String commonCode);
	
	/**
	 * Returns the status of this trade
	 * @return the status of this trade
	 */
	public String getStatus();

	/**
	 * Sets the status of this trade
	 * @param status the new status
	 */
	public void setStatus(String status);

	/**
	 * Returns the status message for this trade
	 * @return the status message 
	 */
	public String getMessage();

	/**
	 * Sets the status message for this trade
	 * @param message the status message
	 */
	public void setMessage(String message);

	/**
	 * Returns the timestamp of the last update to this trade
	 * @return the timestamp of the last update to this trade
	 */
	public Date getLastUpdate();
	
	/**
	 * <p>Title: TradeStatus</p>
	 * <p>Description: Enumerates the possible states of a trade</p> 
	 * <p>Company: Helios Development Group LLC</p>
	 * @author Whitehead (nwhitehead AT heliosdev DOT org)
	 * <p><code>org.tradex.domain.trade.ITrade.TradeStatus</code></p>
	 */
	public static enum TradeStatus {
		INITIAL,
		CAPTURED,
		SENT,
		CONFIRMED,
		ERROR;
	}
	
}
