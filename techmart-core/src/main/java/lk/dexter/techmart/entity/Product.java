package lk.dexter.techmart.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "product")
public class Product implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Integer productId;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "description",columnDefinition = "TEXT")
    private String description;

    @Column(name = "price")
    private Double price;

    @Column(name = "prodcut_img", columnDefinition = "TEXT")
    private String productImg;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_category_id")
    private Category category;

    @Column(name = "status")
    private Integer status;

    public Product() {}

    // Getters and Setters
    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getProductImg() { return productImg; }
    public void setProductImg(String productImg) { this.productImg = productImg; }
    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
    public Integer getStatus() {
        return status;
    }
    public void setStatus(Integer status) {
        this.status = status;
    }
}