package app.farmy.farmy.controllers;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpStatus;

import app.farmy.farmy.model.Compra;
import app.farmy.farmy.model.Proveedor;
import app.farmy.farmy.model.Reporte;
import app.farmy.farmy.model.EstadoPago;
import app.farmy.farmy.model.Lote;
import app.farmy.farmy.model.TipoCompra;
import app.farmy.farmy.repository.CompraRepository;
import app.farmy.farmy.repository.ProveedorRepository;
import app.farmy.farmy.security.FarmySesion;
import jakarta.servlet.http.HttpSession;
import app.farmy.farmy.repository.ReporteRepository;
//import jakarta.servlet.http.HttpServletRequest;
import app.farmy.farmy.repository.ProductosRepository;
import app.farmy.farmy.repository.LoteRepository;
import app.farmy.farmy.model.PagoCompra;
import app.farmy.farmy.repository.PagoCompraRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import app.farmy.farmy.repository.MetodoPagoRepository;

@Controller
@RequestMapping("/compras")
public class CompraController implements FarmySesion {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private PagoCompraRepository pagoCompraRepository;

    @Autowired
    private ReporteRepository reporteRepository; 

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping
    public String listaCompras(Model model, HttpSession session) {
        var compras = compraRepository.findAll().stream()
                .filter(arg0 -> arg0.getProveedor().getFarmacia().getId() == getFarmaciaActual(session).getId()).toList();
        System.out.println("Compras encontradas: " + compras.size());
        model.addAttribute("listaCompras", compras);
        model.addAttribute("metodosPago", metodoPagoRepository.findAll());
        model.addAttribute("pagoCompra", new PagoCompra());
        return "home/compras/compras";
    }

