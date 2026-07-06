package lk.dexter.techmart.sessionbean;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.dexter.techmart.entity.Category;
import lk.dexter.techmart.entity.Product;
import lk.dexter.techmart.entity.Inventory;
import lk.dexter.techmart.entity.Warehouse;
import lk.dexter.techmart.service.ProductServiceRemote;
import lk.dexter.techmart.service.InventoryManagerRemote;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless(name = "ProductService")
public class ProductService implements ProductServiceRemote {

    @PersistenceContext(unitName = "TechMart_Db_PU")
    private EntityManager em;

    @EJB
    private InventoryManagerRemote inventoryManager;

    @Override
    public List<Product> getAllProducts() {
        return em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
    }

    @Override
    public List<Category> getAllCategories() {
        return em.createQuery("SELECT c FROM Category c", Category.class).getResultList();
    }

    @Override
    public Product getProductById(Integer id) {
        return em.find(Product.class, id);
    }

    @Override
    public void addProduct(Product product) {
        em.persist(product);
    }

    @Override
    public List<Product> searchProductsByName(String name) {
        return em.createQuery("SELECT p FROM Product p WHERE p.productName LIKE :name", Product.class)
                .setParameter("name", "%" + name + "%")
                .getResultList();
    }

    @Override
    public List<Product> getProductsByCategory(Integer categoryId) {
        return em.createQuery("SELECT p FROM Product p WHERE p.category.categoryId = :catId", Product.class)
                .setParameter("catId", categoryId)
                .getResultList();
    }

    @Override
    public void updateProductStatus(Integer productId, Integer status) {
        Product product = em.find(Product.class, productId);
        if (product != null) {
            product.setStatus(status);
            em.merge(product);
        }
    }

    @Override
    public Map<Warehouse, Integer> getAvailableStockByProduct(Integer productId) {
        Map<Warehouse, Integer> stockMap = new HashMap<>();

        List<Inventory> inventories = em.createQuery(
                        "SELECT i FROM Inventory i WHERE i.product.productId = :pId", Inventory.class)
                .setParameter("pId", productId)
                .getResultList();

        for (Inventory inv : inventories) {
            stockMap.put(inv.getWarehouse(), inv.getQuantity());
        }
        return stockMap;
    }

    @Override
    public Map<String, List<Product>> getProductsGroupedByCategory() {
        List<Category> categories = getAllCategories();
        Map<String, List<Product>> groupedProducts = new HashMap<>();

        for (Category category : categories) {
            List<Product> products = em.createQuery(
                            "SELECT p FROM Product p WHERE p.category.categoryId = :catId AND p.status = 1", Product.class)
                    .setParameter("catId", category.getCategoryId())
                    .getResultList();

            if (!products.isEmpty()) {
                groupedProducts.put(category.getCategoryName(), products);
            }
        }
        return groupedProducts;
    }

    @Override
    public void updateProduct(Product product) {
        em.merge(product);
    }

}