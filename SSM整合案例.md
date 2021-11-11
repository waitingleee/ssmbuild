# SSM整合案例

## 环境

- IDEA
- Mysql 5.7.25
- tomcat 8.5.72
- Maven 3.3.9

## 数据库环境

```mysql
CREATE DATABASE `ssmbuild`;

USE `ssmbuild`;

DROP TABLE IF EXISTS `books`;

CREATE TABLE `books` (
`bookID` INT(10) NOT NULL AUTO_INCREMENT COMMENT '书id',
`bookName` VARCHAR(100) NOT NULL COMMENT '书名',
`bookCounts` INT(11) NOT NULL COMMENT '数量',
`detail` VARCHAR(200) NOT NULL COMMENT '描述',
KEY `bookID` (`bookID`)
) ENGINE=INNODB DEFAULT CHARSET=utf8

INSERT  INTO `books`(`bookID`,`bookName`,`bookCounts`,`detail`)VALUES
(1,'Java',1,'从入门到放弃'),
(2,'MySQL',10,'从删库到跑路'),
(3,'Linux',5,'从进门到进牢');
```

## 基本环境搭建

### 新建Maven项目 

### 添加web支持

鼠标右键项目名称 --> Add framework support --> web application

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>waiting.dope</groupId>
    <artifactId>ssmbuild</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
    </properties>

    <!--注入依赖：junit，数据库驱动，连接池，servlet，jsp，mybatis，mybatis-spring，spring-->

    <dependencies>
        <!--Junit-->
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>4.12</version>
        </dependency>
        <!--数据库驱动-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.47</version>
        </dependency>
        <!-- 数据库连接池 -->
        <dependency>
            <groupId>com.mchange</groupId>
            <artifactId>c3p0</artifactId>
            <version>0.9.5.2</version>
        </dependency>
        <!--Servlet - JSP -->
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>servlet-api</artifactId>
            <version>2.5</version>
        </dependency>
        <dependency>
            <groupId>javax.servlet.jsp</groupId>
            <artifactId>jsp-api</artifactId>
            <version>2.2</version>
        </dependency>
        <dependency>
            <groupId>javax.servlet</groupId>
            <artifactId>jstl</artifactId>
            <version>1.2</version>
        </dependency>

        <!--Mybatis-->
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis</artifactId>
            <version>3.5.2</version>
        </dependency>
        <dependency>
            <groupId>org.mybatis</groupId>
            <artifactId>mybatis-spring</artifactId>
            <version>2.0.2</version>
        </dependency>

        <!--Spring-->
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-webmvc</artifactId>
            <version>5.1.9.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>5.1.9.RELEASE</version>
        </dependency>

        <!--lombok 创建pojo类的方法-->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.4</version>
        </dependency>

    </dependencies>

    <!--静态资源导出问题，防止有些文件无法解析或导出
		filtering = false 不过滤上述资源
	-->
    <build>
        <resources>
            <resource>
                <directory>src/main/java</directory>
                <includes>
                    <include>**/*.properties</include>
                    <include>**/*.xml</include>
                </includes>
                <filtering>false</filtering>
            </resource>
            <resource>
                <directory>src/main/resources</directory>
                <includes>
                    <include>**/*.properties</include>
                    <include>**/*.xml</include>
                </includes>
                <filtering>false</filtering>
            </resource>
        </resources>
    </build>
</project>
```

### Mybatis层

#### database.properties

```properties
jdbc.driver=com.mysql.jdbc.Driver
#如果使用mysql 8.0+  增加时区配置  &serverTimezone=Aisa/Shanghai
jdbc.url=jdbc:mysql://localhost:3306/ssmbuild?useSSL=true&useUnicode=true&characterEncoding=utf8
jdbc.username=root
jdbc.password=123456
```

#### mybatis-config.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>

    <!--配置数据源，交给spring去做-->

    <!--取别名-->
    <typeAliases>
        <package name="waiting.pojo"/>
    </typeAliases>

    <mappers>
        <mapper class="waiting.dao.BookMapper"/>
    </mappers>

</configuration>
```

### Spring层

#### applicationContext.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

