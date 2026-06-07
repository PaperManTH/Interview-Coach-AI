import { defineStore } from 'pinia';
import type { ChatMessage, SessionStatus } from '@/types/chat';
import type { InterviewStage, InterviewFocus, InterviewStageMeta } from '@/types/scene';
import { getStageMeta, INTERVIEW_STAGES } from '@/types/scene';
import { createWebSocketClient, getWebSocketClient, destroyWebSocketClient, type WebSocketMessage } from '@/utils/websocket';
import { getAudioRecorder, destroyAudioRecorder } from '@/utils/audioRecorder';
import { getSpeechSynthesizer, destroySpeechSynthesizer, type SpeechCallbacks } from '@/utils/speechSynthesis';
import { convertToPcm, pcmToBase64 } from '@/utils/audioConverter';
import { useAuthStore } from '@/stores/authStore';
import {
  getSessionDetail,
  getSessionMessages,
  pauseSession,
  type InterviewMessage,
} from '@/services/conversationApi';

// ===== 阶段配置 =====
const STAGE_OPENINGS: Record<InterviewStage, string> = {
  warmup: "Hi! Thanks for joining today. Let's start with a warm-up question — could you tell me about yourself?",
  technical: "Great! Now let's move into the technical part. I'll ask you some questions based on your background.",
  pressure: "Excellent answers. Let me push you a bit further — I want to test how you think under pressure."
};

/** 每阶段多少轮对话后自动切换下一阶段 */
const ROUNDS_PER_STAGE = 3;

const FALLBACK_OPENING = "Hi, let's begin the interview. Please start with a brief introduction.";

function uid(): string {
  return Math.random().toString(36).slice(2) + Date.now().toString(36);
}

const MOCK_REPLIES: string[] = [
  "Got it. Could you elaborate on the specific challenge you faced and how you resolved it?",
  "Interesting. Let me follow up — what trade-offs did you consider and why did you pick that approach?",
  "Thanks. Now let's go deeper. Walk me through the end-to-end flow.",
  "Good. Can you give me a concrete example with numbers and outcomes?",
  "Let's switch gears. Tell me about a time when you disagreed with your teammate."
];

