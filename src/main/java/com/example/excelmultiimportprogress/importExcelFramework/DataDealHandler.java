package com.example.excelmultiimportprogress.importExcelFramework;

import org.springframework.transaction.annotation.Transactional;

public interface DataDealHandler {

    /**
     * 返回模板表头行数（根据对应模板配置重写该方法）
     * @return
     */
    public Integer getHeadRows();

    /**
     * 处理单条数据（比如去重，特殊判断，或者其他自定义业务）
     *  并保存该条数据到数据库
     * @param importData EasyExcelReadData的实现类
     * @return 若返回类为null，则该条数据导入成功；若不为null，则将其放入结果集中
     */
    @Transactional(rollbackFor = Exception.class)
    public ExcelResDto handleOneDataAndSave(EasyExcelReadData importData);

}
