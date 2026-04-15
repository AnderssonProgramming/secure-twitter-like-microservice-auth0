import React from 'react';
import ReactDOM from 'react-dom/client';
import { Auth0Provider } from '@auth0/auth0-react';
import { BrowserRouter } from 'react-router-dom';
import App from './App';
import { auth0Config } from './auth0-config';
import './index.css';

function isMissingOrPlaceholder(value) {
  return !value || value.startsWith('YOUR_') || value.includes('YOUR_');
}

const missingAuth0Config =
  isMissingOrPlaceholder(auth0Config.domain) ||
  isMissingOrPlaceholder(auth0Config.clientId) ||
  isMissingOrPlaceholder(auth0Config.audience);

const isLocalhost = ['localhost', '127.0.0.1'].includes(window.location.hostname);
const insecureOrigin = window.location.protocol !== 'https:' && !isLocalhost;

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    {missingAuth0Config ? (
      <div className="min-h-screen bg-twitter-darker text-white flex items-center justify-center p-6">
        <div className="max-w-xl rounded-xl border border-yellow-500/40 bg-yellow-500/10 p-6">
          <h1 className="text-xl font-semibold mb-2">Missing Auth0 frontend configuration</h1>
          <p className="text-sm text-gray-200">
            Set VITE_AUTH0_DOMAIN, VITE_AUTH0_CLIENT_ID, VITE_AUTH0_AUDIENCE, and VITE_API_BASE_URL before running npm run build.
          </p>
          <p className="text-sm text-gray-300 mt-3">
            Then rebuild and upload frontend/dist to S3 again.
          </p>
        </div>
      </div>
    ) : insecureOrigin ? (
      <div className="min-h-screen bg-twitter-darker text-white flex items-center justify-center p-6">
        <div className="max-w-xl rounded-xl border border-red-500/40 bg-red-500/10 p-6">
          <h1 className="text-xl font-semibold mb-2">Auth0 requires HTTPS in production</h1>
          <p className="text-sm text-gray-200">
            This URL is using HTTP, so Auth0 login cannot start. The S3 website endpoint is HTTP-only.
          </p>
          <p className="text-sm text-gray-300 mt-3">
            Use localhost for development, or deploy the same S3 bucket behind CloudFront (HTTPS) and use that CloudFront URL in Auth0 callback/logout/web origin settings.
          </p>
        </div>
      </div>
    ) : (
      <Auth0Provider
        domain={auth0Config.domain}
        clientId={auth0Config.clientId}
        authorizationParams={{
          redirect_uri: window.location.origin,
          audience: auth0Config.audience,
          scope: 'openid profile email read:posts write:posts read:profile',
        }}
        cacheLocation="localstorage"
        useRefreshTokens
      >
        <BrowserRouter>
          <App />
        </BrowserRouter>
      </Auth0Provider>
    )}
  </React.StrictMode>
);
