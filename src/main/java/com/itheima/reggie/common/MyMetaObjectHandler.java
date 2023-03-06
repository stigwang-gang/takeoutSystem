package com.itheima.reggie.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 自定义原数据对象处理器
 */
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 如果不使用Thread方法可以选择直接注入request
     */
//    @Autowired
//    protected HttpServletRequest request;
    /**
     * 插入操作自动填充
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject){
        log.info(metaObject.toString());
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
//        Long empId=(Long) request.getSession().getAttribute("employee");

        metaObject.setValue("createUser", BaseContext.getCurrentId());
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }

    /**
     * 更新修改操作自动填充
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.info(metaObject.toString());
        long id=Thread.currentThread().getId();
        log.info("mybmeta线程id为{}",id);
        metaObject.setValue("updateTime", LocalDateTime.now());
//        Long empId=(Long) request.getSession().getAttribute("employee");
//        log.info("用户id{}",empId);
        metaObject.setValue("updateUser", BaseContext.getCurrentId());
    }
}
