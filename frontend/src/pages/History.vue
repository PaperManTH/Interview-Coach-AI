<script setup lang="ts">
/**
 * 面试历史记录页面
 * 
 * 功能：
 * - 显示用户的面试会话列表
 * - 点击会话查看详细对话记录
 * - 支持删除会话
 * - 支持分页加载
 */
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import {
  getSessionList,
  getSessionMessages,
  deleteSession,
  type InterviewSession,
  type InterviewMessage,
} from '@/services/conversationApi';
import bgPng from '@/assets/background.png';

const router = useRouter();
const auth = useAuthStore();

// ===== 状态 =====
const sessions = ref<InterviewSession[]>([]);
const selectedSession = ref<InterviewSession | null>(null);
const messages = ref<InterviewMessage[]>([]);
const loading = ref(true);
const loadingMessages = ref(false);
const currentPage = ref(1);
const totalSessions = ref(0);
const pageSize = ref(10);
const errorMsg = ref('');
const deleting = ref(false);

// ===== 计算属性 =====
const hasMorePages = computed(() => {
  return currentPage.value * pageSize.value < totalSessions.value;
});

const interviewTypeLabel = computed(() => {
  const labels: Record<string, string> = {
    hr: 'HR 面试',
    technical: '技术面试',
    pressure: '压力面试',
    dynamic: '动态面试',
    general: '通用面试',
  };
  return (type: string) => labels[type] || type;
});

const statusLabel = computed(() => {
  const labels: Record<string, { text: string; class: string }> = {
    active: { text: '进行中', class: 'status-active' },
    ended: { text: '已结束', class: 'status-ended' },
  };
  return (status: string) => labels[status] || { text: status, class: '' };
});

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const formatDuration = (session: InterviewSession) => {
  if (!session.endedAt) return '进行中';
  const start = new Date(session.startedAt).getTime();
  const end = new Date(session.endedAt).getTime();
  const diff = Math.floor((end - start) / 1000);
  const minutes = Math.floor(diff / 60);
  const seconds = diff % 60;
  return `${minutes}分${seconds}秒`;
};

// ===== 方法 =====
async function loadSessions() {
  loading.value = true;
  errorMsg.value = '';
  try {
    const response = await getSessionList(auth.userId || 'anonymous', currentPage.value, pageSize.value);
    if (response.success) {
      sessions.value = response.data;
      totalSessions.value = response.total;
    } else {
      errorMsg.value = '加载失败';
    }
  } catch (e) {
    errorMsg.value = e instanceof Error ? e.message : '加载失败';
  } finally {
    loading.value = false;
  }
}

async function selectSession(session: InterviewSession) {
  selectedSession.value = session;
  messages.value = [];
  loadingMessages.value = true;
  try {
    const response = await getSessionMessages(session.sessionId);
    if (response.success) {
      messages.value = response.data;
    }
  } catch (e) {
    console.error('加载消息失败:', e);
  } finally {
    loadingMessages.value = false;
  }
}

function closeDetail() {
  selectedSession.value = null;
  messages.value = [];
}

async function handleDelete(sessionId: string) {
  if (!confirm('确定要删除这个面试记录吗？此操作不可恢复。')) return;
  
  deleting.value = true;
  try {
    const response = await deleteSession(sessionId);
    if (response.success) {
      sessions.value = sessions.value.filter(s => s.sessionId !== sessionId);
      if (selectedSession.value?.sessionId === sessionId) {
        closeDetail();
      }
      totalSessions.value--;
    } else {
      errorMsg.value = response.message;
    }
  } catch (e) {
    errorMsg.value = e instanceof Error ? e.message : '删除失败';
  } finally {
    deleting.value = false;
  }
}

function loadNextPage() {
  if (hasMorePages.value) {
    currentPage.value++;
    loadSessions();
  }
}

function goBack() {
  router.push('/');
}

function startNewInterview() {
  router.push('/');
}

// 解析 metadata 获取双语内容
function parseMetadata(metadata: string | null): { chinese?: string; english?: string } {
  if (!metadata) return {};
  try {
    return JSON.parse(metadata);
  } catch {
    return {};
  }
}

onMounted(() => {
  loadSessions();
});
</script>

