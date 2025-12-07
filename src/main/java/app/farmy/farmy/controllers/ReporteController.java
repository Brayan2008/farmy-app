package app.farmy.farmy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import app.farmy.farmy.model.Reporte;
import app.farmy.farmy.repository.ReporteRepository;

//import java.time.LocalDate;
import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reportes")
public class ReporteController {
    
    @Autowired
    private ReporteRepository reporteRepository;
    
    @GetMapping
    public String listarReportes(Model model) {
        List<Reporte> reportes = reporteRepository.findAllByOrderByFechaGeneracionDesc();
        model.addAttribute("reportes", reportes);
        model.addAttribute("reporte", new Reporte());
        return "home/reportes/reportes";
    }
    
    @PostMapping("/crear")
    public String crearReporte(
            @RequestParam String nombre,
            @RequestParam String tipo,
            @RequestParam String formato,
            @RequestParam String periodo,
            @RequestParam String estado,
            @RequestParam String fechaDesde,
            @RequestParam String fechaHasta,
            @RequestParam(required = false) String descripcion,
            Model model) {
        
        // Guardar los parámetros del reporte en la sesión
        Map<String, String> reporteData = new HashMap<>();
        reporteData.put("nombre", nombre);
        reporteData.put("tipo", tipo);
        reporteData.put("formato", formato);
        reporteData.put("periodo", periodo);
        reporteData.put("estado", estado);
        reporteData.put("fechaDesde", fechaDesde);
        reporteData.put("fechaHasta", fechaHasta);
        reporteData.put("descripcion", descripcion != null ? descripcion : "");
        reporteData.put("fechaCreacion", LocalDateTime.now().toString());
        
        // Redirigir según el tipo de reporte
        if ("ventas".equals(tipo)) {
            model.addAttribute("reporteData", reporteData);
            return "redirect:/ventas/reportes?fromModal=true&nombre=" + nombre 
                   + "&tipo=" + tipo + "&formato=" + formato 
                   + "&periodo=" + periodo + "&estado=" + estado
                   + "&fechaDesde=" + fechaDesde + "&fechaHasta=" + fechaHasta;
        } else if ("compras".equals(tipo)) {
            // Redirigir a compras/reportes cuando el tipo es "compras"
            model.addAttribute("reporteData", reporteData);
            return "redirect:/compras/reportes?fromModal=true&nombre=" + nombre 
                   + "&tipo=" + tipo + "&formato=" + formato 
                   + "&periodo=" + periodo + "&estado=" + estado
                   + "&fechaDesde=" + fechaDesde + "&fechaHasta=" + fechaHasta;
        } else {
            // Para otros tipos, crear el reporte directamente
            Reporte reporte = new Reporte();
            reporte.setNombre(nombre);
            reporte.setTipo(tipo);
            reporte.setFormato(formato);
            reporte.setEstado("proceso");
            reporte.setFechaGeneracion(null); // No se genera aún
            reporte.setCodigo("RPT" + String.format("%03d", reporteRepository.count() + 1));
            reporte.setDescripcion(descripcion);
            
            reporteRepository.save(reporte);
            return "redirect:/reportes";
        }
    }
    
    @PostMapping("/guardar")
    public String guardarReporte(@ModelAttribute Reporte reporte) {
        // Generar código único
        reporte.setCodigo("RPT" + String.format("%03d", reporteRepository.count() + 1));
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setEstado("proceso"); // Cambiar a "generado" cuando se complete
        
        reporteRepository.save(reporte);
        
        // Aquí se podría iniciar la generación del reporte en segundo plano
        // generarReporteEnSegundoPlano(reporte);
        
        return "redirect:/reportes";
    }
    
