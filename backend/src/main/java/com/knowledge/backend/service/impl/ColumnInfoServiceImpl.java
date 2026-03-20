package com.knowledge.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.knowledge.backend.entity.ColumnInfo;
import com.knowledge.backend.mapper.ColumnInfoMapper;
import com.knowledge.backend.service.ColumnInfoService;
import org.springframework.stereotype.Service;

@Service
public class ColumnInfoServiceImpl extends ServiceImpl<ColumnInfoMapper, ColumnInfo> implements ColumnInfoService {
}
