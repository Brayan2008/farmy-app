package app.farmy.farmy.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import app.farmy.farmy.model.Compra;
import app.farmy.farmy.model.Proveedor;
import app.farmy.farmy.model.EstadoPago;
import app.farmy.farmy.model.Lote;
import app.farmy.farmy.model.TipoCompra;
import app.farmy.farmy.repository.CompraRepository;
import app.farmy.farmy.repository.ProveedorRepository;
import app.farmy.farmy.security.FarmySesion;
import jakarta.servlet.http.HttpSession;
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

}