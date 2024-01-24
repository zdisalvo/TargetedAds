package com.amazon.ata.advertising.service.dao;

import com.amazon.ata.advertising.service.targeting.TargetingGroup;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;

import java.util.List;
import java.util.concurrent.Callable;

public class TargetingGroupDaoExecutor implements Callable<List<TargetingGroup>> {

    private final ReadableDao<String, List<TargetingGroup>> targetingGroupDao;
    private final String contentId;

    public TargetingGroupDaoExecutor(ReadableDao<String, List<TargetingGroup>> targetingGroupDao, String contentId) {
        this.targetingGroupDao = targetingGroupDao;
        this.contentId = contentId;
    }


    @Override
    public List<TargetingGroup> call() throws Exception {
        return this.targetingGroupDao.get(contentId);
    }
}
