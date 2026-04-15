import React, { useEffect, useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { getMe } from '../api/client';
import LoadingSpinner from './LoadingSpinner';
import { Calendar, Mail, Link as LinkIcon } from 'lucide-react';

function formatDate(isoStr) {
  if (!isoStr) return '—';
  return new Date(isoStr).toLocaleDateString('en-US', {
    year: 'numeric', month: 'long', day: 'numeric'
  });
}

export default function Profile() {
  const { isAuthenticated, getAccessTokenSilently, loginWithRedirect, user: auth0User } = useAuth0();
  const [profile, setProfile]   = useState(null);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState(null);

  useEffect(() => {
    if (!isAuthenticated) {
      setLoading(false);
      return;
    }

    (async () => {
      try {
        const token = await getAccessTokenSilently();
        const data  = await getMe(token);
        setProfile(data);
      } catch (err) {
        setError('Failed to load profile. Please try again.');
        console.error(err);
      } finally {
        setLoading(false);
      }
    })();
  }, [isAuthenticated, getAccessTokenSilently]);

  if (!isAuthenticated) {
    return (
      <div className="p-8 text-center text-twitter-muted">
        <p className="text-xl font-bold text-twitter-white mb-2">Not logged in</p>
        <p className="mb-4">Log in to view your profile.</p>
        <button onClick={() => loginWithRedirect()} className="btn-primary">
          Log in
        </button>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="flex justify-center py-16">
        <LoadingSpinner />
      </div>
    );
  }

  if (error) {
    return (
      <div className="m-4 p-4 bg-red-900/30 border border-red-500/30 rounded-xl text-red-400">
        {error}
      </div>
    );
  }

  const name    = profile?.name    || auth0User?.name    || '—';
  const email   = profile?.email   || auth0User?.email   || '—';
  const picture = profile?.picture || auth0User?.picture;
  const sub     = profile?.auth0Sub || auth0User?.sub    || '—';

  return (
    <div>
      {/* Cover gradient */}
      <div className="h-32 bg-gradient-to-r from-twitter-blue via-purple-500 to-pink-500" />

      {/* Profile header */}
      <div className="px-4 pb-4 border-b border-twitter-border bg-twitter-dark">
        <div className="flex justify-between items-end -mt-12 mb-3">
          {picture ? (
            <img
              src={picture}
              alt={name}
              className="w-24 h-24 rounded-full border-4 border-twitter-dark"
            />
          ) : (
            <div className="w-24 h-24 rounded-full border-4 border-twitter-dark
                            bg-gradient-to-br from-twitter-blue to-purple-500
                            flex items-center justify-center text-white text-3xl font-bold">
              {name[0]?.toUpperCase() ?? 'U'}
            </div>
          )}
        </div>

        <h1 className="text-xl font-bold text-twitter-white">{name}</h1>
        <p className="text-twitter-muted text-sm">@{email.split('@')[0]}</p>

        {/* Meta */}
        <div className="mt-3 flex flex-wrap gap-x-4 gap-y-1 text-twitter-muted text-sm">
          <span className="flex items-center gap-1">
            <Mail className="w-4 h-4" />
            {email}
          </span>
          <span className="flex items-center gap-1">
            <Calendar className="w-4 h-4" />
            Joined {formatDate(profile?.createdAt)}
          </span>
        </div>

        {/* Stats */}
        <div className="mt-4 flex gap-6 text-sm">
          <div>
            <span className="font-bold text-twitter-white">{profile?.postCount ?? 0}</span>
            <span className="text-twitter-muted ml-1">Posts</span>
          </div>
        </div>
      </div>

      {/* Auth0 debug info */}
      <div className="m-4 p-4 bg-twitter-dark rounded-xl border border-twitter-border text-sm">
        <p className="text-twitter-muted mb-1 text-xs uppercase tracking-wide font-semibold">Auth0 Subject</p>
        <p className="text-twitter-white font-mono text-xs break-all">{sub}</p>
      </div>
    </div>
  );
}
