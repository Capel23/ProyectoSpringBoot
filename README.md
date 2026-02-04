# Plataforma SaaS

Sistema de gestion de usuarios y planes de suscripcion.

## Tecnologias

- **Backend:** Spring Boot 3.2, Spring Data JPA, Hibernate Envers
- **Frontend:** React 18, Vite, Tailwind CSS
- **Base de datos:** H2 (desarrollo) / MySQL (produccion)

## Diagrama E-R

```
Usuario (1) -----> (1) Perfil
   |
   | 1:N
   v
Suscripcion (N) <----- (1) Plan
   |                       - BASIC
   | 1:N                   - PREMIUM
   v                       - ENTERPRISE
Factura

MetodoPago (Herencia SINGLE_TABLE)
   |-- TarjetaCredito
   |-- PayPal
   |-- Transferencia
```

### Relaciones
- Usuario - Perfil: OneToOne
- Usuario - Suscripcion: OneToMany
- Suscripcion - Plan: ManyToOne
- Suscripcion - Factura: OneToMany
- Usuario - MetodoPago: OneToMany

## Instalacion

### Backend
```bash
.\mvnw.cmd spring-boot:run
```
http://localhost:8080

### Frontend
```bash
cd frontend
npm install
npm run dev
```
http://localhost:5173

## Perfiles de BD

Editar `spring.profiles.active` en `application.properties`:
- `dev`: H2 en memoria (por defecto)
- `mysql`: MySQL localhost:3306
