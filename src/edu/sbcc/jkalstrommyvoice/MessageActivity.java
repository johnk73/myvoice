package edu.sbcc.jkalstrommyvoice;

import java.io.*;

import android.app.*;
import android.media.*;
import android.os.*;
import android.view.*;
import android.widget.*;

/**
 * Displays message details When the user presses button, plays audio message
 */
public class MessageActivity extends Activity {
	// GUI objects
	private TextView errorMessageLabel;
	private ProgressBar loadingProgressBar;
	private Button playButton;

	// Message ID
	private int id;

	// error message generated by asynctask -- copied to UI in postExecute
	private String errorMessage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message);
		errorMessageLabel = (TextView) findViewById(R.id.errorMessageMessage);
		loadingProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBarMessage);
		TextView callerIDLabel = (TextView) findViewById(R.id.callerIDLabel);
		TextView dateLabel = (TextView) findViewById(R.id.dateLabel);
		TextView durationLabel = (TextView) findViewById(R.id.durationLabel);
		TextView messageLabel = (TextView) findViewById(R.id.messageLabel);
		playButton = (Button) findViewById(R.id.playButton);

		// Message passed from login
		Message message = (Message) getIntent().getSerializableExtra("MESSAGE");
		if (message == null) {
			errorMessageLabel.setText("Empty message");
			return;
		}
		callerIDLabel.setText(message.callerID);
		dateLabel.setText(message.date);
		durationLabel.setText(message.duration);
		messageLabel.setText(message.speechToText);
		id = message.id;

		/*
		 * AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); float volume = (float)
		 * audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) / (float)
		 * audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC); mediaPlayer.setVolume(volume, volume);
		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_message, menu);
		return true;
	}

	// User presses PLAY
	public void onPlayButtonClick(View v) {
		// Reveal progress bar to entertain user and indicate play state
		loadingProgressBar.setVisibility(View.VISIBLE);
		playButton.setEnabled(false);

		// 4) PLAY MESSAGE AUDIO
		/*
		 * TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		 * telephonyManager.listen(new SomaRadioServicePhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
		 */
		// https://www.evoice.com/evoice-api/accountData/getVoiceMail?folderName=Voice%20Inbox&messageID=nnn
		String url = getResources().getString(R.string.base_url) + getResources().getString(R.string.message_url);
		url = url.replaceAll("IDENTITY", String.valueOf(id));
		new PlayAsyncTask().execute(new String[] { url });
	}

	/**
	 * Send login request to eVoice HTTP server EvoiceSession retains the authentication cookies for future requests
	 */
	class PlayAsyncTask extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... args) {
			// Set up HTTP connection
			EvoiceSession session = EvoiceSession.getInstance(MessageActivity.this);
			InputStream in = session.sendRequestRaw(args[0]);

			if (in == null) {
				errorMessage = session.getErrorMessage();
				return false;
			}
			byte[] buffer = new byte[512];

			// Set up AudioTrack
			int minBufferSize = AudioTrack.getMinBufferSize(8000, AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT);
			AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL, 8000, AudioFormat.CHANNEL_OUT_MONO,
					AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);
			audioTrack.play();
			// Discard first 44 bytes (.wav header)
			int lenRead;
			try {
				in.read(buffer, 0, 44);
				int i = 0;
				while ((lenRead = in.read(buffer, 0, buffer.length)) != -1) {
					System.out.println("write" + i++);
					audioTrack.write(buffer, 0, lenRead);
				}
			} catch (IOException e) {
				errorMessage = "Invalid sound file: " + e.getMessage();
			}
			audioTrack.release();

			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			playButton.setEnabled(true);
			loadingProgressBar.setVisibility(View.INVISIBLE);

			// Display error message, if any
			if (errorMessage != null) {
				errorMessageLabel.setText(errorMessage);
			}
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

	}

	/*
	 * class SomaRadioServicePhoneStateListener extends PhoneStateListener {
	 * 
	 * @Override public void onCallStateChanged(int state, String incomingNumber) { try {
	 * super.onCallStateChanged(state, incomingNumber);
	 * 
	 * switch (state) { case TelephonyManager.CALL_STATE_IDLE: // Try to determine if paused if (mediaPlayer != null &&
	 * mediaPlayer.getCurrentPosition() != 0 && !mediaPlayer.isPlaying()) mediaPlayer.start(); break;
	 * 
	 * case TelephonyManager.CALL_STATE_RINGING: if (mediaPlayer != null && mediaPlayer.isPlaying())
	 * mediaPlayer.pause(); break;
	 * 
	 * case TelephonyManager.CALL_STATE_OFFHOOK: break;
	 * 
	 * } } catch (Throwable t) { Log.e("SomaRadioServicePhoneStateListener.onCallStateChanged", t.toString()); } }
	 * 
	 * }
	 */
}
