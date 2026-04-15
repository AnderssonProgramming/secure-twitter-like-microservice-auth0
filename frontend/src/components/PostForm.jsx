import React, { useState } from 'react';
import { useAuth0 } from '@auth0/auth0-react';
import { createPost } from '../api/client';

const MAX_CHARS = 140;

export default function PostForm({ onPostCreated }) {
  const { getAccessTokenSilently, user } = useAuth0();
  const [content, setContent]   = useState('');
  const [loading, setLoading]   = useState(false);
  const [error, setError]       = useState(null);

  const remaining = MAX_CHARS - content.length;
  const isOverLimit = remaining < 0;
  const isEmpty     = content.trim().length === 0;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (isEmpty || isOverLimit) return;

    setLoading(true);
    setError(null);

    try {
      const token = await getAccessTokenSilently();
      const newPost = await createPost(content.trim(), token);
      setContent('');
      onPostCreated?.(newPost);
    } catch (err) {
      setError('Failed to post. Please try again.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
      handleSubmit(e);
    }
  };

  return (
    <form
      onSubmit={handleSubmit}
      className="border-b border-twitter-border px-4 pt-3 pb-2 bg-twitter-dark"
    >
      <div className="flex gap-3">
        {/* Avatar */}
        <div className="flex-shrink-0">
          {user?.picture ? (
            <img src={user.picture} alt={user.name} className="w-10 h-10 rounded-full" />
          ) : (
            <div className="w-10 h-10 rounded-full bg-twitter-blue flex items-center justify-center text-white font-bold">
              {user?.name?.[0]?.toUpperCase() ?? 'U'}
            </div>
          )}
        </div>

        {/* Text area */}
        <div className="flex-1 min-w-0">
          <textarea
            className="input-field min-h-[80px] pt-2"
            placeholder="What's happening?"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            onKeyDown={handleKeyDown}
            maxLength={MAX_CHARS + 10}
            disabled={loading}
          />

          {error && (
            <p className="text-red-400 text-sm mt-1">{error}</p>
          )}

          <div className="flex items-center justify-between mt-2 pt-2 border-t border-twitter-border/50">
            {/* Character counter */}
            <div className="flex items-center gap-2">
              {content.length > 0 && (
                <>
                  {/* Circular progress */}
                  <svg className="w-8 h-8 -rotate-90" viewBox="0 0 32 32">
                    <circle
                      cx="16" cy="16" r="12"
                      fill="none"
                      stroke="#2F3336"
                      strokeWidth="3"
                    />
                    <circle
                      cx="16" cy="16" r="12"
                      fill="none"
                      stroke={isOverLimit ? '#F4212E' : remaining <= 20 ? '#FFD400' : '#1DA1F2'}
                      strokeWidth="3"
                      strokeDasharray={`${Math.max(0, (content.length / MAX_CHARS) * 75.4)} 75.4`}
                      strokeLinecap="round"
                    />
                  </svg>
                  <span className={`text-sm font-medium ${isOverLimit ? 'text-red-400' : remaining <= 20 ? 'text-yellow-400' : 'text-twitter-muted'}`}>
                    {remaining}
                  </span>
                </>
              )}
            </div>

            {/* Post button */}
            <button
              type="submit"
              className="btn-primary"
              disabled={loading || isEmpty || isOverLimit}
            >
              {loading ? (
                <span className="flex items-center gap-2">
                  <span className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  Posting…
                </span>
              ) : (
                'Post'
              )}
            </button>
          </div>
        </div>
      </div>
    </form>
  );
}
