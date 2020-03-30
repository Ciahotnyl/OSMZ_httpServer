package com.example.httpserver2;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


public class HttpServerActivity extends Activity implements OnClickListener{

	private SocketServer s;
	private static final int READ_EXTERNAL_STORAGE = 1;
	private Camera mCamera;
	private CameraPreview mPreview;
	static TextView tv;
	static byte[] obrazek;			// Obrázek pro Snapshot/Stream
	static TextView allView;
	private int TransferredCount = 0;
	int permits;
	Timer t;
	TimerTask timerTask;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_http_server);

		tv = (TextView) findViewById(R.id.textView);
		tv.setMovementMethod(new ScrollingMovementMethod());

		Button btn1 = (Button)findViewById(R.id.button1);
		Button btn2 = (Button)findViewById(R.id.button2);

		btn1.setOnClickListener(this);
		btn2.setOnClickListener(this);

		allView = (TextView) findViewById(R.id.textView2);
		allView.setText("\n Total: "+TransferredCount);

		//---------------------------CAMERA-----------------------------------
		mCamera = getCameraInstance();
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
		/*
		Button captureButton = (Button) findViewById(R.id.button_capture);

		captureButton.setOnClickListener(
				new View.OnClickListener(){
					@Override
					public void onClick(View v) {
						t = new Timer();

						timerTask= new TimerTask() {
							@Override
							public void run() {

								mCamera.startPreview();
								mCamera.takePicture(null, null, mPicture);

							}
						};
						t.scheduleAtFixedRate(timerTask, 0, 3000);
					}
				}
		);

		Button stopButton = (Button) findViewById(R.id.button_stop);
		stopButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						t.cancel();
					}
				}
		);
		*/

		// STREAM
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(cameraTakingPictures, 0, 3000, TimeUnit.MILLISECONDS);
	}
	// STREAM
	Runnable cameraTakingPictures = new Runnable() {
		public void run() {
			mCamera.startPreview();
			mCamera.takePicture(null, null, mPicture);
		}
	};

	@Override
	public void onClick(View v) {
		EditText tv2 = findViewById(R.id.clientsNumber);
		permits = Integer.parseInt(tv2.getText().toString());

		if (v.getId() == R.id.button1) {

			int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

			Log.d("SRV", "onClick: "+ PackageManager.PERMISSION_GRANTED );
			if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
				ActivityCompat.requestPermissions(
						this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_STORAGE);
			} else {
				s = new SocketServer(myHandler, permits);
				s.start();
			}
		}
		if (v.getId() == R.id.button2) {
			s.close();
			try {
				s.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public Handler myHandler = new Handler(Looper.getMainLooper()){
		@Override
		public void handleMessage(Message msg) {
			String requestMessage = msg.getData().getString("list");
			if(requestMessage != null){
				tv.append(requestMessage + "\n \n");

				String pom = requestMessage.substring(requestMessage.lastIndexOf(":")+ 1);
				String CountString = pom.substring(1);
				boolean numeric = true;
				try {
					Integer.parseInt(CountString);
				} catch(NumberFormatException e) {
					numeric = false;
				}

				if(numeric){
					int addingSize = Integer.parseInt(CountString);
					TransferredCount += addingSize;
					allView.setText("\n Total: " + TransferredCount);
				}
			}
		}
	};

	public static Camera getCameraInstance(){
		Camera c = null;
		try {
			c = Camera.open();
		}
		catch (Exception e){

		}
		return c;
	}

	private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			if(data == null)
			{
				return;
			}
			obrazek = Arrays.copyOf(data, data.length);
		}
	};

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {

			case READ_EXTERNAL_STORAGE:
				if ((grantResults.length > 0) && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
					s = new SocketServer(myHandler, permits);
					s.start();
				}
				break;

			default:
				break;
		}
	}
}