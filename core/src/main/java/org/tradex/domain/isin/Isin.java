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
package org.tradex.domain.isin;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;


/**
 * <p>Title: Isin</p>
 * <p>Description: Represents an ISIN record acquired from the <a href="https://www.euroclear.com/site/public/EB/!ut/p/c4/04_SB8K8xLLM9MSSzPy8xBz9CP0os3gz08BgH3MPIwN3R29nAyPjYMsApxAzI3c_A_2CbEdFALEZIu8!/">Euroclear Bank Securities Database</a></p>
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.domain.isin.Isin</code></p>
 */
@CsvRecord(separator = "\t")
public class Isin {
	/** The security name */
	@DataField(pos = 1, required = true, trim= true)
	private String name;
	/** The security isin code */
	@DataField(pos = 2, required = true, trim= true)
	private String isin;
	/** The security common code */
	@DataField(pos = 3, required = true, trim= true)
	private String commonCode;
	/** The security rate */
	@DataField(pos = 4, required = false, trim= true, pattern="##.###.###", precision=3)
	private BigDecimal rate;
	/** The security currency */
	@DataField(pos = 5, required = true, trim= true)
	private String currency;
	/** The security close date */
	@DataField(pos = 6, required = false, trim= true, pattern="dd MMM yyyy")
	private Date closeDate;
	/** The security next coupon date */
	@DataField(pos = 7, required = false, trim= true, pattern="dd MMM yyyy")
	private Date nextCouponDate;
	/** The security recorded date */
	@DataField(pos = 8, required = false, trim= true, pattern="dd MMM yyyy")
	private Date recordDate;
	/** The security market type */
	@DataField(pos = 9, required = true, trim= true)
	private String market;
	/** The security instrument type */
	@DataField(pos = 10, required = true, trim= true)
	private String instrument;
	/** The security record last update date */
	@DataField(pos = 11, required = true, trim= true, pattern="dd MMM yyyy")
	private Date lastUpdate;

	/**
	 * Creates a new Isin
	 */
	public Isin() {
	}


	public String getIsin() {
		return this.isin;
	}

	public void setIsin(String isin) {
		this.isin = isin;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCommonCode() {
		return this.commonCode;
	}

	public void setCommonCode(String commonCode) {
		this.commonCode = commonCode;
	}

	public BigDecimal getRate() {
		return this.rate;
	}

	public void setRate(BigDecimal rate) {
		this.rate = rate;
	}

	public String getCurrency() {
		return this.currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Date getCloseDate() {
		return this.closeDate;
	}

	public void setCloseDate(Date closeDate) {
		this.closeDate = closeDate;
	}

	public Date getNextCouponDate() {
		return this.nextCouponDate;
	}

	public void setNextCouponDate(Date nextCouponDate) {
		this.nextCouponDate = nextCouponDate;
	}

	public Date getRecordDate() {
		return this.recordDate;
	}

	public void setRecordDate(Date recordDate) {
		this.recordDate = recordDate;
	}

	public String getMarket() {
		return this.market;
	}

	public void setMarket(String market) {
		this.market = market;
	}

	public String getInstrument() {
		return this.instrument;
	}

	public void setInstrument(String instrument) {
		this.instrument = instrument;
	}

	public Date getLastUpdate() {
		return this.lastUpdate;
	}

	public void setLastUpdate(Date lastUpdate) {
		this.lastUpdate = lastUpdate;
	}


	/**
	 * {@inheritDoc}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Isin [\n\tisin=");
		builder.append(isin);
		builder.append("\n\tname=");
		builder.append(name);
		builder.append("\n\tcommonCode=");
		builder.append(commonCode);
		builder.append("\n\trate=");
		builder.append(rate);
		builder.append("\n\tcurrency=");
		builder.append(currency);
		builder.append("\n\tcloseDate=");
		builder.append(closeDate);
		builder.append("\n\tnextCouponDate=");
		builder.append(nextCouponDate);
		builder.append("\n\trecordDate=");
		builder.append(recordDate);
		builder.append("\n\tmarket=");
		builder.append(market);
		builder.append("\n\tinstrument=");
		builder.append(instrument);
		builder.append("\n\tlastUpdate=");
		builder.append(lastUpdate);
		builder.append("\n]");
		return builder.toString();
	}
	
	

}
