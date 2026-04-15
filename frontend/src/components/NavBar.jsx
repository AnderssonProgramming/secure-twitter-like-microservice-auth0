import React from 'react';
import { Link, useLocation } from 'react-router-dom';
import { useAuth0 } from '@auth0/auth0-react';
import { Twitter, Home, User } from 'lucide-react';

export default function NavBar() {
  const { isAuthenticated, user, loginWithRedirect, logout } = useAuth0();
  const location = useLocation();

  const isActive = (path) => location.pathname === path;

  return (
    <header className="fixed top-0 left-0 right-0 z-50 bg-twitter-darker/90 backdrop-blur border-b border-twitter-border">
      <div className="max-w-2xl mx-auto px-4 h-14 flex items-center justify-between">

        {/* Logo */}
        <Link to="/" className="flex items-center gap-2 text-twitter-blue hover:text-blue-400 transition-colors">
          <Twitter className="w-7 h-7 fill-current" />
          <span className="font-bold text-lg text-twitter-white hidden sm:block">TwitterLite</span>
        </Link>

        {/* Nav links */}
        <nav className="flex items-center gap-1">
          <Link
            to="/"
            className={`flex items-center gap-2 px-3 py-2 rounded-full text-sm font-medium transition-colors
              ${isActive('/') ? 'text-twitter-blue bg-twitter-blue/10' : 'text-twitter-muted hover:text-twitter-white hover:bg-twitter-border/30'}`}
          >
            <Home className="w-5 h-5" />
            <span className="hidden sm:block">Home</span>
          </Link>

          {isAuthenticated && (
            <Link
              to="/profile"
              className={`flex items-center gap-2 px-3 py-2 rounded-full text-sm font-medium transition-colors
                ${isActive('/profile') ? 'text-twitter-blue bg-twitter-blue/10' : 'text-twitter-muted hover:text-twitter-white hover:bg-twitter-border/30'}`}
            >
              <User className="w-5 h-5" />
              <span className="hidden sm:block">Profile</span>
            </Link>
          )}
        </nav>

        {/* Auth buttons */}
        <div className="flex items-center gap-2">
          {isAuthenticated ? (
            <div className="flex items-center gap-3">
              {user?.picture && (
                <img
                  src={user.picture}
                  alt={user.name}
                  className="w-8 h-8 rounded-full border border-twitter-border"
                />
              )}
              <button
                onClick={() => logout({ logoutParams: { returnTo: window.location.origin } })}
                className="btn-outline text-sm py-1.5 px-4"
              >
                Log out
              </button>
            </div>
          ) : (
            <button
              onClick={() => loginWithRedirect()}
              className="btn-primary text-sm py-1.5"
            >
              Log in
            </button>
          )}
        </div>
      </div>
    </header>
  );
}
