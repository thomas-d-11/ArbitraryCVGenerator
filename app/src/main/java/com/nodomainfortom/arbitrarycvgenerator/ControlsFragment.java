package com.nodomainfortom.arbitrarycvgenerator;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ToggleButton;

import static com.nodomainfortom.arbitrarycvgenerator.R.*;

public class ControlsFragment extends Fragment implements AdapterView.OnItemSelectedListener {
	//Log Tags
	private static final String CLASS_TAG = "ControlsFragment";
	private static final String SCOPE_VIEW_BUTTON_TAG = "ScopeViewButton";
	private static final String CV1_BUTTON_TAG = "CV1Button";
	private static final String CV2_BUTTON_TAG = "CV2Button";
	private static final String CLOCK_MODE_BUTTON_TAG = "ClockModeButton";
	private static final String BPM_MODE_BUTTON_TAG = "BPMModeButton";
	private static final String ONE_SHOT_MODE_BUTTON_TAG = "OneShotModeButton";
	private static final String CYCLE_MODE_BUTTON_TAG = "LoopModeButton";
	private static final String TIME_INPUT_TAG = "TimeInput";
	private static final String TIME_UNIT_TAG = "TimeUnit";
	private static final String VOLT_MIN_TAG = "VoltMin";
	private static final String VOLT_MAX_TAG = "VoltMax";
	private static final String PLAY_FROM_START_BUTTON_TAG = "PlayFromStartButton";
	private static final String PLAY_BUTTON_TAG = "PlayButton";
	private static final String PAUSE_BUTTON_TAG = "PauseButton";


	//UI Declarations
	private Button mScopeViewButton;
	private ToggleButton mCv1Button;
	private ToggleButton mCv2Button;
	private ToggleButton mClockModeButton;
	private ToggleButton mBpmModeButton;
	private ToggleButton mOneShotModeButton;
	private ToggleButton mCycleModeButton;
	private EditText mTimeInputEditText;
	private Spinner mTimeUnitSpinner;
	private EditText mVoltMinEditText;
	private EditText mVoltMaxEditText;
	private ToggleButton mPlayFromStartButton;
	private ToggleButton mPlayButton;
	private ToggleButton mPauseButton;


	//Initializers and Constructors
	public static ControlsFragment newInstance() {
		return new ControlsFragment();
	}


	//Lifecycle Methods
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(layout.fragment_controls, container, false);

