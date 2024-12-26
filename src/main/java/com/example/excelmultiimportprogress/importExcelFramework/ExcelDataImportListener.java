package com.example.excelmultiimportprogress.importExcelFramework;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author pyf
 */
public class ExcelDataImportListener<T> extends AnalysisEventListener<T> {

    public ExcelDataImportListener(DataDealHandler dataHandler, RedisTemplate redisTemplate) {
        this.dataHandler = dataHandler;
        this.redisTemplate = redisTemplate;
    }

    private static final int BATCH_COUNT = ExcelImportMainTool.BATCH_COUNT; // 每批处理的数据量
    private final List<EasyExcelReadData> cachedDataList = new ArrayList<>(BATCH_COUNT);

    @Setter
    private DataDealHandler dataHandler;

    @Getter
    private final List<ExcelResDto> resDtoListRes = Collections.synchronizedList(new ArrayList<>());
    final AtomicInteger totalCount = new AtomicInteger(-1);
    final AtomicInteger currentCount = new AtomicInteger(0);

    @Setter
    private ExecutorService executorService;
    private final List<Future<ExcelResDto>> futures = new ArrayList<>();

    @Setter
    private RedisTemplate redisTemplate;
    @Setter
    private String processKey;

    @Override
    public void invoke(T data, AnalysisContext context) {
        if (totalCount.get()==-1){
            totalCount.set(context.readSheetHolder().getApproximateTotalRowNumber() - dataHandler.getHeadRows());
        }
        EasyExcelReadData step = (EasyExcelReadData) data;
        step.setRowIndex(context.readRowHolder().getRowIndex());
        cachedDataList.add(step);
        if (cachedDataList.size() >= BATCH_COUNT) {
            saveData();
            cachedDataList.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        saveData();
        ImportProgress progressObj = new ImportProgress(processKey, ((double)currentCount.get()/(double)totalCount.get()), 2, new ArrayList<>(resDtoListRes));
        redisTemplate.opsForValue().set(processKey, progressObj);
    }

    private void saveData() {
        List<EasyExcelReadData> list = cachedDataList.stream().filter(obj -> {obj.trimAllFields();return !obj.dataIsAllEmpty();}).collect(Collectors.toList());
        for (EasyExcelReadData data : list) {
            Future<ExcelResDto> future = executorService.submit(() -> dataHandler.handleOneDataAndSave(data));
            futures.add(future);
        }
        // 等待所有任务完成
        for (Future<ExcelResDto> future : futures) {
            try {
                ExcelResDto resDto = future.get();
                currentCount.incrementAndGet();
                if (resDto != null){
                    resDtoListRes.add(resDto);
                }
                ImportProgress progressObj = new ImportProgress(processKey, ((double)currentCount.get()/(double)totalCount.get()), 1, new ArrayList<>(resDtoListRes));
                redisTemplate.opsForValue().set(processKey, progressObj);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                ImportProgress progressObj = new ImportProgress(processKey, ((double)currentCount.get()/(double)totalCount.get()), 3, new ArrayList<>(resDtoListRes));
                progressObj.setWrongCode("111000"); // 错误码，可自定义
                redisTemplate.opsForValue().set(processKey, progressObj);
            }
        }
        futures.clear();
    }

    public Integer getHeadRows(){
        return dataHandler.getHeadRows();
    }

    public static String generateConcurrentUUID(String ident) {
        String result = ident +
                System.currentTimeMillis() +
                COUNT.incrementAndGet();
        try {
            result = UUID.nameUUIDFromBytes(result.getBytes(StandardCharsets.UTF_8)).toString().replace("-", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static final AtomicInteger COUNT = new AtomicInteger(0);
    public static final String IMPORT_EXCEL_REDIS_KEY = "multi_import_excel:";
}
