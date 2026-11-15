import React, { createContext, useContext, useState, useEffect } from 'react';
import { workspaceApi } from '../api/workspaceApi';

const AuthContext = createContext(null);
const STORAGE_KEY = 'executive_workspace_user';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem(STORAGE_KEY);
    return saved ? JSON.parse(saved) : null;
  });

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const connected = params.get('connected') === 'true';
    const urlUserId = params.get('userId');
    const urlName = params.get('name');

    if (connected && urlUserId) {
      const googleUser = {
        userId: urlUserId,
        name: urlName || urlUserId.split('@')[0],
        googleConnected: true,
      };
      setUser(googleUser);
      localStorage.setItem(STORAGE_KEY, JSON.stringify(googleUser));
      window.history.replaceState({}, document.title, window.location.pathname);
      return;
    }

    // Verify backend token status if user is already logged in
    workspaceApi
      .fetchAuthStatus()
      .then((status) => {
        if (status?.googleConnected && status.userEmail) {
          const syncedUser = {
            userId: status.userEmail,
            name: status.userName || status.userEmail.split('@')[0],
            googleConnected: true,
          };
          setUser(syncedUser);
          localStorage.setItem(STORAGE_KEY, JSON.stringify(syncedUser));
        }
      })
      .catch(() => {});
  }, []);

  const loginWithWorkspaceId = (userIdInput, displayName) => {
    const cleanId = userIdInput.trim().toLowerCase();
    const newUser = {
      userId: cleanId,
      name: displayName?.trim() || cleanId.split('@')[0],
      googleConnected: false,
    };
    setUser(newUser);
    localStorage.setItem(STORAGE_KEY, JSON.stringify(newUser));
  };

  const logout = async () => {
    try {
      await workspaceApi.logoutSession();
    } catch (e) {
      console.error(e);
    }
    setUser(null);
    localStorage.removeItem(STORAGE_KEY);
  };

  return (
    <AuthContext.Provider value={{ user, loginWithWorkspaceId, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
}