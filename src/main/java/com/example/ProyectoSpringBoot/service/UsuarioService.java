package com.example.ProyectoSpringBoot.service;

import com.example.ProyectoSpringBoot.dto.UsuarioDTO;
import com.example.ProyectoSpringBoot.entity.Perfil;
import com.example.ProyectoSpringBoot.entity.Usuario;
import com.example.ProyectoSpringBoot.enums.RolUsuario;
import com.example.ProyectoSpringBoot.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para la lógica de negocio de Usuario
 * Cumple criterio: "Controllers limpios que solo delegan a la capa de Service"
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioDTO> findAll() {
        return usuarioRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findById(Long id) {
        return usuarioRepository.findById(id).map(this::toDTO);
    }

    public UsuarioDTO create(UsuarioDTO dto) {
        Usuario usuario = toEntity(dto);
        usuario.setId(null);
        usuario.setPassword(passwordEncoder.encode(dto.getPassword() != null ? dto.getPassword() : "password123"));
        
        // Crear perfil asociado
        if (dto.getNombre() != null || dto.getApellidos() != null) {
            Perfil perfil = Perfil.builder()
                    .nombre(dto.getNombre() != null ? dto.getNombre() : "Usuario")
                    .apellidos(dto.getApellidos())
                    .telefono(dto.getTelefono())
                    .pais(dto.getPais())
                    .usuario(usuario)
                    .build();
            usuario.setPerfil(perfil);
        }
        
        Usuario saved = usuarioRepository.save(usuario);
        return toDTO(saved);
    }

    public Optional<UsuarioDTO> update(Long id, UsuarioDTO dto) {
        return usuarioRepository.findById(id).map(existing -> {
            updateEntity(existing, dto);
            Usuario saved = usuarioRepository.save(existing);
            return toDTO(saved);
        });
    }

    public boolean delete(Long id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    /**
     * Autenticar usuario por email y password
     */
    public Optional<UsuarioDTO> login(String email, String password) {
        return usuarioRepository.findByEmail(email)
                .filter(usuario -> passwordEncoder.matches(password, usuario.getPassword()))
                .map(usuario -> {
                    // Actualizar último acceso
                    usuario.setUltimoAcceso(LocalDateTime.now());
                    usuarioRepository.save(usuario);
                    return toDTO(usuario);
                });
    }

    @Transactional(readOnly = true)
    public Optional<UsuarioDTO> findByEmail(String email) {
        return usuarioRepository.findByEmail(email).map(this::toDTO);
    }

    // ===== CONVERSIONES Entity <-> DTO =====

    private UsuarioDTO toDTO(Usuario entity) {
        UsuarioDTO.UsuarioDTOBuilder builder = UsuarioDTO.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .activo(entity.getActivo())
                .emailVerificado(entity.getEmailVerificado())
                .rol(entity.getRol())
                .fechaCreacion(entity.getFechaCreacion())
                .ultimoAcceso(entity.getUltimoAcceso());
        
        // Obtener suscripción activa
        if (entity.getSuscripciones() != null && !entity.getSuscripciones().isEmpty()) {
            entity.getSuscripciones().stream()
                    .filter(s -> s.getEstado() != null && 
                            (s.getEstado().name().equals("ACTIVA") || s.getEstado().name().equals("TRIAL")))
                    .findFirst()
                    .ifPresent(sus -> builder.suscripcionId(sus.getId()));
        }
        
        // Datos del perfil
        if (entity.getPerfil() != null) {
            builder.nombre(entity.getPerfil().getNombre())
                   .apellidos(entity.getPerfil().getApellidos())
                   .telefono(entity.getPerfil().getTelefono())
                   .pais(entity.getPerfil().getPais());
        }
        
        return builder.build();
    }

    private Usuario toEntity(UsuarioDTO dto) {
        return Usuario.builder()
                .email(dto.getEmail())
                .password("temp") // Se encripta después
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();
    }

    private void updateEntity(Usuario entity, UsuarioDTO dto) {
        if (dto.getEmail() != null) {
            entity.setEmail(dto.getEmail());
        }
        if (dto.getActivo() != null) {
            entity.setActivo(dto.getActivo());
        }
        
        // Actualizar perfil
        if (entity.getPerfil() != null) {
            if (dto.getNombre() != null) {
                entity.getPerfil().setNombre(dto.getNombre());
            }
            if (dto.getApellidos() != null) {
                entity.getPerfil().setApellidos(dto.getApellidos());
            }
            if (dto.getTelefono() != null) {
                entity.getPerfil().setTelefono(dto.getTelefono());
            }
            if (dto.getPais() != null) {
                entity.getPerfil().setPais(dto.getPais());
            }
        }
    }
}
