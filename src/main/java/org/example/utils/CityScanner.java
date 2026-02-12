package org.example.utils;

import org.example.entity.BaseCity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动扫描并实例化所有城市插槽类
 */
public class CityScanner {
    private static final Logger logger = LoggerFactory.getLogger(CityScanner.class);
    private static final String PACKAGE_PATH = "org.example.entity.city";

    public static List<BaseCity> scanCities() {
        List<BaseCity> cities = new ArrayList<>();
        try {
            String path = PACKAGE_PATH.replace('.', '/');
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL resource = classLoader.getResource(path);
            
            if (resource == null) {
                logger.error("未找到城市插槽包: {}", PACKAGE_PATH);
                return cities;
            }

            File directory = new File(resource.getFile());
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().endsWith(".class")) {
                        String className = PACKAGE_PATH + "." + file.getName().substring(0, file.getName().length() - 6);
                        Class<?> clazz = Class.forName(className);
                        
                        // 确保它是 BaseCity 的子类
                        if (BaseCity.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                            BaseCity instance = (BaseCity) clazz.getDeclaredConstructor().newInstance();
                            cities.add(instance);
                            logger.info("已成功通过插槽扫描发现城市: [{} ({})]", instance.getCityName(), instance.getCityId());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("扫描及实例化城市插槽失败: ", e);
        }
        return cities;
    }
}
