package com.example.excelmultiimportprogress.importExcelFramework;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.redis.core.RedisTemplate;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author pyf
 */
public class ExcelDataImportListener<T> extends AnalysisEventListener<T> {

    private boolean importSingleOrMulti = true; // ture是单个导入，false是批量导入

    public ExcelDataImportListener(DataDealHandler dataHandler, RedisTemplate redisTemplate, int batchInsertCount) {
        this.dataHandler = dataHandler;
        this.redisTemplate = redisTemplate;
        this.BATCH_INSERT_COUNT = batchInsertCount;
    }

    private static final int BATCH_COUNT = ExcelImportMainTool.BATCH_COUNT; // 每批处理的数据量
    private int BATCH_INSERT_COUNT; // 每批处理的数据量
    private final List<EasyExcelReadData> cachedDataList = new ArrayList<>(BATCH_COUNT);

    @Setter
    private DataDealHandler dataHandler;

    @Getter
    private final List<ExcelResDto> resDtoListRes = Collections.synchronizedList(new ArrayList<>());
    final AtomicInteger totalCount = new AtomicInteger(-1);
    final AtomicInteger currentCount = new AtomicInteger(0);

    @Setter
    private ExecutorService executorService;

    @Setter
    private RedisTemplate redisTemplate;
    @Setter
    private String processKey;

    @Override
    public void invoke(T data, AnalysisContext context) {
        if (totalCount.get()==-1){
            totalCount.set(context.readSheetHolder().getApproximateTotalRowNumber() - dataHandler.getHeadRows());
        }
        EasyExcelReadDataAbstract step = (EasyExcelReadDataAbstract) data;
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

        if (importSingleOrMulti){
            List<Future<ExcelResDto>> futures = new ArrayList<>();
            // 单个导入
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
        }else{
            @Data
            class CountAndResList{
                private Integer handleCount;
                private List<ExcelResDto> resList;
                public CountAndResList(Integer handleCount, List<ExcelResDto> resList) {
                    this.handleCount = handleCount;
                    this.resList = resList;
                }
            }
            List<Future<CountAndResList>> futures = new ArrayList<>();
            // 批量导入
            List<List<EasyExcelReadData>> lists = splitList(list,BATCH_INSERT_COUNT);

            for (List<EasyExcelReadData> dataList : lists) {
                Future<CountAndResList> future = executorService.submit(() -> {
                    List<ExcelResDto> excelResDtos = dataHandler.handleMultiDataAndSave(dataList);
                    return new CountAndResList(dataList.size(),excelResDtos);
                });
                futures.add(future);
            }
            // 等待所有任务完成
            for (Future<CountAndResList> future : futures) {
                try {
                    CountAndResList res = future.get();
                    List<ExcelResDto> resDto = res.getResList();
                    currentCount.addAndGet(res.getHandleCount());
                    if (resDto != null){
                        resDtoListRes.addAll(resDto);
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

    public void setBATCH_INSERT_COUNT(int BATCH_INSERT_COUNT) {
        if (BATCH_INSERT_COUNT < 1) throw new RuntimeException("每批次数量不能小于1");
        this.BATCH_INSERT_COUNT = BATCH_INSERT_COUNT;
    }

    public void setImportSingleOrMulti(boolean importSingleOrMulti) {
        this.importSingleOrMulti = importSingleOrMulti;
    }

    private static List<List<EasyExcelReadData>> splitList(List<EasyExcelReadData> list, int x) {
        List<List<EasyExcelReadData>> result = new ArrayList<>();
        int size = list.size();
        if (size <= x) {
            result.add(new ArrayList<>(list));
            return result;
        }
        int start = 0;
        while (start < size) {
            int end = start + x;
            if (end > size) {
                end = size;
            }
            result.add(new ArrayList<>(list.subList(start, end)));
            start += x;
        }
        return result;
    }

    private static final AtomicInteger COUNT = new AtomicInteger(0);
    public static final String IMPORT_EXCEL_REDIS_KEY = "multi_import_excel:";
}
