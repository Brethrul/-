package com.sky.controller.user;

import com.sky.constant.MessageConstant;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@Api("地址簿相关业务")
@RequestMapping("/user/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @PostMapping
    @ApiOperation("新增地址")
    public Result insert(@org.springframework.web.bind.annotation.RequestBody AddressBook addressBook){
        log.info("用户新增地址：{}",addressBook);
        addressBookService.insert(addressBook);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation("查询用户地址")
    public Result<List<AddressBook>> list(){
        List<AddressBook> list = addressBookService.list();
        return Result.success(list);
    }
    @GetMapping("/default")
    @ApiOperation("查询用户默认地址")
    public Result<AddressBook> defaultAddress(){
        AddressBook address = addressBookService.defaultAddress();
        return Result.success(address);
    }
    @PutMapping
    @ApiOperation("根据id修改地址")
    public Result update(@RequestBody AddressBook addressBook){
        addressBookService.update(addressBook);
        return Result.success();
    }
    @DeleteMapping
    @ApiOperation("根据id删除地址")
    public Result delete(@RequestParam Long id) {
        addressBookService.delete(id);
        return Result.success();
    }
    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getById(@PathVariable Long id){
        AddressBook address = addressBookService.getById(id);
        return Result.success(address);
    }
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result setDefault(@RequestBody AddressBook addressBook){
        addressBookService.setDefault(addressBook);
        return Result.success();
    }
}
