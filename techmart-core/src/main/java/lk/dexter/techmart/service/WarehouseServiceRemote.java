package lk.dexter.techmart.service;

import jakarta.ejb.Remote;
import lk.dexter.techmart.entity.Warehouse;
import java.util.List;

@Remote
public interface WarehouseServiceRemote {
    void addWarehouse(Warehouse warehouse);
    void updateWarehouse(Warehouse warehouse);
    List<Warehouse> getAllWarehouses();
    Warehouse getWarehouseById(Integer id);
}