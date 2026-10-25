package com.example.forupc_backend.service;

import com.example.forupc_backend.dto.PublicacionesPaginadasResponse;
import com.example.forupc_backend.modelo.Publicacion;
import com.example.forupc_backend.modelo.PublicacionDestino;
import com.example.forupc_backend.repository.PublicacionRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PublicacionConsultaService {

    private final PublicacionRepository publicacionRepository;

    public PublicacionConsultaService(PublicacionRepository publicacionRepository) {
        this.publicacionRepository = publicacionRepository;
    }

    @Transactional(readOnly = true)
    public PublicacionesPaginadasResponse listarPublicaciones(int page, int size) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("id"))
        );

        Page<Publicacion> resultado = publicacionRepository.findAll(pageable);

        List<PublicacionesPaginadasResponse.PublicacionItem> publicaciones =
                resultado.getContent().stream()
                        .map(this::convertirPublicacion)
                        .toList();

        return new PublicacionesPaginadasResponse(
                publicaciones,
                resultado.getTotalElements(),
                resultado.getTotalPages(),
                resultado.getSize(),
                resultado.getNumber(),
                resultado.isFirst(),
                resultado.isLast(),
                resultado.isEmpty()
        );
    }

    private PublicacionesPaginadasResponse.PublicacionItem convertirPublicacion(Publicacion publicacion) {
        List<PublicacionesPaginadasResponse.DestinoItem> destinos =
                publicacion.getDestinos().stream()
                        .map(this::convertirDestino)
                        .toList();

        return new PublicacionesPaginadasResponse.PublicacionItem(
                publicacion.getId(),
                publicacion.getTitulo(),
                publicacion.getMensaje(),
                publicacion.getFecha(),
                publicacion.getFechaExpiracion(),
                publicacion.isUrgente(),
                publicacion.getImagenUrl(),
                publicacion.getArchivoUrl(),
                publicacion.getArchivoNombre(),
                publicacion.getCarrera() != null ? publicacion.getCarrera().getId() : null,
                publicacion.getAnio() != null ? publicacion.getAnio().getId() : null,
                destinos
        );
    }

    private PublicacionesPaginadasResponse.DestinoItem convertirDestino(PublicacionDestino destino) {
        return new PublicacionesPaginadasResponse.DestinoItem(
                destino.getId(),
                new PublicacionesPaginadasResponse.CarreraItem(
                        destino.getCarrera().getId(),
                        destino.getCarrera().getNombre()
                ),
                new PublicacionesPaginadasResponse.AnioItem(
                        destino.getAnio().getId(),
                        destino.getAnio().getNumero()
                )
        );
    }
}
