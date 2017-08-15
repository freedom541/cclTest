package com.ccl.code;

import com.ccl.jersey.codegen.SimpleMetaDataExporter;
import com.ccl.jersey.codegen.serializer.*;
import com.querydsl.sql.Configuration;
import com.querydsl.sql.SQLTemplates;
import com.querydsl.sql.types.DateTimeType;
import com.querydsl.sql.types.LocalDateType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.sql.DataSource;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

public class DomainCodeGenTest {
    ApplicationContext ctx;

    @Before
    public void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext("classpath*:META-INF/spring/*.xml");
    }

    @After
    public void tearDown() throws Exception {
        ctx = null;
    }

    @Test
    public void generateCode() throws SQLException {
        Connection connection = null;
        try {
            DataSource ds = (DataSource) ctx.getBean("dataSource");
            SimpleMetaDataExporter exporter = new SimpleMetaDataExporter();
            exporter.setBeanPrefix("E");
            exporter.setPackageName("com.ccl.querydsl.data");
            exporter.setValidationAnnotations(true);
            exporter.setExportHasManys(true);

            SimpleBeanSerializer beanSerializer = new SimpleBeanSerializer();
            beanSerializer.setAddToString(true);
            beanSerializer.setPrintSupertype(true);
            beanSerializer.setAddFullConstructor(true);
            exporter.setBeanSerializer(beanSerializer);

            SimpleModelSerializer modelSerializer = new SimpleModelSerializer();
            modelSerializer.setAddToString(true);
            modelSerializer.setPrintSupertype(true);
            exporter.setModelSerializer(modelSerializer);

            exporter.setMetadataSerializerClass(SimpleMetaDataSerializer.class);

            SimpleDaoIfcSerializer daoIfcSerializer = new SimpleDaoIfcSerializer();
            exporter.setDaoIfcSerializer(daoIfcSerializer);
            SimpleDaoImplSerializer daoImplSerializer = new SimpleDaoImplSerializer();
            exporter.setDaoImplSerializer(daoImplSerializer);

            SimpleActionFactorySerializer actionFactorySerializer = new SimpleActionFactorySerializer();
            exporter.setActionFactorySerializer(actionFactorySerializer);

            SimpleDictSerializer dictSerializer = new SimpleDictSerializer();
            exporter.setDictSerializer(dictSerializer);

            exporter.setTargetFolder(new File("src/main/java"));
            connection = ds.getConnection();
            exporter.setInnerClassesForKeys(true);
            Configuration configuration = new Configuration(SQLTemplates.DEFAULT);
            configuration.registerNumeric(10, 2, BigDecimal.class);
            configuration.register(new LocalDateType());
            configuration.register(new DateTimeType());
            exporter.setConfiguration(configuration);
//			exporter.setTableNamePattern("goods");

//            exporter.serializeDicts();

            exporter.export(connection.getMetaData(), connection.createStatement());

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            connection.close();
        }
    }

}
