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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAResource;

import org.apache.log4j.Logger;

import com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionSynchronizationRegistryImple;

/**
 * <p>Title: TransactionHelper</p>
 * <p>Description: </p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.tx.TransactionHelper</code></p>
 */

public class TransactionHelper {
	/** The transaction implementation class name */
	public static final String TXClassName = "com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionImple";
	/** The transaction manager implementation class name */
	public static final String ArjunaTXManagerName  = "com.arjuna.ats.internal.jta.transaction.arjunacore.TransactionManagerImple";
	/** The user transaction implementation class name */
	public static final String TXUserTransactionClassName = "com.arjuna.ats.jta.UserTransaction";
	/** The atomic action implementation class name */
	public static final String  AtomicActionClassName = "com.arjuna.ats.arjuna.AtomicAction";
	/** The interposed synchronization implementation class name */
	public static final String InterposedSynchronization = "com.arjuna.ats.internal.jta.resources.arjunacore.SynchronizationImple";
	
	private static final Logger LOG = Logger.getLogger(TransactionHelper.class);
	private static final String xAResourcesFieldName = "_resources";
	private static Field xAResourcesField = null;
	private static Method getUidMethod = null;
	private static Method getTimeoutMethod = null;
	private static Method getUserTransactionMethod = null;
	private static Method getTXTimeoutMethod = null;
	private static Method getAtomicActionMethod = null;
	private static Constructor<?> interposedSynchronizationCtor = null;
	
	/** Reflective method for registering interposed synchronization */
	private static Method registerSynchronizationImpleMethod;
	
	/** XAResource Name for WebSphereMQ XA Resource */
	public static final String MQ_RESOURCE_NAME = "com.ibm.mq.jmqi.JmqiXAResource";
	/** XAResource Name for JBoss JMS XA Resource */
	public static final String JBOSSMQ_RESOURCE_NAME = "org.jboss.jms.tx.MessagingXAResource";
	/** XAResource Name for JDBC XA Resource (not DataSource specific) */
	public static final String JDBC_RESOURCE_NAME = "org.jboss.resource.connectionmanager.xa.JcaXAResourceWrapper";
	/** XAResource Name for Local JDBC XA Resource (not DataSource specific) */
	public static final String JDBC_LOCAL_RESOURCE_NAME = "org.jboss.resource.connectionmanager.TxConnectionManager$LocalXAResource";
	
	/** Random to generate a unique key for registered synch runnables */
	private static Random random = new Random(System.nanoTime());
	
	/** The transaction status of the transaction associated with the current thread */
	private static final ThreadLocal<TXStatus> txStat = new ThreadLocal<TXStatus>();
	
	/** The JTA transaction synchronization registry */
	private static final TransactionSynchronizationRegistry TX_REGISTRY = new TransactionSynchronizationRegistryImple();
	
	/**
	 * Returns the TX completion status.
	 * Only in scope for the Synchrnonization callback.
	 * @return the TX completion status.
	 */
	public static TXStatus getCompletionTXStatus() {
		return txStat.get();
	}
	
	static {
		Class<?> clazz = null;
		try {
			clazz = Class.forName(TXClassName);
			xAResourcesField = clazz.getDeclaredField(xAResourcesFieldName);
			xAResourcesField.setAccessible(true);
			getAtomicActionMethod = clazz.getDeclaredMethod("getAtomicAction");
			getAtomicActionMethod.setAccessible(true);
		} catch (Exception e) {
			LOG.fatal("Failed to acquire resource field from class [" + TXClassName + "]", e);
		}
		try {
			getUidMethod = clazz.getDeclaredMethod("get_uid");
			getUidMethod.setAccessible(true);
		} catch (Exception e) {
			LOG.fatal("Failed to acquire get_uid method from class [" + TXClassName + "]", e);
		}
		try {
			clazz = Class.forName(TXUserTransactionClassName);
			getUserTransactionMethod = clazz.getDeclaredMethod("userTransaction");
		} catch (Exception e) {
			LOG.fatal("Failed to acquire userTransaction method from class [" + TXUserTransactionClassName + "]", e);
		}
		try {
			clazz = Class.forName(AtomicActionClassName);
			getTXTimeoutMethod = clazz.getDeclaredMethod("getTimeout");
			getTXTimeoutMethod.setAccessible(true);
		} catch (Exception e) {
			LOG.fatal("Failed to acquire AtomicAction.getTimeout method from class [" + AtomicActionClassName + "]", e);
		}
		
		try {
			Class<?> txclazz = Class.forName(TXClassName);
			Class<?> interposedSynchClass = Class.forName(InterposedSynchronization);
			registerSynchronizationImpleMethod = txclazz.getDeclaredMethod("registerSynchronizationImple", interposedSynchClass);
			registerSynchronizationImpleMethod.setAccessible(true);
			interposedSynchronizationCtor = interposedSynchClass.getDeclaredConstructor(Synchronization.class, boolean.class);
			interposedSynchronizationCtor.setAccessible(true);
		} catch (Exception e) {
			LOG.fatal("Failed to get registerSynchronizationImple method from Arjuna TX Class", e);
		}
		
	}
	
