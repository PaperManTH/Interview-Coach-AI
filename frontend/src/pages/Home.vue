<script setup lang="ts">
import logoPng from '@/assets/logo.png';
import settingPng from '@/assets/setting_icon.png';
import chatPng from '@/assets/chat_icon.png';
import voicePng from '@/assets/voice_icon.png';
import toolPng from '@/assets/tool_icon.png';
import dataPng from '@/assets/data_analysis.png';
import uploadBtnPng from '@/assets/upload_button_icon.png';
import heroPng from '@/assets/study_process.png';
import bgPng from '@/assets/background.png';
import loginIconPng from '@/assets/login_icon.png';
import flowChartPng from '@/assets/flow_chart.png';
import { ref, onMounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import ResumeUpload from '@/components/resume/ResumeUpload.vue';
import UserDropdown from '@/components/UserDropdown.vue';
import type { ResumeResponse } from '@/types/resume';
import type { InterviewFocus } from '@/types/scene';
import { INTERVIEW_FOCUS_OPTIONS } from '@/types/scene';

const router = useRouter();
const auth = useAuthStore();

// 简历
const resumeUpload = ref<InstanceType<typeof ResumeUpload> | null>(null);
const resumeResult = ref<ResumeResponse | null>(null);
const resumeName = computed(() => resumeResult.value?.fileName ?? '');
const resumeSkills = computed(() => resumeResult.value?.parsedData?.skills ?? []);
const hasResume = computed(() => !!resumeResult.value);

// 面试焦点选择
const selectedFocus = ref<InterviewFocus>('general');

onMounted(() => {
  try {
    const raw = localStorage.getItem('icai:resume-result');
    if (raw) {
      const data = JSON.parse(raw) as ResumeResponse;
      if (data?.id) resumeResult.value = data;
    }
  } catch { /* noop */ }
});

function onResumeUploaded(payload: { name: string; size: number; result: any }) {
  if (payload.result) {
    resumeResult.value = payload.result as ResumeResponse;
    // 有简历时自动切换到 resume 模式
    selectedFocus.value = 'resume';
  }
}

function clearResume() {
  resumeResult.value = null;
  resumeUpload.value?.clearResult();
  try { localStorage.removeItem('icai:resume-result'); } catch { /* noop */ }
}

function startInterview() {
  // 跳转到动态面试页面，携带 focus 参数
  router.push({
    name: 'DynamicInterview',
    query: { focus: selectedFocus.value }
  });
}
</script>

<template>
  <!-- 全宽背景层 -->
  <div class="page-wrapper" :style="{ '--bg-image': `url(${bgPng})` }">
    <div class="home">
      <header class="nav">
        <div class="logo-wrap">
          <img :src="logoPng" class="logo-mark" alt="logo" />
          <div class="logo-text">
            <div class="logo-title">Interview Coach</div>
            <div class="logo-sub">AI 面试教练</div>
          </div>
        </div>
        <nav class="nav-actions">
          <button v-if="auth.isLoggedIn" class="nav-btn" @click="router.push('/settings')" title="API 配置">
            <img :src="settingPng" class="nav-icon" alt="settings" />
            <span class="nav-label">Settings</span>
          </button>
          <UserDropdown v-if="auth.isLoggedIn" />
          <button v-else class="login-btn-icon" @click="router.push('/login')" title="登录">
            <img :src="loginIconPng" class="login-btn-icon-img" alt="登录" />
          </button>
        </nav>
      </header>

      <!-- ===== 主 Hero 区：AI Interview Coach ===== -->
      <section class="hero">
        <div class="hero-tag">
          <span class="tag-dot"></span>
          <span>Real-time · AI Powered · Dynamic Stages</span>
          <span class="tag-zh">实时对话 · 动态阶段 · 自然过渡</span>
        </div>

        <h1 class="hero-title">
          <span class="hero-main">AI Interview Coach</span>
          <span class="hero-subtitle">One Interview, Three Natural Stages</span>
        </h1>
        <p class="hero-desc">
          Experience a real interview flow — from warm-up self-introduction to technical deep-dive and pressure challenges.
          All in one seamless session.
        </p>

        <!-- 未登录提示 -->
        <div v-if="!auth.isLoggedIn" class="hero-cta">
          <button class="btn-login" @click="router.push('/login')">
            <img :src="heroPng" class="cta-emoji" alt="" />
            <span>登录后开始面试练习</span>
            <span class="arrow">→</span>
          </button>
        </div>

        <!-- 已登录：Start Interview 主按钮 -->
        <div v-else class="start-section">
          <button class="btn-start" @click="startInterview">
            <span class="btn-start-icon">▶</span>
            <span class="btn-start-text">Start Interview</span>
            <span class="btn-start-zh">开始面试</span>
            <span class="arrow">→</span>
          </button>

          <!-- Interview Focus 选择 -->
          <div class="focus-selector">
            <div class="focus-label">Interview Focus / 面试重点</div>
            <div class="focus-options">
              <label
                v-for="opt in INTERVIEW_FOCUS_OPTIONS"
                :key="opt.value"
                class="focus-option"
                :class="{ active: selectedFocus === opt.value }"
              >
                <input
                  type="radio"
                  :value="opt.value"
                  v-model="selectedFocus"
                  class="focus-radio"
                />
                <span class="focus-radio-circle"></span>
                <div class="focus-info">
                  <span class="focus-name">{{ opt.label }}</span>
                  <span class="focus-name-zh">{{ opt.labelZh }}</span>
                  <span class="focus-desc">{{ opt.desc }}</span>
                </div>
              </label>
            </div>
          </div>
        </div>
      </section>

      <!-- 三阶段流程展示 -->
      <section class="stages-section">
        <div class="flow-chart-wrap">
          <img :src="flowChartPng" class="flow-chart-img" alt="Interview Flow" />
        </div>
      </section>

      <!-- 特性卡片 -->
      <section class="features">
        <div class="feature-card">
          <div class="feature-icon"><img :src="chatPng" alt="" /></div>
          <div class="feature-text">
            <h3>Dynamic Flow</h3>
            <p>动态流程 · 三阶段自然递进</p>
          </div>
        </div>
        <div class="feature-card">
          <div class="feature-icon"><img :src="voicePng" alt="" /></div>
          <div class="feature-text">
            <h3>Voice Recognition</h3>
            <p>语音识别 · 说话即时获得反馈</p>
          </div>
        </div>
        <div class="feature-card">
          <div class="feature-icon"><img :src="toolPng" alt="" /></div>
          <div class="feature-text">
            <h3>Adaptive Questions</h3>
            <p>自适应提问 · 根据回答深度调整</p>
          </div>
        </div>
      </section>

      <!-- 简历上传（需登录） -->
      <section v-if="auth.isLoggedIn" class="resume-section">
        <div class="resume-card">
          <div class="resume-icon-wrap">
            <img :src="dataPng" class="resume-icon" alt="resume" />
          </div>
          <div class="resume-text">
            <div class="resume-title">Upload Your Resume (Optional)</div>
            <div class="resume-sub">上传简历（可选）· 让 AI 基于你的经历定制技术问题</div>
          </div>
          <div class="resume-action">
            <div v-if="hasResume" class="resume-info">
              <span class="resume-name" :title="resumeName">{{ resumeName }}</span>
              <span class="resume-meta">解析完成 · 已就绪</span>
              <div v-if="resumeSkills.length" class="resume-skills">
                <span v-for="s in resumeSkills.slice(0, 5)" :key="s" class="skill-tiny">{{ s }}</span>
                <span v-if="resumeSkills.length > 5" class="skill-more">+{{ resumeSkills.length - 5 }}</span>
              </div>
              <button class="ghost-btn" @click="clearResume" title="移除">✕</button>
            </div>
            <button v-else class="upload-btn" @click="resumeUpload?.quickUpload()">
              <img :src="uploadBtnPng" class="upload-btn-icon" alt="上传简历" />
            </button>
          </div>
        </div>
      </section>

      <footer class="footer">
        <p class="footer-en">Practice makes perfect · 🚀 Keep going!</p>
        <p class="footer-zh">熟能生巧 · 继续练习，你离 offer 只差一步</p>
      </footer>

      <ResumeUpload v-if="auth.isLoggedIn" ref="resumeUpload" :hide-trigger="true" @uploaded="onResumeUploaded" />
    </div>
  </div>
</template>

<style scoped>
/* ============ 全宽背景层 ============ */
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

.home {
  position: relative;
  z-index: 1;
  max-width: min(1100px, 100%);
  margin: 0 auto;
  min-height: 100vh;
  padding: 0 clamp(16px, 4vw, 48px) 48px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

/* ============ 导航 ============ */
.nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0 24px;
}
.logo-wrap { display: flex; align-items: center; gap: 12px; }
.logo-mark {
  width: 63px; height: 63px;
  border-radius: 12px;
  object-fit: contain;
  display: block;
}
.logo-title { font-size: 16px; font-weight: 700; color: #0f172a; letter-spacing: -0.01em; }
.logo-sub   { font-size: 12px; color: #475569; }

.nav-actions { display: flex; align-items: center; gap: 10px; }
.nav-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(148, 163, 184, 0.3);
  color: #1e293b;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: all 150ms;
  backdrop-filter: blur(4px);
}
.nav-btn:hover { background: rgba(241, 245, 249, 0.8); border-color: rgba(148, 163, 184, 0.5); }
.nav-icon { width: 27px; height: 27px; object-fit: contain; display: block; }

.nav-btn.primary {
  background: linear-gradient(135deg, #1e40af, #1e3a5f);
  color: #fff;
  border-color: transparent;
  box-shadow: 0 2px 8px rgba(30, 64, 175, 0.35);
}
.nav-btn.primary:hover { box-shadow: 0 4px 14px rgba(30, 64, 175, 0.45); }

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 4px 4px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.65);
  border: 1px solid rgba(148, 163, 184, 0.3);
  backdrop-filter: blur(4px);
  position: relative;
}

.avatar-clickable {
  position: relative;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.avatar-glow {
  width: 38px; height: 38px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  transition: all 250ms cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
}
.avatar-glow::before {
  content: '';
  position: absolute;
  inset: -3px;
  border-radius: 50%;
  border: 2px solid transparent;
  background: conic-gradient(from 0deg, #3b82f6, #8b5cf6, #3b82f6) border-box;
  -webkit-mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  mask: linear-gradient(#fff 0 0) padding-box, linear-gradient(#fff 0 0);
  -webkit-mask-composite: xor;
  mask-composite: exclude;
  opacity: 0;
  transition: opacity 250ms;
}
.avatar-clickable:hover .avatar-glow::before {
  opacity: 1;
  animation: glow-rotate 2s linear infinite;
}
@keyframes glow-rotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
.avatar-clickable:hover .avatar-glow { transform: scale(1.08); }
.avatar-clickable:active .avatar-glow { transform: scale(0.95); }

.avatar-badge {
  position: absolute;
  bottom: -4px; right: -4px;
  width: 18px; height: 18px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  border: 2px solid #fff;
  font-size: 10px;
  display: flex; align-items: center; justify-content: center;
  color: #fff;
  box-shadow: 0 2px 6px rgba(59, 130, 246, 0.35);
  pointer-events: none;
  z-index: 5;
}

.avatar {
  width: 36px; height: 36px;
  border-radius: 50%;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px rgba(148, 163, 184, 0.3);
  background: #f1f5f9;
  object-fit: cover;
  display: block;
  position: relative;
  z-index: 1;
}
.avatar.fallback {
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  color: #fff;
  font-size: 15px; font-weight: 700;
}
.username { font-size: 14px; font-weight: 600; color: #1e293b; }
.logout-btn {
  border: none; background: transparent;
  color: #64748b;
  width: 26px; height: 26px;
  border-radius: 50%;
  cursor: pointer;
  font-size: 14px;
  transition: all 150ms;
}
.logout-btn:hover { background: rgba(239, 68, 68, 0.12); color: #dc2626; }

/* ============ Hero 区 ============ */
.hero {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 40px 0 36px;
}
.hero-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  background: rgba(219, 234, 254, 0.45);
  color: #1e40af;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 24px;
  backdrop-filter: blur(6px);
}
.tag-dot {
  width: 6px; height: 6px; border-radius: 50%;
  background: #3b82f6;
  animation: pulse 1.6s infinite;
}
.tag-zh { color: #2563eb; opacity: 0.7; margin-left: 4px; }
@keyframes pulse { 0%,100% { opacity: 1; transform: scale(1); } 50% { opacity: 0.5; transform: scale(1.3); } }

.hero-title {
  margin: 0 0 16px;
  font-size: clamp(32px, 5vw, 56px);
  font-weight: 800;
  line-height: 1.12;
  color: #0f172a;
  letter-spacing: -0.02em;
}
.hero-main {
  display: block;
  background: linear-gradient(135deg, #1e40af, #6366f1, #8b5cf6);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}
.hero-subtitle {
  display: block;
  font-size: clamp(16px, 2.5vw, 22px);
  font-weight: 600;
  color: #334155;
  margin-top: 6px;
}

.hero-desc {
  max-width: 560px;
  margin: 0 auto;
  font-size: 16px;
  line-height: 1.65;
  color: #475569;
}

/* ============ Start Interview 按钮 ============ */
.start-section {
  margin-top: 28px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}
.btn-start {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 18px 40px;
  background: linear-gradient(135deg, #1e40af, #6366f1);
  color: #fff;
  border: none;
  border-radius: 16px;
  font-size: 20px;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 8px 30px rgba(30, 64, 175, 0.4);
  transition: all 250ms cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
  overflow: hidden;
}
.btn-start::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, transparent, rgba(255,255,255,0.15));
  opacity: 0;
  transition: opacity 250ms;
}
.btn-start:hover {
  transform: translateY(-3px) scale(1.02);
  box-shadow: 0 14px 40px rgba(30, 64, 175, 0.5);
}
.btn-start:hover::before { opacity: 1; }
.btn-start:active { transform: translateY(0) scale(0.98); }

.btn-start-icon {
  width: 28px; height: 28px;
  background: rgba(255,255,255,0.25);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
}
.btn-start-text { letter-spacing: 0.02em; }
.btn-start-zh {
  font-size: 13px;
  font-weight: 500;
  opacity: 0.8;
  margin-left: -4px;
}
.arrow {
  margin-left: 4px;
  transition: transform 200ms;
  font-size: 18px;
}
.btn-start:hover .arrow { transform: translateX(6px); }

/* ============ Interview Focus 选择器 ============ */
.focus-selector {
  width: 100%;
  max-width: 520px;
}
.focus-label {
  font-size: 13px;
  font-weight: 600;
  color: #64748b;
  margin-bottom: 10px;
  letter-spacing: 0.02em;
}
.focus-options {
  display: flex;
  gap: 10px;
  justify-content: center;
}
.focus-option {
  flex: 1;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 12px 14px;
  background: rgba(255, 255, 255, 0.6);
  border: 1.5px solid rgba(148, 163, 184, 0.25);
  border-radius: 14px;
  cursor: pointer;
  transition: all 200ms cubic-bezier(0.16, 1, 0.3, 1);
  backdrop-filter: blur(4px);
}
.focus-option:hover {
  background: rgba(255, 255, 255, 0.85);
  border-color: rgba(99, 102, 241, 0.4);
}
.focus-option.active {
  background: rgba(99, 102, 241, 0.08);
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}

.focus-radio {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}
.focus-radio-circle {
  width: 18px; height: 18px;
  border-radius: 50%;
  border: 2px solid #94a3b8;
  flex-shrink: 0;
  margin-top: 1px;
  transition: all 200ms;
  position: relative;
}
.focus-option.active .focus-radio-circle {
  border-color: #6366f1;
}
.focus-option.active .focus-radio-circle::after {
  content: '';
  position: absolute;
  inset: 3px;
  border-radius: 50%;
  background: #6366f1;
}

.focus-info { display: flex; flex-direction: column; gap: 2px; }
.focus-name {
  font-size: 14px;
  font-weight: 700;
  color: #0f172a;
}
.focus-name-zh {
  font-size: 11px;
  color: #64748b;
}
.focus-desc {
  font-size: 11px;
  color: #94a3b8;
  margin-top: 2px;
  line-height: 1.4;
}

/* ============ 三阶段流程展示 ============ */
.stages-section {
  padding: 32px 0 16px;
}
.stages-flow {
  display: flex;
  align-items: stretch;
  justify-content: center;
  gap: 28px;
}
.stage-node {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  max-width: 280px;
  padding: 16px 18px;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 16px;
  backdrop-filter: blur(6px);
  position: relative;
}
.stage-number {
  width: 36px; height: 36px;
  border-radius: 50%;
  background: var(--stage-accent, #2563eb);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  font-weight: 800;
  flex-shrink: 0;
}
.stage-body { flex: 1; min-width: 0; }
.stage-label {
  font-size: 15px;
  font-weight: 700;
  color: #0f172a;
}
.stage-label-zh {
  font-size: 11px;
  color: var(--stage-accent, #2563eb);
  font-weight: 600;
}
.stage-desc {
  font-size: 11px;
  color: #64748b;
  margin-top: 2px;
  line-height: 1.4;
}
.stage-arrow {
  position: absolute;
  right: -16px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  color: #cbd5e1;
  z-index: 2;
}

/* ============ 登录按钮（未登录状态）============ */
.hero-cta { margin-top: 28px; }
.btn-login {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  padding: 14px 28px;
  background: linear-gradient(135deg, #0f172a, #1e293b);
  color: #fff;
  border: none;
  border-radius: 14px;
  font-size: 15px;
  font-weight: 700;
  cursor: pointer;
  box-shadow: 0 6px 20px rgba(15, 23, 42, 0.25);
  transition: all 200ms cubic-bezier(0.16, 1, 0.3, 1);
}
.btn-login:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.35);
}
.btn-login .arrow { margin-left: 4px; transition: transform 200ms; }
.btn-login:hover .arrow { transform: translateX(4px); }
.cta-emoji { width: 30px; height: 30px; object-fit: contain; display: inline-block; }

/* ============ 特性卡片 ============ */
.features {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  padding: 24px 0 8px;
}
.feature-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
  background: rgba(255, 255, 255, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 16px;
  backdrop-filter: blur(6px);
  transition: all 200ms cubic-bezier(0.16, 1, 0.3, 1);
}
.feature-card:hover {
  transform: translateY(-2px);
  background: rgba(255, 255, 255, 0.7);
  box-shadow: 0 8px 20px -8px rgba(15, 23, 42, 0.12);
  border-color: rgba(148, 163, 184, 0.35);
}
.feature-icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.feature-icon img {
  width: 48px;
  height: 48px;
  object-fit: contain;
  display: block;
}
.feature-text h3 { margin: 0 0 4px; font-size: 15px; font-weight: 700; color: #0f172a; }
.feature-text p  { margin: 0; font-size: 12.5px; color: #475569; line-height: 1.5; }

/* ============ 简历上传卡片 ============ */
.resume-section { padding: 28px 0 12px; }
.resume-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 20px 24px;
  background: rgba(255, 255, 255, 0.5);
  border: 1.5px dashed rgba(59, 130, 246, 0.3);
  border-radius: 18px;
  backdrop-filter: blur(6px);
  transition: all 200ms;
}
.resume-card:hover {
  border-color: rgba(59, 130, 246, 0.55);
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.65);
}
.resume-icon-wrap {
  width: 84px; height: 84px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.15), rgba(99, 102, 241, 0.12));
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.resume-icon { width: 48px; height: 48px; object-fit: contain; display: block; }
.resume-text { flex: 1; min-width: 0; }
.resume-title { font-size: 16px; font-weight: 700; color: #0f172a; margin-bottom: 3px; }
.resume-sub   { font-size: 13px; color: #475569; }

.resume-action { display: flex; align-items: center; gap: 10px; }
.upload-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 200ms cubic-bezier(0.16, 1, 0.3, 1);
}
.upload-btn-icon {
  height: 52px;
  width: auto;
  display: block;
  object-fit: contain;
  filter: drop-shadow(0 4px 12px rgba(30, 64, 175, 0.3));
  transition: all 200ms;
}
.upload-btn:hover .upload-btn-icon {
  transform: translateY(-2px);
  filter: drop-shadow(0 6px 18px rgba(30, 64, 175, 0.45));
}
.upload-btn:active .upload-btn-icon { transform: translateY(0); }

.resume-info {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  padding: 10px 14px;
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid rgba(16, 185, 129, 0.25);
  border-radius: 12px;
  position: relative;
}
.resume-info .resume-name {
  font-size: 13px; font-weight: 700; color: #065f46;
  max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.resume-info .resume-meta { font-size: 11px; color: #059669; }
.resume-skills {
  display: flex; flex-wrap: wrap; gap: 4px; margin-top: 2px; max-width: 200px;
}
.skill-tiny {
  display: inline-block; padding: 2px 8px; border-radius: 999px;
  background: rgba(59, 130, 246, 0.12); border: 1px solid rgba(59, 130, 246, 0.2);
  color: #1d4ed8; font-size: 11px; font-weight: 500;
}
.skill-more {
  display: inline-block; padding: 2px 8px; border-radius: 999px;
  background: rgba(148, 163, 184, 0.1); color: #64748b; font-size: 11px; font-weight: 500;
}
.ghost-btn {
  position: absolute; top: 4px; right: 4px;
  width: 18px; height: 18px;
  border: none; border-radius: 50%;
  background: #fff; color: #059669;
  font-size: 11px; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  transition: all 120ms;
}
.ghost-btn:hover { background: #fee2e2; color: #dc2626; }

/* ============ Footer ============ */
.footer {
  text-align: center;
  padding: 40px 0 0;
  margin-top: 24px;
  border-top: 1px solid rgba(148, 163, 184, 0.2);
}
.footer-en { margin: 0 0 4px; font-size: 13px; color: #334155; font-weight: 600; }
.footer-zh { margin: 0; font-size: 12px; color: #64748b; }

/* ============ 登录图标按钮 ============ */
.login-btn-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 200ms cubic-bezier(0.16, 1, 0.3, 1);
}
.login-btn-icon-img {
  height: 64px;
  width: auto;
  display: block;
  object-fit: contain;
  filter: drop-shadow(0 4px 12px rgba(30, 64, 175, 0.3));
  transition: all 200ms;
}
.login-btn-icon:hover .login-btn-icon-img {
  transform: translateY(-2px);
  filter: drop-shadow(0 6px 18px rgba(30, 64, 175, 0.45));
}
.login-btn-icon:active .login-btn-icon-img { transform: translateY(0); }

/* ============ 流程图展示 ============ */
.stages-section {
  padding: 40px 0 24px;
}
.flow-chart-wrap {
  background: #ffffff;
  border-radius: 24px;
  padding: 28px 36px;
  box-shadow: 
    0 4px 24px rgba(15, 23, 42, 0.08),
    0 1px 2px rgba(15, 23, 42, 0.04);
  display: flex;
  justify-content: center;
  align-items: center;
  border: 1px solid rgba(148, 163, 184, 0.15);
  transition: all 200ms cubic-bezier(0.16, 1, 0.3, 1);
}
.flow-chart-wrap:hover {
  transform: translateY(-2px);
  box-shadow: 
    0 8px 36px rgba(15, 23, 42, 0.12),
    0 2px 4px rgba(15, 23, 42, 0.06);
}
.flow-chart-img {
  max-width: 100%;
  width: min(1000px, 100%);
  height: auto;
  display: block;
  object-fit: contain;
}

/* ============ 响应式 ============ */
@media (max-width: 880px) {
  .features { grid-template-columns: 1fr; }
  .flow-chart-wrap { padding: 20px 20px; border-radius: 18px; }
  .focus-options { flex-direction: column; }
}

@media (max-width: 640px) {
  .home { padding: 0 16px 40px; background: rgba(255, 255, 255, 0.78); }
  .nav { padding: 16px 0 20px; }
  .logo-mark { width: 54px; height: 54px; }
  .logo-sub   { display: none; }
  .nav-label { display: none; }
  .hero { padding: 32px 0 24px; }
  .hero-desc { font-size: 14px; }
  .btn-start { padding: 14px 28px; font-size: 17px; }
  .btn-login { padding: 12px 22px; font-size: 14px; }
  .login-btn-icon-img { height: 52px; }
  .resume-card { flex-direction: column; text-align: center; padding: 18px; }
  .resume-text { width: 100%; }
  .resume-action { width: 100%; justify-content: center; }
  .resume-info { align-items: center; text-align: center; }
  .flow-chart-wrap { padding: 16px 14px; border-radius: 14px; }
}
</style>
