package com.atguigu.r2dbc.config.converter;

import com.atguigu.r2dbc.entity.TAuthor;
import com.atguigu.r2dbc.entity.TBook;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.Instant;

/**
 * @author lfy
 * @Description
 * @create 2023-12-23 22:04
 *
 * 告诉Spring Data 怎么封装Book对象
 */
@ReadingConverter //读取数据库数据的时候,把row转成 TBook
public class BookConverter implements Converter<Row, TBook> {
    @Override
    public TBook convert(Row source) {
        if(source == null) return null;
        //自定义结果集的封装
        TBook tBook = new TBook();

        tBook.setId(source.get("id", Long.class));
        tBook.setTitle(source.get("title", String.class));

        Long author_id = source.get("author_id", Long.class);
        tBook.setAuthorId(author_id);
//        tBook.setPublishTime(source.get("publish_time", Instant.class));


        TAuthor tAuthor = new TAuthor();
        tAuthor.setId(author_id);
        tAuthor.setName(source.get("name", String.class));

        tBook.setAuthor(tAuthor);

        return null;
    }
}
