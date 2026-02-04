import { useState, useEffect } from 'react';
import { 
  Plus, Edit2, Trash2, X, Save, AlertCircle, 
  CheckCircle, CreditCard, Loader2, Sparkles, Building2, Zap,
  HardDrive, Users, HeadphonesIcon
} from 'lucide-react';
import { planesApi } from '../services/api';

/**
 * Página de gestión de Planes - CRUD completo
 * Planes disponibles: BASIC, PREMIUM, ENTERPRISE
 */
const PlanesPage = () => {
  // Estado principal
  const [planes, setPlanes] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Estado del modal
  const [showModal, setShowModal] = useState(false);
  const [modalMode, setModalMode] = useState('create');
  const [currentPlan, setCurrentPlan] = useState(null);
  
  // Estado del formulario
  const [formData, setFormData] = useState({
    nombre: '',
    tipoPlan: 'BASIC',
    precioMensual: '',
    descripcion: '',
    caracteristicas: '',
    maxUsuarios: '',
    almacenamientoGb: '',
    soportePrioritario: false,
    activo: true,
  });
  const [formErrors, setFormErrors] = useState({});
  const [saving, setSaving] = useState(false);
  
  // Estado de notificaciones
  const [notification, setNotification] = useState(null);

  // Cargar planes al montar
  useEffect(() => {
    loadPlanes();
  }, []);

  // Ocultar notificación después de 3 segundos
  useEffect(() => {
    if (notification) {
      const timer = setTimeout(() => setNotification(null), 3000);
      return () => clearTimeout(timer);
    }
  }, [notification]);

  const loadPlanes = async () => {
    try {
      setLoading(true);
      const data = await planesApi.getAll();
      setPlanes(data);
    } catch (error) {
      showNotification('error', 'Error al cargar los planes');
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const showNotification = (type, message) => {
    setNotification({ type, message });
  };

  // Icono según tipo de plan
  const getPlanIcon = (tipoPlan) => {
    switch (tipoPlan) {
      case 'BASIC':
        return <Zap className="w-6 h-6" />;
      case 'PREMIUM':
        return <Sparkles className="w-6 h-6" />;
      case 'ENTERPRISE':
        return <Building2 className="w-6 h-6" />;
      default:
        return <CreditCard className="w-6 h-6" />;
    }
  };

  // Color según tipo de plan
  const getPlanColor = (tipoPlan) => {
    switch (tipoPlan) {
      case 'BASIC':
        return 'from-blue-500 to-blue-600';
      case 'PREMIUM':
        return 'from-purple-500 to-purple-600';
      case 'ENTERPRISE':
        return 'from-amber-500 to-amber-600';
      default:
        return 'from-gray-500 to-gray-600';
    }
  };

  // Descripción del tipo de plan
  const getTipoPlanLabel = (tipoPlan) => {
    switch (tipoPlan) {
      case 'BASIC':
        return 'Plan Básico';
      case 'PREMIUM':
        return 'Plan Premium';
      case 'ENTERPRISE':
        return 'Plan Empresarial';
      default:
        return tipoPlan;
    }
  };

  // Abrir modal para crear
  const handleCreate = () => {
    setModalMode('create');
    setFormData({
      nombre: '',
      tipoPlan: 'BASIC',
      precioMensual: '',
      descripcion: '',
      caracteristicas: '',
      maxUsuarios: '',
      almacenamientoGb: '',
      soportePrioritario: false,
      activo: true,
    });
    setFormErrors({});
    setCurrentPlan(null);
    setShowModal(true);
  };

  // Abrir modal para editar
  const handleEdit = (plan) => {
    setModalMode('edit');
    setFormData({
      nombre: plan.nombre || '',
      tipoPlan: plan.tipoPlan || 'BASIC',
      precioMensual: plan.precioMensual?.toString() || '',
      descripcion: plan.descripcion || '',
      caracteristicas: plan.caracteristicas || '',
      maxUsuarios: plan.maxUsuarios?.toString() || '',
      almacenamientoGb: plan.almacenamientoGb?.toString() || '',
      soportePrioritario: plan.soportePrioritario ?? false,
      activo: plan.activo ?? true,
    });
    setFormErrors({});
    setCurrentPlan(plan);
    setShowModal(true);
  };

  // Validar formulario
  const validateForm = () => {
    const errors = {};
    
    if (!formData.nombre.trim()) {
      errors.nombre = 'El nombre es obligatorio';
    }
    
    if (!formData.tipoPlan) {
      errors.tipoPlan = 'El tipo de plan es obligatorio';
    }
    
    if (!formData.precioMensual) {
      errors.precioMensual = 'El precio es obligatorio';
    } else if (isNaN(parseFloat(formData.precioMensual)) || parseFloat(formData.precioMensual) < 0) {
      errors.precioMensual = 'El precio debe ser un número positivo';
    }
    
    if (formData.maxUsuarios && (isNaN(parseInt(formData.maxUsuarios)) || parseInt(formData.maxUsuarios) < 1)) {
      errors.maxUsuarios = 'Debe ser un número mayor a 0';
    }
    
    if (formData.almacenamientoGb && (isNaN(parseInt(formData.almacenamientoGb)) || parseInt(formData.almacenamientoGb) < 1)) {
      errors.almacenamientoGb = 'Debe ser un número mayor a 0';
    }
    
    setFormErrors(errors);
    return Object.keys(errors).length === 0;
  };

  // Guardar plan
  const handleSave = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) return;
    
    try {
      setSaving(true);
      
      const dataToSend = {
        ...formData,
        precioMensual: parseFloat(formData.precioMensual),
        maxUsuarios: formData.maxUsuarios ? parseInt(formData.maxUsuarios) : null,
        almacenamientoGb: formData.almacenamientoGb ? parseInt(formData.almacenamientoGb) : null,
      };
      
      if (modalMode === 'create') {
        await planesApi.create(dataToSend);
        showNotification('success', 'Plan creado correctamente');
      } else {
        await planesApi.update(currentPlan.id, dataToSend);
        showNotification('success', 'Plan actualizado correctamente');
      }
      
      setShowModal(false);
      loadPlanes();
    } catch (error) {
      const message = error.response?.data?.message || 'Error al guardar el plan';
      showNotification('error', message);
      console.error(error);
    } finally {
      setSaving(false);
    }
  };

  // Eliminar plan
  const handleDelete = async (plan) => {
    if (!window.confirm(`¿Eliminar el plan "${plan.nombre}"?`)) return;
    
    try {
      await planesApi.delete(plan.id);
      showNotification('success', 'Plan eliminado correctamente');
      loadPlanes();
    } catch (error) {
      showNotification('error', 'Error al eliminar el plan. Puede tener suscripciones asociadas.');
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
          <h1 className="text-2xl font-bold text-gray-900">Planes de Suscripción</h1>
          <p className="text-gray-600 mt-1">Configura los planes Basic, Premium y Enterprise</p>
        </div>
        <button onClick={handleCreate} className="btn-primary">
          <Plus className="w-4 h-4 mr-2" />
          Nuevo Plan
        </button>
      </div>

      {/* Grid de planes */}
      {loading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="w-8 h-8 animate-spin text-primary-600" />
          <span className="ml-2 text-gray-600">Cargando planes...</span>
        </div>
      ) : planes.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-xl border border-gray-200">
          <CreditCard className="w-12 h-12 mx-auto text-gray-400 mb-4" />
          <p className="text-gray-600">No hay planes configurados</p>
          <button onClick={handleCreate} className="mt-4 text-primary-600 hover:text-primary-700 font-medium">
            Crear el primer plan
          </button>
        </div>
      ) : (
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {planes.map((plan) => (
            <div
              key={plan.id}
              className={`bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden
                         ${!plan.activo ? 'opacity-60' : ''}`}
            >
              {/* Header del plan */}
              <div className={`bg-gradient-to-r ${getPlanColor(plan.tipoPlan)} p-6 text-white`}>
                <div className="flex items-center justify-between">
                  <div className="flex items-center">
                    {getPlanIcon(plan.tipoPlan)}
                    <span className="ml-2 text-sm font-medium opacity-90">
                      {getTipoPlanLabel(plan.tipoPlan)}
                    </span>
                  </div>
                  {!plan.activo && (
                    <span className="text-xs bg-white/20 px-2 py-1 rounded">Inactivo</span>
                  )}
                </div>
                <h3 className="text-xl font-bold mt-3">{plan.nombre}</h3>
                <div className="mt-2">
                  <span className="text-3xl font-bold">{plan.precioMensual?.toFixed(2)}€</span>
                  <span className="text-sm opacity-90">/mes</span>
                </div>
              </div>

              {/* Contenido del plan */}
              <div className="p-6">
                {plan.descripcion && (
                  <p className="text-gray-600 text-sm mb-4">{plan.descripcion}</p>
                )}
                
                <ul className="space-y-3">
                  {plan.maxUsuarios && (
                    <li className="flex items-center text-sm text-gray-700">
                      <Users className="w-4 h-4 mr-3 text-gray-400" />
                      Hasta {plan.maxUsuarios} usuarios
                    </li>
                  )}
                  {plan.almacenamientoGb && (
                    <li className="flex items-center text-sm text-gray-700">
                      <HardDrive className="w-4 h-4 mr-3 text-gray-400" />
                      {plan.almacenamientoGb} GB de almacenamiento
                    </li>
                  )}
                  {plan.soportePrioritario && (
                    <li className="flex items-center text-sm text-gray-700">
                      <HeadphonesIcon className="w-4 h-4 mr-3 text-gray-400" />
                      Soporte prioritario
                    </li>
                  )}
                </ul>

                {/* Acciones */}
                <div className="flex justify-end mt-6 pt-4 border-t border-gray-100 space-x-2">
                  <button
                    onClick={() => handleEdit(plan)}
                    className="btn-secondary py-2 px-3"
                    title="Editar"
                  >
                    <Edit2 className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => handleDelete(plan)}
                    className="btn-danger py-2 px-3"
                    title="Eliminar"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

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
            <div className="relative bg-white rounded-xl shadow-xl max-w-lg w-full p-6 animate-fade-in max-h-[90vh] overflow-y-auto">
              <div className="flex items-center justify-between mb-6">
                <h2 className="text-xl font-semibold text-gray-900">
                  {modalMode === 'create' ? 'Nuevo Plan' : 'Editar Plan'}
                </h2>
                <button
                  onClick={() => setShowModal(false)}
                  className="text-gray-400 hover:text-gray-600 p-1"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>

              <form onSubmit={handleSave} className="space-y-4">
                {/* Nombre */}
                <div>
                  <label className="form-label">Nombre del Plan *</label>
                  <input
                    type="text"
                    name="nombre"
                    value={formData.nombre}
                    onChange={handleInputChange}
                    className={`form-input ${formErrors.nombre ? 'border-red-500' : ''}`}
                    placeholder="Ej: Plan Profesional"
                  />
                  {formErrors.nombre && <p className="form-error">{formErrors.nombre}</p>}
                </div>

                {/* Tipo de Plan */}
                <div>
                  <label className="form-label">Tipo de Plan *</label>
                  <select
                    name="tipoPlan"
                    value={formData.tipoPlan}
                    onChange={handleInputChange}
                    className={`form-input ${formErrors.tipoPlan ? 'border-red-500' : ''}`}
                  >
                    <option value="BASIC">Básico (BASIC)</option>
                    <option value="PREMIUM">Premium (PREMIUM)</option>
                    <option value="ENTERPRISE">Empresarial (ENTERPRISE)</option>
                  </select>
                  {formErrors.tipoPlan && <p className="form-error">{formErrors.tipoPlan}</p>}
                </div>

                {/* Precio Mensual */}
                <div>
                  <label className="form-label">Precio Mensual (€) *</label>
                  <input
                    type="number"
                    name="precioMensual"
                    value={formData.precioMensual}
                    onChange={handleInputChange}
                    step="0.01"
                    min="0"
                    className={`form-input ${formErrors.precioMensual ? 'border-red-500' : ''}`}
                    placeholder="29.99"
                  />
                  {formErrors.precioMensual && <p className="form-error">{formErrors.precioMensual}</p>}
                </div>

                {/* Descripción */}
                <div>
                  <label className="form-label">Descripción</label>
                  <textarea
                    name="descripcion"
                    value={formData.descripcion}
                    onChange={handleInputChange}
                    rows={2}
                    className="form-input"
                    placeholder="Descripción breve del plan..."
                  />
                </div>

                {/* Max Usuarios y Almacenamiento */}
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="form-label">Máx. Usuarios</label>
                    <input
                      type="number"
                      name="maxUsuarios"
                      value={formData.maxUsuarios}
                      onChange={handleInputChange}
                      min="1"
                      className={`form-input ${formErrors.maxUsuarios ? 'border-red-500' : ''}`}
                      placeholder="10"
                    />
                    {formErrors.maxUsuarios && <p className="form-error">{formErrors.maxUsuarios}</p>}
                  </div>
                  <div>
                    <label className="form-label">Almacenamiento (GB)</label>
                    <input
                      type="number"
                      name="almacenamientoGb"
                      value={formData.almacenamientoGb}
                      onChange={handleInputChange}
                      min="1"
                      className={`form-input ${formErrors.almacenamientoGb ? 'border-red-500' : ''}`}
                      placeholder="50"
                    />
                    {formErrors.almacenamientoGb && <p className="form-error">{formErrors.almacenamientoGb}</p>}
                  </div>
                </div>

                {/* Características */}
                <div>
                  <label className="form-label">Características adicionales</label>
                  <textarea
                    name="caracteristicas"
                    value={formData.caracteristicas}
                    onChange={handleInputChange}
                    rows={3}
                    className="form-input"
                    placeholder="Una característica por línea..."
                  />
                </div>

                {/* Checkboxes */}
                <div className="flex flex-wrap gap-6">
                  <div className="flex items-center">
                    <input
                      type="checkbox"
                      name="soportePrioritario"
                      id="soportePrioritario"
                      checked={formData.soportePrioritario}
                      onChange={handleInputChange}
                      className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                    />
                    <label htmlFor="soportePrioritario" className="ml-2 text-sm text-gray-700">
                      Soporte prioritario
                    </label>
                  </div>
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
                      Plan activo
                    </label>
                  </div>
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

export default PlanesPage;
