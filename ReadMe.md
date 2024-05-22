# MyBatis-Plus Demo
## 1. MyBatis-Plus的使用
在`src/main/java/com/itheima/mp/mapper/UserMapper.java`下，使类继承`BaseMapper<User>`，就可以使用常见的一些语句

测试见`src/test/java/com/itheima/mp/MpDemoApplicationTests.java`下的：
- `testInsert`
- `testSelectById`
- `testQueryByIds`
- `testUpdateById`
- `testDeleteUser`

## 2. MyBatis-Plus常用注解
见`src/main/java/com/itheima/mp/domain/po/User.java`实体类

这里使用了：
- `@TableName()`指定表名
- `@TableId()`指定Id类型的一些属性
- `@TableField()`指定对应字段名

## 3. MyBatis-Plus条件构造器
`src/test/java/com/itheima/mp/MpDemoApplicationTests.java`下演示了复杂的条件构造器模式：
- `testQueryWrapper`
- `testUpdateByQueryWrapper`
- `testUpdateWrapper`

这里只展示了非lambda形式。推荐使用lambda形式，将column修改为：
User::getId
User::getName
User::getInfo
User::getBalance
等等等等......

这样的好处是假如在日后更新了数据库字段名，就不需要修改具体语句中的内容，只需要使用`@TableField`改一下实体类的映射就可以

## 4. 自定义Sql
`src/test/java/com/itheima/mp/MpDemoApplicationTests.java`下演示了,即`testUpdateCustomSql`

需求：更新id为 1,2,4 的⽤⼾的余额，扣200

语句为：UPDATE user SET balance = balance - 200 WHERE id in (1, 2, 4)

假设`testUpdateWrapper`是业务层，不推荐将Sql语句写在业务层，而应该写在数据持久层，也就是Mapper中

通过Wrapper自定义条件，并将参数传入到自己自定义的语句中

在自己自定义的语句中通过`@Param("ew")`来指明wrapper对象（这个是固定的！），通过其他名字来指定传入的参数

## 4.1 自定义Sql用于多表查询

见`src/test/java/com/itheima/mp/MpDemoApplicationTests.java`下的`testQueryMultiTable`

我尝试使用LambdaQueryWrapper，但是好像并不好使，在定义：
```java
LambdaQueryWrapper<User> users = new LambdaQueryWrapper<User>
        .in(User::getId, List.of(1L, 2L, 4L))
        .eq(Address::getCity, "北京") // 这里会出问题，因为上面定义了类是User，这里好像找不到这个getCity方法
```

但是可以通过`.lambda`转换：
```java
LambdaQueryWrapper<User> userLambdaQueryWrapper = new QueryWrapper<User>()
        .in("u.id", ids)
        .eq("a.city", "北京")
        .lambda();
```