<template>
  <div class="page-wrapper" :style="{ '--bg-image': `url(${bgPng})` }">
    <div class="history-page">
      <!-- ============ Header ============ -->
      <div class="history-header">
        <button class="back-btn" @click="goBack">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m15 18-6-6 6-6"/></svg>
          <span>返回</span>
        </button>
        <div class="header-content">
          <h1>面试历史</h1>
          <p class="subtitle">Interview History · 查看过去的面试记录</p>
        </div>
        <button class="btn-new-interview" @click="startNewInterview">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 5v14M5 12h14"/></svg>
          <span>新面试</span>
        </button>
      </div>

      <!-- ============ Loading ============ -->
      <div v-if="loading" class="history-body">
        <div class="loading-state">
          <div class="spinner"></div>
          <p>加载面试记录...</p>
        </div>
      </div>

      <!-- ============ Empty State ============ -->
      <div v-else-if="sessions.length === 0" class="history-body">
        <div class="empty-state">
          <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line :x1="16" :y1="13" :x2="8" :y2="13"/>
            <line :x1="16" :y1="17" :x2="8" :y2="17"/>
            <polyline points="10 9 9 9 8 9"/>
          </svg>
          <h3>暂无面试记录</h3>
          <p>完成面试后，记录会自动保存在这里</p>
          <button class="btn-primary" @click="startNewInterview">开始面试</button>
        </div>
      </div>

      <!-- ============ Session List + Detail ============ -->
      <div v-else class="history-body">
        <div class="content-grid">
          <!-- 左侧：会话列表 -->
          <div class="session-list-panel">
            <div class="panel-header">
              <span class="panel-title">面试记录</span>
              <span class="session-count">{{ totalSessions }} 条</span>
            </div>
            
            <div class="session-list">
              <div
                v-for="session in sessions"
                :key="session.sessionId"
                class="session-card"
                :class="{ active: selectedSession?.sessionId === session.sessionId }"
                @click="selectSession(session)"
              >
                <div class="session-main">
                  <div class="session-type-badge">{{ interviewTypeLabel(session.interviewType) }}</div>
                  <div class="session-date">{{ formatDate(session.startedAt) }}</div>
                </div>
                <div class="session-meta">
                  <span :class="['session-status', statusLabel(session.status).class]">
                    {{ statusLabel(session.status).text }}
                  </span>
                  <span class="session-duration">{{ formatDuration(session) }}</span>
                </div>
                <button
                  class="delete-btn"
                  @click.stop="handleDelete(session.sessionId)"
                  :disabled="deleting"
                  title="删除记录"
                >
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <polyline points="3 6 5 6 21 6"/>
                    <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"/>
                  </svg>
                </button>
              </div>
            </div>

            <!-- 分页 -->
            <div v-if="hasMorePages" class="pagination">
              <button class="btn-load-more" @click="loadNextPage" :disabled="loading">
                加载更多
              </button>
            </div>
          </div>

          <!-- 右侧：对话详情 -->
          <div class="detail-panel">
            <div v-if="!selectedSession" class="detail-empty">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
              </svg>
              <p>选择左侧的面试记录查看详情</p>
            </div>

            <div v-else class="detail-content">
              <div class="detail-header">
                <div class="detail-title">
                  <span class="type-badge">{{ interviewTypeLabel(selectedSession.interviewType) }}</span>
                  <span class="detail-date">{{ formatDate(selectedSession.startedAt) }}</span>
                </div>
                <button class="close-btn" @click="closeDetail">
                  <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <line :x1="18" :y1="6" :x2="6" :y2="18"/>
                    <line :x1="6" :y1="6" :x2="18" :y2="18"/>
                  </svg>
                </button>
              </div>

              <!-- 消息列表 -->
              <div v-if="loadingMessages" class="messages-loading">
                <div class="spinner small"></div>
                <span>加载对话...</span>
              </div>

              <div v-else class="message-list">
                <div
                  v-for="msg in messages"
                  :key="msg.messageId"
                  :class="['message-item', msg.sender]"
                >
                  <div class="message-avatar">
                    <span v-if="msg.sender === 'user'" class="avatar-user">你</span>
                    <span v-else class="avatar-ai">AI</span>
                  </div>
                  <div class="message-body">
                    <div class="message-text">{{ msg.content }}</div>
                    <div v-if="msg.metadata" class="message-bilingual">
                      <div class="bilingual-row">
                        <span class="lang-label">中文</span>
                        <span class="lang-text">{{ parseMetadata(msg.metadata).chinese || '-' }}</span>
                      </div>
                      <div class="bilingual-row">
                        <span class="lang-label">英文</span>
                        <span class="lang-text">{{ parseMetadata(msg.metadata).english || '-' }}</span>
                      </div>
                    </div>
                    <div class="message-time">
                      {{ new Date(msg.timestampMs).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Error Toast -->
      <Transition name="fade">
        <div v-if="errorMsg" class="toast error">{{ errorMsg }}</div>
      </Transition>
    </div>
  </div>
</template>

<style scoped>
/* ============ Page wrapper ============ */
.page-wrapper {
  min-height: 100vh;
  background: var(--bg-image) center center / cover no-repeat fixed;
  position: relative;
}
.page-wrapper::before {
  content: '';
  position: fixed;
  inset: 0;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.45) 0%, rgba(15, 23, 42, 0.2) 30%, rgba(15, 23, 42, 0.35) 100%);
  pointer-events: none;
  z-index: 0;
}

