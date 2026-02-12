package org.example.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.entity.BaseCity;
import org.example.entity.Weather;
import java.time.LocalDate;

/**
 * 城市与天气数据联动的持久层接口
 */
public interface CityWeatherMapper {

    /**
     * 直接通过城市名查询预报（最符合用户直觉）
     * @param cityName 城市名称 (如: "福州")
     */
    BaseCity selectForecastByCityName(@Param("cityName") String cityName);



    /**
     * 插入城市基本信息 (用于系统启动时的自动注册同步)
     */
    int insertCity(BaseCity city);

    /**
     * 核心插入/更新逻辑：
     * 利用 MySQL 的 ON DUPLICATE KEY UPDATE 特性实现“动态覆盖”
     */
    int upsertWeather(@Param("data") java.util.Map<String, Object> data, @Param("cityId") String cityId);
}