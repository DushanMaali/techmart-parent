package lk.dexter.techmart.sessionbean;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lk.dexter.techmart.entity.Warehouse;
import lk.dexter.techmart.service.WarehouseServiceRemote;
import java.util.List;

@Stateless(name = "WarehouseService")
public class WarehouseService implements WarehouseServiceRemote {

    @PersistenceContext(unitName = "TechMart_Db_PU")
    private EntityManager em;

    @Override
    public void addWarehouse(Warehouse warehouse) {
        em.persist(warehouse);
    }

    @Override
    public void updateWarehouse(Warehouse warehouse) {
        em.merge(warehouse);
    }

    @Override
    public List<Warehouse> getAllWarehouses() {
        return em.createQuery("SELECT w FROM Warehouse w", Warehouse.class).getResultList();
    }

    @Override
    public Warehouse getWarehouseById(Integer id) {
        return em.find(Warehouse.class, id);
    }
}