export const useInterviewStore = defineStore('interview', {
  state: () => ({
    // ===== 兼容旧模式 =====
    scene: null as string | null,
    // ===== 动态模式 =====
    isDynamicMode: false,
    currentStage: null as InterviewStage | null,
    focus: 'general' as InterviewFocus,
    stageRound: 0,          // 当前阶段已进行轮数
    totalRounds: 0,          // 总轮数
    completedStages: [] as InterviewStage[],

    // ===== 通用状态 =====
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

  getters: {
    /** 当前阶段元数据 */
    stageMeta(): InterviewStageMeta | undefined {
      return this.currentStage ? getStageMeta(this.currentStage) : undefined;
    },
    /** 当前后端需要的 system prompt 场景标识（兼容旧接口） */
    backendSceneKey(): string {
      if (this.isDynamicMode) {
        // 映射动态阶段到旧场景 key，供后端 prompt 使用
        const map: Record<InterviewStage, string> = {
          warmup: 'hr',
          technical: 'technical',
          pressure: 'pressure'
        };
        return map[this.currentStage!] || 'hr';
      }
      return this.scene || 'hr';
    },
    /** 阶段进度 (0-100) */
    stageProgress(): number {
      if (!this.isDynamicMode) return 0;
      const stageIndex = INTERVIEW_STAGES.findIndex(s => s.key === this.currentStage);
      const base = (stageIndex / INTERVIEW_STAGES.length) * 100;
      const withinStage = (this.stageRound / ROUNDS_PER_STAGE) * (100 / INTERVIEW_STAGES.length);
      return Math.min(Math.round(base + withinStage), 99);
    }
  },

  actions: {
    // ===== 启动会话 =====

    /** 启动传统模式会话（保留兼容） */
    startSession(scene: string) {
      this.isDynamicMode = false;
      this.scene = scene;
      this.currentStage = null;
      this._initCore();
      const opening = STAGE_OPENINGS['warmup']; // 传统模式也用 warmup 开场
      if (scene === 'technical') {
        this.messages.push({
          id: uid(), role: 'ai', content: STAGE_OPENINGS['technical'],
          createdAt: Date.now()
        });
      } else if (scene === 'pressure') {
        this.messages.push({
          id: uid(), role: 'ai', content: STAGE_OPENINGS['pressure'],
          createdAt: Date.now()
        });
      } else {
        this.messages.push({
          id: uid(), role: 'ai', content: opening,
          createdAt: Date.now()
        });
      }
      // 延迟连接 WebSocket，直到用户发送第一条消息
    },

    /** 启动动态模式会话 */
    startDynamicSession(focus: InterviewFocus = 'general') {
      this.isDynamicMode = true;
      this.focus = focus;
      this.currentStage = 'warmup';
      this.stageRound = 0;
      this.totalRounds = 0;
      this.completedStages = [];
      this._initCore();

      const opening = STAGE_OPENINGS['warmup'];
      this.messages.push({
        id: uid(),
        role: 'ai',
        content: opening,
        createdAt: Date.now(),
        stageInfo: { stage: 'warmup', label: 'Warm-up', labelZh: '热身环节' }
      });
      this.connectWebSocket();
    },

    /** 暂停当前会话（用户点击退出/返回时调用） */
    async pauseCurrentSession(): Promise<void> {
      const sid = this.sessionId;
      console.log('[Interview] 准备暂停会话, sessionId=', sid);
      if (!sid) {
        console.log('[Interview] 无 sessionId，跳过暂停');
        return;
      }
      try {
        const result = await pauseSession(sid);
        console.log('[Interview] 会话暂停结果:', sid, result);
        if (!result.success) {
          console.warn('[Interview] 暂停会话失败，sessionId可能不正确:', sid);
        }
      } catch (e) {
        console.error('[Interview] 暂停会话异常:', e);
      }
    },

    /** 恢复一个已有的会话（在"继续"按钮被点击时调用） */
    async resumeSession(sessionId: string) {
      this._initCore();
      this.isDynamicMode = true;
      this.focus = 'general';
      this.currentStage = 'warmup';
      this.stageRound = 0;
      this.totalRounds = 0;
      this.completedStages = [];
      // 注意：sessionId 必须在 _initCore 之后赋值，因为 _initCore 会重置它
      this.sessionId = sessionId;

      try {
        const [sessionResp, msgResp] = await Promise.all([
          getSessionDetail(sessionId),
          getSessionMessages(sessionId)
        ]);
        if (sessionResp?.session) {
          const type = String(sessionResp.session.interviewType || 'dynamic');
          if (type !== 'dynamic') {
            this.isDynamicMode = false;
            this.scene = type;
          }
          // 优先从会话数据中直接读取阶段信息（最可靠）
          if (sessionResp.session.currentStage) {
            this.currentStage = sessionResp.session.currentStage as InterviewStage;
            console.log('[Store] 从会话数据恢复阶段:', this.currentStage);
          }
          if (sessionResp.session.stageRound != null) {
            this.stageRound = Number(sessionResp.session.stageRound);
            console.log('[Store] 从会话数据恢复阶段轮次:', this.stageRound);
          }
          if (sessionResp.session.totalRounds != null) {
            this.totalRounds = Number(sessionResp.session.totalRounds);
            console.log('[Store] 从会话数据恢复总轮次:', this.totalRounds);
          }
        }
        if (msgResp?.success && Array.isArray(msgResp.data)) {
          let savedStage: string | undefined;
          
          for (let i = 0; i < msgResp.data.length; i++) {
            const m = msgResp.data[i] as InterviewMessage;
            let content = m.content || '';
            let chinese: string | undefined;
            
            // 尝试解析 metadata，提取英文和中文翻译
            try {
              if (m.metadata) {
                const meta = JSON.parse(String(m.metadata));
                if (meta && typeof meta === 'object') {
                  content = (meta.english as string) || (meta.chinese as string) || content;
                  chinese = meta.chinese as string;
                  // 从 AI 消息的 metadata 中读取阶段信息作为备份
                  if ((m.sender === 'assistant' || m.sender === 'ai') && meta.stage) {
                    savedStage = meta.stage as string;
                  }
                }
              }
            } catch {
              // ignore
            }
            
            const role = m.sender === 'user' ? 'user' : (m.sender === 'system' ? 'system' : 'ai');
            
            this.messages.push({
              id: m.messageId || `${m.id}`,
              role,
              content,
              createdAt: new Date(m.timestampMs || Date.now()).getTime(),
              chinese: role === 'ai' ? chinese : undefined,
              isSystem: role === 'system'
            });
          }
          
          // 如果会话数据中没有阶段信息，尝试从消息 metadata 中恢复
          if (!sessionResp?.session?.currentStage && savedStage) {
            this.currentStage = savedStage as InterviewStage;
            console.log('[Store] 从消息 metadata 恢复阶段:', savedStage);
          }
          
          // 如果会话数据中也没有轮次信息，根据消息数量估算
          if (sessionResp?.session?.stageRound == null) {
            const aiMsgCount = this.messages.filter(m => m.role === 'ai').length;
            this.totalRounds = Math.max(0, aiMsgCount - 1);
            // 简单估算当前阶段内的轮次
            if (this.currentStage && this.isDynamicMode) {
              const stageIndex = INTERVIEW_STAGES.findIndex(s => s.key === this.currentStage);
              this.stageRound = Math.max(0, this.totalRounds - stageIndex * 3);
            }
          }
          console.log('[Store] 恢复会话完成: stage=', this.currentStage, 'round=', this.stageRound, 'total=', this.totalRounds);
        }
      } catch (e) {
        console.warn('[Store] 恢复会话消息失败:', e);
      }

      // 如果拉取不到历史消息，则至少显示一条开场白
      if (this.messages.length === 0) {
        this.messages.push({
          id: uid(),
          role: 'ai',
          content: STAGE_OPENINGS['warmup'],
          createdAt: Date.now(),
          stageInfo: { stage: 'warmup', label: 'Warm-up', labelZh: '热身环节' }
        });
      }
      this.connectWebSocket();
    },

    /** 核心初始化 */
    _initCore() {
      this.sessionId = uid();
      this.messages = [];
      this.mockReplyCursor = 0;
      this.status = 'idle';
      this.isWsConnected = false;
    },

    // ===== WebSocket =====

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
        if (connected) console.log('[WS] 连接成功');
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
        case 'STAGE_TRANSITION':
          this.handleStageTransition(message);
          break;
        case 'SESSION_INIT':
          this.sessionId = message.sessionId
            || (message.metadata && typeof message.metadata === 'object' ? (message.metadata as any).dbSessionId : undefined)
            || this.sessionId;
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
      const transcript = message.content || '语音识别失败';
      this.messages.push({
        id: uid() + '-' + Date.now(),
        role: 'user',
        content: transcript,
        createdAt: message.timestamp || Date.now(),
        isVoice: true
      });
      this.status = 'thinking';
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
          // 动态模式：AI 回复完成后检查是否需要切换阶段
          if (this.isDynamicMode) {
            this.checkStageTransition();
          }
        }
      }, 60);
    },

    handleTextMessage(message: WebSocketMessage) {
      console.log('[WS] 收到文本消息:', message);
      let role: 'user' | 'ai' | 'system';
      if (message.sender === 'user') role = 'user';
      else if (message.sender === 'system') role = 'system';
      else role = 'ai';

      // 从 metadata 中提取中文翻译和阶段信息
      let content = message.content;
      let chinese: string | undefined;
      let msgStage: string | undefined;
      
      if (message.metadata && typeof message.metadata === 'object') {
        const meta = message.metadata as Record<string, unknown>;
        if (meta.chinese && typeof meta.chinese === 'string') {
          chinese = meta.chinese;
        }
        if (meta.english && typeof meta.english === 'string') {
          content = meta.english;
        }
        if (meta.stage && typeof meta.stage === 'string') {
          msgStage = meta.stage;
        }
      }

      this.messages.push({
        id: message.id || uid(),
        role: role,
        content: content,
        createdAt: message.timestamp || Date.now(),
        chinese: role === 'ai' ? chinese : undefined,
        stageInfo: msgStage ? { stage: msgStage as InterviewStage } : undefined
      });

      if (role === 'ai') {
        this.status = 'idle';
        // 动态模式：根据 AI 消息中的阶段信息更新当前阶段
        if (this.isDynamicMode && msgStage) {
          this.currentStage = msgStage as InterviewStage;
          console.log('[Store] 从AI消息更新阶段:', msgStage);
        }
        // 动态模式：检查阶段切换
        if (this.isDynamicMode) {
          this.checkStageTransition();
        }
      }
    },

    handleAudioMessage(message: WebSocketMessage) {
      if (!message.content) return;
      const audio = new Audio('data:audio/wav;base64,' + message.content);
      audio.play().then(() => {
        console.log('[TTS] 音频播放完成');
      }).catch((e) => {
        console.error('[TTS] 音频播放失败:', e);
      });
    },

    /** 处理后端发来的阶段切换指令 */
    handleStageTransition(message: WebSocketMessage) {
      try {
        const data = JSON.parse(message.content);
        const nextStage = data.stage as InterviewStage;
        if (nextStage && INTERVIEW_STAGES.find(s => s.key === nextStage)) {
          this.advanceToStage(nextStage);

          // 插入阶段过渡消息
          this.messages.push({
            id: uid(),
            role: 'ai',
            content: data.transitionMessage || `--- Moving to ${nextStage} phase ---`,
            createdAt: Date.now(),
            isSystem: true
          });

          // 发送新阶段开场白
          const meta = getStageMeta(nextStage);
          this.messages.push({
            id: uid(),
            role: 'ai',
            content: STAGE_OPENINGS[nextStage],
            createdAt: Date.now(),
            stageInfo: { stage: nextStage, label: meta?.label ?? '', labelZh: meta?.labelZh ?? '' }
          });
        }
      } catch (e) {
        console.warn('[WS] 解析阶段切换消息失败:', e);
      }
    },

    // ===== 消息发送 =====

    async sendUserMessage(text: string) {
      const trimmed = text.trim();
      if (!trimmed || this.status !== 'idle') return;

      this.status = 'thinking';

      // 记录轮次（动态模式下）
      if (this.isDynamicMode) {
        this.stageRound++;
        this.totalRounds++;
      }

      // 如果 WebSocket 未连接，则先连接
      const wsClient = getWebSocketClient();
      if (!wsClient || !wsClient.isConnectedState()) {
        this.connectWebSocket();
      }

      await waitForWs(this, 5000);
      const wsClientAfterWait = getWebSocketClient();
      if (wsClientAfterWait && wsClientAfterWait.isConnectedState()) {
        // 构建消息体，包含阶段信息
        const payload: WebSocketMessage = {
          id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
          type: 'TEXT',
          sender: this.sessionId || 'anonymous',
          receiver: '',
          content: trimmed,
          timestamp: Date.now()
        };

        // 动态模式附加上下文到 sessionId 和 metadata（双保险）
        if (this.isDynamicMode) {
          const stageInfo = {
            mode: 'dynamic',
            stage: this.currentStage,
            focus: this.focus,
            stageRound: this.stageRound,
            totalRounds: this.totalRounds
          };
          payload.sessionId = JSON.stringify(stageInfo);
          payload.metadata = stageInfo;
        }

        wsClientAfterWait.send(payload);
        console.log('[Chat] 已发送文本到后端, stage=', this.currentStage, 'round=', this.stageRound);
      } else {
        // Mock 降级
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
        if (this.isDynamicMode) this.checkStageTransition();
      }
    },

    appendAiChunk(messageId: string, chunk: string) {
      const msg = this.messages.find((m) => m.id === messageId);
      if (!msg) return;
      msg.content += chunk;
    },

    // ===== 阶段管理 =====

    /** 前端主动推进到下一阶段（基于轮数计数） */
    checkStageTransition() {
      if (!this.isDynamicMode || !this.currentStage) return;

      // 达到该阶段轮数上限时自动切换
      if (this.stageRound >= ROUNDS_PER_STAGE) {
        const currentIndex = INTERVIEW_STAGES.findIndex(s => s.key === this.currentStage);
        const nextIndex = currentIndex + 1;

        if (nextIndex < INTERVIEW_STAGES.length) {
          const nextStage = INTERVIEW_STAGES[nextIndex].key;
          this.advanceToStage(nextStage);

          // 插入过渡系统消息
          const meta = getStageMeta(nextStage);
          this.messages.push({
            id: uid(),
            role: 'system',
            content: `--- ${meta?.labelZh ?? nextStage} ---`,
            createdAt: Date.now(),
            isSystem: true
          });

          // 新阶段开场白
          this.messages.push({
            id: uid(),
            role: 'ai',
            content: STAGE_OPENINGS[nextStage],
            createdAt: Date.now(),
            stageInfo: { stage: nextStage, label: meta?.label ?? '', labelZh: meta?.labelZh ?? '' }
          });
        }
        // 最后一个阶段结束后不再切换
      }
    },

    /** 切换到指定阶段 */
    advanceToStage(stage: InterviewStage) {
      if (this.currentStage) {
        this.completedStages.push(this.currentStage);
      }
      this.currentStage = stage;
      this.stageRound = 0;
      console.log(`[Stage] 切换到阶段: ${stage}`);
    },

    // ===== 录音相关 =====

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

        try {
          const pcmBuffer = await convertToPcm(audioBlob);
          const base64 = pcmToBase64(pcmBuffer);
          console.log('[Recorder] PCM 转换完成，大小:', base64.length, 'chars');

          await waitForWs(this, 5000);
          const wsClient = getWebSocketClient();
          if (wsClient && wsClient.isConnectedState()) {
            const payload: WebSocketMessage = {
              id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
              type: 'VOICE_END',
              sender: this.sessionId || 'anonymous',
              receiver: '',
              content: base64,
              timestamp: Date.now()
            };
            if (this.isDynamicMode) {
              payload.sessionId = JSON.stringify({
                mode: 'dynamic',
                stage: this.currentStage,
                focus: this.focus,
                stageRound: this.stageRound + 1,
                totalRounds: this.totalRounds + 1
              });
            }
            wsClient.send(payload);
            console.log('[Recorder] 已发送 PCM 音频到后端');

            // 录音也计轮
            if (this.isDynamicMode) {
              this.stageRound++;
              this.totalRounds++;
            }
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

    // ===== TTS =====

    async playAiMessage(messageId: string) {
      const message = this.messages.find((m) => m.id === messageId);
      if (!message || message.role !== 'ai') return;

      if (this.isSpeaking) {
        const synthesizer = getSpeechSynthesizer();
        synthesizer.stop();
      }

      this.isSpeaking = true;

      const callbacks: SpeechCallbacks = {
        onEnd: () => { this.isSpeaking = false; },
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
      this.isDynamicMode = false;
      this.currentStage = null;
      this.focus = 'general';
      this.stageRound = 0;
      this.totalRounds = 0;
      this.completedStages = [];
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
