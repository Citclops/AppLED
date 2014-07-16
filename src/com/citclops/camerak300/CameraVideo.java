package com.citclops.camerak300;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.citclops.cameralibrary.CameraConstants.ANTIBANDING;
import com.citclops.cameralibrary.CameraConstants.COLOR_EFFECT;
import com.citclops.cameralibrary.CameraConstants.FLASH_MODE;
import com.citclops.cameralibrary.CameraConstants.FOCUS_MODE;
import com.citclops.cameralibrary.CameraConstants.IMAGEFORMAT;
import com.citclops.cameralibrary.CameraConstants.SCENE;
import com.citclops.cameralibrary.CameraConstants.WHITE_BALANCE;
import com.citclops.cameralibrary.CitclopsCameraLibrary;
import com.citclops.util.ExternalStorageUtil;
import com.citclops.util.LocationUtils;
import com.citclops.util.MessageDialog;
import com.citclops.util.MusicIntentReceiver;
import com.citclops.util.MusicIntentReceiver.HEADESET_STATE;
import com.citclops.widgets.NumericKeyPadWidget;

public class CameraVideo extends Activity {
			
	final int stepFlashSize = 50;									// Step del seekbar del Flash
	final int delayVideoMillisecondsInternalLed = 1000;				// Milliseconds before and after capture video 
	final int delayVideoMillisExternalLedInit = 1000;				// Milliseconds before and before capture video external led 
	final int delayVideoMillisExternalLedSound = 2000;				// Milliseconds before and after capture video external led
	final int delayVideoMillisExternalLedEnd = 2000;				// Milliseconds before and after capture video external led
	private final String FolderVideoName = "AppLED";				// Video Folder
	Context _this;													// Auto reference

	String baseFullPath;											// Full Folder Path	
	String lastVideoName = "";										// Last video file name 
	boolean firstExecution = true;									// Boolean to execute only once
	
	FrameLayout previewContainer;									// Preview Camera Container
	CitclopsCameraLibrary cameraLib = null;							// Camera Library
	Button btMeasure;												// Measure Button
	Button bWFS;													// Set Filter Parameters
	SeekBar sbFlashDuration;										// Seekbar Flash Duration 
	TextView textFlashDurationExt;									// Duration Flash Text
	TextView textParameters;										// Options Description Text
	TextView txtPhytoplanktonOption;								// Text of Phytoplankton option
	TextView txtCDOMOption;											// Text of CDOM option
		
	LinearLayout LayoutInternalLed;									// Layout of internal Led
	LinearLayout LayoutExternalLed;									// Layout of external Led
	
	private boolean bInternalLed = true;							// Internal Led
	private boolean bPhytoplankton = true;							// Phytoplankton option enabled
	
	DisplayMetrics metrics = null;									// Metrics to get display properties
	int videoWidth = 0;												// Video capture width
	int videoHeight = 0;											// Video capture height

	LocationUtils mLocationUtils;									// GPS Positioning
	double bestLatLocation = Double.MIN_VALUE;						// Best Location Latitude
	double bestLongLocation = Double.MIN_VALUE;						// Best Location Longitude
	double bestAccurtacy = Double.MIN_VALUE;						// Best Accuracy

    private int selectedFlashDuration;								// Selected Flash Duration at the end of video
    private int selectedVideoWidth;									// Selected Video Width at the end of video
    private int selectedVideoHeight;								// Selected Video Height at the end of video

	private List<WHITE_BALANCE> supportedWhiteBalances;				// LLista de possibles white balances
	private List<SCENE> supportedScene;								// Llista de escenes suportades
	private String [] supportedStringWhiteBalance;					// LLista de possibles white balances
	private String [] supportedStringScene;							// Llista de escenes suportades
	
	private MusicIntentReceiver musicIntentReceiver;				// Get Receiver for HeadPhones
	private AudioManager audioManager;								// AudioManager to control volume
	private int startVolumeValue = 0;								// Current volume on enter Activity
	
