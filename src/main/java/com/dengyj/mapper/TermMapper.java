package com.dengyj.mapper;

import com.dengyj.model.Term;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TermMapper {
    @Results({
            @Result(property = "term", column = "term"),
            @Result(property = "position", column = "position"),
            @Result(property = "length", column = "length")
    })
    @Select("SELECT * FROM term WHERE term = #{term}")
    Term selectByPrimaryKey(String term);
}