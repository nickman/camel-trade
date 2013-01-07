
package org.tradex.spring;

import java.lang.management.ManagementFactory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.ConstructorArgumentValues.ValueHolder;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.support.MethodReplacer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jmx.export.MBeanExporter;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.naming.SelfNaming;
import org.tradex.jmx.JMXHelper;
import org.tradex.util.Banner;



/**
 * <p>Title: BeanContextUtil</p>
 * <p>Description: Spring app context utilities</p> 
 * <p>Company: Helios Development Group LLC</p>
 * @author Whitehead (nwhitehead AT heliosdev DOT org)
 * <p><code>org.tradex.spring.BeanContextUtil</code></p>
 */
public class BeanContextUtil  {
	
	/** A static reference to the fully initialized BeanContextUtil instance */
	protected static final AtomicReference<BeanContextUtil> staticInstance = new AtomicReference<BeanContextUtil>(null);
	
	/** A latch to wait on for threads wishing to wait for this service to be ready */
	protected static final CountDownLatch readyLatch = new CountDownLatch(1);
	
	/** The wrapping generic application context */
	protected GenericApplicationContext genericAppContext = null;
	/** The Spring autowiring factory */
	protected AutowireCapableBeanFactory autoWiringFactory = null;
	
	/** Static class logger */
	protected static final Logger log = Logger.getLogger(BeanContextUtil.class);
	
	/**
	 * A static accessor for the BeanContextUtil
	 * @return the BeanContextUtil singleton
	 */
	public static BeanContextUtil get() {
		BeanContextUtil bcu = staticInstance.get();
		if(bcu==null) throw new IllegalStateException("The BeanContextUtil has not been initialized", new Throwable());
		return bcu;
	}
	
	/**
	 * A static accessor for the BeanContextUtil
	 * @param waitFor If true, if the BeanContextUtil static instance has not been set, the thread will wait for it.
	 * @return the BeanContextUtil singleton
	 */
	public static BeanContextUtil get(boolean waitFor) {
		if(waitFor) {
			try {
				readyLatch.await();
			} catch (InterruptedException ie) {}
		} 
		return get();
	}
	
	/** The default data source for settlement runs */
	@Autowired(required=true)
	@Qualifier("Default")
	protected DataSource defaultDataSource = null;

	
	

	/** The JdbcTemplate for the default data source for settlement runs */
	protected JdbcTemplate defaultJdbcTemplate = null;
	/** The named parameter JdbcTemplate for the default data source for settlement runs */
	protected NamedParameterJdbcTemplate defaultNamedJdbcTemplate = null;
	
	

	/** A map of all sprng defined data sources keyed by bean name */
	protected final Map<String, DataSource> allDataSources = new HashMap<String, DataSource>();
	/** A lazilly populated map of JDBC templates accessed by DataSource bean name  */
	protected final Map<String, JdbcTemplate> allJdbcTemplates = new ConcurrentHashMap<String, JdbcTemplate>();
	/** A lazilly populated map of named parameter JDBC templates accessed by DataSource bean name  */
	protected final Map<String, NamedParameterJdbcTemplate> allNamedJdbcTemplates = new ConcurrentHashMap<String, NamedParameterJdbcTemplate>();

	/** The core spring application context */
	private ApplicationContext applicationContext;

	
	/**
	 * Callback from the Spring context when it has refreshed
	 * @param event The context refreshed event
	 */	
	public void onContextRefreshedEvent(ContextRefreshedEvent event) {		
		
		defaultJdbcTemplate = new JdbcTemplate(defaultDataSource);
		defaultNamedJdbcTemplate = new NamedParameterJdbcTemplate(defaultDataSource);
		allDataSources.putAll(applicationContext.getBeansOfType(DataSource.class));
		if(applicationContext instanceof GenericApplicationContext) {
			genericAppContext = (GenericApplicationContext)applicationContext;
		} else {
			//genericAppContext = new GenericApplicationContext((DefaultListableBeanFactory) applicationContext.getBeanFactory(), applicationContext);
			genericAppContext = new GenericApplicationContext(applicationContext);
		}		
		autoWiringFactory = applicationContext.getAutowireCapableBeanFactory();		
		log.info(Banner.banner("*", 3, 10, "BeanContextUtil Started", "JVM Process:" + ManagementFactory.getRuntimeMXBean().getName()).toString());
		staticInstance.set(this);
		readyLatch.countDown();		
	}
	
	/**
	 * Returns a generic application context wrapper
	 * @return a generic application context 
	 */
	public ApplicationContext getGenericApplicationContext() {
		return genericAppContext;
	}
	

	
	/**
	 * Returns the default JdbcTemplate
	 * @return the default JdbcTemplate
	 */
	public JdbcTemplate getDefaultJDBCTemplate() {
		return defaultJdbcTemplate;
	}
	
