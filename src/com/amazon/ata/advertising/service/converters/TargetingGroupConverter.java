package com.amazon.ata.advertising.service.converters;

import com.amazon.ata.advertising.service.exceptions.AdvertisementClientException;
import com.amazon.ata.advertising.service.exceptions.AdvertisementServiceException;
import com.amazon.ata.advertising.service.model.TargetingGroup;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TargetingGroupConverter implements DynamoDBTypeConverter<String, List<TargetingGroup>> {

    private ObjectMapper mapper;

    public TargetingGroupConverter() {
        mapper = new ObjectMapper();
    }


    //Serialization

    public String convert(List<TargetingGroup> targetingGroups) {
        if (targetingGroups == null) {
            return "";
        }

        String jsonTargetingGroups;

        try {
            jsonTargetingGroups = mapper.writeValueAsString(targetingGroups);
        } catch (JsonProcessingException e) {
            throw new AdvertisementServiceException(e.getMessage(), e);
        }

        return jsonTargetingGroups;
    }

    //Deserialization

    public List<TargetingGroup> unconvert(String jsonTargetingGroups) {

        List<TargetingGroup> targetingGroups = new ArrayList<>();

        if (jsonTargetingGroups.isBlank()) {
            return targetingGroups;
        }

        try {
            targetingGroups = mapper.readValue(jsonTargetingGroups, new TypeReference<List<TargetingGroup>>(){});
        } catch (IOException e) {
            throw new AdvertisementServiceException(e.getMessage(), e);
        }

        return targetingGroups;
    }
}
