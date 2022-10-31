package com.vikadata.api.modular.player.service;

import java.io.IOException;
import java.util.List;

import cn.hutool.core.lang.Dict;
import com.baomidou.mybatisplus.extension.service.IService;

import com.vikadata.api.cache.bean.LoginUserDto;
import com.vikadata.api.component.notification.NotificationToTag;
import com.vikadata.api.model.dto.player.NotificationModelDto;
import com.vikadata.api.model.ro.player.NotificationCreateRo;
import com.vikadata.api.model.ro.player.NotificationListRo;
import com.vikadata.api.model.ro.player.NotificationPageRo;
import com.vikadata.api.model.ro.player.NotificationRevokeRo;
import com.vikadata.api.model.vo.player.NotificationDetailVo;
import com.vikadata.api.model.vo.player.NotificationStatisticsVo;
import com.vikadata.entity.PlayerNotificationEntity;
import com.vikadata.system.config.notification.NotificationTemplate;

/**
 * <p>
 * Player Notification Service
 * </p>
 */
public interface IPlayerNotificationService extends IService<PlayerNotificationEntity> {

    /**
     * Batch create notification records, send notifications
     */
    boolean batchCreateNotify(List<NotificationCreateRo> roList);

    /**
     * Create notification records, send notifications
     */
    void createNotify(NotificationCreateRo ro);

    /**
     * Create notification records, send notifications
     */
    void createUserNotify(NotificationTemplate template, NotificationCreateRo ro);

    /**
     * Create notification records, send notifications
     */
    void createMemberNotify(NotificationTemplate template, NotificationCreateRo ro,
            NotificationToTag toTag);

    /**
     * Create a member mention notification
     */
    void createMemberMentionedNotify(List<Long> toUserIds,
            NotificationTemplate template,
            NotificationCreateRo ro);

    /**
     * Create system notifications
     */
    void createAllUserNotify(NotificationTemplate template, NotificationCreateRo ro) throws IOException;

    /**
     * Create records and send notifications without any data validation
     */
    boolean createNotifyWithoutVerify(List<Long> userIds, NotificationTemplate template,
            NotificationCreateRo ro);

    /**
     * Get notification page list
     */
    List<NotificationDetailVo> pageList(NotificationPageRo notificationPageRo, LoginUserDto toUser);

    /**
     * Assemble the data returned to the client
     */
    List<NotificationDetailVo> formatDetailVos(List<NotificationModelDto> notificationModelDtoList, String uuid);

    /**
     * Set notification read
     */
    boolean setNotificationIsRead(String[] ids, Integer isAll);

    /**
     * User message statistics
     */
    NotificationStatisticsVo statistic(Long userId);

    /**
     * Delete message
     */
    boolean setDeletedIsTrue(String[] ids);

    /**
     * Bulk create messages
     */
    boolean createBatch(List<PlayerNotificationEntity> notifyEntities,
            List<PlayerNotificationEntity> createEntities);

    /**
     * Bulk create messages
     */
    boolean createBatch(List<PlayerNotificationEntity> notifyEntities);

    /**
     * Get user notification list
     */
    List<NotificationDetailVo> list(NotificationListRo notificationListRo,
            LoginUserDto toUser);

    /**
     * Get user notification list
     */
    List<NotificationModelDto> getUserNotificationByTypeAndIsRead(Long toUser, Integer isRead);

    /**
     * Revoke notification
     */
    boolean revokeNotification(NotificationRevokeRo ro);

    /**
     * Revoke all user notification
     */
    boolean revokeAllUserNotification(NotificationTemplate template,
            NotificationRevokeRo ro);

    /**
     * Batch send mail notification
     * To specify user
     */
    void sendMailNotifyBatch(NotificationTemplate template, List<Long> userIds, Dict dict);
}
