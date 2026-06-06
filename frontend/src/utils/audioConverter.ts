/**
 * 浏览器端音频格式转换工具。
 * 将 MediaRecorder 输出的 webm/opus 转换为讯飞 ASR 所需的 PCM Int16 16kHz mono。
 */

/**
 * 将 Blob（webm/opus 等浏览器录音格式）转换为 PCM Int16 16kHz 单声道 ArrayBuffer。
 */
export async function convertToPcm(audioBlob: Blob): Promise<ArrayBuffer> {
  // Step 1: 将 Blob 解码为 AudioBuffer（浏览器原生解码，支持 webm/opus）
  const arrayBuffer = await audioBlob.arrayBuffer();
  const audioCtx = new AudioContext();
  const decodedBuffer = await audioCtx.decodeAudioData(arrayBuffer);
  await audioCtx.close();

  // Step 2: 重采样到 16kHz 单声道
  return resampleTo16kMono(decodedBuffer);
}

/**
 * 将 AudioBuffer 重采样为 16kHz 单声道 Int16 PCM。
 */
function resampleTo16kMono(buffer: AudioBuffer): ArrayBuffer {
  const targetSampleRate = 16000;
  const sourceSampleRate = buffer.sampleRate;
  const totalSamples = Math.floor(buffer.duration * targetSampleRate);

  // 创建输出缓冲区（Int16 = 2 字节/样本）
  const output = new ArrayBuffer(totalSamples * 2);
  const outputView = new DataView(output);

  // 混合多声道为单声道（取平均值）
  const channelCount = buffer.numberOfChannels;
  const sourceLength = buffer.length;
  const ratio = sourceSampleRate / targetSampleRate;

  for (let i = 0; i < totalSamples; i++) {
    const srcIndex = i * ratio;
    const srcIndexFloor = Math.floor(srcIndex);
    const srcIndexCeil = Math.min(srcIndexFloor + 1, sourceLength - 1);
    const frac = srcIndex - srcIndexFloor;

    // 多声道混合 + 线性插值
    let sample = 0;
    for (let ch = 0; ch < channelCount; ch++) {
      const chData = buffer.getChannelData(ch);
      const s1 = chData[srcIndexFloor];
      const s2 = chData[srcIndexCeil];
      sample += s1 + (s2 - s1) * frac;
    }
    sample /= channelCount;

    // Clamp to [-1, 1]
    sample = Math.max(-1, Math.min(1, sample));

    // Float32 → Int16
    const int16 = sample < 0 ? sample * 0x8000 : sample * 0x7FFF;
    outputView.setInt16(i * 2, int16, true); // little-endian
  }

  return output;
}

/**
 * 将 PCM ArrayBuffer 转为 Base64 字符串。
 */
export function pcmToBase64(pcm: ArrayBuffer): string {
  const bytes = new Uint8Array(pcm);
  let binary = '';
  for (let i = 0; i < bytes.length; i++) {
    binary += String.fromCharCode(bytes[i]);
  }
  return btoa(binary);
}
