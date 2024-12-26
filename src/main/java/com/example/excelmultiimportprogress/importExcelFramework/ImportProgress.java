package com.example.excelmultiimportprogress.importExcelFramework;

import lombok.Data;

import java.util.List;

/**
 * 获取导入进度信息实体类
 */
@Data
public class ImportProgress {
    private String key; // 唯一标识
    private Double progress; // 进度百分比
    private Integer status; // 处理状态  0是未开始   1是处理中   2是处理完毕   3是报错了
    private List<ExcelResDto> results; // 处理结果，错误行信息
    private Object wrongCode; // 报错code

    public ImportProgress(String key, Double progress, Integer status, List<ExcelResDto> results) {
        this.key = key;
        this.progress = progress;
        this.status = status;
        this.results = results;
    }

    public ImportProgress() {
    }
}
