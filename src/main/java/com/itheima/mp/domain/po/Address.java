package com.itheima.mp.domain.po;

import lombok.Data;

@Data
public class Address {
    Long id;
    Long user_id;
    String province;
    String city;
    String town;
    String mobile;
    String street;
    String contact;
    Integer is_default;
    String notes;
    Integer deleted;
}
