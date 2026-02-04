import { BrowserRouter as Router, Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Home from './pages/Home'
import UsuariosPage from './pages/UsuariosPage'
import PlanesPage from './pages/PlanesPage'

function App() {
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
}

export default App
