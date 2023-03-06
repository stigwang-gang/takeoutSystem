package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Employee;
import com.itheima.reggie.service.AddressBookService;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {
    @Autowired
    AddressBookService addressBookService;

    /**
     * 添加新地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<AddressBook>add(@RequestBody AddressBook addressBook){
        log.info(addressBook.toString());
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 显示地址
     * @param addressBook
     * @return
     */
   @GetMapping("/list")
    public R<List<AddressBook>>list(AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook:{}", addressBook);
        LambdaQueryWrapper<AddressBook>queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(addressBook!=null,AddressBook::getUserId,addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);
        return R.success(addressBookService.list(queryWrapper));
   }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<AddressBook>check(@PathVariable Long id){

        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return R.success(addressBook);
        } else {
            return R.error("没有找到该对象");
        }
    }

    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String>update(@RequestBody AddressBook addressBook){
        addressBookService.updateById(addressBook);
        return R.success("修改成功");
    }

    /**
     * 删除地址
     * @param ids
     * @return
     */
    @DeleteMapping()
    public R<String>delete(Long ids){
        AddressBook addressBook = addressBookService.getById(ids);
        addressBookService.removeById(addressBook);
        return R.success("删除成功");
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping ("default")
    public R<AddressBook>setDefault(@RequestBody AddressBook  addressBook){
        log.info("addressBook:{}",addressBook);
        LambdaUpdateWrapper<AddressBook> wrapper=new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        //将用户所有地址簿信息清零
        wrapper.set(AddressBook::getIsDefault,0);
        addressBookService.update(wrapper);
        //设置default对象
        addressBook.setIsDefault(1);
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }
    @GetMapping("/default")
    public R<AddressBook> getDefault() {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);

        //SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);

        if (null == addressBook) {
            return R.error("没有找到该对象");
        } else {
            return R.success(addressBook);
        }
    }
}
