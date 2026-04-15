/**
 * Auth0 SPA configuration.
 *
 * Set these values in a .env file:
 *   VITE_AUTH0_DOMAIN=YOUR_DOMAIN.auth0.com
 *   VITE_AUTH0_CLIENT_ID=YOUR_SPA_CLIENT_ID
 *   VITE_AUTH0_AUDIENCE=YOUR_API_AUDIENCE
 *   VITE_API_BASE_URL=http://localhost:8080   (monolith) or AWS API Gateway URL
 */
export const auth0Config = {
  domain:    import.meta.env.VITE_AUTH0_DOMAIN   || 'YOUR_DOMAIN.auth0.com',
  clientId:  import.meta.env.VITE_AUTH0_CLIENT_ID || 'YOUR_SPA_CLIENT_ID',
  audience:  import.meta.env.VITE_AUTH0_AUDIENCE  || 'YOUR_API_AUDIENCE',
};

export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
