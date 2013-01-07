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
package org.tradex.camel.split;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.tradex.tx.TXStatus;
import org.tradex.tx.TransactionHelper;


/**
 * <p>Title: ObjectListUnwrapper</p>
 * <p>Description: A transformer to convert an array list of maps containing unmarshalled bindy objects into one aggregated iterable. </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.camel.split.ObjectListUnwrapper</code></p>
 * @param <T> The expected object type unmarshalled by bindy
 */

public class ObjectListUnwrapper<T> {
	/** Instance logger */
	protected final Logger log = Logger.getLogger(getClass());
	
	/**
	 * Splits the body of the post-bindy unmarshaller exchange into an iterable of individual trades.
	 * @param list The list of maps from the exchange
	 * @return A list of trades
	 */
	@SuppressWarnings("unchecked")
	public List<T> split(ArrayList<Map<Object, Object>> list) {
		log.info(dumpTxInfo());
		ArrayList<T> splits = new ArrayList<T>(list.size()); 
		for(Map<Object, Object> m: list) {
			for(Object obj: m.values()) {
				splits.add((T)obj);
			}
		}
		return splits;
	}
	
	private String dumpTxInfo() {
		StringBuilder b = new StringBuilder("\nTransaction Dump:");
		b.append("\n\tTX Status:").append(TransactionHelper.getTransactionState());
		b.append("\n\tTX UID:").append(TransactionHelper.getTransactionUID());
		b.append("\n\tTX Class:").append(TransactionHelper.getCurrentTransaction().getClass().getName());
		b.append("\n===================");
		TransactionHelper.registerSynchronizationRunnable(new Runnable(){
			@Override
			public void run() {
				StringBuilder b = new StringBuilder("\nTransaction Dump:");
				b.append("\n\tTX Status:").append(TransactionHelper.getTransactionState());
				b.append("\n\tTX UID:").append(TransactionHelper.getTransactionUID());
				b.append("\n===================");
				log.info(b);
			}
		}, TXStatus.values());
		return b.toString();
	}

}
