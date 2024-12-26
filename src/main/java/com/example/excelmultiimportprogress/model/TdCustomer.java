package com.example.excelmultiimportprogress.model;

import lombok.Data;

import java.util.Date;

/**
 * 示例实体类
 */
@Data
public class TdCustomer {
    private Integer id;
    private String name;
    private String contacts;
    private String phone;
    private String sucCode;
    private String legalPerson;
    private String registerMoney;
    private String address;
    private Date createTime;
    private Date updateTime;
    private String createBy;
    private String updateBy;
}
