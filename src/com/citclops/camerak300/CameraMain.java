package com.citclops.camerak300;

//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.OutputStreamWriter;
//import java.util.List;

import android.app.Activity;
//import android.app.AlertDialog;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Bundle;
//import android.os.Environment;
//import android.view.View;
//import android.view.View.OnClickListener;
//import android.view.ViewGroup.LayoutParams;
//import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.FrameLayout;
//import android.widget.SeekBar;
//import android.widget.SeekBar.OnSeekBarChangeListener;
//import android.widget.TextView;
//import android.widget.Toast;

import com.citclops.cameralibrary.CameraCallback;
//import com.citclops.cameralibrary.CameraConstants.ANTIBANDING;
//import com.citclops.cameralibrary.CameraConstants.COLOR_EFFECT;
//import com.citclops.cameralibrary.CameraConstants.FLASH_MODE;
//import com.citclops.cameralibrary.CameraConstants.FOCUS_MODE;
//import com.citclops.cameralibrary.CameraConstants.IMAGEFORMAT;
//import com.citclops.cameralibrary.CameraConstants.SCENE;
//import com.citclops.cameralibrary.CameraConstants.WHITE_BALANCE;
//import com.citclops.cameralibrary.CitclopsCameraLibrary;

public class CameraMain extends Activity implements CameraCallback {
	
//	private final String FolderPhotoName = "AppLED";				// Carpeta on desarem les fotos
//	Context _this;													// Auto referencia
//
//	final int stepFlashSize = 50;									// Step del seekbar del Flash
//	final int MILLISECONDS_FLASH_DARK_MEASUREMENT = 200;			// Millisegons flash dark measurement
//	
//	FrameLayout previewContainer;									// Container del preview de la camera
//	boolean firstExecution = true;									// Booleana per saber si estem en primera execucio
//	CitclopsCameraLibrary cameraLib = null;							// Llibreria de la camera
//	Button btMeasure;												// Botó de realitzar la foto
//	CheckBox cbDarkMeasurament;										// CheckBox per realiztar el darl measurement
//	Button bWFS;													// Botó de les opcions de la camera
//	SeekBar sbFlashDuration;										// Seekbar de la duracio del Flash 
//	TextView textFlashDurationExt;									// Texte de la duració del Flash
//	TextView textParameters;										// Texte descripció opcions
//	String baseFullPath;											// Path sencer on desarem les fotos
//	
//	String lastPictureName = "";									// Ultim nom d'imatge... per el darkmeasurement
//
//	private List<WHITE_BALANCE> supportedWhiteBalances;				// LLista de possibles white balances
//	private List<FOCUS_MODE> supportedFocusMode;					// LLista de possibles focus mode
//	private List<SCENE> supportedScene;								// Llista de escenes suportades
//	private String [] supportedStringFocusMode;						// LLista de possibles focus mode
//	private String [] supportedStringWhiteBalance;					// LLista de possibles white balances
//	private String [] supportedStringScene;							// Llista de escenes suportades

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.appled_main_activity_layout);
//
//		// Autoreferencia
//		_this = this;
//		
//		//Fem referencia als controls
//		previewContainer = (FrameLayout) findViewById(R.id.PreviewContainer);
//		btMeasure = (Button) findViewById(R.id.btMeasure);
//		sbFlashDuration = (SeekBar) findViewById(R.id.sbFlashDuration);
//		cbDarkMeasurament = (CheckBox) findViewById(R.id.cbDarkMeasurament);
//		bWFS = (Button) findViewById(R.id.buttonWFS);
//		textFlashDurationExt = (TextView) findViewById(R.id.textFlashDurationExt);
//		textParameters = (TextView) findViewById(R.id.textParameters);
//		
//		// Definició d'events
//		sbFlashDuration.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {			
//			@Override
//			public void onStopTrackingTouch(SeekBar seekBar) {}			
//			@Override
//			public void onStartTrackingTouch(SeekBar seekBar) {}			
//			@Override
//			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
//				progress = ((int)Math.round(progress/stepFlashSize))*stepFlashSize;
//			    seekBar.setProgress(progress);
//			    
//				// Informem del canvi de valor
//				textFlashDurationExt.setText(progress + " milliseconds");
//			}
//		});
//		bWFS.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				try {
//					selectFocusWhiteScene();
//				} catch (Exception e) {
//					showOKMessage("ERROR: " + e.getMessage());
//				}
//			}
//		});	
//		btMeasure.setOnClickListener(new OnClickListener() {			
//			@Override
//			public void onClick(View v) {
//				try{
//					if(!cbDarkMeasurament.isChecked()){
//						takePicture(sbFlashDuration.getProgress());
//					}else{
//						darkMeasurementPicture();
//					}
//				} catch (Exception e){
//					Toast.makeText(_this, e.getMessage(), Toast.LENGTH_LONG).show();
//				} finally{
//					
//				}
//			}
//		});
	}
