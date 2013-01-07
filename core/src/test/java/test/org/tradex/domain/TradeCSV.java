
package test.org.tradex.domain;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

/**
 * <p>Title: TradeCSV</p>
 * <p>Description: Trade implementation annotated for CSV parsing</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>test.org.tradex.domain.TradeCSV</code></p>
 */
@CsvRecord(separator = ",")
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
	
	/**
	 * {@inheritDoc}
	 * @see test.org.tradex.domain.ITrade#getTradeId()
	 */
	@Override
	public long getTradeId() {		
		return tradeId;
	}

	/**
	 * {@inheritDoc}
	 * @see test.org.tradex.domain.ITrade#getIsin()
	 */
	@Override
	public String getIsin() {
		return isin;
	}

	/**
	 * {@inheritDoc}
	 * @see test.org.tradex.domain.ITrade#getBusinessDay()
	 */
	@Override
	public Date getBusinessDay() {
		return businessDay;
	}

	/**
	 * {@inheritDoc}
	 * @see test.org.tradex.domain.ITrade#getSendingTime()
	 */
	@Override
	public Date getSendingTime() {
		return sendingTime;
	}

	/**
	 * {@inheritDoc}
	 * @see test.org.tradex.domain.ITrade#getType()
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * {@inheritDoc}
	 * @see test.org.tradex.domain.ITrade#getOrderId()
	 */
	@Override
	public String getOrderId() {
		return orderId;
	}

	/**
	 * {@inheritDoc}
	 * @see test.org.tradex.domain.ITrade#getPrice()
	 */
	@Override
	public BigDecimal getPrice() {
		return price;
	}

	@Override
	public String toString() {
		return "TradeCSV [tradeId=" + tradeId + ", isin=" + isin + ", orderId="
				+ orderId + ", price=" + price + ", businessDay=" + businessDay
				+ ", sendingTime=" + sendingTime + ", type=" + type + "]";
	}

	public void setTradeId(long tradeId) {
		this.tradeId = tradeId;
	}

	public void setIsin(String isin) {
		this.isin = isin;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	public void setBusinessDay(Date businessDay) {
		this.businessDay = businessDay;
	}

	public void setSendingTime(Date sendingTime) {
		this.sendingTime = sendingTime;
	}

	public void setType(String type) {
		this.type = type;
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
