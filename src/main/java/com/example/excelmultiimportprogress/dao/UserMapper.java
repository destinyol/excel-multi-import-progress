package com.example.excelmultiimportprogress.dao;

import com.example.excelmultiimportprogress.model.TdUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 示例mapper
 */
@Mapper
public interface UserMapper {
    TdUser queryOneByUserName(String userName);
}
