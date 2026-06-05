// 面试场景类型定义：单一事实来源，便于未来扩展
// 新场景只需在此追加一项并补充对应后端 prompt/provider。

export type InterviewSceneKey = 'hr' | 'technical' | 'pressure';

export interface InterviewSceneMeta {
  key: InterviewSceneKey;
  title: string;
  titleZh: string;
  description: string;
  descriptionZh: string;
  accent: string; // CSS 色值；用 style 绑定，保持可扩展
  icon: string;   // emoji 占位；后续可替换为 SVG/图标库
}

// 场景元数据清单：组件与路由均消费该数组
export const INTERVIEW_SCENES: InterviewSceneMeta[] = [
  {
    key: 'hr',
    title: 'HR Interview',
    titleZh: 'HR 面试',
    description: 'Typical HR scenarios: self-introduction, career planning, conflict resolution, teamwork.',
    descriptionZh: '典型 HR 场景：自我介绍、职业规划、冲突解决、团队协作。',
    accent: '#2563eb',
    icon: '👥'
  },
  {
    key: 'technical',
    title: 'Technical Interview',
    titleZh: '技术面试',
    description: 'Job skills, system design, project details, problem troubleshooting and trade-offs.',
    descriptionZh: '岗位技术栈、系统设计、项目细节、问题定位与权衡。',
    accent: '#0ea5e9',
    icon: '💻'
  },
  {
    key: 'pressure',
    title: 'Pressure Interview',
    titleZh: '压力面试',
    description: 'Simulate high-pressure questioning and uncertainty scenarios, train on-the-spot response and emotional management.',
    descriptionZh: '模拟高压追问与不确定性场景，训练临场反应与情绪管理。',
    accent: '#f97316',
    icon: '🔥'
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
