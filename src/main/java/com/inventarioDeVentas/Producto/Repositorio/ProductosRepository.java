
package com.inventarioDeVentas.Producto.Repositorio;

import com.inventarioDeVentas.Producto.Producto.Producto;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductosRepository extends CrudRepository<Producto, Integer> {
    
     Producto findFirstByCodigo(String codigo);
}
