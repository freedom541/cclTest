package com.ccl.jersey.codegen;

import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.inject.Provider;
import javax.sql.DataSource;
import java.sql.Connection;

/**
 * {@code SpringConnectionProvider} is a Provider implementation which provides a transactionally bound connection
 *
 * <p>Usage example</p>
 * <pre>
 * {@code
 * Provider<Connection> provider = new SpringConnectionProvider(dataSource());
 * SQLQueryFactory queryFactory = SQLQueryFactory(configuration, provider);
 * }
 * </pre>
 */
public class SpringConnectionProvider implements Provider<Connection> {

    private final DataSource dataSource;

    public SpringConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection get() {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        if (!DataSourceUtils.isConnectionTransactional(connection, dataSource)) {
            throw new IllegalStateException("Connection is not transactional");
        }
        return connection;
    }

}