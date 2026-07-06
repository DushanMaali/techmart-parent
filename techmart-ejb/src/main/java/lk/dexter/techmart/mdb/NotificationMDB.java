package lk.dexter.techmart.mdb;

import jakarta.ejb.ActivationConfigProperty;
import jakarta.ejb.MessageDriven;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lk.dexter.techmart.entity.Notification;
import lk.dexter.techmart.entity.NotificationType;
import lk.dexter.techmart.entity.User;

import java.util.List;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationLookup", propertyValue = "jms/techMartNotificationQueue"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "jakarta.jms.Queue")
})
public class NotificationMDB implements MessageListener {

    @PersistenceContext(unitName = "TechMart_Db_PU")
    private EntityManager em;

    @Override
    @Transactional
    public void onMessage(Message message) {
        try {
            if (!(message instanceof TextMessage)) return;

            String text = ((TextMessage) message).getText();
            String[] parts = text.split("\\|");

            if (parts.length < 3) {
                System.err.println("Invalid notification message: " + text);
                return;
            }

            String userId = parts[0];
            String msgContent = parts[1];
            Integer typeId = Integer.parseInt(parts[2]);

            User user = em.find(User.class, userId);


            if (user == null) {
                System.err.println("**************************************************");
                System.err.println("LOG: User not found: " + userId + ". Processing as guest.");
                System.err.println("**************************************************");
            }else{
                NotificationType type = em.find(NotificationType.class, typeId);

                    Notification notification = new Notification();
                    notification.setUser(user);
                    notification.setMessage(msgContent);
                    notification.setNotificationType(type);
                    notification.setIsRead(0);

                    em.persist(notification);
                    System.out.println("Notification saved successfully.");

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}