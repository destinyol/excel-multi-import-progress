package com.example.excelmultiimportprogress.importExcelImpl;

import com.alibaba.excel.annotation.ExcelProperty;
import com.example.excelmultiimportprogress.importExcelFramework.EasyExcelReadData;
import lombok.Data;

/**
 * 示例：easyExcel导入的数据实体类
 *      （ EasyExcelReadData的实现类 ）
 */
@Data
public class CustomerImportDto implements EasyExcelReadData {

    @ExcelProperty(value = "销售")
    private String saleUserName;

    @ExcelProperty(value = "客户名称")
    private String name;

    @ExcelProperty(value = "联系人")
    private String contacts;

    @ExcelProperty(value = "联系电话")
    private String phone;

    @ExcelProperty(value = "统一社会信用代码")
    private String sucCode;

    @ExcelProperty(value = "法人")
    private String legalPerson;

    @ExcelProperty(value = "注册资本(单位万元)")
    private String registerMoney;

    @ExcelProperty(value = "客户地址")
    private String address;

    private Integer rowIndex;

    /**
     * 检查类中的每个属性是否为null或空字符串，如果是，则返回true；否则返回false
     * @return
     */
    @Override
    public boolean dataIsAllEmpty(){
        return (saleUserName == null || saleUserName.isEmpty()) &&
                (name == null || name.isEmpty()) &&
                (contacts == null || contacts.isEmpty()) &&
                (phone == null || phone.isEmpty()) &&
                (sucCode == null || sucCode.isEmpty()) &&
                (legalPerson == null || legalPerson.isEmpty()) &&
                (registerMoney == null || registerMoney.isEmpty()) &&
                (address == null || address.isEmpty());
    }

    @Override
    public void trimAllFields() {
        if (saleUserName != null){
            saleUserName = saleUserName.replaceAll("\\s+", "");
        }
        if (name != null) name = name.trim();
        if (contacts != null) contacts = contacts.trim();
        if (phone != null) phone = phone.trim();
        if (sucCode != null) sucCode = sucCode.trim();
        if (legalPerson != null) legalPerson = legalPerson.trim();
        if (registerMoney != null) registerMoney = registerMoney.trim();
        if (address != null) address = address.trim();
    }

    @Override
    public Integer getRowIndex() {
        return rowIndex;
    }

    @Override
    public void setRowIndex(Integer rowIndex) {
        this.rowIndex = rowIndex;
    }
}
