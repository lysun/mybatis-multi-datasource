package com.example.multidatasource.service;

import com.example.multidatasource.config.DynamicDataSource;
import com.example.multidatasource.config.DynamicDataSourceContextHolder;
import com.example.multidatasource.dao.UserDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sunli
 * @date 2021/7/20
 */
@Service
@Slf4j
public class UserService {
    @Autowired
    private UserDao dao;
    @Autowired
    DynamicDataSource dynamicDataSource;

    /**
     * 事务测试
     * 注意：(1)有@Transactional注解的方法，方法内部不可以做切换数据库操作
     *      (2)在同一个service其他方法调用带@Transactional的方法，事务不起作用，（比如：在本类中使用testProcess调用process()）
     *         可以用其他service中调用带@Transactional注解的方法，或在controller中调用.

     * @return
     */
    //propagation 传播行为 isolation 隔离级别  rollbackFor 回滚规则
    @Transactional(propagation = Propagation.REQUIRED,isolation = Isolation.DEFAULT,timeout=36000,rollbackFor=Exception.class)
    public void process(int id,String name) {
        String currentKey = DynamicDataSourceContextHolder.getDataSourceKey();
        log.info("＝＝＝＝＝service当前连接的数据库是:" + currentKey);
        //return new DomainResponse<String>(1, "新增成功", "");
        dao.insert(id,name);

    }
}
