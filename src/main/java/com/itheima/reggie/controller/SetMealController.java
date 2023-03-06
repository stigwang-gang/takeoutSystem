package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetMealController {
    @Autowired
    private SetMealService setMealService;
    @Autowired
    private DishService dishService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String>save(@RequestBody SetmealDto setmealDto){
        log.info(setmealDto.toString());
        setMealService.saveWithDish(setmealDto);
        //清理某个分类下面的菜品缓存数据
        String key="dish_"+setmealDto.getCategoryId()+"_1";
        redisTemplate.delete(key);
        return R.success("新增成功");
    }

    /**
     * 套餐分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page>page(int page, int pageSize, String name){
        //分页构造器
        Page<Setmeal>pageInfo=new Page<>(page,pageSize);
        Page<SetmealDto>dtoPage=new Page<>();
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        //查询条件 name模糊查询
        queryWrapper.like(name!=null,Setmeal::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setMealService.page(pageInfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");
        List<Setmeal>records= pageInfo.getRecords();
        List<SetmealDto>list=records.stream().map((item)->
        {
            SetmealDto setmealDto=new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            //分类id
            Long categoryId = item.getCategoryId();
            //根据分类id
            Category category = categoryService.getById(categoryId);
            if(category!=null){
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());
        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }
    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String>delete(@RequestParam List<Long>ids){
        log.info("ids{}",ids);
        setMealService.removeWithDish(ids);
        return R.success("删除成功");
    }
    @PostMapping("/status/{status}")
    public R<String>status(@PathVariable Integer status,@RequestParam List<Long>ids){
        log.info("ids{}",ids);
        setMealService.upadateWithDish(status,ids);
        return R.success("修改成功");
    }

    /**
     * 根据条件分页查询
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>>list(Setmeal setmeal){
        //构造查询条件
        LambdaQueryWrapper<Setmeal> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(setmeal.getCategoryId()!=null, Setmeal::getCategoryId,setmeal.getCategoryId());
        //查询状态为1的起售菜品
        queryWrapper.eq(Setmeal::getStatus,1);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        List<Setmeal> list = setMealService.list(queryWrapper);
        return R.success(list);
    }
}
