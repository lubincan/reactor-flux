package com.atguigu.r2dbc;

import com.atguigu.r2dbc.entity.TAuthor;
import com.atguigu.r2dbc.entity.TBook;
import com.atguigu.r2dbc.repositories.AuthorRepositories;
import com.atguigu.r2dbc.repositories.BookRepostory;
import io.asyncer.r2dbc.mysql.MySqlConnectionConfiguration;
import io.asyncer.r2dbc.mysql.MySqlConnectionFactory;
import io.r2dbc.spi.*;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.beans.Transient;
import java.io.IOException;
import java.util.Arrays;


@SpringBootTest
/**
 * @author lfy
 * @Description
 * @create 2023-12-23 20:16
 */
public class R2DBCTest {

    //最佳实践：  提升生产效率的做法
        //1、Spring Data R2DBC，基础的CRUD用 R2dbcRepository 提供好了
        //2、自定义复杂的SQL（单表）： @Query；
        //3、多表查询复杂结果集： DatabaseClient 自定义SQL及结果封装；


    //Spring Data 提供的两个核心底层组件

    @Autowired  // join查询不好做； 单表查询用
    R2dbcEntityTemplate r2dbcEntityTemplate; //CRUD API； 更多API操作示例： https://docs.spring.io/spring-data/relational/reference/r2dbc/entity-persistence.html


    @Autowired  //贴近底层，join操作好做； 复杂查询好用
    DatabaseClient databaseClient; //数据库客户端


    @Autowired
    AuthorRepositories authorRepositories;

    @Autowired
    BookRepostory bookRepostory;

    @Autowired
    R2dbcCustomConversions r2dbcCustomConversions;

    @Test
    void book() throws IOException {
//        bookRepostory.findAll()
//                .subscribe(tBook -> System.out.println("tBook = " + tBook));

//        bookRepostory.findBookAndAuthor(1L)
//                .map(book-> {
//                    Long authorId = book.getAuthorId();
//                    TAuthor block = authorRepositories.findById(authorId).block();
//                    book.setAuthor(block);
//                    return book;
//                });


        //1-1： 第一种方式
        bookRepostory.hahaBook(1L)
                .subscribe(tBook -> System.out.println("tBook = " + tBook));



        //1-1：第二种方式
//        databaseClient.sql("select b.*,t.name as name from t_book b " +
//                        "LEFT JOIN t_author t on b.author_id = t.id " +
//                        "WHERE b.id = ?")
//                .bind(0, 1L)
//                .fetch()
//                .all()
//                .map(row-> {
//                    String id = row.get("id").toString();
//                    String title = row.get("title").toString();
//                    String author_id = row.get("author_id").toString();
//                    String name = row.get("name").toString();
//                    TBook tBook = new TBook();
//
//                    tBook.setId(Long.parseLong(id));
//                    tBook.setTitle(title);
//
//                    TAuthor tAuthor = new TAuthor();
//                    tAuthor.setName(name);
//                    tAuthor.setId(Long.parseLong(author_id));
//
//                    tBook.setAuthor(tAuthor);
//
//                    return tBook;
//                })
//                .subscribe(tBook -> System.out.println("tBook = " + tBook));

        // buffer api: 实现一对N；

        //两种办法：
        //1、一次查询出来，封装好
        //2、两次查询

        // 1-N： 一个作者；可以查询到很多图书


        System.in.read();
    }

    //简单查询： 人家直接提供好接口
    //复杂条件查询：
    //    1、QBE API
    //    2、自定义方法
    //    3、自定义SQL

    @Test
    void authorRepositories() throws IOException {
//        authorRepositories.findAll()
//                .subscribe(tAuthor -> System.out.println("tAuthor = " + tAuthor));


        //statement
        // [SELECT t_author.id, t_author.name FROM t_author WHERE t_author.id IN (?, ?)
        // AND (t_author.name LIKE ?)]


        //方法起名
//        authorRepositories.findAllByIdInAndNameLike(
//                Arrays.asList(1L,2L),
//                "张%"
//        ).subscribe(tAuthor -> System.out.println("tAuthor = " + tAuthor));


        //自定义@Query注解
        authorRepositories.findHaha()
                .subscribe(tAuthor -> System.out.println("tAuthor = " + tAuthor));

        System.in.read();
    }


    @Test
    void databaseClient() throws IOException {

        // 底层操作
        databaseClient
                .sql("select * from t_author")
//                .bind(0,2L)
                .fetch() //抓取数据
                .all()//返回所有
                .map(map -> {  //map == bean  属性=值
                    System.out.println("map = " + map);
                    String id = map.get("id").toString();
                    String name = map.get("name").toString();
                    return new TAuthor(Long.parseLong(id), name);
                })
                .subscribe(tAuthor -> System.out.println("tAuthor = " + tAuthor));
        System.in.read();


    }


    @Test
    void r2dbcEntityTemplate() throws IOException {

        // Query By Criteria: QBC

        //1、Criteria构造查询条件  where id=1 and name=张三
        Criteria criteria = Criteria
                .empty()
                .and("id").is(1L)
                .and("name").is("张三");

        //2、封装为 Query 对象
        Query query = Query.query(criteria);


        r2dbcEntityTemplate
                .select(query, TAuthor.class)
                .subscribe(tAuthor -> System.out.println("tAuthor = " + tAuthor));

        System.in.read();
    }


    //思想：
    // 1、有了r2dbc，我们的应用在数据库层面天然支持高并发、高吞吐量。
    // 2、并不能提升开发效率

    @Test
    void connection() throws IOException {

        // r2dbc基于全异步、响应式、消息驱动
        // jdbc:mysql://localhost:3306/test
        // r2dbc:mysql://localhost:3306/test

        //0、MySQL配置
        MySqlConnectionConfiguration configuration = MySqlConnectionConfiguration.builder()
                .host("localhost")
                .port(3306)
                .username("root")
                .password("123456")
                .database("test")
                .build();

        //1、获取连接工厂
        MySqlConnectionFactory connectionFactory = MySqlConnectionFactory.from(configuration);


        //2、获取到连接，发送sql

        // JDBC： Statement： 封装sql的
        //3、数据发布者
        Mono.from(connectionFactory.create())
                .flatMapMany(connection ->
                        connection
                                .createStatement("select * from t_author where id=?id and name=?name")
                                .bind("id", 1L) //具名参数
                                .bind("name", "张三")
                                .execute()
                ).flatMap(result -> {
                    return result.map(readable -> {
                        Long id = readable.get("id", Long.class);
                        String name = readable.get("name", String.class);
                        return new TAuthor(id, name);
                    });
                })
                .subscribe(tAuthor -> System.out.println("tAuthor = " + tAuthor))
        ;

        //背压； 不用返回所有东西，基于请求量返回；

        System.in.read();


    }
}
