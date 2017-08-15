package com.ccl.jersey.codegen;

import com.querydsl.core.QueryMetadata;
import com.querydsl.core.support.QueryBase;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.SubQueryExpression;
import com.querydsl.sql.*;
import com.querydsl.sql.dml.SQLInsertBatch;
import com.querydsl.sql.dml.SQLMergeBatch;
import com.querydsl.sql.dml.SQLUpdateBatch;
import com.querydsl.sql.types.DateTimeType;
import com.querydsl.sql.types.LocalDateType;
import com.querydsl.sql.types.LocalTimeType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.MarkerFactory;

import javax.inject.Provider;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 领域查询对象创建工厂
 *
 * @author ccl
 */
public final class QueryDslConfig {
    final Logger logger = LoggerFactory
            .getLogger(Repository.class);
    private final DataSource dataSource;
    private final Configuration configuration;

    private SQLQueryFactory sqlQueryFactory;

    public QueryDslConfig(DataSource dataSource) {
        this.dataSource = dataSource;
        String databaseType = new PlatformUtils().determineDatabaseType(dataSource);
        SQLTemplates templates;
        switch (databaseType) {
            case "Oracle":
                templates = new OracleTemplates();
                break;
            case "PostgreSql":
                templates = new PostgreSQLTemplates();
                break;
            default:
                templates = new MySQLTemplates();
                break;
        }

        configuration = new Configuration(templates);
        configuration.setExceptionTranslator(new SpringExceptionTranslator());
        configuration.register(new DateTimeType());
        configuration.register(new LocalDateType());
        configuration.register(new LocalTimeType());
        Provider<Connection> provider = new SpringConnectionProvider(dataSource);
        sqlQueryFactory = new SQLQueryFactory(configuration, provider);
        configuration.addListener(new SQLListener() {
            @Override
            public void notifyQuery(QueryMetadata md) {
                printSQL();
            }

            @Override
            public void notifyDelete(RelationalPath<?> entity, QueryMetadata md) {
                printSQL();
            }

            @Override
            public void notifyDeletes(RelationalPath<?> entity, List<QueryMetadata> batches) {
                printSQL();
            }

            @Override
            public void notifyMerge(RelationalPath<?> entity, QueryMetadata md, List<Path<?>> keys, List<Path<?>> columns, List<Expression<?>> values, SubQueryExpression<?> subQuery) {
                printSQL();
            }

            @Override
            public void notifyMerges(RelationalPath<?> entity, QueryMetadata md, List<SQLMergeBatch> batches) {
                printSQL();
            }

            @Override
            public void notifyInsert(RelationalPath<?> entity, QueryMetadata md, List<Path<?>> columns, List<Expression<?>> values, SubQueryExpression<?> subQuery) {
                printSQL();
            }

            @Override
            public void notifyInserts(RelationalPath<?> entity, QueryMetadata md, List<SQLInsertBatch> batches) {
                printSQL();
            }

            @Override
            public void notifyUpdate(RelationalPath<?> relationalPath, QueryMetadata queryMetadata, Map<Path<?>, Expression<?>> map) {
                printSQL();
            }

            @Override
            public void notifyUpdates(RelationalPath<?> entity, List<SQLUpdateBatch> batches) {
                printSQL();
            }
        });
    }

    private void printSQL() {
        String sql = MDC.get(QueryBase.MDC_QUERY);
        String params = MDC.get(QueryBase.MDC_PARAMETERS);
        if (Objects.nonNull(params)) {
            params = params.substring(1, params.length() - 1);
            String[] split = params.split(",");
            sql = sql.replace("?", "%s");
            Object[] objs = new Object[split.length];
            for (int i = 0; i < split.length; i++) {
                String cs = split[i].trim();
                if (StringUtils.isNumeric(cs) || "true".equals(cs) || "false".equals(cs)) {
                    objs[i] = cs;
                } else if (cs.startsWith("com.querydsl.sql.types.Null")) {
                    objs[i] = "''";
                } else {
                    objs[i] = "'" + cs + "'";
                }
            }
            sql = String.format(sql, objs);
        }

        logger.debug(MarkerFactory.getMarker(LogMarker.DATABASE), "=====> Perform database operation : " + sql);
    }


    public SQLQueryFactory getSqlQueryFactory() {
        return sqlQueryFactory;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
