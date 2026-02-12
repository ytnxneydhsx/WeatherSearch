package org.example.mapper;

import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

/**
 * 数据库架构管理接口 (黑科技核心：DDL 操作)
 */
public interface SchemaMapper {

    /**
     * 执行原生建表 SQL
     */
    void execUpdate(@Param("sql") String sql);

    /**
     * 查询表结构，检查列是否存在
     */
    List<Map<String, Object>> showColumns(@Param("tableName") String tableName);

    /**
     * 动态添加列
     * @param tableName 表名
     * @param columnName 新列名
     * @param type 类型 (如 VARCHAR(100))
     */
    void addColumn(@Param("tableName") String tableName, 
                   @Param("columnName") String columnName, 
                   @Param("type") String type);
}
