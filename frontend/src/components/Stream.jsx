import React, { useEffect, useState, useCallback } from 'react';
import { fetchStream } from '../api/client';
import PostCard from './PostCard';
import LoadingSpinner from './LoadingSpinner';
import { RefreshCw } from 'lucide-react';

export default function Stream({ newPost }) {
  const [posts, setPosts]       = useState([]);
  const [loading, setLoading]   = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError]       = useState(null);
  const [page, setPage]         = useState(0);
  const [hasMore, setHasMore]   = useState(true);

  const loadPosts = useCallback(async (pageNum = 0, append = false) => {
    try {
      const data = await fetchStream(pageNum, 20);
      const items = data.content ?? data.items ?? data ?? [];
      const total = data.totalElements ?? data.total ?? items.length;

      setPosts(prev => append ? [...prev, ...items] : items);
      setHasMore(items.length === 20 && (append ? posts.length + items.length : items.length) < total);
      setError(null);
    } catch (err) {
      setError('Failed to load the stream. Please try again.');
      console.error(err);
    }
  }, [posts.length]);

  useEffect(() => {
    setLoading(true);
    loadPosts(0).finally(() => setLoading(false));
  }, []);

  // Prepend a newly created post optimistically
  useEffect(() => {
    if (newPost) {
      setPosts(prev => [newPost, ...prev]);
    }
  }, [newPost]);

  const handleRefresh = async () => {
    setRefreshing(true);
    setPage(0);
    await loadPosts(0);
    setRefreshing(false);
  };

  const handleLoadMore = async () => {
    const nextPage = page + 1;
    setPage(nextPage);
    await loadPosts(nextPage, true);
  };

  if (loading) {
    return (
      <div className="flex justify-center py-16">
        <LoadingSpinner />
      </div>
    );
  }

  return (
    <section aria-label="Public stream">
      {/* Stream header */}
      <div className="sticky top-14 z-40 bg-twitter-darker/90 backdrop-blur
                      border-b border-twitter-border px-4 py-3
                      flex items-center justify-between">
        <h2 className="font-bold text-lg text-twitter-white">Home</h2>
        <button
          onClick={handleRefresh}
          disabled={refreshing}
          className="p-2 rounded-full hover:bg-twitter-border/30 text-twitter-muted
                     hover:text-twitter-white transition-colors disabled:opacity-50"
          aria-label="Refresh stream"
        >
          <RefreshCw className={`w-4 h-4 ${refreshing ? 'animate-spin' : ''}`} />
        </button>
      </div>

      {/* Error state */}
      {error && (
        <div className="m-4 p-4 bg-red-900/30 border border-red-500/30 rounded-xl text-red-400 text-sm">
          {error}
        </div>
      )}

      {/* Empty state */}
      {!error && posts.length === 0 && (
        <div className="py-16 text-center text-twitter-muted">
          <p className="text-xl font-bold mb-2">No posts yet</p>
          <p className="text-sm">Be the first to post something!</p>
        </div>
      )}

      {/* Posts */}
      <div>
        {posts.map((post, idx) => (
          <PostCard key={post.id ?? post.postId ?? idx} post={post} />
        ))}
      </div>

      {/* Load more */}
      {hasMore && (
        <div className="p-4 flex justify-center">
          <button
            onClick={handleLoadMore}
            className="btn-outline text-sm"
          >
            Load more
          </button>
        </div>
      )}
    </section>
  );
}
