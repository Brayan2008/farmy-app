package app.farmy.farmy.controllers;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;
import app.farmy.farmy.model.Lote;
import app.farmy.farmy.repository.LoteRepository;
import app.farmy.farmy.repository.FarmaciaRepository;
import app.farmy.farmy.model.Farmacia;
import app.farmy.farmy.model.Usuario;
import app.farmy.farmy.model.Ventas;
import app.farmy.farmy.model.Compra;
import app.farmy.farmy.model.Caja;
import app.farmy.farmy.model.EstadoPago;
import app.farmy.farmy.model.TipoMovimientoEnum;
import app.farmy.farmy.repository.VentasRepository;
import app.farmy.farmy.repository.CompraRepository;
import app.farmy.farmy.repository.CajaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Controller
public class PageController {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private FarmaciaRepository farmaciaRepository;

    @Autowired
    private VentasRepository ventasRepository;

    @Autowired
    private CompraRepository compraRepository;

    @Autowired
    private CajaRepository cajaRepository;

    // Entorno 1: SuperAdmin
    @GetMapping("/superadmin/dashboard")
    public String superAdminDashboard(HttpSession session) {
        return "superadmin/dashboard";
    }

    // Entorno 2: Gestión Farmacia
    @GetMapping("/home")
    public String gestionDashboard(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null || usuario.getFarmacia() == null) {
            return "redirect:/login";
        }
        Farmacia farmacia = usuario.getFarmacia();
        Long idFarmacia = farmacia.getId();

        // 1. Ventas del Día
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.atTime(23, 59, 59);
        
        List<Ventas> ventasHoy = ventasRepository.findByFarmaciaAndFechaVentaBetween(farmacia, inicioDia, finDia);
        BigDecimal totalVentasHoy = ventasHoy.stream()
            .map(Ventas::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        int numVentasHoy = ventasHoy.size();

        // 2. Compras Pendientes
        List<Compra> comprasPendientes = compraRepository.findByUsuario_FarmaciaAndEstadoPago(farmacia, EstadoPago.PENDIENTE);
        BigDecimal totalComprasPendientes = comprasPendientes.stream()
            .map(Compra::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        int numComprasPendientes = comprasPendientes.size();

        // 3. Saldo en Caja
        List<Caja> movimientosCaja = cajaRepository.findByFarmacia(farmacia);
        double saldoCaja = movimientosCaja.stream()
            .mapToDouble(c -> c.getTipo() == TipoMovimientoEnum.Ingreso ? c.getMonto() : -c.getMonto())
            .sum();
        double ingresosCaja = movimientosCaja.stream()
            .filter(c -> c.getTipo() == TipoMovimientoEnum.Ingreso)
            .mapToDouble(Caja::getMonto)
            .sum();

        // 4. Productos Críticos
        long productosCriticos = loteRepository.findAll().stream()
            .filter(l -> l.getProducto().getFarmacia() != null && l.getProducto().getFarmacia().getId().equals(idFarmacia))
            .filter(l -> l.getCantidadActual() < 10)
            .count();

        // 5. Últimas Ventas
        List<Ventas> ultimasVentas = ventasRepository.findTop5ByFarmaciaOrderByFechaVentaDesc(farmacia);

        // 6. Movimientos de Caja Recientes
        List<Caja> ultimosMovimientosCaja = cajaRepository.findTop5ByFarmaciaOrderByFechaDescHoraDesc(farmacia);

        model.addAttribute("totalVentasHoy", totalVentasHoy);
        model.addAttribute("numVentasHoy", numVentasHoy);
        model.addAttribute("totalComprasPendientes", totalComprasPendientes);
        model.addAttribute("numComprasPendientes", numComprasPendientes);
        model.addAttribute("saldoCaja", saldoCaja);
        model.addAttribute("ingresosCaja", ingresosCaja);
        model.addAttribute("productosCriticos", productosCriticos);
        model.addAttribute("ultimasVentas", ultimasVentas);
        model.addAttribute("ultimosMovimientosCaja", ultimosMovimientosCaja);

        return "/home/dashboard";
    }

    // Entorno 3: Tienda Cliente
    @GetMapping("/tienda/{idFarmacia}/home")
    public String tiendaHome(@PathVariable Long idFarmacia, Model model) {
        // Fetch products with stock > 0 and belonging to the specific pharmacy
        List<Lote> productosDisponibles = loteRepository.findAll().stream()
                .filter(l -> l.getCantidadActual() > 0)
                .filter(l -> l.getProducto().getFarmacia() != null && l.getProducto().getFarmacia().getId().equals(idFarmacia))
                .collect(Collectors.toList());
        
        Farmacia farmacia = farmaciaRepository.findById(idFarmacia).orElse(null);
        model.addAttribute("farmacia", farmacia);

        model.addAttribute("productos", productosDisponibles);
        model.addAttribute("idFarmacia", idFarmacia);
        return "web/client-web";
    }

    @GetMapping("/tienda/{idFarmacia}/checkout")
    public String tiendaCheckout(@PathVariable Long idFarmacia, Model model) {
        Farmacia farmacia = farmaciaRepository.findById(idFarmacia).orElse(null);
        model.addAttribute("farmacia", farmacia);
        model.addAttribute("idFarmacia", idFarmacia);
        return "web/tu";
    }

    @GetMapping("/tienda/{idFarmacia}/producto/{idLote}")
    public String productoDetalle(@PathVariable Long idFarmacia, @PathVariable Integer idLote, Model model) {
        Lote lote = loteRepository.findById(idLote).orElse(null);
        if (lote == null) {
            return "redirect:/tienda/" + idFarmacia + "/home";
        }
        Farmacia farmacia = farmaciaRepository.findById(idFarmacia).orElse(null);
        model.addAttribute("farmacia", farmacia);

        model.addAttribute("producto", lote);
        model.addAttribute("idFarmacia", idFarmacia);
        return "web/product-detail";
    }

    // Redirección raíz
    @GetMapping("/")
    public String root() {
        return "redirect:/login";
    }

    @GetMapping("/superadmin/usuarios")
    public String superAdminUsuarios() {
        return "superadmin/usuarios"; // Apunta al archivo HTML que crearemos
    }
}