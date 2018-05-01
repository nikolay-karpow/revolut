package com.revolut.accounts.persistence;

import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

public class DataSourceFactory {
    public static DataSource createDataSource() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testDb;" +
                "DB_CLOSE_DELAY=-1;" +
                "INIT=RUNSCRIPT FROM 'classpath:schema.sql'\\;");
        dataSource.setUser("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
