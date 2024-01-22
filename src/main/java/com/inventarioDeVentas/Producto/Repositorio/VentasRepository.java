
package com.inventarioDeVentas.Producto.Repositorio;

import com.inventarioDeVentas.Producto.Producto.Venta;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VentasRepository extends CrudRepository<Venta, Integer> {
    
}
