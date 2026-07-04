package lk.dexter.techmart.sessionbean;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.dexter.techmart.entity.Category;
import lk.dexter.techmart.entity.Product;
import lk.dexter.techmart.service.ProductServiceRemote;

import java.util.List;

@Stateless(name = "ProductService")
public class ProductService implements ProductServiceRemote {

    @PersistenceContext(unitName = "TechMart_Db_PU")
    private EntityManager em;

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

}
