import { useState, useEffect } from 'react';
import { Check, ArrowRight, AlertCircle, Loader2 } from 'lucide-react';
import { planesApi, suscripcionesApi } from '../services/api';

/**
 * Componente para mostrar los planes disponibles y permitir cambiar de plan
 * Valida que los planes (Basic, Premium, Enterprise) se guardan correctamente
 */
function PlanesSelector({ suscripcionId, planActualId, onPlanChanged }) {
  const [planes, setPlanes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [changing, setChanging] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    cargarPlanes();
  }, []);

  const cargarPlanes = async () => {
    try {
      const data = await planesApi.getActivos();
      // Ordenar por precio ascendente
      data.sort((a, b) => a.precioMensual - b.precioMensual);
      setPlanes(data);
    } catch (err) {
      setError('Error al cargar los planes');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleCambiarPlan = async (planId) => {
    if (planId === planActualId) {
      setError('Ya tienes este plan activo');
      return;
    }

    setChanging(true);
    setError('');
    setSuccess('');

    try {
      const resultado = await suscripcionesApi.cambiarPlan(suscripcionId, planId);
      setSuccess(resultado.mensaje || 'Plan actualizado correctamente');
      
      if (onPlanChanged) {
        const planNuevo = planes.find(p => p.id === planId);
        onPlanChanged(planNuevo);
      }
    } catch (err) {
      setError(err.response?.data?.error || 'Error al cambiar de plan');
      console.error(err);
    } finally {
      setChanging(false);
    }
  };

  const getPlanStyle = (tipoPlan) => {
    switch (tipoPlan) {
      case 'BASIC':
        return 'border-gray-200 bg-white';
      case 'PREMIUM':
        return 'border-blue-500 bg-blue-50 ring-2 ring-blue-500';
      case 'ENTERPRISE':
        return 'border-purple-500 bg-purple-50';
      default:
        return 'border-gray-200 bg-white';
    }
  };

  const getButtonStyle = (plan) => {
    if (plan.id === planActualId) {
      return 'bg-green-100 text-green-700 cursor-default';
    }
    if (plan.precioMensual > planes.find(p => p.id === planActualId)?.precioMensual) {
      return 'bg-blue-600 hover:bg-blue-700 text-white';
    }
    return 'bg-gray-200 hover:bg-gray-300 text-gray-700';
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="w-8 h-8 animate-spin text-blue-600" />
      </div>
    );
  }

  return (
    <div className="py-8">
      <h2 className="text-2xl font-bold text-center text-gray-900 mb-2">
        Planes Disponibles
      </h2>
      <p className="text-gray-600 text-center mb-8">
        Selecciona el plan que mejor se adapte a tus necesidades
      </p>

      {/* Mensajes de error/éxito */}
      {error && (
        <div className="max-w-3xl mx-auto mb-6 flex items-center gap-2 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          <AlertCircle className="w-5 h-5 flex-shrink-0" />
          <span>{error}</span>
        </div>
      )}
      
      {success && (
        <div className="max-w-3xl mx-auto mb-6 flex items-center gap-2 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg">
          <Check className="w-5 h-5 flex-shrink-0" />
          <span>{success}</span>
        </div>
      )}

      {/* Grid de planes */}
      <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-6 px-4">
        {planes.map((plan) => (
          <div
            key={plan.id}
            className={`rounded-xl border-2 p-6 transition-all duration-200 ${getPlanStyle(plan.tipoPlan)}`}
          >
            {/* Badge de plan actual */}
            {plan.id === planActualId && (
              <div className="mb-4">
                <span className="bg-green-100 text-green-700 text-xs font-semibold px-3 py-1 rounded-full">
                  Plan Actual
                </span>
              </div>
            )}

            {/* Badge Premium */}
            {plan.tipoPlan === 'PREMIUM' && plan.id !== planActualId && (
              <div className="mb-4">
                <span className="bg-blue-600 text-white text-xs font-semibold px-3 py-1 rounded-full">
                  Más Popular
                </span>
              </div>
            )}

            {/* Nombre y tipo */}
            <h3 className="text-xl font-bold text-gray-900 mb-1">{plan.nombre}</h3>
            <p className="text-sm text-gray-500 mb-4">{plan.tipoPlan}</p>

            {/* Precio */}
            <div className="mb-6">
              <span className="text-4xl font-bold text-gray-900">€{plan.precioMensual}</span>
              <span className="text-gray-500">/mes</span>
            </div>

            {/* Descripción */}
            <p className="text-gray-600 mb-6">{plan.descripcion}</p>

            {/* Características */}
            <ul className="space-y-3 mb-6">
              <li className="flex items-center gap-2 text-sm text-gray-600">
                <Check className="w-4 h-4 text-green-500" />
                {plan.maxUsuarios ? `${plan.maxUsuarios} usuario(s)` : 'Usuarios ilimitados'}
              </li>
              <li className="flex items-center gap-2 text-sm text-gray-600">
                <Check className="w-4 h-4 text-green-500" />
                {plan.almacenamientoGb}GB almacenamiento
              </li>
              {plan.soportePrioritario && (
                <li className="flex items-center gap-2 text-sm text-gray-600">
                  <Check className="w-4 h-4 text-green-500" />
                  Soporte prioritario
                </li>
              )}
            </ul>

            {/* Botón de acción */}
            <button
              onClick={() => handleCambiarPlan(plan.id)}
              disabled={plan.id === planActualId || changing}
              className={`w-full py-3 px-4 rounded-lg font-semibold transition-colors flex items-center justify-center gap-2 ${getButtonStyle(plan)}`}
            >
              {changing ? (
                <Loader2 className="w-5 h-5 animate-spin" />
              ) : plan.id === planActualId ? (
                <>
                  <Check className="w-5 h-5" />
                  Plan Actual
                </>
              ) : (
                <>
                  Cambiar a {plan.nombre}
                  <ArrowRight className="w-4 h-4" />
                </>
              )}
            </button>

            {/* Aviso de prorrateo */}
            {plan.id !== planActualId && 
             plan.precioMensual > (planes.find(p => p.id === planActualId)?.precioMensual || 0) && (
              <p className="text-xs text-gray-500 mt-3 text-center">
                * Se aplicará prorrateo por los días restantes
              </p>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

export default PlanesSelector;
