package com.example.excelmultiimportprogress.importExcelFramework;

import com.alibaba.excel.EasyExcel;
import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static com.example.excelmultiimportprogress.importExcelFramework.ExcelDataImportListener.generateConcurrentUUID;

/**
 *      excel带进度条多线程批量导入主方法
 *
 * 使用方法：
 * 1、实现EasyExcelReadData接口和对应方法
 * 2、实现DataDealHandler接口和对应方法
 * 3、实例化handler，handler中需要进行的数据库交互，自行处理
 * 4、最后使用：ExcelImportMainTool.buildImport(EasyExcelReadData的实现类.class ,DataDealHandler的实现实例 ,redisTemplate).run(file);
 * @author pyf
 */
public class ExcelImportMainTool {

    public static final int BATCH_COUNT = 100; // 批量处理，每批从excel中读取并处理的数据行数，可根据情况自行修改

    /**
     * 导入工具的build方法
     * @param easyExcelReadDataImplementsClass EasyExcelReadData的实现类
     * @return ExcelImportMainTool实例
     */
    public static ExcelImportMainTool buildImport(Class easyExcelReadDataImplementsClass,DataDealHandler dataHandler, RedisTemplate redisTemplate){
        ExcelImportMainTool excelImport = new ExcelImportMainTool();
        excelImport.easyExcelReadDataImplementsClass = easyExcelReadDataImplementsClass;

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        ExcelDataImportListener excelDataImportListener = new ExcelDataImportListener(dataHandler,redisTemplate);
        excelDataImportListener.setExecutorService(executorService);
        excelDataImportListener.setDataHandler(dataHandler);
        excelDataImportListener.setRedisTemplate(redisTemplate);

        excelImport.setExcelDataImportListener(excelDataImportListener);
        return excelImport;
    }

    /**
     * 主要的run方法
     * @return 返回该进度的key，可返回给前端
     */
    public String runAsync(MultipartFile file) throws IOException {
        return this.runAsync(file.getInputStream());
    }

    /**
     * 获取导入进度（静态方法）
     * @param redisTemplate 导入时候的那个redisTemplate
     * @param processKey 进度key
     * @return ImportProgress
     */
    public static ImportProgress getProgress(RedisTemplate redisTemplate, String processKey){
        processKey = ExcelDataImportListener.IMPORT_EXCEL_REDIS_KEY + processKey;
        ImportProgress progress = (ImportProgress) redisTemplate.opsForValue().get(processKey);
        return progress;
    }


    /**
     * 主要的run方法
     * @return 返回该进度的key，可返回给前端
     */
    public String runAsync(InputStream inputStream){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String key = generateConcurrentUUID(ExcelDataImportListener.IMPORT_EXCEL_REDIS_KEY);
        String keyStr = ExcelDataImportListener.IMPORT_EXCEL_REDIS_KEY + key;
        this.excelDataImportListener.setProcessKey(keyStr);
        executor.submit(() -> {
            try {
                this.run(inputStream);
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        return key;
    }

    private void run(InputStream inputStream){
        EasyExcel.read(inputStream, easyExcelReadDataImplementsClass, excelDataImportListener)
                .head(easyExcelReadDataImplementsClass)
                .headRowNumber(excelDataImportListener.getHeadRows())
                .sheet().doRead();
    }

    private void run(MultipartFile file) throws IOException {
        if (file == null) return;
        run(file.getInputStream());
    }

    @Setter
    private ExcelDataImportListener excelDataImportListener;
    @Setter
    private Class easyExcelReadDataImplementsClass;

}
