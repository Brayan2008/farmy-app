package app.farmy.farmy.services;

import app.farmy.farmy.model.Farmacia;
import app.farmy.farmy.repository.FarmaciaRepository; // Asegúrate de crear esta interfaz si no existe (vacía extends JpaRepository)
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FarmaciaService {

    @Autowired
    private FarmaciaRepository farmaciaRepository;

    public List<SalidaFarmaciaDTO> listarTodas() {
        return farmaciaRepository.findAll().stream()
                .map(SalidaFarmaciaDTO::new)
                .toList();
    }

    public Farmacia guardar(Farmacia farmacia) {
        // Aquí podrías validar que el RUC no se repita antes de guardar
        return farmaciaRepository.save(farmacia);
    }

    public SalidaFarmaciaDTO obtenerPorId(Long id) {
        var farmacia = farmaciaRepository.findById(id).get();
        if (farmacia != null) {
            return new SalidaFarmaciaDTO(farmacia);
        }
        return null;
    }

    public void cambiarEstado(Long id) {
        Farmacia farmacia = farmaciaRepository.findById(id).orElse(null);
        if (farmacia != null) {
            farmacia.setEstado(!farmacia.getEstado()); // Invierte el estado (True <-> False)
            farmaciaRepository.save(farmacia);
        }
    }

    public record SalidaFarmaciaDTO(Long id, String nombreComercial, String ruc, String direccion, Boolean estado) {

        public SalidaFarmaciaDTO(Farmacia farmacia) {
            this(farmacia.getId(), farmacia.getNombreComercial(), farmacia.getRuc(), farmacia.getDireccion(), farmacia.getEstado());
        }
    }
}