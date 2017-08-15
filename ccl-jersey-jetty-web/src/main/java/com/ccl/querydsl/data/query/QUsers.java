package com.ccl.querydsl.data.query;

import static com.querydsl.core.types.PathMetadataFactory.*;
import com.ccl.querydsl.data.entity.EUsers;


import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import com.querydsl.sql.*;

import com.ccl.jersey.codegen.Label;


/**
 * QUsers is a Querydsl query type for EUsers
 */
@Label("Users查询")
@Generated("com.ccl.jersey.codegen.serializer.SimpleMetaDataSerializer")
public class QUsers extends RelationalPathBase<EUsers> {

    private static final long serialVersionUID = 1610213684;

    public static final QUsers users = new QUsers("users");

    public class PrimaryKeys {

        public final PrimaryKey<EUsers> primary = createPrimaryKey(id);

    }

    public final BooleanPath available = createBoolean("available");

    public final StringPath email = createString("email");

    public final StringPath gender = createString("gender");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath password = createString("password");

    public final StringPath qq = createString("qq");

    public final StringPath username = createString("username");

    public final PrimaryKeys pk = new PrimaryKeys();

    public QUsers(String variable) {
        super(EUsers.class, forVariable(variable), "null", "users");
        addMetadata();
    }

    public QUsers(String variable, String schema, String table) {
        super(EUsers.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QUsers(Path<? extends EUsers> path) {
        super(path.getType(), path.getMetadata(), "null", "users");
        addMetadata();
    }

    public QUsers(PathMetadata metadata) {
        super(EUsers.class, metadata, "null", "users");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(available, ColumnMetadata.named("available").withIndex(4).ofType(Types.BIT).notNull());
        addMetadata(email, ColumnMetadata.named("email").withIndex(6).ofType(Types.VARCHAR).withSize(45));
        addMetadata(gender, ColumnMetadata.named("gender").withIndex(5).ofType(Types.CHAR).withSize(2));
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(password, ColumnMetadata.named("password").withIndex(3).ofType(Types.VARCHAR).withSize(20).notNull());
        addMetadata(qq, ColumnMetadata.named("qq").withIndex(7).ofType(Types.VARCHAR).withSize(15));
        addMetadata(username, ColumnMetadata.named("username").withIndex(2).ofType(Types.VARCHAR).withSize(15).notNull());
    }

}

