package com.itheima.mp.utils;

import com.baomidou.mybatisplus.core.toolkit.AES;
import org.junit.jupiter.api.Test;

public class AESGenerate {
    @Test
    void generateAESPassword(){
        String randomKey = AES.generateRandomKey();
        System.out.println("randomKey = " + randomKey);

        String username = AES.encrypt("root", randomKey);
        System.out.println("username = " + username);

        String password = AES.encrypt("123456", randomKey);
        System.out.println("password = " + password);
    }
}
