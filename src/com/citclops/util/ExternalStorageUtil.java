package com.citclops.util;

import java.io.File;

import android.os.Environment;

public class ExternalStorageUtil {
    public static boolean isExternalStorageActive(){
		boolean retorn = false;
		File rootSD = null;
		try{
			rootSD = Environment.getExternalStorageDirectory();
			if (rootSD != null){
				if (rootSD.canWrite()){  
					//Nom�s acceptem la SD, si exietix y s'hi por escriure
					retorn = true;
				}
			}			
		} catch (Exception ex){
			retorn = false;
		} finally{
			
		}
		return retorn;
	}

}
