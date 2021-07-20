package com.example.multidatasource.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sunli
 * @date 2021/7/20
 */
@Slf4j
@Component
public class DynamicDataSource extends AbstractRoutingDataSource {

    //单例句柄
    private static DynamicDataSource instance;
    private static byte[] lock=new byte[0];
    //用于存储已实例的数据源map
    private static Map<Object,Object> dataSourceMap=new HashMap<Object, Object>();

    /**
     * 获取当前数据源
     * @return
     */
    @Override
    protected Object determineCurrentLookupKey() {
        log.info("Current DataSource is [{}]", DynamicDataSourceContextHolder.getDataSourceKey());
        return DynamicDataSourceContextHolder.getDataSourceKey();
    }

    /**
     * 设置数据源
     * @param targetDataSources
     */
    @Override
    public void setTargetDataSources(Map<Object, Object> targetDataSources) {
        super.setTargetDataSources(targetDataSources);
        dataSourceMap.putAll(targetDataSources);
        super.afterPropertiesSet();// 必须添加该句，否则新添加数据源无法识别到
    }

    /**
     * 获取存储已实例的数据源map
     * @return
     */
    public Map<Object, Object> getDataSourceMap() {
        return dataSourceMap;
    }

    /**
     * 单例方法
     * @return
     */
    public static synchronized DynamicDataSource getInstance(){
        if(instance==null){
            synchronized (lock){
                if(instance==null){
                    instance=new DynamicDataSource();
                }
            }
        }
        return instance;
    }

    /**
     * 是否存在当前key的 DataSource
     * @param key
     * @return 存在返回 true, 不存在返回 false
     */
    public static boolean isExistDataSource(String key) {
        return dataSourceMap.containsKey(key);
    }
}
