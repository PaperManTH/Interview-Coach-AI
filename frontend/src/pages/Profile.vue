<script setup lang="ts">
/**
 * 个人主页 — 手动录入 / 编辑简历信息。
 * 视觉风格与 Settings 页面对齐：玻璃态容器 + 背景图 + 统一卡片系统。
 */
import { ref, onMounted, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/authStore';
import { saveManualResume, getManualResume, ResumeUploadError } from '@/services/resumeApi';
import bgPng from '@/assets/background.png';
import overviewPng from '@/assets/icon_candidate_overview.png';
import toolPng from '@/assets/icon_skills_list.png';
import workPng from '@/assets/icon_work_experience.png';
import projectPng from '@/assets/icon_project_experience.png';
import strengthsPng from '@/assets/icon_candidate_strengths.png';
import questionsPng from '@/assets/icon_interview_questions.png';

const router = useRouter();
const auth = useAuthStore();

const saving = ref(false);
const loading = ref(true);
const saved = ref(false);
const errorMsg = ref('');

interface FormState {
  candidateSummary: string;
  skills: string;
  projects: string;
  workExperience: string;
  strengths: string;
  possibleQuestions: string;
}

const form = reactive<FormState>({
  candidateSummary: '',
  skills: '',
  projects: '',
  workExperience: '',
  strengths: '',
  possibleQuestions: '',
});

// ==================== 默认预设 ====================

const defaultSkills = [
  'Java', 'Spring', 'Spring Boot', 'Spring Cloud', 'MySQL', 'Redis',
  'AI', 'RAG', 'Function Call', '数据结构',
];

const defaultQuestions = [
  '请简要介绍一下你自己',
  '请谈谈你对 Java 多线程的理解',
  'Spring Boot 自动配置的原理是什么？',
  '你在项目中是如何使用数据库和缓存的？',
  '描述一个你遇到的最有挑战性的技术问题，以及你是如何解决的',
];

function getDefaultSummary(): string {
  const s = (form.skills || defaultSkills.join('、'));
  return '候选人具有 ' + s + ' 等相关技能';
}

// ==================== 完成度 ====================

const completionPercent = computed(() => {
  let filled = 0;
  const fields: (keyof FormState)[] = [
    'candidateSummary', 'skills', 'projects',
    'workExperience', 'strengths',
  ];
  for (const f of fields) {
    if (form[f].trim()) filled++;
  }
  return Math.round((filled / fields.length) * 100);
});

const skillCount = computed(() =>
  form.skills ? form.skills.split(/[,，\n]/).filter(Boolean).length : 0,
);

const questionCount = computed(() =>
  form.possibleQuestions ? form.possibleQuestions.split(/[,，\n]/).filter(Boolean).length : 0,
);

// ==================== 加载已有数据 ====================

onMounted(async () => {
  try {
    const data = await getManualResume(auth.userId || undefined);
    if (data) {
      form.candidateSummary = data.candidateSummary || '';
      form.skills = (data.skills || []).join(', ');
      form.possibleQuestions = (data.possibleQuestions || []).join('\n');
      form.strengths = (data.strengths || []).join(', ');
      form.projects = (data.projects || []).join('\n');
      form.workExperience = (data.workExperience || []).join('\n');
    } else {
      form.skills = defaultSkills.join(', ');
      form.possibleQuestions = defaultQuestions.join('\n');
      form.candidateSummary = getDefaultSummary();
    }
  } catch {
    errorMsg.value = '加载失败，请稍后重试';
  } finally {
    loading.value = false;
  }
});

// ==================== 保存 ====================

async function handleSave() {
  saving.value = true;
  errorMsg.value = '';
  saved.value = false;

  try {
    // 拆分逗号/换行的字段
    const skills = form.skills ? form.skills.split(/[,，\n]/).map(s => s.trim()).filter(Boolean) : [];
    const projects = form.projects ? form.projects.split(/\n/).map(s => s.trim()).filter(Boolean) : [];
    const workExperience = form.workExperience ? form.workExperience.split(/\n/).map(s => s.trim()).filter(Boolean) : [];
    const strengths = form.strengths ? form.strengths.split(/[,，\n]/).map(s => s.trim()).filter(Boolean) : [];
    // 面试问题来自 PDF 上传的 AI 解析，手动保存时保留已有值，不覆盖
    const questions = form.possibleQuestions ? form.possibleQuestions.split('\n').map(s => s.trim()).filter(Boolean) : [];

    await saveManualResume({
      candidateSummary: form.candidateSummary.trim(),
      skills,
      projects,
      workExperience,
      strengths,
      possibleQuestions: questions,
    }, auth.userId || undefined);
    saved.value = true;
    setTimeout(() => (saved.value = false), 3000);
  } catch (e) {
    if (e instanceof ResumeUploadError) {
      errorMsg.value = e.message;
    } else {
      errorMsg.value = e instanceof Error ? e.message : '保存失败';
    }
  } finally {
    saving.value = false;
  }
}

function goBack() {
  router.push('/');
}

function autoFillDefaults() {
  form.skills = defaultSkills.join(', ');
  form.possibleQuestions = defaultQuestions.join('\n');
  form.candidateSummary = getDefaultSummary();
}
</script>

<template>
  <div class="page-wrapper" :style="{ '--bg-image': `url(${bgPng})` }">
    <div class="profile-page">
      <!-- ============ Header ============ -->
      <div class="profile-header">
        <button class="back-btn" @click="goBack">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="m15 18-6-6 6-6"/></svg>
          <span>返回</span>
        </button>
        <div class="header-content">
          <h1>个人主页</h1>
          <p class="subtitle">Resume Builder · AI 驱动的简历信息管理</p>
        </div>

        <!-- 完成度统计 -->
        <div v-if="!loading" class="header-progress">
          <div class="progress-ring">
            <svg width="40" height="40" viewBox="0 0 40 40">
              <circle cx="20" cy="20" r="17" fill="none" stroke="rgba(148,163,184,0.15)" stroke-width="3" />
              <circle
                cx="20" cy="20" r="17"
                fill="none"
                stroke="url(#pg)"
                stroke-width="3"
                stroke-linecap="round"
                :stroke-dasharray="106.8"
                :stroke-dashoffset="106.8 - (106.8 * completionPercent) / 100"
                style="transform: rotate(-90deg); transform-origin: center;"
              />
              <defs><linearGradient id="pg" x1="0%" y1="0%" x2="100%" y2="0%"><stop offset="0%" stop-color="#2563eb" /><stop offset="100%" stop-color="#6366f1" /></linearGradient></defs>
            </svg>
            <span class="progress-text">{{ completionPercent }}%</span>
          </div>
        </div>
      </div>

      <!-- ============ Loading ============ -->
      <div v-if="loading" class="profile-body">
        <p class="loading-text">加载中...</p>
      </div>

      <!-- ============ Form Grid ============ -->
      <div v-else class="profile-body">
        <div class="form-grid">
          <!-- 概览 -->
          <div class="form-col wide">
            <div class="section-card">
              <div class="card-header">
                <div class="card-icon"><img :src="overviewPng" alt="" /></div>
                <div class="card-title-group">
                  <div class="card-title">候选人概览</div>
                </div>
              </div>
              <div class="card-body">
                <textarea
                  v-model="form.candidateSummary"
                  class="form-textarea"
                  rows="3"
                  placeholder="一句话概括候选人的技术背景和方向"
                ></textarea>
              </div>
            </div>
          </div>

          <!-- 技能 -->
          <div class="form-col">
            <div class="section-card">
              <div class="card-header">
                <div class="card-icon"><img :src="toolPng" alt="" /></div>
                <div class="card-title-group">
                  <div class="card-title">技能列表</div>
                  <span class="configured-badge" v-if="skillCount > 0">{{ skillCount }} 项</span>
                </div>
              </div>
              <div class="card-body">
                <textarea
                  v-model="form.skills"
                  class="form-textarea"
                  rows="5"
                  placeholder="Java, Spring Boot, MySQL, Redis..."
                ></textarea>
              </div>
            </div>
          </div>

          <!-- 工作经历 -->
          <div class="form-col">
            <div class="section-card">
              <div class="card-header">
                <div class="card-icon"><img :src="workPng" alt="" /></div>
                <div class="card-title-group">
                  <div class="card-title">工作经历</div>
                </div>
              </div>
              <div class="card-body">
                <textarea
                  v-model="form.workExperience"
                  class="form-textarea"
                  rows="4"
                  placeholder="Senior Engineer at ABC Corp (2020-2024)&#10;负责微服务架构设计与实现"
                ></textarea>
              </div>
            </div>
          </div>

          <!-- 项目经验 -->
          <div class="form-col">
            <div class="section-card">
              <div class="card-header">
                <div class="card-icon"><img :src="projectPng" alt="" /></div>
                <div class="card-title-group">
                  <div class="card-title">项目经验</div>
                </div>
              </div>
              <div class="card-body">
                <textarea
                  v-model="form.projects"
                  class="form-textarea"
                  rows="4"
                  placeholder="电商平台架构升级：负责核心模块重构&#10;实时数据管道：搭建 Kafka + Flink 处理链路"
                ></textarea>
              </div>
            </div>
          </div>

          <!-- 优势 -->
          <div class="form-col">
            <div class="section-card">
              <div class="card-header">
                <div class="card-icon"><img :src="strengthsPng" alt="" /></div>
                <div class="card-title-group">
                  <div class="card-title">候选人优势</div>
                </div>
              </div>
              <div class="card-body">
                <textarea
                  v-model="form.strengths"
                  class="form-textarea"
                  rows="3"
                  placeholder="代码质量意识强、擅长技术攻坚、英语口语流利"
                ></textarea>
              </div>
            </div>
          </div>

          <!-- 面试问题（仅展示，不参与保存） -->
          <div class="form-col wide">
            <div class="section-card readonly-card">
              <div class="card-header">
                <div class="card-icon"><img :src="questionsPng" alt="" /></div>
                <div class="card-title-group">
                  <div class="card-title">可能的面试问题</div>
                  <span class="configured-badge" v-if="questionCount > 0">{{ questionCount }} 题</span>
                  <span class="readonly-tag">仅展示</span>
                </div>
              </div>
              <div class="card-body">
                <textarea
                  v-model="form.possibleQuestions"
                  class="form-textarea readonly"
                  rows="6"
                  readonly
                  placeholder="AI 面试官将根据你的简历自动生成个性化问题..."
                ></textarea>
              </div>
            </div>
          </div>
        </div>

        <!-- ============ Footer Actions ============ -->
        <div class="settings-footer">
          <div class="footer-content">
            <button class="btn btn-secondary" type="button" @click="autoFillDefaults">填入默认示例</button>
            <div class="footer-right">
              <button class="btn btn-secondary" type="button" @click="goBack">返回</button>
              <button class="btn btn-primary" type="submit" @click="handleSave" :disabled="saving">
                {{ saving ? '保存中...' : '保存' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Feedback Toast -->
        <Transition name="fade">
          <div v-if="saved" class="toast success">简历信息已保存</div>
        </Transition>
        <Transition name="fade">
          <div v-if="errorMsg" class="toast error">{{ errorMsg }}</div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ============ Page wrapper (identical to Settings) ============ */
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

/* ============ Content container (identical to Settings) ============ */
.profile-page {
  position: relative;
  z-index: 1;
  min-height: 100vh;
  padding-bottom: 100px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}

/* ============ Header (identical to Settings) ============ */
.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  max-width: min(1100px, 100%);
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
  backdrop-filter: blur(4px);
}
.back-btn:hover {
  background: rgba(241, 245, 249, 0.8);
  border-color: rgba(148, 163, 184, 0.5);
  color: #1e293b;
  transform: translateX(-2px);
}
.header-content h1 { margin: 0; font-size: 19px; font-weight: 700; color: #0f172a; letter-spacing: -0.01em; }
.subtitle { margin: 4px 0 0; font-size: 14px; color: #64748b; }

/* Completion progress ring */
.header-progress { margin-left: auto; flex-shrink: 0; }
.progress-ring { position: relative; width: 40px; height: 40px; }
.progress-text {
  position: absolute; inset: 0;
  display: flex; align-items: center; justify-content: center;
  font-size: 11px; font-weight: 800; color: #1e40af;
}
.progress-ring circle { transition: stroke-dashoffset 600ms cubic-bezier(0.16, 1, 0.3, 1); }

/* ============ Body ============ */
.profile-body {
  max-width: min(1100px, 100%);
  margin: 0 auto;
  padding: 24px clamp(16px, 4vw, 48px) 0;
}
.loading-text {
  text-align: center; padding: 80px 0;
  color: #64748b; font-size: 15px;
}

/* ============ Grid (two columns like Settings sidebar) ============ */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  align-items: stretch;
}
/* 双列卡片高度拉齐 + 内部 textarea 撑满 */
.form-col:not(.wide) .section-card {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.form-col:not(.wide) .card-body {
  flex: 1;
  display: flex;
  flex-direction: column;
}
.form-col:not(.wide) .form-textarea {
  flex: 1;
  resize: none;
}
.form-col.wide { grid-column: 1 / -1; }

/* ============ Cards (identical to Settings .section-card) ============ */
.section-card {
  background: rgba(255, 255, 255, 0.5);
  border-radius: 16px;
  border: 1px solid rgba(148, 163, 184, 0.2);
  backdrop-filter: blur(6px);
  -webkit-backdrop-filter: blur(6px);
  transition: all 200ms cubic-bezier(0.16, 1, 0.3, 1);
}
.section-card:hover {
  background: rgba(255, 255, 255, 0.62);
  border-color: rgba(148, 163, 184, 0.3);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 22px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.12);
  background: rgba(248, 250, 252, 0.5);
}
.card-icon {
  width: 54px; height: 54px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  padding: 1px;
  box-sizing: border-box;
  background: rgba(148, 163, 184, 0.1);
}
.card-icon img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  min-width: 0;
  min-height: 0;
}
.card-title-group { display: flex; align-items: center; gap: 10px; flex: 1; }
.card-title { font-size: 15px; font-weight: 700; color: #0f172a; }

.configured-badge {
  padding: 3px 10px;
  background: rgba(16, 185, 129, 0.12);
  border: 1px solid rgba(16, 185, 129, 0.2);
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  color: #059669;
}

.readonly-tag {
  padding: 2px 8px;
  background: rgba(245, 158, 11, 0.12);
  border: 1px solid rgba(245, 158, 11, 0.2);
  border-radius: 999px;
  font-size: 11px;
  font-weight: 600;
  color: #b45309;
}

.readonly-card { opacity: 0.85; }
.readonly-card .card-header { background: rgba(255, 251, 235, 0.4); }

.form-textarea.readonly {
  background: rgba(248, 250, 252, 0.5);
  color: #64748b;
  cursor: default;
  resize: none;
  border-style: dashed;
}
.form-textarea.readonly:focus {
  border-color: rgba(148, 163, 184, 0.3);
  box-shadow: none;
  background: rgba(248, 250, 252, 0.5);
}

.card-body { padding: 20px 22px; }

/* ============ Textarea (matching Settings inputs) ============ */
.form-textarea {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 10px;
  font-size: 15px;
  font-family: inherit;
  background: rgba(255, 255, 255, 0.6);
  color: #0f172a;
  transition: all 150ms cubic-bezier(0.16, 1, 0.3, 1);
  box-sizing: border-box;
  backdrop-filter: blur(4px);
  resize: vertical;
  line-height: 1.6;
}
.form-textarea:focus {
  outline: none;
  border-color: #1e40af;
  background: rgba(255, 255, 255, 0.85);
  box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.1);
}
.form-textarea::placeholder { color: #94a3b8; }

/* ============ Footer actions (identical to Settings) ============ */
.settings-footer {
  max-width: min(1100px, 100%);
  margin: 24px auto 0;
  padding: 20px clamp(16px, 4vw, 48px);
  border-top: 1px solid rgba(148, 163, 184, 0.12);
}
.footer-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.footer-right { display: flex; gap: 10px; }

/* ============ Buttons (identical to Settings) ============ */
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 22px;
  border-radius: 10px;
  font-size: 14.5px;
  font-weight: 600;
  cursor: pointer;
  transition: all 150ms cubic-bezier(0.16, 1, 0.3, 1);
  white-space: nowrap;
  border: none;
}
.btn-primary {
  background: linear-gradient(135deg, #1e40af, #1e3a5f);
  color: #fff;
  box-shadow: 0 2px 8px rgba(30, 64, 175, 0.3);
}
.btn-primary:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(30, 64, 175, 0.4);
}
.btn-primary:active:not(:disabled) { transform: translateY(0); }
.btn-primary:disabled { background: #94a3b8; cursor: not-allowed; box-shadow: none; transform: none; }
.btn-secondary {
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.3);
  color: #475569;
}
.btn-secondary:hover {
  background: rgba(248, 250, 252, 0.85);
  border-color: rgba(148, 163, 184, 0.5);
  color: #1e293b;
}

/* ============ Toast (identical to Settings) ============ */
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
  backdrop-filter: blur(10px);
}
.toast.success {
  background: rgba(16, 185, 129, 0.92);
  color: #fff;
  box-shadow: 0 4px 20px rgba(16, 185, 129, 0.3);
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
.fade-leave-to   { opacity: 0; }

/* ============ Responsive ============ */
@media (max-width: 768px) {
  .form-grid { grid-template-columns: 1fr; }
  .form-col.wide { grid-column: 1 / -1; }
  .header-progress { display: none; }
  .profile-header { padding: 16px clamp(16px, 4vw, 48px); }
  .profile-body { padding: 20px clamp(16px, 4vw, 48px) 0; }
  .footer-content { flex-direction: column; align-items: stretch; }
  .footer-right { justify-content: flex-end; }
  .back-btn span { display: none; }
}
</style>
