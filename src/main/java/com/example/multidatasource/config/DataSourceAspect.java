package com.example.multidatasource.config;


import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@Slf4j
@Order(-1)
public class DataSourceAspect {
    //私有库数据源key
    private static String prefix = "shard";

    @Autowired
    private Environment env;

    @Autowired
    DynamicDataSource dynamicDataSource;

    // 根据实际情况定义需要在哪些方法前改数据库
    @Pointcut("execution(* com.example.multidatasource.service.*.*(..))")
    public void dataSourcePoint() {
    }

    @Before("dataSourcePoint()")
    public void before(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            toDB(Integer.valueOf(args[0].toString()));
            //获取当前连接的数据源对象的key
            String currentKey = DynamicDataSourceContextHolder.getDataSourceKey();
            log.info("＝＝＝＝＝当前连接的数据库是:" + currentKey);
        }
    }

    /**
     * 创建新的私有库数据源
     *
     * @param id
     */
    private void toDB(int id) {
        if (id < 1 || id > 100)
            throw new IllegalArgumentException("id值非法");
        //组合私有库数据源对象key
        String dbSourceKey = prefix + id;
        //获取当前连接的数据源对象的key
        String currentKey = DynamicDataSourceContextHolder.getDataSourceKey();
        if (dbSourceKey == currentKey) return;

        if (!DynamicDataSource.getInstance().isExistDataSource(dbSourceKey)) {
            createDataSource(env.getProperty("db.url"),
                    env.getProperty("db.port"),
                    dbSourceKey,
                    env.getProperty("db.username"),
                    env.getProperty("db.password")
            );
        }

        //切换到当前数据源
        DynamicDataSourceContextHolder.setDataSourceKey(dbSourceKey);
        log.info("＝＝＝＝＝私有库: " + id + ",切换完毕");
    }

    /**
     * 创建私有库数据源，并将数据源赋值到targetDataSources中，供后切库用
     *
     * @return
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
        //将创建的数据源，新增到targetDataSources中
        Map<Object, Object> map = new HashMap<>();
        map.put(dbname, dataSource);
        DynamicDataSource.getInstance().setTargetDataSources(map);
        return dataSource;
    }
}
