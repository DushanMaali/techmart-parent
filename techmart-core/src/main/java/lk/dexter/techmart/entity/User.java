package lk.dexter.techmart.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
public class User implements Serializable {

    @Id
    @Column(name = "id", length = 10)
    private String id;

    @Column(name = "fname", length = 45)
    private String fname;

    @Column(name = "lname", length = 45)
    private String lname;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "password", length = 8)
    private String password;

    @Column(name = "mobile", length = 10)
    private String mobile;

    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    public User() {}

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFname() { return fname; }
    public void setFname(String fname) { this.fname = fname; }
    public String getLname() { return lname; }
    public void setLname(String lname) { this.lname = lname; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }
}