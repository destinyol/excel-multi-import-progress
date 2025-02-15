package com.example.excelmultiimportprogress.importExcelImpl;

import com.example.excelmultiimportprogress.dao.TdCustomerMapper;
import com.example.excelmultiimportprogress.dao.UserMapper;
import com.example.excelmultiimportprogress.importExcelFramework.DataDealHandler;
import com.example.excelmultiimportprogress.importExcelFramework.EasyExcelReadData;
import com.example.excelmultiimportprogress.importExcelFramework.ExcelResDto;
import com.example.excelmultiimportprogress.model.TdCustomer;
import com.example.excelmultiimportprogress.model.TdUser;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.alibaba.excel.util.StringUtils.isBlank;

/**
 * 示例：客户批量导入handler
 *      （ DataDealHandler实现类 ）
 */
public class CustomerImportDataHandler implements DataDealHandler {

    private UserMapper userMapper;
    private TdCustomerMapper tdCustomerMapper;

    public CustomerImportDataHandler(UserMapper userMapper, TdCustomerMapper tdCustomerMapper) {
        this.userMapper = userMapper;
        this.tdCustomerMapper = tdCustomerMapper;
    }

    @Override
    public Integer getHeadRows() {
        return 2;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExcelResDto handleOneDataAndSave(EasyExcelReadData importData) {
        CustomerImportDto customerImportDto = (CustomerImportDto) importData;
        ExcelResDto resDto = null;
        TdUser user = userMapper.queryOneByUserName(customerImportDto.getSaleUserName());
        if (user == null){
            resDto = ExcelResDto.build(customerImportDto,"该姓名用户不存在：（"+customerImportDto.getSaleUserName()+"） ，对应客户名："+customerImportDto.getName());
            return resDto;
        }

        TdCustomer step = new TdCustomer();
        step.setName(customerImportDto.getName());
        step.setContacts(customerImportDto.getContacts());
        step.setAddress(customerImportDto.getAddress());
        step.setPhone(customerImportDto.getPhone());
        step.setCreateBy(user.getUserId());
        step.setLegalPerson(customerImportDto.getLegalPerson());
        step.setSucCode(customerImportDto.getSucCode());
        step.setRegisterMoney(customerImportDto.getRegisterMoney());

        if (isBlank(step.getName())){
            resDto = ExcelResDto.build(customerImportDto,"客户名不能为空");
            return resDto;
        }

        TdCustomer customerByName = tdCustomerMapper.getCustomerByName(customerImportDto.getName());
        if (customerByName != null) {
            resDto = ExcelResDto.build(customerImportDto,"客户名重复："+customerImportDto.getName());
            return resDto;
        }

        if (isBlank(step.getContacts())){
            resDto = ExcelResDto.build(customerImportDto,"客户联系人不能为空，客户名："+customerImportDto.getName());
            return resDto;
        }
        if (isBlank(step.getPhone())){
            resDto = ExcelResDto.build(customerImportDto,"客户联系方式不能为空，客户名："+customerImportDto.getName());
            return resDto;
        }

        boolean save = tdCustomerMapper.insert(step) != 0;
        if (!save){
            resDto = ExcelResDto.build(customerImportDto,"客户添加失败，客户名："+customerImportDto.getName());
        }

        return resDto;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<ExcelResDto> handleMultiDataAndSave(List<EasyExcelReadData> importDataList) {
        // 结果数组
        List<ExcelResDto> res = new ArrayList<>();

        List<CustomerImportDto> customerImportDtoList = new ArrayList<>();
        for (EasyExcelReadData easyExcelReadData : importDataList) {
            customerImportDtoList.add((CustomerImportDto)easyExcelReadData);
        }

        List<TdCustomer> tdCustomerList = new ArrayList<>();
        for (CustomerImportDto customerImportDto : customerImportDtoList) {
            TdCustomer step = new TdCustomer();
            step.setName(customerImportDto.getName());
            step.setContacts(customerImportDto.getContacts());
            step.setAddress(customerImportDto.getAddress());
            step.setPhone(customerImportDto.getPhone());
            step.setLegalPerson(customerImportDto.getLegalPerson());
            step.setSucCode(customerImportDto.getSucCode());
            step.setRegisterMoney(customerImportDto.getRegisterMoney());
        }

        //==========================================================
        // 省略各种业务检查，判断重复之类的
        //==========================================================

        Boolean insetCheck = tdCustomerMapper.saveBatch(tdCustomerList);
        if (!insetCheck){
            for (CustomerImportDto customerImportDto : customerImportDtoList) {
                res.add(ExcelResDto.build(customerImportDto,customerImportDto.getName()+"添加失败"));
            }
        }

        return res;
    }
}
