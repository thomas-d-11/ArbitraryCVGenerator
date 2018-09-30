package com.nodomainfortom.arbitrarycvgenerator;

import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

public class StereoWaveOutViewModel extends ViewModel {
	//Member Variables
	private Waveform mWaveL, mWaveR;
	private Waveform.OutputChannelEnum mActiveChannel;
	private AudioTrack mTrackL, mTrackR;


	//Constructors
	public StereoWaveOutViewModel() {
		mWaveL = new Waveform();
		mWaveL.setOutputChannel(Waveform.OutputChannelEnum.left);

		mWaveR = new Waveform();
		mWaveR.setOutputChannel(Waveform.OutputChannelEnum.right);
	}


	//Utility Methods
	public AudioTrack getActiveTrack() {
		if (mActiveChannel == Waveform.OutputChannelEnum.left) {
			return mTrackL;
		} else if (mActiveChannel == Waveform.OutputChannelEnum.right) {
			return mTrackR;
		} else return null;
	}

	public Waveform getActiveWave() {
		if (mActiveChannel == Waveform.OutputChannelEnum.left) {
			return mWaveL;
		} else if (mActiveChannel == Waveform.OutputChannelEnum.right) {
			return mWaveR;
		} else return null;
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	public void updateTrackForWave(Waveform wave, Context context) {
		if (wave.getInterpolatedWaveData() == null) return;

		int sampleRate = Integer.parseInt(
				((AudioManager) context.getSystemService(Context.AUDIO_SERVICE))
						.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));

		if (wave.getOutputChannel() == Waveform.OutputChannelEnum.left) {
			if (mTrackL != null) mTrackL.release();
			mTrackL = new AudioTrack(AudioManager.USE_DEFAULT_STREAM_TYPE, sampleRate,
					AudioFormat.CHANNEL_OUT_FRONT_LEFT, AudioFormat.ENCODING_PCM_FLOAT,
					Float.BYTES * wave.getInterpolatedWaveData().length,
					AudioTrack.MODE_STATIC);
			mTrackL.setVolume(AudioTrack.getMaxVolume());
		} else if (wave.getOutputChannel() == Waveform.OutputChannelEnum.right) {
			if (mTrackR != null) mTrackR.release();
			mTrackR = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
					AudioFormat.CHANNEL_OUT_FRONT_RIGHT, AudioFormat.ENCODING_PCM_FLOAT,
					Float.BYTES * wave.getInterpolatedWaveData().length,
					AudioTrack.MODE_STATIC);
			mTrackR.setVolume(AudioTrack.getMaxVolume());
		}
	}


	//Getters and Setters
	public Waveform getWaveL() {
		return mWaveL;
	}
	public void setWaveL(Waveform wave) {
		mWaveL = wave;
	}

	public Waveform getWaveR() {
		return mWaveR;
	}
	public void setWaveR(Waveform wave) {
		mWaveR = wave;
	}

	public Waveform.OutputChannelEnum getActiveChannel() {
		return mActiveChannel;
	}
	public void setActiveChannel(Waveform.OutputChannelEnum out) {
		mActiveChannel = out;
	}

	public AudioTrack getTrackL() {
		return mTrackL;
	}
	public void setTrackL(AudioTrack trackL) {
		mTrackL = trackL;
	}

	public AudioTrack getTrackR() {
		return mTrackR;
	}
	public void setTrackR(AudioTrack trackR) {
		mTrackR = trackR;
	}
}
