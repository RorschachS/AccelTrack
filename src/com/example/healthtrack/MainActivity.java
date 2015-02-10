package com.example.healthtrack;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	Button btnDiagno;
	Button btnUpload;
	FileOutputStream fos;
	File currentDB, sd; 
	 HttpURLConnection conn = null;
     DataOutputStream dos = null;  
     String DB_PATH = Environment.getExternalStorageDirectory().getPath().toString();
     private SQLiteDatabase db;
     
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnDiagno=(Button)findViewById(R.id.btnDiagno);
        btnUpload=(Button)findViewById(R.id.btnUploadDB);
        btnDiagno.setOnClickListener(this);
        btnUpload.setOnClickListener(this);
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnDiagno:
			startHealthMonitoringActivity();
			break;
		case R.id.btnUploadDB:
			new UploadDB().execute();
			break;
		default:
			break;
		}
	}
	


	public void startHealthMonitoringActivity(){
		Intent intent=new Intent(this,HealthMonitoring.class);
		startActivity(intent);
	}

	
	
class UploadDB extends AsyncTask<String, String, String>{

		
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			
			
			Log.d("1", "started");
			try{
				
		          conn = null;
		          DataOutputStream dos = null; 
		          String lineEnd = "\r\n";
		          String twoHyphens = "--";
		          String boundary = "*****";
		          int bytesRead, bytesAvailable, bufferSize;
		          byte[] buffer;
		          int maxBufferSize = 1 * 1024 * 1024;
		          String sourceFileUri = "/storage/emulated/0/AccelDB";
		          File sourceFile = new File(sourceFileUri); 
		          String upLoadServerUri = "http://www.androidexample.com/media/UploadToServer.php"; 
		          
		          Log.d("xxx", "started01");
		          if (!sourceFile.isFile()) {
		               
		               Log.d("uploadFile", "Source File not exist :"); 
		               Toast.makeText(getApplicationContext(), "Source file doesn't exists", Toast.LENGTH_SHORT).show();
		                   
		          }
		          else{
		        	  // open a URL connection to the Servlet
	                   FileInputStream fileInputStream = new FileInputStream(sourceFile);
	                   URL url = new URL(upLoadServerUri);
	          
	                 
	                   // Open a HTTP  connection to  the URL
	                   
	                   conn = (HttpURLConnection) url.openConnection();
	                   conn.setDoInput(true); // Allow Inputs
	                   conn.setDoOutput(true); // Allow Outputs
	                   conn.setUseCaches(false); // Don't use a Cached Copy
	                   conn.setRequestMethod("POST");
	                   conn.setRequestProperty("Connection", "Keep-Alive");
	                   conn.setRequestProperty("ENCTYPE", "multipart/form-data");
	                   conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
	                   conn.setRequestProperty("uploaded_file", "AccelDB"); 
	                   
	                   Log.d("xxx", "started01");
	                   
	                   dos = new DataOutputStream(conn.getOutputStream());
	                   
	                   Log.d("xxx", "started0YYY");
	                   
	                   dos.writeBytes(twoHyphens + boundary + lineEnd);
	                   dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
	                                             + "AccelDB" + "\"" + lineEnd);
	                    
	                   dos.writeBytes(lineEnd);
	          
	                   // create a buffer of  maximum size
	                   bytesAvailable = fileInputStream.available();
	          
	                   bufferSize = Math.min(bytesAvailable, maxBufferSize);
	                   buffer = new byte[bufferSize];
	          
	                   // read file and write it into form...
	                   bytesRead = fileInputStream.read(buffer, 0, bufferSize); 
	                      
	                   while (bytesRead > 0) {
	                        
	                     dos.write(buffer, 0, bufferSize);
	                     bytesAvailable = fileInputStream.available();
	                     bufferSize = Math.min(bytesAvailable, maxBufferSize);
	                     bytesRead = fileInputStream.read(buffer, 0, bufferSize);  
	                      
	                    }
	           
	                   // send multipart form data necesssary after file data...
	                   dos.writeBytes(lineEnd);
	                   dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
	          
	                   // Responses from the server (code and message)
	                   int serverResponseCode = conn.getResponseCode();
	                   String serverResponseMessage = conn.getResponseMessage();
	                     
	                   Log.i("uploadFile", "HTTP Response is : "
	                           + serverResponseMessage + ": " + serverResponseCode);
	                    
	                   if(serverResponseCode == 200){
	                        
	                	   publishProgress("uploadedFile"+serverResponseMessage + ": " + serverResponseCode);         
	                   }   
	                    
	                   //close the streams //
	                  
	                   
	                   
	                   fileInputStream.close();
	                   dos.flush();
	                   dos.close();
	                   
	                   Log.d("No1","hi");
	                     
	                   
	           				Log.d("1-","Before Delete");
	           				SQLiteDatabase.deleteDatabase(new File(DB_PATH+"/AccelDB"));
	           				Log.d("2-","After Delete");
	           			
	                   Log.d("No2","Zeel deleted");
	                   
	                   //Process to delete database
	                   
	                   
		          }
		          
				
		  
			     
			   }
				catch(Exception e){
					e.printStackTrace();
				}
			return null;
		}

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
		}

		@Override
		protected void onProgressUpdate(String... values) {
			// TODO Auto-generated method stub
			super.onProgressUpdate(values);
			
			Toast.makeText(getApplicationContext(), values[0], Toast.LENGTH_LONG).show();
		}
		
		
	}
	
}


