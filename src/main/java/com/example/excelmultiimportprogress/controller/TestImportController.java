package com.example.excelmultiimportprogress.controller;

import com.example.excelmultiimportprogress.dao.TdCustomerMapper;
import com.example.excelmultiimportprogress.dao.UserMapper;
import com.example.excelmultiimportprogress.importExcelFramework.ExcelImportMainTool;
import com.example.excelmultiimportprogress.importExcelFramework.ImportProgress;
import com.example.excelmultiimportprogress.importExcelImpl.CustomerImportDataHandler;
import com.example.excelmultiimportprogress.importExcelImpl.CustomerImportDto;
import com.example.excelmultiimportprogress.model.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 示例controller
 */
@RestController
@RequestMapping("/test")
public class TestImportController {

    @Autowired
    private TdCustomerMapper tdCustomerMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * excel 导入客户
     * @return
     */
    @PostMapping("/importExcel")
    public Response importExcel(MultipartFile file){
        Response response = new Response();
        response.ret(0,"成功了");
        try {
            CustomerImportDataHandler handler = new CustomerImportDataHandler(userMapper,tdCustomerMapper);
            String processKey = ExcelImportMainTool.buildImport(CustomerImportDto.class, handler, redisTemplate).runAsync(file);
            response.setData(processKey);
        } catch (Exception e) {
            e.printStackTrace();
            response.ret(111000,"出错了");
        }
        return response;
    }

    /**
     * 获取进度
     * @return
     */
    @GetMapping("/getProgress")
    public Response getProgress(String key){
        Response response = new Response();
        response.ret(0,"成功了");
        try {
            ImportProgress progress = ExcelImportMainTool.getProgress(redisTemplate, key);
            response.setData(progress);
        } catch (Exception e) {
            e.printStackTrace();
            response.ret(111001,"出错了");
        }
        return response;
    }

}
