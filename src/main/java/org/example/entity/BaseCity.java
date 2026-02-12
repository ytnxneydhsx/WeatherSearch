package org.example.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 城市基类：通过动态计算的方式实现“今天、明天、后天”的插槽
 */
public class BaseCity {
    private String cityId;
    private String cityName;
    
    // 真实存储从数据库查出的所有天气
    private List<Weather> forecastList = new ArrayList<>();

    public BaseCity() {}

    // --- 核心逻辑：动态从列表中寻找对应的天气 ---

    public Weather getToday() {
        return findWeatherByOffset(0);
    }

    public Weather getTomorrow() {
        return findWeatherByOffset(1);
    }

    public Weather getDayAfterTomorrow() {
        return findWeatherByOffset(2);
    }

    private Weather findWeatherByOffset(int days) {
        LocalDate targetDate = LocalDate.now().plusDays(days);
        if (forecastList == null) return null;
        for (Weather w : forecastList) {
            // 使用字符串对比作为兜底，防止某些驱动下的 LocalDate 包含隐藏的时间戳
            if (w.getFxDate() != null && w.getFxDate().toString().equals(targetDate.toString())) {
                return w;
            }
        }
        return null;
    }

    // --- 标准 Getter/Setter ---

    public List<Weather> getForecastList() {
        return forecastList;
    }

    public void setForecastList(List<Weather> forecastList) {
        this.forecastList = forecastList;
    }

    public String getCityId() { return cityId; }
    public void setCityId(String cityId) { this.cityId = cityId; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }

    @Override
    public String toString() {
        return "CityInfo{" +
                "cityId='" + cityId + '\'' +
                ", cityName='" + cityName + '\'' +
                ", \ntoday=" + getToday() +
                ", \ntomorrow=" + getTomorrow() +
                ", \ndayAfterTomorrow=" + getDayAfterTomorrow() +
                '}';
    }
}