</beans>
```

#### spring-dao.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context https://www.springframework.org/schema/context/spring-context.xsd">


    <!--1.关联数据库配置文件-->
    <context:property-placeholder location="classpath:database.properties"/>

    <!--2.连接池
        dbcp:半自动化操作﹐不能自动连接
        c3pe:自动化操作（自动化的加载配置文件，并且可以自动设置到对象中!)druid : hikari
    -->
    <bean id="dataSource" class="com.mchange.v2.c3p0.ComboPooledDataSource">
        <property name="driverClass" value="${jdbc.driver}"></property>
        <property name="jdbcUrl" value="${jdbc.url}"></property>
        <property name="user" value="${jdbc.username}"></property>
        <property name="password" value="${jdbc.password}"></property>

        <!-- c3p0连接池的私有属性 -->
        <property name="maxPoolSize" value="30"/>
        <property name="minPoolSize" value="10"/>
        <!-- 关闭连接后不自动commit -->
        <property name="autoCommitOnClose" value="false"/>
        <!-- 获取连接超时时间 -->
        <property name="checkoutTimeout" value="10000"/>
        <!-- 当获取连接失败重试次数 -->
        <property name="acquireRetryAttempts" value="2"/>
    </bean>

    <!--3.sqlSessionFactory-->
    <bean id="sqlSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
        <property name="dataSource" ref="dataSource"/>
        <!--绑定mybatis配置文件,configLocation指定全局配置文件的位置-->
        <property name="configLocation" value="classpath:mybatis-config.xml"></property>
    </bean>

    <!--4.配置dao接口扫描包，动态实现Dao接口注入Spring容器中
        解释：https://www.cnblogs.com/jpfss/p/7799806.html
    -->
    <bean class="org.mybatis.spring.mapper.MapperScannerConfigurer">
        <!--注入sqlSessionFactory-->
        <property name="sqlSessionFactoryBeanName" value="sqlSessionFactory"/>
        <!--扫描Dao接口 扫描所有的mapper接口的实现，让mapper自动注入-->
        <property name="basePackage" value="waiting.dao"></property>
    </bean>
</beans>
```

#### spring-service.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:contert="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context https://www.springframework.org/schema/context/spring-context.xsd">

    <!--1.扫描service下的包-->
    <contert:component-scan base-package="waiting.service"/>

    <!--2.将所有业务类注入Spring，注解或配置实现-->
    <bean id="booksServiceImpl" class="waiting.service.BooksServiceImpl">
        <property name="bookMapper" ref="bookMapper"></property>
    </bean>

    <!--3.声明式 事务配置-->
    <bean id="dataSourceTransactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource"/>
    </bean>

    <!--4.Aop事务支持-->

</beans>
```

### Spring-mvc层

#### web.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">
    
    <!--DispatchServlet-->
    <!--
    springMVc，整合的时候没调用到我们的service层的bean;
        1. applicationContext.xml 没有注入bean
        2. web.xml中，我们也绑定过配置文件!
        发现问题，我们配置的是Spring-mvc.xml
        这里面确实没有service bean，所以报空指针

    -->
    <servlet>
        <servlet-name>dispatcherServlet</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:applicationContext.xml</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>dispatcherServlet</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
    
    <!--乱码过滤-->
    <filter>
        <filter-name>encodingFilter</filter-name>
        <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
        <init-param>
            <param-name>encoding</param-name>
            <param-value>UTF-8</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>encodingFilter</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>

    <!--session-->
    <session-config>
        <session-timeout>15</session-timeout>
    </session-config>
</web-app>
```

#### spring-mvc.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd
       http://www.springframework.org/schema/mvc
       http://www.springframework.org/schema/mvc/spring-mvc.xsd http://www.springframework.org/schema/context https://www.springframework.org/schema/context/spring-context.xsd">

    <!--映射器，适配器，视图解析器-->

    <!--1.注解驱动-->
    <mvc:annotation-driven/>

    <!--2.静态资源过滤-->
    <mvc:default-servlet-handler/>

    <!--3.扫描包：controller-->
    <context:component-scan base-package="waiting.controller"/>

    <!--4.视图解析器-->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/jsp/"/>
        <property name="suffix" value=".jsp"/>
    </bean>
    
</beans>
```

### Spring配置整合文件

#### applicationContext.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd">

    <import resource="classpath:spring-dao.xml"/>
    <import resource="classpath:spring-service.xml"/>
    <import resource="classpath:spring-mvc.xml"/>

</beans>
```

## 基本结构

main/src/下建立包：

- waiting.pojo
- waiting.dao
- waiting.service
- waiting.controller

### pojo层的实体类

```java
package waiting.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author WaitingL1
 * @Description
 * @create 2021-11-09 20:37
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Books {
    
    private int bookID;
    private String bookName;
    private int bookCounts;
    private String detail;
    
}
```

### Dao层的Mapper接口

```java
package waiting.dao;

import org.apache.ibatis.annotations.Param;
import waiting.pojo.Books;
import java.awt.print.Book;
import java.util.List;

/**
 * @author WaitingL1
 * @Description
 * @create 2021-11-09 20:41
 */
public interface BookMapper {

    //增加一本书
    int addBooks(Books books);

    //删除一本书
    int deleteById(@Param("bookID") int id);

    //更新一本书
    int updateBook(Books book);

    //根据id查询一本书
    Books queryBookById(@Param("bookID") int id);

    //查询全部书
    List<Books> queryAllBook();

    //查询一本书
    Books queryBookByName(@Param("bookName") String bookName);
    
}
```

