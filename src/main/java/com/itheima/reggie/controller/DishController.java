package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.IntroductionAwareMethodMatcher;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;
    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        dishService.saveWithFlavor(dishDto);
        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page>page(int page,int pageSize,String name){
        //构造分页构造器
        Page<Dish>pageinfo=new Page<>(page,pageSize);
        Page<DishDto>dishDtoPage=new Page<>();
        //条件构造器
        LambdaQueryWrapper<Dish>queryWrapper=new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name!=null,Dish::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        dishService.page(pageinfo,queryWrapper);
        //对象拷贝
        BeanUtils.copyProperties(pageinfo,dishDtoPage,"records");
        List<Dish> record =pageinfo.getRecords();

        List<DishDto>list=record.stream().map((item)->{
           DishDto dishDto=new DishDto();
           BeanUtils.copyProperties(item,dishDto);

           Long categoryId=item.getCategoryId();//分类id
            //根据id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category!=null){
            String categoryName = category.getName();
            dishDto.setCategoryName(categoryName);}
            return dishDto;
        }).collect(Collectors.toList());
        dishDtoPage.setRecords(list);
        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<DishDto>get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateWithFlavor(dishDto);

        //清理所有菜品的缓存数据
        Set keys = redisTemplate.keys("dish_*");
        redisTemplate.delete(keys);
        //清理某个分类下面的菜品缓存数据
        String key="dish_"+dishDto.getCategoryId()+"_1";
        redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }
   /* @GetMapping("/list")
    public R<List<Dish>>list(Dish dish){
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null, Dish::getCategoryId,dish.getCategoryId());
        //查询状态为1的起售菜品
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> list = dishService.list(queryWrapper);

        return R.success(list);
    }*/

    @GetMapping("/list")
    public R<List<DishDto>>list(Dish dish){
        List<DishDto>dtoList=null;
        //动态构造key
        String key ="dish_"+dish.getCategoryId()+"_"+dish.getStatus();//
        //从redis中获取缓存数据
        dtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果存在，直接返回，无需查询数据库
        if(dtoList !=null){
            return R.success(dtoList);
        }
        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper =new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId()!=null, Dish::getCategoryId,dish.getCategoryId());
        //查询状态为1的起售菜品
        queryWrapper.eq(Dish::getStatus,1);
        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish>list=dishService.list(queryWrapper);
        dtoList=list.stream().map((item)->{
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
//            Long categoryId=item.getCategoryId();//分类id
            //根据id查询分类对象
//            Category category = categoryService.getById(categoryId);
//            if(category!=null){
//                String categoryName = category.getName();
//                dishDto.setCategoryName(categoryName);}
            //当前菜品id
            Long id=item.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper1=new LambdaQueryWrapper<>();
            queryWrapper1.eq(DishFlavor::getDishId,id);
            //select * from fish_flavor where dish_id=?
            List<DishFlavor> dishFlaorList = dishFlavorService.list(queryWrapper1);
            dishDto.setFlavors(dishFlaorList);
            return dishDto;
        }).collect(Collectors.toList());
        //如果不存在，需要查询数据库，将查询的菜品数据缓存刀Redis
        redisTemplate.opsForValue().set(key,dtoList,60, TimeUnit.MINUTES);

        return R.success(dtoList);
    }
}
