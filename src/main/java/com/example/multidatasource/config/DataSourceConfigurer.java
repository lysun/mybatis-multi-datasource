package com.example.multidatasource.config;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.boot.autoconfigure.MybatisProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sunli
 * @date 2021/7/20
 */
@Slf4j
@Configuration
@MapperScan(basePackages = "com.example.multidatasource.dao")
@EnableConfigurationProperties(MybatisProperties.class)
public class DataSourceConfigurer {
    @Autowired
    private Environment env;
    private MybatisProperties mybatisProperties;


    public DataSourceConfigurer(MybatisProperties properties) {
        this.mybatisProperties = properties;
    }


    @Bean
    public DynamicDataSource dynamicDataSource() {
        //获取动态数据库的实例（单例方式）
        DynamicDataSource dynamicDataSource = DynamicDataSource.getInstance();
        /*//创建默认数据源
        DruidDataSource defaultDataSource = createDataSource(env.getProperty("db.url"),
                env.getProperty("db.port"),
                env.getProperty("db.dbname"),
                env.getProperty("db.username"),
                env.getProperty("db.password")
                );*/
        Map<Object, Object> map = new HashMap<>();
//        map.put("default", defaultDataSource);
        //自定义数据源key值，将创建好的数据源对象，赋值到targetDataSources中,用于切换数据源时指定对应key即可切换
        dynamicDataSource.setTargetDataSources(map);
        //设置默认数据源
//        dynamicDataSource.setDefaultTargetDataSource(defaultDataSource);

        return dynamicDataSource;
    }

    /**
     * 创建数据源对象
     *
     * @return data source
     */
    private DruidDataSource createDataSource(String host, String port, String dbname,
                                             String username, String password) {
        //如果不指定数据库类型，则使用默认数据库连接
        DruidDataSource dataSource = new DruidDataSource();
        String dbUrl = "jdbc:mysql://" + host + ":" + port + "/" + dbname
                + "?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
        log.info("+++default默认数据库连接url = " + dbUrl);
        dataSource.setUrl(dbUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return dataSource;
    }

    /**
     * 　配置mybatis的sqlSession连接动态数据源
     *
     * @param dynamicDataSource
     * @return
     * @throws Exception
     */
    @Bean
    public SqlSessionFactory sqlSessionFactory(
            @Qualifier("dynamicDataSource") DataSource dynamicDataSource)
            throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dynamicDataSource);
        bean.setMapperLocations(mybatisProperties.resolveMapperLocations());
        bean.setTypeAliasesPackage(mybatisProperties.getTypeAliasesPackage());
        bean.setConfiguration(mybatisProperties.getConfiguration());
        return bean.getObject();
    }

    @Bean(name = "sqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(
            @Qualifier("sqlSessionFactory") SqlSessionFactory sqlSessionFactory)
            throws Exception {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * 将动态数据源添加到事务管理器中，并生成新的bean
     *
     * @return the platform transaction manager
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dynamicDataSource());
    }
}