/* ============ Content container ============ */
.history-page {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  padding-bottom: 60px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

/* ============ Header ============ */
.history-header {
  display: flex;
  align-items: center;
  gap: 20px;
  max-width: min(1200px, 100%);
  margin: 0 auto;
  padding: 20px clamp(16px, 4vw, 48px);
  border-bottom: 1px solid rgba(148, 163, 184, 0.15);
}
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 16px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 10px;
  color: #475569;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: all 150ms;
}
.back-btn:hover {
  background: rgba(241, 245, 249, 0.8);
  border-color: rgba(148, 163, 184, 0.5);
  transform: translateX(-2px);
}
.header-content h1 { margin: 0; font-size: 19px; font-weight: 700; color: #0f172a; }
.subtitle { margin: 4px 0 0; font-size: 14px; color: #64748b; }
.btn-new-interview {
  margin-left: auto;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 18px;
  background: linear-gradient(135deg, #1e40af, #1e3a5f);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 150ms;
}
.btn-new-interview:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 14px rgba(30, 64, 175, 0.4);
}

/* ============ Body ============ */
.history-body {
  max-width: min(1200px, 100%);
  margin: 0 auto;
  padding: 24px clamp(16px, 4vw, 48px);
}

/* ============ Loading / Empty ============ */
.loading-state, .empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 20px;
  text-align: center;
}
.spinner {
  width: 40px; height: 40px;
  border: 3px solid rgba(148, 163, 184, 0.2);
  border-top-color: #1e40af;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
.spinner.small { width: 24px; height: 24px; border-width: 2px; }
@keyframes spin { to { transform: rotate(360deg); } }
.loading-state p { margin-top: 16px; color: #64748b; font-size: 15px; }
.empty-state svg { color: #94a3b8; }
.empty-state h3 { margin: 16px 0 8px; font-size: 18px; color: #0f172a; }
.empty-state p { margin: 0 0 24px; color: #64748b; font-size: 14px; }
.btn-primary {
  padding: 10px 24px;
  background: linear-gradient(135deg, #1e40af, #1e3a5f);
  border: none;
  border-radius: 10px;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}
.btn-primary:hover { transform: translateY(-1px); box-shadow: 0 4px 14px rgba(30, 64, 175, 0.4); }

/* ============ Content Grid ============ */
.content-grid {
  display: grid;
  grid-template-columns: 340px 1fr;
  gap: 24px;
  min-height: 500px;
}

/* ============ Session List Panel ============ */
.session-list-panel {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  display: flex;
  flex-direction: column;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
}
.panel-title { font-size: 15px; font-weight: 700; color: #0f172a; }
.session-count {
  padding: 3px 10px;
  background: rgba(30, 64, 175, 0.1);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #1e40af;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}
.session-card {
  display: flex;
  align-items: center;
  padding: 14px 16px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.15);
  border-radius: 12px;
  margin-bottom: 10px;
  cursor: pointer;
  transition: all 150ms;
}
.session-card:hover {
  background: rgba(241, 245, 249, 0.8);
  border-color: rgba(148, 163, 184, 0.3);
}
.session-card.active {
  background: rgba(30, 64, 175, 0.08);
  border-color: rgba(30, 64, 175, 0.3);
}
.session-main { flex: 1; }
.session-type-badge {
  display: inline-block;
  padding: 3px 8px;
  background: rgba(30, 64, 175, 0.12);
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #1e40af;
  margin-bottom: 6px;
}
.session-date { font-size: 13px; color: #64748b; }
.session-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  margin-right: 12px;
}
.session-status {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
}
.status-active { background: rgba(16, 185, 129, 0.15); color: #059669; }
.status-ended { background: rgba(148, 163, 184, 0.15); color: #64748b; }
.session-duration { font-size: 12px; color: #94a3b8; }
.delete-btn {
  padding: 6px;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: #94a3b8;
  cursor: pointer;
  transition: all 150ms;
}
.delete-btn:hover { background: rgba(239, 68, 68, 0.1); color: #dc2626; }
.delete-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.pagination { padding: 12px 16px; border-top: 1px solid rgba(148, 163, 184, 0.12); }
.btn-load-more {
  width: 100%;
  padding: 8px;
  background: rgba(30, 64, 175, 0.08);
  border: 1px solid rgba(30, 64, 175, 0.2);
  border-radius: 8px;
  color: #1e40af;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}
.btn-load-more:hover { background: rgba(30, 64, 175, 0.15); }
.btn-load-more:disabled { opacity: 0.5; cursor: not-allowed; }

/* ============ Detail Panel ============ */
.detail-panel {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  display: flex;
  flex-direction: column;
  min-height: 500px;
}
.detail-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
}
.detail-empty p { margin-top: 12px; font-size: 14px; }

.detail-content { flex: 1; display: flex; flex-direction: column; }
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
}
.detail-title { display: flex; align-items: center; gap: 12px; }
.type-badge {
  padding: 4px 10px;
  background: rgba(30, 64, 175, 0.12);
  border-radius: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #1e40af;
}
.detail-date { font-size: 13px; color: #64748b; }
.close-btn {
  padding: 6px;
  background: transparent;
  border: none;
  border-radius: 8px;
  color: #64748b;
  cursor: pointer;
}
.close-btn:hover { background: rgba(148, 163, 184, 0.15); color: #0f172a; }

.messages-loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #64748b;
  font-size: 14px;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
}
.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.message-item.user { flex-direction: row-reverse; }
.message-avatar {
  width: 32px; height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
}
.avatar-user { background: rgba(30, 64, 175, 0.15); color: #1e40af; }
.avatar-ai { background: rgba(16, 185, 129, 0.15); color: #059669; }
.message-body {
  flex: 1;
  max-width: 80%;
  padding: 12px 16px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 12px;
  border: 1px solid rgba(148, 163, 184, 0.15);
}
.message-item.user .message-body {
  background: rgba(30, 64, 175, 0.08);
  border-color: rgba(30, 64, 175, 0.2);
}
.message-text {
  font-size: 14px;
  color: #0f172a;
  line-height: 1.5;
  white-space: pre-wrap;
}
.message-bilingual {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed rgba(148, 163, 184, 0.2);
}
.bilingual-row {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
}
.lang-label {
  padding: 2px 6px;
  background: rgba(148, 163, 184, 0.15);
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  color: #64748b;
}
.lang-text { font-size: 13px; color: #475569; }
.message-time {
  margin-top: 8px;
  font-size: 11px;
  color: #94a3b8;
}
.message-item.user .message-time { text-align: right; }

/* ============ Toast ============ */
.toast {
  position: fixed;
  bottom: 40px;
  left: 50%;
  transform: translateX(-50%);
  padding: 12px 28px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  z-index: 1000;
}
.toast.error {
  background: rgba(239, 68, 68, 0.92);
  color: #fff;
  box-shadow: 0 4px 20px rgba(239, 68, 68, 0.3);
}

/* Transitions */
.fade-enter-active { transition: all 300ms cubic-bezier(0.16, 1, 0.3, 1); }
.fade-leave-active { transition: all 200ms ease-in; }
.fade-enter-from { opacity: 0; transform: translateY(8px); }
.fade-leave-to { opacity: 0; }

/* ============ Responsive ============ */
@media (max-width: 768px) {
  .content-grid { grid-template-columns: 1fr; }
  .detail-panel { display: none; }
  .session-card.active + .detail-panel { display: flex; position: fixed; inset: 0; z-index: 100; }
  .btn-new-interview span { display: none; }
  .back-btn span { display: none; }
}
</style>