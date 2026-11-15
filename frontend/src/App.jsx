import React from 'react';
import { QueryProvider } from './providers/QueryProvider';
import { AuthProvider, useAuth } from './context/AuthContext';
import WorkspaceDashboard from './pages/WorkspaceDashboard';
import LoginPage from './pages/LoginPage';

function AppRouter() {
  const { user } = useAuth();
  return user ? <WorkspaceDashboard /> : <LoginPage />;
}

export default function App() {
  return (
    <QueryProvider>
      <AuthProvider>
        <AppRouter />
      </AuthProvider>
    </QueryProvider>
  );
}