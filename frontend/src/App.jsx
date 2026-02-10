import { useState } from 'react'
// COMENTADO - Código anterior del sistema completo
// import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
// import Layout from './components/Layout'
// import Home from './pages/Home'
// import UsuariosPage from './pages/UsuariosPage'
// import PlanesPage from './pages/PlanesPage'

// NUEVO - Vista simplificada de Login y Dashboard
import LoginPage from './pages/LoginPage'
import DashboardPage from './pages/DashboardPage'

function App() {
  const [userData, setUserData] = useState(null);

  const handleLogin = (data) => {
    setUserData(data);
  };

  const handleLogout = () => {
    setUserData(null);
  };

  // Si no hay usuario logueado, mostrar Login
  // Si hay usuario logueado, mostrar Dashboard
  return userData ? (
    <DashboardPage userData={userData} onLogout={handleLogout} />
  ) : (
    <LoginPage onLogin={handleLogin} />
  );

  /* COMENTADO - Código anterior del sistema completo con rutas
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/usuarios" element={<UsuariosPage />} />
          <Route path="/planes" element={<PlanesPage />} />
        </Routes>
      </Layout>
    </Router>
  )
  */
}

export default App
