package com.example.ProyectoSpringBoot.repository;

import com.example.ProyectoSpringBoot.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Repositorio de Usuario - hereda CRUD de JpaRepository
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // Búsqueda por email
    Optional<Usuario> findByEmail(String email);

    // Verifica si existe email
    boolean existsByEmail(String email);

    // Usuarios activos/inactivos
    List<Usuario> findByActivoTrue();
    List<Usuario> findByActivoFalse();

    // Por estado de verificación
    List<Usuario> findByEmailVerificado(Boolean emailVerificado);

    // Por fechas
    List<Usuario> findByFechaCreacionAfter(LocalDateTime fecha);
    List<Usuario> findByFechaCreacionBetween(LocalDateTime inicio, LocalDateTime fin);

    // Usuarios inactivos (sin acceso reciente)
    @Query("SELECT u FROM Usuario u WHERE u.ultimoAcceso < :fecha OR u.ultimoAcceso IS NULL")
    List<Usuario> findUsuariosInactivosDesde(@Param("fecha") LocalDateTime fecha);

    // Conteo
    long countByActivoTrue();

    // Con relaciones cargadas (evita N+1)
    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.perfil WHERE u.id = :id")
    Optional<Usuario> findByIdConPerfil(@Param("id") Long id);

    @Query("SELECT u FROM Usuario u LEFT JOIN FETCH u.suscripciones WHERE u.id = :id")
    Optional<Usuario> findByIdConSuscripciones(@Param("id") Long id);
}
