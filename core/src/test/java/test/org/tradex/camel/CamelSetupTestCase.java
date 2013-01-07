
package test.org.tradex.camel;

import java.sql.Connection;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.tradex.jdbc.JDBCHelper;
import org.tradex.spring.BeanContextUtil;

/**
 * <p>Title: CamelSetupTestCase</p>
 * <p>Description: Base test class that builds a Camel context for testing</p> 
 * <p>Company: ICE</p>
 * @author Whitehead 
 * <p><code>com.cpex.spice.test.camel.CamelSetupTestCase</code></p>
 */

public class CamelSetupTestCase {
	/** Static class logger */
	protected static final Logger LOG = Logger.getLogger(CamelSetupTestCase.class);
	/** Class app context */
	protected static GenericXmlApplicationContext APP_CONTEXT = null;
	/** JDBC Helper */
	protected static JDBCHelper jdbcHelper;
	/** JDBC Connection (holds open the in mem DB) */
	protected static Connection conn;
	private static BeanContextUtil beanContextUtil;
	
	/** Random Number Generator */
	protected static final Random random = new Random(System.nanoTime());
	/** Serial Number Generator */
	protected static final AtomicLong serial = new AtomicLong(0L);
	
	/**  */
	protected final static String LOG4J_CONFIGURED = "settlement.run.log4j.configured";
	
	protected static void checkLog4j() {
		if(!System.getProperty(LOG4J_CONFIGURED, "false").equals("true")) {
			BasicConfigurator.configure();
			System.setProperty(LOG4J_CONFIGURED, "true");
		} 
		Logger.getLogger("com.arjuna.ats.arjuna").setLevel(Level.WARN);
	}
	
	/** A day's worth of ms.  */
	public static final long ONE_DAY_IN_MS = 1000 * 60 * 60 * 24;
	/** A weeks's worth of ms.  */
	public static final long ONE_WEEK_IN_MS = ONE_DAY_IN_MS * 7;
	
	
	/**
	 * Test case init
	 * @throws Exception thrown on any errors
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		checkLog4j();
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getLogger(CamelSetupTestCase.class).setLevel(Level.INFO);
		APP_CONTEXT = TestCaseAppContextBuilder.buildFor("./src/main/resources/spring/conf/spring.xml");
		Assert.assertNotNull("The static test app context", APP_CONTEXT);
		jdbcHelper = new JDBCHelper((DataSource)APP_CONTEXT.getBean("primaryDataSource"));
		conn = APP_CONTEXT.getBean("primaryDataSourceH2", DataSource.class).getConnection();
		beanContextUtil = BeanContextUtil.get(true);
		LOG.info("Event test DB setup");
		serial.set(0L);
	}
	
	/** The current test name */
	@Rule
	public TestName name = new TestName();
	
	
	/**
	 *  Logs the test name about to be executed. 
	 */
	protected void logTestName() {
		StringBuilder b = new StringBuilder("\n\t^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n\t");
		b.append("Running Test [").append(name.getMethodName()).append("]\n\t");
		b.append("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n");
		LOG.info(b);
	}
	

	/**
	 * Test case cleanup
	 * @throws Exception thrown on any errors
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		if(conn!=null) {
			try { conn.close(); } catch (Exception e) {}
		}
		if(APP_CONTEXT!=null) {
			APP_CONTEXT.close();
			APP_CONTEXT.destroy();
		}
	}

	/**
	 * Test init
	 * @throws Exception thrown on any errors
	 */
	@Before
	public void setUp() throws Exception {
		Logger.getLogger(getClass()).setLevel(Level.INFO);
		logTestName();		
	}

	/**
	 * Test cleanup
	 * @throws Exception thrown on any errors
	 */
	@After
	public void tearDown() throws Exception {
		StringBuilder b = new StringBuilder("\n\tVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV\n\t");
		b.append("COMPLETED Test [").append(name.getMethodName()).append("]\n\t");
		b.append("VVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV\n");
		LOG.info(b);
	}

	
	/**
	 * Tests the spring launch of the current config
	 * @throws Exception thrown on any errors
	 */
	@Test
	public void testDBSetup() throws Exception {
		LOG.info("Testing DB Setup....");
		int knownTableCount = jdbcHelper.queryForInt("SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME IN ('AUDITLOG', 'CAMEL_MESSAGEPROCESSED')");
		Assert.assertEquals("The number of known table names",  2, knownTableCount);
		
		
	}
	/** Class name signature that class has been cglibbed */
	public static final String CGLIB_SIGNATURE = "$$EnhancerByCGLIB$$";
	
	/**
	 * Removes the CGLib suffix from the end of a string 
	 * @param s The string to clean (usually a class name)
	 * @return the cleaned string or the passed value if it did not have the cglib signature.
	 */
	public static String cleanCgString(String s) {
		if(s==null || !s.contains(CGLIB_SIGNATURE)) return s;
		return s.substring(0, s.indexOf(CGLIB_SIGNATURE));
	}
	

	
	
	
	/**
	 * Stalls the test 
	 * @throws Exception thrown on any errors
	 */
	@Test(timeout=Long.MAX_VALUE)	
	public void stall() throws Exception {
		Thread.currentThread().join();
	}


}
