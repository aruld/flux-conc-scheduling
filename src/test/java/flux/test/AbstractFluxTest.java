package flux.test;

import flux.*;
import fluximpl.org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public abstract class AbstractFluxTest {
    protected Factory factory = Factory.makeInstance();
    private Configuration config;
    protected Engine engine;
    protected String enginePropertiesFile = "engine-config.properties";
    private final String fluxUsername = "admin";
    private final String fluxPassword = "admin";
    protected boolean clearEngine = false;
    protected Date startTime;
    public static boolean fired = false;

    Logger log = LoggerFactory.getLogger(AbstractFluxTest.class);

    @Before
    public void setUpTest() throws Exception {
        log.info("[START] AbstractFluxTest.setUpTest");

        startTime = new Date();
        String database = System.getProperty("database");
        if (database != null && database.equalsIgnoreCase(DatabaseType.POSTGRES.toString())) {
            enginePropertiesFile = "engine-postgres-config.properties";
            log.info("Using Postgres backend.");
        } else if (database != null && database.equalsIgnoreCase(DatabaseType.MYSQL.toString())) {
            enginePropertiesFile = "engine-mysql-config.properties";
            log.info("Using MySQL backend.");
        } else if (database != null && database.equalsIgnoreCase(DatabaseType.SQL_SERVER.toString())) {
            enginePropertiesFile = "engine-sqlserver-config.properties";
            log.info("Using SQL Server backend.");
        }
        config = new Configuration(enginePropertiesFile);
        engine = factory.makeEngine(config);
        engine.login(fluxUsername, fluxPassword);
        String clear = System.getProperty("clearEngine");
        if (StringUtils.isNotBlank(clear)) {
            clearEngine = Boolean.valueOf(clear);
        }
        if (clearEngine) {
            log.info("[START] AbstractFluxTest.setUpTest.clearEngine");
            engine.clear();
            log.info("[END] AbstractFluxTest.setUpTest.clearEngine");
        }
        engine.start();
        log.info("[END] AbstractFluxTest.setUpTest");
        log.info("Engine version # " + engine.getVersion());
    }

    @After
    public void tearDownTest() throws Exception {
        log.info("[START] AbstractFluxTest.tearDownTest");
        fired = false;
        Thread.sleep(5000);
        engine.stop();
        engine.dispose();

        log.info("[END] AbstractFluxTest.tearDownTest");
    }

    protected boolean didActionExecute(String namespace, String actionName) throws Exception {
        log.info("didActionExecute namespace: " + namespace + " actionName: " + actionName);
        ActionHistoryIterator it = engine.getActionHistory(namespace, actionName, startTime, new Date());

        try {
            return it.next() != null;
        } catch (Exception e) {
            return false;
        } finally {
            it.close();
        }
    }

    public long waitForRuns(String namespace, int minimumExpectedRunCount, int delayInSeconds, int timeoutInSeconds) throws Exception {
        long runCount = 0;
        long totalTime = 0;
        int delayInMillis = delayInSeconds * 1000;

        String initialMsg = "waitForRuns namespace:" + namespace + " minimumExpectedRunCount: " + minimumExpectedRunCount + " delayInSeconds: " + delayInSeconds +
                " timeoutInSeconds: " + timeoutInSeconds;
        log.info(initialMsg);

        while (runCount < minimumExpectedRunCount && totalTime < timeoutInSeconds) {
            runCount = engine.getRunCount(namespace);

            log.info(initialMsg + " runCount: " + runCount + " totalTime: " + totalTime);
            if (runCount < minimumExpectedRunCount) {
                Thread.sleep(delayInMillis);
                totalTime += delayInSeconds;
            }
        }

        return runCount;
    }
}