import React from 'react';
import { Routes, Route } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import NavBar from './components/NavBar';
import Home from './components/Home';
import Profile from './components/Profile';
import LoadingSpinner from './components/LoadingSpinner';

export default function App() {
  const { isLoading } = useAuth0();

  if (isLoading) {
    return (
      <div className="min-h-screen bg-twitter-darker flex items-center justify-center">
        <LoadingSpinner />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-twitter-darker">
      <div className="max-w-2xl mx-auto">
        <NavBar />
        <main className="pt-14">
          <Routes>
            <Route path="/"        element={<Home />} />
            <Route path="/profile" element={<Profile />} />
          </Routes>
        </main>
      </div>
    </div>
  );
}
