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
package org.tradex.tx;

import javax.transaction.xa.XAResource;

import org.apache.log4j.Logger;

/**
 * <p>Title: DumpTransaction</p>
 * <p>Description: Dumps the current transaction data</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.tx.DumpTransaction</code></p>
 */

public class DumpTransaction {
	/** The instance logger */
	protected final Logger log = Logger.getLogger(getClass());
	
	/**
	 * Logs current transaction data
	 */
	public void dumpTx() {
		StringBuilder b = new StringBuilder("\nTransaction Dump:");
		b.append("\n\tTX Status:").append(TransactionHelper.getTransactionState());
		b.append("\n\tTX UID:").append(TransactionHelper.getTransactionUID());
		b.append("\n\tXA Resources:");
		for(XAResource xar : TransactionHelper.getTransactionResources().keySet()) {
			b.append("\n\t\t[").append(xar.getClass().getName()).append("]:").append(xar);
		}
		b.append("\n===================");
		log.info(b);
	}
}
