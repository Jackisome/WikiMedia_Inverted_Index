package com.dengyj.mapper;

import com.dengyj.model.Title;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TitleMapper {
    @Results({
            @Result(property = "title", column = "title"),
            @Result(property = "position", column = "position"),
            @Result(property = "length", column = "length")
    })
    @Select("SELECT * FROM title WHERE title = #{title}")
    Title selectByPrimaryKey(String title);
}