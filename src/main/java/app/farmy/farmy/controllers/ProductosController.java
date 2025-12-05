package app.farmy.farmy.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import app.farmy.farmy.model.Productos;
import app.farmy.farmy.repository.CategoriaRepository;
import app.farmy.farmy.repository.LaboratorioRepository;
import app.farmy.farmy.repository.MarcaRepository;
import app.farmy.farmy.repository.PresentacionRepository;
import app.farmy.farmy.repository.ProductosRepository;

@Controller
@RequestMapping("/productos")
public class ProductosController {

    @Autowired
    private ProductosRepository productosRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private MarcaRepository marcaRepository;

    @Autowired
    private PresentacionRepository presentacionRepository;

    @Autowired
    private LaboratorioRepository laboratorioRepository;

    @GetMapping
    public String listarProductos(Model model) {
        model.addAttribute("listaProductos", productosRepository.findAll());
        model.addAttribute("producto", new Productos());
        cargarListas(model);
        return "home/productos/productos";
    }

    @PostMapping("/guardar")
    public String guardarProducto(@ModelAttribute Productos producto, @RequestParam("file") MultipartFile file) {
        if (!file.isEmpty()) {
            try {
                String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path uploadPath = Paths.get("uploads");
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(file.getInputStream(), filePath);
                producto.setImgUrl(fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        productosRepository.save(producto);
        return "redirect:/productos";
    }

    @GetMapping("/detalle/{id}")
    public String detalleProducto(@PathVariable("id") int id, Model model) {
        Productos producto = productosRepository.findById(id).orElse(null);
        if (producto == null) {
            return "redirect:/productos";
        }
        model.addAttribute("producto", producto);
        cargarListas(model);
        return "home/productos/detalles-producto";
    }
    
    @PostMapping("/actualizar")
    public String actualizarProducto(@ModelAttribute Productos producto, @RequestParam("file") MultipartFile file) {
         Productos productoExistente = productosRepository.findById(producto.getIdProducto()).orElse(null);
         
         if(productoExistente != null) {
             if (!file.isEmpty()) {
                try {
                    String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                    Path uploadPath = Paths.get("uploads");
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    Path filePath = uploadPath.resolve(fileName);
                    Files.copy(file.getInputStream(), filePath);
                    producto.setImgUrl(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                producto.setImgUrl(productoExistente.getImgUrl());
            }
            producto.setFechaRegistro(productoExistente.getFechaRegistro());
            
            productosRepository.save(producto);
         }
         
        return "redirect:/productos";
    }

    private void cargarListas(Model model) {
        model.addAttribute("categorias", categoriaRepository.findAll());
        model.addAttribute("marcas", marcaRepository.findAll());
        model.addAttribute("presentaciones", presentacionRepository.findAll());
        model.addAttribute("laboratorios", laboratorioRepository.findAll());
    }
}