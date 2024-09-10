package com.sky.mapper;

import com.sky.entity.AddressBook;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AddressBookMapper {
    /**
     * 根据用户id查询所有地址
     * @param userId
     * @return
     */
    @Select("select * from address_book where user_id = #{userId}")
    List<AddressBook> list(Long userId);

    /**
     * 根据用户id查询默认地址
     * @param userId
     * @param isDefault
     * @return
     */
    @Select("select * from address_book where user_id = #{userId} and is_default = ${isDefault}")
    AddressBook defaultAddress(Long userId, Integer isDefault);

    /**
     * 修改地址
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @Select("select * from address_book where id = #{id}")
    AddressBook getById(Long id);

    /**
     * 删除地址
     * @param id
     */
    @Delete("delete from address_book where id = #{id}")
    void delete(Long id);

    /**
     * 插入新地址
     * @param addressBook
     */
    void insert(AddressBook addressBook);
    @Select("select count(*) from address_book where user_id = #{userId}")
    Integer countByUserId(Long userId);
}
