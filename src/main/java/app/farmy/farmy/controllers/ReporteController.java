package app.farmy.farmy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import app.farmy.farmy.model.Reporte;
import app.farmy.farmy.model.TipoVenta;
import app.farmy.farmy.model.Usuario;
import app.farmy.farmy.model.Ventas;
import app.farmy.farmy.model.Compra;
import app.farmy.farmy.model.EstadoPago;
import app.farmy.farmy.model.Farmacia;
import app.farmy.farmy.repository.ReporteRepository;
import app.farmy.farmy.repository.VentasRepository;
import app.farmy.farmy.repository.CompraRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Controller
@RequestMapping("/reportes")
public class ReporteController {
    
    @Autowired
    private ReporteRepository reporteRepository;
    
    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private VentasRepository ventasRepository;
    
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
        HttpServletRequest request) {

        System.out.println("=== CREANDO NUEVO REPORTE DESDE MODAL ===");
        System.out.println("Nombre: " + nombre);
        System.out.println("Tipo: " + tipo);
        System.out.println("Formato: " + formato);
        System.out.println("Periodo: " + periodo);
        
        Reporte reporte = new Reporte();
        reporte.setNombre(nombre);
        reporte.setTipo(tipo);
        reporte.setFormato(formato);
        reporte.setEstado("proceso");
        reporte.setFechaGeneracion(LocalDateTime.now());
        
        long siguienteNumero = reporteRepository.count() + 1;
        reporte.setCodigo("RPT" + String.format("%03d", siguienteNumero));
        
        reporte.setDescripcion(descripcion);
        
        String parametros = "Período: " + periodo + "\n" +
                        "Fechas: " + fechaDesde + " - " + fechaHasta + "\n" +
                        "Estado inicial: " + estado;
        reporte.setParametros(parametros);
        
        Reporte reporteGuardado = reporteRepository.save(reporte);
        System.out.println("Reporte guardado ID: " + reporteGuardado.getId());
        System.out.println("Reporte código: " + reporteGuardado.getCodigo());
        
        if ("ventas".equals(tipo)) {
            return "redirect:/ventas/reportes?fromModal=true&nombre=" + nombre 
                + "&tipo=" + tipo + "&formato=" + formato 
                + "&periodo=" + periodo + "&estado=proceso"
                + "&fechaDesde=" + fechaDesde + "&fechaHasta=" + fechaHasta
                + "&reporteId=" + reporteGuardado.getId();
        } else if ("compras".equals(tipo)) {
            return "redirect:/compras/reportes?fromModal=true&nombre=" + nombre 
                + "&tipo=" + tipo + "&formato=" + formato 
                + "&periodo=" + periodo + "&estado=proceso"
                + "&fechaDesde=" + fechaDesde + "&fechaHasta=" + fechaHasta
                + "&reporteId=" + reporteGuardado.getId();
        } else {
            return "redirect:/reportes";
        }
    }

    private Farmacia getFarmaciaActual(HttpSession session) {
        Farmacia farmacia = (Farmacia) session.getAttribute("farmaciaActual");
        System.out.println("Farmacia en sesión: " + (farmacia != null ? 
                        "ID=" + farmacia.getId() + ", Nombre=" + farmacia.getNombreComercial() : 
                        "null"));
        return farmacia;
    }

    private Usuario getUsuarioActual(HttpSession session) {
        return (Usuario) session.getAttribute("usuario");
    }
    
    // En el método verReporteDetalles de ReporteController:
    @GetMapping("/ver/{codigo}")
    public String verReporteDetalles(@PathVariable String codigo, Model model, HttpSession session) {
        Reporte reporte = reporteRepository.findByCodigo(codigo);
        
        if (reporte == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reporte no encontrado");
        }
        
        model.addAttribute("reporte", reporte);
        
        BigDecimal totalGeneral = BigDecimal.ZERO;
        BigDecimal totalPendiente = BigDecimal.ZERO;
        BigDecimal totalPagado = BigDecimal.ZERO;
        BigDecimal totalContado = BigDecimal.ZERO;
        BigDecimal totalCredito = BigDecimal.ZERO;
        List<Compra> comprasMostrar = null;
        List<Ventas> ventasMostrar = null;
        
        if ("compras".equals(reporte.getTipo())) {
            // Para compras
            comprasMostrar = compraRepository.findAll();
            model.addAttribute("compras", comprasMostrar);
            model.addAttribute("tipoDatos", "compras");
            
            // Calcular totales para compras
            for (Compra compra : comprasMostrar) {
                if (compra.getTotal() != null) {
                    totalGeneral = totalGeneral.add(compra.getTotal());
                    
                    if (compra.getEstadoPago() != null) {
                        if (compra.getEstadoPago() == EstadoPago.PENDIENTE) {
                            totalPendiente = totalPendiente.add(compra.getTotal());
                        } else if (compra.getEstadoPago() == EstadoPago.PAGADO) {
                            totalPagado = totalPagado.add(compra.getTotal());
                        }
                    }
                }
            }
            
            // Agregar totales al modelo
            model.addAttribute("totalGeneral", totalGeneral);
            model.addAttribute("totalPendiente", totalPendiente);
            model.addAttribute("totalPagado", totalPagado);
            
        } else if ("ventas".equals(reporte.getTipo())) {
            // Para ventas - FILTRAR POR FARMACIA
            ventasMostrar = ventasRepository.findAll().stream()
                .filter(v -> v.getUsuario() != null && 
                            v.getUsuario().getFarmacia() != null &&
                            v.getUsuario().getFarmacia().getId() == getFarmaciaActual(session).getId())
                .collect(Collectors.toList());
            
            model.addAttribute("ventas", ventasMostrar);
            model.addAttribute("tipoDatos", "ventas");
            
            // Calcular totales para ventas
            for (Ventas venta : ventasMostrar) {
                if (venta.getTotal() != null) {
                    totalGeneral = totalGeneral.add(venta.getTotal());
                    
                    // Calcular por tipo de venta
                    if (venta.getTipoVenta() != null) {
                        if (venta.getTipoVenta() == TipoVenta.CONTADO) {
                            totalContado = totalContado.add(venta.getTotal());
                        } else if (venta.getTipoVenta() == TipoVenta.CREDITO) {
                            totalCredito = totalCredito.add(venta.getTotal());
                        }
                    }
                    
                    // Calcular por estado de pago
                    if (venta.getEstadoPago() != null) {
                        if (venta.getEstadoPago() == EstadoPago.PENDIENTE) {
                            totalPendiente = totalPendiente.add(venta.getTotal());
                        } else if (venta.getEstadoPago() == EstadoPago.PAGADO) {
                            totalPagado = totalPagado.add(venta.getTotal());
                        }
                    }
                }
            }
            
            // Agregar totales al modelo
            model.addAttribute("totalGeneral", totalGeneral);
            model.addAttribute("totalContado", totalContado);
            model.addAttribute("totalCredito", totalCredito);
            model.addAttribute("totalPendiente", totalPendiente);
            model.addAttribute("totalPagado", totalPagado);
            
        } else {
            model.addAttribute("tipoDatos", "otro");
        }
        
        // Parsear parámetros para mostrar en la vista
        if (reporte.getParametros() != null && !reporte.getParametros().isEmpty()) {
            Map<String, String> parametrosMap = new HashMap<>();
            String[] lineas = reporte.getParametros().split("\n");
            for (String linea : lineas) {
                String[] partes = linea.split(":", 2);
                if (partes.length == 2) {
                    parametrosMap.put(partes[0].trim(), partes[1].trim());
                }
            }
            model.addAttribute("parametros", parametrosMap);
        }
        
        return "home/reportes/detalle_reporte";
    }
    
    @PostMapping("/guardar")
    public String guardarReporte(@ModelAttribute Reporte reporte) {
        reporte.setCodigo("RPT" + String.format("%03d", reporteRepository.count() + 1));
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setEstado("proceso");
        
        reporteRepository.save(reporte);
        
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
        
        long siguienteNumero = reporteRepository.count() + 1;
        reporte.setCodigo("RPT" + String.format("%03d", siguienteNumero));
        
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
        return "redirect:/reportes";
    }

    @PostMapping("/guardar-desde-compras")
    public String guardarReporteDesdeCompras(
            @RequestParam String nombre,
            @RequestParam String tipo,
            @RequestParam String formato,
            @RequestParam(required = false) String descripcion,
            @RequestParam String fechaInicio,
            @RequestParam String horaInicio,
            @RequestParam String fechaFin,
            @RequestParam String horaFin,
            @RequestParam int totalRegistros) {
        
        System.out.println("=== GUARDANDO REPORTE DESDE COMPRAS ===");
        System.out.println("Nombre recibido: " + nombre);
        
        if (nombre == null || nombre.trim().isEmpty()) {
            nombre = "Reporte de Compras " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));
        }
        
        if (formato == null || formato.trim().isEmpty()) {
            formato = "pdf";
        }
        
        System.out.println("Nombre final: " + nombre);
        
        Reporte reporte = new Reporte();
        reporte.setNombre(nombre);
        reporte.setTipo(tipo != null ? tipo : "compras");
        reporte.setFormato(formato);
        reporte.setDescripcion(descripcion);
        reporte.setEstado("proceso");
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte.setFechaCompletado(null);
        reporte.setRegistrosProcesados(totalRegistros);
        
        long count = reporteRepository.count() + 1;
        reporte.setCodigo("RPT" + String.format("%03d", count));
        
        String parametros = "Período: " + fechaInicio + " " + horaInicio + 
                        " - " + fechaFin + " " + horaFin + "\n" +
                        "Registros: " + totalRegistros;
        reporte.setParametros(parametros);
        
        reporteRepository.save(reporte);
        
        System.out.println("Reporte guardado: " + reporte.getCodigo() + " - " + reporte.getNombre());
        return "redirect:/reportes";
    }

    // Añade estos métodos en ReporteController para soportar GET
    @GetMapping("/guardar-desde-ventas")
    public String guardarReporteDesdeVentasGet(
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
        
        return guardarReporteDesdeVentas(nombre, formato, descripcion, tipo,
                                        fechaInicio, horaInicio, fechaFin, horaFin,
                                        tipoDocumento, numeroDocumento, nombreCliente,
                                        metodoPagoNombre, estadoVenta, importeMinimo,
                                        importeMaximo, totalRegistros);
    }
}