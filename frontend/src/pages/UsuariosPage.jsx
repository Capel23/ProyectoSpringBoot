import { useState, useEffect } from 'react';
import { 
  Plus, Edit2, Trash2, Search, X, Save, AlertCircle, 
  CheckCircle, User, Mail, Phone, Globe, Loader2 
} from 'lucide-react';
import { usuariosApi } from '../services/api';

/**
 * Página de gestión de Usuarios - CRUD completo
 * Cumple criterios:
 * - Interfaz responsiva y amigable
 * - Feedback visual claro ante errores
 * - Validación de formularios
 */
const UsuariosPage = () => {
  // Estado principal
  const [usuarios, setUsuarios] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  
  // Estado del modal
  const [showModal, setShowModal] = useState(false);
  const [modalMode, setModalMode] = useState('create'); // 'create' | 'edit'
  const [currentUsuario, setCurrentUsuario] = useState(null);
  
  // Estado del formulario
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    nombre: '',
    apellidos: '',
    telefono: '',
    pais: '',
    activo: true,
  });
  const [formErrors, setFormErrors] = useState({});
  const [saving, setSaving] = useState(false);
  
  // Estado de notificaciones
  const [notification, setNotification] = useState(null);

  // Cargar usuarios al montar
  useEffect(() => {
    loadUsuarios();
  }, []);

  // Ocultar notificación después de 3 segundos
  useEffect(() => {
    if (notification) {
      const timer = setTimeout(() => setNotification(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [notification]);

  const loadUsuarios = async () => {
    try {
      setLoading(true);
      const data = await usuariosApi.getAll();
      setUsuarios(data);
    } catch (error) {
      showNotification('error', 'Error al cargar los usuarios');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const showNotification = (type, message) => {
    setNotification({ type, message });
  };

  // Filtrar usuarios por búsqueda
  const filteredUsuarios = usuarios.filter(usuario =>
    usuario.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    usuario.nombre?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    usuario.apellidos?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Abrir modal para crear
  const handleCreate = () => {
    setModalMode('create');
    setFormData({
      email: '',
      password: '',
      nombre: '',
      apellidos: '',
      telefono: '',
      pais: '',
      activo: true,
    });
    setFormErrors({});
    setCurrentUsuario(null);
    setShowModal(true);
  };

  // Abrir modal para editar
  const handleEdit = (usuario) => {
    setModalMode('edit');
    setFormData({
      email: usuario.email || '',
      password: '',
      nombre: usuario.nombre || '',
      apellidos: usuario.apellidos || '',
      telefono: usuario.telefono || '',
      pais: usuario.pais || '',
      activo: usuario.activo ?? true,
    });
    setFormErrors({});
    setCurrentUsuario(usuario);
    setShowModal(true);
  };

  // Validar formulario
  const validateForm = () => {
    const errors = {};
    
    if (!formData.email.trim()) {
      errors.email = 'El email es obligatorio';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      errors.email = 'El email no es válido';
    }
    
    if (modalMode === 'create' && !formData.password.trim()) {
      errors.password = 'La contraseña es obligatoria';
    } else if (formData.password && formData.password.length < 8) {
      errors.password = 'Mínimo 8 caracteres';
    }
    
    if (!formData.nombre.trim()) {
      errors.nombre = 'El nombre es obligatorio';
    }
    
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Guardar usuario (crear o editar)
  const handleSave = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    try {
      setSaving(true);
      
      const dataToSend = { ...formData };
      if (modalMode === 'edit' && !dataToSend.password) {
        delete dataToSend.password;
      }
      
      if (modalMode === 'create') {
        await usuariosApi.create(dataToSend);
        showNotification('success', 'Usuario creado correctamente');
      } else {
        await usuariosApi.update(currentUsuario.id, dataToSend);
        showNotification('success', 'Usuario actualizado correctamente');
      }
      
      setShowModal(false);
      loadUsuarios();
    } catch (error) {
      const message = error.response?.data?.message || 'Error al guardar el usuario';
      showNotification('error', message);
      console.error(error);
    } finally {
      setSaving(false);
    }
  };

  // Eliminar usuario
  const handleDelete = async (usuario) => {
    if (!window.confirm(`¿Eliminar al usuario ${usuario.email}?`)) return;
    
    try {
      await usuariosApi.delete(usuario.id);
      showNotification('success', 'Usuario eliminado correctamente');
      loadUsuarios();
    } catch (error) {
      showNotification('error', 'Error al eliminar el usuario');
      console.error(error);
    }
  };

  // Manejar cambios en el formulario
  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
    // Limpiar error del campo al escribir
    if (formErrors[name]) {
      setFormErrors(prev => ({ ...prev, [name]: null }));
    }
  };

  return (
    <div className="space-y-6">
      {/* Notificación */}
      {notification && (
        <div className={`fixed top-4 right-4 z-50 flex items-center px-4 py-3 rounded-lg shadow-lg animate-fade-in
          ${notification.type === 'success' ? 'bg-green-500 text-white' : 'bg-red-500 text-white'}`}
        >
          {notification.type === 'success' ? (
            <CheckCircle className="w-5 h-5 mr-2" />
          ) : (
            <AlertCircle className="w-5 h-5 mr-2" />
          )}
          {notification.message}
        </div>
      )}

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Usuarios</h1>
          <p className="text-gray-600 mt-1">Gestiona los usuarios del sistema</p>
        </div>
        <button onClick={handleCreate} className="btn-primary">
          <Plus className="w-4 h-4 mr-2" />
          Nuevo Usuario
        </button>
      </div>

      {/* Barra de búsqueda */}
      <div className="relative">
        <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
        <input
          type="text"
          placeholder="Buscar por email, nombre o apellidos..."
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 
                     focus:ring-primary-500 focus:border-primary-500"
        />
        {searchTerm && (
          <button
            onClick={() => setSearchTerm('')}
            className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
          >
            <X className="w-5 h-5" />
          </button>
        )}
      </div>

      {/* Tabla de usuarios */}
      <div className="table-container">
        {loading ? (
          <div className="flex items-center justify-center py-12">
            <Loader2 className="w-8 h-8 animate-spin text-primary-600" />
            <span className="ml-2 text-gray-600">Cargando usuarios...</span>
          </div>
        ) : filteredUsuarios.length === 0 ? (
          <div className="text-center py-12">
            <User className="w-12 h-12 mx-auto text-gray-400 mb-4" />
            <p className="text-gray-600">
              {searchTerm ? 'No se encontraron usuarios' : 'No hay usuarios registrados'}
            </p>
            {!searchTerm && (
              <button onClick={handleCreate} className="mt-4 text-primary-600 hover:text-primary-700 font-medium">
                Crear el primer usuario
              </button>
            )}
          </div>
        ) : (
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Usuario
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Contacto
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  País
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Estado
                </th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredUsuarios.map((usuario) => (
                <tr key={usuario.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <div className="w-10 h-10 bg-primary-100 rounded-full flex items-center justify-center">
                        <span className="text-primary-700 font-medium text-sm">
                          {(usuario.nombre?.[0] || usuario.email?.[0] || '?').toUpperCase()}
                        </span>
                      </div>
                      <div className="ml-4">
                        <div className="text-sm font-medium text-gray-900">
                          {usuario.nombre} {usuario.apellidos}
                        </div>
                        <div className="text-sm text-gray-500 flex items-center">
                          <Mail className="w-3 h-3 mr-1" />
                          {usuario.email}
                        </div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {usuario.telefono ? (
                      <div className="text-sm text-gray-600 flex items-center">
                        <Phone className="w-4 h-4 mr-1 text-gray-400" />
                        {usuario.telefono}
                      </div>
                    ) : (
                      <span className="text-sm text-gray-400">—</span>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {usuario.pais ? (
                      <div className="text-sm text-gray-600 flex items-center">
                        <Globe className="w-4 h-4 mr-1 text-gray-400" />
                        {usuario.pais}
                      </div>
                    ) : (
                      <span className="text-sm text-gray-400">—</span>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium
                      ${usuario.activo 
                        ? 'bg-green-100 text-green-800' 
                        : 'bg-red-100 text-red-800'}`}
                    >
                      {usuario.activo ? 'Activo' : 'Inactivo'}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right">
                    <button
                      onClick={() => handleEdit(usuario)}
                      className="text-primary-600 hover:text-primary-900 p-2 rounded-lg hover:bg-primary-50"
                      title="Editar"
                    >
                      <Edit2 className="w-4 h-4" />
                    </button>
                    <button
                      onClick={() => handleDelete(usuario)}
                      className="text-red-600 hover:text-red-900 p-2 rounded-lg hover:bg-red-50 ml-1"
                      title="Eliminar"
                    >
                      <Trash2 className="w-4 h-4" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {/* Modal de creación/edición */}
      {showModal && (
        <div className="fixed inset-0 z-50 overflow-y-auto">
          <div className="flex items-center justify-center min-h-screen px-4">
            {/* Backdrop */}
            <div 
              className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
              onClick={() => setShowModal(false)}
            />
            
            {/* Modal */}
            <div className="relative bg-white rounded-xl shadow-xl max-w-md w-full p-6 animate-fade-in">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-xl font-semibold text-gray-900">
                  {modalMode === 'create' ? 'Nuevo Usuario' : 'Editar Usuario'}
                </h2>
                <button
                  onClick={() => setShowModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <form onSubmit={handleSave} className="space-y-4">
                {/* Email */}
                <div>
                  <label className="form-label">Email *</label>
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleInputChange}
                    className={`form-input ${formErrors.email ? 'border-red-500' : ''}`}
                    placeholder="usuario@ejemplo.com"
                  />
                  {formErrors.email && <p className="form-error">{formErrors.email}</p>}
                </div>

                {/* Password */}
                <div>
                  <label className="form-label">
                    Contraseña {modalMode === 'create' ? '*' : '(dejar vacío para mantener)'}
                  </label>
                  <input
                    type="password"
                    name="password"
                    value={formData.password}
                    onChange={handleInputChange}
                    className={`form-input ${formErrors.password ? 'border-red-500' : ''}`}
                    placeholder="Mínimo 8 caracteres"
                  />
                  {formErrors.password && <p className="form-error">{formErrors.password}</p>}
                </div>

                {/* Nombre y Apellidos */}
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="form-label">Nombre *</label>
                    <input
                      type="text"
                      name="nombre"
                      value={formData.nombre}
                      onChange={handleInputChange}
                      className={`form-input ${formErrors.nombre ? 'border-red-500' : ''}`}
                      placeholder="Juan"
                    />
                    {formErrors.nombre && <p className="form-error">{formErrors.nombre}</p>}
                  </div>
                  <div>
                    <label className="form-label">Apellidos</label>
                    <input
                      type="text"
                      name="apellidos"
                      value={formData.apellidos}
                      onChange={handleInputChange}
                      className="form-input"
                      placeholder="García López"
                    />
                  </div>
                </div>

                {/* Teléfono y País */}
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="form-label">Teléfono</label>
                    <input
                      type="tel"
                      name="telefono"
                      value={formData.telefono}
                      onChange={handleInputChange}
                      className="form-input"
                      placeholder="+34 600 000 000"
                    />
                  </div>
                  <div>
                    <label className="form-label">País</label>
                    <input
                      type="text"
                      name="pais"
                      value={formData.pais}
                      onChange={handleInputChange}
                      className="form-input"
                      placeholder="España"
                    />
                  </div>
                </div>

                {/* Activo */}
                <div className="flex items-center">
                  <input
                    type="checkbox"
                    name="activo"
                    id="activo"
                    checked={formData.activo}
                    onChange={handleInputChange}
                    className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                  />
                  <label htmlFor="activo" className="ml-2 text-sm text-gray-700">
                    Usuario activo
                  </label>
                </div>

                {/* Botones */}
                <div className="flex justify-end space-x-3 pt-4">
                  <button
                    type="button"
                    onClick={() => setShowModal(false)}
                    className="btn-secondary"
                    disabled={saving}
                  >
                    Cancelar
                  </button>
                  <button
                    type="submit"
                    className="btn-primary"
                    disabled={saving}
                  >
                    {saving ? (
                      <>
                        <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                        Guardando...
                      </>
                    ) : (
                      <>
                        <Save className="w-4 h-4 mr-2" />
                        {modalMode === 'create' ? 'Crear' : 'Guardar'}
                      </>
                    )}
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UsuariosPage;
