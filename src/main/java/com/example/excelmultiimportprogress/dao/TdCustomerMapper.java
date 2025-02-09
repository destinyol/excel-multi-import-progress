package com.example.excelmultiimportprogress.dao;

import com.example.excelmultiimportprogress.model.TdCustomer;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 示例mapper
 */
@Mapper
public interface TdCustomerMapper {

    TdCustomer getCustomerByName(String customerName);

    Integer insert(TdCustomer tdCustomer);

    Boolean saveBatch(List<TdCustomer> tdCustomerList);

}
