package com.example.excelmultiimportprogress.importExcelFramework;

/**
 * 数据实体类抽象父类，需继承才能使用导入功能
 */
public abstract class EasyExcelReadDataAbstract implements EasyExcelReadData{

    private Integer rowIndex;

    public Integer getRowIndex() {
        return rowIndex;
    }

    public void setRowIndex(Integer rowIndex) {
        this.rowIndex = rowIndex;
    }

}
