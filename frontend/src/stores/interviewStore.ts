import { defineStore } from 'pinia';
import type { ChatMessage, SessionStatus } from '@/types/chat';
import { createWebSocketClient, getWebSocketClient, destroyWebSocketClient, type WebSocketMessage } from '@/utils/websocket';
import { getAudioRecorder, destroyAudioRecorder } from '@/utils/audioRecorder';
import { getSpeechSynthesizer, destroySpeechSynthesizer, type SpeechCallbacks } from '@/utils/speechSynthesis';
import { convertToPcm, pcmToBase64 } from '@/utils/audioConverter';
import { useAuthStore } from '@/stores/authStore';

const OPENING_BY_SCENE: Record<string, string> = {
  hr: "Hi, thanks for joining today. Let's start with a quick self-introduction.",
  technical: "Let's dive into the technical part. Tell me about the most complex system you've built recently.",
  pressure: "Let's begin. I'll push you a bit — stay sharp and answer concisely."
};

const FALLBACK_OPENING = "Hi, let's begin the interview. Please start with a brief introduction.";

function uid(): string {
  return Math.random().toString(36).slice(2) + Date.now().toString(36);
}

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
    isSpeaking: false,
    sessionId: null as string | null,
    mockReplyCursor: 0,
    isWsConnected: false,
    recordingDuration: 0,
    audioTranscript: ''
  }),

  actions: {
    startSession(scene: string) {
      this.scene = scene;
      this.sessionId = uid();
      this.messages = [];
      this.mockReplyCursor = 0;
      this.status = 'idle';
      this.isWsConnected = false;
      const opening = OPENING_BY_SCENE[scene] ?? FALLBACK_OPENING;
      this.messages.push({
        id: uid(),
        role: 'ai',
        content: opening,
        createdAt: Date.now()
      });
      this.connectWebSocket();
    },

    connectWebSocket() {
      if (!this.sessionId) return;

      const authStore = useAuthStore();
      const realUserId = authStore.userId || this.sessionId;

      const wsClient = createWebSocketClient({
        userId: realUserId,
        reconnectDelay: 5000,
        maxReconnectAttempts: 5
      });

      wsClient.onConnection((connected) => {
        this.isWsConnected = connected;
        if (connected) {
          console.log('[WS] 连接成功');
        }
      });

      wsClient.onMessage((message: WebSocketMessage) => {
        this.handleWebSocketMessage(message);
      });

      wsClient.connect();
    },

    handleWebSocketMessage(message: WebSocketMessage) {
      switch (message.type) {
        case 'CHAT':
          this.handleChatMessage(message);
          break;
        case 'TEXT':
          this.handleTextMessage(message);
          break;
        case 'AUDIO':
          this.handleAudioMessage(message);
          break;
        case 'VOICE_TEXT':
          this.handleVoiceTextMessage(message);
          break;
        case 'PONG':
          console.log('[WS] 收到心跳响应');
          break;
        case 'ERROR':
          console.error('[WS] 错误:', message.content);
          break;
        default:
          console.log('[WS] 未知消息类型:', message.type);
      }
    },

    handleVoiceTextMessage(message: WebSocketMessage) {
      // 后端返回的语音识别文字，作为用户语音消息加入聊天
      const transcript = message.content || '语音识别失败';
      this.messages.push({
        id: uid() + '-' + Date.now(),
        role: 'user',
        content: transcript,
        createdAt: message.timestamp || Date.now(),
        isVoice: true
      });
      this.status = 'thinking'; // 接下来等待 AI 回复
      console.log('[ASR] 识别到文字:', transcript);
    },

    handleChatMessage(message: WebSocketMessage) {
      const aiMessageId = uid();
      this.messages.push({
        id: aiMessageId,
        role: 'ai',
        content: '',
        createdAt: Date.now(),
        streaming: true
      });
      this.status = 'speaking';

      const chunks = message.content.match(/.{1,6}/g) || [message.content];
      let index = 0;
      const interval = setInterval(() => {
        if (index < chunks.length) {
          this.appendAiChunk(aiMessageId, chunks[index]);
          index++;
        } else {
          clearInterval(interval);
          const target = this.messages.find((m) => m.id === aiMessageId);
          if (target) target.streaming = false;
          this.status = 'idle';
        }
      }, 60);
    },

    handleTextMessage(message: WebSocketMessage) {
      // 根据 sender 区分用户消息、AI消息和系统消息
      console.log('[WS] 收到文本消息:', message);
      let role: 'user' | 'ai' | 'system';
      if (message.sender === 'user') {
        role = 'user';
      } else if (message.sender === 'system') {
        role = 'system';
      } else {
        role = 'ai'; // assistant 或其他都当作 AI
      }
      console.log('[WS] 消息角色:', role, 'sender:', message.sender);
      this.messages.push({
        id: message.id || uid(),
        role: role,
        content: message.content,
        createdAt: message.timestamp || Date.now()
      });
      // 只有 AI 消息到达后才设为 idle
      if (role === 'ai') {
        this.status = 'idle';
      }
    },

    handleAudioMessage(message: WebSocketMessage) {
      if (!message.content) return;
      // 将 Base64 音频数据转换为 Audio 并播放
      const audio = new Audio('data:audio/wav;base64,' + message.content);
      audio.play().then(() => {
        console.log('[TTS] 音频播放完成');
      }).catch((e) => {
        console.error('[TTS] 音频播放失败:', e);
      });
    },

    async sendUserMessage(text: string) {
      const trimmed = text.trim();
      if (!trimmed || this.status !== 'idle') return;

      this.status = 'thinking';

      // 通过 WebSocket 发送到后端（LLM + TTS），等待连接就绪
      console.log('[Chat] 当前状态: status=', this.status, 'isWsConnected=', this.isWsConnected);
      await waitForWs(this, 5000);
      const wsClient = getWebSocketClient();
      console.log('[Chat] wsClient存在=', !!wsClient, '已连接=', wsClient?.isConnectedState());
      if (wsClient && wsClient.isConnectedState()) {
        wsClient.send({
          id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
          type: 'TEXT',
          sender: this.sessionId || 'anonymous',
          receiver: '',
          content: trimmed,
          timestamp: Date.now()
        });
        console.log('[Chat] 已发送文本到后端');
      } else {
        // WebSocket 未连接，降级为 Mock
        console.warn('[Chat] WebSocket 未连接，使用 Mock');
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
      }
    },

    appendAiChunk(messageId: string, chunk: string) {
      const msg = this.messages.find((m) => m.id === messageId);
      if (!msg) return;
      msg.content += chunk;
    },

    async startRecording() {
      if (this.isMicActive || this.status !== 'idle') return;
      
      this.isMicActive = true;
      this.status = 'listening';
      this.recordingDuration = 0;
      this.audioTranscript = '';

      const recorder = getAudioRecorder({
        onData: (data) => {
          console.log('[Recorder] 收到音频数据:', data.size, 'bytes');
        },
        onError: (error) => {
          console.error('[Recorder] 录音错误:', error);
          this.stopRecording();
        }
      });

      try {
        await recorder.start((duration) => {
          this.recordingDuration = duration;
        });
        console.log('[Recorder] 开始录音');
      } catch (error) {
        console.error('[Recorder] 启动失败:', error);
        this.isMicActive = false;
        this.status = 'idle';
      }
    },

    async stopRecording() {
      if (!this.isMicActive) return;

      const recorder = getAudioRecorder();
      const audioBlob = recorder.stop();
      this.isMicActive = false;

      if (audioBlob) {
        console.log('[Recorder] 录音完成，时长:', this.recordingDuration, '秒');
        this.status = 'processing';

        // 将 webm/opus Blob 转换为 PCM Int16 16kHz mono Base64
        try {
          const pcmBuffer = await convertToPcm(audioBlob);
          const base64 = pcmToBase64(pcmBuffer);
          console.log('[Recorder] PCM 转换完成，大小:', base64.length, 'chars');
          await waitForWs(this, 5000);
          const wsClient = getWebSocketClient();
          if (wsClient && wsClient.isConnectedState()) {
            wsClient.send({
              id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
              type: 'VOICE_END',
              sender: this.sessionId || 'anonymous',
              receiver: '',
              content: base64,
              timestamp: Date.now()
            });
            console.log('[Recorder] 已发送 PCM 音频到后端');
          } else {
            console.warn('[Recorder] WebSocket 未连接，使用模拟文本');
            const mockTranscript = this.generateMockTranscript();
            this.audioTranscript = mockTranscript;
            await this.sendUserMessage(mockTranscript);
          }
        } catch (e) {
          console.error('[Recorder] PCM 转换失败，降级为模拟文本:', e);
          const mockTranscript = this.generateMockTranscript();
          this.audioTranscript = mockTranscript;
          await this.sendUserMessage(mockTranscript);
        }
      } else {
        this.status = 'idle';
      }
    },

    toggleMic() {
      if (this.isMicActive) {
        this.stopRecording();
      } else {
        this.startRecording();
      }
    },

    generateMockTranscript(): string {
      const transcripts = [
        "I have three years of experience in software development, mainly using Java and Spring Boot.",
        "I led a team of five developers to build a microservices architecture for our e-commerce platform.",
        "The project involved integrating multiple third-party APIs and implementing real-time data synchronization.",
        "We faced challenges with performance optimization, which we solved by implementing caching strategies.",
        "I'm proficient in both front-end and back-end development, with experience in React and Vue.js."
      ];
      return transcripts[this.mockReplyCursor % transcripts.length];
    },

    async playAiMessage(messageId: string) {
      const message = this.messages.find((m) => m.id === messageId);
      if (!message || message.role !== 'ai') return;

      if (this.isSpeaking) {
        const synthesizer = getSpeechSynthesizer();
        synthesizer.stop();
      }

      this.isSpeaking = true;

      const callbacks: SpeechCallbacks = {
        onEnd: () => {
          this.isSpeaking = false;
        },
        onError: (error) => {
          console.error('[TTS] 语音合成错误:', error);
          this.isSpeaking = false;
        }
      };

      const synthesizer = getSpeechSynthesizer();
      synthesizer.setCallbacks(callbacks);
      synthesizer.speak(message.content);
    },

    stopSpeaking() {
      const synthesizer = getSpeechSynthesizer();
      synthesizer.stop();
      this.isSpeaking = false;
    },

    resetSession() {
      this.scene = null;
      this.messages = [];
      this.status = 'idle';
      this.isMicActive = false;
      this.isSpeaking = false;
      this.sessionId = null;
      this.isWsConnected = false;
      this.recordingDuration = 0;
      this.audioTranscript = '';
      
      destroyWebSocketClient();
      destroyAudioRecorder();
      destroySpeechSynthesizer();
    }
  }
});

function delay(ms: number): Promise<void> {
  return new Promise((r) => setTimeout(r, ms));
}

/**
 * 等待 WebSocket 连接就绪，超时后不阻塞。
 */
function waitForWs(store: { isWsConnected: boolean }, timeoutMs: number): Promise<void> {
  const start = Date.now();
  return new Promise((resolve) => {
    const check = () => {
      if (store.isWsConnected) return resolve();
      if (Date.now() - start >= timeoutMs) return resolve();
      setTimeout(check, 100);
    };
    check();
  });
}
