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

/**
 * <p>Title: TradeImportBusinessException</p>
 * <p>Description: Exception thrown when a business rule is violated during a trade import</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.domain.trade.TradeImportBusinessException</code></p>
 */

public class TradeImportBusinessException extends TradeImportException {

	/**  */
	private static final long serialVersionUID = 8446696647642569689L;

	/**
	 * Creates a new TradeImportBusinessException
	 */
	public TradeImportBusinessException() {
		super();
	}

	/**
	 * Creates a new TradeImportBusinessException
	 * @param message The error message
	 */
	public TradeImportBusinessException(String message) {
		super(message);
	}

	/**
	 * Creates a new TradeImportBusinessException
	 * @param cause The underlying exception cause
	 */
	public TradeImportBusinessException(Throwable cause) {
		super(cause);		
	}

	/**
	 * Creates a new TradeImportBusinessException
	 * @param message The error message
	 * @param cause The underlying exception cause
	 */
	public TradeImportBusinessException(String message, Throwable cause) {
		super(message, cause);
	}

}
