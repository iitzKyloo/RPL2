package simka.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class DbConfig {
    private static final JdbcTemplate JDBC;

    static {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUrl("jdbc:mysql://localhost:3306/simka?useSSL=false&serverTimezone=Asia/Jakarta");
        ds.setUsername("root");
        ds.setPassword("");
        JDBC = new JdbcTemplate(ds);
    }

    public static JdbcTemplate jdbc() { return JDBC; }
}