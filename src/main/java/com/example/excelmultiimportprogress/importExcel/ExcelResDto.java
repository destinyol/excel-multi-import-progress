package com.example.excelmultiimportprogress.importExcel;

import lombok.Data;

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
}
