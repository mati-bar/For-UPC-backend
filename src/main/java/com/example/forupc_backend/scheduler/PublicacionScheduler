package com.example.forupc_backend.scheduler;

import com.example.forupc_backend.modelo.Publicacion;
import com.example.forupc_backend.repository.PublicacionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class PublicacionScheduler {

    private final PublicacionRepository publicacionRepository;

    public PublicacionScheduler(PublicacionRepository publicacionRepository) {
        this.publicacionRepository = publicacionRepository;
    }

    @Scheduled(fixedRate = 60000)
    public void eliminarPublicacionesVencidas() {

        LocalDateTime ahora = LocalDateTime.now();

        List<Publicacion> publicacionesVencidas =
                publicacionRepository.findByFechaExpiracionBefore(ahora);

        if (!publicacionesVencidas.isEmpty()) {

            publicacionRepository.deleteAll(publicacionesVencidas);

            System.out.println(
                    "Se eliminaron "
                    + publicacionesVencidas.size()
                    + " publicaciones vencidas."
            );
        }
    }
}