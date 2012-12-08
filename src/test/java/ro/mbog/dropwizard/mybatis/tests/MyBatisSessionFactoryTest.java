package ro.mbog.dropwizard.mybatis.tests;

import com.yammer.dropwizard.config.Environment;
import org.junit.Before;
import org.junit.Test;
import ro.mbog.dropwizard.mybatis.Database;
import ro.mbog.dropwizard.mybatis.DatabaseConfiguration;
import ro.mbog.dropwizard.mybatis.DatabaseEnvironmentConfiguration;
import ro.mbog.dropwizard.mybatis.MyBatisSessionFactory;

import java.sql.SQLException;

import static org.mockito.Mockito.*;
import static org.fest.assertions.api.Assertions.*;

/**
 * Created with IntelliJ IDEA.
 * User: marius.bogdanescu
 * Date: 12/8/12
 * Time: 8:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyBatisSessionFactoryTest {

    private DatabaseEnvironmentConfiguration environmentConfiguration;
    private Environment environment = mock(Environment.class);
    @Before
    public void setUp(){
        environmentConfiguration = new DatabaseEnvironmentConfiguration();
        environmentConfiguration.setName("test");
        environmentConfiguration.setMapperPackage("ro.mbog.dropwizard.mybatis.tests.mapper");
        DatabaseConfiguration databaseConfiguration = new DatabaseConfiguration();
        databaseConfiguration.setDriverClass("org.h2.Driver");
        databaseConfiguration.setUser("user");
        databaseConfiguration.setPassword("pass");
        databaseConfiguration.setUrl("jdbc:h2:mem:test");
        environmentConfiguration.setDatabase(databaseConfiguration);
    }

    @Test
    public void testDatabaseConnection() throws ClassNotFoundException {
        MyBatisSessionFactory factory = new MyBatisSessionFactory(environment);
        Database database = factory.build(environmentConfiguration);
        try {
            database.ping();
        } catch (SQLException e) {
            fail("Database exception thrown.");
        }

    }
}
