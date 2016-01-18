package com.blow.main.sensor;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;

import com.blow.main.ActivityBlow;


/**
 * Created by SiKang on 2016/1/5.
 */
public class AudioSensor extends MySensor {
    private static final String TAG = "AudioSensor";
    private static AudioSensor mMicSensor = null;
    static final int SAMPLE_RATE_IN_HZ = 8000;
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
            AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    AudioRecord mAudioRecord;
    boolean isGetVoiceRun;//是否正在录音
    private Handler mHandler;
    private AudioSensor(Handler handler) {
        super();
        this.mHandler = handler;
        isGetVoiceRun = false;
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
    }

    public static AudioSensor getInstance(Handler handler) {
        if (mMicSensor == null) {
            synchronized (AudioSensor.class) {
                if (mMicSensor == null) {
                    mMicSensor = new AudioSensor(handler);
                }
            }
        }
        return mMicSensor;
    }

    @Override
    public void start() {
        if (isGetVoiceRun) {
            Log.e(TAG, "正在录音");
            return;
        }
        if (mAudioRecord == null) {
            throw new NullPointerException("mAudioRecord初始化失败！");
        }
        isGetVoiceRun = true;
        new Thread(new Runnable() {
            int index = 0;
            @Override
            public void run() {
                mAudioRecord.startRecording();
                short[] buffer = new short[BUFFER_SIZE];
                double volumeSum = 0;
                while (isGetVoiceRun) {
                    //r是实际读取的数据长度，一般而言r会小于buffersize
                    int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
                    long v = 0;
                    // 将 buffer 内容取出，进行平方和运算
                    for (int i = 0; i < buffer.length; i++) {
                        v += buffer[i] * buffer[i];
                    }
                    // 平方和除以数据总长度，得到音量大小。
                    double mean = v / (double) r;
                    double volume = 10 * Math.log10(mean);
//                    if (needVolume > 0) {
                    //采集平均值不太稳定（iPhone非常稳定）这里用定值
                        if (volume > 75) {
                            Log.d(TAG, volume + "");
                            mHandler.sendEmptyMessage(ActivityBlow.BLOW_START);
                        }
//                    }
//                  采集前10次最大最小分贝差 计算吹气标准
//                    if (!gotNeedVolume) {
//                        if (index++ == 0) {
//                            //初始化最大和最小的采集分别
//                            maxVolume = volume;
//                            minVolume = volume;
//                        } else if (volume > maxVolume) {
//                            maxVolume = volume;
//                        } else if (volume < minVolume) {
//                            minVolume = volume;
//                        }
//                        //计算出吹气标准分贝
//                        if (index >= 10) {
//                            double avg = (maxVolume + minVolume) / 2;
//                            needVolume = avg * 2;
//                            Log.d(TAG, needVolume + "");
//                            gotNeedVolume = true;
//                        }
//
//                    }

                    //采集前10次分贝平均值计算吹气标准
//                    if (!gotNeedVolume) {
//                        //采集10前10次录入
//                        if (index >= 10) {
//                            //将吹气判定标准设为平均值的1.5倍
//                            needVolume = (volumeSum / 10) * 1.5;
//                            Log.d(TAG, needVolume + "");
//                            gotNeedVolume = true;
//                        }else{
//                            volumeSum+=volume;
//                            index++;
//                        }
//                    }

                    synchronized (mLock) {
                        try {
                            mLock.wait(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (mAudioRecord != null) {
                    mAudioRecord.stop();

                }
            }
        }).start();
    }

    @Override
    public void shutDown() {
        isGetVoiceRun = false;
    }

    @Override
    public void destory() {
        if (mAudioRecord != null) {
            mAudioRecord.release();
            mAudioRecord = null;
        }
        mMicSensor = null;
    }
}
