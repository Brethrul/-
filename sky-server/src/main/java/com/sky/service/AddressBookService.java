package com.sky.service;

import com.sky.entity.AddressBook;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AddressBookService {
    /**
     * 新增地址簿
     * @param addressBook
     */
    void insert(AddressBook addressBook);

    /**
     * 查询用户地址簿
     *
     * @return
     */
    List<AddressBook> list();

    /**
     * 查询用户默认地址
     * @return
     */
    AddressBook defaultAddress();

    /**
     * 修改用户地址数据
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 根据id删除地址
     * @param id
     */
    void delete(Long id);

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    AddressBook getById(Long id);

    /**
     * 根据id设置默认地址
     * @param id
     */
    void setDefault(AddressBook id);
}
