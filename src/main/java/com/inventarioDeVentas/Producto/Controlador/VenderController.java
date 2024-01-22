
package com.inventarioDeVentas.Producto.Controlador;

import com.inventarioDeVentas.Producto.Producto.Producto;
import com.inventarioDeVentas.Producto.Producto.ProductoParaVender;
import com.inventarioDeVentas.Producto.Producto.ProductoVendido;
import com.inventarioDeVentas.Producto.Producto.Venta;
import com.inventarioDeVentas.Producto.Repositorio.ProductosRepository;
import com.inventarioDeVentas.Producto.Repositorio.ProductosVendidosRepository;
import com.inventarioDeVentas.Producto.Repositorio.VentasRepository;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(path = "/vender")
public class VenderController {
    @Autowired
    private ProductosRepository productosRepository;
    @Autowired
    private VentasRepository ventasRepository;
    @Autowired
    private ProductosVendidosRepository productosVendidosRepository;

    @PostMapping(value = "/quitar/{indice}")
    public String quitarDelCarrito(@PathVariable int indice, HttpServletRequest request) {
        ArrayList<ProductoParaVender> carrito = this.obtenerCarrito(request);
        if (carrito != null && carrito.size() > 0 && carrito.get(indice) != null) {
            carrito.remove(indice);
            this.guardarCarrito(carrito, request);
        }
        return "redirect:/vender/";
    }

    private void limpiarCarrito(HttpServletRequest request) {
        this.guardarCarrito(new ArrayList<>(), request);
    }

    @GetMapping(value = "/limpiar")
    public String cancelarVenta(HttpServletRequest request, RedirectAttributes redirectAttrs) {
        this.limpiarCarrito(request);
        redirectAttrs
                .addFlashAttribute("mensaje", "Venta cancelada")
                .addFlashAttribute("clase", "info");
        return "redirect:/vender/";
    }

    @PostMapping(value = "/terminar")
    public String terminarVenta(HttpServletRequest request, RedirectAttributes redirectAttrs) {
        ArrayList<ProductoParaVender> carrito = this.obtenerCarrito(request);

        // Si no hay carrito o está vacío, regresamos inmediatamente
        if (carrito == null || carrito.isEmpty()) {
            return "redirect:/vender/";
        }

        Venta v = ventasRepository.save(new Venta());

        // Recorrer el carrito
        for (ProductoParaVender productoParaVender : carrito) {
            // Obtener el producto fresco desde la base de datos
            Integer productId = productoParaVender.getId();
            if (productId != null) {
                Producto p = productosRepository.findById(productId).orElse(null);
                if (p != null) {
                    // Le restamos existencia
                    Float cantidad = productoParaVender.getCantidad();
                    if (cantidad != null) {
                        p.restarExistencia(cantidad);

                        // Lo guardamos con la existencia ya restada
                        productosRepository.save(p);

                        // Creamos un nuevo producto que será el que se guarda junto con la venta
                        ProductoVendido productoVendido = new ProductoVendido(
                                cantidad,
                                productoParaVender.getPrecio(),
                                productoParaVender.getNombre(),
                                productoParaVender.getCodigo(),
                                v);

                        // Y lo guardamos
                        productosVendidosRepository.save(productoVendido);
                    } else {
                        // Manejar el caso en que la cantidad sea nula
                        redirectAttrs
                                .addFlashAttribute("mensaje", "Error al procesar la venta. Cantidad nula.")
                                .addFlashAttribute("clase", "danger");
                        return "redirect:/vender/";
                    }
                } else {
                    // Manejar el caso en que el producto no existe
                    redirectAttrs
                            .addFlashAttribute("mensaje", "Error al procesar la venta. Producto no encontrado.")
                            .addFlashAttribute("clase", "danger");
                    return "redirect:/vender/";
                }
            } else {
                // Manejar el caso en que el ID del producto sea nulo
                redirectAttrs
                        .addFlashAttribute("mensaje", "Error al procesar la venta. ID del producto nulo.")
                        .addFlashAttribute("clase", "danger");
                return "redirect:/vender/";
            }
        }

        // Al final limpiamos el carrito
        this.limpiarCarrito(request);

        // E indicamos una venta exitosa
        redirectAttrs
                .addFlashAttribute("mensaje", "Venta realizada correctamente")
                .addFlashAttribute("clase", "success");

        return "redirect:/vender/";
    }

    @GetMapping(value = "/")
    public String interfazVender(Model model, HttpServletRequest request) {
        model.addAttribute("producto", new Producto());
        float total = 0;
        ArrayList<ProductoParaVender> carrito = this.obtenerCarrito(request);
        for (ProductoParaVender p : carrito)
            total += p.getTotal();
        model.addAttribute("total", total);
        return "vender/vender";
    }

    private ArrayList<ProductoParaVender> obtenerCarrito(HttpServletRequest request) {
        @SuppressWarnings("unchecked") // Para suprimir la advertencia del cast no verificado
        ArrayList<ProductoParaVender> carrito = (ArrayList<ProductoParaVender>) request.getSession()
                .getAttribute("carrito");

        if (carrito == null) {
            carrito = new ArrayList<>();
            request.getSession().setAttribute("carrito", carrito);
        }

        return carrito;
    }

    private void guardarCarrito(ArrayList<ProductoParaVender> carrito, HttpServletRequest request) {
        request.getSession().setAttribute("carrito", carrito);
    }

    @PostMapping(value = "/agregar")
    public String agregarAlCarrito(@ModelAttribute Producto producto, HttpServletRequest request,
            RedirectAttributes redirectAttrs) {
        ArrayList<ProductoParaVender> carrito = this.obtenerCarrito(request);
        Producto productoBuscadoPorCodigo = productosRepository.findFirstByCodigo(producto.getCodigo());
        if (productoBuscadoPorCodigo == null) {
            redirectAttrs
                    .addFlashAttribute("mensaje", "El producto con el código " + producto.getCodigo() + " no existe")
                    .addFlashAttribute("clase", "warning");
            return "redirect:/vender/";
        }
        if (productoBuscadoPorCodigo.sinExistencia()) {
            redirectAttrs
                    .addFlashAttribute("mensaje", "El producto está agotado")
                    .addFlashAttribute("clase", "warning");
            return "redirect:/vender/";
        }
        boolean encontrado = false;
        for (ProductoParaVender productoParaVenderActual : carrito) {
            if (productoParaVenderActual.getCodigo().equals(productoBuscadoPorCodigo.getCodigo())) {
                productoParaVenderActual.aumentarCantidad();
                encontrado = true;
                break;
            }
        }
        if (!encontrado) {
            carrito.add(new ProductoParaVender(productoBuscadoPorCodigo.getNombre(),
                    productoBuscadoPorCodigo.getCodigo(), productoBuscadoPorCodigo.getPrecio(),
                    productoBuscadoPorCodigo.getExistencia(), productoBuscadoPorCodigo.getId(), 1f));
        }
        this.guardarCarrito(carrito, request);
        return "redirect:/vender/";
    }
}
