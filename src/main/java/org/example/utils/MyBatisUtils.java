package org.example.utils;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import java.io.IOException;
import java.io.InputStream;

public class MyBatisUtils {
    private static SqlSessionFactory sqlSessionFactory;
    private static String configResource = "mybatis-config.xml";

    public static void setConfigResource(String resource) {
        configResource = resource;
        sqlSessionFactory = null; // 重置以降后重新初始化
    }

    private static void init() {
        if (sqlSessionFactory == null) {
            try {
                InputStream inputStream = Resources.getResourceAsStream(configResource);
                sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
            } catch (IOException e) {
                throw new RuntimeException("MyBatis 配置文件加载失败: " + configResource, e);
            }
        }
    }

    public static SqlSession openSession() {
        init();
        return sqlSessionFactory.openSession();
    }
}