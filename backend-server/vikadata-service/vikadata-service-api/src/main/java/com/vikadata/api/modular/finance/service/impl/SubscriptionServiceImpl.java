package com.vikadata.api.modular.finance.service.impl;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;

import com.vikadata.api.enums.audit.ChangeType;
import com.vikadata.api.enums.finance.SubscriptionPhase;
import com.vikadata.api.enums.finance.SubscriptionState;
import com.vikadata.api.modular.finance.core.Subscription;
import com.vikadata.api.modular.finance.mapper.SubscriptionMapper;
import com.vikadata.api.modular.finance.service.IBundleService;
import com.vikadata.api.modular.finance.service.ISubscriptionHistoryService;
import com.vikadata.api.modular.finance.service.ISubscriptionService;
import com.vikadata.api.util.billing.model.ProductCategory;
import com.vikadata.core.util.SqlTool;
import com.vikadata.entity.SubscriptionEntity;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * <p>
 * Subscription Service Implement Class
 * </p>
 */
@Service
@Slf4j
public class SubscriptionServiceImpl extends ServiceImpl<SubscriptionMapper, SubscriptionEntity> implements ISubscriptionService {

    @Resource
    private ISubscriptionHistoryService iSubscriptionHistoryService;

    @Resource
    private IBundleService iBundleService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SubscriptionEntity entity) {
        save(entity);
        iSubscriptionHistoryService.saveHistory(entity, ChangeType.INSERT);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createBatch(List<SubscriptionEntity> entities) {
        saveBatch(entities);
        iSubscriptionHistoryService.saveBatchHistory(entities, ChangeType.INSERT);
    }

    @Override
    public SubscriptionEntity getBySubscriptionId(String subscriptionId) {
        return baseMapper.selectBySubscriptionId(subscriptionId);
    }

    @Override
    public List<SubscriptionEntity> getByBundleIds(List<String> bundleIds) {
        return baseMapper.selectByBundleIds(bundleIds);
    }

    @Override
    public List<Subscription> getSubscriptionsByBundleIds(List<String> bundleIds) {
        List<SubscriptionEntity> subscriptionEntities = getByBundleIds(bundleIds);
        // convert subscription entity to viewable subscription
        Function<SubscriptionEntity, Subscription> subscriptionConverter = subscriptionEntity ->
                Subscription.builder()
                        .spaceId(subscriptionEntity.getSpaceId())
                        .bundleId(subscriptionEntity.getBundleId())
                        .subscriptionId(subscriptionEntity.getSubscriptionId())
                        .productName(subscriptionEntity.getProductName())
                        .productCategory(ProductCategory.valueOf(subscriptionEntity.getProductCategory()))
                        .planId(subscriptionEntity.getPlanId())
                        .state(SubscriptionState.valueOf(subscriptionEntity.getState()))
                        .startDate(subscriptionEntity.getStartDate())
                        .expireDate(subscriptionEntity.getExpireDate())
                        .phase(SubscriptionPhase.of(subscriptionEntity.getPhase()))
                        .build();
        return subscriptionEntities.stream()
                .map(subscriptionConverter).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateBySubscriptionId(String subscriptionId, SubscriptionEntity updatedSubscription) {
        SubscriptionEntity subscriptionEntity = getBySubscriptionId(subscriptionId);
        if (subscriptionEntity == null) {
            throw new RuntimeException("update subscription error");
        }
        updatedSubscription.setId(subscriptionEntity.getId());
        updateById(updatedSubscription);
        if (updatedSubscription.getProductName() != null) {
            subscriptionEntity.setProductName(updatedSubscription.getProductName());
        }
        if (updatedSubscription.getProductCategory() != null) {
            subscriptionEntity.setProductCategory(updatedSubscription.getProductCategory());
        }
        if (updatedSubscription.getPlanId() != null) {
            subscriptionEntity.setPlanId(updatedSubscription.getPlanId());
        }
        if (updatedSubscription.getStartDate() != null) {
            subscriptionEntity.setStartDate(updatedSubscription.getStartDate());
        }
        if (updatedSubscription.getExpireDate() != null) {
            subscriptionEntity.setExpireDate(updatedSubscription.getExpireDate());
        }
        if (updatedSubscription.getState() != null) {
            subscriptionEntity.setState(updatedSubscription.getState());
        }
        if (updatedSubscription.getPhase() != null) {
            subscriptionEntity.setPhase(updatedSubscription.getPhase());
        }
        iSubscriptionHistoryService.saveHistory(subscriptionEntity, ChangeType.UPDATE);
    }

    @Override
    public List<SubscriptionEntity> getByBundleIdAndState(String bundleId, SubscriptionState state) {
        return baseMapper.selectByBundleIdAndState(bundleId, state);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeBatchBySubscriptionIds(List<String> subscriptionIds) {
        baseMapper.updateIsDeletedBySubscriptionIds(subscriptionIds, true);
        List<SubscriptionEntity> subscriptionEntities = baseMapper.selectByBundleIds(subscriptionIds);
        if (!subscriptionEntities.isEmpty()) {
            iSubscriptionHistoryService.saveBatchHistory(subscriptionEntities, ChangeType.DELETE);
        }
    }

    @Override
    public void restoreBySubscriptionIds(List<String> subscriptionIds) {
        baseMapper.updateIsDeletedBySubscriptionIds(subscriptionIds, false);
        List<SubscriptionEntity> subscriptionEntities = baseMapper.selectBySubscriptionIds(subscriptionIds);
        List<String> bundleIds =
                subscriptionEntities.stream().map(SubscriptionEntity::getBundleId).collect(Collectors.toList());
        if (!subscriptionEntities.isEmpty()) {
            iSubscriptionHistoryService.saveBatchHistory(subscriptionEntities, ChangeType.UPDATE);
        }
        if (!bundleIds.isEmpty()) {
            iBundleService.restoreByBundleIds(bundleIds);
        }
    }

    @Override
    public String getActiveTrailSubscriptionIdBySpaceId(String spaceId) {
        return baseMapper.selectSubscriptionIdBySpaceIdAndPhaseIgnoreDeleted(spaceId, SubscriptionPhase.TRIAL.getName());
    }

    @Override
    public List<String> getBundleIdsBySubscriptionIds(List<String> subscriptionIds) {
        return baseMapper.selectBundleIdsBySubscriptionIds(subscriptionIds);
    }

    @Override
    public boolean bundlesHaveSubscriptions(List<String> bundleIds) {
        return SqlTool.retCount(baseMapper.selectCountByBundleIds(bundleIds)) > 0;
    }
}
