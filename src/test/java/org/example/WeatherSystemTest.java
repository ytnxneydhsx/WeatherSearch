package org.example;

import org.apache.ibatis.session.SqlSession;
import org.example.entity.BaseCity;
import org.example.mapper.CityWeatherMapper;
import org.example.mapper.SchemaMapper;
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
public class WeatherSystemTest {

    @BeforeAll
    void setup() {
        // 关键：切换到测试数据库配置
        MyBatisUtils.setConfigResource("mybatis-config-test.xml");
    }

    @Test
    @Order(1)
    @DisplayName("测试 DDL 同步：自动建表与加列")
    void testSchemaSync() {
        // 执行同步
        SchemaSyncUtils.syncSchema();

        try (SqlSession session = MyBatisUtils.openSession()) {
            SchemaMapper mapper = session.getMapper(SchemaMapper.class);
            
            // 验证表是否存在
            List<Map<String, Object>> cityCols = mapper.showColumns("city");
            assertFalse(cityCols.isEmpty(), "city 表应该已创建");

            List<Map<String, Object>> weatherCols = mapper.showColumns("weather_forecast");
            assertFalse(weatherCols.isEmpty(), "weather_forecast 表应该已创建");
            
            // 验证是否存在我们默认的列
            boolean hasTempMax = weatherCols.stream().anyMatch(c -> c.get("Field").toString().equalsIgnoreCase("temp_max"));
            assertTrue(hasTempMax, "应该包含 temp_max 列");
        }
    }

    @Test
    @Order(2)
    @DisplayName("测试城市扫描与注册")
    void testCityScannerAndRegistration() {
        List<BaseCity> cities = CityScanner.scanCities();
        assertFalse(cities.isEmpty(), "应该能扫到北京、福州等城市");

        try (SqlSession session = MyBatisUtils.openSession()) {
            CityWeatherMapper mapper = session.getMapper(CityWeatherMapper.class);
            for (BaseCity city : cities) {
                mapper.insertCity(city);
            }
            session.commit();
            
            // 验证数据库中是否有数据
            BaseCity beijing = mapper.selectForecastByCityName("北京");
            assertNotNull(beijing, "北京应该已注册成功");
            assertEquals("101010100", beijing.getCityId());
        }
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
            assertNotNull(fuzhou.getToday());
            assertEquals(35, fuzhou.getToday().getTempMax(), "最高气温应该被覆盖更新为 35");
        }
    }
}
