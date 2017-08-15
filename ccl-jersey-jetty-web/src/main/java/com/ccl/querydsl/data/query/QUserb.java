package com.ccl.querydsl.data.query;

import static com.querydsl.core.types.PathMetadataFactory.*;
import com.ccl.querydsl.data.entity.EUserb;


import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import com.querydsl.sql.*;

import com.ccl.jersey.codegen.Label;


/**
 * QUserb is a Querydsl query type for EUserb
 */
@Label("Userb查询")
@Generated("com.ccl.jersey.codegen.serializer.SimpleMetaDataSerializer")
public class QUserb extends RelationalPathBase<EUserb> {

    private static final long serialVersionUID = 1610213667;

    public static final QUserb userb = new QUserb("userb");

    public class PrimaryKeys {

        public final PrimaryKey<EUserb> primary = createPrimaryKey(id);

    }

    public final StringPath addr = createString("addr");

    public final DateTimePath<org.joda.time.DateTime> createTime = createDateTime("createTime", org.joda.time.DateTime.class);

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath userId = createString("userId");

    public final PrimaryKeys pk = new PrimaryKeys();

    public QUserb(String variable) {
        super(EUserb.class, forVariable(variable), "null", "userb");
        addMetadata();
    }

    public QUserb(String variable, String schema, String table) {
        super(EUserb.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QUserb(Path<? extends EUserb> path) {
        super(path.getType(), path.getMetadata(), "null", "userb");
        addMetadata();
    }

    public QUserb(PathMetadata metadata) {
        super(EUserb.class, metadata, "null", "userb");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(addr, ColumnMetadata.named("addr").withIndex(3).ofType(Types.VARCHAR).withSize(128));
        addMetadata(createTime, ColumnMetadata.named("create_time").withIndex(4).ofType(Types.TIMESTAMP).withSize(19));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(userId, ColumnMetadata.named("userId").withIndex(2).ofType(Types.VARCHAR).withSize(64));
    }

}

