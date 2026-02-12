-- 城市基本信息表
CREATE TABLE IF NOT EXISTS `city` (
    `id` VARCHAR(20) NOT NULL COMMENT '和风城市ID',
    `name` VARCHAR(100) NOT NULL COMMENT '城市中文名称',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_city_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 天气预报数据表 (核心动态表)
CREATE TABLE IF NOT EXISTS `weather_forecast` (
    `id` INT AUTO_INCREMENT PRIMARY KEY COMMENT '自增主键',
    `city_id` VARCHAR(20) NOT NULL COMMENT '关联城市ID',
    `fx_date` DATE NOT NULL COMMENT '预报日期',
    `temp_max` INT COMMENT '最高温度',
    `temp_min` INT COMMENT '最低温度',
    `text_day` VARCHAR(100) COMMENT '天气状况',
    UNIQUE KEY `uk_city_date` (`city_id`, `fx_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
