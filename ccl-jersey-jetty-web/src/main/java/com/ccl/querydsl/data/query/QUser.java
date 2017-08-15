package com.ccl.querydsl.data.query;

import static com.querydsl.core.types.PathMetadataFactory.*;
import com.ccl.querydsl.data.entity.EUser;


import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import com.querydsl.sql.*;

import com.ccl.jersey.codegen.Label;


/**
 * QUser is a Querydsl query type for EUser
 */
@Label("User查询")
@Generated("com.ccl.jersey.codegen.serializer.SimpleMetaDataSerializer")
public class QUser extends RelationalPathBase<EUser> {

    private static final long serialVersionUID = 1991605023;

    public static final QUser user = new QUser("user");

    public class PrimaryKeys {

        public final PrimaryKey<EUser> primary = createPrimaryKey(id);

    }

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath name = createString("name");

    public final PrimaryKeys pk = new PrimaryKeys();

    public QUser(String variable) {
        super(EUser.class, forVariable(variable), "null", "user");
        addMetadata();
    }

    public QUser(String variable, String schema, String table) {
        super(EUser.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QUser(Path<? extends EUser> path) {
        super(path.getType(), path.getMetadata(), "null", "user");
        addMetadata();
    }

    public QUser(PathMetadata metadata) {
        super(EUser.class, metadata, "null", "user");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(name, ColumnMetadata.named("name").withIndex(2).ofType(Types.VARCHAR).withSize(255));
    }

}

