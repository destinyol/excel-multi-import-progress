package com.example.excelmultiimportprogress.importExcelFramework;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface DataDealHandler {

    /**
     * 返回模板表头行数（根据对应模板配置实现该方法，例如表头三行：return 3;）
     * @return
     */
    public Integer getHeadRows();

    /**
     * 处理单条数据（比如去重，特殊判断，或者其他自定义业务）
     *  并自行保存该条数据到数据库
     * @param importData EasyExcelReadDataAbstract的子类
     * @return 返回结果内容（显示给前端，表格行号+错误内容）。若返回类为null，则该条数据导入成功，不用显示；若不为null，则将其放入错误结果集中
     */
    @Transactional(rollbackFor = Exception.class)
    default public ExcelResDto handleOneDataAndSave(EasyExcelReadData importData){
        throw new RuntimeException("未实现逐条插入数据处理函数handleOneDataAndSave");
    }

    /**
     * 批量处理数据（比如去重，特殊判断，或者其他自定义业务）
     *  并自行保存数据到数据库，若抛出错误则这一批全部回滚
     * @param importDataList EasyExcelReadDataAbstract的子类list
     * @return 返回结果内容数组（显示给前端，表格行号+错误内容）。若数组长度为0，则全部成功；如果长度大于0，则将其放入错误结果集中
     */
    @Transactional(rollbackFor = Exception.class)
    default public List<ExcelResDto> handleMultiDataAndSave(List<EasyExcelReadData> importDataList){
        throw new RuntimeException("未实现批量插入数据处理函数handleMultiDataAndSave");
    }

}
