# Plataforma SaaS - Spring Boot + React

Sistema completo de gestión de suscripciones con autenticación, facturación automática y panel de administración.

## 🚀 Tecnologías

- **Backend:** Spring Boot 3.2, JPA, Hibernate Envers, MySQL
- **Frontend:** React 18, Vite, Tailwind CSS
- **Seguridad:** BCrypt, role-based access control

## ⚡ Características

- ✅ Autenticación con roles (Admin/Usuario)
- ✅ Gestión de suscripciones y ciclo de vida
- ✅ Facturación automática con impuestos
- ✅ Panel de administración con auditoría
- ✅ 3 planes: Básico (€9.99), Premium (€29.99), Empresarial (€99.99)

## 📦 Instalación

### 1. Configurar MySQL
```bash
# Iniciar XAMPP con MySQL en puerto 3306
# Base de datos: saas_platform (se crea automáticamente)
```

### 2. Backend
```bash
mvn spring-boot:run
```
http://localhost:8080

### 3. Frontend
```bash
cd frontend
npm install
npm run dev
```
http://localhost:5174

## 🔐 Credenciales

**Admin:** `admin@saas.com` / `admin123`

**Usuarios:** Registrarse en la aplicación

## Perfiles de BD

Editar `spring.profiles.active` en `application.properties`:
- `dev`: H2 en memoria (por defecto)
- `mysql`: MySQL localhost:3306
