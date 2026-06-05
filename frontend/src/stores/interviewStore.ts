import { defineStore } from 'pinia';
import type { ChatMessage, SessionStatus } from '@/types/chat';
import { createWebSocketClient, destroyWebSocketClient, type WebSocketMessage } from '@/utils/websocket';
import { getAudioRecorder, destroyAudioRecorder } from '@/utils/audioRecorder';
import { getSpeechSynthesizer, destroySpeechSynthesizer, type SpeechCallbacks } from '@/utils/speechSynthesis';

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
      
      const wsClient = createWebSocketClient({
        userId: this.sessionId,
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
      this.messages.push({
        id: uid(),
        role: 'ai',
        content: message.content,
        createdAt: Date.now()
      });
    },

    async sendUserMessage(text: string) {
      const trimmed = text.trim();
      if (!trimmed || this.status !== 'idle') return;

      this.messages.push({
        id: uid(),
        role: 'user',
        content: trimmed,
        createdAt: Date.now()
      });

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
        
        await delay(1000);
        
        const mockTranscript = this.generateMockTranscript();
        this.audioTranscript = mockTranscript;
        await this.sendUserMessage(mockTranscript);
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