    @PostMapping("/guardar-desde-ventas")
    public String guardarReporteDesdeVentas(
            @RequestParam String nombre,
            @RequestParam String formato,
            @RequestParam(required = false) String descripcion,
            @RequestParam String tipo,
            @RequestParam String fechaInicio,
            @RequestParam String horaInicio,
            @RequestParam String fechaFin,
            @RequestParam String horaFin,
            @RequestParam(required = false) String tipoDocumento,
            @RequestParam(required = false) String numeroDocumento,
            @RequestParam(required = false) String nombreCliente,
            @RequestParam(required = false) String metodoPagoNombre,
            @RequestParam(required = false) String estadoVenta,
            @RequestParam(required = false) Double importeMinimo,
            @RequestParam(required = false) Double importeMaximo,
            @RequestParam int totalRegistros) {
        
        Reporte reporte = new Reporte();
        reporte.setNombre(nombre);
        reporte.setTipo(tipo);
        reporte.setFormato(formato);
        reporte.setDescripcion(descripcion);
        reporte.setEstado("generado");
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setFechaCompletado(LocalDateTime.now());
        reporte.setRegistrosProcesados(totalRegistros);
        
        // Generar código único
        long siguienteNumero = reporteRepository.count() + 1;
        reporte.setCodigo("RPT" + String.format("%03d", siguienteNumero));
        
        // Guardar parámetros del filtro
        StringBuilder parametros = new StringBuilder();
        parametros.append("Período: ").append(fechaInicio).append(" ").append(horaInicio)
                  .append(" - ").append(fechaFin).append(" ").append(horaFin).append("\n");
        if (tipoDocumento != null && !tipoDocumento.isEmpty()) {
            parametros.append("Tipo Documento: ").append(tipoDocumento).append("\n");
        }
        if (numeroDocumento != null && !numeroDocumento.isEmpty()) {
            parametros.append("N° Documento: ").append(numeroDocumento).append("\n");
        }
        if (nombreCliente != null && !nombreCliente.isEmpty()) {
            parametros.append("Cliente: ").append(nombreCliente).append("\n");
        }
        if (metodoPagoNombre != null && !metodoPagoNombre.isEmpty()) {
            parametros.append("Método Pago: ").append(metodoPagoNombre).append("\n");
        }
        if (estadoVenta != null && !estadoVenta.isEmpty() && !estadoVenta.equals("todos")) {
            parametros.append("Estado Venta: ").append(estadoVenta).append("\n");
        }
        parametros.append("Registros: ").append(totalRegistros);
        
        reporte.setParametros(parametros.toString());
        
        reporteRepository.save(reporte);
        
        return "redirect:/reportes";
    }
    
    @PostMapping("/eliminar/{codigo}")
    public String eliminarReporte(@PathVariable String codigo) {
        reporteRepository.deleteByCodigo(codigo);
        return "redirect:/reportes";
    }
    
    @GetMapping("/descargar/{codigo}")
    public String descargarReporte(@PathVariable String codigo) {
        // Aquí implementar la lógica para descargar el archivo
        // Por ahora solo redirige
        return "redirect:/reportes";
    }

    @PostMapping("/guardar-desde-compras")
    public String guardarReporteDesdeCompras(
            @RequestParam String nombre,
            @RequestParam String formato,
            @RequestParam(required = false) String descripcion,
            @RequestParam String tipo,
            @RequestParam String fechaInicio,
            @RequestParam String horaInicio,
            @RequestParam String fechaFin,
            @RequestParam String horaFin,
            @RequestParam(required = false) String numeroFactura,
            @RequestParam(required = false) String rucProveedor,
            @RequestParam(required = false) String razonSocial,
            @RequestParam(required = false) Boolean tipoContado,
            @RequestParam(required = false) Boolean tipoCredito,
            @RequestParam(required = false) String estadoCompra,
            @RequestParam(required = false) Double totalMinimo,
            @RequestParam(required = false) Double totalMaximo,
            @RequestParam int totalRegistros) {
        
        Reporte reporte = new Reporte();
        reporte.setNombre(nombre);
        reporte.setTipo(tipo);
        reporte.setFormato(formato);
        reporte.setDescripcion(descripcion);
        reporte.setEstado("generado");
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setFechaCompletado(LocalDateTime.now());
        reporte.setRegistrosProcesados(totalRegistros);
        
        // Generar código único
        long siguienteNumero = reporteRepository.count() + 1;
        reporte.setCodigo("RPT" + String.format("%03d", siguienteNumero));
        
        // Guardar parámetros del filtro
        StringBuilder parametros = new StringBuilder();
        parametros.append("Período: ").append(fechaInicio).append(" ").append(horaInicio)
                .append(" - ").append(fechaFin).append(" ").append(horaFin).append("\n");
        if (numeroFactura != null && !numeroFactura.isEmpty()) {
            parametros.append("N° Factura: ").append(numeroFactura).append("\n");
        }
        if (rucProveedor != null && !rucProveedor.isEmpty()) {
            parametros.append("RUC Proveedor: ").append(rucProveedor).append("\n");
        }
        if (razonSocial != null && !razonSocial.isEmpty()) {
            parametros.append("Proveedor: ").append(razonSocial).append("\n");
        }
        if (tipoContado != null || tipoCredito != null) {
            parametros.append("Tipos: ");
            if (tipoContado != null && tipoContado) parametros.append("Contado ");
            if (tipoCredito != null && tipoCredito) parametros.append("Crédito");
            parametros.append("\n");
        }
        if (estadoCompra != null && !estadoCompra.isEmpty() && !estadoCompra.equals("todos")) {
            parametros.append("Estado: ").append(estadoCompra).append("\n");
        }
        if (totalMinimo != null && totalMinimo > 0) {
            parametros.append("Total Mínimo: S/ ").append(totalMinimo).append("\n");
        }
        if (totalMaximo != null && totalMaximo > 0) {
            parametros.append("Total Máximo: S/ ").append(totalMaximo).append("\n");
        }
        parametros.append("Registros: ").append(totalRegistros);
        
        reporte.setParametros(parametros.toString());
        
        reporteRepository.save(reporte);
        
        return "redirect:/reportes";
    }
}