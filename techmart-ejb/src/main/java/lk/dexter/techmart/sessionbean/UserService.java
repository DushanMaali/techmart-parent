package lk.dexter.techmart.sessionbean;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.dexter.techmart.entity.User;
import lk.dexter.techmart.service.UserServiceRemote;

import java.util.Date;
import java.util.List;

@Stateless(name = "UserService")
public class UserService implements UserServiceRemote {

    @PersistenceContext(unitName = "TechMart_Db_PU")
    private EntityManager em;

    @Override
    public boolean registerUser(User user) {
        try {
            if (em.find(User.class, user.getId()) != null) {
                return false;
            }
            user.setRegisteredAt(new Date());
            em.persist(user);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User loginUser(String email, String password) {
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.email = :email AND u.password = :password", User.class)
                    .setParameter("email", email)
                    .setParameter("password", password)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public User getUserById(String id) {
        return em.find(User.class, id);
    }

    @Override
    public String generateNextUserId() {
        Long count = em.createQuery("SELECT COUNT(u) FROM User u", Long.class)
                .getSingleResult();
        int nextNumber = count.intValue() + 1;
        return String.format("U%04d", nextNumber);
    }

    @Override
    public List<User> getAllUsers() {
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

}
