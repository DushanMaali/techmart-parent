package lk.dexter.techmart.service;

import jakarta.ejb.Remote;
import lk.dexter.techmart.entity.Inventory;
import lk.dexter.techmart.entity.Warehouse;
import java.util.List;

@Remote
public interface InventoryManagerRemote {
    void addInventory(Integer productId, int quantity, Integer warehouseId);
    boolean checkAndReduceStock(Integer productId, Integer warehouseId, int quantity);
    int getAvailableStock(Integer productId, Integer warehouseId);
    void updateStock(Integer productId, Integer warehouseId, int newQuantity);
    List<Inventory> getAllInventory();

    void addWarehouse(Warehouse warehouse);
    void updateWarehouse(Warehouse warehouse);
    List<Warehouse> getAllWarehouses();
}