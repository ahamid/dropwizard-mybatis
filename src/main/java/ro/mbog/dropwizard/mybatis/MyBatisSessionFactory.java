package ro.mbog.dropwizard.mybatis;

import com.yammer.dropwizard.config.Environment;
import org.apache.ibatis.session.Configuration;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.apache.tomcat.dbcp.dbcp.DriverManagerConnectionFactory;
import org.apache.tomcat.dbcp.dbcp.PoolableConnectionFactory;
import org.apache.tomcat.dbcp.dbcp.PoolingDataSource;
import org.apache.tomcat.dbcp.pool.impl.GenericObjectPool;

import javax.sql.DataSource;
import java.util.Map;
import java.util.Properties;

import ro.mbog.dropwizard.mybatis.mapper.PingMapper;

/**
 * Created with IntelliJ IDEA.
 * User: marius.bogdanescu
 * Date: 12/7/12
 * Time: 8:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyBatisSessionFactory {

    private Environment environment;

    public MyBatisSessionFactory(Environment environment){
        this.environment = environment;
    }

    public Database build(DatabaseEnvironmentConfiguration environmentConfiguration) throws ClassNotFoundException{
        DatabaseConfiguration configuration = environmentConfiguration.getDatabase();
        Class.forName(configuration.getDriverClass());
        final GenericObjectPool pool = buildPool(configuration);
        final DataSource dataSource = buildDataSource(configuration, pool);
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        org.apache.ibatis.mapping.Environment myBatisEnvironment = new org.apache.ibatis.mapping.Environment(environmentConfiguration.getName(), transactionFactory, dataSource);
        Configuration mybatisConfiguration = new Configuration(myBatisEnvironment);
        mybatisConfiguration.addMappers(environmentConfiguration.getMapperPackage());
        mybatisConfiguration.addMapper(PingMapper.class);

        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(mybatisConfiguration);
        Database database = new Database(sqlSessionFactory, pool, configuration.getValidationQuery());
        environment.manage(database);
        return database;
    }

    private static DataSource buildDataSource(DatabaseConfiguration connectionConfig, GenericObjectPool pool) {
        final Properties properties = new Properties();
        for (Map.Entry<String, String> property : connectionConfig.getProperties().entrySet()) {
            properties.setProperty(property.getKey(), property.getValue());
        }
        properties.setProperty("user", connectionConfig.getUser());
        properties.setProperty("password", connectionConfig.getPassword());

        final DriverManagerConnectionFactory factory = new DriverManagerConnectionFactory(connectionConfig.getUrl(),
                properties);


        final PoolableConnectionFactory connectionFactory = new PoolableConnectionFactory(factory,
                pool,
                null,
                connectionConfig.getValidationQuery(),
                connectionConfig.isDefaultReadOnly(),
                true);
        connectionFactory.setPool(pool);

        return new PoolingDataSource(pool);
    }

    private static GenericObjectPool buildPool(DatabaseConfiguration configuration) {
        final GenericObjectPool pool = new GenericObjectPool(null);
        pool.setMaxWait(configuration.getMaxWaitForConnection().toMilliseconds());
        pool.setMinIdle(configuration.getMinSize());
        pool.setMaxActive(configuration.getMaxSize());
        pool.setMaxIdle(configuration.getMaxSize());
        pool.setTestWhileIdle(configuration.isCheckConnectionWhileIdle());
        pool.setTimeBetweenEvictionRunsMillis(configuration.getCheckConnectionHealthWhenIdleFor().toMilliseconds());
        pool.setMinEvictableIdleTimeMillis(configuration.getCloseConnectionIfIdleFor()
                .toMilliseconds());
        pool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_BLOCK);
        return pool;
    }
}
