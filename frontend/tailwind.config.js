/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        // Colores Juegos Olímpicos
        olympic: {
          blue: '#0081C8',
          yellow: '#FCB131',
          black: '#1D1D1B',
          green: '#00A651',
          red: '#EE334E',
        },
        primary: {
          50: '#e6f4fb',
          100: '#cce9f7',
          200: '#99d3ef',
          300: '#66bde7',
          400: '#33a7df',
          500: '#0081C8',
          600: '#0070af',
          700: '#005f96',
          800: '#004e7d',
          900: '#003d64',
        }
      }
    },
  },
  plugins: [],
}
