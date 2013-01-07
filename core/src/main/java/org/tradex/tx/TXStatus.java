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
package org.tradex.tx;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.transaction.SystemException;
import javax.transaction.Transaction;

/**
 * <p>Title: TXStatus</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.tx.TXStatus</code></p>
 */

public enum TXStatus {
	/**  */
	TX_ACTIVE(true, 0),
	/**  */
	TX_MARKED_ROLLBACK(false, 1),
	/**  */
	TX_PREPARED(true, 2),
	/**  */
	TX_COMMITTED(false, 3),
	/**  */
	TX_ROLLEDBACK(false, 4),
	/**  */
	TX_UNKNOWN(false, 5),
	/**  */
	TX_NO_TRANSACTION(false, 6),
	/**  */
	TX_PREPARING(true, 7),
	/**  */
	TX_COMMITTING(true, 8),
	/**  */
	TX_ROLLING_BACK(false, 9);
	
	/** A map to decode the transaction status codes */
	public static final Map<Integer, TXStatus> CODE2ENUM;
	
	static {
		final TXStatus[] st = TXStatus.values();
		 Map<Integer, TXStatus> map = new HashMap<Integer, TXStatus>(st.length);
		 for(TXStatus tx: st) {
			 map.put(tx.status, tx);
		 }
		 CODE2ENUM = Collections.unmodifiableMap(map);
	}

	private TXStatus(boolean active, int status) {
		this.active = active;
		this.status = status;
	}
	
	private final boolean active;
	private final int status;
	
	/**
	 * Returns the {@link TXStatus} that represents the status of the passed transaction
	 * @param tx The transaction to get the status for
	 * @return a {@link TXStatus}
	 */
	public static TXStatus statusOf(Transaction tx) {
		if(tx==null) return TX_NO_TRANSACTION;
		try {
			return decode(tx.getStatus());
		} catch (SystemException e) {
			throw new RuntimeException("Failed to get TX status for [" + tx + "]", e);
		}
	}

	/**
	 * Indicates if this {@link TXStatus} is considered to represent an active transaction
	 * @return true if this {@link TXStatus} is considered to represent an active transaction, false otherwise
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Decodes the passed status integer 
	 * @param status A transaction status code from {@link javax.transaction.Status}.
	 * @return The {@link TXStatus} that represents the transaction status int passed
	 */
	public static TXStatus decode(int status) {
		TXStatus txs = CODE2ENUM.get(status);
		if(txs==null) throw new IllegalArgumentException("The passed status [" + status + "] is not a valid status code", new Throwable());
		return txs;
	}

	/**
	 * The status code for this {@link TXStatus} that maps to the status codes in {@link javax.transaction.Status}
	 * @return The status code
	 */
	public int getStatus() {
		return status;
	}

}
