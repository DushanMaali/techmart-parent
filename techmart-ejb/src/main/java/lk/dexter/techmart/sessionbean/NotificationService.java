package lk.dexter.techmart.sessionbean;

import jakarta.annotation.Resource;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.jms.Destination;
import jakarta.jms.JMSContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lk.dexter.techmart.entity.Notification;
import lk.dexter.techmart.service.NotificationServiceRemote;

import java.util.List;

@Stateless(name = "NotificationService")
public class NotificationService implements NotificationServiceRemote {

    @PersistenceContext(unitName = "TechMart_Db_PU")
    private EntityManager em;

    @Resource(lookup = "jms/techMartNotificationQueue")
    private Destination queue;

    @Inject
    private JMSContext jmsContext;

    @Override
    public void addNotification(Notification notification) {
        em.persist(notification);
    }

    @Override
    public List<Notification> getUnreadNotifications(String userId) {
        return em.createQuery("SELECT n FROM Notification n WHERE n.user.id = :userId AND n.isRead = 0", Notification.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public void sendNotificationToQueue(String userId, String message, Integer typeId) {
        jmsContext.createProducer().send(queue, userId + "|" + message + "|" + typeId);
    }

    @Override
    @Transactional
    public void markAsRead(Integer notificationId) {
        Notification notification = em.find(Notification.class, notificationId);
        if (notification != null) {
            notification.setIsRead(1);
            em.merge(notification);
        }
    }

    @Override
    public List<Notification> getAllNotifications() {
        return em.createQuery("SELECT n FROM Notification n", Notification.class).getResultList();
    }

    @Override
    public List<Notification> getAdminNotifications() {
        return em.createQuery("SELECT n FROM Notification n WHERE n.notificationType.id = 4", Notification.class)
                .getResultList();
    }
}