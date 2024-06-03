package com.itheima.mp.domain.dto;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.mp.domain.vo.UserVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "分页结果")
public class PageDTO<V> {
    @ApiModelProperty("总条数")
    private Long total;
    @ApiModelProperty("总页数")
    private Long pages;
    @ApiModelProperty("集合")
    private List<V> list;

    public static <V, P> PageDTO<V> change(Page<P> p, Class<V> voClass){

        if (CollUtil.isEmpty(p.getRecords())){
            return new PageDTO<>(p.getTotal(), p.getPages(), Collections.emptyList());
        }

        List<V> ts = BeanUtil.copyToList(p.getRecords(), voClass);
        return new PageDTO<>(p.getTotal(), p.getPages(), ts);
    }

    public static <V, P> PageDTO<V> change(Page<P> p, Function<P, V> convertor){

        if (CollUtil.isEmpty(p.getRecords())){
            return new PageDTO<>(p.getTotal(), p.getPages(), Collections.emptyList());
        }

        List<V> collect = p.getRecords().stream().map(convertor).collect(Collectors.toList());
        return new PageDTO<>(p.getTotal(), p.getPages(), collect);
    }
}