	/** An atomic reference to cache the TransactionManager */
	protected static final AtomicReference<TransactionManager> TX_MANAGER = new AtomicReference<TransactionManager>(null);
	
	/**
	 * Returns the containers transaction manager
	 * @return the JTA transaction manager
	 */
	public static TransactionManager getTransactionManager() {
		TransactionManager tm = TX_MANAGER.get();
		if(tm==null) {
			synchronized(TX_MANAGER) {
				tm = TX_MANAGER.get();
				if(tm==null) {
					tm = TransactionManagerLocator.locate();
					TX_MANAGER.set(tm);
				}				
			}			
		}
		return tm;
	}
	
	private static class TransactionManagerLocator {
		public static TransactionManager locate() {
			try {
				return (TransactionManager)Class.forName(ArjunaTXManagerName).newInstance();
			} catch (Exception e) {
				throw new RuntimeException("Failed to locate TransactionManager", e);
			}
		}
	}
	
	/**
	 * Returns the timeout of the current transaction in seconds
	 * @return the current transaction timeout in seconds
	 */
	public static int getTransactionTimeout() {
		try {
			return (Integer)getTXTimeoutMethod.invoke(getAtomicActionMethod.invoke(getCurrentTransaction()));
		} catch (Exception e) {
			return 0;
		}
	}
	
	/**
	 * Returns the timeout of the passed transaction in seconds
	 * @param tx The transaction to get the timeout for.
	 * @return the passed transaction timeout in seconds
	 */
	public static int getTransactionTimeout(Transaction tx) {
		try {
			return (Integer)getTXTimeoutMethod.invoke(getAtomicActionMethod.invoke(tx));
		} catch (Exception e) {
			System.err.println("Failed to get Transaction Timeout");
			e.printStackTrace(System.err);
			return 0;
		}
	}
	