	/**
	 * Returns a JdbcTemplate for the named data source
	 * @param dsName The bean name of the datasource to get a JDBC template for
	 * @return a JdbcTemplate
	 */
	public JdbcTemplate getJDBCTemplate(String dsName) {
		if(dsName==null) throw new IllegalArgumentException("The passed JDBCTemplate name was null", new Throwable());
		JdbcTemplate template = allJdbcTemplates.get(dsName);
		if(template==null) {
			synchronized(allJdbcTemplates) {
				template = allJdbcTemplates.get(dsName);
				if(template==null) {
					DataSource ds = allDataSources.get(dsName);
					if(ds==null) {
						throw new IllegalStateException("No datasource registered with bean name [" + dsName + "]", new Throwable());
					}
					template = new JdbcTemplate(ds);
					allJdbcTemplates.put(dsName, template);
				}
			}
		}
		return template;
	}
	
	/**
	 * Returns a NamedParameterJdbcTemplate for the named data source
	 * @param dsName The bean name of the datasource to get a NamedParameterJdbcTemplate for
	 * @return a NamedParameterJdbcTemplate
	 */
	public NamedParameterJdbcTemplate getNamedJDBCTemplate(String dsName) {
		if(dsName==null) throw new IllegalArgumentException("The passed NamedParameterJdbcTemplate name was null", new Throwable());
		NamedParameterJdbcTemplate template = allNamedJdbcTemplates.get(dsName);
		if(template==null) {
			synchronized(allNamedJdbcTemplates) {
				template = allNamedJdbcTemplates.get(dsName);
				if(template==null) {
					DataSource ds = allDataSources.get(dsName);
					if(ds==null) {
						throw new IllegalStateException("No datasource registered with bean name [" + dsName + "]", new Throwable());
					}
					template = new NamedParameterJdbcTemplate(ds);
					allNamedJdbcTemplates.put(dsName, template);
				}
			}
		}
		return template;
	}	
	
	
	/**
	 * Returns the default named parameter JdbcTemplate
	 * @return the default named parameter JdbcTemplate
	 */
	public NamedParameterJdbcTemplate getDefaultNamedJDBCTemplate() {
		return defaultNamedJdbcTemplate;
	}
	
	
	/**
	 * Returns the default data source for this platform
	 * @return the default data source
	 */
	public DataSource getDefaultDataSource() {
		return defaultDataSource;
	}
	
	/**
	 * Returns the named data source
	 * @param name The bean name of the datasource to retrieve
	 * @return the named data source
	 */
	public DataSource getDataSource(String name) {
		if(name==null) throw new IllegalArgumentException("The passed name was null", new Throwable());
		DataSource ds = allDataSources.get(name);
		if(ds==null) throw new IllegalArgumentException("The passed name [" + name + "] is not a registered data source", new Throwable());
		return ds;
	}
	
	/**
	 * Retrieves a number from the named sequence in the default data source
	 * @param sequenceName The sequence name
	 * @return The next value of the sequence
	 */
	public Number getDefaultSequence(String sequenceName) {	
		return _getSequence(sequenceName, getDefaultJDBCTemplate());
	}
	
	/**
	 * Retrieves a number from the named sequence in the default data source
	 * @param sequenceName The sequence name
	 * @param dsName The bean name of the datasource to get the sequence value from
	 * @return The next value of the sequence
	 */
	public Number getDefaultSequence(String sequenceName, String dsName) {	
		if(dsName==null) throw new IllegalArgumentException("The passed data source bean was null", new Throwable());
		return _getSequence(sequenceName, getJDBCTemplate(dsName));
	}
	
	
	/**
	 * Retrieves a number from the named sequence in the data source the passed template is declared for
	 * @param sequenceName The sequence name
	 * @param template The template to use
	 * @return The next value of the sequence
	 */
	protected Number _getSequence(String sequenceName, JdbcTemplate template) {
		if(sequenceName==null) throw new IllegalArgumentException("The passed sequence name was null", new Throwable());
		return template.query("SELECT " + sequenceName + ".NEXTVAL FROM DUAL", new ResultSetExtractor<Number>(){
			public Number extractData(ResultSet rs) throws SQLException, DataAccessException {
				rs.next();
				Number number = (Number)rs.getObject(1);
				return number;
			}
		});		
		
	}

	
	/**
	 * Wires a bean created outside the application context
	 * @param <T> The type of the bean being passed
	 * @param clazz The type of the bean to be created
	 * @param ctorArgs The constructor arguments to use when constructing the bean
	 * @return A newly created and wired bean
	 */	
	@SuppressWarnings("unchecked")
	public <T> T createWiredBeanInstance(Class<T> clazz, Object...ctorArgs) {
		String beanName = clazz.getName() + ".BeanDefinition";
		
		if(!genericAppContext.containsBeanDefinition(beanName)) {
			synchronized(genericAppContext) {
				if(!genericAppContext.containsBeanDefinition(beanName)) {
					GenericBeanDefinition beanDef = new GenericBeanDefinition(); 
					beanDef.setBeanClass(clazz);
					beanDef.setScope("prototype");
					ConstructorArgumentValues ctorValues = new ConstructorArgumentValues();
					if(ctorArgs!=null) {
						for(int i = 0; i < ctorArgs.length; i++) {
							ctorValues.addGenericArgumentValue(new ValueHolder(ctorArgs[i], ctorArgs[i].getClass().getName()));							
						}
						beanDef.setConstructorArgumentValues(ctorValues);
					}
					genericAppContext.registerBeanDefinition(beanName, beanDef);					
				}
			}
		}		
		T t =  (T)genericAppContext.getBean(beanName, ctorArgs);
		autoWiringFactory.autowireBean(t);
		return t;
	}
	
