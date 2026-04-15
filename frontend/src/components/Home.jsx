import React, { useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import PostForm from './PostForm';
import Stream from './Stream';
import { Twitter } from 'lucide-react';

export default function Home() {
  const { isAuthenticated, loginWithRedirect } = useAuth0();
  const [latestPost, setLatestPost] = useState(null);

  return (
    <div>
      {/* Post form — only shown when authenticated */}
      {isAuthenticated ? (
        <PostForm onPostCreated={setLatestPost} />
      ) : (
        <div className="border-b border-twitter-border px-4 py-6 bg-twitter-dark text-center">
          <Twitter className="w-10 h-10 text-twitter-blue mx-auto mb-3" />
          <p className="text-twitter-white font-semibold mb-1">Join the conversation</p>
          <p className="text-twitter-muted text-sm mb-4">
            Log in to post your thoughts (max 140 characters).
          </p>
          <button
            onClick={() => loginWithRedirect()}
            className="btn-primary"
          >
            Log in with Auth0
          </button>
        </div>
      )}

      {/* Public stream */}
      <Stream newPost={latestPost} />
    </div>
  );
}
