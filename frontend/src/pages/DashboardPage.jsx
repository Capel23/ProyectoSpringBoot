import { CreditCard, Calendar, CheckCircle, XCircle } from 'lucide-react';

function DashboardPage({ userData }) {
  const { usuario, plan } = userData;

  const formatearFecha = (fecha) => {
    if (!fecha) return 'N/A';
    return new Date(fecha).toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center h-16">
            <div className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-[#0081C8] rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-xl">S</span>
              </div>
              <span className="text-xl font-bold text-gray-900">SaaS Platform</span>
            </div>
          </div>
        </div>
      </header>

      {/* Contenido principal */}
      <main className="flex-grow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          {/* Mensaje de bienvenida */}
          <div className="text-center mb-10">
            <h1 className="text-4xl font-bold text-gray-900">
              ¡Bienvenido, {usuario.nombre || usuario.email}!
            </h1>
            <p className="text-gray-600 mt-2">Tu cuenta ha sido creada exitosamente</p>
          </div>

          {/* Card con información */}
          <div className="max-w-lg mx-auto">
            <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
              <div className="px-6 py-5 border-b border-gray-200">
                <h2 className="text-lg font-semibold text-gray-900">Detalles de tu Cuenta</h2>
              </div>

              <div className="divide-y divide-gray-100">
                {/* Plan */}
                <div className="px-6 py-4 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="p-2 bg-blue-50 rounded-lg">
                      <CreditCard className="w-5 h-5 text-[#0081C8]" />
                    </div>
                    <span className="text-gray-600">Plan</span>
                  </div>
                  <span className="font-semibold text-gray-900">
                    {plan?.nombre || 'Sin plan'}
                  </span>
                </div>

                {/* Precio */}
                <div className="px-6 py-4 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="p-2 bg-green-50 rounded-lg">
                      <span className="text-green-600 font-bold text-sm">€</span>
                    </div>
                    <span className="text-gray-600">Precio</span>
                  </div>
                  <span className="font-semibold text-green-600">
                    {plan?.precioMensual ? `€${plan.precioMensual}/mes` : 'N/A'}
                  </span>
                </div>

                {/* Estado */}
                <div className="px-6 py-4 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className={`p-2 rounded-lg ${usuario.activo ? 'bg-green-50' : 'bg-red-50'}`}>
                      {usuario.activo ? (
                        <CheckCircle className="w-5 h-5 text-green-600" />
                      ) : (
                        <XCircle className="w-5 h-5 text-red-600" />
                      )}
                    </div>
                    <span className="text-gray-600">Estado</span>
                  </div>
                  <span className={`px-3 py-1 rounded-full text-sm font-medium ${
                    usuario.activo 
                      ? 'bg-green-100 text-green-700' 
                      : 'bg-red-100 text-red-700'
                  }`}>
                    {usuario.activo ? 'Activa' : 'Inactiva'}
                  </span>
                </div>

                {/* Fecha de creación */}
                <div className="px-6 py-4 flex items-center justify-between">
                  <div className="flex items-center gap-3">
                    <div className="p-2 bg-gray-100 rounded-lg">
                      <Calendar className="w-5 h-5 text-gray-600" />
                    </div>
                    <span className="text-gray-600">Fecha de Creación</span>
                  </div>
                  <span className="font-medium text-gray-900">
                    {formatearFecha(usuario.fechaCreacion)}
                  </span>
                </div>
              </div>

              {/* Footer del card */}
              {plan?.descripcion && (
                <div className="px-6 py-4 bg-gray-50 border-t border-gray-200">
                  <p className="text-sm text-gray-500 text-center">{plan.descripcion}</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200 mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <p className="text-gray-500 text-sm text-center">© 2026 SaaS Platform</p>
        </div>
      </footer>
    </div>
  );
}

export default DashboardPage;
