import axios from 'axios';
import { API_BASE_URL, API_PREFIX } from '../auth0-config';

/**
 * Creates an authenticated Axios instance.
 * Pass the Auth0 access token to add the Authorization header.
 */
export function createApiClient(accessToken) {
  return axios.create({
    baseURL: API_BASE_URL,
    headers: accessToken
      ? { Authorization: `Bearer ${accessToken}` }
      : {},
  });
}

function apiPath(path) {
  return `${API_PREFIX}${path}`;
}

/** Fetch the public stream — no auth required */
export async function fetchStream(page = 0, size = 20) {
  const client = createApiClient(null);
  const { data } = await client.get(`${apiPath('/stream')}?page=${page}&size=${size}`);
  return data;
}

/** Create a new post — auth required */
export async function createPost(content, accessToken) {
  const client = createApiClient(accessToken);
  const { data } = await client.post(apiPath('/posts'), { content });
  return data;
}

/** Get current user profile — auth required */
export async function getMe(accessToken) {
  const client = createApiClient(accessToken);
  const { data } = await client.get(apiPath('/me'));
  return data;
}
