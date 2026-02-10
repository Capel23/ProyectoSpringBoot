import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

export const usuariosApi = {
  getAll: async () => (await api.get('/usuarios')).data,
  getById: async (id) => (await api.get(`/usuarios/${id}`)).data,
  create: async (usuario) => (await api.post('/usuarios', usuario)).data,
  update: async (id, usuario) => (await api.put(`/usuarios/${id}`, usuario)).data,
  delete: async (id) => await api.delete(`/usuarios/${id}`),
  login: async (email, password) => (await api.post('/usuarios/login', { email, password })).data,
};

export const planesApi = {
  getAll: async () => (await api.get('/planes')).data,
  getActivos: async () => (await api.get('/planes/activos')).data,
  getById: async (id) => (await api.get(`/planes/${id}`)).data,
  create: async (plan) => (await api.post('/planes', plan)).data,
  update: async (id, plan) => (await api.put(`/planes/${id}`, plan)).data,
  delete: async (id) => await api.delete(`/planes/${id}`),
};

export const suscripcionesApi = {
  getAll: async () => (await api.get('/suscripciones')).data,
  getById: async (id) => (await api.get(`/suscripciones/${id}`)).data,
  getByUsuario: async (usuarioId) => (await api.get(`/suscripciones/usuario/${usuarioId}`)).data,
  create: async (suscripcion) => (await api.post('/suscripciones', suscripcion)).data,
  cambiarPlan: async (id, planId) => (await api.post(`/suscripciones/${id}/cambiar-plan`, { planId })).data,
  cambiarEstado: async (id, estado) => (await api.patch(`/suscripciones/${id}/estado`, { estado })).data,
  delete: async (id) => await api.delete(`/suscripciones/${id}`),
  // Ciclo de vida
  cancelar: async (id, motivo) => (await api.post(`/suscripciones/ciclo-vida/${id}/cancelar?motivo=${encodeURIComponent(motivo)}`)).data,
  reactivar: async (id) => (await api.post(`/suscripciones/ciclo-vida/${id}/reactivar`)).data,
  toggleRenovacion: async (id, renovacion) => (await api.post(`/suscripciones/ciclo-vida/${id}/toggle-renovacion?renovacionAutomatica=${renovacion}`)).data,
  getEstadisticasCicloVida: async () => (await api.get('/suscripciones/ciclo-vida/estadisticas')).data,
};

export const facturasApi = {
  getAll: async () => (await api.get('/facturas')).data,
  getById: async (id) => (await api.get(`/facturas/${id}`)).data,
  getBySuscripcion: async (suscripcionId) => (await api.get(`/facturas/suscripcion/${suscripcionId}`)).data,
  getPendientes: async () => (await api.get('/facturas/pendientes')).data,
  marcarPagada: async (id) => (await api.post(`/facturas/${id}/pagar`)).data,
  ejecutarFacturacion: async () => (await api.post('/facturas/ejecutar-facturacion')).data,
  // Filtros avanzados (Parte 2)
  getVencidas: async () => (await api.get('/facturas/vencidas')).data,
  filtrarPorFecha: async (inicio, fin, estado) => {
    const params = new URLSearchParams({ inicio, fin });
    if (estado) params.append('estado', estado);
    return (await api.get(`/facturas/filtrar/fecha?${params}`)).data;
  },
  filtrarPorMonto: async (minimo, maximo, estado) => {
    const params = new URLSearchParams({ minimo, maximo });
    if (estado) params.append('estado', estado);
    return (await api.get(`/facturas/filtrar/monto?${params}`)).data;
  },
  buscar: async (filtros) => {
    const params = new URLSearchParams();
    Object.entries(filtros).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        params.append(key, value);
      }
    });
    return (await api.get(`/facturas/buscar?${params}`)).data;
  },
  getEstadisticas: async () => (await api.get('/facturas/estadisticas')).data,
  getResumenPorEstado: async () => (await api.get('/facturas/resumen-estado')).data,
  getTotales: async (inicio, fin) => (await api.get(`/facturas/totales?inicio=${inicio}&fin=${fin}`)).data,
};

// API de Auditoría (Parte 2)
export const auditoriaApi = {
  getCambiosRecientes: async (limite = 50) => (await api.get(`/auditoria/recientes?limite=${limite}`)).data,
  getHistorialSuscripciones: async (limite = 50) => (await api.get(`/auditoria/suscripciones?limite=${limite}`)).data,
  getHistorialFacturas: async (limite = 50) => (await api.get(`/auditoria/facturas?limite=${limite}`)).data,
  getHistorialUsuarios: async (limite = 50) => (await api.get(`/auditoria/usuarios?limite=${limite}`)).data,
  getHistorialEntidad: async (tipo, id) => (await api.get(`/auditoria/entidad/${tipo}/${id}`)).data,
  compararRevisiones: async (tipoEntidad, entityId, revisionAnterior, revisionActual) => 
    (await api.get(`/auditoria/comparar?tipoEntidad=${tipoEntidad}&entityId=${entityId}&revisionAnterior=${revisionAnterior}&revisionActual=${revisionActual}`)).data,
  getEstadisticas: async () => (await api.get('/auditoria/estadisticas')).data,
};

export default api;
