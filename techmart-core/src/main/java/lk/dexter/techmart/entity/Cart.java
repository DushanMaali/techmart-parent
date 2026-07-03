package lk.dexter.techmart.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cart")
public class Cart implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_id")
    private Integer cartId;

    @Column(name = "total")
    private Double total;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    public Cart() {}

    // Getters and Setters
    public Integer getCartId() { return cartId; }
    public void setCartId(Integer cartId) { this.cartId = cartId; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}