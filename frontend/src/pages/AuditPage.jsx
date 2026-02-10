import { useState, useEffect } from 'react';
import { auditoriaApi } from '../services/api';

/**
 * Panel de Auditoría (Admin) - Parte 2
 * Vista especial para ver el historial de cambios usando Hibernate Envers
 */
export default function AuditPage() {
  const [registros, setRegistros] = useState([]);
  const [estadisticas, setEstadisticas] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedEntity, setSelectedEntity] = useState('recientes');
  const [limite, setLimite] = useState(50);
  const [detailModal, setDetailModal] = useState(null);

  useEffect(() => {
    cargarDatos();
  }, [selectedEntity, limite]);

  const cargarDatos = async () => {
    try {
      setLoading(true);
      let data;
      
      switch (selectedEntity) {
        case 'suscripciones':
          data = await auditoriaApi.getHistorialSuscripciones(limite);
          break;
        case 'facturas':
          data = await auditoriaApi.getHistorialFacturas(limite);
          break;
        case 'usuarios':
          data = await auditoriaApi.getHistorialUsuarios(limite);
          break;
        default:
          data = await auditoriaApi.getCambiosRecientes(limite);
      }
      
      setRegistros(data);
      
      // Cargar estadísticas generales
      const stats = await auditoriaApi.getEstadisticas();
      setEstadisticas(stats);
      
      setError(null);
    } catch (err) {
      setError('Error al cargar datos de auditoría');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const formatDateTime = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleString('es-ES', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit'
    });
  };

  const getOperacionBadge = (operacion) => {
    const badges = {
      CREACION: 'bg-green-100 text-green-800',
      MODIFICACION: 'bg-blue-100 text-blue-800',
      ELIMINACION: 'bg-red-100 text-red-800'
    };
    return badges[operacion] || 'bg-gray-100 text-gray-800';
  };

  const getOperacionIcon = (operacion) => {
    const icons = {
      CREACION: '➕',
      MODIFICACION: '✏️',
      ELIMINACION: '🗑️'
    };
    return icons[operacion] || '📝';
  };

  const getEntidadIcon = (entidad) => {
    const icons = {
      Usuario: '👤',
      Suscripcion: '📋',
      Factura: '🧾',
      Plan: '📦',
      Perfil: '📇'
    };
    return icons[entidad] || '📄';
  };

  const verDetalles = (registro) => {
    setDetailModal(registro);
  };

  if (loading && !registros.length) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">Panel de Auditoría (Admin)</h1>
        <div className="flex items-center space-x-4">
          <select
            value={limite}
            onChange={(e) => setLimite(Number(e.target.value))}
            className="border rounded-md px-3 py-2 text-sm"
          >
            <option value={10}>10 registros</option>
            <option value={25}>25 registros</option>
            <option value={50}>50 registros</option>
            <option value={100}>100 registros</option>
          </select>
          <button
            onClick={cargarDatos}
            className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
          >
            Actualizar
          </button>
        </div>
      </div>

      {error && (
        <div className="bg-red-50 border-l-4 border-red-400 p-4">
          <p className="text-red-700">{error}</p>
        </div>
      )}

      {/* Estadísticas de Auditoría */}
      {estadisticas && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="bg-white rounded-lg shadow p-4">
            <h3 className="text-lg font-semibold mb-3">Revisiones por Entidad</h3>
            <div className="space-y-2">
              {Object.entries(estadisticas.revisionesPorEntidad || {}).map(([entidad, count]) => (
                <div key={entidad} className="flex justify-between items-center">
                  <span className="flex items-center">
                    <span className="mr-2">{getEntidadIcon(entidad)}</span>
                    {entidad}
                  </span>
                  <span className="font-medium text-indigo-600">{count}</span>
                </div>
              ))}
            </div>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <h3 className="text-lg font-semibold mb-3">Revisiones por Operación</h3>
            <div className="space-y-2">
              {Object.entries(estadisticas.revisionesPorOperacion || {}).map(([op, count]) => (
                <div key={op} className="flex justify-between items-center">
                  <span className="flex items-center">
                    <span className="mr-2">{getOperacionIcon(op)}</span>
                    <span className={`px-2 py-1 rounded-full text-xs ${getOperacionBadge(op)}`}>
                      {op}
                    </span>
                  </span>
                  <span className="font-medium">{count}</span>
                </div>
              ))}
            </div>
            <div className="mt-4 pt-4 border-t">
              <div className="flex justify-between items-center font-semibold">
                <span>Total Revisiones</span>
                <span className="text-indigo-600">{estadisticas.totalRevisiones}</span>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Filtros por Entidad */}
      <div className="bg-white rounded-lg shadow p-4">
        <h3 className="text-lg font-semibold mb-3">Filtrar por Tipo</h3>
        <div className="flex flex-wrap gap-2">
          {[
            { key: 'recientes', label: 'Cambios Recientes', icon: '🕐' },
            { key: 'suscripciones', label: 'Suscripciones', icon: '📋' },
            { key: 'facturas', label: 'Facturas', icon: '🧾' },
            { key: 'usuarios', label: 'Usuarios', icon: '👤' }
          ].map((item) => (
            <button
              key={item.key}
              onClick={() => setSelectedEntity(item.key)}
              className={`px-4 py-2 rounded-md flex items-center space-x-2 transition-colors ${
                selectedEntity === item.key
                  ? 'bg-indigo-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              <span>{item.icon}</span>
              <span>{item.label}</span>
            </button>
          ))}
        </div>
      </div>

      {/* Historial de Cambios */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <div className="px-6 py-4 border-b bg-gray-50">
          <h3 className="text-lg font-semibold">
            Historial de Cambios
            {selectedEntity !== 'recientes' && ` - ${selectedEntity.charAt(0).toUpperCase() + selectedEntity.slice(1)}`}
          </h3>
        </div>
        <div className="divide-y divide-gray-200">
          {registros.length === 0 ? (
            <div className="px-6 py-8 text-center text-gray-500">
              No hay registros de auditoría
            </div>
          ) : (
            registros.map((registro, index) => (
              <div key={`${registro.numeroRevision}-${index}`} className="px-6 py-4 hover:bg-gray-50">
                <div className="flex items-start justify-between">
                  <div className="flex items-start space-x-4">
                    <div className="flex-shrink-0 w-10 h-10 bg-gray-100 rounded-full flex items-center justify-center text-xl">
                      {getEntidadIcon(registro.tipoEntidad)}
                    </div>
                    <div>
                      <div className="flex items-center space-x-2">
                        <span className="font-medium text-gray-900">
                          {registro.tipoEntidad} #{registro.entityId}
                        </span>
                        <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${getOperacionBadge(registro.tipoOperacion)}`}>
                          {registro.tipoOperacion}
                        </span>
                      </div>
                      <div className="text-sm text-gray-500 mt-1">
                        Revisión #{registro.numeroRevision} • {formatDateTime(registro.fechaCambio)}
                      </div>
                      {/* Mostrar algunos detalles */}
                      {registro.detalles && Object.keys(registro.detalles).length > 0 && (
                        <div className="mt-2 text-sm text-gray-600">
                          {Object.entries(registro.detalles).slice(0, 3).map(([key, value]) => (
                            <span key={key} className="mr-4">
                              <span className="text-gray-400">{key}:</span> {value || '-'}
                            </span>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                  <button
                    onClick={() => verDetalles(registro)}
                    className="text-indigo-600 hover:text-indigo-800 text-sm font-medium"
                  >
                    Ver Detalles
                  </button>
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Modal de Detalles */}
      {detailModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-auto">
            <div className="px-6 py-4 border-b flex justify-between items-center">
              <h3 className="text-lg font-semibold">
                Detalles de la Revisión #{detailModal.numeroRevision}
              </h3>
              <button
                onClick={() => setDetailModal(null)}
                className="text-gray-400 hover:text-gray-600"
              >
                ✕
              </button>
            </div>
            <div className="px-6 py-4 space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm text-gray-500">Entidad</label>
                  <div className="font-medium">{detailModal.tipoEntidad}</div>
                </div>
                <div>
                  <label className="text-sm text-gray-500">ID</label>
                  <div className="font-medium">{detailModal.entityId}</div>
                </div>
                <div>
                  <label className="text-sm text-gray-500">Operación</label>
                  <div>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getOperacionBadge(detailModal.tipoOperacion)}`}>
                      {detailModal.tipoOperacion}
                    </span>
                  </div>
                </div>
                <div>
                  <label className="text-sm text-gray-500">Fecha</label>
                  <div className="font-medium">{formatDateTime(detailModal.fechaCambio)}</div>
                </div>
              </div>
              
              <div>
                <label className="text-sm text-gray-500 mb-2 block">Datos de la Entidad</label>
                <div className="bg-gray-50 rounded-lg p-4">
                  {detailModal.detalles && Object.keys(detailModal.detalles).length > 0 ? (
                    <table className="w-full">
                      <tbody>
                        {Object.entries(detailModal.detalles).map(([key, value]) => (
                          <tr key={key} className="border-b border-gray-200 last:border-0">
                            <td className="py-2 text-sm text-gray-500 w-1/3">{key}</td>
                            <td className="py-2 text-sm font-medium text-gray-900">{value || '-'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  ) : (
                    <p className="text-gray-500 text-sm">No hay detalles disponibles</p>
                  )}
                </div>
              </div>
            </div>
            <div className="px-6 py-4 border-t bg-gray-50">
              <button
                onClick={() => setDetailModal(null)}
                className="w-full px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300"
              >
                Cerrar
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
