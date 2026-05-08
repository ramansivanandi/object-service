package com.objectservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class DbQueryResult {

    private Long queryId;
    private String queryName;
    private String executedSql;
    private int rowCount;
    private List<Map<String, Object>> rows;
}