//	@Override
//	public void onConfigurationChanged(Configuration newConfig) {
//	  super.onConfigurationChanged(newConfig);
//	}
//	@Override
//	protected void onPause() {
//		super.onPause();
//		// Alliberem la càmera
//		if (cameraLib != null) cameraLib.releaseCameraAndPreview();		
//	}
//	@Override
//	protected void onRestart() {
//		super.onRestart();
//		Intent intent = getIntent();
//		finish();
//		startActivity(intent);
//	}	
//	@Override
//	protected void onResume() {
//		super.onResume();		
//		try{
//			if (firstExecution){
//				firstExecution = false;
//				
//				// Instanciem la llibreria
//				cameraLib = new CitclopsCameraLibrary(this);
//				
//				// Passem els callbacks
//				cameraLib.setCallback(this);
//							
//				// Obtenim la ruta on desarem les imatges
//				baseFullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + FolderPhotoName + "/";
//				File TmpDirectory = new File(baseFullPath);
//				if (!TmpDirectory.exists()) TmpDirectory.mkdirs();				
//			}		
//			// Instanciem la càmera
//			if (!cameraLib.openCamera()){
//				// No hem pogut obrir la càmera... ho informem
//				Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show();
//			} else {
//				// Càmera assignada. Posem el preview en l'activity
//				FrameLayout.LayoutParams camLayoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
//				previewContainer.removeView(cameraLib.getSurfacePreview());
//				previewContainer.addView(cameraLib.getSurfacePreview(), 0, camLayoutParams);
//							
//				// Obtenim els possibles valors de white balances
//				supportedWhiteBalances = cameraLib.getSupportedWhiteBalances();
//				supportedStringWhiteBalance = new String[supportedWhiteBalances.size()];
//				for (int i = 0;i < supportedStringWhiteBalance.length; i++){
//					supportedStringWhiteBalance[i] = supportedWhiteBalances.get(i).toString();
//				}
//				
//				// Obtenim els possibles valors de Scene
//				supportedScene = cameraLib.getSupportedScene();
//				supportedStringScene = new String[supportedScene.size()];
//				for (int i = 0;i < supportedStringScene.length; i++){
//					supportedStringScene[i] = supportedScene.get(i).toString();
//				}
//				
//				// Obtenim els possibles valors del focus Mode
//				supportedFocusMode = cameraLib.getSupportedFocusMode();
//				supportedStringFocusMode = new String[supportedFocusMode.size()];	
//				for (int i = 0;i < supportedStringFocusMode.length; i++){
//					supportedStringFocusMode[i] = supportedFocusMode.get(i).toString();
//				}				
//				// Inicialitzem les paràmetres de la càmera
//				inicialitzarCamera();
//				
//				// Actualitzem el label dels valors de la càmera
//				actualitzaLabelParametres();
//			}		
//		} catch (Exception e){
//			Toast.makeText(_this, "Load Error: " + e.getMessage(), Toast.LENGTH_LONG).show();			
//		} finally{
//			
//		}
//	}
//	/*
//	 * Inicialitzem els paràmetres de la càmera
//	 */
//	private void inicialitzarCamera() throws Exception{
//		if (cameraLib != null){
//			// Inicialitzem paràmetres
//			cameraLib.setAntibanding(ANTIBANDING.OFF);
//			cameraLib.setAutoExposureLock(true);
//			cameraLib.setAutoWhiteBalanceLock(true);
//			cameraLib.setColorEffect(COLOR_EFFECT.NONE);
//			cameraLib.setFlashMode(FLASH_MODE.OFF);
//			cameraLib.setPictureFormat(IMAGEFORMAT.JPEG);
//			cameraLib.setCameraZoom(0);
//			cameraLib.setCurrentJpegQuality(100);
//			cameraLib.setExposureCompensation(0);
//		}
//	}	
//	/*
//	 * Actualitza el valor dels paràmetres en la etiqueta
//	 */
//	private void actualitzaLabelParametres(){
//		try{
//			textParameters.setText("WhtBlnc: " + cameraLib.getCurrentWhiteBalance().toString() + ". FcsMd: " + cameraLib.getCurrentFocusMode().toString() + ". Scn: " + cameraLib.getCurrentScene().toString());		
//		} catch(Exception e){
//			textParameters.setText("");		
//		} finally{
//			
//		}
//	}
	/*******************************************************************************/
	/***** I m p l e m e n t a c i o    d e       l a     I n t e r f a c e ********/
	/*******************************************************************************/
	boolean isInDarkMeasurement = false;
	@Override
	public void onJpegPictureTaken(byte[] data, Camera camera, boolean isDark) {
//		// Callback que es crida si volem desar en JPEG
//        try{
//        	String filePath = "";
//        	if ((isInDarkMeasurement) && (!isDark)){
//        		// Estem en la segona foto del darkmeasurement
//        		filePath = baseFullPath + lastPictureName + "_B.jpg";        		
//        	} else {
//        		// Estem en primera foto
//        		lastPictureName = System.currentTimeMillis() + "";
//        		filePath = baseFullPath + lastPictureName + ".jpg";        		
//        	}
//        	
//        	// En el cas que estiguem en la primera foto del darkmeasurement
//        	if (isDark) isInDarkMeasurement = true;
//        	else isInDarkMeasurement = false;
//        	        	
//        	// Desem la imatge
//            FileOutputStream outStream = new FileOutputStream(filePath);            
//            outStream.write(data);
//            outStream.close();
//            
//            // Només farem paràmetres quan NO és darkmeasurement
//        	if (!isDark) {
//        		// Obtenim els paràmetres
//                String strParameters = cameraLib.getParametersString().toString();
//
//                // Escribim l'arxiu a la mateixa carpeta
//                File  fileParameters = new File(baseFullPath, lastPictureName + ".xml");            
//                FileOutputStream fout = new FileOutputStream(fileParameters);
//                OutputStreamWriter ows = null;
//                try{
//                	ows = new  OutputStreamWriter(fout);
//                    ows.write(strParameters);
//                    ows.flush();
//                } catch (Exception e){
//        			Toast.makeText(this, "Can't create parameters file: " + e.getMessage(), Toast.LENGTH_SHORT).show();                	
//                } finally{
//                	if (ows != null) ows.close();
//                }
//        	}
//        } catch(Exception e) {
//        	Toast.makeText(_this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
//        }
//        
//        // Reiniciem el preview
//        camera.startPreview();
//        
//        try{
//        	// Si hem fet la primera foto del darkmeasurement, fem la segona
//        	if (isInDarkMeasurement) {
//        		if (sbFlashDuration.getProgress() == 0) takePicture(MILLISECONDS_FLASH_DARK_MEASUREMENT);
//        		else takePicture(sbFlashDuration.getProgress());
//        	}         	
//        } catch (Exception e){
//        	Toast.makeText(_this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();    	
//        }
	}
	@Override
	public void onPreviewFrame(byte[] arg0, Camera arg1) {}
	@Override
	public void onRawPictureTaken(byte[] arg0, Camera arg1, boolean arg2) {}
	@Override
	public void onShutter() {}		
	/*****************************************************/
	/****** L i s t e n e r s      W i d g e t s *********/
	/******************************************************/	