		//ArrayAdapters for mTimeUnitSpinner
		final ArrayAdapter<CharSequence> mTimeUnitAdapterInit = ArrayAdapter.createFromResource(
				getActivity(), array.unit_init_array, android.R.layout.simple_spinner_item);
		mTimeUnitAdapterInit.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		final ArrayAdapter<CharSequence> mTimeUnitAdapterClock = ArrayAdapter.createFromResource(
				getActivity(), array.clock_mode_unit_array, android.R.layout.simple_spinner_item);
		mTimeUnitAdapterClock.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		final ArrayAdapter<CharSequence> mTimeUnitAdapterBpm = ArrayAdapter.createFromResource(
				getActivity(), array.bpm_mode_unit_array, android.R.layout.simple_spinner_item);
		mTimeUnitAdapterBpm.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);


		//Wiring up UI Elements
		mScopeViewButton = (Button) v.findViewById(id.scope_view_button);
		mScopeViewButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(SCOPE_VIEW_BUTTON_TAG, "selected");

				if (getActiveWave() == null) {
					noCvSelectedError();
					return;
				}

				FragmentTransaction trans = getActivity().getSupportFragmentManager()
						.beginTransaction();
				trans.replace(id.fragment_container, DrawFragment.newInstance())
						.addToBackStack(null).commit();
			}
		});

		//mCv1Button and mCv2Button are logically/functionally interdependent
		mCv1Button = (ToggleButton) v.findViewById(id.cv1_button);
		mCv1Button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mCv2Button.setChecked(false);
					updateActiveWave(Waveform.OutputChannelEnum.left);
					loadWaveDataToUi(getActiveWave());
					Log.d(CV1_BUTTON_TAG, "Toggle On");
				} else {
					if (!mCv2Button.isChecked()) {
						updateActiveWave(null);
						mTimeUnitSpinner.setAdapter(mTimeUnitAdapterInit);
					}
					loadWaveDataToUi(getActiveWave());
					Log.d(CV1_BUTTON_TAG, "Toggle Off");
				}
			}
		});

		mCv2Button = (ToggleButton) v.findViewById(id.cv2_button);
		mCv2Button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mCv1Button.setChecked(false);
					updateActiveWave(Waveform.OutputChannelEnum.right);
					loadWaveDataToUi(getActiveWave());
					Log.d(CV2_BUTTON_TAG, "Toggle On");
				} else {
					if (!mCv1Button.isChecked()) {
						updateActiveWave(null);
						mTimeUnitSpinner.setAdapter(mTimeUnitAdapterInit);
					}
					loadWaveDataToUi(getActiveWave());
					Log.d(CV2_BUTTON_TAG, "Toggle Off");
				}
			}
		});

		//mClockModeButton and mBpmModeButton are logically/functionally interdependent;
		//also, they affect mTimeInputEditText and mTimeUnitSpinner (but not vice versa)
		mClockModeButton = (ToggleButton) v.findViewById(id.clock_button);
		mClockModeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Waveform wave = getActiveWave();
				if (wave == null) {
					noCvSelectedError();
					if (isChecked) mClockModeButton.setChecked(false);
					return;
				}

				if (isChecked) {
					wave.setClockMode(true);
					mBpmModeButton.setChecked(false);
					mTimeUnitSpinner.setAdapter(mTimeUnitAdapterClock);
					mTimeUnitSpinner.setSelection(Waveform.ClockModeUnitEnum.unitToIndex(wave
							.getClockUnit()));
					mTimeInputEditText.setText(String.valueOf(wave.getClockTime()));

					Log.d(TIME_UNIT_TAG, "Clock Units Loaded");

					Log.d(CLOCK_MODE_BUTTON_TAG, "Toggle On");
				} else {
					wave.setClockMode(false);
					if (!mBpmModeButton.isChecked()) {
						mTimeInputEditText.getText().clear();
						mTimeUnitSpinner.setAdapter(mTimeUnitAdapterInit);
					}

					Log.d(CLOCK_MODE_BUTTON_TAG, "Toggle Off");
				}
				}
		});

		mBpmModeButton = (ToggleButton) v.findViewById(id.bpm_button);
		mBpmModeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Waveform wave = getActiveWave();
				if (wave == null) {
					noCvSelectedError();
					if (isChecked) mBpmModeButton.setChecked(false);
					return;
				}

				if (isChecked) {
					Log.d(BPM_MODE_BUTTON_TAG, "Toggle On");

					wave.setBpmMode(true);
					mClockModeButton.setChecked(false);
					mTimeUnitSpinner.setAdapter(mTimeUnitAdapterBpm);
					mTimeUnitSpinner.setSelection(Waveform.BpmModeUnitEnum.unitToIndex(wave.getBpmUnit()));
					mTimeInputEditText.setText(String.valueOf(wave.getBpmTime()));

					Log.d(TIME_UNIT_TAG, "BPM Units Loaded");
				} else {
					wave.setBpmMode(false);
					if (!mClockModeButton.isChecked()) {
						mTimeInputEditText.getText().clear();
						mTimeUnitSpinner.setAdapter(mTimeUnitAdapterInit);
					}

					Log.d(BPM_MODE_BUTTON_TAG, "Toggle Off");
				}
			}
		});

		//mOneShotModeButton and mCycleModeButton are logically/functionally interdependent
		mOneShotModeButton = (ToggleButton) v.findViewById(id.one_shot_button);
		mOneShotModeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Waveform wave = getActiveWave();
				if (wave == null) {
					noCvSelectedError();
					if (isChecked) mOneShotModeButton.setChecked(false);
					return;
				}

				if (isChecked) {
					wave.setOneShotMode(true);
					mCycleModeButton.setChecked(false);

					Log.d(ONE_SHOT_MODE_BUTTON_TAG, "Toggle On");
				} else {
					wave.setOneShotMode(false);

					Log.d(ONE_SHOT_MODE_BUTTON_TAG, "Toggle Off");
				}
			}
		});

		mCycleModeButton = (ToggleButton) v.findViewById(id.cycle_button);
		mCycleModeButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Waveform wave = getActiveWave();
				if (wave == null) {
					noCvSelectedError();
					if (isChecked) mCycleModeButton.setChecked(false);
					return;
				}

				if (isChecked) {
					wave.setCycleMode(true);
					mOneShotModeButton.setChecked(false);

					Log.d(CYCLE_MODE_BUTTON_TAG, "Toggle On");
				} else {
					wave.setCycleMode(false);

					Log.d(CYCLE_MODE_BUTTON_TAG, "Toggle Off");
				}
			}
		});

		mTimeInputEditText = (EditText) v.findViewById(id.time_edittext);
		mTimeInputEditText.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mTimeInputEditText.getText().clear();
				return false;
			}
		});
		mTimeInputEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Waveform wave = getActiveWave();
				if (wave == null) return;

				if (mClockModeButton.isChecked()) {
					try {
						wave.setClockTime(Float.valueOf(s.toString()));
					} catch (NumberFormatException nfe) {
						Log.e(TIME_INPUT_TAG, "NumberFormatException");
					}
				}

				if (mBpmModeButton.isChecked()) {
					try {
						wave.setBpmTime(Float.valueOf(s.toString()));
					} catch (NumberFormatException nfe) {
						Log.e(TIME_INPUT_TAG, "NumberFormatException");
					}
				}

				Log.d(TIME_INPUT_TAG, "updated");
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		mTimeUnitSpinner = (Spinner) v.findViewById(id.time_spinner);
		mTimeUnitSpinner.setAdapter(mTimeUnitAdapterInit);
		mTimeUnitSpinner.setOnItemSelectedListener(this);


		mVoltMinEditText = (EditText) v.findViewById(id.voltage_min_edittext);
		mVoltMinEditText.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mVoltMinEditText.getText().clear();
				return false;
			}
		});
		mVoltMinEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Waveform wave = getActiveWave();
				if (wave == null) return;

				try {
					wave.setVMin(Float.valueOf(s.toString()));
				} catch (NumberFormatException nfe) {
					Log.e(VOLT_MIN_TAG, "NumberFormatException");
				}

				Log.d(VOLT_MIN_TAG, "updated");
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		mVoltMaxEditText = (EditText) v.findViewById(id.voltage_max_edittext);
		mVoltMaxEditText.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mVoltMaxEditText.getText().clear();
				return false;
			}
		});
		mVoltMaxEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				Waveform wave = getActiveWave();
				if (wave == null) return;

				try {
					wave.setVMax(Float.valueOf(s.toString()));
				} catch (NumberFormatException nfe) {
					Log.e(VOLT_MAX_TAG, "NumberFormatException");
				}

				Log.d(VOLT_MAX_TAG, "updated");
			}
			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		//mPlayFromStartButton, mPlayButton, and mPauseButton are logically/functionally interdependent
		mPlayFromStartButton = (ToggleButton) v.findViewById(id.play_from_start_button);
		mPlayFromStartButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Waveform wave = getActiveWave();
				if (wave == null) {
					noCvSelectedError();
					if (isChecked) mPlayFromStartButton.setChecked(false);
					if (mPlayButton.isChecked()) mPlayButton.setChecked(false);
					if (mPauseButton.isChecked()) mPauseButton.setChecked(false);
					return;
				}

				if (isChecked) {
					if (mPlayButton.isChecked()) mPlayButton.setChecked(false);
					if (mPauseButton.isChecked()) mPauseButton.setChecked(false);
					StereoWaveOutViewModel mWaveOutViewModel = ViewModelProviders.of(getActivity())
							.get(StereoWaveOutViewModel.class);
					mWaveOutViewModel.updateTrackForWave(wave, getContext());
					playWaveOnTrackFromStart(wave, getActiveTrack());
				} else {
					if (!mPlayButton.isChecked() && !mPauseButton.isChecked())
						mPauseButton.setChecked(true);
				}
			}
		});

		mPlayButton = (ToggleButton) v.findViewById(id.play_button);
		mPlayButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@RequiresApi(api = Build.VERSION_CODES.M)
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Waveform wave = getActiveWave();
				if (wave == null) {
					noCvSelectedError();
					if (isChecked) mPlayButton.setChecked(false);
					if (mPlayFromStartButton.isChecked()) mPlayFromStartButton.setChecked(false);
					if (mPauseButton.isChecked()) mPauseButton.setChecked(false);
					return;
				}

				if (isChecked) {
					if (mPlayFromStartButton.isChecked()) mPlayFromStartButton.setChecked(false);
					if (mPauseButton.isChecked()) mPauseButton.setChecked(false);
					StereoWaveOutViewModel mWaveOutViewModel = ViewModelProviders.of(getActivity())
							.get(StereoWaveOutViewModel.class);
					mWaveOutViewModel.updateTrackForWave(wave, getContext());
					playWaveOnTrack(wave, getActiveTrack());
				} else {
					if (!mPlayFromStartButton.isChecked() && !mPauseButton.isChecked())
						mPauseButton.setChecked(true);
				}
			}
		});

		mPauseButton = (ToggleButton) v.findViewById(id.pause_button);
		mPauseButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Waveform wave = getActiveWave();
				if (wave == null) {
					noCvSelectedError();
					if (isChecked) mPauseButton.setChecked(false);
					if (mPlayFromStartButton.isChecked()) mPlayFromStartButton.setChecked(false);
					if (mPlayButton.isChecked()) mPlayButton.setChecked(false);
					return;
				}

				if (isChecked) {
					if (mPlayFromStartButton.isChecked()) mPlayFromStartButton.setChecked(false);
					if (mPlayButton.isChecked()) mPlayButton.setChecked(false);
					pauseTrack(getActiveTrack());
				} else {
					if (!mPlayFromStartButton.isChecked() && !mPlayButton.isChecked())
						stopTrack(getActiveTrack());
				}
			}
		});

		loadWaveDataToUi(getActiveWave());
		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
	}


	//Transport Control Methods
	private void playWaveOnTrackFromStart(Waveform wave, AudioTrack track) {
		if (track == null) return;

		StereoWaveOutViewModel mWaveOutViewModel = ViewModelProviders.of(getActivity())
				.get(StereoWaveOutViewModel.class);

		if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) track.pause();
		if (track.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) track.flush();

		if (wave.isCycleMode()) track.setLoopPoints(0,
				wave.getInterpolatedWaveData().length, -1);
		else if (wave.isOneShotMode()) track.setLoopPoints(0,
				wave.getInterpolatedWaveData().length, 0);

		wave.updateInterpolatedWaveData(getContext());
		track.write(wave.getInterpolatedWaveData(), 0,
				wave.getInterpolatedWaveData().length, AudioTrack.WRITE_BLOCKING);
		track.play();
	}

	@RequiresApi(api = Build.VERSION_CODES.M)
	private void playWaveOnTrack(Waveform wave, AudioTrack track) {
		if (track == null) return;

		if (track.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
			return;
		} else {
			if (wave.isCycleMode()) track.setLoopPoints(0,
					wave.getInterpolatedWaveData().length, -1);
			else if (wave.isOneShotMode()) track.setLoopPoints(0,
					wave.getInterpolatedWaveData().length, 0);

			wave.updateInterpolatedWaveData(getContext());
			if (track.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
				track.play();
			} else if (track.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) {
				track.write(wave.getInterpolatedWaveData(), 0,
						wave.getInterpolatedWaveData().length, AudioTrack.WRITE_BLOCKING);
				track.play();
			}

			Log.d(CLASS_TAG, String.valueOf(track.getRoutedDevice()));
		}
	}

	private void pauseTrack(AudioTrack track) {
		if (track == null) return;
		if (track.getPlayState() != AudioTrack.PLAYSTATE_PAUSED) track.pause();
	}

	private void stopTrack(AudioTrack track) {
		if (track == null) return;
		track.stop();
	}


	//mTimeUnitSpinner's OnItemSelectedListener Implementation Methods
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		//See whether mClockModeButton or mBpmModeButton is checked as a stand in for seeing
		//whether mTimeUnitSpinner has array.clock_mode_unit_array or array.bpm_mode_unit_array loaded
		Waveform wave = getActiveWave();

		if (wave == null) {
			noCvSelectedError();
			return;
		}

		if (mClockModeButton.isChecked()) {
			wave.setClockUnit(Waveform.ClockModeUnitEnum.indexToUnit(position));
			Log.d(TIME_UNIT_TAG, "ClockMode set");
		} else if (mBpmModeButton.isChecked()) {
			wave.setBpmUnit(Waveform.BpmModeUnitEnum.indexToUnit(position));
			Log.d(TIME_UNIT_TAG, "BPMMode set");
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}


	//Utility Methods
	private AudioTrack getActiveTrack() {
		StereoWaveOutViewModel mWaveOutViewModel = ViewModelProviders.of(getActivity())
				.get(StereoWaveOutViewModel.class);
		return mWaveOutViewModel.getActiveTrack();
	}

	private Waveform getActiveWave() {
		StereoWaveOutViewModel mWaveOutViewModel = ViewModelProviders.of(getActivity())
				.get(StereoWaveOutViewModel.class);
		return mWaveOutViewModel.getActiveWave();
	}

	private void loadWaveDataToUi(Waveform wave) {
		if (wave == null) {
			mCv1Button.setChecked(false);
			mCv2Button.setChecked(false);

			mClockModeButton.setChecked(false);
			mBpmModeButton.setChecked(false);

			mOneShotModeButton.setChecked(false);
			mCycleModeButton.setChecked(false);

			mTimeInputEditText.getText().clear();
			//No update for mTimeUnitSpinner because spinner adapters
			//are local to onCreateView() method

			mVoltMinEditText.getText().clear();
			mVoltMaxEditText.getText().clear();

			mPlayFromStartButton.setChecked(false);
			mPlayButton.setChecked(false);
			mPauseButton.setChecked(false);

			noCvSelectedError();
			return;
		}

		StereoWaveOutViewModel mWaveOutViewModel = ViewModelProviders.of(getActivity())
				.get(StereoWaveOutViewModel.class);
		if (mWaveOutViewModel.getActiveChannel() == Waveform.OutputChannelEnum.left) {
			mCv1Button.setChecked(true);
			mCv2Button.setChecked(false);
			if (mWaveOutViewModel.getTrackL() == null)
				Log.d(CLASS_TAG, "AudioTracks not initialized");
			else if (mWaveOutViewModel.getTrackL().getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
				mPlayButton.setChecked(true);
			else if (mWaveOutViewModel.getTrackL().getPlayState() == AudioTrack.PLAYSTATE_PAUSED)
				mPauseButton.setChecked(true);
		} else if (mWaveOutViewModel.getActiveChannel() == Waveform.OutputChannelEnum.right) {
			mCv2Button.setChecked(true);
			mCv1Button.setChecked(false);
			if (mWaveOutViewModel.getTrackR() == null)
				Log.d(CLASS_TAG, "AudioTracks not initialized");
			else if (mWaveOutViewModel.getTrackR().getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
				mPlayButton.setChecked(true);
			else if (mWaveOutViewModel.getTrackR().getPlayState() == AudioTrack.PLAYSTATE_PAUSED)
				mPauseButton.setChecked(true);
		}

		mOneShotModeButton.setChecked(wave.isOneShotMode());
		mCycleModeButton.setChecked(wave.isCycleMode());

		mClockModeButton.setChecked(wave.isClockMode());
		mBpmModeButton.setChecked(wave.isBpmMode());

		if (mClockModeButton.isChecked()) {
			mTimeInputEditText.setText(String.valueOf(wave.getClockTime()));
			mTimeUnitSpinner.setSelection(Waveform.ClockModeUnitEnum.unitToIndex(wave.getClockUnit()));
		} else if (mBpmModeButton.isChecked()) {
			mTimeInputEditText.setText(String.valueOf(wave.getBpmTime()));
			mTimeUnitSpinner.setSelection(Waveform.BpmModeUnitEnum.unitToIndex(wave.getBpmUnit()));
		} else {
			mTimeInputEditText.getText().clear();
		}

		mVoltMinEditText.setText(String.valueOf(wave.getVMin()));
		mVoltMaxEditText.setText(String.valueOf(wave.getVMax()));
	}

	private void noCvSelectedError() {
		Toast tst = Toast.makeText(getActivity(), R.string.no_cv_selected_errmsg,
				Toast.LENGTH_SHORT);
		tst.setGravity(Gravity.TOP, -140, 200);
		tst.show();
	}

	private void updateActiveWave(Waveform.OutputChannelEnum chan) {
		StereoWaveOutViewModel mWaveOutViewModel = ViewModelProviders.of(getActivity())
				.get(StereoWaveOutViewModel.class);
		mWaveOutViewModel.setActiveChannel(chan);
	}

	private void updateTrackForWave (Waveform wave, Context context) {
		StereoWaveOutViewModel mWaveOutViewModel = ViewModelProviders.of(getActivity())
				.get(StereoWaveOutViewModel.class);
		mWaveOutViewModel.updateTrackForWave(wave, context);
	}

}
