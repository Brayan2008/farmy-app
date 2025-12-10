package app.farmy.farmy.controllers;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.farmy.farmy.model.Cliente;
import app.farmy.farmy.model.EstadoPago;
import app.farmy.farmy.model.Farmacia;
import app.farmy.farmy.model.InventarioMovimiento;
import app.farmy.farmy.model.InventarioMovimiento.TipoMovimiento;
import app.farmy.farmy.model.Lote;
import app.farmy.farmy.model.Reporte;
import app.farmy.farmy.model.TipoVenta;
import app.farmy.farmy.model.VentaDetalle;
import app.farmy.farmy.model.VentaPago;
import app.farmy.farmy.model.Ventas;
import app.farmy.farmy.repository.ClienteRepository;
import app.farmy.farmy.repository.InventarioMovimientoRepository;
import app.farmy.farmy.repository.LoteRepository;
import app.farmy.farmy.repository.MetodoPagoRepository;
import app.farmy.farmy.repository.ReporteRepository;
import app.farmy.farmy.repository.VentaDetalleRepository;
import app.farmy.farmy.repository.VentaPagoRepository;
import app.farmy.farmy.repository.VentasRepository;
import app.farmy.farmy.security.FarmySesion;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
//import java.time.format.DateTimeFormatter;
//import java.util.Comparator;
import java.util.HashMap;

@Controller
@RequestMapping("/ventas")
public class VentaController implements FarmySesion{

    @Autowired
    private VentasRepository ventasRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private VentaDetalleRepository ventaDetalleRepository;

    @Autowired
    private VentaPagoRepository ventaPagoRepository;

    @Autowired
    private InventarioMovimientoRepository inventarioMovimientoRepository;

    @Autowired
    private ReporteRepository reporteRepository; 

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping
    public String listaVentas(Model model, HttpSession session) {
        model.addAttribute("listaVentas", ventasRepository.findAll().stream().filter(v -> v.getUsuario().getFarmacia().getId() == getFarmaciaActual(session).getId()).toList());
        model.addAttribute("metodosPago", metodoPagoRepository.findAll());
        model.addAttribute("ventaPago", new VentaPago());
        model.addAttribute("estado_caja_usuario",false);
        return "home/ventas/ventas";
    }