	/**
	 * Wires an already created bean
	 * @param bean The bean to wire
	 */
	public void wireBean(Object bean) {
		autoWiringFactory.autowireBean(bean);
		autoWiringFactory.applyBeanPostProcessorsAfterInitialization(bean, bean.getClass().getName() + ".BeanDefinition");
		 
	}

	/**
	 * Wires a bean created outside the application context
	 * @param <T> The type of the bean being passed
	 * @param factoryMethodName The static method name in the target class to use as a factory. Ignored if null.
	 * @param clazz The type of the bean to be created
	 * @param ctorArgs The constructor arguments to use when constructing the bean
	 * @return A newly created and wired bean
	 */	
	public <T> T factorizeWiredBeanInstance(String factoryMethodName, Class<T> clazz, Object...ctorArgs) {
		String beanName = clazz.getName() +  ".BeanDefinition";//generateKey(ctorArgs);		
		if(!genericAppContext.containsBeanDefinition(beanName)) {
			synchronized(genericAppContext) {
				if(!genericAppContext.containsBeanDefinition(beanName)) {
					GenericBeanDefinition beanDef = new GenericBeanDefinition(); 
					beanDef.setBeanClass(clazz);
					beanDef.setScope("prototype");
					if(factoryMethodName!=null) {
						beanDef.setFactoryMethodName(factoryMethodName);
					}
					ConstructorArgumentValues ctorValues = new ConstructorArgumentValues();
					if(ctorArgs!=null) {
						for(int i = 0; i < ctorArgs.length; i++) {
							if(ctorArgs[i]==null) {
								ctorValues.addIndexedArgumentValue(i, (Object)null);
							} else {
								ctorValues.addIndexedArgumentValue(i, new ValueHolder(ctorArgs[i], ctorArgs[i].getClass().getName()));
							}
						}
						beanDef.setConstructorArgumentValues(ctorValues);
//						beanDef.setSynthetic(true);
						
					}
					genericAppContext.registerBeanDefinition(beanName, beanDef);					
				}
			}
		}		
		
		T t = (T)genericAppContext.getBean(beanName, ctorArgs);
		if(clazz.getAnnotation(ManagedResource.class)!=null) {
			MBeanExporter exporter = applicationContext.getBean(MBeanExporter.class);
			try {
				ObjectName on = getObjectName(t);
				if(on!=null) {
					exporter.registerManagedResource(t, on);
				} else {
					exporter.registerManagedResource(t);
				}
			} catch (Exception e) {
				e.printStackTrace(System.err);
			}
		}
		return t;
	}
	
	/**
	 * Determines the intended JMX ObjectName for the passed instance.
	 * @param instance The object to determine the JMX ObjectName for
	 * @return an ObjectName or null if one could not be determined.
	 */
	protected static ObjectName getObjectName(Object instance) {
		Class<?> clazz = instance.getClass();
		ObjectName on = null;
		ManagedResource mr = clazz.getAnnotation(ManagedResource.class);
		if(mr!=null && !mr.objectName().equals("")) {
			return JMXHelper.objectName(mr.objectName());
		}
		if(instance instanceof SelfNaming) {
			try {
				return ((SelfNaming)instance).getObjectName();
			} catch (MalformedObjectNameException e) {
			}
		}
		return on;
	}
	
	/**
	 * Generates a unique key from the passed args.
	 * @param ctorArgs
	 * @return
	 */
	protected static String generateKey(Object...ctorArgs) {
		StringBuilder b = new StringBuilder();
		if(ctorArgs!=null) {
			for(Object obj: ctorArgs) {
				if(obj!=null) {
					b.append(obj.toString());
				}
			}
		}
		if(b.length()<1) {
			b.append(System.nanoTime());
		}
		return b.toString();
	}

	/**
	 * Sets the default datasource
	 * @param defaultDataSource
	 */
	public void setDefaultDataSource(DataSource defaultDataSource) {
		this.defaultDataSource = defaultDataSource;
	}




	

}
