package org.example;

import org.apache.ibatis.session.SqlSession;
import org.example.entity.BaseCity;
import org.example.mapper.CityWeatherMapper;
import org.example.mapper.SchemaMapper;
import org.example.service.WeatherService;
import org.example.utils.MyBatisUtils;
import org.example.utils.SchemaSyncUtils;
import org.example.utils.CityScanner;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WeatherSystemTest {

    @BeforeAll
    void setup() {
        // 1. 切换到测试数据库配置
        MyBatisUtils.setConfigResource("mybatis-config-test.xml");
        // 2. 确保数据库表是干净的且已同步
        SchemaSyncUtils.syncSchema();
    }

    @Test
    @Order(1)
    @DisplayName("测试 DDL 同步：自动建表与加列")
    void testSchemaSync() {
        try (SqlSession session = MyBatisUtils.openSession()) {
            SchemaMapper mapper = session.getMapper(SchemaMapper.class);
            
            // 验证表是否存在
            List<Map<String, Object>> cityCols = mapper.showColumns("city");
            assertFalse(cityCols.isEmpty(), "city 表应该已创建");

            List<Map<String, Object>> weatherCols = mapper.showColumns("weather_forecast");
            assertFalse(weatherCols.isEmpty(), "weather_forecast 表应该已创建");
        }
    }

    @Test
    @Order(2)
    @DisplayName("测试分页查询：6个城市分页（5+1模式）且包含天气预报")
    void testPagedForecast() {
        // 1. 准备数据：先给这 6 个城市都塞点天气
        List<BaseCity> cities = CityScanner.scanCities();
        try (SqlSession session = MyBatisUtils.openSession()) {
            CityWeatherMapper mapper = session.getMapper(CityWeatherMapper.class);
            for (BaseCity city : cities) {
                // 插入城市 (防止未注册)
                mapper.insertCity(city);
                
                // 构造一条今天的天气数据
                Map<String, Object> data = new HashMap<>();
                data.put("fx_date", LocalDate.now());
                data.put("temp_max", 25);
                data.put("temp_min", 15);
                data.put("text_day", "多云");
                mapper.upsertWeather(data, city.getCityId());
            }
            session.commit();
        }

        WeatherService service = new WeatherService();
        
        // 2. 测试第一页 (Offset 0, Limit 5)
        List<BaseCity> page1 = service.listCityForecast(0, 5);
        assertEquals(5, page1.size(), "第一页应该返回 5 个城市");
        for (BaseCity city : page1) {
            assertNotNull(city.getToday(), city.getCityName() + " 的天气预报应该已通过 Left Join 查出");
        }

        // 3. 测试第二页 (Offset 5, Limit 5)
        List<BaseCity> page2 = service.listCityForecast(5, 5);
        assertEquals(1, page2.size(), "第二页应该只剩下 1 个城市 (总共 6 个)");
        assertNotNull(page2.get(0).getToday(), "最后的城市天气也要有");
    }

    @Test
    @Order(3)
    @DisplayName("测试动态数据 Upsert 覆盖逻辑")
    void testDynamicUpsert() {
        String cityId = "101230101"; // 福州
        Map<String, Object> data = new HashMap<>();
        data.put("fx_date", LocalDate.now());
        data.put("temp_max", 30);
        data.put("temp_min", 20);
        data.put("text_day", "晴朗");

        try (SqlSession session = MyBatisUtils.openSession()) {
            CityWeatherMapper mapper = session.getMapper(CityWeatherMapper.class);
            
            // 第一次插入
            mapper.upsertWeather(data, cityId);
            session.commit();

            // 第二次更新（覆盖）
            data.put("temp_max", 35);
            mapper.upsertWeather(data, cityId);
            session.commit();

            // 验证
            BaseCity fuzhou = mapper.selectForecastByCityName("福州");
            assertNotNull(fuzhou, "福州查询结果不应为空");
            // 注意：由于 selectForecastByCityName 使用了 CURDATE() 过滤，确保上面的 LocalDate.now() 匹配
            assertNotNull(fuzhou.getToday(), "福州今日天气不应为空");
            assertEquals(35, fuzhou.getToday().getTempMax(), "最高气温应该被覆盖更新为 35");
        }
    }
}
