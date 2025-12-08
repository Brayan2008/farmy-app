package app.farmy.farmy.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import app.farmy.farmy.model.Cliente;
import app.farmy.farmy.model.EstadoPago;
import app.farmy.farmy.model.InventarioMovimiento;
import app.farmy.farmy.model.InventarioMovimiento.TipoMovimiento;
import app.farmy.farmy.model.Lote;
import app.farmy.farmy.model.TipoVenta;
import app.farmy.farmy.model.VentaDetalle;
import app.farmy.farmy.model.VentaPago;
import app.farmy.farmy.model.Ventas;
import app.farmy.farmy.repository.ClienteRepository;
import app.farmy.farmy.repository.InventarioMovimientoRepository;
import app.farmy.farmy.repository.LoteRepository;
import app.farmy.farmy.repository.MetodoPagoRepository;
import app.farmy.farmy.repository.VentaDetalleRepository;
import app.farmy.farmy.repository.VentaPagoRepository;
import app.farmy.farmy.repository.VentasRepository;
import app.farmy.farmy.security.FarmySesion;
import jakarta.servlet.http.HttpSession;

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
}
