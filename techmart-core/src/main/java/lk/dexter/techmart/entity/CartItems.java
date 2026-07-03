package lk.dexter.techmart.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cart_items")
public class CartItems implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cart_items_id")
    private Integer cartItemsId;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "sub_total")
    private Double subTotal;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_cart_id")
    private Cart cart;

    public CartItems() {}

    // Getters and Setters
    public Integer getCartItemsId() { return cartItemsId; }
    public void setCartItemsId(Integer cartItemsId) { this.cartItemsId = cartItemsId; }
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }
    public Double getSubTotal() { return subTotal; }
    public void setSubTotal(Double subTotal) { this.subTotal = subTotal; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }
}