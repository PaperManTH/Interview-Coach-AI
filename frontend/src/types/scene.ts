// 面试场景类型定义：单一事实来源，便于未来扩展
// 新场景只需在此追加一项并补充对应后端 prompt/provider。

// 本地 PNG 图标（统一从 /src/assets 加载，避免外部/CDN 资源
import hrIcon from '@/assets/hr_Interview.png';
import techIcon from '@/assets/Technical_Interview.png';
import pressureIcon from '@/assets/tool_icon.png';

export type InterviewSceneKey = 'hr' | 'technical' | 'pressure';

// ===== 动态面试模式类型 =====

/** 动态面试阶段 */
export type InterviewStage = 'warmup' | 'technical' | 'pressure';

/** 面试聚焦方向（用户在首页选择） */
export type InterviewFocus = 'general' | 'resume' | 'technical';

export interface InterviewStageMeta {
  key: InterviewStage;
  label: string;        // 英文标签
  labelZh: string;      // 中文标签
  description: string;  // 英文描述
  descriptionZh: string;// 中文描述
  accent: string;       // CSS 色值
  icon: string;         // 图标
}

/** 动态面试三阶段定义 */
export const INTERVIEW_STAGES: InterviewStageMeta[] = [
  {
    key: 'warmup',
    label: 'Warm-up',
    labelZh: '热身环节',
    description: 'Self-introduction & behavioral questions',
    descriptionZh: '自我介绍与行为问题',
    accent: '#2563eb',
    icon: hrIcon
  },
  {
    key: 'technical',
    label: 'Technical',
    labelZh: '技术深挖',
    description: 'Deep technical questions based on your background',
    descriptionZh: '基于背景的技术深度提问',
    accent: '#0ea5e9',
    icon: techIcon
  },
  {
    key: 'pressure',
    label: 'Challenge',
    labelZh: '压力挑战',
    description: 'High-stakes follow-ups & edge cases',
    descriptionZh: '高压追问与极端场景',
    accent: '#f97316',
    icon: pressureIcon
  }
];

/** 面试焦点选项 */
export const INTERVIEW_FOCUS_OPTIONS: { value: InterviewFocus; label: string; labelZh: string; desc: string }[] = [
  { value: 'general', label: 'General', labelZh: '通用', desc: 'Balanced interview covering all areas' },
  { value: 'resume', label: 'Resume-based', labelZh: '基于简历', desc: 'Questions tailored to your resume' },
  { value: 'technical', label: 'Technical', labelZh: '技术专项', desc: 'Focus on technical depth' }
];

export function getStageMeta(key: string | undefined): InterviewStageMeta | undefined {
  return INTERVIEW_STAGES.find((s) => s.key === key);
}

// ===== 原有场景类型（保留兼容） =====

export interface InterviewSceneMeta {
  key: InterviewSceneKey;
  title: string;           // 英文标题
  titleZh: string;           // 中文标题
  subtitle: string;            // 英文副标题
  subtitleZh: string;          // 中文副标题
  description: string;        // 英文描述
  descriptionZh: string;      // 中文描述
  accent: string;           // CSS 色值；用 style 绑定，保持可扩展
  icon: string;         // 图标资源 URL（来自 /src/assets 的 PNG）
}

// 场景元数据清单：组件与路由均消费该数组
export const INTERVIEW_SCENES: InterviewSceneMeta[] = [
  {
    key: 'hr',
    title: 'HR Interview',
    titleZh: 'HR 面试',
    subtitle: 'Behavioral & Soft Skills',
    subtitleZh: '行为 & 综合素质',
    description: 'Typical HR scenarios: self-introduction, career planning, conflict resolution, and teamwork.',
    descriptionZh: '典型 HR 场景：自我介绍、职业规划、冲突解决、团队协作。',
    accent: '#2563eb',
    icon: hrIcon
  },
  {
    key: 'technical',
    title: 'Technical Interview',
    titleZh: '技术面试',
    subtitle: 'Deep Technical Dive',
    subtitleZh: '技术深度追问',
    description: 'Tech stack, system design, project details, problem diagnosis and trade-offs.',
    descriptionZh: '岗位技术栈、系统设计、项目细节、问题定位与权衡。',
    accent: '#0ea5e9',
    icon: techIcon
  },
  {
    key: 'pressure',
    title: 'Pressure Interview',
    titleZh: '压力面试',
    subtitle: 'High-Stakes Q&A',
    subtitleZh: '高压场景应对',
    description: 'Simulated high-pressure follow-ups and ambiguous scenarios to build on-your-feet thinking.',
    descriptionZh: '模拟高压追问与不确定性场景，训练临场反应与情绪管理。',
    accent: '#f97316',
    icon: pressureIcon
  }
];

// 安全的 key 解析函数，避免非法路由参数
export function isValidSceneKey(value: unknown): value is InterviewSceneKey {
  if (typeof value !== 'string') return false;
  return INTERVIEW_SCENES.some((s) => s.key === value);
}

export function getSceneMeta(key: string | undefined): InterviewSceneMeta | undefined {
  return INTERVIEW_SCENES.find((s) => s.key === key);
}
