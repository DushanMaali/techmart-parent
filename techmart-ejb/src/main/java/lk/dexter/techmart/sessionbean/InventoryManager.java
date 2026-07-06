package lk.dexter.techmart.sessionbean;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.dexter.techmart.entity.*;
import lk.dexter.techmart.service.InventoryManagerRemote;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton(name = "InventoryManager")
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.CONTAINER)
public class InventoryManager implements InventoryManagerRemote {

    @PersistenceContext(unitName = "TechMart_Db_PU")
    private EntityManager em;

    private final Map<String, Integer> stockCache = new ConcurrentHashMap<>();

    // Circuit Breaker Variables
    private int failureCount = 0;
    private static final int THRESHOLD = 3;
    private boolean isAvailable = true;

    @PostConstruct
    public void init() {
        em.createQuery("SELECT i FROM Inventory i", Inventory.class)
                .getResultList()
                .forEach(inv -> stockCache.put(inv.getProduct().getProductId() + ":" + inv.getWarehouse().getWarehouse_id(), inv.getQuantity()));
    }

    private String getCacheKey(Integer pId, Integer wId) {
        return pId + ":" + wId;
    }

    @Override
    @Lock(LockType.WRITE)
    public boolean checkAndReduceStock(Integer productId, Integer warehouseId, int quantity) {
        // 1. Circuit Breaker Check
        if (!isAvailable) {
            System.err.println("Circuit is OPEN. Inventory service unavailable.");
            return false;
        }

        try {
            long startTime = System.currentTimeMillis(); // Performance Start

            String key = getCacheKey(productId, warehouseId);
            int currentStock = stockCache.getOrDefault(key, 0);

            if (currentStock >= quantity) {
                Inventory inv = em.createQuery("SELECT i FROM Inventory i WHERE i.product.productId = :pId AND i.warehouse.warehouse_id = :wId", Inventory.class)
                        .setParameter("pId", productId)
                        .setParameter("wId", warehouseId)
                        .getSingleResult();

                inv.setQuantity(currentStock - quantity);
                em.merge(inv);
                stockCache.put(key, currentStock - quantity);

                failureCount = 0; // Reset failures on success

                long duration = System.currentTimeMillis() - startTime; // Performance End
                System.out.println("Performance: checkAndReduceStock took " + duration + "ms");

                return true;
            }
            return false;
        } catch (Exception e) {
            failureCount++;
            if (failureCount >= THRESHOLD) {
                isAvailable = false; // Trip the circuit
            }
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Lock(LockType.READ)
    public int getAvailableStock(Integer productId, Integer warehouseId) {
        return stockCache.getOrDefault(getCacheKey(productId, warehouseId), 0);
    }

    @Override
    @Lock(LockType.WRITE)
    public void updateStock(Integer productId, Integer warehouseId, int newQuantity) {
        Inventory inv = em.createQuery("SELECT i FROM Inventory i WHERE i.product.productId = :pId AND i.warehouse.warehouse_id = :wId", Inventory.class)
                .setParameter("pId", productId)
                .setParameter("wId", warehouseId)
                .getSingleResult();

        inv.setQuantity(newQuantity);
        em.merge(inv);
        stockCache.put(getCacheKey(productId, warehouseId), newQuantity);
    }

    @Override
    @Lock(LockType.WRITE)
    public void addInventory(Integer productId, int quantity, Integer warehouseId) {
        List<Inventory> existingInventories = em.createQuery(
                        "SELECT i FROM Inventory i WHERE i.product.productId = :pId AND i.warehouse.warehouse_id = :wId", Inventory.class)
                .setParameter("pId", productId)
                .setParameter("wId", warehouseId)
                .getResultList();

        if (!existingInventories.isEmpty()) {
            Inventory inv = existingInventories.get(0);
            int newTotal = inv.getQuantity() + quantity;
            inv.setQuantity(newTotal);
            em.merge(inv);
            stockCache.put(getCacheKey(productId, warehouseId), newTotal);
        } else {
            Product product = em.find(Product.class, productId);
            Warehouse warehouse = em.find(Warehouse.class, warehouseId);

            if (product != null && warehouse != null) {
                Inventory inv = new Inventory();
                inv.setProduct(product);
                inv.setWarehouse(warehouse);
                inv.setQuantity(quantity);
                em.persist(inv);
                stockCache.put(getCacheKey(productId, warehouseId), quantity);
            }
        }
    }

    @Override
    public List<Inventory> getAllInventory() {
        return em.createQuery("SELECT i FROM Inventory i", Inventory.class).getResultList();
    }

    @Override
    @Lock(LockType.WRITE)
    public void addWarehouse(Warehouse warehouse) {
        em.persist(warehouse);
    }

    @Override
    @Lock(LockType.WRITE)
    public void updateWarehouse(Warehouse warehouse) {
        em.merge(warehouse);
    }

    @Override
    public List<Warehouse> getAllWarehouses() {
        return em.createQuery("SELECT w FROM Warehouse w", Warehouse.class).getResultList();
    }
}