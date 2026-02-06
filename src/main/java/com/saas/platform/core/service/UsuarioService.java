package com.saas.platform.core.service;

import com.saas.platform.core.model.entity.Perfil;
import com.saas.platform.core.model.entity.Usuario;
import com.saas.platform.core.repository.PerfilRepository;
import com.saas.platform.core.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;

    /**
     * Registra un usuario nuevo y le crea un perfil vacío.
     */
    public Usuario registrar(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con el email: " + usuario.getEmail());
        }
        Usuario guardado = usuarioRepository.save(usuario);

        Perfil perfil = Perfil.builder()
                .usuario(guardado)
                .build();
        perfilRepository.save(perfil);

        return guardado;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Usuario actualizar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
