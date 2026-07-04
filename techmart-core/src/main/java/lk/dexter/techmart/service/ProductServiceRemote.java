package lk.dexter.techmart.service;

import jakarta.ejb.Remote;
import lk.dexter.techmart.entity.Category;
import lk.dexter.techmart.entity.Product;

import java.util.List;

@Remote
public interface ProductServiceRemote {

    List<Product> getAllProducts();
    List<Category> getAllCategories();
    Product getProductById(Integer id);
    void addProduct(Product product);
    List<Product> searchProductsByName(String name);
    List<Product> getProductsByCategory(Integer categoryId);

}
