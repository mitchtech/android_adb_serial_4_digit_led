package net.mitchtech.adb;

import java.io.IOException;

import kankan.wheel.widget.OnWheelChangedListener;
import kankan.wheel.widget.OnWheelScrollListener;
import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.NumericWheelAdapter;

import net.mitchtech.adb.serial4digitled.R;

import org.microbridge.server.Server;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.Button;

public class SerialLedActivity extends Activity {
	private final String TAG = SerialLedActivity.class.getSimpleName();

	// Microbridge TCP server
	Server mServer = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		initWheel(R.id.pin_0);
		initWheel(R.id.pin_1);
		initWheel(R.id.pin_2);
		initWheel(R.id.pin_3);

		Button btnMix = (Button) findViewById(R.id.btn_random);
		btnMix.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mixWheel(R.id.pin_0);
				mixWheel(R.id.pin_1);
				mixWheel(R.id.pin_2);
				mixWheel(R.id.pin_3);
				sendByteArray();
			}
		});

		// Create new TCP Server
		try {
			mServer = new Server(4567);
			mServer.start();
		} catch (IOException e) {
			Log.e(TAG, "Unable to start TCP server", e);
			System.exit(-1);
		}

		sendByteArray();
	}

	// Wheel scrolled flag
	private boolean mWheelScrolled = false;

	// Wheel scrolled listener
	OnWheelScrollListener mScrolledListener = new OnWheelScrollListener() {
		public void onScrollingStarted(WheelView wheel) {
			mWheelScrolled = true;
		}

		public void onScrollingFinished(WheelView wheel) {
			mWheelScrolled = false;
			sendByteArray();
		}
	};

	// Wheel changed listener
	private OnWheelChangedListener mWheelChangedListener = new OnWheelChangedListener() {
		public void onChanged(WheelView wheel, int oldValue, int newValue) {
			if (!mWheelScrolled) {
				sendByteArray();
			}
		}
	};

	/**
	 * Initializes wheel
	 * 
	 * @param id
	 *            the wheel widget Id
	 */
	private void initWheel(int id) {
		WheelView mWheel = getWheel(id);
		mWheel.setViewAdapter(new NumericWheelAdapter(this, 0, 9));
		mWheel.setCurrentItem((int) (Math.random() * 10));
		mWheel.addChangingListener(mWheelChangedListener);
		mWheel.addScrollingListener(mScrolledListener);
		mWheel.setCyclic(true);
		mWheel.setInterpolator(new AnticipateOvershootInterpolator());
	}

	/**
	 * Returns wheel by Id
	 * 
	 * @param id
	 *            the wheel Id
	 * @return the wheel with passed Id
	 */
	private WheelView getWheel(int id) {
		return (WheelView) findViewById(id);
	}
	
	/**
	 * Send state of digits to Arduino as byte array
	 */
	private void sendByteArray() {
		byte[] msg = new byte[4]; 
		msg[0] = (byte) getWheel(R.id.pin_0).getCurrentItem();
		msg[1] = (byte) getWheel(R.id.pin_1).getCurrentItem();
		msg[2] = (byte) getWheel(R.id.pin_2).getCurrentItem();
		msg[3] = (byte) getWheel(R.id.pin_3).getCurrentItem();
		Log.i(TAG, "byte array: " + msg[0] + "," + msg[1] + "," + msg[2] + "," + msg[3]);
		try {
			mServer.send(msg);
		} catch (IOException e) {
			Log.e(TAG, "problem sending TCP message", e);
		}
	}

	/**
	 * Mixes wheel
	 * 
	 * @param id
	 *            the wheel id
	 */
	private void mixWheel(int id) {
		WheelView mWheel = getWheel(id);
		mWheel.scroll(-50 + (int) (Math.random() * 50), 500);
	}

	/**
	 * Set wheel to position
	 * 
	 * @param id
	 *            the wheel id
	 * @param position
	 *            the value to set
	 */
	private void setWheel(int id, int position) {
		WheelView mWheel = getWheel(id);
		mWheel.setCurrentItem(position);
	}

}
