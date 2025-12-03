package app.farmy.farmy.controllers;

import java.time.LocalDate;
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
import app.farmy.farmy.model.CompraDetalle;
import app.farmy.farmy.model.CompraDetalleId;
import app.farmy.farmy.model.Proveedor;
import app.farmy.farmy.model.EstadoPago;
import app.farmy.farmy.model.MetodoPago;
import app.farmy.farmy.model.TipoCompra;
import app.farmy.farmy.repository.CompraRepository;
import app.farmy.farmy.repository.CompraDetalleRepository;
import app.farmy.farmy.repository.ProveedorRepository;
import app.farmy.farmy.repository.ProductosRepository;
import app.farmy.farmy.repository.LoteRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import app.farmy.farmy.repository.MetodoPagoRepository;

@Controller
@RequestMapping("/compras")
public class CompraController {

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private CompraDetalleRepository compraDetalleRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private MetodoPagoRepository metodoPagoRepository;

    @Autowired
    private LoteRepository loteRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @GetMapping
    public String listaCompras(Model model) {
        model.addAttribute("listaCompras", compraRepository.findAll());
        return "home/compras/compras";
    }

    @GetMapping("/ver/{id}")
    public String verCompra(@PathVariable int id, Model model) {
        Optional<Compra> c = compraRepository.findById(id);
        if (c.isPresent()) {
            Compra compra = c.get();
            List<CompraDetalle> detalles = compraDetalleRepository.findByCompra_NumeroFactura(id);
            model.addAttribute("compra", compra);
            model.addAttribute("detalles", detalles);
            return "home/compras/detalle_compra";
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/anular")
    public String anularCompra(@RequestParam int numeroFactura, @RequestParam String motivo) {
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
    public String nuevaCompra(Model model) {
        model.addAttribute("productos", productosRepository.findAll());
        model.addAttribute("proveedores", proveedorRepository.findAll());
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
            @RequestParam String itemsJson) {

        Compra compra = new Compra();

        if (proveedorId != null) {
            Proveedor p = proveedorRepository.findById(proveedorId).orElse(null);
            compra.setProveedor(p);
        }

        compra.setSubtotal(subtotal == null ? 0.0 : subtotal);
        compra.setIgv(igv == null ? 0.0 : igv);
        compra.setTotal(total == null ? 0.0 : total);

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

        compra.setFechaVencimientoPago(LocalDate.parse(fechaVencimientoPago));
        
        if (metodoPago != null) {
            metodoPagoRepository.findById(metodoPago).ifPresent(compra::setMetodoPago);
        }

        // persist compra first to get generated numeroFactura
        compraRepository.save(compra);
        System.out.println("\n".repeat(20) + "Esta es la compra" + compra.toString() + "\n".repeat(20));

        // si vienen items en JSON, parsearlos y persistir CompraDetalle
        if (itemsJson != null && !itemsJson.isBlank()) {
            try {
                List<Map<String, Object>> items = mapper.readValue(itemsJson,
                        new TypeReference<List<Map<String, Object>>>() {
                        });
                for (Map<String, Object> it : items) {
                    Integer idProd = null;
                    if (it.get("id") != null)
                        idProd = (Integer) ((Number) it.get("id")).intValue();
                    else if (it.get("idProducto") != null)
                        idProd = (Integer) ((Number) it.get("idProducto")).intValue();

                    Integer cantidad = (it.get("cantidad") != null) ? ((Number) it.get("cantidad")).intValue() : 0;
                    Double precio = (it.get("precio") != null) ? ((Number) it.get("precio")).doubleValue() : 0.0;
                    Integer idLote = null;

                    if (it.get("idLote") != null)
                        idLote = ((Number) it.get("idLote")).intValue();

                    CompraDetalle detalle = new CompraDetalle();

                    CompraDetalleId detId = new CompraDetalleId(compra.getNumeroFactura(), idProd == null ? 0 : idProd,
                            idLote == null ? 0 : idLote);
                    detalle.setCompraDetalleId(detId);
                    detalle.setCompra(compra);

                    if (idProd != null) {
                        var prodOpt = productosRepository.findById(idProd);
                        if (prodOpt.isPresent()) {
                            var prod = prodOpt.get();
                            detalle.setProducto(prod);
                            // aumentar stock del producto
                            if (prod.getStock() == null)
                                prod.setStock(cantidad);
                            else
                                prod.setStock(prod.getStock() + cantidad);
                            productosRepository.save(prod);
                        }
                    }

                    if (idLote != null && idLote != 0) {
                        var loteOpt = loteRepository.findById(idLote);
                        if (loteOpt.isPresent()) {
                            var lote = loteOpt.get();
                            detalle.setLote(lote);
                            // aumentar cantidad en lote
                            lote.setCantidadActual(lote.getCantidadActual() + cantidad);
                            loteRepository.save(lote);
                        }
                    }

                    detalle.setCantidad(cantidad);
                    detalle.setPrecioCompra(precio);
                    detalle.setPrecioVenta(precio);

                    compraDetalleRepository.save(detalle);
                }
            } catch (Exception ex) {
                // parsing error -> ignore or log
                ex.printStackTrace();
            }
        }

        return "redirect:/compras";
    }

}