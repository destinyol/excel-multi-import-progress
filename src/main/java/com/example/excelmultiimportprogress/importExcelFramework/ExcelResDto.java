package com.example.excelmultiimportprogress.importExcelFramework;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 放表格导入错误行的信息（实体类）
 */
@Data
public class ExcelResDto {

    private Integer rowIndex;
    private String errorContent;

    public ExcelResDto() {
    }

    public ExcelResDto(Integer rowIndex, String errorContent) {
        this.rowIndex = rowIndex;
        this.errorContent = errorContent;
    }

    public static ExcelResDto build(EasyExcelReadDataAbstract easyExcelReadDataAbstract, String errorContent){
        return new ExcelResDto(easyExcelReadDataAbstract.getRowIndex(),errorContent);
    }
}
