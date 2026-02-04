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
};

export const planesApi = {
  getAll: async () => (await api.get('/planes')).data,
  getActivos: async () => (await api.get('/planes/activos')).data,
  getById: async (id) => (await api.get(`/planes/${id}`)).data,
  create: async (plan) => (await api.post('/planes', plan)).data,
  update: async (id, plan) => (await api.put(`/planes/${id}`, plan)).data,
  delete: async (id) => await api.delete(`/planes/${id}`),
};

export default api;