    @GetMapping("/ver/{id}")
    public String verCompra(@PathVariable int id, Model model) {
        Optional<Compra> c = compraRepository.findById(id);

        if (c.isPresent()) {
            Compra compra = c.get();
            List<Lote> detalles = compra.getLotes();
            model.addAttribute("compra", compra);

            model.addAttribute("detalles", detalles);
            return "home/compras/detalle_compra";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/anular")
    public String anularCompra(@RequestParam int numeroFactura, @RequestParam String motivo) {
        System.out.println(
                "\n".repeat(20) + "Anulando compra " + numeroFactura + " por motivo: " + motivo + "\n".repeat(20));
        var c = compraRepository.findById(numeroFactura);
        if (c.isPresent()) {
            Compra compra = c.get();
            compra.setEstado("Anulado");
            compra.setMotivoAnulacion(motivo);
            compra.setEstadoPago(EstadoPago.ANULADO);
            compraRepository.save(compra);
            return "redirect:/compras";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @GetMapping("/nueva")
    public String nuevaCompra(Model model, HttpSession session) {
        model.addAttribute("productos", productosRepository.findByFarmacia(getFarmaciaActual(session)));
        model.addAttribute("proveedores", proveedorRepository.findByFarmacia(getFarmaciaActual(session)));
        model.addAttribute("metodosPago", metodoPagoRepository.findAll());
        model.addAttribute("compra", new Compra());
        return "home/compras/nueva_compra";
    }

    @PostMapping("/guardar")
    public String guardarCompra(@RequestParam Integer proveedorId,
            @RequestParam Double subtotal,
            @RequestParam Double igv,
            @RequestParam Double total,
            @RequestParam int tipoCompra,
            @RequestParam(required = false) String estadoPago,
            @RequestParam(required = false) String fechaVencimientoPago,
            @RequestParam(required = false) Integer metodoPago,
            @RequestParam String itemsJson,
            @RequestParam(required = false, defaultValue = "0") Double montoPagoInicial,
            HttpSession session) {

        Compra compra = new Compra();

        if (proveedorId != null) {
            Proveedor p = proveedorRepository.findById(proveedorId).get();
            compra.setProveedor(p);
        }

        compra.setSubtotal(subtotal == null ? BigDecimal.ZERO : BigDecimal.valueOf(subtotal));
        compra.setSaldoPendiente(total == null ? BigDecimal.ZERO : BigDecimal.valueOf(total));
        compra.setIgv(igv == null ? BigDecimal.ZERO : BigDecimal.valueOf(igv));
        compra.setTotal(total == null ? BigDecimal.ZERO : BigDecimal.valueOf(total));

        compra.setTipoCompra(switch (tipoCompra) {
            case 1 -> {
                compra.setEstadoPago(EstadoPago.PENDIENTE);
                yield TipoCompra.CREDITO;
            }
            default -> {
                compra.setEstadoPago(EstadoPago.PAGADO);
                yield TipoCompra.CONTADO;
            }
        });

        if (fechaVencimientoPago != null && fechaVencimientoPago != "") {
            compra.setFechaVencimientoPago(LocalDate.parse(fechaVencimientoPago));
        }

        if (metodoPago != null) {
            metodoPagoRepository.findById(metodoPago).ifPresent(compra::setMetodoPago);
        }

        // Handle initial payment for CREDIT purchases
        if (compra.getTipoCompra() == TipoCompra.CREDITO && montoPagoInicial != null && montoPagoInicial > 0) {

            if (BigDecimal.valueOf(montoPagoInicial).compareTo(compra.getTotal()) > 0) {
                montoPagoInicial = compra.getTotal().doubleValue();
            }

            BigDecimal nuevoSaldo = compra.getSaldoPendiente().subtract(BigDecimal.valueOf(montoPagoInicial));
            compra.setSaldoPendiente(nuevoSaldo);

            if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
                compra.setEstadoPago(EstadoPago.PAGADO);
            }
        }

        if (compra.getTipoCompra() == TipoCompra.CONTADO) {
            compra.setSaldoPendiente(BigDecimal.ZERO);
            compra.setEstadoPago(EstadoPago.PAGADO);
        }
        /*
         * Ejemplo del JSON
         * 
         * 
         * 
         * 
         * 
         * Esta es el
         * json[{"id":2,"cantidad":15,"precio_venta":13,"precio_compra":12,"lote":"1414"
         * ,"fechaVencimiento":"2025-12-05", "fechaFabricacion":"2025-12-05"}]
         * 
         * 
         */
        // si vienen items en JSON, parsearlos y persistir CompraDetalle
        if (itemsJson != null && !itemsJson.isBlank()) {
            try {
                List<Map<String, Object>> items = mapper.readValue(itemsJson,
                        new TypeReference<List<Map<String, Object>>>() {
                        });

                compra.setUsuario(getUsuarioActual(session));
                compraRepository.save(compra);

                for (Map<String, Object> it : items) {

                    int idProd = (Integer) (it.get("id"));
                    String idLote = it.get("lote").toString();
                    int cantidad = (it.get("cantidad") != null) ? ((Integer) it.get("cantidad")) : 0;
                    Double precio_venta = (it.get("precio_venta") != null)
                            ? ((Number) it.get("precio_venta")).doubleValue()
                            : 0.0;
                    Double precio_compra = (it.get("precio_compra") != null)
                            ? ((Number) it.get("precio_compra")).doubleValue()
                            : 0.0;
                    LocalDate fecha_vencimiento = LocalDate.parse(
                            it.get("fechaVencimiento").toString() != "" ? it.get("fechaVencimiento").toString() : null);
                    LocalDate fecha_fabricacion = LocalDate.parse(
                            it.get("fechaFabricacion").toString() != "" ? it.get("fechaFabricacion").toString() : null);

                    Lote lote = new Lote();
                    lote.setNumeroLote(idLote);
                    lote.setEstado("Activo");
                    lote.setCantidadInicial(cantidad);
                    lote.setCantidadActual(cantidad);
                    lote.setPrecioCompra(precio_compra);
                    lote.setPrecioVenta(precio_venta);
                    lote.setFechaVencimiento(fecha_vencimiento);
                    lote.setFechaFabricacion(fecha_fabricacion);

                    lote.setProducto(productosRepository.findById(idProd).get());

                    loteRepository.save(lote);

                    compra.getLotes().add(lote);

                    compraRepository.save(compra);
                }

                if (compra.getTipoCompra() == TipoCompra.CREDITO && montoPagoInicial != null && montoPagoInicial > 0) {
                    PagoCompra pago = new PagoCompra();
                    pago.setCompra(compra);
                    pago.setMontoPago(BigDecimal.valueOf(montoPagoInicial));
                    pago.setFechaPago(LocalDateTime.now());
                    pago.setMetodoPago(compra.getMetodoPago());
                    pago.setUsuario(getUsuarioActual(session));
                    pago.setObservaciones("Pago inicial al registrar compra");

                    pagoCompraRepository.save(pago);
                }

            } catch (Exception ex) {
                // parsing error -> ignore or log
                ex.printStackTrace();
            }
        }

        return "redirect:/compras";
    }

    @GetMapping("/reportes")
    public String reporteCompras(
        @RequestParam(required = false) String fechaInicio,
        @RequestParam(required = false) String horaInicio,
        @RequestParam(required = false) String fechaFin,
        @RequestParam(required = false) String horaFin,
        @RequestParam(required = false) String numeroFactura,
        @RequestParam(required = false) String rucProveedor,
        @RequestParam(required = false) String razonSocial,
        @RequestParam(required = false) Boolean tipoContado,
        @RequestParam(required = false) Boolean tipoCredito,
        @RequestParam(required = false) String estadoCompra,
        @RequestParam(required = false) Double totalMinimo,
        @RequestParam(required = false) Double totalMaximo,
        @RequestParam(required = false) String ordenarPor,
        @RequestParam(required = false) String orden,
        // Parámetros del modal
        @RequestParam(required = false) Boolean fromModal,
        @RequestParam(required = false) String nombre,
        @RequestParam(required = false) String tipo,
        @RequestParam(required = false) String formato,
        @RequestParam(required = false) String periodo,
        @RequestParam(required = false) String estado,
        @RequestParam(required = false) String fechaDesde,
        @RequestParam(required = false) String fechaHasta,
        @RequestParam(required = false) String descripcion,
        @RequestParam(required = false) Long reporteId,  // <-- Este es importante
        Model model) {
        
        // Hacer copias locales de las variables para usar en el lambda
        final String fechaInicioFinal = fechaInicio;
        final String horaInicioFinal = horaInicio;
        final String fechaFinFinal = fechaFin;
        final String horaFinFinal = horaFin;
        final String numeroFacturaFinal = numeroFactura;
        final String rucProveedorFinal = rucProveedor;
        final String razonSocialFinal = razonSocial;
        final Boolean tipoContadoFinal = tipoContado;
        final Boolean tipoCreditoFinal = tipoCredito;
        final String estadoCompraFinal = estadoCompra;
        final Double totalMinimoFinal = totalMinimo;
        final Double totalMaximoFinal = totalMaximo;

        // Establecer fechas desde el modal si vienen de allí
        if (fromModal != null && fromModal) {
            if (fechaInicioFinal == null && fechaDesde != null) {
                fechaInicio = fechaDesde;
                horaInicio = "00:00";
            }
            if (fechaFinFinal == null && fechaHasta != null) {
                fechaFin = fechaHasta;
                horaFin = "23:59";
            }
            
            // Guardar datos del modal para mostrar en la vista
            Map<String, Object> reporteData = new HashMap<>();
            reporteData.put("nombre", nombre);
            reporteData.put("tipo", tipo);
            reporteData.put("formato", formato);
            reporteData.put("periodo", periodo);
            reporteData.put("estado", estado);
            reporteData.put("fechaDesde", fechaDesde);
            reporteData.put("fechaHasta", fechaHasta);
            
            model.addAttribute("reporteData", reporteData);
            model.addAttribute("fromModal", true);
        }

        // Si viene con reporteId, mostrarlo en logs
        if (reporteId != null) {
            System.out.println("=== CARGANDO REPORTE EXISTENTE ===");
            System.out.println("Reporte ID: " + reporteId);
            System.out.println("Nombre: " + nombre);
            System.out.println("Tipo: " + tipo);
            System.out.println("Desde modal: " + fromModal);
            
            // Pasar el reporteId al modelo
            model.addAttribute("reporteId", reporteId);
            
            // También cargar el reporte desde la BD para verificar
            Optional<Reporte> reporteExistente = reporteRepository.findById(reporteId);
            if (reporteExistente.isPresent()) {
                System.out.println("Reporte encontrado: " + reporteExistente.get().getNombre());
                model.addAttribute("nombre", reporteExistente.get().getNombre());
                model.addAttribute("formato", reporteExistente.get().getFormato());
                model.addAttribute("descripcion", reporteExistente.get().getDescripcion());
            }
        }
        
        // Obtener todas las compras
        List<Compra> compras = compraRepository.findAll();
        
        // Aplicar filtros
        List<Compra> comprasFiltradas = compras.stream()
            .filter(c -> {
                boolean pasaFiltro = true;
                
                // Filtrar por fecha
                if (fechaInicioFinal != null && !fechaInicioFinal.isEmpty() && 
                    fechaFinFinal != null && !fechaFinFinal.isEmpty()) {
                    try {
                        LocalDateTime fechaInicioCompleta = LocalDateTime.parse(
                            fechaInicioFinal + "T" + (horaInicioFinal != null ? horaInicioFinal : "00:00"));
                        LocalDateTime fechaFinCompleta = LocalDateTime.parse(
                            fechaFinFinal + "T" + (horaFinFinal != null ? horaFinFinal : "23:59"));
                        
                        if (c.getFechaFactura() != null) {
                            pasaFiltro = pasaFiltro && 
                                    !c.getFechaFactura().isBefore(fechaInicioCompleta) && 
                                    !c.getFechaFactura().isAfter(fechaFinCompleta);
                        }
                    } catch (Exception e) {
                        // Si hay error en el parsing, ignorar filtro de fecha
                    }
                }
                
                // Filtrar por número de factura
                if (numeroFacturaFinal != null && !numeroFacturaFinal.isEmpty()) {
                    pasaFiltro = pasaFiltro && String.valueOf(c.getNumeroFactura()).contains(numeroFacturaFinal);
                }
                
                // Filtrar por RUC proveedor
                if (rucProveedorFinal != null && !rucProveedorFinal.isEmpty() && c.getProveedor() != null) {
                    pasaFiltro = pasaFiltro && c.getProveedor().getRuc().contains(rucProveedorFinal);
                }
                
                // Filtrar por razón social
                if (razonSocialFinal != null && !razonSocialFinal.isEmpty() && c.getProveedor() != null) {
                    pasaFiltro = pasaFiltro && c.getProveedor().getRazonSocial().toLowerCase().contains(razonSocialFinal.toLowerCase());
                }
                
                // Filtrar por tipo de compra
                if (tipoContadoFinal != null || tipoCreditoFinal != null) {
                    boolean pasaTipo = true;
                    if (tipoContadoFinal != null && tipoContadoFinal && tipoCreditoFinal != null && tipoCreditoFinal) {
                        // Ambos seleccionados, mostrar todos
                    } else if (tipoContadoFinal != null && tipoContadoFinal) {
                        pasaTipo = c.getTipoCompra() == TipoCompra.CONTADO;
                    } else if (tipoCreditoFinal != null && tipoCreditoFinal) {
                        pasaTipo = c.getTipoCompra() == TipoCompra.CREDITO;
                    }
                    pasaFiltro = pasaFiltro && pasaTipo;
                }
                
                // Filtrar por estado de compra
                if (estadoCompraFinal != null && !estadoCompraFinal.isEmpty() && !estadoCompraFinal.equals("todos")) {
                    if (c.getEstadoPago() != null) {
                        pasaFiltro = pasaFiltro && c.getEstadoPago().name().equals(estadoCompraFinal);
                    } else {
                        pasaFiltro = false;
                    }
                }
                
                // Filtrar por total mínimo
                if (totalMinimoFinal != null && totalMinimoFinal > 0 && c.getTotal() != null) {
                    pasaFiltro = pasaFiltro && c.getTotal().doubleValue() >= totalMinimoFinal;
                }
                
                // Filtrar por total máximo
                if (totalMaximoFinal != null && totalMaximoFinal > 0 && c.getTotal() != null) {
                    pasaFiltro = pasaFiltro && c.getTotal().doubleValue() <= totalMaximoFinal;
                }
                
                return pasaFiltro;
            })
            .sorted((c1, c2) -> {
                // Ordenar según criterio seleccionado
                String ordenPor = ordenarPor != null ? ordenarPor : "fecha";
                boolean descendente = orden != null && orden.equals("desc");
                
                int resultado = 0;
                
                switch (ordenPor) {
                    case "codigo":
                        resultado = Integer.compare(c1.getNumeroFactura(), c2.getNumeroFactura());
                        break;
                    case "total":
                        resultado = c1.getTotal().compareTo(c2.getTotal());
                        break;
                    case "proveedor":
                        String prov1 = c1.getProveedor() != null ? c1.getProveedor().getRazonSocial() : "";
                        String prov2 = c2.getProveedor() != null ? c2.getProveedor().getRazonSocial() : "";
                        resultado = prov1.compareTo(prov2);
                        break;
                    case "fecha":
                    default:
                        resultado = c1.getFechaFactura().compareTo(c2.getFechaFactura());
                        break;
                }
                
                return descendente ? -resultado : resultado;
            })
            .collect(Collectors.toList());
        
        // Calcular totales en Java (no en Thymeleaf)
        BigDecimal totalGeneral = BigDecimal.ZERO;
        BigDecimal totalPendiente = BigDecimal.ZERO;
        BigDecimal totalPagado = BigDecimal.ZERO;
        
        for (Compra compra : comprasFiltradas) {
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
        
        // Agregar datos al modelo
        model.addAttribute("compras", comprasFiltradas);
        model.addAttribute("proveedores", proveedorRepository.findAll());
        model.addAttribute("metodosPago", metodoPagoRepository.findAll());
        
        // Agregar totales al modelo
        model.addAttribute("totalGeneral", totalGeneral);
        model.addAttribute("totalPendiente", totalPendiente);
        model.addAttribute("totalPagado", totalPagado);
        
        // Mantener valores de filtros
        model.addAttribute("fechaInicio", fechaInicio != null ? fechaInicio : LocalDate.now().minusDays(30).toString());
        model.addAttribute("horaInicio", horaInicio != null ? horaInicio : "00:00");
        model.addAttribute("fechaFin", fechaFin != null ? fechaFin : LocalDate.now().toString());
        model.addAttribute("horaFin", horaFin != null ? horaFin : "23:59");
        model.addAttribute("numeroFactura", numeroFactura != null ? numeroFactura : "");
        model.addAttribute("rucProveedor", rucProveedor != null ? rucProveedor : "");
        model.addAttribute("razonSocial", razonSocial != null ? razonSocial : "");
        model.addAttribute("tipoContado", tipoContado != null ? tipoContado : true);
        model.addAttribute("tipoCredito", tipoCredito != null ? tipoCredito : true);
        model.addAttribute("estadoCompraSeleccionado", estadoCompra != null ? estadoCompra : "todos");
        model.addAttribute("totalMinimo", totalMinimo != null ? totalMinimo : "");
        model.addAttribute("totalMaximo", totalMaximo != null ? totalMaximo : "");
        model.addAttribute("ordenarPor", ordenarPor != null ? ordenarPor : "fecha");
        model.addAttribute("orden", orden != null ? orden : "desc");
        
        return "home/reportes/reporte_compras";
    }

    @PostMapping("/reportes/generar")
    public String generarReporteSimple(
        @RequestParam(required = false) String nombreReporte,
        @RequestParam(required = false) String tipoReporte,
        @RequestParam(required = false) String formato,
        @RequestParam(required = false) String descripcion,
        @RequestParam(required = false) String fechaInicio,
        @RequestParam(required = false) String horaInicio,
        @RequestParam(required = false) String fechaFin,
        @RequestParam(required = false) String horaFin,
        @RequestParam(required = false) Integer totalRegistros,
        @RequestParam(required = false) Long reporteId,  // ← AÑADIR este parámetro
        RedirectAttributes redirectAttributes) {
        
        System.out.println("=== GENERANDO REPORTE ===");
        System.out.println("Nombre: " + (nombreReporte != null ? nombreReporte : "(vacío)"));
        System.out.println("Formato: " + (formato != null ? formato : "(vacío)"));
        System.out.println("Reporte ID recibido: " + reporteId);
        
        // Si ya existe un reporteId, actualizar ese reporte en lugar de crear uno nuevo
        if (reporteId != null && reporteId > 0) {
            try {
                Optional<Reporte> reporteExistente = reporteRepository.findById(reporteId);
                if (reporteExistente.isPresent()) {
                    Reporte reporte = reporteExistente.get();
                    
                    // Actualizar los datos del reporte existente
                    if (nombreReporte != null && !nombreReporte.trim().isEmpty()) {
                        reporte.setNombre(nombreReporte);
                    }
                    if (formato != null && !formato.trim().isEmpty()) {
                        reporte.setFormato(formato);
                    }
                    if (descripcion != null) {
                        reporte.setDescripcion(descripcion);
                    }
                    reporte.setEstado("generado");
                    reporte.setFechaCompletado(LocalDateTime.now());
                    if (totalRegistros != null) {
                        reporte.setRegistrosProcesados(totalRegistros);
                    }
                    
                    // Actualizar parámetros
                    String parametros = "Período: " + fechaInicio + " " + horaInicio + 
                                    " - " + fechaFin + " " + horaFin + "\n" +
                                    "Registros: " + (totalRegistros != null ? totalRegistros : 0);
                    reporte.setParametros(parametros);
                    
                    reporteRepository.save(reporte);
                    System.out.println("Reporte actualizado ID: " + reporteId);
                    
                    redirectAttributes.addFlashAttribute("mensaje", "Reporte actualizado exitosamente");
                    return "redirect:/reportes";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Si no hay reporteId, crear uno nuevo (flujo original)
        if (nombreReporte == null || nombreReporte.trim().isEmpty()) {
            nombreReporte = "Reporte de Compras " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
        
        if (formato == null || formato.trim().isEmpty()) {
            formato = "pdf";
        }
        
        if (totalRegistros == null) {
            totalRegistros = 0;
        }
        
        try {
            String redirectUrl = "/reportes/guardar-desde-compras?" +
                            "nombre=" + URLEncoder.encode(nombreReporte, "UTF-8") +
                            "&tipo=" + (tipoReporte != null ? tipoReporte : "compras") +
                            "&formato=" + formato +
                            "&fechaInicio=" + (fechaInicio != null ? fechaInicio : "") +
                            "&horaInicio=" + (horaInicio != null ? horaInicio : "00:00") +
                            "&fechaFin=" + (fechaFin != null ? fechaFin : "") +
                            "&horaFin=" + (horaFin != null ? horaFin : "23:59") +
                            "&totalRegistros=" + totalRegistros;
            
            if (descripcion != null && !descripcion.isEmpty()) {
                redirectUrl += "&descripcion=" + URLEncoder.encode(descripcion, "UTF-8");
            }
            
            System.out.println("Redirigiendo a: " + redirectUrl);
            return "redirect:" + redirectUrl;
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al generar reporte: " + e.getMessage());
            return "redirect:/compras/reportes";
        }
    }
}