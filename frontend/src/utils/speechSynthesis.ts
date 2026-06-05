// 语音合成工具
export interface SpeechConfig {
  voice?: string;
  rate?: number;
  pitch?: number;
  volume?: number;
}

export interface SpeechCallbacks {
  onStart?: () => void;
  onEnd?: () => void;
  onError?: (error: Error) => void;
}

class SpeechSynthesizer {
  private utterance: SpeechSynthesisUtterance | null = null;
  private config: SpeechConfig;
  private callbacks: SpeechCallbacks = {};

  constructor(config: SpeechConfig = {}) {
    this.config = {
      voice: config.voice || 'female',
      rate: config.rate ?? 1,
      pitch: config.pitch ?? 1,
      volume: config.volume ?? 1
    };
  }

  speak(text: string): void {
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();

      this.utterance = new SpeechSynthesisUtterance(text);
      
      this.utterance.rate = this.config.rate ?? 1;
      this.utterance.pitch = this.config.pitch ?? 1;
      this.utterance.volume = this.config.volume ?? 1;

      this.selectVoice();

      this.utterance.onstart = () => {
        if (this.callbacks.onStart) {
          this.callbacks.onStart();
        }
      };

      this.utterance.onend = () => {
        if (this.callbacks.onEnd) {
          this.callbacks.onEnd();
        }
      };

      this.utterance.onerror = (event: Event) => {
        const errorEvent = event as SpeechSynthesisErrorEvent;
        if (this.callbacks.onError) {
          this.callbacks.onError(new Error(errorEvent.error || '语音合成错误'));
        }
      };

      window.speechSynthesis.speak(this.utterance);
    } else {
      console.warn('当前浏览器不支持语音合成');
    }
  }

  stop(): void {
    if ('speechSynthesis' in window) {
      window.speechSynthesis.cancel();
    }
  }

  pause(): void {
    if ('speechSynthesis' in window) {
      window.speechSynthesis.pause();
    }
  }

  resume(): void {
    if ('speechSynthesis' in window) {
      window.speechSynthesis.resume();
    }
  }

  isSpeaking(): boolean {
    return 'speechSynthesis' in window && window.speechSynthesis.speaking;
  }

  setConfig(config: SpeechConfig): void {
    this.config = { 
      ...this.config, 
      rate: config.rate ?? this.config.rate,
      pitch: config.pitch ?? this.config.pitch,
      volume: config.volume ?? this.config.volume,
      voice: config.voice ?? this.config.voice
    };
  }

  getConfig(): SpeechConfig {
    return { ...this.config };
  }

  setCallbacks(callbacks: SpeechCallbacks): void {
    this.callbacks = callbacks;
  }

  getVoices(): SpeechSynthesisVoice[] {
    if ('speechSynthesis' in window) {
      return window.speechSynthesis.getVoices();
    }
    return [];
  }

  private selectVoice(): void {
    if (!this.utterance) return;

    const voices = this.getVoices();
    
    if (this.config.voice === 'male') {
      const maleVoice = voices.find(v => 
        v.name.toLowerCase().includes('male') || 
        v.name.toLowerCase().includes('男')
      );
      if (maleVoice) {
        this.utterance.voice = maleVoice;
      }
    } else {
      const femaleVoice = voices.find(v => 
        v.name.toLowerCase().includes('female') || 
        v.name.toLowerCase().includes('女') ||
        v.lang.startsWith('zh')
      );
      if (femaleVoice) {
        this.utterance.voice = femaleVoice;
      }
    }
  }
}

export { SpeechSynthesizer };

let synthesizerInstance: SpeechSynthesizer | null = null;

export function getSpeechSynthesizer(config?: SpeechConfig): SpeechSynthesizer {
  if (!synthesizerInstance) {
    synthesizerInstance = new SpeechSynthesizer(config || {});
  }
  return synthesizerInstance;
}

export function destroySpeechSynthesizer(): void {
  if (synthesizerInstance) {
    synthesizerInstance.stop();
    synthesizerInstance = null;
  }
}
