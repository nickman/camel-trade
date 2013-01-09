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
package org.tradex.hibernate;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;
import org.tradex.domain.trade.ITrade;

/**
 * <p>Title: TradeInterceptor</p>
 * <p>Description: Hibernate interceptor to tick the last update timestamp on trade instances</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.hibernate.TradeInterceptor</code></p>
 */

public class TradeInterceptor extends EmptyInterceptor {
	/** The index of the last update field */
	protected final int lastUpdateField;

	/**
	 * Creates a new TradeInterceptor
	 * @param lastUpdateField The index of the last update field
	 */
	public TradeInterceptor(int lastUpdateField) {
		super();
		this.lastUpdateField = lastUpdateField;
	}

	/**  */
	private static final long serialVersionUID = 5134429608310861549L;

	/**
	 * {@inheritDoc}
	 * @see org.hibernate.EmptyInterceptor#onFlushDirty(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
	 */
	public boolean onFlushDirty(
			Object entity, 
			Serializable id, 
			Object[] currentState, 
			Object[] previousState, 
			String[] propertyNames, 
			Type[] types) {
		if(entity instanceof ITrade) {
			currentState[lastUpdateField] = new Date();
			return true;
		}
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 * @see org.hibernate.EmptyInterceptor#onSave(java.lang.Object, java.io.Serializable, java.lang.Object[], java.lang.String[], org.hibernate.type.Type[])
	 */
	@Override
	public boolean onSave(Object entity, Serializable id, Object[] state,
			String[] propertyNames, Type[] types) {
		if(entity instanceof ITrade) {
			state[lastUpdateField] = new Date();
			return true;
		}
		return false; //super.onSave(entity, id, state, propertyNames, types);
	}
}
