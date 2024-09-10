package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.exception.AddressBookBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class AddressBookServiceImpl implements AddressBookService {
    //todo 把地址信息放在redis中

    @Autowired
    private AddressBookMapper addressBookMapper;
    @Override
    public void insert(AddressBook addressBook) {
        Integer number = addressBookMapper.countByUserId(BaseContext.getCurrentId());
        if (number >= 10) {
            throw new AddressBookBusinessException(MessageConstant.TOO_MUCH_ADDRESS);
        } else {
            if(number == 0){
                addressBook.setIsDefault(StatusConstant.ENABLE);
            }else{
                addressBook.setIsDefault(StatusConstant.DISABLE);
            }
            addressBook.setUserId(BaseContext.getCurrentId());
            addressBookMapper.insert(addressBook);
        }
    }

    @Override
    public List<AddressBook> list() {
        Long userId = BaseContext.getCurrentId();
        return addressBookMapper.list(userId);
    }

    @Override
    public AddressBook defaultAddress() {
        Long userId = BaseContext.getCurrentId();
        return addressBookMapper.defaultAddress(userId,StatusConstant.ENABLE);
    }

    @Override
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    @Override
    public void delete(Long id) {
        AddressBook address = addressBookMapper.getById(id);
        //是默认地址
        if(address.getIsDefault().equals(StatusConstant.ENABLE)){
            throw new AddressBookBusinessException(MessageConstant.DELETE_DEFAULT_ADDRESS);
        }else{
            addressBookMapper.delete(id);
        }
    }

    @Override
    public AddressBook getById(Long id) {
        return addressBookMapper.getById(id);
    }

    @Override
    public void setDefault(AddressBook addressBook) {
        //查找当前用户的默认地址
        AddressBook defaultAddress = addressBookMapper.defaultAddress(BaseContext.getCurrentId(), StatusConstant.ENABLE);

        defaultAddress.setIsDefault(StatusConstant.DISABLE);
        addressBookMapper.update(defaultAddress);

        addressBook.setIsDefault(StatusConstant.ENABLE);
        addressBookMapper.update(addressBook);
    }
}