### Mapper.xml配置文件

在src/main/resources目录下，逐步建立waiting包和dao包，直接建立会报错，

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="waiting.dao.BookMapper">

    <insert id="addBooks" parameterType="waiting.pojo.Books">
        insert into books(bookName,bookCounts,detail)
        values(#{bookName},#{bookCounts},#{detail})
    </insert>

    <delete id="deleteById" parameterType="int">
        delete from books where bookID = #{bookID}
    </delete>

    <update id="updateBook" parameterType="waiting.pojo.Books">
        update books
        set bookName = #{bookName},bookCounts = #{bookCounts},detail = #{detail}
        where bookID = #{bookID}
    </update>

    <select id="queryBookById" resultType="waiting.pojo.Books">
        select * from books where bookID = #{bookID}
    </select>

    <select id="queryAllBook" resultType="waiting.pojo.Books">
        select * from books
    </select>

    <select id="queryBookByName" resultType="waiting.pojo.Books">
        select * from books where bookName = #{bookName}
    </select>
</mapper>
```

### service层的接口和实现类

#### 接口

```java
package waiting.service;

import org.apache.ibatis.annotations.Param;
import waiting.pojo.Books;

import java.awt.print.Book;
import java.util.List;

/**
 * @author WaitingL1
 * @Description
 * @create 2021-11-09 21:00
 */
public interface BooksService {
    //增加一本书
    int addBooks(Books books);

    //删除一本书
    int deleteById(int id);

    //更新一本书
    int updateBook(Books book);

    //根据id查询一本书
    Books queryBookById(int id);

    //查询全部书
    List<Books> queryAllBook();

    //查询一本书
    Books queryBookByName(String bookName);

}
```

#### 实现类

```java
package waiting.service;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import waiting.dao.BookMapper;
import waiting.pojo.Books;

import java.awt.print.Book;
import java.util.List;

/**
 * @author WaitingL1
 * @Description
 * @create 2021-11-09 21:01
 */
@Service
public class BooksServiceImpl implements BooksService {

    //service层调dao层，组合dao
    @Autowired
    private BookMapper bookMapper;

    public void setBookMapper(BookMapper bookMapper) {
        this.bookMapper = bookMapper;
    }

    @Override
    public int addBooks(Books books) {
        return bookMapper.addBooks(books);
    }

    @Override
    public int deleteById(int id) {
        return bookMapper.deleteById(id);
    }

    @Override
    public int updateBook(Books books) {
        return bookMapper.updateBook(books);
    }

    @Override
    public Books queryBookById(int id) {
        return bookMapper.queryBookById(id);
    }

    @Override
    public List<Books> queryAllBook() {
        return bookMapper.queryAllBook();
    }

    @Override
    public Books queryBookByName(String bookName) {
        return bookMapper.queryBookByName(bookName);
    }
}
```

### controller层

```java
package waiting.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import waiting.pojo.Books;
import waiting.service.BooksService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author WaitingL1
 * @Description
 * @create 2021-11-10 9:59
 */
@Controller
@RequestMapping("/book")
public class BookController {


    //controller层调dao层
    @Autowired
    @Qualifier("booksServiceImpl")
    private BooksService booksService;


    //查询全部的书籍，并返回到一个书籍展示的页面
    @RequestMapping("/allBook")
    public String allBook(Model model) {
        List<Books> list = booksService.queryAllBook();
        model.addAttribute("list", list);
        return "allBook";
    }

    //跳转到书籍添加的页面
    @RequestMapping("/toAddBook")
    public String toAddPage() {
        return "addBook";
    }

    //添加书籍的请求
    @RequestMapping("/addBook")
    public String addBook(Books books) {
        System.out.println("addBook==>" + books);
        booksService.addBooks(books);
        return "redirect:/book/allBook";
    }

    //跳转到修改书籍的页面
    @RequestMapping("/toUpdateBook")
    public String toUpdatPage(int id,Model model) {
        Books books = booksService.queryBookById(id);
        model.addAttribute("queryBook",books);
        return "updateBook";
    }

    //修改书籍的页面
    @RequestMapping("/updateBook")
    public String updateBook(Books books) {
        System.out.println("updateBook ==>" + books);
        booksService.updateBook(books);
        return "redirect:/book/allBook";
    }

    //删除书籍
    @RequestMapping("/deleteBook/{bookId}")
    public String deleteBook(@PathVariable("bookId") int id){
        booksService.deleteById(id);
        return "redirect:/book/allBook";
    }

    //查询书籍
    @RequestMapping("/queryBook")
    public String queryBook(String queryBookName,Model model){
        Books books = booksService.queryBookByName(queryBookName);
        List<Books> list = new ArrayList<>();
        list.add(books);
        if(books == null){
            list = booksService.queryAllBook();
            model.addAttribute("error","未查到该书籍");
        }
        model.addAttribute("list", list);
        return "allBook";

    }

}
```

### 前端页面

#### index.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>首页</title>

    <style type="text/css">
        a {
            text-decoration: none;
            color: black;
            font-size: 18px;
        }
        h3 {
            width: 180px;
            height: 38px;
            margin: 100px auto;
            text-align: center;
            line-height: 38px;
            background: deepskyblue;
            border-radius: 4px;
        }
    </style>

</head>
<body>

<h3>
  <a href="${pageContext.request.contextPath}/book/allBook">点击进入列表页</a>
</h3>

</body>
</html>
```

#### allBook.jsp

```jsp
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>书籍列表</title>
    <%--引入 BootStrap --%>
    <link href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container">

    <div class="row clearfix">
        <div class="col-md-12 column">
            <div class="page-header">
                <h1>
                    <small>书籍列表 —— 显示所有书籍</small>
                </h1>
            </div>
        </div>
    </div>

    <div class="row">
        <div class="col-md-4 column">
            <%--toAddBook 新增书籍--%>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/book/toAddBook"> 新增书籍</a>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/book/allBook"> 显示全部书籍</a>
        </div>
        <div class="col-md-4 column"></div>
        <div class="col-md-4 column">
            <%--queryBook  查询书籍--%>
            <form class="form-inline" action="${pageContext.request.contextPath}/book/queryBook" method="post" style="float: right">
                <span style="color: red;font-weight: bold">${error}</span>
                <input type="text" name="queryBookName" class="form-control" placeholder="请输入要查询的书籍名称">
                <input type="submit" value="查询" class="btn btn-primary">
            </form>
        </div>
    </div>


    <div class="row clearfix">
        <div class="col-md-12 column">
            <table class="table table-hover table-striped">
                <thead>
                <tr>
                    <th>书籍编号</th>
                    <th>书籍名字</th>
                    <th>书籍数量</th>
                    <th>书籍详情</th>
                    <th>操作</th>
                </tr>
                </thead>

                <%--书籍由数据库查询出，从list遍历出来：foreach--%>
                <tbody><%--items="${}"--%>
                <c:forEach var="book" items="${requestScope.get('list')}">
                    <tr>
                        <td>${book.getBookID()}</td>
                        <td>${book.getBookName()}</td>
                        <td>${book.getBookCounts()}</td>
                        <td>${book.getDetail()}</td>
                        <td>
                            <a href="${pageContext.request.contextPath}/book/toUpdateBook?id=${book.getBookID()}">修改</a>
                            &nbsp; | &nbsp;
                            <a href="${pageContext.request.contextPath}/book/deleteBook/${book.getBookID()}">删除</a>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>
```

#### addBook.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>添加书籍</title>
    <link href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>

<div class="container">

    <div class="row clearfix">
        <div class="col-md-12 column">
            <div class="page-header">
                <h1>
                    <small>新增书籍</small>
                </h1>
            </div>
        </div>
    </div>
    <form action="${pageContext.request.contextPath}/book/addBook" method="post">
        书籍名称：<input type="text" name="bookName" required><br><br><br>
        书籍数量：<input type="text" name="bookCounts" required><br><br><br>
        书籍详情：<input type="text" name="detail" required><br><br><br>
        <input type="submit" value="添加">
    </form>

</div>

</body>
</html>
```

#### updateBook.jsp

```jsp
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>修改书籍</title>
    <link href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>

<div class="container">

    <div class="row clearfix">
        <div class="col-md-12 column">
            <div class="page-header">
                <h1>
                    <small>修改书籍</small>
                </h1>
            </div>
        </div>
    </div>
    <form action="${pageContext.request.contextPath}/book/updateBook" method="post">
        <%--前端传递隐藏域--%>
        <input type="hidden" name="bookID" value="${queryBook.bookID}">
        书籍名称：<input type="text" name="bookName"  value="${queryBook.bookName}" required><br><br><br>
        书籍数量：<input type="text" name="bookCounts" value="${queryBook.bookCounts}" required><br><br><br>
        书籍详情：<input type="text" name="detail" value="${queryBook.detail}" required><br><br><br>
        <input type="submit" value="修改">
    </form>
</div>

</body>
```

## 项目结构图

![image-20211111131745905](G:\md picture\SSM整合案例.assets\image-20211111131745905.png)



