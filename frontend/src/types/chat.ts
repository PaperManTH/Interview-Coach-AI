// 聊天与会话状态类型：单一事实来源
// - 消息结构支持 AI 流式增量（通过 appendAiChunk 更新 content）
// - 会话状态含 thinking / speaking，方便 UI 呈现与未来 websocket 对接

export type ChatRole = 'user' | 'ai';

export interface ChatMessage {
  id: string;
  role: ChatRole;
  content: string;
  createdAt: number;
  streaming?: boolean; // 流式输出中
}

// 会话状态：idle / listening（录音中）/ thinking（生成中）/ speaking（TTS 播放中）
export type SessionStatus = 'idle' | 'listening' | 'thinking' | 'speaking';

export interface InterviewSessionState {
  scene: string | null;           // hr / technical / pressure
  messages: ChatMessage[];
  status: SessionStatus;
  isMicActive: boolean;
  sessionId: string | null;
}
