
package com.inventarioDeVentas.Producto.Controlador;

import com.inventarioDeVentas.Producto.Producto.Producto;
import com.inventarioDeVentas.Producto.Repositorio.ProductosRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(path = "/productos")
public class ProductosController {
    @Autowired
    private ProductosRepository productosRepository;

    @GetMapping(value = "/agregar")
    public String agregarProducto(Model model) {
        model.addAttribute("producto", new Producto());
        return "productos/agregar_producto";
    }

    @GetMapping(value = "/mostrar")
    public String mostrarProductos(Model model) {
        model.addAttribute("productos", productosRepository.findAll());
        return "productos/ver_productos";
    }

    @PostMapping(value = "/eliminar")
    public String eliminarProducto(@ModelAttribute Producto producto, RedirectAttributes redirectAttrs) {
        if (producto != null && producto.getId() != null) {
            try {
                // Verificar que el ID no sea nulo antes de intentar eliminar
                Integer productId = producto.getId();
                if (productId != null) {
                    productosRepository.deleteById(productId);
                    redirectAttrs
                            .addFlashAttribute("mensaje", "Eliminado correctamente")
                            .addFlashAttribute("clase", "warning");
                } else {
                    redirectAttrs
                            .addFlashAttribute("mensaje", "Error al eliminar el producto. ID nulo.")
                            .addFlashAttribute("clase", "danger");
                }
            } catch (Exception e) {
                // Manejar cualquier excepción que pueda ocurrir durante la eliminación
                redirectAttrs
                        .addFlashAttribute("mensaje", "Error al eliminar el producto. Por favor, inténtalo de nuevo.")
                        .addFlashAttribute("clase", "danger");
            }
        } else {
            // Manejar el caso en que el producto o su ID son nulos
            redirectAttrs
                    .addFlashAttribute("mensaje", "Error al eliminar el producto. Producto o ID nulos.")
                    .addFlashAttribute("clase", "danger");
        }
        return "redirect:/productos/mostrar";
    }

    // Se colocó el parámetro ID para eso de los errores, ya sé el id se puede
    // recuperar
    // a través del modelo, pero lo que yo quiero es que se vea la misma URL para
    // regresar la vista con
    // los errores en lugar de hacer un redirect, ya que si hago un redirect, no se
    // muestran los errores del formulario
    // y por eso regreso mejor la vista ;)
    @PostMapping(value = "/editar/{id}")
    public String actualizarProducto(@ModelAttribute @Valid Producto producto, BindingResult bindingResult,
            RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            if (producto.getId() != null) {
                return "productos/editar_producto";
            }
            return "redirect:/productos/mostrar";
        }
        Producto posibleProductoExistente = productosRepository.findFirstByCodigo(producto.getCodigo());

        if (posibleProductoExistente != null && !posibleProductoExistente.getId().equals(producto.getId())) {
            redirectAttrs
                    .addFlashAttribute("mensaje", "Ya existe un producto con ese código")
                    .addFlashAttribute("clase", "warning");
            return "redirect:/productos/agregar";
        }
        productosRepository.save(producto);
        redirectAttrs
                .addFlashAttribute("mensaje", "Editado correctamente")
                .addFlashAttribute("clase", "success");
        return "redirect:/productos/mostrar";
    }

    @GetMapping(value = "/editar/{id}")
    public String mostrarFormularioEditar(@PathVariable int id, Model model) {
        model.addAttribute("producto", productosRepository.findById(id).orElse(null));
        return "productos/editar_producto";
    }

    @PostMapping(value = "/agregar")
    public String guardarProducto(@ModelAttribute @Valid Producto producto, BindingResult bindingResult,
            RedirectAttributes redirectAttrs) {
        if (bindingResult.hasErrors()) {
            return "productos/agregar_producto";
        }
        if (productosRepository.findFirstByCodigo(producto.getCodigo()) != null) {
            redirectAttrs
                    .addFlashAttribute("mensaje", "Ya existe un producto con ese código")
                    .addFlashAttribute("clase", "warning");
            return "redirect:/productos/agregar";
        }
        productosRepository.save(producto);
        redirectAttrs
                .addFlashAttribute("mensaje", "Agregado correctamente")
                .addFlashAttribute("clase", "success");
        return "redirect:/productos/agregar";
    }
}
