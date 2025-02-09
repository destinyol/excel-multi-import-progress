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
 * 1、继承EasyExcelReadDataAbstract抽象类并实现对应方法
 * 2、实现DataDealHandler接口和对应方法
 * 3、实例化handler，handler中需要进行的数据库交互，自行处理
 * 4、最后使用：ExcelImportMainTool.buildImport(EasyExcelReadDataAbstract的子类.class ,DataDealHandler的实现实例 ,redisTemplate).run(file);
 * @author pyf
 */
public class ExcelImportMainTool {

    public static final int BATCH_COUNT = 1000; // 批量处理，每批从excel中读取并处理的数据行数，可根据情况自行修改
    public int BATCH_INSERT_COUNT = 200; // 使用批量插入时的每批次处理数量（传入handleMultiDataAndSave函数的数组大小），可根据情况自行修改，可动态设置

    /**
     * 导入工具的build方法
     * @param easyExcelReadDataImplementsClass EasyExcelReadDataAbstract的子类
     * @return ExcelImportMainTool实例
     */
    public static ExcelImportMainTool buildImport(Class easyExcelReadDataImplementsClass, DataDealHandler dataHandler, RedisTemplate redisTemplate){
        ExcelImportMainTool excelImport = new ExcelImportMainTool();
        excelImport.easyExcelReadDataImplementsClass = easyExcelReadDataImplementsClass;

        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        ExcelDataImportListener excelDataImportListener = new ExcelDataImportListener(dataHandler,redisTemplate,excelImport.BATCH_INSERT_COUNT);
        excelDataImportListener.setExecutorService(executorService);
        excelDataImportListener.setDataHandler(dataHandler);
        excelDataImportListener.setRedisTemplate(redisTemplate);

        excelImport.setExcelDataImportListener(excelDataImportListener);
        return excelImport;
    }

    /**
     * 设置批量插入时的每批次处理数量
     * @param BATCH_INSERT_COUNT
     */
    public ExcelImportMainTool setBatchInsertCount(int BATCH_INSERT_COUNT) {
        this.BATCH_INSERT_COUNT = BATCH_INSERT_COUNT;
        this.excelDataImportListener.setBATCH_INSERT_COUNT(BATCH_INSERT_COUNT);
        return this;
    }

    /**
     * 主要的run方法（单个导入）
     * @return 返回该进度的key，可返回给前端
     */
    public String runAsyncSingle(MultipartFile file) throws IOException {
        return this.runAsyncSingle(file.getInputStream());
    }

    /**
     * 主要的run方法（单个导入）
     * @return 返回该进度的key，可返回给前端
     */
    public String runAsyncSingle(InputStream inputStream){
        return start(inputStream,true);
    }

    /**
     * 主要的run方法（批量导入）
     * @return 返回该进度的key，可返回给前端
     */
    public String runAsyncMulti(MultipartFile file) throws IOException {
        return start(file.getInputStream(),false);
    }

    /**
     * 主要的run方法（批量导入）
     * @return 返回该进度的key，可返回给前端
     */
    public String runAsyncMulti(InputStream inputStream){
        return start(inputStream,false);
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



    private String start(InputStream inputStream, boolean importSingleOrMulti){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        String key = generateConcurrentUUID(ExcelDataImportListener.IMPORT_EXCEL_REDIS_KEY);
        String keyStr = ExcelDataImportListener.IMPORT_EXCEL_REDIS_KEY + key;
        this.excelDataImportListener.setProcessKey(keyStr);
        executor.submit(() -> {
            try {
                this.run(inputStream,importSingleOrMulti);
            }catch (Exception e){
                e.printStackTrace();
            }
        });
        return key;
    }

    private void run(InputStream inputStream, boolean importSingleOrMulti){
        excelDataImportListener.setImportSingleOrMulti(importSingleOrMulti);
        EasyExcel.read(inputStream, easyExcelReadDataImplementsClass, excelDataImportListener)
                .head(easyExcelReadDataImplementsClass)
                .headRowNumber(excelDataImportListener.getHeadRows())
                .sheet().doRead();
    }

    private void run(MultipartFile file, boolean importSingleOrMulti) throws IOException {
        if (file == null) return;
        run(file.getInputStream(),importSingleOrMulti);
    }

    @Setter
    private ExcelDataImportListener excelDataImportListener;
    @Setter
    private Class easyExcelReadDataImplementsClass;

}
