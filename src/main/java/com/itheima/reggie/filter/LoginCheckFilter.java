package com.itheima.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检测用户是否已经完成登录
 */
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
@Slf4j
public class LoginCheckFilter implements Filter {

    public static final AntPathMatcher PATH_MATCHER= new AntPathMatcher();
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest)servletRequest;
        HttpServletResponse response=(HttpServletResponse)servletResponse;
        String requestURI = request.getRequestURI();
        //定义不需要处理请求路径
        String []urls=new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**",
                "/common/**",
                "/user/sendMsg",
                "/user/login"
        };
        boolean check=check(urls,requestURI);
        //放行其他
        if(check){

            chain.doFilter(request,response);
            return;
        }
        //管理员界面判断是否登录登录就放行
        if (request.getSession().getAttribute("employee")!=null){
            Long empId=(Long)request.getSession().getAttribute("employee");
            BaseContext.setCurrentId(empId);
//            log.info("线程id为{}",id);
            chain.doFilter(request,response);
            return;
        }
        //用户界面判断是否登录登录就放行
        if (request.getSession().getAttribute("user")!=null){
            Long userId=(Long)request.getSession().getAttribute("user");
            BaseContext.setCurrentId(userId);
//            log.info("线程id为{}",id);
            chain.doFilter(request,response);
            return;
        }
        //如果未登录就返回未登录结果，通过输出流方式向客户响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;

//        log.info("拦截到请求:{}",request.getRequestURL());
//        chain.doFilter(request,response);
    }
    public boolean check(String []urls,String requestURI){
        for (String url : urls) {
            boolean match=PATH_MATCHER.match(url,requestURI);
            if(match){
                return true;
            }
        }
        return false;
    }


}
