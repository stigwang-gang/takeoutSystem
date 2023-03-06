package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.DIshMapper;
import com.itheima.reggie.mapper.SetMealMapper;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetMealServiceImpl extends ServiceImpl<SetMealMapper, Setmeal>implements SetMealService {
    @Autowired
    private SetMealDishService setMealDishService;
    @Autowired
    private SetMealService setMealService;
    /**
     * 新增套餐
     * @param setmealDto
     */
    @Transactional
    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //保存套餐基本信息操作setmeal执行insert
        this.save(setmealDto);
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes.stream().map((item)->
                {
                    item.setSetmealId(setmealDto.getId());
                    return item;
                }).collect(Collectors.toList());
        //保存套餐和菜品的关联信息，操作setmeal_dish执行insert
        setMealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐同时删除套餐和菜品的关联数据
     * @param ids
     */
    @Override
    public void removeWithDish(List<Long> ids) {
        //查询套餐状态确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);
        if(count>0){
            //不能删除抛出业务异常
            throw new CustomException("套餐正在售卖不能删除");
        }
        //如果可以删除。先删除套餐表中数据
        this.removeByIds(ids);
        //删除关系表中数据
        LambdaQueryWrapper<SetmealDish> queryWrapper1=new LambdaQueryWrapper<>();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);
        setMealDishService.remove(queryWrapper1);
    }

    /**
     * 更新套餐起售情况
     * @param ids
     */
    @Transactional
    @Override
    public void upadateWithDish(Integer status,List<Long> ids) {
        //根据ids查寻得到套餐
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(ids!=null,Setmeal::getId,ids);
        List<Setmeal> list = this.list(queryWrapper);
        //Setmeal setmeal=new Setmeal();
        //setmeal.setStatus(status);
//        for (Setmeal setmeal:list)
//            if(setmeal!=null){
//            setmeal.setStatus(status);
//            this.updateById(setmeal);
//            }
        list.stream().map((item)->
        {
            item.setStatus(status);
            return item;
        }).collect(Collectors.toList());
        setMealService.updateBatchById(list);
    }

}
