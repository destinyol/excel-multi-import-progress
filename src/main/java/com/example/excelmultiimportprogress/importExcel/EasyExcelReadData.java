package com.example.excelmultiimportprogress.importExcel;

public interface EasyExcelReadData {

    /**
     * 设置该条数据在表格中行数index
     * @param rowIndex
     */
    public void setRowIndex(Integer rowIndex);
    /**
     * 返回该条数据在表格中行数index
     */
    public Integer getRowIndex();

    /**
     * 去掉每个字段首尾空格（若不需要则方法体为空即可）
     */
    public void trimAllFields();

    /**
     * 为了排除表格空行（建议重写，空行比较容易出现）
     *      检查该行中的每个属性是否都为null或空字符串，如果是，则返回true；否则返回false
     */
    public boolean dataIsAllEmpty();

}
