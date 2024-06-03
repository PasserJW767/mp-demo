package com.itheima.mp.domain.query;

import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mp.domain.po.User;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "分页查询实体")
public class PageQuery<T> {

    @ApiModelProperty("页码")
    private Long pageNo;
    @ApiModelProperty("每页数据条数")
    private Long pageSize;
    @ApiModelProperty("排序字段")
    private String sortBy;
    @ApiModelProperty("是否升序")
    private Boolean isAsc;

    public Page<T> toMpPage(OrderItem ... orderItems){
//        1. 设置page
//        1.1 设置Page的当前页码和大小
        Page<T> page = Page.of(pageNo, pageSize);
//        1.2 设置page的排序条件
        if (sortBy != null){
            page.addOrder(new OrderItem(sortBy, isAsc));
        } else {
            page.addOrder(orderItems);
        }
        return page;
    }

    public Page<T> toMpPageDefaultSortByUpdateTimeDesc(){
        return toMpPage(new OrderItem("update_time", false));
    }

    public Page<T> toMpPageDefaultSortByCreateTimeDesc(){
        return toMpPage(new OrderItem("create_time", false));
    }

}
