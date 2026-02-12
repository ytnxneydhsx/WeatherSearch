package org.example.entity;
import java.time.LocalDate;

/**
 * 天气实体类，代表某一日的天气详情
 */
public class Weather {

    private Integer id;
    private LocalDate fxDate;   // 预报日期
    private Integer tempMax; // 最高气温
    private Integer tempMin; // 最低气温
    private String textDay;  // 白天天气状况

    public Weather() {}

    public Weather(LocalDate fxDate, Integer tempMax, Integer tempMin, String textDay) {
        this.fxDate = fxDate;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.textDay = textDay;
    }

    public LocalDate getFxDate() {
        return fxDate;
    }

    public void setFxDate(LocalDate fxDate) {
        this.fxDate = fxDate;
    }

    public Integer getTempMax() {
        return tempMax;
    }

    public void setTempMax(Integer tempMax) {
        this.tempMax = tempMax;
    }

    public Integer getTempMin() {
        return tempMin;
    }

    public void setTempMin(Integer tempMin) {
        this.tempMin = tempMin;
    }

    public String getTextDay() {
        return textDay;
    }

    public void setTextDay(String textDay) {
        this.textDay = textDay;
    }
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Weather{" +
                "fxDate='" + fxDate + '\'' +
                ", tempMax=" + tempMax +
                ", tempMin=" + tempMin +
                ", textDay='" + textDay + '\'' +
                '}';
    }
}