//	private void takePicture(int milliseconds) throws Exception{
//		try{
//			if(milliseconds > 0){
//				// Engeguem el flash el que sigui necessari
//				cameraLib.takePictureMillisecondsFlash(sbFlashDuration.getProgress());							
//			} else {
//				// No hi ha flash
//				cameraLib.takePicture();							
//			}
//		} catch (Exception e){
//			throw e;
//		} finally{
//			
//		}						
//	}	
//	private void darkMeasurementPicture() throws Exception{
//		try{
//			cameraLib.darkMeasurementPicture();
//		} catch (Exception e){
//			throw e;
//		} finally{
//			
//		}						
//	}	
//	private void selectFocusMode() throws Exception{
//		try{
//			FOCUS_MODE currentFocusMode = cameraLib.getCurrentFocusMode();
//			int idxSelectedFocusMode = -1;
//			for(int i = 0; i < supportedFocusMode.size(); i++){
//				if (supportedFocusMode.get(i) == currentFocusMode){
//					idxSelectedFocusMode = i;
//					break;
//				}
//			}
//			
//			// Creem el selector 
//			final AlertDialog.Builder builder = new AlertDialog.Builder(this);			 
//	        builder.setTitle("Selector Focus Mode");
//	        builder.setSingleChoiceItems(
//	        		supportedStringFocusMode, 
//	        		idxSelectedFocusMode, 
//	                new DialogInterface.OnClickListener() {
//			            @Override
//			            public void onClick(DialogInterface dialog, int which) {
//			            	try{
//				                // Actualitzem el valor 
//				                cameraLib.setFocusMode(supportedFocusMode.get(which));	
//				                
//								// Actualitzem el label dels valors de la càmera
//								actualitzaLabelParametres();
//			        		} catch (Exception e){
//								Toast.makeText(_this, e.getMessage(), Toast.LENGTH_LONG).show();
//			        		} finally{
//				                // Tanquem el dialeg
//				                dialog.dismiss();
//			        		}						
//			            }
//	        });
//	         
//	        // Mostrem el dialog
//	        builder.show();
//		} catch (Exception e){
//			throw e;
//		} finally{
//			
//		}						
//	}
//	
//	private void selectWhiteBalance(){
//		try {
//			WHITE_BALANCE currentWhiteBalance = cameraLib.getCurrentWhiteBalance();
//			int idxSelectedWhiteBalance = -1;
//			for (int i = 0; i < supportedWhiteBalances.size(); i++){
//				if(supportedWhiteBalances.get(i) == currentWhiteBalance){
//					idxSelectedWhiteBalance = i;
//					break;
//				}
//			}
//			
//			final AlertDialog.Builder builder = new AlertDialog.Builder(this);			 
//	        builder.setTitle("Selector White Balance");
//	        builder.setSingleChoiceItems(
//	        		supportedStringWhiteBalance, 
//	        		idxSelectedWhiteBalance, 
//	                new DialogInterface.OnClickListener() {
//			            @Override
//			            public void onClick(DialogInterface dialog, int which) {
//			            	try{
//				                // Actualitzem el valor 
//				                cameraLib.setWhiteBalance(supportedWhiteBalances.get(which));	
//				                
//								// Actualitzem el label dels valors de la càmera
//								actualitzaLabelParametres();
//			        		} catch (Exception e){
//								Toast.makeText(_this, e.getMessage(), Toast.LENGTH_LONG).show();
//			        		} finally{
//				                // Tanquem el dialeg
//				                dialog.dismiss();
//			        		}						
//			            }
//	        });
//	         
//	        // Mostrem el dialog
//	        builder.show();
//		} catch (Exception e) {
//			Toast.makeText(_this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
//		}
//	}
//	
//	private void selectScene(){
//		try {
//			SCENE currentScene = cameraLib.getCurrentScene();
//			int idxSelectedScene = -1;
//			for (int i = 0; i < supportedScene.size(); i++){
//				if(supportedScene.get(i) == currentScene){
//					idxSelectedScene = i;
//					break;
//				}
//			}
//			
//			final AlertDialog.Builder builder = new AlertDialog.Builder(this);			 
//	        builder.setTitle("Selector Scene");
//	        builder.setSingleChoiceItems(
//	        		supportedStringScene, 
//	        		idxSelectedScene, 
//	                new DialogInterface.OnClickListener() {
//			            @Override
//			            public void onClick(DialogInterface dialog, int which) {
//			            	try{
//				                // Actualitzem el valor 
//				                cameraLib.setScene(supportedScene.get(which));	
//
//								// Actualitzem el label dels valors de la càmera
//								actualitzaLabelParametres();
//			        		} catch (Exception e){
//								Toast.makeText(_this, e.getMessage(), Toast.LENGTH_LONG).show();
//			        		} finally{
//				                // Tanquem el dialeg
//				                dialog.dismiss();
//			        		}						
//			            }
//	        });
//	         
//	        // Mostrem el dialog
//	        builder.show();
//			
//		} catch (Exception e) {
//			Toast.makeText(_this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
//		}
//	}
//	
//	private void selectFocusWhiteScene(){
//		final CharSequence[] items = {"White Balance", "Focus Mode", "Scene Mode"};
//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//		builder.setTitle("Specify the parameters");
//		builder.setItems(items, new DialogInterface.OnClickListener() {
//			
//			@Override
//			public void onClick(DialogInterface dialog, int item) {
//				switch (item) {
//				case 0:
//					selectWhiteBalance();
//					break;
//				case 1:
//					try {
//						selectFocusMode();
//					} catch (Exception e) {
//						Toast.makeText(_this, "ERROR: " + e.getMessage(), Toast.LENGTH_LONG).show();
//					}
//					break;
//				case 2:
//					selectScene();
//					break;
//				}
//				dialog.cancel();
//				
//			}
//		});
//		AlertDialog alert = builder.create();
//		alert.show();
//	}
//	
//    /*
//     * Show message in a Dialog
//     */
//    @SuppressWarnings("deprecation")
//	private void showOKMessage(String message){
//    	
//    	
//    	// Toast.makeText(_this, message, Toast.LENGTH_LONG).show();
//    	
//    	AlertDialog ad = new AlertDialog.Builder(this).create();
//    	ad.setCancelable(false); // This blocks the 'BACK' button
//    	ad.setMessage(message);
//    	ad.setButton("OK", new DialogInterface.OnClickListener() {
//    	    @Override
//    	    public void onClick(DialogInterface dialog, int which) {
//    	        dialog.dismiss();                    
//    	    }
//    	});
//    	ad.show();	
//    }
//
//	
}
