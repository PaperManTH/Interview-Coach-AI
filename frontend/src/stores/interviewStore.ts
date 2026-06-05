import { defineStore } from 'pinia';
import type { ChatMessage, SessionStatus } from '@/types/chat';

// 为每个场景生成一句定制开场，便于未来扩展到后端 prompt 选择
const OPENING_BY_SCENE: Record<string, string> = {
  hr: "Hi, thanks for joining today. Let's start with a quick self-introduction.",
  technical: "Let's dive into the technical part. Tell me about the most complex system you've built recently.",
  pressure: "Let's begin. I'll push you a bit — stay sharp and answer concisely."
};

const FALLBACK_OPENING = "Hi, let's begin the interview. Please start with a brief introduction.";

function uid(): string {
  return Math.random().toString(36).slice(2) + Date.now().toString(36);
}

// Mock 回复生成：按字符分片模拟流式输出
const MOCK_REPLIES: string[] = [
  "Got it. Could you elaborate on the specific challenge you faced and how you resolved it?",
  "Interesting. Let me follow up — what trade-offs did you consider and why did you pick that approach?",
  "Thanks. Now let's go deeper. Walk me through the end-to-end flow.",
  "Good. Can you give me a concrete example with numbers and outcomes?",
  "Let's switch gears. Tell me about a time when you disagreed with your interviewer / teammate."
];

export const useInterviewStore = defineStore('interview', {
  state: () => ({
    scene: null as string | null,
    messages: [] as ChatMessage[],
    status: 'idle' as SessionStatus,
    isMicActive: false,
    sessionId: null as string | null,
    mockReplyCursor: 0
  }),

  actions: {
    // 初始化会话：清空并注入场景对应的 AI 开场白
    startSession(scene: string) {
      this.scene = scene;
      this.sessionId = uid();
      this.messages = [];
      this.mockReplyCursor = 0;
      this.status = 'idle';
      const opening = OPENING_BY_SCENE[scene] ?? FALLBACK_OPENING;
      this.messages.push({
        id: uid(),
        role: 'ai',
        content: opening,
        createdAt: Date.now()
      });
    },

    // 用户发送文本消息：推入列表并触发 mock 回复流
    async sendUserMessage(text: string) {
      const trimmed = text.trim();
      if (!trimmed || this.status !== 'idle') return;

      this.messages.push({
        id: uid(),
        role: 'user',
        content: trimmed,
        createdAt: Date.now()
      });

      // —— 模拟 AI 回复流（思考 + 分片输出） ——
      // 未来替换为 WebSocket 的 onmessage 回调，逐块调用 appendAiChunk。
      this.status = 'thinking';
      await delay(600);

      const reply = MOCK_REPLIES[this.mockReplyCursor % MOCK_REPLIES.length];
      this.mockReplyCursor += 1;

      const aiMessageId = uid();
      this.messages.push({
        id: aiMessageId,
        role: 'ai',
        content: '',
        createdAt: Date.now(),
        streaming: true
      });
      this.status = 'speaking';

      const chunks = reply.match(/.{1,6}/g) || [reply];
      for (const chunk of chunks) {
        this.appendAiChunk(aiMessageId, chunk);
        await delay(60);
      }

      const target = this.messages.find((m) => m.id === aiMessageId);
      if (target) target.streaming = false;
      this.status = 'idle';
    },

    // 流式输出：未来 WebSocket handler 可直接调用
    appendAiChunk(messageId: string, chunk: string) {
      const msg = this.messages.find((m) => m.id === messageId);
      if (!msg) return;
      msg.content += chunk;
    },

    // 麦克风开关（仅切换状态，后续接入 MediaRecorder）
    toggleMic() {
      this.isMicActive = !this.isMicActive;
      this.status = this.isMicActive ? 'listening' : 'idle';
    },

    // 回到选择页：清空状态
    resetSession() {
      this.scene = null;
      this.messages = [];
      this.status = 'idle';
      this.isMicActive = false;
      this.sessionId = null;
    }
  }
});

function delay(ms: number): Promise<void> {
  return new Promise((r) => setTimeout(r, ms));
}
