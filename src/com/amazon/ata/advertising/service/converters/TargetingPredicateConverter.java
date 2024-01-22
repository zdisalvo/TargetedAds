package com.amazon.ata.advertising.service.converters;

import com.amazon.ata.advertising.service.exceptions.AdvertisementServiceException;
import com.amazon.ata.advertising.service.model.TargetingGroup;
import com.amazon.ata.advertising.service.model.TargetingPredicate;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TargetingPredicateConverter implements DynamoDBTypeConverter<String, TargetingPredicate> {

    private ObjectMapper mapper;

    public TargetingPredicateConverter() {
        mapper = new ObjectMapper();
    }


    //Serialization

    public String convert(TargetingPredicate targetingPredicate) {
        if (targetingPredicate == null) {
            return "";
        }

        String jsonTargetingPredicate;

        try {
            jsonTargetingPredicate = mapper.writeValueAsString(targetingPredicate);
        } catch (JsonProcessingException e) {
            throw new AdvertisementServiceException(e.getMessage(), e);
        }

        return jsonTargetingPredicate;
    }

    //Deserialization

    public TargetingPredicate unconvert(String jsonTargetingPredicate) {


        if (jsonTargetingPredicate.isBlank()) {
            return null;
        }

        TargetingPredicate targetingPredicate;

        try {
            targetingPredicate = mapper.readValue(jsonTargetingPredicate, TargetingPredicate.class);
        } catch (IOException e) {
            throw new AdvertisementServiceException(e.getMessage(), e);
        }

        return targetingPredicate;
    }
}
