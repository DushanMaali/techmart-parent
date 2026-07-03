package lk.dexter.techmart.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "order_items")
public class OrderItems implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Integer orderItemId;

    @Column(name = "qty")
    private Integer qty;

    @Column(name = "subtotal")
    private Double subtotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orders_order_id")
    private Orders order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_product_id")
    private Product product;

    public OrderItems() {}

    // Getters and Setters
    public Integer getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Integer orderItemId) { this.orderItemId = orderItemId; }
    public Integer getQty() { return qty; }
    public void setQty(Integer qty) { this.qty = qty; }
    public Double getSubtotal() { return subtotal; }
    public void setSubtotal(Double subtotal) { this.subtotal = subtotal; }
    public Orders getOrder() { return order; }
    public void setOrder(Orders order) { this.order = order; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}