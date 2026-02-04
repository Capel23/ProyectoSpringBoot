import { Link } from 'react-router-dom';
import { Users, CreditCard, ArrowRight } from 'lucide-react';

const Home = () => {
  const features = [
    {
      icon: Users,
      title: 'Gestión de Usuarios',
      description: 'Administra los usuarios del sistema. Crea, edita y elimina usuarios.',
      link: '/usuarios',
      color: 'bg-blue-500',
    },
    {
      icon: CreditCard,
      title: 'Planes de Suscripción',
      description: 'Configura los planes Basic, Premium y Enterprise.',
      link: '/planes',
      color: 'bg-green-500',
    },
  ];

  return (
    <div className="space-y-8">
      {/* Hero */}
      <div className="text-center py-8">
        <h1 className="text-4xl font-bold text-gray-900 mb-3">Plataforma SaaS</h1>
        <p className="text-lg text-gray-600 max-w-xl mx-auto">
          Sistema de gestión de suscripciones y facturación.
        </p>
      </div>

      {/* Cards */}
      <div className="grid md:grid-cols-2 gap-6">
        {features.map((feature) => {
          const Icon = feature.icon;
          return (
            <Link
              key={feature.title}
              to={feature.link}
              className="group bg-white rounded-xl shadow-sm border border-gray-200 p-6 
                         hover:shadow-md hover:border-primary-300 transition-all"
            >
              <div className="flex items-start space-x-4">
                <div className={`${feature.color} p-3 rounded-lg`}>
                  <Icon className="w-6 h-6 text-white" />
                </div>
                <div className="flex-1">
                  <h3 className="text-lg font-semibold text-gray-900 group-hover:text-primary-600 flex items-center">
                    {feature.title}
                    <ArrowRight className="w-4 h-4 ml-2 opacity-0 group-hover:opacity-100 transition-opacity" />
                  </h3>
                  <p className="text-gray-600 mt-1">{feature.description}</p>
                </div>
              </div>
            </Link>
          );
        })}
      </div>
    </div>
  );
};

export default Home;
