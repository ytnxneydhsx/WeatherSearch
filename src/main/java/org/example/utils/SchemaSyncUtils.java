package org.example.utils;

import org.apache.ibatis.session.SqlSession;
import org.example.mapper.SchemaMapper;
import org.example.entity.Weather;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据库架构同步工具类 (黑科技中心)
 */
public class SchemaSyncUtils {
    private static final Logger logger = LoggerFactory.getLogger(SchemaSyncUtils.class);

    /**
     * 同步入口：初始化表并检查字段同步
     */
    public static void syncSchema() {
        try (SqlSession session = MyBatisUtils.openSession()) {
            SchemaMapper mapper = session.getMapper(SchemaMapper.class);

            // 1. 初始化基础表结构 (执行 init.sql)
            initBaseTables(mapper);

            // 2. 动态同步 Weather 实体的字段到数据库
            syncWeatherColumns(mapper);

            session.commit();
        } catch (Exception e) {
            logger.error("架构同步失败: ", e);
        }
    }

    /**
     * 读取并执行 init.sql
     */
    private static void initBaseTables(SchemaMapper mapper) throws Exception {
        logger.info("正在检查并初始化基础表结构...");
        InputStream is = SchemaSyncUtils.class.getClassLoader().getResourceAsStream("sql/init.sql");
        if (is == null) return;

        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty() || line.startsWith("--")) continue;
            sb.append(line);
            if (line.trim().endsWith(";")) {
                mapper.execUpdate(sb.toString());
                sb.setLength(0);
            }
        }
    }

    /**
     * 自动加列逻辑
     */
    private static void syncWeatherColumns(SchemaMapper mapper) {
        logger.info("正在对比 Weather 实体与数据库表结构...");
        
        // A. 获取数据库现有列名 (转换为小写对比)
        List<Map<String, Object>> columns = mapper.showColumns("weather_forecast");
        Set<String> dbColumns = columns.stream()
                .map(c -> c.get("Field").toString().toLowerCase())
                .collect(Collectors.toSet());

        // B. 获取 Weather 类中的所有字段 (反射)
        Field[] fields = Weather.class.getDeclaredFields();
        for (Field field : fields) {
            String fieldName = field.getName();
            // 驼峰转下划线以匹配数据库习惯 (简单实现)
            String columnName = fieldName.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();

            // C. 发现新大陆：如果数据库没这列
            if (!dbColumns.contains(columnName)) {
                logger.warn("检测到新字段 [{}], 正在自动为数据库添加列 [{}]...", fieldName, columnName);
                String dbType = getDbType(field.getType());
                try {
                    mapper.addColumn("weather_forecast", columnName, dbType);
                    logger.info("列 [{}] 添加成功！类型: {}", columnName, dbType);
                } catch (Exception e) {
                    logger.error("添加列 [{}] 失败: {}", columnName, e.getMessage());
                }
            }
        }
    }

    /**
     * 简单的 Java 类型到 MySQL 类型的映射
     */
    private static String getDbType(Class<?> type) {
        if (type == Integer.class || type == int.class) return "INT";
        if (type == java.time.LocalDate.class) return "DATE";
        if (type == Double.class || type == double.class) return "DOUBLE";
        return "VARCHAR(100)"; // 默认字符串

    }
}
