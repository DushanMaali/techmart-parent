package lk.dexter.techmart.service;

import jakarta.ejb.Remote;
import lk.dexter.techmart.entity.Category;
import lk.dexter.techmart.entity.Product;
import lk.dexter.techmart.entity.Warehouse;

import java.util.List;
import java.util.Map;

@Remote
public interface ProductServiceRemote {

    List<Product> getAllProducts();
    List<Category> getAllCategories();
    Product getProductById(Integer id);
    void addProduct(Product product);
    List<Product> searchProductsByName(String name);
    List<Product> getProductsByCategory(Integer categoryId);

    void updateProductStatus(Integer productId, Integer status);
    Map<Warehouse, Integer> getAvailableStockByProduct(Integer productId);
    Map<String, List<Product>> getProductsGroupedByCategory();

}
