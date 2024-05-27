package com.itheima.mp.service;

import com.itheima.mp.domain.po.Address;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class AddressServiceTest {

    @Autowired
    IAddressService addressService;

    @Test
    public void testDeleteAndQuery(){
        System.out.println(addressService.lambdaQuery().eq(Address::getUserId, 2L).list());
        addressService.removeById(59);
        System.out.println(addressService.lambdaQuery().eq(Address::getUserId, 2L).list());
    }


}
