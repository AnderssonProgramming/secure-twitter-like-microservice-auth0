import React, { useEffect, useState } from 'react';
import { Routes, Route } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import NavBar from './components/NavBar';
import Home from './components/Home';
import Profile from './components/Profile';
import LoadingSpinner from './components/LoadingSpinner';

export default function App() {
  const { isLoading } = useAuth0();
  const [authError, setAuthError] = useState(null);

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const error = params.get('error');
    const errorDescription = params.get('error_description');

    if (error || errorDescription) {
      setAuthError(errorDescription || error);
      window.history.replaceState({}, document.title, window.location.pathname);
    }
  }, []);

  if (isLoading) {
    return (
      <div className="min-h-screen bg-twitter-darker flex flex-col items-center justify-center gap-4 text-twitter-muted">
        <LoadingSpinner size={10} />
        <p className="text-sm">Loading authentication...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-twitter-darker">
      <div className="max-w-2xl mx-auto">
        <NavBar />
        <main className="pt-14">
          {authError && (
            <div className="mx-4 mt-4 rounded-xl border border-red-500/40 bg-red-500/10 p-3 text-sm text-red-200">
              Auth0 login error: {authError}
            </div>
          )}
          <Routes>
            <Route path="/"        element={<Home />} />
            <Route path="/profile" element={<Profile />} />
          </Routes>
        </main>
      </div>
    </div>
  );
}
