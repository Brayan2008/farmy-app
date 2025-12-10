package app.farmy.farmy.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.farmy.farmy.dto.CartItemDTO;
import app.farmy.farmy.dto.PedidoRequest;
import app.farmy.farmy.model.Cliente;
import app.farmy.farmy.model.EstadoPago;
import app.farmy.farmy.model.Farmacia;
import app.farmy.farmy.model.Lote;
import app.farmy.farmy.model.TipoDocumentoEnum;
import app.farmy.farmy.model.TipoVenta;
import app.farmy.farmy.model.VentaDetalle;
import app.farmy.farmy.model.Ventas;
import app.farmy.farmy.repository.ClienteRepository;
import app.farmy.farmy.repository.FarmaciaRepository;
import app.farmy.farmy.repository.LoteRepository;
import app.farmy.farmy.repository.UsuarioRepository;
import app.farmy.farmy.repository.VentaDetalleRepository;
import app.farmy.farmy.repository.VentasRepository;

@RestController
@RequestMapping("/api/tienda")
public class TiendaApiController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VentasRepository ventasRepository;

    @Autowired
    private VentaDetalleRepository ventaDetalleRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private FarmaciaRepository farmaciaRepository;

    @PostMapping("/pedido")
    public ResponseEntity<?> registrarPedido(@RequestBody PedidoRequest request) {
        try {
            // 1. Gestionar Cliente
            Cliente cliente = null;
            Optional<Cliente> clienteOpt = clienteRepository.findByNumeroDocumento(request.getCliente().getNumeroDocumento());
            Farmacia farmacia = farmaciaRepository.findById(request.getIdFarmacia()).orElse(null);
            
            if (clienteOpt.isPresent()) {
                cliente = clienteOpt.get();
                // Actualizar datos si es necesario
                cliente.setNombre(request.getCliente().getNombres());
                cliente.setApellidos(request.getCliente().getApellidos());
                cliente.setDireccion(request.getCliente().getDireccion());
                cliente.setTelefono(request.getCliente().getTelefono());
                cliente.setEmail(request.getCliente().getEmail());
                clienteRepository.save(cliente);
            } else {
                cliente = new Cliente();
                cliente.setNumeroDocumento(request.getCliente().getNumeroDocumento());
                // Asumimos DNI por defecto o mapeamos desde el string
                cliente.setTipoDocumento(TipoDocumentoEnum.DNI); 
                cliente.setNombre(request.getCliente().getNombres());
                cliente.setApellidos(request.getCliente().getApellidos());
                cliente.setDireccion(request.getCliente().getDireccion());
                cliente.setTelefono(request.getCliente().getTelefono());
                cliente.setEmail(request.getCliente().getEmail());
                cliente.setFarmacia(farmacia);
                clienteRepository.save(cliente);
            }

            // 2. Crear Venta (Pedido)
            Ventas venta = new Ventas();
            venta.setCliente(cliente);
            venta.setFechaVenta(LocalDateTime.now());
            venta.setEstadoPago(EstadoPago.PEDIDO);
            venta.setTipoVenta(TipoVenta.CONTADO); // O lo que corresponda
            venta.setEstado("Activo");

            // Guardar venta inicial para tener ID
            venta = ventasRepository.save(venta);

            BigDecimal total = BigDecimal.ZERO;
            BigDecimal subtotal = BigDecimal.ZERO;


            // 3. Procesar Items
            for (CartItemDTO item : request.getItems()) {
                Lote lote = loteRepository.findById(item.getId()).orElse(null);
                if (lote != null && lote.getCantidadActual() >= item.getQuantity()) {
                    VentaDetalle detalle = new VentaDetalle();
                    detalle.setVenta(venta);
                    detalle.setLote(lote);
                    detalle.setCantidad(item.getQuantity());
    
                    subtotal = subtotal.add(BigDecimal.valueOf(lote.getPrecioVenta()).multiply(BigDecimal.valueOf(item.getQuantity())));
                    
                    ventaDetalleRepository.save(detalle);
                    
                    // Actualizar Stock
                    lote.setCantidadActual(lote.getCantidadActual() - item.getQuantity());
                    loteRepository.save(lote);
                }
            }

            BigDecimal igv = subtotal.multiply(BigDecimal.valueOf(0.18));
            total = subtotal.add(igv);
            // Asumiendo IGV incluido o calculo simple
            venta.setSubTotal(subtotal); // Ajustar si hay IGV
            venta.setTotal(total);
            venta.setIgv(igv);
            venta.setSaldoPendiente(total);
            venta.setFarmacia(farmacia);
            venta.setUsuario(usuarioRepository.findByEmail("farmacia" + farmacia.getId() + "@farmy.com").get());
            ventasRepository.save(venta);

            return ResponseEntity.ok().body("Pedido registrado con éxito. ID: " + venta.getIdVenta());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error al registrar el pedido: " + e.getMessage());
        }
    }
}
