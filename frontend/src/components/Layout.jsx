import { Link, useLocation } from 'react-router-dom';
import { Users, CreditCard, Home, Menu, X } from 'lucide-react';
import { useState } from 'react';

const Layout = ({ children }) => {
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  const nav = [
    { name: 'Inicio', href: '/', icon: Home },
    { name: 'Usuarios', href: '/usuarios', icon: Users },
    { name: 'Planes', href: '/planes', icon: CreditCard },
  ];

  const isActive = (path) => location.pathname === path;

  return (
    <div className="min-h-screen flex flex-col">
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <Link to="/" className="flex items-center space-x-3">
              <div className="w-10 h-10 bg-gradient-to-br from-primary-500 to-primary-700 rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-xl">S</span>
              </div>
              <span className="text-xl font-bold text-gray-900">SaaS Platform</span>
            </Link>

            <nav className="hidden md:flex space-x-1">
              {nav.map(({ name, href, icon: Icon }) => (
                <Link
                  key={name}
                  to={href}
                  className={`flex items-center px-4 py-2 rounded-lg text-sm font-medium transition-colors
                    ${isActive(href) ? 'bg-primary-50 text-primary-700' : 'text-gray-600 hover:bg-gray-100'}`}
                >
                  <Icon className="w-4 h-4 mr-2" />
                  {name}
                </Link>
              ))}
            </nav>

            <button onClick={() => setMenuOpen(!menuOpen)} className="md:hidden p-2 rounded-lg text-gray-600 hover:bg-gray-100">
              {menuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>

        {menuOpen && (
          <div className="md:hidden border-t border-gray-200 py-2 animate-fade-in">
            <div className="px-4 space-y-1">
              {nav.map(({ name, href, icon: Icon }) => (
                <Link
                  key={name}
                  to={href}
                  onClick={() => setMenuOpen(false)}
                  className={`flex items-center px-4 py-3 rounded-lg text-sm font-medium
                    ${isActive(href) ? 'bg-primary-50 text-primary-700' : 'text-gray-600 hover:bg-gray-100'}`}
                >
                  <Icon className="w-5 h-5 mr-3" />
                  {name}
                </Link>
              ))}
            </div>
          </div>
        )}
      </header>

      <main className="flex-grow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">{children}</div>
      </main>

      <footer className="bg-white border-t border-gray-200 mt-auto">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <p className="text-gray-500 text-sm text-center">© 2026 SaaS Platform</p>
        </div>
      </footer>
    </div>
  );
};

export default Layout;
