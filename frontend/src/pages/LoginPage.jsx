import { useState, useEffect } from 'react';
import { User, Mail, Lock, CreditCard, ArrowRight, AlertCircle, LogIn, UserPlus } from 'lucide-react';
import { usuariosApi, planesApi, suscripcionesApi } from '../services/api';

function LoginPage({ onLogin }) {
  const [activeTab, setActiveTab] = useState('login');
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    nombre: '',
    apellidos: '',
    planId: ''
  });
  const [planes, setPlanes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    planesApi.getActivos().then(setPlanes).catch(console.error);
  }, []);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const usuario = await usuariosApi.login(formData.email, formData.password);
      
      // Obtener suscripción activa del usuario
      let plan = null;
      let suscripcionId = null;
      
      try {
        const suscripciones = await suscripcionesApi.getByUsuario(usuario.id);
        const suscActiva = suscripciones.find(s => s.estado === 'ACTIVA' || s.estado === 'MOROSA');
        if (suscActiva) {
          plan = await planesApi.getById(suscActiva.planId);
          suscripcionId = suscActiva.id;
        }
      } catch (err) {
        console.log('Usuario sin suscripción activa');
      }

      onLogin({
        usuario: { ...usuario, suscripcionId },
        plan
      });
    } catch (err) {
      if (err.response?.status === 401) {
        setError('Email o contraseña incorrectos');
      } else {
        setError(err.response?.data?.message || 'Error al iniciar sesión');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      // Crear usuario
      const nuevoUsuario = await usuariosApi.create({
        email: formData.email,
        password: formData.password,
        nombre: formData.nombre,
        apellidos: formData.apellidos
      });

      // Crear suscripción si seleccionó un plan
      let plan = null;
      let suscripcionId = null;
      
      if (formData.planId) {
        const suscripcion = await suscripcionesApi.create({
          usuarioId: nuevoUsuario.id,
          planId: parseInt(formData.planId)
        });
        plan = planes.find(p => p.id === parseInt(formData.planId));
        suscripcionId = suscripcion.id;
      }

      onLogin({
        usuario: { ...nuevoUsuario, suscripcionId },
        plan
      });
    } catch (err) {
      setError(err.response?.data?.message || 'Error al crear la cuenta. Intente nuevamente.');
    } finally {
      setLoading(false);
    }
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
      <main className="flex-grow flex items-center justify-center px-4 py-12">
        <div className="w-full max-w-md">
          {/* Título */}
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-gray-900">
              {activeTab === 'login' ? 'Iniciar Sesión' : 'Crear Cuenta'}
            </h1>
            <p className="text-gray-600 mt-2">
              {activeTab === 'login' ? 'Accede a tu cuenta' : 'Únete a nuestra plataforma'}
            </p>
          </div>

          {/* Card del formulario */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
            {/* Tabs */}
            <div className="flex border-b border-gray-200">
              <button
                onClick={() => { setActiveTab('login'); setError(''); }}
                className={`flex-1 flex items-center justify-center gap-2 py-4 text-sm font-medium transition-colors ${
                  activeTab === 'login'
                    ? 'text-[#0081C8] border-b-2 border-[#0081C8] bg-blue-50'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                <LogIn className="w-4 h-4" />
                Iniciar Sesión
              </button>
              <button
                onClick={() => { setActiveTab('register'); setError(''); }}
                className={`flex-1 flex items-center justify-center gap-2 py-4 text-sm font-medium transition-colors ${
                  activeTab === 'register'
                    ? 'text-[#0081C8] border-b-2 border-[#0081C8] bg-blue-50'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                <UserPlus className="w-4 h-4" />
                Registrarse
              </button>
            </div>

            <div className="p-8">
              {error && (
                <div className="flex items-center gap-2 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
                  <AlertCircle className="w-5 h-5 flex-shrink-0" />
                  <span className="text-sm">{error}</span>
                </div>
              )}

              {/* Formulario de Login */}
              {activeTab === 'login' && (
                <form onSubmit={handleLogin} className="space-y-5">
                  {/* Email */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Email</label>
                    <div className="relative">
                      <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                      <input
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        required
                        className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#0081C8] focus:border-[#0081C8] transition-colors"
                        placeholder="correo@ejemplo.com"
                      />
                    </div>
                  </div>

                  {/* Contraseña */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Contraseña</label>
                    <div className="relative">
                      <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                      <input
                        type="password"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        required
                        className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#0081C8] focus:border-[#0081C8] transition-colors"
                        placeholder="Tu contraseña"
                      />
                    </div>
                  </div>

                  {/* Info admin */}
                  <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 text-sm text-blue-700">
                    <strong>Admin:</strong> admin@saas.com / admin123
                  </div>

                  {/* Botón submit */}
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-[#0081C8] text-white py-3 rounded-lg font-medium hover:bg-[#0070af] focus:ring-2 focus:ring-[#0081C8] focus:ring-offset-2 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                  >
                    {loading ? (
                      <span>Iniciando sesión...</span>
                    ) : (
                      <>
                        <LogIn className="w-5 h-5" />
                        <span>Entrar</span>
                      </>
                    )}
                  </button>
                </form>
              )}

              {/* Formulario de Registro */}
              {activeTab === 'register' && (
                <form onSubmit={handleRegister} className="space-y-5">
                  {/* Nombre */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Nombre</label>
                    <div className="relative">
                      <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                      <input
                        type="text"
                        name="nombre"
                        value={formData.nombre}
                        onChange={handleChange}
                        required
                        className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#0081C8] focus:border-[#0081C8] transition-colors"
                        placeholder="Tu nombre"
                      />
                    </div>
                  </div>

                  {/* Apellidos */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Apellidos</label>
                    <div className="relative">
                      <User className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                      <input
                        type="text"
                        name="apellidos"
                        value={formData.apellidos}
                        onChange={handleChange}
                        required
                        className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#0081C8] focus:border-[#0081C8] transition-colors"
                        placeholder="Tus apellidos"
                      />
                    </div>
                  </div>

                  {/* Email */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Email</label>
                    <div className="relative">
                      <Mail className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                      <input
                        type="email"
                        name="email"
                        value={formData.email}
                        onChange={handleChange}
                        required
                        className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#0081C8] focus:border-[#0081C8] transition-colors"
                        placeholder="correo@ejemplo.com"
                      />
                    </div>
                  </div>

                  {/* Contraseña */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Contraseña</label>
                    <div className="relative">
                      <Lock className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                      <input
                        type="password"
                        name="password"
                        value={formData.password}
                        onChange={handleChange}
                        required
                        minLength={8}
                        className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#0081C8] focus:border-[#0081C8] transition-colors"
                        placeholder="Mínimo 8 caracteres"
                      />
                    </div>
                  </div>

                  {/* Plan */}
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1.5">Plan</label>
                    <div className="relative">
                      <CreditCard className="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                      <select
                        name="planId"
                        value={formData.planId}
                        onChange={handleChange}
                        required
                        className="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-[#0081C8] focus:border-[#0081C8] transition-colors appearance-none bg-white"
                      >
                        <option value="">Selecciona un plan</option>
                        {planes.map(plan => (
                          <option key={plan.id} value={plan.id}>
                            {plan.nombre} - €{plan.precioMensual}/mes
                          </option>
                        ))}
                      </select>
                    </div>
                  </div>

                  {/* Botón submit */}
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-[#0081C8] text-white py-3 rounded-lg font-medium hover:bg-[#0070af] focus:ring-2 focus:ring-[#0081C8] focus:ring-offset-2 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                  >
                    {loading ? (
                      <span>Creando cuenta...</span>
                    ) : (
                      <>
                        <span>Registrarse</span>
                        <ArrowRight className="w-5 h-5" />
                      </>
                    )}
                  </button>
                </form>
              )}
            </div>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-white border-t border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <p className="text-gray-500 text-sm text-center">© 2026 SaaS Platform</p>
        </div>
      </footer>
    </div>
  );
}

export default LoginPage;
