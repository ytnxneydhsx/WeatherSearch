package org.example.service;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import org.apache.ibatis.session.SqlSession;
import org.example.entity.BaseCity;
import org.example.entity.Weather;
import org.example.mapper.CityWeatherMapper;
import org.example.utils.HttpUtils;
import org.example.utils.MyBatisUtils;
import org.example.utils.CityScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherService {
    private static final Logger logger = LoggerFactory.getLogger(WeatherService.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * 启动系统核心流程
     */
    public void startSystem() {
        logger.info("系统核心业务启动...");
        
        // 1. 扫描并注册城市
        List<BaseCity> registeredCities = CityScanner.scanCities();
        registerCitiesToDb(registeredCities);

        // 2. 立即执行一次数据抓取
        updateAllWeatherData(registeredCities);




        // 3. 开启定时抓取任务 (每6小时更新一次)
        scheduler.scheduleAtFixedRate(() -> updateAllWeatherData(registeredCities), 6, 6, TimeUnit.HOURS);
    }

    public void stopSystem() {
        logger.info("正在关闭系统...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }

    private void registerCitiesToDb(List<BaseCity> cities) {
        try (SqlSession session = MyBatisUtils.openSession()) {
            CityWeatherMapper mapper = session.getMapper(CityWeatherMapper.class);
            for (BaseCity city : cities) {
                mapper.insertCity(city);
            }
            session.commit();
        }
    }

    private void updateAllWeatherData(List<BaseCity> cities) {
        logger.info("开始例行抓取天气数据...");
        
        // 提前获取 Weather 类的所有合法字段名 (用于动态存入)
        java.lang.reflect.Field[] fields = Weather.class.getDeclaredFields();

        for (BaseCity city : cities) {
            try {
                JSONObject root = HttpUtils.getWeatherDataJson(city.getCityId());
                JSONArray dailyArray = root.getJSONArray("daily");

                try (SqlSession session = MyBatisUtils.openSession()) {
                    CityWeatherMapper mapper = session.getMapper(CityWeatherMapper.class);
                    for (int i = 0; i < dailyArray.size(); i++) {
                        JSONObject item = dailyArray.getJSONObject(i);
                        
                        // 【黑科技关键】：动态构建数据 Map
                        java.util.Map<String, Object> dataMap = new java.util.HashMap<>();
                        for (java.lang.reflect.Field field : fields) {
                            String fieldName = field.getName();
                            // 跳过可能存在的 id 自动维护字段 (如果有的话，这里我们只存业务字段)
                            if ("id".equals(fieldName)) continue;

                            // 1. 尝试从 JSON 中取值 (API 通常是驼峰)
                            if (item.containsKey(fieldName)) {
                                Object value = item.get(fieldName);
                                // 2. 将类变量名转为数据库下划线名
                                String colName = fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
                                dataMap.put(colName, value);
                            }
                        }
                        
                        // 3. 执行动态 Upsert
                        if (!dataMap.isEmpty()) {
                            mapper.upsertWeather(dataMap, city.getCityId());
                        }
                    }
                    session.commit();
                    logger.info("城市 [{}] 天气更新成功 (动态同步 {} 个字段)", city.getCityName(), fields.length);
                }
            } catch (Exception e) {
                logger.error("城市 [{}] 天气更新失败: {}", city.getCityName(), e.getMessage());
            }
        }
    }

    /**
     * 对外提供的查询接口
     */
    public BaseCity queryByCityName(String cityName) {
        try (SqlSession session = MyBatisUtils.openSession()) {
            CityWeatherMapper mapper = session.getMapper(CityWeatherMapper.class);
            return mapper.selectForecastByCityName(cityName);
        }
    }
}
