package lk.dexter.techmart.service;

import jakarta.ejb.Remote;
import lk.dexter.techmart.entity.User;

import java.util.List;

@Remote
public interface UserServiceRemote {
    boolean registerUser(User user);
    User loginUser(String email, String password);
    User getUserById(String id);
    String generateNextUserId();
    List<User> getAllUsers();
}