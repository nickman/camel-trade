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
package org.tradex.camel.idempotent;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;

import org.apache.camel.processor.idempotent.jdbc.JdbcMessageIdRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jmx.export.naming.SelfNaming;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * <p>Title: JmxNamedJdbcMessageIdRepository</p>
 * <p>Description: An extension of {@link JdbcMessageIdRepository} that supports a configured JMX {@link ObjectName}.</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.camel.idempotent.JmxNamedJdbcMessageIdRepository</code></p>
 */

public class JmxNamedJdbcMessageIdRepository extends JdbcMessageIdRepository implements SelfNaming {
	/** The configured object name */
	protected ObjectName objectName;
	
	/**
	 * Creates a new JmxNamedJdbcMessageIdRepository
	 */
	public JmxNamedJdbcMessageIdRepository() { }

	/**
	 * Creates a new JmxNamedJdbcMessageIdRepository
	 * @param dataSource The JDBC data source
	 * @param processorName The processor name
	 * @param objectName The designated JMX ObjectName
	 */
	public JmxNamedJdbcMessageIdRepository(DataSource dataSource, String processorName, ObjectName objectName) {
		super(dataSource, processorName);
		this.objectName = objectName;
	}

	/**
	 * Creates a new JmxNamedJdbcMessageIdRepository
	 * @param jdbcTemplate The spring jdbc template
	 * @param transactionTemplate The spring transaction template
	 */
	public JmxNamedJdbcMessageIdRepository(JdbcTemplate jdbcTemplate,
			TransactionTemplate transactionTemplate) {
		super(jdbcTemplate, transactionTemplate);
	}

	/**
	 * Creates a new JmxNamedJdbcMessageIdRepository
	 * @param dataSource The JDBC data source
	 * @param transactionTemplate The spring transaction template
	 * @param processorName The spring transaction template
	 */
	public JmxNamedJdbcMessageIdRepository(DataSource dataSource,
			TransactionTemplate transactionTemplate, String processorName) {
		super(dataSource, transactionTemplate, processorName);
	}

	/**
	 * {@inheritDoc}
	 * @see org.springframework.jmx.export.naming.SelfNaming#getObjectName()
	 */
	@Override
	public ObjectName getObjectName() throws MalformedObjectNameException {
		return objectName;
	}
	
	/**
	 * Configues the repository JMX ObjectName
	 * @param objectName the repository JMX ObjectName
	 */
	public void setObjectName(ObjectName objectName) {
		this.objectName = objectName;
	}

}
