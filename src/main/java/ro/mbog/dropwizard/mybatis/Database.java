package ro.mbog.dropwizard.mybatis;

import ch.qos.logback.classic.Level;
import com.yammer.dropwizard.lifecycle.Managed;
import com.yammer.metrics.Metrics;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.tomcat.dbcp.pool.ObjectPool;

import java.sql.SQLException;

/**
 * Created with IntelliJ IDEA.
 * User: marius.bogdanescu
 * Date: 12/8/12
 * Time: 12:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class Database implements Managed {

    private SqlSessionFactory sqlSessionFactory;

    private ObjectPool pool;

    private String validationQuery;

    public Database(SqlSessionFactory sqlSessionFactory, ObjectPool pool, String validationQuery){
        this.sqlSessionFactory = sqlSessionFactory;
        this.pool = pool;
        this.validationQuery = validationQuery;
    }

    public SqlSession openSession(){
        return this.sqlSessionFactory.openSession();
    }

    @Override
    public void start() throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void stop() throws Exception {
        pool.close();
    }

    public void ping() throws SQLException {
        SqlSession session = sqlSessionFactory.openSession();
        try {
            session.selectOne(validationQuery);
        } finally {
            session.close();
        }
    }
}
