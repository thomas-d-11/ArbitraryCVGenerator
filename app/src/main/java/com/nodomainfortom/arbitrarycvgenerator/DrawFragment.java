package com.nodomainfortom.arbitrarycvgenerator;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ToggleButton;

public class DrawFragment extends Fragment {
	//Log Tags
	private static final String CLASS_TAG = "DrawFragment";
	private static final String TOUCH_EVENT_ERROR = "TouchEventError";
	private static final String CV1_BUTTON_TAG = "CV1Button";
	private static final String CV2_BUTTON_TAG = "CV2Button";



	//UI Declarations
	private WaveDrawerView mCvWaveDrawerView;
	private ToggleButton mCv1Button;
	private ToggleButton mCv2Button;
	private Button mReturnButton;


	//Constructors
	public static DrawFragment newInstance() {
		return new DrawFragment();
	}

	//Lifecycle Methods
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_draw, container, false);

		mCvWaveDrawerView = (WaveDrawerView) v.findViewById(R.id.scope_drawing_layout);
		mCvWaveDrawerView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (getActiveWave() == null) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) noCvSelectedError();
					return true;
				} else {
					mCvWaveDrawerView.setActiveWave(getActiveWave());
					mCvWaveDrawerView.doDrawLogic(event);
					return true;
				}
			}
		});


		//mCv1Button and mCv2Button are logically/functionally interdependent
		mCv1Button = (ToggleButton) v.findViewById(R.id.cv1_button_draw_fragment);
		mCv1Button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mCv2Button.setChecked(false);
					updateActiveWave(Waveform.OutputChannelEnum.left);
					mCvWaveDrawerView.setActiveWave(getActiveWave());
					mCvWaveDrawerView.invalidate();
					Log.d(CV1_BUTTON_TAG, "Toggle On");
				} else {
					Log.d(CV1_BUTTON_TAG, "Toggle Off");
					if (!mCv1Button.isChecked() && !mCv2Button.isChecked()) {
						updateActiveWave(null);
						mCvWaveDrawerView.setActiveWave(getActiveWave());
						mCvWaveDrawerView.invalidate();
					}
				}
			}
		});

		mCv2Button = (ToggleButton) v.findViewById(R.id.cv2_button_draw_fragment);
		mCv2Button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					mCv1Button.setChecked(false);
					updateActiveWave(Waveform.OutputChannelEnum.right);
					mCvWaveDrawerView.setActiveWave(getActiveWave());
					mCvWaveDrawerView.invalidate();
					Log.d(CV2_BUTTON_TAG, "Toggle On");
				} else {
					Log.d(CV2_BUTTON_TAG, "Toggle Off");
					if (!mCv1Button.isChecked() && !mCv2Button.isChecked()) {
						updateActiveWave(null);
						mCvWaveDrawerView.setActiveWave(getActiveWave());
						mCvWaveDrawerView.invalidate();
					}
				}
			}
		});


		mReturnButton = (Button) v.findViewById(R.id.return_button);
		mReturnButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FragmentTransaction trans = getActivity().getSupportFragmentManager()
						.beginTransaction();
				trans.replace(R.id.fragment_container, ControlsFragment.newInstance())
						.addToBackStack(null).commit();
			}
		});


		loadWaveDataToUi(getActiveWave());
		return v;
	}

	@Override
	public void onPause() {
		super.onPause();
	}


	//Utility Methods
	private Waveform getActiveWave() {
		StereoWaveOutViewModel mWaveOutViewModel = ViewModelProviders.of(getActivity())
				.get(StereoWaveOutViewModel.class);
		return mWaveOutViewModel.getActiveWave();
	}

	private void updateActiveWave(Waveform.OutputChannelEnum chan) {
		StereoWaveOutViewModel mWaveOutViewModel = ViewModelProviders.of(getActivity())
				.get(StereoWaveOutViewModel.class);
		mWaveOutViewModel.setActiveChannel(chan);
	}

	private void loadWaveDataToUi(Waveform wave) {
		if (wave == null) {
			mCv1Button.setChecked(false);
			mCv2Button.setChecked(false);
			noCvSelectedError();
			return;
		}

		StereoWaveOutViewModel mWaveOutViewModel = ViewModelProviders.of(getActivity())
				.get(StereoWaveOutViewModel.class);
		if (mWaveOutViewModel.getActiveChannel() == Waveform.OutputChannelEnum.left) {
			mCv1Button.setChecked(true);
		} else if (mWaveOutViewModel.getActiveChannel() == Waveform.OutputChannelEnum.right) {
			mCv2Button.setChecked(true);
		}
	}

	private void noCvSelectedError() {
		Toast tst = Toast.makeText(getActivity(), R.string.no_cv_selected_errmsg,
				Toast.LENGTH_SHORT);
		tst.setGravity(Gravity.CENTER, 0, 0);
		tst.show();
	}


}