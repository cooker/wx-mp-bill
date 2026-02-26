package com.github.cooker.bill.application.dto;

import java.util.List;

/** 分页结果封装。 */
public record PageResult<T>(List<T> items, int offset, int limit, boolean hasMore) {}
