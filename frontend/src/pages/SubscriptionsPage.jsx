import { useState, useEffect } from 'react';
import { Users, Calendar, CreditCard, CheckCircle, XCircle, Clock, RefreshCw } from 'lucide-react';
import { suscripcionesApi } from '../services/api';

/**
 * Vista de Suscripciones para Admin
 * Muestra todas las suscripciones activas y su estado
 */
export default function SubscriptionsPage() {
  const [suscripciones, setSuscripciones] = useState([]);
  const [estadisticas, setEstadisticas] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    try {
      setLoading(true);
      const [data, stats] = await Promise.all([
        suscripcionesApi.getAll(),
        suscripcionesApi.getEstadisticasCicloVida()
      ]);
      setSuscripciones(data);
      setEstadisticas(stats);
      setError(null);
    } catch (err) {
      setError('Error al cargar las suscripciones');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount || 0);
  };

  const getEstadoBadge = (estado) => {
    const badges = {
      ACTIVA: { class: 'bg-green-100 text-green-800', icon: CheckCircle },
      TRIAL: { class: 'bg-blue-100 text-blue-800', icon: Clock },
      CANCELADA: { class: 'bg-red-100 text-red-800', icon: XCircle },
      EXPIRADA: { class: 'bg-gray-100 text-gray-800', icon: XCircle },
      SUSPENDIDA: { class: 'bg-yellow-100 text-yellow-800', icon: Clock }
    };
    return badges[estado] || { class: 'bg-gray-100 text-gray-800', icon: Clock };
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center py-12">
        <RefreshCw className="w-8 h-8 animate-spin text-blue-600" />
        <span className="ml-2 text-gray-600">Cargando suscripciones...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
        {error}
      </div>
    );
  }

  return (
    <div className="space-y-6">
      {/* Título */}
      <div className="flex items-center justify-between">
        <h2 className="text-2xl font-bold text-gray-900">Suscripciones</h2>
        <button
          onClick={cargarDatos}
          className="flex items-center gap-2 px-4 py-2 text-sm bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
        >
          <RefreshCw className="w-4 h-4" />
          Actualizar
        </button>
      </div>

      {/* Estadísticas */}
      {estadisticas && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-blue-50 rounded-lg">
                <Users className="w-5 h-5 text-blue-600" />
              </div>
              <div>
                <p className="text-sm text-gray-500">Total</p>
                <p className="text-xl font-bold text-gray-900">{estadisticas.totalSuscripciones || 0}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-green-50 rounded-lg">
                <CheckCircle className="w-5 h-5 text-green-600" />
              </div>
              <div>
                <p className="text-sm text-gray-500">Activas</p>
                <p className="text-xl font-bold text-green-600">{estadisticas.activas || 0}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-yellow-50 rounded-lg">
                <Clock className="w-5 h-5 text-yellow-600" />
              </div>
              <div>
                <p className="text-sm text-gray-500">En Trial</p>
                <p className="text-xl font-bold text-yellow-600">{estadisticas.enTrial || 0}</p>
              </div>
            </div>
          </div>

          <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-red-50 rounded-lg">
                <XCircle className="w-5 h-5 text-red-600" />
              </div>
              <div>
                <p className="text-sm text-gray-500">Canceladas</p>
                <p className="text-xl font-bold text-red-600">{estadisticas.canceladas || 0}</p>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Tabla de suscripciones */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Usuario
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Plan
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Estado
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Precio
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Fecha Inicio
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Próx. Facturación
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Renovación
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {suscripciones.length === 0 ? (
                <tr>
                  <td colSpan="7" className="px-6 py-12 text-center text-gray-500">
                    No hay suscripciones registradas
                  </td>
                </tr>
              ) : (
                suscripciones.map((sus) => {
                  const badge = getEstadoBadge(sus.estado);
                  const BadgeIcon = badge.icon;
                  return (
                    <tr key={sus.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="w-8 h-8 bg-indigo-100 rounded-full flex items-center justify-center">
                            <span className="text-sm font-medium text-indigo-700">
                              {sus.usuarioNombre ? sus.usuarioNombre.charAt(0).toUpperCase() : '?'}
                            </span>
                          </div>
                          <div className="ml-3">
                            <p className="text-sm font-medium text-gray-900">
                              {sus.usuarioNombre || 'Sin nombre'}
                            </p>
                            <p className="text-xs text-gray-500">ID: {sus.usuarioId}</p>
                          </div>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <CreditCard className="w-4 h-4 text-gray-400 mr-2" />
                          <span className="text-sm text-gray-900">{sus.planNombre || '-'}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium ${badge.class}`}>
                          <BadgeIcon className="w-3 h-3" />
                          {sus.estado}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {formatCurrency(sus.precioActual)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {formatDate(sus.fechaInicio)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {formatDate(sus.proximaFacturacion)}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        {sus.renovacionAutomatica ? (
                          <span className="inline-flex items-center gap-1 text-green-600">
                            <CheckCircle className="w-4 h-4" />
                            <span className="text-xs">Sí</span>
                          </span>
                        ) : (
                          <span className="inline-flex items-center gap-1 text-red-600">
                            <XCircle className="w-4 h-4" />
                            <span className="text-xs">No</span>
                          </span>
                        )}
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
