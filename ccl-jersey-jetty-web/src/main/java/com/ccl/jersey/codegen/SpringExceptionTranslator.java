package com.ccl.jersey.codegen;

import org.springframework.jdbc.support.SQLExceptionTranslator;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;

import java.sql.SQLException;
import java.util.List;

/**
 * {@code SpringExceptionTranslator} is an {@link SQLExceptionTranslator} implementation which uses Spring's
 * exception translation functionality internally
 * <p>
 * <p>Usage example</p>
 * <pre>
 * {@code
 * Configuration configuration = new Configuration(templates);
 * configuration.setExceptionTranslator(new SpringExceptionTranslator());
 * }
 * </pre>
 */
public class SpringExceptionTranslator implements com.querydsl.sql.SQLExceptionTranslator {

    private final SQLExceptionTranslator translator;

    public SpringExceptionTranslator() {
        this.translator = new SQLStateSQLExceptionTranslator();
    }

    public SpringExceptionTranslator(SQLExceptionTranslator translator) {
        this.translator = translator;
    }

    @Override
    public RuntimeException translate(String sql, List<Object> bindings, SQLException e) {
        return translator.translate(null, sql, e);
    }

    @Override
    public RuntimeException translate(SQLException e) {
        return translator.translate(null, null, e);
    }

}