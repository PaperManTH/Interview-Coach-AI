/**
 * 对话历史 API 服务
 * 
 * 提供面试会话和消息的查询接口
 */

// ===== 配置 =====
const API_BASE_URL = '/api';

function getAuthHeaders(): Record<string, string> {
  const token = localStorage.getItem('auth_token');
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
  };
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  return headers;
}

function getApiBaseUrl(): string {
  return API_BASE_URL;
}

// ===== 类型定义 =====

export interface InterviewSession {
  id: number;
  sessionId: string;
  userId: string;
  status: string;
  interviewType: string;
  startedAt: string;
  endedAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface InterviewMessage {
  id: number;
  sessionId: string;
  messageId: string;
  sender: string;
  type: string;
  content: string;
  timestampMs: number;
  metadata: string | null;
  createdAt: string;
}

export interface SessionListResponse {
  success: boolean;
  data: InterviewSession[];
  total: number;
  page: number;
  size: number;
}

export interface MessageListResponse {
  success: boolean;
  data: InterviewMessage[];
  count: number;
}

export interface SessionDetailResponse {
  success: boolean;
  session: InterviewSession;
}

// ===== 工具函数 =====

async function safeJsonParse<T>(response: Response, fallback: T): Promise<T> {
  const contentType = response.headers.get('content-type') || '';
  if (!contentType.includes('application/json') && !contentType.includes('json')) {
    // 响应不是 JSON，可能是后端错误页面或未找到
    console.warn(`[API] 响应不是 JSON (content-type=${contentType})`);
    return fallback;
  }
  try {
    return await response.json();
  } catch (e) {
    console.error('[API] JSON 解析失败:', e);
    return fallback;
  }
}

// ===== API 函数 =====

/**
 * 获取用户的面试会话列表
 */
export async function getSessionList(
  userId: string,
  page: number = 1,
  size: number = 10
): Promise<SessionListResponse> {
  const url = `${getApiBaseUrl()}/conversation/sessions/user/${userId}?page=${page}&size=${size}`;
  const response = await fetch(url, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    throw new Error(`获取会话列表失败: ${response.status} ${response.statusText}`);
  }

  const data = await safeJsonParse<SessionListResponse>(response, {
    success: false,
    data: [],
    total: 0,
    page: page,
    size: size,
  });

  return data;
}

/**
 * 获取会话详情
 */
export async function getSessionDetail(sessionId: string): Promise<SessionDetailResponse> {
  const url = `${getApiBaseUrl()}/conversation/sessions/${sessionId}`;
  const response = await fetch(url, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    throw new Error(`获取会话详情失败: ${response.status} ${response.statusText}`);
  }

  return safeJsonParse<SessionDetailResponse>(response, {
    success: false,
    session: {} as InterviewSession,
  });
}

/**
 * 获取会话的所有消息
 */
export async function getSessionMessages(sessionId: string): Promise<MessageListResponse> {
  const url = `${getApiBaseUrl()}/conversation/messages/session/${sessionId}`;
  const response = await fetch(url, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    throw new Error(`获取消息列表失败: ${response.status} ${response.statusText}`);
  }

  return safeJsonParse<MessageListResponse>(response, {
    success: false,
    data: [],
    count: 0,
  });
}

/**
 * 删除会话
 */
export async function deleteSession(sessionId: string): Promise<{ success: boolean; message: string }> {
  const url = `${getApiBaseUrl()}/conversation/sessions/${sessionId}`;
  const response = await fetch(url, {
    method: 'DELETE',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    throw new Error(`删除会话失败: ${response.status} ${response.statusText}`);
  }

  return safeJsonParse(response, {
    success: false,
    message: '操作失败',
  });
}

/**
 * 结束会话
 */
export async function endSession(sessionId: string): Promise<{ success: boolean; message: string }> {
  const url = `${getApiBaseUrl()}/conversation/sessions/${sessionId}/end`;
  const response = await fetch(url, {
    method: 'POST',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    throw new Error(`结束会话失败: ${response.status} ${response.statusText}`);
  }

  return safeJsonParse(response, {
    success: false,
    message: '操作失败',
  });
}

/**
 * 获取用户的会话数量统计
 */
export async function getSessionCount(userId: string): Promise<{ success: boolean; count: number }> {
  const url = `${getApiBaseUrl()}/conversation/stats/user/${userId}/count`;
  const response = await fetch(url, {
    method: 'GET',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    throw new Error(`获取统计失败: ${response.status} ${response.statusText}`);
  }

  return safeJsonParse(response, {
    success: false,
    count: 0,
  });
}