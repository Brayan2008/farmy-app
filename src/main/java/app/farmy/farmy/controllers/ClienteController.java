package app.farmy.farmy.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import app.farmy.farmy.model.Cliente;
import app.farmy.farmy.repository.ClienteRepository;
import app.farmy.farmy.security.FarmySesion;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("clientes")
public class ClienteController implements FarmySesion {

    @Autowired
    private ClienteRepository clienteRepo;

    private String documentoInvalidoMensage ="Número de documento inválido para el tipo de documento. Recuerde: DNI : 8 dígitos; RUC: 11 dígitos";


    @GetMapping
    public String getClientes(Model model, HttpSession session) {
        model.addAttribute("listaClientes", clienteRepo.findByFarmacia(getFarmaciaActual(session)));
        model.addAttribute("cliente", new Cliente());
        return "/home/clientes/clientes";
    }

    @PostMapping("/save")
    public String saveCliente(@ModelAttribute Cliente cliente, HttpSession session) {
        // validar longitud de documento antes de guardar
        if (!validarLongitudDocumento(cliente)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, documentoInvalidoMensage);
        }
        cliente.setFarmacia(getFarmaciaActual(session));
        clienteRepo.save(cliente);
        return "redirect:/clientes";
    }

    @GetMapping("/editar/{id}")
    public String editarCliente(@PathVariable int id, Model model) {
        var opt = clienteRepo.findById(id);
        if (opt.isPresent()) {
            model.addAttribute("clienteEditar", opt.get());
            model.addAttribute("idCliente", id);
            return "home/clientes/clientes_editar";
        }

        throw new ResponseStatusException(HttpStatus.NOT_FOUND);
    }

    @PostMapping("/editar/{id}")
    public String editarCliente(@PathVariable int id, @ModelAttribute Cliente clienteEditar) {
        var exists = clienteRepo.findById(id).orElseThrow();
        // validar longitud de documento antes de actualizar
        if (!validarLongitudDocumento(clienteEditar)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, documentoInvalidoMensage);
        }

        // Actualizar campos permitidos; preservar fechaRegistro
        exists.setNumeroDocumento(clienteEditar.getNumeroDocumento());
        exists.setTipoDocumento(clienteEditar.getTipoDocumento());
        exists.setNombre(clienteEditar.getNombre());
        exists.setApellidos(clienteEditar.getApellidos());
        exists.setDireccion(clienteEditar.getDireccion());
        exists.setEmail(clienteEditar.getEmail());
        exists.setEstado(clienteEditar.getEstado());

        clienteRepo.save(exists);
        return "redirect:/clientes";
    }

    /**
     * Valida la longitud de numeroDocumento según tipoDocumento.
     * - DNI  -> 8 caracteres
     * - RUC  -> 11 caracteres
     * Acepta que el tipo venga como enum en el objeto Cliente.
     */
    private boolean validarLongitudDocumento(Cliente c) {
        if (c == null || c.getNumeroDocumento() == null || c.getTipoDocumento() == null) {
            return false;
        }

        String num = c.getNumeroDocumento().trim();
        switch (c.getTipoDocumento()) {
            case DNI:
                return num.length() == 8;
            case RUC:
                return num.length() == 11;
            default:
                return false;
        }
    }
}


