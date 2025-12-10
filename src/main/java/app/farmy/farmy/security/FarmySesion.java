package app.farmy.farmy.security;

import app.farmy.farmy.model.Farmacia;
import app.farmy.farmy.model.Usuario;
import jakarta.servlet.http.HttpSession;

public interface FarmySesion {
    
    static final String FARMACIA_ACTUAL = "farmaciaActual";
    static final String USUARIO_ACTUAL = "usuarioLogueado";

    default Farmacia getFarmaciaActual(HttpSession session) {
        return (Farmacia) session.getAttribute(FARMACIA_ACTUAL);
    }
    
    default Usuario getUsuarioActual(HttpSession session) {
        return (Usuario) session.getAttribute(USUARIO_ACTUAL);
    }

}