    @GetMapping("/ver/{id}")
    public String verVenta(@PathVariable int id, Model model) {
        Optional<Ventas> v = ventasRepository.findById(id);

        if (v.isPresent()) {
            Ventas venta = v.get();
            List<VentaDetalle> detalles = venta.getVentaDetalles();
            model.addAttribute("venta", venta);
            model.addAttribute("detalles", detalles);
            return "home/ventas/detalle_venta";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/anular")
    public String anularVenta(@RequestParam int idVenta, @RequestParam String motivo) {
        var v = ventasRepository.findById(idVenta);
        if (v.isPresent()) {
            Ventas venta = v.get();

            if ("Anulado".equals(venta.getEstado())) {
                return "redirect:/ventas";
            }

            venta.setEstado("Anulado");
            venta.setMotivoAnulacion(motivo);
            venta.setEstadoPago(EstadoPago.ANULADO);

            // Restore stock
            for (VentaDetalle detalle : venta.getVentaDetalles()) {
                Lote lote = detalle.getLote();
                lote.setCantidadActual(lote.getCantidadActual() + detalle.getCantidad());
                loteRepository.save(lote);

                // Register movement
                InventarioMovimiento mov = new InventarioMovimiento();
                mov.setLote(lote);
                mov.setTipoMovimiento(TipoMovimiento.ENTRADA);
                mov.setCantidad(detalle.getCantidad());
                mov.setStockAnterior(lote.getCantidadActual() - detalle.getCantidad());
                mov.setStockNuevo(lote.getCantidadActual());
                mov.setObservaciones("Anulación de venta #" + venta.getIdVenta());
                mov.setUsuario(venta.getUsuario()); // Assuming user is linked
                inventarioMovimientoRepository.save(mov);
            }

            ventasRepository.save(venta);
        }
        return "redirect:/ventas";
    }

    @GetMapping("/nueva")
    public String nuevaVenta(Model model, HttpSession session) {
        model.addAttribute("venta", new Ventas());
        model.addAttribute("clientes", clienteRepository.findByFarmacia(getFarmaciaActual(session)));

        // Filter products that have at least one active lote with stock > 0
        List<Lote> lotesConStock = loteRepository.findAll()
                .stream()
                .filter(l -> "Activo".equals(l.getEstado()) && l.getCantidadActual() > 0 && l.getProducto().getFarmacia().getId() == getFarmaciaActual(session).getId())
                .collect(Collectors.toList());

        lotesConStock.forEach(arg0 -> System.out.println(arg0.getIdLote() + " - " + arg0.getProducto().getNombreProducto() + " - Stock: " + arg0.getCantidadActual())); //TODO borrar esta linea despues de probar

        System.out.println("\n".repeat(20) + "Productos disponibles para venta: " + lotesConStock.size() + " productos."
                + "\n".repeat(20));

        model.addAttribute("lotes", lotesConStock);
        model.addAttribute("metodosPago", metodoPagoRepository.findAll());
        return "home/ventas/nueva_venta";
    }

    @PostMapping("/guardar")
    public String guardarVenta(@RequestParam Integer idCliente,
            @RequestParam Double subtotal,
            @RequestParam Double igv,
            @RequestParam Double total,
            @RequestParam String tipoVenta,
            @RequestParam(required = false) String fechaVencimientoPago,
            @RequestParam(required = false) Integer metodoPago,
            @RequestParam String itemsJson,
            @RequestParam(required = false) Double montoPagoInicial,
            HttpSession session) {

        Ventas venta = new Ventas();

        if (idCliente != null) {
            Cliente c = clienteRepository.findById(idCliente).orElse(null);
            venta.setCliente(c);
        }

        venta.setSubTotal(subtotal == null ? BigDecimal.ZERO : BigDecimal.valueOf(subtotal));
        venta.setIgv(igv == null ? BigDecimal.ZERO : BigDecimal.valueOf(igv));
        venta.setTotal(total == null ? BigDecimal.ZERO : BigDecimal.valueOf(total));
        venta.setSaldoPendiente(total == null ? BigDecimal.ZERO : BigDecimal.valueOf(total));
        venta.setEstado("Activo");

        venta.setTipoVenta(switch (TipoVenta.valueOf(tipoVenta)) {
            case CREDITO -> {
                venta.setEstadoPago(EstadoPago.PENDIENTE);
                yield TipoVenta.CREDITO;
            }
            default -> {
                venta.setEstadoPago(EstadoPago.PAGADO);
                yield TipoVenta.CONTADO;
            }
        });

        if (fechaVencimientoPago != null && !fechaVencimientoPago.isEmpty()) {
            venta.setFechaVencimientoPago(LocalDateTime.parse(fechaVencimientoPago + "T23:59:59"));
        }

        if (metodoPago != null) {
            metodoPagoRepository.findById(metodoPago).ifPresent(venta::setMetodoPago);
        }



        // Handle initial payment or full payment logic
        if (venta.getTipoVenta() == TipoVenta.CREDITO) {
            if (montoPagoInicial != null && montoPagoInicial > 0) {
                System.out.println("Procesando pago inicial de: " + montoPagoInicial + "\n".repeat(10));
                BigDecimal nuevoSaldo = venta.getSaldoPendiente().subtract(BigDecimal.valueOf(montoPagoInicial));
                venta.setSaldoPendiente(nuevoSaldo);
                if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
                    venta.setEstadoPago(EstadoPago.PAGADO);
                }
            }
        } else {
            // Contado implies fully paid
            venta.setSaldoPendiente(BigDecimal.ZERO);
            venta.setEstadoPago(EstadoPago.PAGADO);
        }
        venta.setFarmacia(getFarmaciaActual(session));
        venta.setUsuario(getUsuarioActual(session));
        ventasRepository.save(venta);

        // Process items
        if (itemsJson != null && !itemsJson.isBlank()) {
            try {
                List<Map<String, Object>> items = mapper.readValue(itemsJson,
                        new TypeReference<List<Map<String, Object>>>() {
                        });

                for (Map<String, Object> it : items) {
                    String idLote = it.get("lote").toString();
                    int cantidad = (Integer) it.get("cantidad");

                    Lote lote = loteRepository.findByNumeroLote(idLote).get(0);

                    // Validate stock again
                    if (lote.getCantidadActual() < cantidad) {
                        throw new RuntimeException("Stock insuficiente para lote " + lote.getNumeroLote());
                    }

                    // Update stock
                    lote.setCantidadActual(lote.getCantidadActual() - cantidad);
                    loteRepository.save(lote);

                    // Create Detail
                    VentaDetalle detalle = new VentaDetalle();
                    detalle.setVenta(venta);
                    detalle.setLote(lote);
                    detalle.setCantidad(cantidad);
                    ventaDetalleRepository.save(detalle);
                }

                double montoPago = 0;
                if (venta.getTipoVenta() == TipoVenta.CONTADO) {
                    montoPago = venta.getTotal().doubleValue();
                } else if (montoPagoInicial != null && montoPagoInicial > 0) {
                    montoPago = montoPagoInicial;
                }

                if (montoPago > 0) {
                    VentaPago pago = new VentaPago();
                    pago.setVenta(venta);
                    pago.setMonto(BigDecimal.valueOf(montoPago));
                    pago.setFechaPago(LocalDateTime.now());
                    pago.setMetodoPago(venta.getMetodoPago());
                    ventaPagoRepository.save(pago);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return "redirect:/ventas";
    }

    // En VentaController.java, modifica el método reporteVentas:
    @GetMapping("/reportes")
    public String reporteVentas(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String horaInicio,
            @RequestParam(required = false) String fechaFin,
            @RequestParam(required = false) String horaFin,
            @RequestParam(required = false) String tipoDocumento,
            @RequestParam(required = false) String numeroDocumento,
            @RequestParam(required = false) String nombreCliente,
            @RequestParam(required = false) String metodoPagoNombre,
            @RequestParam(required = false) String estadoVenta,
            @RequestParam(required = false) Double importeMinimo,
            @RequestParam(required = false) Double importeMaximo,
            @RequestParam(required = false) String ordenarPor,
            @RequestParam(required = false) String orden,
            // Nuevos parámetros del modal
            @RequestParam(required = false) Boolean fromModal,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String formato,
            @RequestParam(required = false) String periodo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(required = false) String descripcion,
            @RequestParam(required = false) Long reporteId,
            Model model, HttpSession session) {
        
        System.out.println("=== FILTROS RECIBIDOS VENTAS ===");
        System.out.println("fechaInicio: " + fechaInicio);
        System.out.println("horaInicio: " + horaInicio);
        System.out.println("fechaFin: " + fechaFin);
        System.out.println("horaFin: " + horaFin);
        System.out.println("tipoDocumento: " + tipoDocumento);
        System.out.println("numeroDocumento: " + numeroDocumento);
        System.out.println("nombreCliente: " + nombreCliente);
        System.out.println("metodoPagoNombre: " + metodoPagoNombre);
        System.out.println("estadoVenta: " + estadoVenta);
        System.out.println("importeMinimo: " + importeMinimo);
        System.out.println("importeMaximo: " + importeMaximo);
        System.out.println("ordenarPor: " + ordenarPor);
        System.out.println("orden: " + orden);
        System.out.println("fromModal: " + fromModal);
        System.out.println("reporteId: " + reporteId);
        System.out.println("===============================");
        
        // Obtener farmacia actual
        Farmacia farmaciaActual = getFarmaciaActual(session);
        System.out.println("Farmacia actual ID: " + farmaciaActual.getId() + ", Nombre: " + farmaciaActual.getNombreComercial());
        
        // Crear copias finales para usar en el lambda
        final String fechaInicioFinal = fechaInicio;
        final String horaInicioFinal = horaInicio;
        final String fechaFinFinal = fechaFin;
        final String horaFinFinal = horaFin;
        final String tipoDocumentoFinal = tipoDocumento;
        final String numeroDocumentoFinal = numeroDocumento;
        final String nombreClienteFinal = nombreCliente;
        final String metodoPagoNombreFinal = metodoPagoNombre;
        final String estadoVentaFinal = estadoVenta;
        final Double importeMinimoFinal = importeMinimo;
        final Double importeMaximoFinal = importeMaximo;
        final String ordenarPorFinal = ordenarPor;
        final String ordenFinal = orden;
        
        // Establecer fechas desde el modal si vienen de allí
        if (fromModal != null && fromModal) {
            System.out.println("Viene desde modal");
            if (fechaInicio == null && fechaDesde != null) {
                fechaInicio = fechaDesde;
                horaInicio = "00:00";
                System.out.println("Fecha inicio desde modal: " + fechaInicio);
            }
            if (fechaFin == null && fechaHasta != null) {
                fechaFin = fechaHasta;
                horaFin = "23:59";
                System.out.println("Fecha fin desde modal: " + fechaFin);
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
            reporteData.put("descripcion", descripcion);
            
            model.addAttribute("reporteData", reporteData);
            model.addAttribute("fromModal", true);
        }

        // Si viene con reporteId, mostrarlo
        if (reporteId != null) {
            System.out.println("=== CARGANDO REPORTE VENTAS EXISTENTE ===");
            System.out.println("Reporte ID: " + reporteId);
            System.out.println("Nombre: " + nombre);
            
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
        
        // Obtener todas las ventas de la farmacia actual
        System.out.println("Obteniendo ventas para farmacia ID: " + farmaciaActual.getId());
        List<Ventas> ventas = ventasRepository.findAll().stream()
            .filter(v -> {
                boolean perteneceAFarmacia = v.getUsuario() != null && 
                            v.getUsuario().getFarmacia() != null &&
                            v.getUsuario().getFarmacia().getId() == farmaciaActual.getId();
                
                if (!perteneceAFarmacia) {
                    System.out.println("Venta ID " + v.getIdVenta() + " NO pertenece a farmacia actual");
                    if (v.getUsuario() == null) {
                        System.out.println("  - Usuario es null");
                    } else if (v.getUsuario().getFarmacia() == null) {
                        System.out.println("  - Farmacia de usuario es null");
                    } else {
                        System.out.println("  - Farmacia venta ID: " + v.getUsuario().getFarmacia().getId() + 
                                        ", Farmacia actual ID: " + farmaciaActual.getId());
                    }
                }
                
                return perteneceAFarmacia;
            })
            .collect(Collectors.toList());
        
        System.out.println("Total ventas encontradas: " + ventas.size());
        
        // Aplicar filtros (usar las variables FINAL en el lambda)
        List<Ventas> ventasFiltradas = ventas.stream()
            .filter(v -> {
                boolean pasaFiltro = true;
                
                // Filtrar por fecha
                if (fechaInicioFinal != null && !fechaInicioFinal.isEmpty() && 
                    fechaFinFinal != null && !fechaFinFinal.isEmpty()) {
                    try {
                        LocalDateTime fechaInicioCompleta = LocalDateTime.parse(
                            fechaInicioFinal + "T" + (horaInicioFinal != null ? horaInicioFinal : "00:00"));
                        LocalDateTime fechaFinCompleta = LocalDateTime.parse(
                            fechaFinFinal + "T" + (horaFinFinal != null ? horaFinFinal : "23:59"));
                        
                        if (v.getFechaVenta() != null) {
                            boolean fechaValida = !v.getFechaVenta().isBefore(fechaInicioCompleta) && 
                                                !v.getFechaVenta().isAfter(fechaFinCompleta);
                            
                            if (!fechaValida) {
                                System.out.println("Venta ID " + v.getIdVenta() + " fuera de rango de fecha: " + 
                                                v.getFechaVenta() + " no está entre " + fechaInicioCompleta + 
                                                " y " + fechaFinCompleta);
                            }
                            
                            pasaFiltro = pasaFiltro && fechaValida;
                        } else {
                            System.out.println("Venta ID " + v.getIdVenta() + " tiene fechaVenta null");
                            pasaFiltro = false;
                        }
                    } catch (Exception e) {
                        // Si hay error en el parsing, ignorar filtro de fecha
                        System.out.println("Error parsing fecha: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                
                // Filtrar por tipo documento
                if (tipoDocumentoFinal != null && !tipoDocumentoFinal.isEmpty() && !tipoDocumentoFinal.equals("todos")) {
                    boolean tipoDocValido = v.getCliente() != null && 
                            v.getCliente().getTipoDocumento() != null &&
                            v.getCliente().getTipoDocumento().name().equals(tipoDocumentoFinal);
                    
                    if (!tipoDocValido) {
                        System.out.println("Venta ID " + v.getIdVenta() + " no pasa filtro tipo documento: " + tipoDocumentoFinal);
                    }
                    
                    pasaFiltro = pasaFiltro && tipoDocValido;
                }
                
                // Filtrar por número de documento
                if (numeroDocumentoFinal != null && !numeroDocumentoFinal.isEmpty() && v.getCliente() != null) {
                    boolean numDocValido = v.getCliente().getNumeroDocumento().contains(numeroDocumentoFinal);
                    
                    if (!numDocValido) {
                        System.out.println("Venta ID " + v.getIdVenta() + " no pasa filtro número documento: " + numeroDocumentoFinal);
                    }
                    
                    pasaFiltro = pasaFiltro && numDocValido;
                }
                
                // Filtrar por nombre del cliente
                if (nombreClienteFinal != null && !nombreClienteFinal.isEmpty() && v.getCliente() != null) {
                    boolean nombreValido = v.getCliente().getNombre().toLowerCase().contains(nombreClienteFinal.toLowerCase()) ||
                                        v.getCliente().getApellidos().toLowerCase().contains(nombreClienteFinal.toLowerCase());
                    
                    if (!nombreValido) {
                        System.out.println("Venta ID " + v.getIdVenta() + " no pasa filtro nombre cliente: " + nombreClienteFinal);
                    }
                    
                    pasaFiltro = pasaFiltro && nombreValido;
                }
                
                // Filtrar por método de pago
                if (metodoPagoNombreFinal != null && !metodoPagoNombreFinal.isEmpty() && v.getMetodoPago() != null) {
                    boolean metodoPagoValido = v.getMetodoPago().getNombreMetodoPago().equals(metodoPagoNombreFinal);
                    
                    if (!metodoPagoValido) {
                        System.out.println("Venta ID " + v.getIdVenta() + " no pasa filtro método pago: " + metodoPagoNombreFinal);
                    }
                    
                    pasaFiltro = pasaFiltro && metodoPagoValido;
                }
                
                // Filtrar por estado de venta
                if (estadoVentaFinal != null && !estadoVentaFinal.isEmpty() && !estadoVentaFinal.equals("todos")) {
                    boolean estadoValido = false;
                    
                    if ("Registrada".equals(estadoVentaFinal)) {
                        estadoValido = "Activo".equals(v.getEstado());
                    } else if ("Anulada".equals(estadoVentaFinal)) {
                        estadoValido = "Anulado".equals(v.getEstado());
                    } else if ("Pendiente".equals(estadoVentaFinal)) {
                        estadoValido = v.getEstadoPago() == EstadoPago.PENDIENTE;
                    }
                    
                    if (!estadoValido) {
                        System.out.println("Venta ID " + v.getIdVenta() + " no pasa filtro estado: " + estadoVentaFinal + 
                                        ", Estado actual: " + v.getEstado() + 
                                        ", EstadoPago: " + (v.getEstadoPago() != null ? v.getEstadoPago().name() : "null"));
                    }
                    
                    pasaFiltro = pasaFiltro && estadoValido;
                }
                
                // Filtrar por importe mínimo
                if (importeMinimoFinal != null && importeMinimoFinal > 0 && v.getTotal() != null) {
                    boolean importeMinValido = v.getTotal().doubleValue() >= importeMinimoFinal;
                    
                    if (!importeMinValido) {
                        System.out.println("Venta ID " + v.getIdVenta() + " no pasa filtro importe mínimo: " + 
                                        importeMinimoFinal + ", Total: " + v.getTotal());
                    }
                    
                    pasaFiltro = pasaFiltro && importeMinValido;
                }
                
                // Filtrar por importe máximo
                if (importeMaximoFinal != null && importeMaximoFinal > 0 && v.getTotal() != null) {
                    boolean importeMaxValido = v.getTotal().doubleValue() <= importeMaximoFinal;
                    
                    if (!importeMaxValido) {
                        System.out.println("Venta ID " + v.getIdVenta() + " no pasa filtro importe máximo: " + 
                                        importeMaximoFinal + ", Total: " + v.getTotal());
                    }
                    
                    pasaFiltro = pasaFiltro && importeMaxValido;
                }
                
                if (pasaFiltro) {
                    System.out.println("Venta ID " + v.getIdVenta() + " PASA TODOS LOS FILTROS");
                } else {
                    System.out.println("Venta ID " + v.getIdVenta() + " NO PASA FILTROS");
                }
                
                return pasaFiltro;
            })
            .sorted((v1, v2) -> {
                // Ordenar según criterio seleccionado
                String ordenPor = ordenarPorFinal != null ? ordenarPorFinal : "fecha";
                boolean descendente = ordenFinal != null && ordenFinal.equals("desc");
                
                int resultado = 0;
                
                switch (ordenPor) {
                    case "importe":
                        resultado = v1.getTotal().compareTo(v2.getTotal());
                        break;
                    case "cliente":
                        String cliente1 = v1.getCliente() != null ? 
                                        v1.getCliente().getNombre() + " " + v1.getCliente().getApellidos() : "";
                        String cliente2 = v2.getCliente() != null ? 
                                        v2.getCliente().getNombre() + " " + v2.getCliente().getApellidos() : "";
                        resultado = cliente1.compareTo(cliente2);
                        break;
                    case "fecha":
                    default:
                        resultado = v1.getFechaVenta().compareTo(v2.getFechaVenta());
                        break;
                }
                
                return descendente ? -resultado : resultado;
            })
            .collect(Collectors.toList());
        
        System.out.println("=== RESULTADOS FILTRADOS ===");
        System.out.println("Total ventas originales: " + ventas.size());
        System.out.println("Total ventas filtradas: " + ventasFiltradas.size());
        System.out.println("===========================");
        
        // Agregar datos al modelo
        model.addAttribute("ventas", ventasFiltradas);
        model.addAttribute("clientes", clienteRepository.findByFarmacia(farmaciaActual));
        model.addAttribute("metodosPago", metodoPagoRepository.findAll());
        
        // Mantener valores de filtros (usar las variables originales, no las final)
        model.addAttribute("fechaInicio", fechaInicio != null ? fechaInicio : LocalDate.now().minusDays(30).toString());
        model.addAttribute("horaInicio", horaInicio != null ? horaInicio : "00:00");
        model.addAttribute("fechaFin", fechaFin != null ? fechaFin : LocalDate.now().toString());
        model.addAttribute("horaFin", horaFin != null ? horaFin : "23:59");
        model.addAttribute("tipoDocumentoSeleccionado", tipoDocumento != null ? tipoDocumento : "todos");
        model.addAttribute("numeroDocumento", numeroDocumento != null ? numeroDocumento : "");
        model.addAttribute("nombreCliente", nombreCliente != null ? nombreCliente : "");
        model.addAttribute("metodoPagoSeleccionado", metodoPagoNombre != null ? metodoPagoNombre : "");
        model.addAttribute("estadoVentaSeleccionado", estadoVenta != null ? estadoVenta : "todos");
        model.addAttribute("importeMinimo", importeMinimo != null ? importeMinimo : "");
        model.addAttribute("importeMaximo", importeMaximo != null ? importeMaximo : "");
        model.addAttribute("ordenarPor", ordenarPor != null ? ordenarPor : "fecha");
        model.addAttribute("orden", orden != null ? orden : "desc");
        
        // Pasar datos del modal
        if (nombre != null) model.addAttribute("nombre", nombre);
        if (tipo != null) model.addAttribute("tipo", tipo);
        if (formato != null) model.addAttribute("formato", formato);
        if (periodo != null) model.addAttribute("periodo", periodo);
        if (estado != null) model.addAttribute("estado", estado);
        if (fechaDesde != null) model.addAttribute("fechaDesde", fechaDesde);
        if (fechaHasta != null) model.addAttribute("fechaHasta", fechaHasta);
        if (descripcion != null) model.addAttribute("descripcion", descripcion);
        
        return "home/reportes/reporte_ventas";
    }

    // Método POST actualizado para aceptar reporteId
    @PostMapping("/reportes/generar")
    public String generarReporteVentas(
            @RequestParam String nombreReporte,
            @RequestParam String formato,
            @RequestParam(required = false) String descripcion,
            @RequestParam String tipoReporte,
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
            @RequestParam int totalRegistros,
            @RequestParam(required = false) Long reporteId,  // <-- Añadir este parámetro
            RedirectAttributes redirectAttributes) {
        
        System.out.println("=== GENERANDO REPORTE VENTAS ===");
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
                    if (totalRegistros > 0) {
                        reporte.setRegistrosProcesados(totalRegistros);
                    }
                    
                    // Actualizar parámetros
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
            nombreReporte = "Reporte de Ventas " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
        
        if (formato == null || formato.trim().isEmpty()) {
            formato = "pdf";
        }
        
        // Guardar el reporte usando el ReporteController
        String redirectUrl = String.format(
            "redirect:/reportes/guardar-desde-ventas?nombre=%s&formato=%s&descripcion=%s&tipo=%s" +
            "&fechaInicio=%s&horaInicio=%s&fechaFin=%s&horaFin=%s" +
            "&tipoDocumento=%s&numeroDocumento=%s&nombreCliente=%s&metodoPagoNombre=%s" +
            "&estadoVenta=%s&importeMinimo=%s&importeMaximo=%s&totalRegistros=%d",
            URLEncoder.encode(nombreReporte, StandardCharsets.UTF_8), 
            formato, 
            descripcion != null ? URLEncoder.encode(descripcion, StandardCharsets.UTF_8) : "", 
            tipoReporte,
            fechaInicio, horaInicio != null ? horaInicio : "", fechaFin, horaFin != null ? horaFin : "",
            tipoDocumento != null ? tipoDocumento : "", numeroDocumento != null ? numeroDocumento : "",
            nombreCliente != null ? nombreCliente : "", metodoPagoNombre != null ? metodoPagoNombre : "",
            estadoVenta != null ? estadoVenta : "", 
            importeMinimo != null ? importeMinimo.toString() : "",
            importeMaximo != null ? importeMaximo.toString() : "",
            totalRegistros
        );
        
        redirectAttributes.addFlashAttribute("mensaje", "Reporte generado exitosamente");
        return redirectUrl;
    }

    // Método GET para redirigir al POST (para compatibilidad)
    @GetMapping("/reportes/generar")
    public String generarReporteGet(
            @RequestParam String nombreReporte,
            @RequestParam String formato,
            @RequestParam(required = false) String descripcion,
            @RequestParam String tipoReporte,
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
            @RequestParam int totalRegistros,
            @RequestParam(required = false) Long reporteId,  // <-- Añadir este parámetro
            RedirectAttributes redirectAttributes) {
        
        // Redirigir al método POST existente
        return generarReporteVentas(
            nombreReporte, formato, descripcion, tipoReporte,
            fechaInicio, horaInicio, fechaFin, horaFin,
            tipoDocumento, numeroDocumento, nombreCliente, metodoPagoNombre,
            estadoVenta, importeMinimo, importeMaximo, totalRegistros,
            reporteId, // <-- Pasar el reporteId
            redirectAttributes
        );
    }
}
