
package com.inventarioDeVentas.Producto.Repositorio;

import com.inventarioDeVentas.Producto.Producto.ProductoVendido;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductosVendidosRepository extends CrudRepository<ProductoVendido, Integer>{
    
}
