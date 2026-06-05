// 音频录制工具
export interface AudioRecorderConfig {
  sampleRate?: number;
  bitRate?: number;
  onData?: (data: Blob) => void;
  onError?: (error: Error) => void;
}

export interface RecordingState {
  isRecording: boolean;
  duration: number;
}

class AudioRecorder {
  private mediaRecorder: MediaRecorder | null = null;
  private audioChunks: Blob[] = [];
  private stream: MediaStream | null = null;
  private startTime: number = 0;
  private durationInterval: number | null = null;
  private config: AudioRecorderConfig;
  private onDurationChange?: (duration: number) => void;

  constructor(config: AudioRecorderConfig = {}) {
    this.config = {
      sampleRate: config.sampleRate || 16000,
      bitRate: config.bitRate || 128000,
      onData: config.onData,
      onError: config.onError
    };
  }

  async start(onDurationChange?: (duration: number) => void): Promise<void> {
    if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
      return;
    }

    try {
      this.stream = await navigator.mediaDevices.getUserMedia({ 
        audio: {
          sampleRate: this.config.sampleRate,
          channelCount: 1,
          echoCancellation: true,
          noiseSuppression: true
        }
      });

      this.mediaRecorder = new MediaRecorder(this.stream, {
        mimeType: 'audio/webm;codecs=opus'
      });

      this.audioChunks = [];
      this.startTime = Date.now();
      this.onDurationChange = onDurationChange;

      this.mediaRecorder.ondataavailable = (event: BlobEvent) => {
        if (event.data.size > 0) {
          this.audioChunks.push(event.data);
          if (this.config.onData) {
            this.config.onData(event.data);
          }
        }
      };

      this.mediaRecorder.onstop = () => {
        this.stopDurationInterval();
      };

      this.mediaRecorder.onerror = (event: Event) => {
        const errorEvent = event as ErrorEvent;
        if (this.config.onError) {
          this.config.onError(new Error(errorEvent.message || '录音错误'));
        }
        this.stop();
      };

      this.mediaRecorder.start(100);
      this.startDurationInterval();

      console.log('[AudioRecorder] 开始录音');
    } catch (error) {
      if (this.config.onError) {
        this.config.onError(error as Error);
      }
      throw error;
    }
  }

  stop(): Blob | null {
    if (!this.mediaRecorder || this.mediaRecorder.state === 'inactive') {
      return null;
    }

    this.mediaRecorder.stop();
    
    if (this.stream) {
      this.stream.getTracks().forEach(track => track.stop());
      this.stream = null;
    }

    console.log('[AudioRecorder] 停止录音');

    if (this.audioChunks.length > 0) {
      return new Blob(this.audioChunks, { type: 'audio/webm' });
    }

    return null;
  }

  pause(): void {
    if (this.mediaRecorder && this.mediaRecorder.state === 'recording') {
      this.mediaRecorder.pause();
      this.stopDurationInterval();
      console.log('[AudioRecorder] 暂停录音');
    }
  }

  resume(): void {
    if (this.mediaRecorder && this.mediaRecorder.state === 'paused') {
      this.mediaRecorder.resume();
      this.startDurationInterval();
      console.log('[AudioRecorder] 恢复录音');
    }
  }

  isRecording(): boolean {
    return this.mediaRecorder?.state === 'recording';
  }

  isPaused(): boolean {
    return this.mediaRecorder?.state === 'paused';
  }

  getDuration(): number {
    if (!this.startTime || this.mediaRecorder?.state === 'inactive') {
      return 0;
    }
    return Math.floor((Date.now() - this.startTime) / 1000);
  }

  private startDurationInterval(): void {
    this.stopDurationInterval();
    this.durationInterval = window.setInterval(() => {
      if (this.onDurationChange && this.mediaRecorder?.state === 'recording') {
        this.onDurationChange(this.getDuration());
      }
    }, 1000);
  }

  private stopDurationInterval(): void {
    if (this.durationInterval) {
      clearInterval(this.durationInterval);
      this.durationInterval = null;
    }
  }

  destroy(): void {
    this.stop();
    this.audioChunks = [];
    this.mediaRecorder = null;
    this.stream = null;
  }
}

export { AudioRecorder };

let recorderInstance: AudioRecorder | null = null;

export function getAudioRecorder(config?: AudioRecorderConfig): AudioRecorder {
  if (!recorderInstance) {
    recorderInstance = new AudioRecorder(config || {});
  }
  return recorderInstance;
}

export function destroyAudioRecorder(): void {
  if (recorderInstance) {
    recorderInstance.destroy();
    recorderInstance = null;
  }
}
