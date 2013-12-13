package ro.mbog.dropwizard.mybatis.transaction;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.spi.container.ResourceMethodDispatchAdapter;
import com.sun.jersey.spi.container.ResourceMethodDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;
import org.apache.ibatis.reflection.ExceptionUtil;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionManager;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Implements around advice that wraps resources in MyBatis SqlSessionManager transactions
 */
public class MyBatisTxResourceMethodDispatchAdapter implements ResourceMethodDispatchAdapter {
    private final SqlSessionManager sessionMgr;

    public MyBatisTxResourceMethodDispatchAdapter(SqlSessionManager sessionMgr) {
        this.sessionMgr = sessionMgr;
    }

    @Override
    public ResourceMethodDispatchProvider adapt(ResourceMethodDispatchProvider provider) {
        return new MyBatisTxResourceMethodDispatchProvider(provider, sessionMgr);
    }

    private static class MyBatisTxResourceMethodDispatchProvider implements ResourceMethodDispatchProvider {
        private final ResourceMethodDispatchProvider provider;
        private final SqlSessionManager sessionMgr;

        public MyBatisTxResourceMethodDispatchProvider(ResourceMethodDispatchProvider provider, SqlSessionManager sessionMgr) {
            this.provider = provider;
            this.sessionMgr = sessionMgr;
        }

        @Override
        public RequestDispatcher create(AbstractResourceMethod method) {
            RequestDispatcher dispatcher = provider.create(method);
            if (dispatcher == null) {
                return null;
            }
            if (isTransactional(method)) {
                dispatcher = new MyBatisTxRequestDispatcher(dispatcher, sessionMgr);
            }
            return dispatcher;
        }

        protected static boolean isTransactional(AbstractResourceMethod method) {
            return method.getResource().isAnnotationPresent(MyBatisTx.class)
                   || method.isAnnotationPresent(MyBatisTx.class);
        }
    }

    private static class MyBatisTxRequestDispatcher implements RequestDispatcher {
        private final RequestDispatcher underlying;
        private final SqlSessionManager sessionMgr;

        private MyBatisTxRequestDispatcher(RequestDispatcher underlying, SqlSessionManager sessionMgr) {
            this.underlying = underlying;
            this.sessionMgr = sessionMgr;
        }

        @Override
        public void dispatch(Object resource, HttpContext context) {
            // sessionMgr managed sessions are not composable
            // this should be the top level transaction entry point
            // otherwise we have a misconfiguration
            if (sessionMgr.isManagedSessionStarted()) {
                throw new IllegalStateException("Managed session already started.  Cannot start new managed session.");
            }
            sessionMgr.startManagedSession();
            try {
                this.underlying.dispatch(resource, context);
                sessionMgr.commit();
            } catch (Throwable t) {
                sessionMgr.rollback();
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                } else {
                    throw new RuntimeException("Unexpected exception occurred", t);
                }
            } finally {
                sessionMgr.close();
            }
        }
    }
}
