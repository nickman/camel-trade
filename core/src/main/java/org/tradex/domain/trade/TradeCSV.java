
package org.tradex.domain.trade;

import java.math.BigDecimal;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

/**
 * <p>Title: TradeCSV</p>
 * <p>Description: Trade implementation annotated for CSV parsing</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>test.org.tradex.domain.trade.TradeCSV</code></p>
 */
@CsvRecord(separator = ",")
@XmlRootElement(name="TradeCSV")
public class TradeCSV implements ITrade {
	/** The trade Id */
	@DataField(pos = 1, required = true, trim= true)
	protected long tradeId = -1L;
	/** The security isin */
	@DataField(pos = 2, required = true, trim= true)
	protected String isin = null;
	/** The trade order Id */
	@DataField(pos = 3, required = true, trim= true)
	protected String orderId = null;
	/** The trade price */
	@DataField(pos = 4, required = true, trim= true, precision=2)
	protected BigDecimal price = null;
	/** The trade business day */
	@DataField(pos = 5, required = true, trim= true, pattern="yyyy/MM/dd")
	protected Date businessDay = null;
	/** The trade send time */
	@DataField(pos = 6, required = true, trim= true, pattern="yyyy/MM/dd HH:mm:ss")
	protected Date sendingTime = null;
	/** The trade type */
	@DataField(pos = 7, required = true, trim= true)
	protected String type = null;
	/** The security common code */
	@DataField(pos = 8, required = false, trim= true)
	protected String commonCode;
	
	/** The status of this trade */	
	protected String status = TradeStatus.INITIAL.name();
	/** A status message */	
	protected String message;
	/** The timestamp of the last update */	
	protected Date lastUpdate = new Date();
	

	
	/**
	 * {@inheritDoc}
	 * @see org.tradex.domain.trade.ITrade#getTradeId()
	 */
	@Override
	public long getTradeId() {		
		return tradeId;
	}

	/**
	 * {@inheritDoc}
	 * @see org.tradex.domain.trade.ITrade#getIsin()
	 */
	@Override
	public String getIsin() {
		return isin;
	}

	/**
	 * {@inheritDoc}
	 * @see org.tradex.domain.trade.ITrade#getBusinessDay()
	 */
	@Override
	public Date getBusinessDay() {
		return businessDay;
	}

	/**
	 * {@inheritDoc}
	 * @see org.tradex.domain.trade.ITrade#getSendingTime()
	 */
	@Override
	public Date getSendingTime() {
		return sendingTime;
	}

	/**
	 * {@inheritDoc}
	 * @see org.tradex.domain.trade.ITrade#getType()
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 * @see org.tradex.domain.trade.ITrade#getOrderId()
	 */
	@Override
	public String getOrderId() {
		return orderId;
	}

	/**
	 * {@inheritDoc}
	 * @see org.tradex.domain.trade.ITrade#getPrice()
	 */
	@Override
	public BigDecimal getPrice() {
		return price;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.tradex.domain.trade.ITrade#getCommonCode()
	 */
	@Override
	public String getCommonCode() {
		return commonCode;
	}
	


	/**
	 * Sets the trade id
	 * @param tradeId the trade id
	 */
	public void setTradeId(long tradeId) {
		this.tradeId = tradeId;
	}

	/**
	 * Sets the ISIN
	 * @param isin the ISIN code
	 */
	public void setIsin(String isin) {
		this.isin = isin;
	}

	/**
	 * Sets the order ID
	 * @param orderId the order ID
	 */
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	/**
	 * Sets the trade price
	 * @param price the trade price
	 */
	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	/**
	 * Sets the trade business day
	 * @param businessDay the trade business day
	 */
	public void setBusinessDay(Date businessDay) {
		this.businessDay = businessDay;
	}

	/**
	 * Sets the trade send time
	 * @param sendingTime the trade send time
	 */
	public void setSendingTime(Date sendingTime) {
		this.sendingTime = sendingTime;
	}

	/**
	 * Sets the trade type
	 * @param type the trade type
	 */
	public void setType(String type) {
		this.type = type;
	}


	/**
	 * {@inheritDoc}
	 * @see org.tradex.domain.trade.ITrade#setCommonCode(java.lang.String)
	 */
	@Override
	public void setCommonCode(String commonCode) {
		this.commonCode = commonCode;
	}

	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TradeCSV [tradeId=");
		builder.append(tradeId);
		builder.append(", isin=");
		builder.append(isin);
		builder.append(", orderId=");
		builder.append(orderId);
		builder.append(", price=");
		builder.append(price);
		builder.append(", businessDay=");
		builder.append(businessDay);
		builder.append(", sendingTime=");
		builder.append(sendingTime);
		builder.append(", type=");
		builder.append(type);
		builder.append(", commonCode=");
		builder.append(commonCode);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Returns the status of this trade
	 * @return the status of this trade
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Sets the status of this trade
	 * @param status the new status
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * Returns the status message for this trade
	 * @return the status message 
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * Sets the status message for this trade
	 * @param message the status message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Returns the timestamp of the last update to this trade
	 * @return the timestamp of the last update to this trade
	 */
	public Date getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * Sets the timestamp of the last update to this trade
	 * @param lastUpdate the timestamp of the last update to this trade
	 */
	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
	

}

/*
 ===========================================================
 Some groovy code to generate sample files 
 ===========================================================
import java.util.concurrent.atomic.*;
import java.text.SimpleDateFormat;

types = ['BUY', 'SELL'] as String[];
isins = ['US0378331005', 'AU0000XVGZA3', 'GB0002634946'] as String[];

df = new SimpleDateFormat("yyyy/MM/dd");
dt = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
today = df.format(new Date());
outFile = new File('c:/temp/spicein/test-trades.csv');
outFile.delete();
random = new Random(System.currentTimeMillis());
r = {max -> return Math.abs(random.nextInt(max));}
tradeId = new AtomicLong(r(10000));
orderId = new AtomicLong(r(90000));
timeOff = new AtomicLong(0L);
for(i in 0..10) {
    b = new StringBuilder();
    b.append(tradeId.incrementAndGet()).append(",");
    b.append(isins[r(3)]).append(",");
    b.append(orderId.incrementAndGet()).append(",");
    priceStr = "${r(1000000)}.${r(99)}";
    price = new BigDecimal(priceStr);
    b.append(price).append(",");
    b.append(today).append(",");
    timeOff.addAndGet(r(5000));
    b.append(dt.format(new Date(System.currentTimeMillis() + timeOff.get()))).append(",");
    b.append(types[r(2)]);
    println b;
    b.append("\n");
    outFile.append(b.toString());
    tradeId.addAndGet(r(200));
    orderId.addAndGet(r(200));            
}
 ===========================================================

 * */
