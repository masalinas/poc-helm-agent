package io.oferto.helm.config;

import java.sql.Types;

import org.hibernate.dialect.Dialect;

public class SQLiteDialect extends Dialect {
	public SQLiteDialect() {
        registerColumnType(Types.BIT, "integer");
        registerColumnType(Types.TINYINT, "tinyint");
        registerColumnType(Types.SMALLINT, "smallint");
        registerColumnType(Types.INTEGER, "integer");
    }
}
