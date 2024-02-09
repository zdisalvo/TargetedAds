package com.amazon.ata.advertising.service.businesslogic;

import com.amazon.ata.advertising.service.dao.CustomerProfileDao;
import com.amazon.ata.advertising.service.dao.ReadableDao;
import com.amazon.ata.advertising.service.dao.TargetingGroupDaoExecutor;
import com.amazon.ata.advertising.service.model.*;
import com.amazon.ata.advertising.service.targeting.TargetingEvaluator;
import com.amazon.ata.advertising.service.targeting.TargetingGroup;

import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicate;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicateResult;
import com.amazon.ata.customerservice.CustomerProfile;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import javax.inject.Inject;

/**
 * This class is responsible for picking the advertisement to be rendered.
 */
public class AdvertisementSelectionLogic {

    private static final Logger LOG = LogManager.getLogger(AdvertisementSelectionLogic.class);

    private final ReadableDao<String, List<AdvertisementContent>> contentDao;
    private final ReadableDao<String, List<TargetingGroup>> targetingGroupDao;

    //TODO
    //private final ReadableDao<String, List<CustomerProfile>> customerProfileDao;

    //todo
    private Random random = new Random();

    /**
     * Constructor for AdvertisementSelectionLogic.
     * @param contentDao Source of advertising content.
     * @param targetingGroupDao Source of targeting groups for each advertising content.
     */
    @Inject
    public AdvertisementSelectionLogic(ReadableDao<String, List<AdvertisementContent>> contentDao,
                                       ReadableDao<String, List<TargetingGroup>> targetingGroupDao) {
        this.contentDao = contentDao;
        this.targetingGroupDao = targetingGroupDao;

    }

    /**
     * Setter for Random class.
     * @param random generates random number used to select advertisements.
     */
    public void setRandom(Random random) {
        this.random = random;
    }

    /**
     * Gets all of the content and metadata for the marketplace and determines which content can be shown.  Returns the
     * eligible content with the highest click through rate.  If no advertisement is available or eligible, returns an
     * EmptyGeneratedAdvertisement.
     *
     * @param customerId - the customer to generate a custom advertisement for
     * @param marketplaceId - the id of the marketplace the advertisement will be rendered on
     * @return an advertisement customized for the customer id provided, or an empty advertisement if one could
     *     not be generated.
     */
    public GeneratedAdvertisement selectAdvertisement(String customerId, String marketplaceId) throws Exception {
//        GeneratedAdvertisement generatedAdvertisement = new EmptyGeneratedAdvertisement();
//        if (StringUtils.isEmpty(marketplaceId)) {
//            LOG.warn("MarketplaceId cannot be null or empty. Returning empty ad.");
//        } else {
//            final List<AdvertisementContent> contents = contentDao.get(marketplaceId);
//
//            if (CollectionUtils.isNotEmpty(contents)) {
//                AdvertisementContent randomAdvertisementContent = contents.get(random.nextInt(contents.size()));
//                generatedAdvertisement = new GeneratedAdvertisement(randomAdvertisementContent);
//            }
//
//        }
//
//        return generatedAdvertisement;
//
        List<AdvertisementContent> contents = contentDao.get(marketplaceId);

        RequestContext requestContext = new RequestContext(customerId, marketplaceId);

        TargetingEvaluator targetingEvaluator = new TargetingEvaluator(requestContext);

        //List<TargetingGroup> targetingGroups = targetingGroupDao.get(contents.get(0).getContentId());

        ExecutorService executor = Executors.newCachedThreadPool();

        List<List<TargetingGroup>> targetingGroupsList = new ArrayList<>();

        for (AdvertisementContent content : contents) {
            TargetingGroupDaoExecutor targetingGroupDaoExecutor = new TargetingGroupDaoExecutor(targetingGroupDao, content.getContentId());

            Future<List<TargetingGroup>> targetingListFuture = executor.submit(targetingGroupDaoExecutor);
            targetingGroupsList.add(targetingListFuture.get());
        }

        executor.shutdown();


//        List<TargetingGroup> targetingGroups = contents.stream()
//                .map(content -> executor.submit(targetingGroupDao.get(content.getContentId())))
//                .flatMap(List::stream)
//                .collect(Collectors.toList());

        List<TargetingGroup> targetingGroups = targetingGroupsList.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());



        //Mastery Task 1
//        TargetingPredicateResult targetingPredicateResult = targetingGroups.stream()
//                .map(targetingGroup ->  targetingEvaluator.evaluate(targetingGroup))
//                .filter(targetingPredicateResult1 -> targetingPredicateResult1.isTrue())
//                .findFirst().get();


        Map<Double, TargetingGroup> targetingGroupMap = targetingGroups.stream()
                .filter(targetingGroup -> targetingEvaluator.evaluate(targetingGroup) == TargetingPredicateResult.TRUE)
                .collect(Collectors.toMap(TargetingGroup::getClickThroughRate, targetingGroup -> targetingGroup));

        Map<Double, TargetingGroup> sortedTargetingGroupMap = new TreeMap<>(Comparator.reverseOrder());
        sortedTargetingGroupMap.putAll(targetingGroupMap);

        List<AdvertisementContent> filteredContents = contents.stream()
                .filter(advertisementContent -> advertisementContent.getMarketplaceId().equals(marketplaceId))
                .collect(Collectors.toList());

        for (Map.Entry<Double, TargetingGroup> entry : sortedTargetingGroupMap.entrySet()) {
            for (AdvertisementContent content : filteredContents) {
                if (entry.getValue().getContentId().equals(content.getContentId())){
                    return new GeneratedAdvertisement(content);
                }
            }

        }

//        if (targetingPredicateResult == null || targetingPredicateResult.isTrue()) {
//
//
//            return contents.stream()
//                    .filter(advertisementContent -> advertisementContent.getMarketplaceId().equals(marketplaceId))
//                    .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
//                        Collections.shuffle(list);
//                        return list;
//                    }))
//                    .stream().findFirst()
//                    .map(advertisementContent -> new GeneratedAdvertisement(advertisementContent))
//                    .orElse(new EmptyGeneratedAdvertisement());
//        }

        return new EmptyGeneratedAdvertisement();

    }
}
