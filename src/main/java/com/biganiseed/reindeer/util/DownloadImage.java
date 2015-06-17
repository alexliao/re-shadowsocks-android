package com.biganiseed.reindeer.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class DownloadImage {
//	static public byte[] downloadImageBytes(String url) throws Exception{
//        URL myFileUrl =new URL(url);   
//        HttpURLConnection conn= (HttpURLConnection)myFileUrl.openConnection();
//        conn.setDoInput(true);
//        conn.connect();
//        InputStream is = conn.getInputStream();
//       
//        Bitmap bm = BitmapFactory.decodeStream(is);
//        //FileOutputStream out = new FileOutputStream(f);   
//        //bm.compress(Bitmap.CompressFormat.PNG, 90, out);
//        final ByteArrayOutputStream os = new ByteArrayOutputStream();  
//        bm.compress(Bitmap.CompressFormat.PNG, 100, os);
//        return os.toByteArray();
//	}

	static public byte[] toBytes(String url, int timeout) throws Exception{
        Bitmap bm = toBitmap(url, timeout);
        //FileOutputStream out = new FileOutputStream(f);   
        //bm.compress(Bitmap.CompressFormat.PNG, 90, out);
        if(bm != null){
        	final ByteArrayOutputStream os = new ByteArrayOutputStream();  
        	bm.compress(Bitmap.CompressFormat.PNG, 100, os);
        	return os.toByteArray();
        }else{
        	return null;
        }
	}

	static public void toFile(String url, File f, int timeout) throws Exception{
        Bitmap bm = toBitmap(url, timeout);
        if(bm != null){
	        FileOutputStream out = new FileOutputStream(f);   
	        bm.compress(Bitmap.CompressFormat.PNG, 100, out);
        }
	}

	static public Bitmap toBitmap(String url, int timeout) throws Exception{
        URL myFileUrl =new URL(url);   
        HttpURLConnection conn= (HttpURLConnection)myFileUrl.openConnection();
        conn.setReadTimeout(timeout);
        conn.setDoInput(true);
        conn.connect();
        InputStream is = conn.getInputStream();
        Bitmap bm = BitmapFactory.decodeStream(is);
        return bm;
	}
	
}
