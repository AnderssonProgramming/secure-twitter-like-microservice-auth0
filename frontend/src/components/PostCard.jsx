import React from 'react';

function timeAgo(dateStr) {
  if (!dateStr) return '';
  const diff = Date.now() - new Date(dateStr).getTime();
  const s = Math.floor(diff / 1000);
  if (s < 60)  return `${s}s`;
  const m = Math.floor(s / 60);
  if (m < 60)  return `${m}m`;
  const h = Math.floor(m / 60);
  if (h < 24)  return `${h}h`;
  return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
}

export default function PostCard({ post }) {
  const name    = post.authorName   || post.userName  || 'Anonymous';
  const email   = post.authorEmail  || post.userEmail || '';
  const picture = post.authorPicture || post.userPicture;
  const date    = post.createdAt;

  const initials = name.split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();

  return (
    <article className="card flex gap-3 cursor-pointer">
      {/* Avatar */}
      <div className="flex-shrink-0">
        {picture ? (
          <img
            src={picture}
            alt={name}
            className="w-10 h-10 rounded-full border border-twitter-border"
          />
        ) : (
          <div className="w-10 h-10 rounded-full bg-gradient-to-br from-twitter-blue to-purple-500
                          flex items-center justify-center text-white text-sm font-bold">
            {initials || 'U'}
          </div>
        )}
      </div>

      {/* Content */}
      <div className="flex-1 min-w-0">
        <div className="flex items-baseline gap-2 flex-wrap">
          <span className="font-bold text-twitter-white truncate">{name}</span>
          {email && (
            <span className="text-twitter-muted text-sm truncate">@{email.split('@')[0]}</span>
          )}
          <span className="text-twitter-muted text-sm ml-auto flex-shrink-0">
            {timeAgo(date)}
          </span>
        </div>
        <p className="mt-1 text-twitter-white leading-relaxed whitespace-pre-wrap break-words">
          {post.content}
        </p>
      </div>
    </article>
  );
}
