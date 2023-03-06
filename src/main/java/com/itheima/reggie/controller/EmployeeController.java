package com.itheima.reggie.controller;

import com.alibaba.druid.sql.visitor.functions.Substring;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    /**
     * 员工登录
     * @param request
     * @param employee
     * @return
     */
    @PostMapping("/login")
    public R<Employee>login(HttpServletRequest request, @RequestBody Employee employee){
        //密码md5加密
        String password = employee.getPassword();
        password= DigestUtils.md5DigestAsHex(password.getBytes());
        //查数据库
       LambdaQueryWrapper<Employee>queryWapper=new LambdaQueryWrapper<>();
       queryWapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWapper);
        //判断是否查到
        if (emp==null){
            return R.error("fail");
        }
        //密码比对
        if(!emp.getPassword().equals(password))
        {
            return R.error("fail");
        }
        //查看员工状态
        if(emp.getStatus()==0){
            return R.error("count fail");
        }
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    /**
     * 员工退出
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public  R<String>logout(HttpServletRequest request){
        //清理session当前保存的登录员工id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功");
    }

    /**
     * 新增员工
     * @param employee
     * @return
     */
    @PostMapping
    public R<String>save(HttpServletRequest request,   @RequestBody Employee employee){
        log.info("add information:{}",employee.toString());
        String id= employee.getIdNumber();
        id= id.substring(12);
        employee.setPassword(DigestUtils.md5DigestAsHex(id.getBytes()));

//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
        //获取当前登录用户id
//        Long empId=(Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(empId);
//        employee.setUpdateUser(empId);
        employeeService.save(employee);
        return R.success("新增员工成功");
    }

    /**
     * 员工信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page>page(int page,int pageSize,String name){
        //log.info("page ={},pagesize={},name={}",page,pageSize,name);
        //构造分页构造器
        Page pageInfo=new Page(page,pageSize);
        //构造条件构造器
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name),Employee::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询
        employeeService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }

    /**
     * 根据id修改员工信息
     * @param employee
     * @return
     */
    @PutMapping
    public  R<String>update(HttpServletRequest request,@RequestBody Employee employee){
        log.info(employee.toString());
        long id=Thread.currentThread().getId();
        log.info("upadate线程id为{}",id);
//        Long empId=(Long) request.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empId);
        employeeService.updateById(employee);
        return R.success("员工信息修改成功");
    }

    /**
     * 根据id查询员工信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<Employee>getById(@PathVariable Long id){
        log.info("select by id");
        Employee employee = employeeService.getById(id);
        if (employee!=null){
            return  R.success(employee);
        }
        return R.error("fail select");
    }




}
