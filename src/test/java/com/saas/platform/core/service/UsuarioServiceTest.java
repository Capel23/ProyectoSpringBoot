package com.saas.platform.core.service;

import com.saas.platform.core.model.entity.Usuario;
import com.saas.platform.core.model.entity.Perfil;
import com.saas.platform.core.repository.PerfilRepository;
import com.saas.platform.core.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Tests Unitarios")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilRepository perfilRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = Usuario.builder()
                .id(1L)
                .nombre("Juan")
                .apellido("García")
                .email("juan@ejemplo.com")
                .password("password123")
                .activo(true)
                .build();
    }

    @Test
    @DisplayName("Debe registrar un usuario nuevo correctamente")
    void registrarUsuario() {
        when(usuarioRepository.existsByEmail("juan@ejemplo.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(perfilRepository.save(any(Perfil.class))).thenReturn(new Perfil());

        Usuario resultado = usuarioService.registrar(usuario);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNombre()).isEqualTo("Juan");
        assertThat(resultado.getEmail()).isEqualTo("juan@ejemplo.com");
        verify(usuarioRepository).save(any(Usuario.class));
        verify(perfilRepository).save(any(Perfil.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción si el email ya existe")
    void registrarUsuarioEmailDuplicado() {
        when(usuarioRepository.existsByEmail("juan@ejemplo.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.registrar(usuario))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Ya existe un usuario con el email");
    }

    @Test
    @DisplayName("Debe listar todos los usuarios")
    void listarTodos() {
        Usuario usuario2 = Usuario.builder()
                .id(2L).nombre("María").apellido("López")
                .email("maria@ejemplo.com").password("pass").activo(true).build();
        when(usuarioRepository.findAll()).thenReturn(Arrays.asList(usuario, usuario2));

        List<Usuario> usuarios = usuarioService.listarTodos();

        assertThat(usuarios).hasSize(2);
    }

    @Test
    @DisplayName("Debe buscar usuario por ID")
    void buscarPorId() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.buscarPorId(1L);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getNombre()).isEqualTo("Juan");
    }

    @Test
    @DisplayName("Debe buscar usuario por email")
    void buscarPorEmail() {
        when(usuarioRepository.findByEmail("juan@ejemplo.com")).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.buscarPorEmail("juan@ejemplo.com");

        assertThat(resultado).isPresent();
    }

    @Test
    @DisplayName("Debe actualizar un usuario")
    void actualizarUsuario() {
        usuario.setNombre("Juan Carlos");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Usuario actualizado = usuarioService.actualizar(usuario);

        assertThat(actualizado.getNombre()).isEqualTo("Juan Carlos");
    }
}
