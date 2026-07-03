package lk.dexter.techmart.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "inventory")
public class Inventory implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inventory_id")
    private Integer inventoryId;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "warehouse_name", length = 45)
    private String warehouseName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_product_id")
    private Product product;

    public Inventory() {}

    // Getters and Setters
    public Integer getInventoryId() { return inventoryId; }
    public void setInventoryId(Integer inventoryId) { this.inventoryId = inventoryId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getWarehouseName() { return warehouseName; }
    public void setWarehouseName(String warehouseName) { this.warehouseName = warehouseName; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}