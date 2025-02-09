package com.example.excelmultiimportprogress.importExcelFramework;

public interface EasyExcelReadData {

    /**
     * 去掉每个字段首尾空格（若不需要则方法体为空即可）
     *      （处理过程中每行会调用一次）
     */
    public void trimAllFields();

    /**
     * 为了排除表格空行（建议实现该方法，空行比较容易出现）
     *      检查该行中的每个属性是否都为null或空字符串，如果是，则返回true；否则返回false
     *      （处理过程中每行会调用一次）
     */
    public boolean dataIsAllEmpty();

}
