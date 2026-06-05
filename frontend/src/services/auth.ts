/**
 * 认证 API 服务
 * 适配：JWT 无状态令牌 + State 防 CSRF
 */

interface LoginResponse {
  userId: string;
  username: string;
  avatarUrl: string;
  token: string;
  config?: any;
}

interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

const CSRF_STATE_KEY = 'csrf_state';

/**
 * 从后端获取防 CSRF 的 state。
 */
export async function generateState(): Promise<string> {
  const resp = await fetch('/api/auth/state', { method: 'POST' });
  const json: ApiResult<{ state: string }> = await resp.json();
  if (json.code !== 0) throw new Error(json.message || '获取 state 失败');
  return json.data.state;
}

/**
 * 构建 GitHub OAuth 跳转 URL（附带 state）。
 */
export async function buildGithubOAuthUrl(): Promise<string> {
  const state = await generateState();
  sessionStorage.setItem(CSRF_STATE_KEY, state);

  const clientId = import.meta.env.VITE_GITHUB_CLIENT_ID || '';
  const redirectUri = encodeURIComponent(window.location.origin + '/login/callback');
  const stateParam = encodeURIComponent(state);

  if (!clientId) {
    // Mock 模式
    return `/login/callback?code=mock_github_code&state=${stateParam}`;
  }
  return `https://github.com/login/oauth/authorize`
    + `?client_id=${clientId}&redirect_uri=${redirectUri}&scope=user:email&state=${stateParam}`;
}

/**
 * 用 code + state 向后端换取 JWT。
 */
export async function loginWithGithub(code: string, state: string): Promise<LoginResponse> {
  const resp = await fetch(
    `/api/auth/login/github?code=${encodeURIComponent(code)}&state=${encodeURIComponent(state)}`,
    { method: 'POST' }
  );
  const json: ApiResult<LoginResponse> = await resp.json();
  if (json.code !== 0) throw new Error(json.message || '登录失败');
  return json.data;
}

/**
 * 获取当前用户信息（JWT 验证，无数据库 token 查询）。
 */
export async function fetchUserInfo(token: string): Promise<LoginResponse> {
  const resp = await fetch(`/api/auth/me`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  const json: ApiResult<LoginResponse> = await resp.json();
  if (json.code !== 0) throw new Error(json.message || '获取用户信息失败');
  return json.data;
}

/**
 * 登出。
 */
export async function logoutApi(token: string): Promise<void> {
  await fetch(`/api/auth/logout`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` },
  });
}

/** 从 URL query 中取 state 并验证是否匹配 sessionStorage */
export function verifyCallbackState(): boolean {
  const params = new URLSearchParams(window.location.search);
  const returnedState = params.get('state');
  const savedState = sessionStorage.getItem(CSRF_STATE_KEY);
  sessionStorage.removeItem(CSRF_STATE_KEY);
  if (!returnedState || !savedState || returnedState !== savedState) {
    console.error('[Auth] State 校验失败');
    return false;
  }
  return true;
}
