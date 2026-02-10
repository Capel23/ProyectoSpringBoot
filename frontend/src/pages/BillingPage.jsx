import { useState, useEffect } from 'react';
import { facturasApi } from '../services/api';

/**
 * Vista de Facturación - Parte 2
 * Muestra facturas generadas con impuestos calculados y filtros avanzados
 * @param {boolean} isAdmin - Si true, muestra todas las facturas. Si false, solo las del usuario.
 * @param {number} suscripcionId - ID de suscripción del usuario (solo se usa si no es admin)
 */
export default function BillingPage({ isAdmin = false, suscripcionId = null }) {
  const [facturas, setFacturas] = useState([]);
  const [estadisticas, setEstadisticas] = useState(null);
  const [resumenEstado, setResumenEstado] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Filtros
  const [filtros, setFiltros] = useState({
    fechaInicio: '',
    fechaFin: '',
    montoMinimo: '',
    montoMaximo: '',
    estado: '',
    page: 0,
    size: 10
  });
  
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    cargarDatos();
  }, [isAdmin, suscripcionId]);

  const cargarDatos = async () => {
    try {
      setLoading(true);
      
      // Si es admin, cargar todas. Si no, solo las del usuario.
      let facturasData;
      if (isAdmin) {
        const [data, stats, resumen] = await Promise.all([
          facturasApi.getAll(),
          facturasApi.getEstadisticas(),
          facturasApi.getResumenPorEstado()
        ]);
        facturasData = data;
        setEstadisticas(stats);
        setResumenEstado(resumen);
      } else if (suscripcionId) {
        facturasData = await facturasApi.getBySuscripcion(suscripcionId);
        setEstadisticas(null);
        setResumenEstado([]);
      } else {
        facturasData = [];
      }
      
      setFacturas(facturasData);
      setError(null);
    } catch (err) {
      setError('Error al cargar los datos de facturación');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const aplicarFiltros = async () => {
    try {
      setLoading(true);
      const resultado = await facturasApi.buscar(filtros);
      setFacturas(resultado.content || []);
      setTotalPages(resultado.totalPages || 0);
      setError(null);
    } catch (err) {
      setError('Error al aplicar filtros');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const limpiarFiltros = () => {
    setFiltros({
      fechaInicio: '',
      fechaFin: '',
      montoMinimo: '',
      montoMaximo: '',
      estado: '',
      page: 0,
      size: 10
    });
    cargarDatos();
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR'
    }).format(amount || 0);
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('es-ES');
  };

  const getEstadoBadge = (estado) => {
    const badges = {
      PENDIENTE: 'bg-yellow-100 text-yellow-800',
      PAGADA: 'bg-green-100 text-green-800',
      CANCELADA: 'bg-red-100 text-red-800',
      VENCIDA: 'bg-red-100 text-red-800'
    };
    return badges[estado] || 'bg-gray-100 text-gray-800';
  };

  const marcarPagada = async (id) => {
    try {
      await facturasApi.marcarPagada(id);
      cargarDatos();
    } catch (err) {
      setError('Error al marcar factura como pagada');
    }
  };

  if (loading && !facturas.length) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-indigo-600"></div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h1 className="text-2xl font-bold text-gray-900">Vista de Facturación</h1>
        <button
          onClick={cargarDatos}
          className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition-colors"
        >
          Actualizar
        </button>
      </div>

      {error && (
        <div className="bg-red-50 border-l-4 border-red-400 p-4">
          <p className="text-red-700">{error}</p>
        </div>
      )}

      {/* Estadísticas */}
      {estadisticas && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
          <div className="bg-white rounded-lg shadow p-4">
            <div className="text-sm text-gray-500">Total Facturas</div>
            <div className="text-2xl font-bold text-gray-900">{estadisticas.totalFacturas}</div>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <div className="text-sm text-gray-500">Pendientes</div>
            <div className="text-2xl font-bold text-yellow-600">{estadisticas.facturasPendientes}</div>
            <div className="text-sm text-gray-400">{formatCurrency(estadisticas.totalPendiente)}</div>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <div className="text-sm text-gray-500">Ingresos del Mes</div>
            <div className="text-2xl font-bold text-green-600">{formatCurrency(estadisticas.ingresosDelMes)}</div>
          </div>
          <div className="bg-white rounded-lg shadow p-4">
            <div className="text-sm text-gray-500">Impuestos del Mes</div>
            <div className="text-2xl font-bold text-indigo-600">{formatCurrency(estadisticas.impuestosDelMes)}</div>
          </div>
        </div>
      )}

      {/* Resumen por Estado */}
      {resumenEstado.length > 0 && (
        <div className="bg-white rounded-lg shadow p-4">
          <h3 className="text-lg font-semibold mb-3">Resumen por Estado</h3>
          <div className="flex flex-wrap gap-4">
            {resumenEstado.map((item) => (
              <div key={item.estado} className="flex items-center space-x-2">
                <span className={`px-2 py-1 rounded-full text-xs font-medium ${getEstadoBadge(item.estado)}`}>
                  {item.estado}
                </span>
                <span className="text-gray-600">{item.cantidad} ({formatCurrency(item.total)})</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Filtros */}
      <div className="bg-white rounded-lg shadow p-4">
        <h3 className="text-lg font-semibold mb-3">Filtros</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 lg:grid-cols-6 gap-4">
          <div>
            <label className="block text-sm text-gray-600 mb-1">Fecha Inicio</label>
            <input
              type="date"
              value={filtros.fechaInicio}
              onChange={(e) => setFiltros({ ...filtros, fechaInicio: e.target.value })}
              className="w-full border rounded-md px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label className="block text-sm text-gray-600 mb-1">Fecha Fin</label>
            <input
              type="date"
              value={filtros.fechaFin}
              onChange={(e) => setFiltros({ ...filtros, fechaFin: e.target.value })}
              className="w-full border rounded-md px-3 py-2 text-sm"
            />
          </div>
          <div>
            <label className="block text-sm text-gray-600 mb-1">Monto Mínimo</label>
            <input
              type="number"
              value={filtros.montoMinimo}
              onChange={(e) => setFiltros({ ...filtros, montoMinimo: e.target.value })}
              className="w-full border rounded-md px-3 py-2 text-sm"
              placeholder="0.00"
            />
          </div>
          <div>
            <label className="block text-sm text-gray-600 mb-1">Monto Máximo</label>
            <input
              type="number"
              value={filtros.montoMaximo}
              onChange={(e) => setFiltros({ ...filtros, montoMaximo: e.target.value })}
              className="w-full border rounded-md px-3 py-2 text-sm"
              placeholder="999.99"
            />
          </div>
          <div>
            <label className="block text-sm text-gray-600 mb-1">Estado</label>
            <select
              value={filtros.estado}
              onChange={(e) => setFiltros({ ...filtros, estado: e.target.value })}
              className="w-full border rounded-md px-3 py-2 text-sm"
            >
              <option value="">Todos</option>
              <option value="PENDIENTE">Pendiente</option>
              <option value="PAGADA">Pagada</option>
              <option value="CANCELADA">Cancelada</option>
            </select>
          </div>
          <div className="flex items-end space-x-2">
            <button
              onClick={aplicarFiltros}
              className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 text-sm"
            >
              Filtrar
            </button>
            <button
              onClick={limpiarFiltros}
              className="px-4 py-2 bg-gray-200 text-gray-700 rounded-md hover:bg-gray-300 text-sm"
            >
              Limpiar
            </button>
          </div>
        </div>
      </div>

      {/* Tabla de Facturas */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Nº Factura
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Usuario
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Fecha Emisión
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Subtotal
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                IVA (%)
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Impuestos
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Total
              </th>
              <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                Estado
              </th>
              <th className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">
                Acciones
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {facturas.length === 0 ? (
              <tr>
                <td colSpan="9" className="px-6 py-4 text-center text-gray-500">
                  No hay facturas
                </td>
              </tr>
            ) : (
              facturas.map((factura) => (
                <tr key={factura.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    {factura.numeroFactura}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {factura.usuarioNombre || `Usuario #${factura.usuarioId}`}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {formatDate(factura.fechaEmision)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 text-right">
                    {formatCurrency(factura.subtotal)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 text-right">
                    {factura.porcentajeImpuestos}%
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-indigo-600 text-right">
                    {formatCurrency(factura.montoImpuestos)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-semibold text-gray-900 text-right">
                    {formatCurrency(factura.total)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-center">
                    <span className={`px-2 py-1 text-xs font-medium rounded-full ${getEstadoBadge(factura.estado)}`}>
                      {factura.estado}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-center">
                    {factura.estado === 'PENDIENTE' && (
                      <button
                        onClick={() => marcarPagada(factura.id)}
                        className="text-green-600 hover:text-green-900 text-sm font-medium"
                      >
                        Marcar Pagada
                      </button>
                    )}
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Paginación */}
      {totalPages > 1 && (
        <div className="flex justify-center space-x-2">
          <button
            onClick={() => setFiltros({ ...filtros, page: Math.max(0, filtros.page - 1) })}
            disabled={filtros.page === 0}
            className="px-3 py-1 border rounded disabled:opacity-50"
          >
            Anterior
          </button>
          <span className="px-3 py-1">
            Página {filtros.page + 1} de {totalPages}
          </span>
          <button
            onClick={() => setFiltros({ ...filtros, page: Math.min(totalPages - 1, filtros.page + 1) })}
            disabled={filtros.page >= totalPages - 1}
            className="px-3 py-1 border rounded disabled:opacity-50"
          >
            Siguiente
          </button>
        </div>
      )}
    </div>
  );
}
