package org.example;

import org.example.entity.BaseCity;
import org.example.service.WeatherService;
import org.example.utils.SchemaSyncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("====================================");
        logger.info("   全自动天气搜寻与监控系统 启动中");
        logger.info("====================================");

        // 1. [黑科技环节] 自动架构同步 (建表、加列)
        SchemaSyncUtils.syncSchema();

        // 2. 启动核心业务服务
        WeatherService weatherService = new WeatherService();
        weatherService.startSystem();

        // 3. 用户模拟交互区
        System.out.println("\n--- 系统已就绪，请输入城市名称进行查询 (输入 'exit' 退出) ---");
        Scanner console = new Scanner(System.in);
        while (true) {
            System.out.print("请输入查询城市: ");
            String input = console.nextLine();
            if ("exit".equalsIgnoreCase(input)) break;

            BaseCity result = weatherService.queryByCityName(input);
            if (result != null) {
                System.out.println("\n查询结果如下：");
                System.out.println("城市：" + result.getCityName() + " (" + result.getCityId() + ")");
                System.out.println("今天天气：" + result.getToday());
                System.out.println("明天天气：" + result.getTomorrow());
                System.out.println("后天天气：" + result.getDayAfterTomorrow());
                System.out.println("------------------------------------");
            } else {
                System.out.println("未找到该城市的天气数据，请确认城市是否已在系统注册。");
            }
        }

        System.out.println("感谢使用，系统正在退出...");
        weatherService.stopSystem();
        System.exit(0);
    }
}
