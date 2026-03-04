/** @type {import('tailwindcss').Config} */
import daisyui from 'daisyui';
export default {
  content: ['./index.html', './src/**/*.{vue,js,ts,jsx,tsx}'],
  theme: {
    extend: {},
  },
  plugins: [daisyui],
  daisyui: {
    themes: [
      'light',
      'dark',
      {
        xidian: {
          primary: '#0ea5e9',
          'primary-content': '#f3f4f6',
          secondary: '#7dd3fc',
          'secondary-content': '#1f2937',
          accent: '#bae6fd',
          'accent-content': '#1f2937',
          neutral: '#bfdbfe',
          'neutral-content': '#0d1116',
          'base-100': '#ffffff',
          'base-200': '#f3f4f6',
          'base-300': '#e5e7eb',
          'base-content': '#111827',
          info: '#006bde',
          'info-content': '#ffffff',
          success: '#388500',
          'success-content': '#ffffff',
          warning: '#ffa900',
          'warning-content': '#ffffff',
          error: '#f87171',
          'error-content': '#ffffff',
        },
      },
    ],
  },
};