    private MediaPlayer mediaPlayerPhytoplankton = null;			// Media Player for Phytoplankton Led
    private MediaPlayer mediaPlayerCDOM = null;						// Media Player for CDOM Led

	NumericKeyPadWidget keyPadDiskDepth;							// KeyPad Sampling Depth
	ScrollView scrollDiskDepth;										// KeyPad Container
	
	/*
	 * Create Activity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.appled_video_activity_layout);

		// Autoreference
		_this = this;
		
		// Reference to Receiver and AudioTrack
		musicIntentReceiver = new MusicIntentReceiver();
		audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
		
		// Init MediaPlayers for Leds
		mediaPlayerPhytoplankton = MediaPlayer.create(CameraVideo.this, R.raw.right);
		mediaPlayerCDOM = MediaPlayer.create(CameraVideo.this, R.raw.left);

		// Create Location Listener
		mLocationUtils = new LocationUtils(_this);
		mLocationUtils.setNewBestLocationListener(newBestLocListener);

		//Reference to Widgets
		getViewsReferences();
		
		// Set Listeners to Widgets
		setListeners();
		
		// At start, for externalLed tho option is Phytoplankton
		stateUIPhytoplankton();
		
		//Set SeekBarValue to init value
		sbFlashDuration.setProgress(0);
	}
	/*
	 * For screen rotation, but orientation is fixed in last version
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	}
	/*
	 * Pause Activity
	 */
	@Override
	protected void onPause() {
		super.onPause();
				
		// Alliberem la càmera
		if (cameraLib != null) cameraLib.releaseCameraAndPreview();
		
		// Stop Listening Location Changes
		if (mLocationUtils != null) mLocationUtils.StopListen();
		
		// Unregister Music Reciver
		unregisterReceiver(musicIntentReceiver);
		
		// Restore Initial Volume
		VolumeRestore();
	}	
	/*
	 * Restart Activity
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}	
	/*
	 * Resume Activity
	 */
	@Override
	protected void onResume() {
		super.onResume();		
		String marcadorExecucio = "";
		try{
			if (firstExecution){
				firstExecution = false;
				
				// Get Display Properties
				metrics = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metrics);
				
				marcadorExecucio = "Before CameraLib";
				
				// Instanciem la llibreria
				cameraLib = new CitclopsCameraLibrary(this);

				marcadorExecucio = "After CameraLib";

				// Get Current Volume Value
				VolumeGetCurrentValue();
				
				// Check for camera instance
				if (cameraLib == null){
					MessageDialog.ShowDialog(_this, Constants.AppTitle, Constants.MsgErrorInstanceCamera, Constants.MsgOkButton, null, true);
				}
				
				// Check for External Storage				
				if (!ExternalStorageUtil.isExternalStorageActive()){					
					MessageDialog.ShowDialog(_this, Constants.AppTitle, Constants.MsgExternalStoragNotActive, Constants.MsgOkButton, null, true);
				}
				
				// Obtenim la ruta on desarem les imatges
				baseFullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FolderVideoName + "/";
				File TmpDirectory = new File(baseFullPath);
				if (!TmpDirectory.exists()) TmpDirectory.mkdirs();				
			}		
			// Camera Instance
			if (!cameraLib.openCamera()){
				// Fail to open camera. 
            	MessageDialog.ShowDialog(_this, Constants.AppTitle, Constants.MsgUnableToOpenCamera, Constants.MsgOkButton, null, true);
			} else {
				// Get Best video resolution based on screen size
				marcadorExecucio = "Before Camera Size";
				Camera.Size fSize = getBestPreviewSize(metrics.widthPixels, metrics.heightPixels);
				videoWidth = fSize.width;
				videoHeight = fSize.height;

				// Càmera assignada. Posem el preview en l'activity
				marcadorExecucio = "Before Preview Container";
				FrameLayout.LayoutParams camLayoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
				previewContainer.removeView(cameraLib.getSurfacePreview());
				previewContainer.addView(cameraLib.getSurfacePreview(), 0, camLayoutParams);

				// Obtenim els possibles valors de white balances
				marcadorExecucio = "Before White Balances";
				supportedWhiteBalances = cameraLib.getSupportedWhiteBalances();
				supportedStringWhiteBalance = new String[supportedWhiteBalances.size()];
				for (int i = 0;i < supportedStringWhiteBalance.length; i++){
					supportedStringWhiteBalance[i] = supportedWhiteBalances.get(i).toString();
				}

				// Obtenim els possibles valors de Scene
				marcadorExecucio = "Before Scenes";
				supportedScene = cameraLib.getSupportedScene();
				supportedStringScene = new String[supportedScene.size()];
				for (int i = 0;i < supportedStringScene.length; i++){
					supportedStringScene[i] = supportedScene.get(i).toString();
				}

				// Inicialitzem les paràmetres de la càmera
				marcadorExecucio = "Before Initialize";
				inicialitzarCamera();

				// Actualitzem el label dels valors de la càmera
				marcadorExecucio = "Before Label Parameters";
				actualitzaLabelParametres();
				
				// Mostrem el misstage				
				MessageDialog.ShowDialog(_this, getString(R.string.app_name), "Take a sample!\nPlease measure the probe directly after sampling!", "OK", null, false);
			}

			// Start Listening Location Changes
			mLocationUtils.StartListen();		

			// Register music Receiver
			IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		    registerReceiver(musicIntentReceiver, filter);
			
		    // Set Max Volume
		    VolumeSetToMax();		    
		} catch (Exception e){
			String msgError = e.getMessage() + ". ";
			for (int i = 0; i < e.getStackTrace().length; i++){
				msgError += e.getStackTrace()[i].toString() + ". ";
			}
        	MessageDialog.ShowDialog(_this, Constants.AppTitle, Constants.MsgErrorLoadActivity + "(" + marcadorExecucio + "): " + msgError, Constants.MsgOkButton, null, true);
		} finally{
			
		}
	}
	/*
	 * Actualitza el valor dels paràmetres en la etiqueta
	 */
	private void actualitzaLabelParametres(){
		try{
			textParameters.setText("WhtBlnc: " + cameraLib.getCurrentWhiteBalance().toString() + ". FcsMd: " + cameraLib.getCurrentFocusMode().toString() + ". Scn: " + cameraLib.getCurrentScene().toString());		
		} catch(Exception e){
			textParameters.setText("");		
		} finally{
			
		}
	}	
	/*************************************************/
	/****** C a m e r a      S e t t i n g s *********/
	/*************************************************/		
	/*
	 * Select White Balance Option
	 */
	private void selectWhiteBalance(){
		try {
			WHITE_BALANCE currentWhiteBalance = cameraLib.getCurrentWhiteBalance();
			int idxSelectedWhiteBalance = -1;
			for (int i = 0; i < supportedWhiteBalances.size(); i++){
				if(supportedWhiteBalances.get(i) == currentWhiteBalance){
					idxSelectedWhiteBalance = i;
					break;
				}
			}
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);			 
	        builder.setTitle(Constants.MsgWhiteBalanceSettingsTitle);
	        builder.setSingleChoiceItems(
	        		supportedStringWhiteBalance, 
	        		idxSelectedWhiteBalance, 
	                new DialogInterface.OnClickListener() {
			            @Override
			            public void onClick(DialogInterface dialog, int which) {
			            	try{
				                // Actualitzem el valor 
				                cameraLib.setWhiteBalance(supportedWhiteBalances.get(which));	
				                
								// Actualitzem el label dels valors de la càmera
								actualitzaLabelParametres();
			        		} catch (Exception e){
								String msgError = e.getMessage() + ". ";
								for (int i = 0; i < e.getStackTrace().length; i++){
									msgError += e.getStackTrace()[i].toString() + ". ";
								}
			                	MessageDialog.ShowDialog(_this, Constants.AppTitle, msgError, Constants.MsgOkButton, null, true);
			        		} finally{
				                // Tanquem el dialeg
				                dialog.dismiss();
			        		}						
			            }
	        });
	         
	        // Mostrem el dialog
	        builder.show();
		} catch (Exception e) {
			String msgError = e.getMessage() + ". ";
			for (int i = 0; i < e.getStackTrace().length; i++){
				msgError += e.getStackTrace()[i].toString() + ". ";
			}
        	MessageDialog.ShowDialog(_this, Constants.AppTitle, Constants.MsgErrorLoadWhiteBalance + ": " + msgError, Constants.MsgOkButton, null, true);
		}
	}
	/*
	 * Select Scene Option
	 */	
	private void selectScene(){
		try {
			SCENE currentScene = cameraLib.getCurrentScene();
			int idxSelectedScene = -1;
			for (int i = 0; i < supportedScene.size(); i++){
				if(supportedScene.get(i) == currentScene){
					idxSelectedScene = i;
					break;
				}
			}
			final AlertDialog.Builder builder = new AlertDialog.Builder(this);			 
	        builder.setTitle(Constants.MsgSceneSettingsTitle);
	        builder.setSingleChoiceItems(
	        		supportedStringScene, 
	        		idxSelectedScene, 
	                new DialogInterface.OnClickListener() {
			            @Override
			            public void onClick(DialogInterface dialog, int which) {
			            	try{
				                // Actualitzem el valor 
				                cameraLib.setScene(supportedScene.get(which));	

								// Actualitzem el label dels valors de la càmera
								actualitzaLabelParametres();
			        		} catch (Exception e){
								String msgError = e.getMessage() + ". ";
								for (int i = 0; i < e.getStackTrace().length; i++){
									msgError += e.getStackTrace()[i].toString() + ". ";
								}
					        	MessageDialog.ShowDialog(_this, Constants.AppTitle, msgError, Constants.MsgOkButton, null, true);
			        		} finally{
				                // Tanquem el dialeg
				                dialog.dismiss();
			        		}						
			            }
	        });
	         
	        // Mostrem el dialog
	        builder.show();
			
		} catch (Exception e) {
			String msgError = e.getMessage() + ". ";
			for (int i = 0; i < e.getStackTrace().length; i++){
				msgError += e.getStackTrace()[i].toString() + ". ";
			}
        	MessageDialog.ShowDialog(_this, Constants.AppTitle, Constants.MsgErrorLoadScene + ": " + msgError, Constants.MsgOkButton, null, true);
		}
	}
	/*
	 * Select Option Scene and White Balance
	 */
	private void selectFocusWhiteScene(){
		final CharSequence[] items = {Constants.MsgWhiteBalanceSettings, Constants.MsgSceneSettings};
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(Constants.MsgWhiteBalanceSceneSettingsTitle);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				case 0:
					selectWhiteBalance();
					break;
				case 1:
					selectScene();
					break;
				}
				dialog.cancel();				
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	/*****************************************************/
	/******  V i d e o   a n d     C a m e r a   *********/
	/*****************************************************/
	/*
	 * Save Video file width delays and milliseconds led
	 */
	private void SaveVideoInternalLed (int flashMilliseconds){		
		try{
			lastVideoName = System.currentTimeMillis() + "";
			cameraLib.SaveMeasurementVideo(delayVideoMillisecondsInternalLed, flashMilliseconds, delayVideoMillisecondsInternalLed, videoWidth, videoHeight, baseFullPath + lastVideoName + ".mp4");
		} catch (Exception e) {			
			String msgError = e.getMessage() + ". ";
			for (int i = 0; i < e.getStackTrace().length; i++){
				msgError += e.getStackTrace()[i].toString() + ". ";
			}
        	MessageDialog.ShowDialog(_this, Constants.AppTitle, Constants.MsgErrorSaveVideo + ": " + msgError, Constants.MsgOkButton, null, true);
		}
	}
	/*
	 * Save Video file width external led
	 */
	private void SaveVideoExternalLed (){		
		try{
			lastVideoName = System.currentTimeMillis() + "";
			cameraLib.SaveMeasurementVideo(delayVideoMillisExternalLedInit, 0, delayVideoMillisExternalLedSound + delayVideoMillisExternalLedEnd, videoWidth, videoHeight, baseFullPath + lastVideoName + ".mp4");
			
			// Start the external led
			if (bPhytoplankton){
				new Handler().postDelayed(new Runnable(){
					@Override
					public void run() {
						setLedPhytoplankton();
					}				
				}, delayVideoMillisExternalLedInit);				
			} else {
				new Handler().postDelayed(new Runnable(){
					@Override
					public void run() {
						setLedCDOM();
					}				
				}, delayVideoMillisExternalLedInit);				
			}
		} catch (Exception e) {			
			String msgError = e.getMessage() + ". ";
			for (int i = 0; i < e.getStackTrace().length; i++){
				msgError += e.getStackTrace()[i].toString() + ". ";
			}
        	MessageDialog.ShowDialog(_this, Constants.AppTitle, Constants.MsgErrorSaveVideo + ": " + msgError, Constants.MsgOkButton, null, true);
		}
	}
	/*
	 * Gets de best Size according to screen size
	 */
    private Camera.Size getBestPreviewSize(int width, int height) {
        Camera.Size result = null;  
        Camera.Size resultMin = null;
        for (Camera.Size size : cameraLib.getSupportedPreviewSizes()) {
        	// Gets de min preview size
        	if (resultMin == null) resultMin = size;
        	int resultMinArea = resultMin.width * resultMin.height;
            int newMinArea = size.width * size.height;
            if (newMinArea < resultMinArea) resultMin = size;
        	
        	// Gets the best Preview Size        	
            if (size.width <= width && size.height <= height) {
                if (result == null) result = size;
                else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea > resultArea) result = size;
                }
            }
        }
        
    	// Screen Size is smaller that previews. Get the
        if (result == null) result = resultMin;
        
        // Return
        return result;
    }
	/*
	 * Inicialitzem els paràmetres de la càmera
	 */
	private void inicialitzarCamera() throws Exception{
		if (cameraLib != null){
			// Inicialitzem paràmetres
			cameraLib.setAntibanding(ANTIBANDING.OFF);
			cameraLib.setAutoExposureLock(true);
			cameraLib.setAutoWhiteBalanceLock(true);
			cameraLib.setColorEffect(COLOR_EFFECT.NONE);
			cameraLib.setFlashMode(FLASH_MODE.OFF);
			cameraLib.setPictureFormat(IMAGEFORMAT.JPEG);
			cameraLib.setCameraZoom(0);
			cameraLib.setCurrentJpegQuality(100);
			cameraLib.setExposureCompensation(0);
			cameraLib.setFocusMode(FOCUS_MODE.FOCUS_CONTINUOUS_VID);
			
			// Set Listener
			cameraLib.setEndMeasureVideoListener(endVideoListener);
		}
	}
    /*********************************************************/
    /*********  E a r p h o n e s     S t a t e  *************/
    /*********************************************************/
	/*
	 * UI for Internal Led
	 */
	private void stateUIInternalLed(){
		LayoutInternalLed.setVisibility(LinearLayout.VISIBLE);
		LayoutExternalLed.setVisibility(LinearLayout.GONE);		
	}
	/*
	 * UI For External Led
	 */
	private void stateUIExternalLed(){
		LayoutInternalLed.setVisibility(LinearLayout.GONE);
		LayoutExternalLed.setVisibility(LinearLayout.VISIBLE);		
	}
	/*
	 * UI For Phytoplankton 
	 */
	private void stateUIPhytoplankton(){
		txtPhytoplanktonOption.setBackgroundDrawable(getResources().getDrawable( R.drawable.border_layout_switch_selected));
		txtPhytoplanktonOption.setTextColor(Color.parseColor("#FF000000"));	    
		txtCDOMOption.setBackgroundDrawable(getResources().getDrawable( R.drawable.border_layout_switch));
		txtCDOMOption.setTextColor(Color.parseColor("#FFFFFFFF"));
		bPhytoplankton = true;
	}
	/*
	 * UI For CDOM 
	 */
	private void stateUICDOM(){
		txtPhytoplanktonOption.setBackgroundDrawable(getResources().getDrawable( R.drawable.border_layout_switch));
		txtPhytoplanktonOption.setTextColor(Color.parseColor("#FFFFFFFF"));	    
		txtCDOMOption.setBackgroundDrawable(getResources().getDrawable( R.drawable.border_layout_switch_selected));
		txtCDOMOption.setTextColor(Color.parseColor("#FF000000"));
		bPhytoplankton = false;
	}
    /*****************************************************/
    /*********  I n i t     A c t i v i t y  *************/
    /*****************************************************/
    /*
     * Gets the Widgets References 
     */
    private void getViewsReferences(){
		previewContainer = (FrameLayout) findViewById(R.id.Video_PreviewContainer);
		btMeasure = (Button) findViewById(R.id.Video_btMeasure);
		sbFlashDuration = (SeekBar) findViewById(R.id.Video_sbFlashDuration);
		bWFS = (Button) findViewById(R.id.Video_buttonWFS);
		textFlashDurationExt = (TextView) findViewById(R.id.Video_textFlashDurationExt);
		textParameters = (TextView) findViewById(R.id.Video_textParameters);
		LayoutInternalLed = (LinearLayout) findViewById(R.id.LayoutInternalLed);
		LayoutExternalLed = (LinearLayout) findViewById(R.id.LayoutExternalLed);		
		txtPhytoplanktonOption = (TextView) findViewById(R.id.txtPhytoplanktonOption);
		txtCDOMOption = (TextView) findViewById(R.id.txtCDOMOption);
		keyPadDiskDepth = (NumericKeyPadWidget)findViewById(R.id.keyPadDiskDepth);
		scrollDiskDepth = (ScrollView)findViewById(R.id.scrollDiskDepth);
    }    
    /*
     * Sets the listeners to widgets
     */
    private void setListeners(){
		sbFlashDuration.setOnSeekBarChangeListener(sbFlashDurationChangeListener);
		bWFS.setOnClickListener(buttonWhiteBalanceSceneClickListener);
		btMeasure.setOnClickListener(buttonMeasureClickListener);
		musicIntentReceiver.setNewSateListener(earphonesStateChangedListener);
		txtPhytoplanktonOption.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				stateUIPhytoplankton();				
			}			
		});
		txtCDOMOption.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				stateUICDOM();				
			}			
		});
    	keyPadDiskDepth.setNewValueListener(listenerSamplingDepthOK);
    }
    /********************************************/
    /*********  L i s t e n e r s   *************/
    /********************************************/
    /*
     * Listener for enter sampling depth in meters  
     */
    NumericKeyPadWidget.newValueListener listenerSamplingDepthOK = new NumericKeyPadWidget.newValueListener() {
		@SuppressLint("SimpleDateFormat")
		@Override
		public void newValue(float newValue) {
			// Save the video measurement file
			OutputStreamWriter ows = null;
            try{
            	String strParameters = cameraLib.getVideoParametersString(selectedFlashDuration, selectedVideoWidth, selectedVideoHeight);
            	strParameters += "<Location>\n";
            	String sLatitude = "N/A";	String sLongitude = "N/A";		String sAccuracy = "N/A";
            	if (bestLatLocation > Double.MIN_VALUE) 	sLatitude = String.valueOf(bestLatLocation);
            	if (bestLongLocation > Double.MIN_VALUE) 	sLongitude = String.valueOf(bestLongLocation);
            	if (bestAccurtacy > Double.MIN_VALUE)		sAccuracy = String.valueOf((int)bestLongLocation);
            	strParameters += "<Latitude>" + sLatitude + "</Latitude>\n";
            	strParameters += "<Longitude>" + sLongitude + "</Longitude>\n";
            	strParameters += "<Accuracy>" + sAccuracy + "</Accuracy>\n";            	
            	strParameters += "</Location>\n";	            	
            	strParameters += "<Led>\n";
            	if (bInternalLed) strParameters += "<Source>INTERNAL</Source>\n";
            	else {
            		strParameters += "<Source>EXTERNAL</Source>\n";
            		if (bPhytoplankton) strParameters += "<Type>PHYTOPLANKTON</Type>\n";
            		else strParameters += "<Type>CDOM</Type>\n";
            	}	            	
            	strParameters += "</Led>\n";
            	strParameters += "<SamplingDepth>" + String.valueOf(newValue) + "</SamplingDepth>\n";

            	            	
            	// Get Local Time
            	Date datenow = new Date();
            	SimpleDateFormat fLocal = new SimpleDateFormat("yyyyMMddHHmmss");
            	strParameters += "<local_datetime>" + fLocal.format(datenow) + "</local_datetime>\n";

            	// Get UTC Time
            	SimpleDateFormat fUTC = new SimpleDateFormat("yyyyMMddHHmmss");
            	fUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
            	strParameters += "<utc_datetime>" + fUTC.format(datenow) + "</utc_datetime>\n";

                File  fileParameters = new File(baseFullPath + lastVideoName + ".xml");            
                FileOutputStream fout = new FileOutputStream(fileParameters);
            	ows = new  OutputStreamWriter(fout);
                ows.write(strParameters);
                ows.flush();
            } catch (Exception e){
				String msgError = e.getMessage() + ". ";
				for (int i = 0; i < e.getStackTrace().length; i++){
					msgError += e.getStackTrace()[i].toString() + ". ";
				}
				MessageDialog.ShowDialog(_this, Constants.AppTitle,  Constants.MsgErrorUnableCameraParamatersFile + ": " + msgError, Constants.MsgOkButton, null, true);
            } finally{
            	if (ows != null) {
            		try {ows.close();} catch (IOException e) {}
            	}					
	            // Activate Buttons
	        	btMeasure.setEnabled(true);	
	        	bWFS.setEnabled(true);	
	        	sbFlashDuration.setEnabled(true);
	        	txtPhytoplanktonOption.setEnabled(true);
	        	txtCDOMOption.setEnabled(true);
	        	
	        	// Hide Numeric KeyPAd
	        	if (scrollDiskDepth != null) scrollDiskDepth.setVisibility(FrameLayout.GONE);    	
			}
		}    	
    };    
    /*
     * Listener on seekbar for flash duration
     */
    OnSeekBarChangeListener sbFlashDurationChangeListener = new OnSeekBarChangeListener(){
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {}			
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {}			
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
			progress = ((int)Math.round(progress/stepFlashSize)) * stepFlashSize;
		    seekBar.setProgress(progress);
		    
			// Update UI Value
			textFlashDurationExt.setText(progress + " milliseconds");
		}
    };
    /*
     * Listener for button White Balance and Scene Click
     */
    OnClickListener buttonWhiteBalanceSceneClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			try {
				selectFocusWhiteScene();
			} catch (Exception e) {
				String msgError = e.getMessage() + ". ";
				for (int i = 0; i < e.getStackTrace().length; i++){
					msgError += e.getStackTrace()[i].toString() + ". ";
				}
				MessageDialog.ShowDialog(_this, Constants.AppTitle, msgError, Constants.MsgOkButton, null, true);
			}
		}
    };
    /*
     * Listener for button Measure click 
     */
    OnClickListener buttonMeasureClickListener = new OnClickListener(){
		@Override
		public void onClick(View v) {
			try{				
				// Disable Buttons
            	btMeasure.setEnabled(false);	
            	bWFS.setEnabled(false);	
            	sbFlashDuration.setEnabled(false);	
            	txtPhytoplanktonOption.setEnabled(false);
            	txtCDOMOption.setEnabled(false);
            	
            	if (bInternalLed){
                	// Save Video with internal Led
            		SaveVideoInternalLed(sbFlashDuration.getProgress());            		
            	} else {
            		// Save Video with external Led
            		SaveVideoExternalLed();
            	}            	
			} catch (Exception e){
				String msgError = e.getMessage() + ". ";
				for (int i = 0; i < e.getStackTrace().length; i++){
					msgError += e.getStackTrace()[i].toString() + ". ";
				}
				MessageDialog.ShowDialog(_this, Constants.AppTitle, msgError, Constants.MsgOkButton, null, true);
			} finally{
				
			}
		}    	
    };
    /*
     * Listener at the end of video measurement 
     */
    CitclopsCameraLibrary.endMeasureVideoListener endVideoListener = new CitclopsCameraLibrary.endMeasureVideoListener(){
		@Override
		public void endMeasureVideo(int flashDuration, int VideoWidth, int VideoHeight) {
			// Selected Values
		    selectedFlashDuration = flashDuration;
		    selectedVideoWidth = VideoWidth;
		    selectedVideoHeight = VideoHeight;
      
		    // Show KeyPad for Sampling Depth
		    showKeyPad();
		}			    	
    };
    /*
     * Listener for earphones change event
     */
    MusicIntentReceiver.changeStateListener earphonesStateChangedListener = new MusicIntentReceiver.changeStateListener(){
		@Override
		public void changeState(HEADESET_STATE newState) {
			//stateUIInternalLed			
			switch (newState){
				case HEADSET_PLUGGED:
					stateUIExternalLed();
					bInternalLed = false;
					break;
				case HEADSET_UNPLUGGED:
					stateUIInternalLed();
					bInternalLed = true;
					break;
			}			
		}    	
    };
	/*
	 * Listener of Best Location Changed
	 */
    LocationUtils.newBestLocationListener newBestLocListener = new LocationUtils.newBestLocationListener() {		
		@Override
		public void newBestLocation(Location currentBestLocation) {
			if (mLocationUtils != null){
				if (mLocationUtils.isLocationEnabled()){
					// if location has accuracy, store them
					if (currentBestLocation.hasAccuracy()){
						bestLatLocation = currentBestLocation.getLatitude();
						bestLongLocation = currentBestLocation.getLongitude();
						bestAccurtacy = currentBestLocation.getAccuracy();
					}
				}						
			}
		}
	}; 
    /**********************************************************************/
    /*****************   A u d i o     S e t t i n g s   ******************/
    /**********************************************************************/
    /*
     * Gets the current Music Volume, on enter activity
     */
    private void VolumeGetCurrentValue(){
    	startVolumeValue = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }
    /*
     * Restore the initial Music volume level
     */
    private void VolumeRestore(){
    	audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, startVolumeValue, 0);	    	
    }
    /*
     * Sets the Music Volume to the Maximum
     */
    private void VolumeSetToMax(){
    	audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);	
    }
    /*****************************************************/
    /*****************   K e y P a d    ******************/
    /*****************************************************/
    /*
     * Shows Numeric KeyPad
     */
    private void showKeyPad(){
    	// Init Value
    	keyPadDiskDepth.InitValue();    	
    	keyPadDiskDepth.setOnlyInteger(false);
    	
    	// Show Keypad
    	if (scrollDiskDepth != null) scrollDiskDepth.setVisibility(FrameLayout.VISIBLE);    	
    }    
    /****************************************************************************/
    /*****************   A u d i o     L e d     P l a y e r   ******************/
    /****************************************************************************/
    /*
     * Sets the Phytplankton Led
     */
    private void setLedPhytoplankton(){
    	mediaPlayerPhytoplankton.seekTo(0);
    	mediaPlayerPhytoplankton.start();
    }
    /*
     * Sets the CDOM Led
     */
    private void setLedCDOM(){
    	mediaPlayerCDOM.seekTo(0);
    	mediaPlayerCDOM.start();    	
    }
}
