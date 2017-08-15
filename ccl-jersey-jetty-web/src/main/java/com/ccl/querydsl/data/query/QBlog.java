package com.ccl.querydsl.data.query;

import static com.querydsl.core.types.PathMetadataFactory.*;
import com.ccl.querydsl.data.entity.EBlog;


import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;

import com.querydsl.sql.ColumnMetadata;
import java.sql.Types;

import com.querydsl.sql.*;

import com.ccl.jersey.codegen.Label;


/**
 * QBlog is a Querydsl query type for EBlog
 */
@Label("Blog查询")
@Generated("com.ccl.jersey.codegen.serializer.SimpleMetaDataSerializer")
public class QBlog extends RelationalPathBase<EBlog> {

    private static final long serialVersionUID = 1991032566;

    public static final QBlog blog = new QBlog("blog");

    public class PrimaryKeys {

        public final PrimaryKey<EBlog> primary = createPrimaryKey(id);

    }

    public final StringPath content = createString("content");

    public final NumberPath<Integer> id = createNumber("id", Integer.class);

    public final StringPath title = createString("title");

    public final PrimaryKeys pk = new PrimaryKeys();

    public QBlog(String variable) {
        super(EBlog.class, forVariable(variable), "null", "blog");
        addMetadata();
    }

    public QBlog(String variable, String schema, String table) {
        super(EBlog.class, forVariable(variable), schema, table);
        addMetadata();
    }

    public QBlog(Path<? extends EBlog> path) {
        super(path.getType(), path.getMetadata(), "null", "blog");
        addMetadata();
    }

    public QBlog(PathMetadata metadata) {
        super(EBlog.class, metadata, "null", "blog");
        addMetadata();
    }

    public void addMetadata() {
        addMetadata(content, ColumnMetadata.named("content").withIndex(3).ofType(Types.LONGVARCHAR).withSize(16777215).notNull());
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.INTEGER).withSize(10).notNull());
        addMetadata(title, ColumnMetadata.named("title").withIndex(2).ofType(Types.VARCHAR).withSize(200).notNull());
    }

}

