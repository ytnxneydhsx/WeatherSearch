package org.example;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import java.io.IOException;
import java.io.InputStream;

public class MyBatisTestUtils {
    private static SqlSessionFactory sqlSessionFactory;

    static {
        try {
            // 使用测试专门的配置文件
            String resource = "mybatis-config-test.xml";
            InputStream inputStream = Resources.getResourceAsStream(resource);
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static SqlSession openSession() {
        return sqlSessionFactory.openSession();
    }
}
