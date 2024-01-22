
package com.inventarioDeVentas.Producto.Producto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class Utiles {
     static String obtenerFechaYHoraActual() {
        String formato = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern(formato);
        LocalDateTime ahora = LocalDateTime.now();
        return formateador.format(ahora);
    }
}