	/**
	 * Acquires the UserTransaction for the current thread
	 * @return the UserTransaction
	 */
	public static UserTransaction getUserTransaction() {
		try {
			return (UserTransaction)getUserTransactionMethod.invoke(null);
		} catch (Exception e) {
			throw new RuntimeException("Failed to acquire UserTransaction", e);
		}
		
	}
	
	
	/**
	 * Returns the transaction manager's TX timeout for the current thread.
	 * @return the TX timeout for the current thread in seconds
	 */
	public static int getTransactionManagerTimeout() {
		try {
			return (Integer)getTimeoutMethod.invoke(getTransactionManager());
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 * Sets the transaction timeout for subsequent started transactions on the current thread.
	 * @param timeoutSeconds The transaction timeout in seconds
	 */
	public static void setTransactionTimeout(int timeoutSeconds) {
		try {
			getTransactionManager().setTransactionTimeout(timeoutSeconds);
		} catch (SystemException e) {
			throw new RuntimeException(new StringBuilder("Failed to set TXManager timeout to [").append(timeoutSeconds).append("] for Thread [").append(Thread.currentThread().getName()).append("/").append(Thread.currentThread().getId()).append("]").toString(), e);
		}
	}
	
	
	/**
	 * Returns the internal UID of the passed transaction
	 * @param tx The transaction
	 * @return the internal UID or a blank string not acquirable
	 */
	public static String getTransactionUID(Transaction tx) {
		try {
			return getUidMethod.invoke(tx).toString();
		} catch (Exception e) {
			return "<Unavailable>";
		}
	}
	
	/**
	 * Returns the internal UID of the current transaction
	 * @return the internal UID or a blank string not acquirable
	 */
	public static String getTransactionUID() {
		return getTransactionUID(getCurrentTransaction());
	}
	
	/**
	 * Returns the TXStatus of the passed transaction
	 * @param tx the transaction
	 * @return the TXStatus of the transaction or null if it could not be determined
	 */
	public static TXStatus getTransactionState(Transaction tx) {
		try {
			return TXStatus.statusOf(tx);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Returns the TXStatus of the current transaction
	 * @return the TXStatus of the transaction or null if it could not be determined
	 */
	public static TXStatus getTransactionState() {
		try {
			return TXStatus.statusOf(getCurrentTransaction());
		} catch (Exception e) {
			return null;
		}
	}

	
	
	/**
	 * Acquires the current transaction
	 * @return a JTA transaction or null if no transaction is associated with the current thread.
	 */
	public static Transaction getCurrentTransaction() {
		try {
			return getTransactionManager().getTransaction();			
		} catch (SystemException e) {
			throw new RuntimeException("System exception acquiring transaction", e);
		}
	}
	
	
	/**
	 * Returns a map of registered resources for the passed transaction
	 * The map is of the transaction TXInfo keyed by XA Resource
	 * @param tx the transaction to get the resources for 
	 * @return an unmodifiable map of the resources located for the passed transaction. If an exception occurs, the map will be empty.
	 */
	@SuppressWarnings("unchecked")
	public static Map<XAResource,?> getTransactionResources(Transaction tx) {
		if(xAResourcesField==null || tx==null || !TXClassName.equals(tx.getClass().getName())) {
			return Collections.emptyMap();
		}
		try {
			return Collections.unmodifiableMap((Map<? extends XAResource, ? extends Object>)xAResourcesField.get(tx));
		} catch (Throwable t) {
			LOG.error("Failed to acquire resources from Transaction [" + tx + "]", t);
			return  Collections.emptyMap();
		}
	}
	
	/**
	 * Returns a map of registered resources for the current transaction.
	 * The map is of the transaction TXInfo keyed by XA Resource
	 * @return an unmodifiable map of the resources located for the passed transaction. If an exception occurs, the map will be empty.
	 */
	public static Map<XAResource,?> getTransactionResources() {
		return getTransactionResources(getCurrentTransaction());
	}
	
	/**
	 * Generates a formatted string representing the registered resources in the passed transaction
	 * @param tx a JTA transaction
	 * @return a formatted string representing the registered resources in the passed transaction
	 */
	public static String getTransactionResourcesDump(Transaction tx) {
		if(tx==null) return "No Current Transaction";
		StringBuilder b = new StringBuilder("Transaction Registered Resource Dump \n\t[");
		b.append(tx.getClass().getName()).append(":").append(tx.toString()).append("]");
		for(Map.Entry<XAResource, ?> entry: getTransactionResources(tx).entrySet()) {
			b.append("\n\t").append(entry.getKey()).append(":").append(entry.getValue());
		}		
		b.append("\n====================================");
		return b.toString();
	}
	
	/**
	 * Executes the passed Runnable in a new transaction which is commited on successful completion or rolledback if the runnable execution throws an exception.
	 * If there is a transaction already in scope on call, it will be suspended and resumed on completion of this method.
	 * The TX timeout is 0 which is the TX Manager's default. 
	 * @param task A runnable task
	 */
	public static void executeInNewTX(Runnable task) {
		executeInNewTX(0, task);
	}
	
	/**
	 * Executes the passed Runnable in a new transaction which is commited on successful completion or rolledback if the runnable execution throws an exception.
	 * If there is a transaction already in scope on call, it will be suspended and resumed on completion of this method. 
	 * @param timeout The TX timeout in seconds.
	 * @param task A runnable task
	 */
	public static void executeInNewTX(int timeout, Runnable task) {
		if(task==null) {
			throw new RuntimeException("executeInNewTX was passed a null task");
		}
		Transaction currentTx = null;
		int currentTXTimeout = TransactionHelper.getTransactionManagerTimeout();
		try {
			currentTx = getTransactionManager().getTransaction();
			if(currentTx!=null) {
				getTransactionManager().suspend();
			}
			getTransactionManager().setTransactionTimeout(timeout);
			getTransactionManager().begin();
			task.run();
			getTransactionManager().commit();
		} catch (Exception e) {
			try { getTransactionManager().rollback(); } catch (Exception e2) {}
			throw new RuntimeException("executeInNewTX on instance of task [" + task.getClass().getName() + "] failed.", e);
		} finally {
			if(currentTx!=null) {
				try {
					getTransactionManager().resume(currentTx);
				} catch (Exception e) {
					LOG.error("Failed to resume transaction [" + currentTx + "]", e);
				}
			}
			try {
				getTransactionManager().setTransactionTimeout(currentTXTimeout);
			} catch (Exception e) {
				LOG.warn("Failed to reset TransactionTimeout to  [" + currentTXTimeout + "]", e);
			}
		}
	}
	
	/**
	 * Executes the passed Callable in a new transaction which is commited on successful completion or rolledback if the runnable execution throws an exception.
	 * If there is a transaction already in scope on call, it will be suspended and resumed on completion of this method. 
	 * @param task A callable task
	 * @return the return value of the callable
	 */
	public static <T> T executeInNewTX(Callable<T> task) {
		if(task==null) {
			throw new RuntimeException("executeInNewTX was passed a null task");
		}
		Transaction currentTx = null;
		try {
			currentTx = getTransactionManager().getTransaction();
			if(currentTx!=null) {
				getTransactionManager().suspend();
			}
			getTransactionManager().begin();
			T returnValue = task.call();
			getTransactionManager().commit();
			return returnValue;
		} catch (Exception e) {
			try { getTransactionManager().rollback(); } catch (Exception e2) {}
			throw new RuntimeException("executeInNewTX on instance of task [" + task.getClass().getName() + "] failed.", e);
		} finally {
			if(currentTx!=null) {
				try {
					getTransactionManager().resume(currentTx);
				} catch (Exception e) {
					LOG.error("Failed to resume transaction [" + currentTx + "]", e);
				}
			}
		}
	}
	

	/**
	 * Generates a formatted string representing the registered resources in the current transaction
	 * @return a formatted string representing the registered resources in the current transaction
	 */	
	public static String getTransactionResourcesDump() {
		return getTransactionResourcesDump(getCurrentTransaction());
	}
	
	/**
	 * Registers a synchronization against the passed transaction.
	 * @param tx The transaction to register with
	 * @param synch the synchronization to register
	 */
	public static void registerSynchronization(Transaction tx, Synchronization synch) {
		try {			
			tx.registerSynchronization(synch);
		} catch (Exception e) {
			throw new RuntimeException("Failed to register synchronization against tx [" + tx + "]", e);
		}
	}
	
	/**
	 * Registers a synchronization wrapped runnable against the passed transaction.
	 * @param tx The transaction to register with
	 * @param synch the synchronization runnable to register
	 * @param runOnStatus optional array of TXStatus. If this is null or zero length, the synchronization will fire on any status call back. If not, it will only fire on the passed statuses.
	 */
	public static void registerSynchronizationRunnable(Transaction tx, Runnable synch, TXStatus...runOnStatus) {
		try {
			String key = String.format("[%s]-[%s]-[%s]", "" + Thread.currentThread().getId(), "" + System.identityHashCode(synch), "" + random.nextLong());
			TX_REGISTRY.putResource(key, RunnableSynchronization.newInstance(synch, runOnStatus));
			//tx.registerSynchronization(RunnableSynchronization.newInstance(synch, runOnStatus));
		} catch (Exception e) {
			throw new RuntimeException("Failed to register synchronization wrapped runnable against tx [" + tx + "]", e);
		}
	}
	
	
	/**
	 * Registers a synchronization against the current transaction.
	 * @param synch the synchronization to register
	 */
	public static void registerSynchronization(Synchronization synch) {
		registerSynchronization(getCurrentTransaction(), synch);
	}
	
	
	/**
	 * Registers a synchronization wrapped runnable against the current transaction.
	 * @param synch the synchronization runnable to register
	 * @param runOnStatus optional array of TXStatus. If this is null or zero length, the synchronization will fire on any status call back. If not, it will only fire on the passed statuses.
	 * @return true if Synchronization was successfully registered.
	 */
	public static boolean registerSynchronizationRunnable(Runnable synch, TXStatus...runOnStatus) {
		Transaction tx = getCurrentTransaction();
		if(tx!=null) {
			registerSynchronization(getCurrentTransaction(), RunnableSynchronization.newInstance(synch, runOnStatus));
			return true;
		}
		return false;
	}
	
	/**
	 * Registers an interposed synchronization with the current transaction.
	 * The afterCompletion callback will be called after 2-phase commit completes but before any SessionSynchronization and Transaction afterCompletion callbacks. 
	 * @param synch The synchronization to register
	 * @param runOnStatus The TXStatus array on which the synchronization should fire. Null or an empty array means any status.
	 * @return true if the interposed synchronization was registered.
	 */
	public static boolean registerInterposedSynchronizationRunnable(Runnable synch, TXStatus...runOnStatus) {
		Transaction tx = getCurrentTransaction();
		if(tx!=null) {
			try {
				Object interposedSynch = interposedSynchronizationCtor.newInstance(RunnableSynchronization.newInstance(synch, runOnStatus), true);
				registerSynchronizationImpleMethod.invoke(tx, interposedSynch);
				return true;
			} catch (Exception e) {
				throw new IllegalStateException("Failed to register InterposedSynchronizationRunnable", e);
			}
		}
		return false;
	}
	
	
	/**
	 * Registers an interposed synchronization
	 * @param interposedSynch the interposed synchronization
	 */
	public static void registerInterposedSynchronization(Synchronization interposedSynch) {
		Transaction tx = getCurrentTransaction();
		if(getTransactionState().isActive() && tx != null) {
			try {
				registerSynchronizationImpleMethod.invoke(tx, interposedSynch);
			} catch (Exception e) {
				throw new IllegalStateException("Failed to register InterposedSynchronizationRunnable", e);
			}
		} else {
			throw new IllegalStateException("Request not executed with an active transaction", new Throwable());
		}
		
	}
	

	

	
	
	/**
	 * Registers for a logging callback when the current transaction completes.
	 * @param txEndMessage The message to display
	 * @param runOnStatus The TX Statuses to fire on.
	 * @return true if the callback request was registered successfully.
	 */
	public static boolean registerSynchronizationRunnable(final CharSequence txEndMessage, TXStatus...runOnStatus) {
		final Transaction tx = getCurrentTransaction();
		final Date startTime = new Date();
		if(tx!=null) {
			registerSynchronization(getCurrentTransaction(), RunnableSynchronization.newInstance(new Runnable(){
				@Override
				public void run() {
					Date endTime = new Date();
					TXStatus status = TXStatus.statusOf(tx);
					String txId = TransactionHelper.getTransactionUID(tx);
					StringBuilder b = new StringBuilder("\n\t================================\nTX Completion Audit Message\n\t================================");
					b.append("\n\tTransaction ID:").append(txId);
					b.append("\n\tTransaction State:").append(status.name());
					b.append("\n\tStart Time:").append(startTime);
					b.append("\n\tEnd Time:").append(endTime);
					b.append("\n\tTX End Message:").append(txEndMessage);	
					b.append("\n\t================================\n");
					LOG.info(b.toString());
				}
			}, runOnStatus));
			LOG.info("Registered TX Completion Notice Request for [" + TransactionHelper.getTransactionUID(tx) + "]");
			return true;
		}
		return false;		
	}
	
	
	
	
	
	/**
	 * <p>Title: RunnableSynchronization</p>
	 * <p>Description: Utility class to wrap a runnable in a TX synchronization</p> 
	 * <p>Company: ICE Futures US</p>
	 * @author Whitehead (nicholas.whitehead@theice.com)
	 * @version $LastChangedRevision$
	 * <p><code>com.onexchange.tx.util.RunnableSynchronization</code></p>
	 */
	public static class RunnableSynchronization implements Runnable, Synchronization {
		/** The runnable to execute on the transaction state change */
		protected final Runnable runnable;
		/** An array of statuses that the runnable should be fired on */
		protected final TXStatus[] runOnStatus;
		/** The current transaction status */
		protected TXStatus currentStatus = null;
		
		private RunnableSynchronization(Runnable runnable, TXStatus...runOnStatus) {
			this.runnable = runnable;
			this.runOnStatus = runOnStatus;
		}
		
		
		/**
		 * Creates a new RunnableSynchronization.
		 * @param runnable The runnable to execute on the transaction state change
		 * @param runOnStatus An array of statuses that the runnable should be fired on
		 * @return the created RunnableSynchronization
		 */
		public static RunnableSynchronization newInstance(Runnable runnable, TXStatus...runOnStatus) {
			return new RunnableSynchronization(runnable, runOnStatus);
		}
		
		/**
		 * {@inheritDoc}
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			runnable.run();
		}

		/**
		 * {@inheritDoc}
		 * @see javax.transaction.Synchronization#afterCompletion(int)
		 */
		@Override
		public void afterCompletion(int status) {
			try {
				txStat.set(TXStatus.decode(status));
				if(runOnStatus==null || runOnStatus.length < 1) {
					run();
				} else {
					TXStatus txStat = TXStatus.decode(status);
					for(TXStatus txs: runOnStatus) {
						if(txs.equals(txStat)) {
							run();
							break;
						}
					}
				}
			} finally {
				txStat.set(null);
			}
		}

		/**
		 * {@inheritDoc}
		 * @see javax.transaction.Synchronization#beforeCompletion()
		 */
		@Override
		public void beforeCompletion() {
			/* No Op */
		}
		
	}
	

	/**
	 * Starts a transaction if one is not currently running.
	 */
	public static void startTXIfNoneRunning() {
		try {
			Transaction tx = getCurrentTransaction();
			if(tx==null || !TXStatus.statusOf(tx).isActive()) {
				getTransactionManager().begin();
			}
		} catch (Exception e) {
			LOG.warn("Failed to call startTXIfNoneRunning", e);
			throw new RuntimeException("Failed to call startTXIfNoneRunning", e);
		}
	}
	
	/**
	 * Commits a TX is one is running
	 */
	public static void stopTXIfOneIsRunning() {
		Transaction tx = null;
		try {
			tx = getCurrentTransaction();
			if(tx==null) return;
			if(TXStatus.statusOf(tx).isActive()) {
				try {
					getTransactionManager().commit();
				} catch (Exception e) {
					LOG.warn("Failed to call commit in stopTXIfOneIsRunning", e);
					try { getTransactionManager().rollback(); } catch (Exception ex) {}
				}
			} else {
				try { getTransactionManager().rollback(); } catch (Exception e) {}
			}
		} catch (Exception e) {
			LOG.warn("Failed to call stopTXIfOneIsRunning", e);			
		} finally {
			tx = getCurrentTransaction();
			if(tx!=null) {
				throw new RuntimeException("stopTXIfOneIsRunning failed to disassociate TX from thread", new Throwable());
			}
		}
	}

	/**
	 * Returns the transaction registry for the current transaction
	 * @return the transaction registry for the current transaction
	 */
	public TransactionSynchronizationRegistry getTXRegistry() {
		return TX_REGISTRY;
	}
	
	
	
	//=============================================================================================
	//  The Arjuna TransactionSynchronizationRegistry implementation is currently broken
	//  so we're using a home grown one which only implements a portion of the spec.
	//  to provide just what we need
	//=============================================================================================
	
//	/**
//	 * Loads the TXManager's TransactionSynchronizationRegistry
//	 */
//	public static TransactionSynchronizationRegistry getTXRegistry() {
//		if(!TransactionSynchronizationRegistry.registered.get()) {
//			synchronized(TransactionSynchronizationRegistry.registered) {
//				if(!TransactionSynchronizationRegistry.registered.get()) {
//					LOG.info("Loading TransactionSynchronizationRegistry");
//					try {
//						Class<?> clazz = Class.forName("com.arjuna.ats.jta.utils.JNDIManager");
//						clazz.getDeclaredMethod("bindJTATransactionSynchronizationRegistryImplementation").invoke(null);
//						TransactionSynchronizationRegistry.registered.set(true);
//						LOG.info("Registered TransactionSynchronizationRegistry");
//					} catch (Exception e) {
//						LOG.fatal("Failed to load TransactionSynchronizationRegistry", e);
//						throw new RuntimeException("Failed to load TransactionSynchronizationRegistry", e);
//					}
//					
//				}
//			}
//		}
//		return generateTXRegProxy();
//	}
	
//	/**
//	 * Generates a dynamic proxy to invoke against the real TransactionSynchronizationRegistry
//	 * @return a TransactionSynchronizationRegistry dynamic proxy.
//	 */
//	private static TransactionSynchronizationRegistry generateTXRegProxy() {
//		try {
//			final Object registry = JNDI.lookup(TransactionSynchronizationRegistry.JNDI_NAME);
//			final Class<?> registryClass = registry.getClass();
//			return (TransactionSynchronizationRegistry) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[]{TransactionSynchronizationRegistry.class}, new InvocationHandler(){
//				public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//					return registryClass.getDeclaredMethod(method.getName(), method.getParameterTypes()).invoke(registry, args);
//				}
//				
//			});
//			
//		} catch (Exception e) {
//			LOG.warn("Failed to generate TransactionSynchronizationRegistry Proxy Invoker. Returning default impl.", e);
//			return DefaultTXRegistryImpl.getInstance();
//			
//		}
//	}
	
	//=============================================================================================	
	
}
