package lk.dexter.techmart.service;

import jakarta.ejb.Remote;
import lk.dexter.techmart.entity.Notification;

import java.util.List;

@Remote
public interface NotificationServiceRemote {

    void addNotification(Notification notification);
    List<Notification> getUnreadNotifications(String userId);
    void sendNotificationToQueue(String userId, String message, Integer typeId);
    void markAsRead(Integer notificationId);
    List<Notification> getAllNotifications();
    List<Notification> getAdminNotifications();

}
