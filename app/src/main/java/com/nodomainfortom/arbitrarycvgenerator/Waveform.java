package com.nodomainfortom.arbitrarycvgenerator;

import android.content.Context;
import android.graphics.PointF;
import android.media.AudioManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class Waveform {
	//Log Tags
	private static final String CLASS_TAG = "Waveform";
	private static final String INTERPOLATED_WAVE_DATA_TAG = "InterpolatedWaveData";


	//Enumerations
	public enum OutputChannelEnum {left, right}

	public enum ClockModeUnitEnum {
		//ensure that the number and order of enum constants here matches the number and order
		//of strings in array.clock_mode_unit_array
		msec, sec, min;

		public static ClockModeUnitEnum indexToUnit(int i) {
			switch (i) {
				case 0: return ClockModeUnitEnum.msec;
				case 1: return ClockModeUnitEnum.sec;
				case 2: return ClockModeUnitEnum.min;
				default: return null;
			}
		}

		public static int unitToIndex(ClockModeUnitEnum c) {
			switch (c) {
				case msec: return 0;
				case sec: return 1;
				case min: return 2;
				default: return -1;			//-1 is null value
			}
		}
	}

	public enum BpmModeUnitEnum {
		//ensure that the number and order of enum constants here matches the number and order
		//of strings in array.bpm_mode_unit_array
		t32, n32, d32, t16, n16, d16, t8, n8, d8, t4, n4, d4, t2, n2, d2, t1, n1, d1;

		public static BpmModeUnitEnum indexToUnit(int i) {
			switch (i) {
				case 0: return BpmModeUnitEnum.t32;
				case 1: return BpmModeUnitEnum.n32;
				case 2: return BpmModeUnitEnum.d32;
				case 3: return BpmModeUnitEnum.t16;
				case 4: return BpmModeUnitEnum.n16;
				case 5: return BpmModeUnitEnum.d16;
				case 6: return BpmModeUnitEnum.t8;
				case 7: return BpmModeUnitEnum.n8;
				case 8: return BpmModeUnitEnum.d8;
				case 9: return BpmModeUnitEnum.t4;
				case 10: return BpmModeUnitEnum.n4;
				case 11: return BpmModeUnitEnum.d4;
				case 12: return BpmModeUnitEnum.t2;
				case 13: return BpmModeUnitEnum.n2;
				case 14: return BpmModeUnitEnum.d2;
				case 15: return BpmModeUnitEnum.t1;
				case 16: return BpmModeUnitEnum.n1;
				case 17: return BpmModeUnitEnum.d1;
				default: return null;
			}
		}

		public static int unitToIndex(BpmModeUnitEnum b) {
			switch (b) {
				case t32: return 0;
				case n32: return 1;
				case d32: return 2;
				case t16: return 3;
				case n16: return 4;
				case d16: return 5;
				case t8: return 6;
				case n8: return 7;
				case d8: return 8;
				case t4: return 9;
				case n4: return 10;
				case d4: return 11;
				case t2: return 12;
				case n2: return 13;
				case d2: return 14;
				case t1: return 15;
				case n1: return 16;
				case d1: return 17;
				default: return -1;			//-1 is null value
			}
		}
	}


	//Member Variables
	private OutputChannelEnum mOutputChannel;
	private int mPlayheadPosition;
	private boolean mClockMode, mBpmMode, mOneShotMode, mCycleMode;
	private float mClockTime, mBpmTime;
	private float mVMin, mVMax;
	/*note: before I go too far with mVMin and mVMax, I need to figure out the specs for a
	*phone's headphones out and how those specs relate to eurorack cv i/o specs*/
	private List<PointF> mOriginalWaveData;
	private float[] mInterpolatedWaveData;
	private ClockModeUnitEnum mClockUnit;
	private BpmModeUnitEnum mBpmUnit;


	//Constructors
	public Waveform() {
		mPlayheadPosition = 0; //edit this if I need to initialize mPlayheadPosition differently

		mClockMode = false;
		mBpmMode = false;

		mOneShotMode = false;
		mCycleMode = false;

		mClockTime = 1.0f;
		mClockUnit = ClockModeUnitEnum.sec;
		mBpmTime = 1.0f;
		mBpmUnit = BpmModeUnitEnum.n8;

		//mVMin = x;
		//mVMax = y;
		/*note: initialize according to headphone/eurorack specs*/

		mOriginalWaveData = new ArrayList<PointF>();
	}


	//Utility Methods
	public void updateInterpolatedWaveData(Context context) {
		if (mOriginalWaveData == null || mOriginalWaveData.size() == 0) return;

		List<PointF> tempWaveData = new ArrayList<PointF>();
		float tempXMin, tempXMax, tempXRange;
		float tempYMin, tempYMax, tempYRange;

		float activeUnitInSec;
		float cycleTimeInSec;
		float sampleRate;
		int numOfSamples;

		int prevIndex, curIndex;
		float prevData, curData;
		float pulseFreqConstant = 50f;
		float pulseWidthConstant = pulseFreqConstant * .1f;


		for (int i = 0; i < mOriginalWaveData.size(); i++) {
			float x = mOriginalWaveData.get(i).x;
			float y = mOriginalWaveData.get(i).y;
			PointF point = new PointF(x, y);
			tempWaveData.add(point);
		}
		tempXMin = tempWaveData.get(0).x;
		tempXMax = tempWaveData.get(tempWaveData.size() - 1).x;
		tempXRange = tempXMax - tempXMin;

		tempYMin = 1f;
		tempYMax = 0f;

		//search for min/max y-values
		for (PointF point : tempWaveData) {
			tempYMin = point.y < tempYMin ? point.y : tempYMin;
			tempYMax = point.y > tempYMax ? point.y : tempYMax;
		}

		tempYRange = tempYMax - tempYMin;

		//normalize tempWaveData to x-range of 0 to 1 and y-range of -1 to 1
		for (PointF point : tempWaveData) {
			point.x = (point.x - tempXMin) / tempXRange;
			point.y = 2f * (((point.y - tempYMin) / tempYRange) - .5f);
			//point.y = (point.y - tempYMin) / tempYRange;
		}

		//determine number of samples to store in mInterpolatedWaveData
		if (mClockMode) {
			switch (mClockUnit) {
				case msec:
					activeUnitInSec = .001f;
					break;
				case sec:
					activeUnitInSec = 1f;
					break;
				case min:
					activeUnitInSec = 60f;
					break;
				default:
					activeUnitInSec = 1f;
			}
			cycleTimeInSec = mClockTime * activeUnitInSec;

		} else if (mBpmMode) {
			switch (mBpmUnit) {
				case t32:
					activeUnitInSec = 15f / 3f;
					break;
				case n32:
					activeUnitInSec = 7.5f;
					break;
				case d32:
					activeUnitInSec = 7.5f * 1.5f;
					break;
				case t16:
					activeUnitInSec = 30f / 3f;
					break;
				case n16:
					activeUnitInSec = 15f;
					break;
				case d16:
					activeUnitInSec = 15f * 1.5f;
					break;
				case t8:
					activeUnitInSec = 60f / 3f;
					break;
				case n8:
					activeUnitInSec = 30f;
					break;
				case d8:
					activeUnitInSec = 30f * 1.5f;
					break;
				case t4:
					activeUnitInSec = 120f / 3f;
					break;
				case n4:
					activeUnitInSec = 60f;
					break;
				case d4:
					activeUnitInSec = 60f * 1.5f;
					break;
				case t2:
					activeUnitInSec = 240f / 3f;
					break;
				case n2:
					activeUnitInSec = 120f;
					break;
				case d2:
					activeUnitInSec = 120f * 1.5f;
					break;
				case t1:
					activeUnitInSec = 480f / 3f;
					break;
				case n1:
					activeUnitInSec = 240f;
					break;
				case d1:
					activeUnitInSec = 240f * 1.5f;
					break;
				default:
					activeUnitInSec = 30f;
			}
			cycleTimeInSec = activeUnitInSec / mBpmTime;

		} else {
			Log.d(CLASS_TAG + mOutputChannel,
					"Unable to update audio data, no Cycle Mode selected");
			return;
		}

		sampleRate = Float.parseFloat(((AudioManager) context.getSystemService(Context.AUDIO_SERVICE))
				.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
		numOfSamples = (int) (cycleTimeInSec * sampleRate);

		//create mInterpolatedWaveData
		mInterpolatedWaveData = new float[numOfSamples];

		//fill mInterpolatedWaveData, stage 1: put tempWaveData y-values into mInterpolatedWaveData
		for (PointF point : tempWaveData) {
			if (point.x == 1) mInterpolatedWaveData[numOfSamples - 1] = -point.y;
			else mInterpolatedWaveData[(int) (point.x * numOfSamples)] = -point.y;
		}

		//fill mInterpolatedWaveData, stage 2: for audible frequencies, fill in gaps left from stage 1
		if (cycleTimeInSec <= .05f) {
			int gapSize = 0;
			curData = mInterpolatedWaveData[0];
			for (int i = 1; i < mInterpolatedWaveData.length; i++) {
				if (mInterpolatedWaveData[i] == 0) {
					gapSize++;
				} else {
					prevData = curData;
					curData = mInterpolatedWaveData[i];

					for (int j = 1; j <= gapSize; j++) {
						float num = j * (curData - prevData);
						float denom = gapSize + 1;
						float offset = num / denom;
						mInterpolatedWaveData[i - j] =
								curData - offset;
					}

					gapSize = 0;
				}
			}
		}

		//fill mInterpolatedWaveData, stage 2': for sub-audible frequencies, produce
		//audio-frequency pulses along linear interpolation of mTempWaveData y-values
		if (cycleTimeInSec > .05f) {
			int iModDivisor = (int) (numOfSamples / pulseFreqConstant);
			int jLimit = (int) ((pulseWidthConstant * numOfSamples)
					/ (pulseFreqConstant * (pulseFreqConstant + pulseWidthConstant)));

			mInterpolatedWaveData[mInterpolatedWaveData.length - 1] =
					tempWaveData.get(tempWaveData.size() - 1).y;

			prevIndex = 0;
			prevData = mInterpolatedWaveData[0];
			mInterpolatedWaveData[0] = 0;

			curIndex = 1;

			while (curIndex < mInterpolatedWaveData.length) {
				//find curIndex & curData
				while (mInterpolatedWaveData[curIndex] == 0) curIndex++;
				curData = mInterpolatedWaveData[curIndex];
				mInterpolatedWaveData[curIndex] = 0;
				//for every index i between prevIndex and curIndex: if i is the upper bound of a range
				//of indexes [i - ((5*sampleRate*cycleTimeInSec)/(50*55)), i] whose data needs to be
				//non-zero; then for every index i-j in that range, set i-j's data to the appropriate
				//value (assuming it has not already been set).
				//note: i = 0 is special case. to avoid iob exception we must count up from i (not down).
				for (int i = prevIndex; i <= curIndex; i++) {
					if ((i != 0) && (i % iModDivisor == 0)) {
						for (int j = 0; j < jLimit; j++) {
							float num = (i - j - prevIndex) * (curData - prevData);
							float denom = curIndex - prevIndex;
							mInterpolatedWaveData[i - j] = (mInterpolatedWaveData[i - j] == 0) ?
									(curData - (num / denom)) : mInterpolatedWaveData[i - j];
						}
					} else if ((i == 0)) {
						for (int j = 0; j < jLimit; j++) {
							float num = j * (curData - prevData);
							float denom = curIndex - prevIndex;
							mInterpolatedWaveData[j] = (mInterpolatedWaveData[j] == 0) ?
									(curData + (num / denom)) : mInterpolatedWaveData[j];
						}
					}
				}
				//update prevIndex
				prevIndex = curIndex;
				prevData = curData;
				mInterpolatedWaveData[prevIndex] = 0;
				curIndex++;
			}
		}



		//log interpolation complete
		Log.d(CLASS_TAG + mOutputChannel, "Audio data updated");
	}


	//Getters and Setters
	public OutputChannelEnum getOutputChannel() {
		return mOutputChannel;
	}

	public void setOutputChannel(OutputChannelEnum outputChannel) {
		this.mOutputChannel = outputChannel;
	}

	public int getPlayheadPosition() {
		return mPlayheadPosition;
	}

	public void setPlayheadPosition(int playheadPosition) {
		mPlayheadPosition = playheadPosition;
	}

	public boolean isClockMode() {
		return mClockMode;
	}

	public void setClockMode(boolean clockMode) {
		this.mClockMode = clockMode;
	}

	public boolean isBpmMode() {
		return mBpmMode;
	}

	public void setBpmMode(boolean bpmMode) {
		this.mBpmMode = bpmMode;
	}

	public boolean isOneShotMode() {
		return mOneShotMode;
	}

	public void setOneShotMode(boolean oneShotMode) {
		this.mOneShotMode = oneShotMode;
	}

	public boolean isCycleMode() {
		return mCycleMode;
	}

	public void setCycleMode(boolean cycleMode) {
		this.mCycleMode = cycleMode;
	}

	public double getClockTime() {
		return mClockTime;
	}

	public void setClockTime(float clockTime) {
		this.mClockTime = clockTime;
	}

	public double getBpmTime() {
		return mBpmTime;
	}

	public void setBpmTime(float bpmTime) {
		mBpmTime = bpmTime;
	}

	public double getVMin() {
		return mVMin;
	}

	public void setVMin(float VMin) {
		//edit the constants (-1.0 & 1.0) as needed to work with phone's headphone out specs
		if (VMin > -1.0f && VMin < 1.0f) {
			mVMin = VMin;
		} else if (VMin >= 1.0) {
			mVMin = 1.0f;
		} else if (VMin <= -1.0) {
			mVMin = -1.0f;
		}
	}

	public double getVMax() {
		return mVMax;
	}

	public void setVMax(float VMax) {
		//edit the constants (-1.0 & 1.0) as needed to work with phone's headphone out specs
		if (VMax > -1.0f && VMax < 1.0f) {
			mVMax = VMax;
		} else if (VMax >= 1.0f) {
			mVMax = 1.0f;
		} else if (VMax <= -1.0f) {
			mVMax = -1.0f;
		}	}

	public List<PointF> getOriginalWaveData() {
		return mOriginalWaveData;
	}

	public void setOriginalWaveData(List<PointF> pointFList) {
		mOriginalWaveData = pointFList;
	}

	public float[] getInterpolatedWaveData() {
		return mInterpolatedWaveData;
	}

	public void setInterpolatedWaveData(float[] floatList) {
		mInterpolatedWaveData = floatList;
	}

	public ClockModeUnitEnum getClockUnit() {
		return mClockUnit;
	}

	public void setClockUnit(ClockModeUnitEnum clockUnit) {
		mClockUnit = clockUnit;
	}

	public BpmModeUnitEnum getBpmUnit() {
		return mBpmUnit;
	}

	public void setBpmUnit(BpmModeUnitEnum bpmUnit) {
		mBpmUnit = bpmUnit;
	}


}
