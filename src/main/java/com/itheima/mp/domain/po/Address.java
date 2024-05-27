package com.itheima.mp.domain.po;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

@Data
public class Address {
    Long id;
    @TableField("user_id")
    Long userId;
    String province;
    String city;
    String town;
    String mobile;
    String street;
    String contact;
    Integer isDefault;
    String notes;
    Integer deleted;
}
