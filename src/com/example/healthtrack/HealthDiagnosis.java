package com.example.healthtrack;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.LegendAlign;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class HealthDiagnosis extends Activity implements OnClickListener{

	Button btnRunDiagno,btnStopDiagno;
	RelativeLayout layout;
	GraphViewSeries healthGraphX,healthGraphY,healthGraphZ;
	GraphView graphView;
	Random randNumber;
	GraphViewData[] gvd_x,gvd_y,gvd_z;
	String DB_PATH = Environment.getExternalStorageDirectory().getPath().toString();
	FirstThread first;
	SecondThread second;
	boolean pauseFlag;
	
	private SensorManager accelManage;
	private Sensor senseAccel;
	float gravity[] ={0.0f,0.0f,0.0f};
	Cursor c;
	boolean stopButtonNotClicked=true;
	private int graph2LastXValue=0;
	private SQLiteDatabase db;
	private ArrayList<Name_ID_Age_Sex> al;
	private Name_ID_Age_Sex[] array;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.healthdiagnosis);
		btnRunDiagno=(Button)findViewById(R.id.btnRunDiagno);
		btnStopDiagno=(Button)findViewById(R.id.btnStopDiagno);
		btnRunDiagno.setOnClickListener(this);
		btnStopDiagno.setOnClickListener(this);

		first=new FirstThread();
		first.execute();

		createDB();
		
		graphView=new LineGraphView(this, "Health Diagnosis");
		graphView.setViewPort(1, 9);
		graphView.setScalable(true);
		graphView.setManualYAxisBounds(30,-30);
		graphView.setShowLegend(true);
		graphView.setLegendAlign(LegendAlign.TOP);

		graphView.setBackgroundColor(Color.BLUE);
		graphView.getGraphViewStyle().setGridColor(Color.GREEN);
		graphView.getGraphViewStyle().setHorizontalLabelsColor(Color.YELLOW);
		graphView.getGraphViewStyle().setVerticalLabelsColor(Color.YELLOW);

		graphView.getGraphViewStyle().setNumHorizontalLabels(10);
		graphView.getGraphViewStyle().setNumVerticalLabels(11);
		graphView.getGraphViewStyle().setVerticalLabelsWidth(80);
		graphView.setKeepScreenOn(true);
		layout=(RelativeLayout)findViewById(R.id.layout);
		layout.addView(graphView);


	}


	@Override
	protected void onStart() {
		super.onStart();
		Log.d("inStart", "start");
	}


	@Override
	protected void onPause() {
		super.onPause();
		Log.d("inPause", "pause");
		if(stopButtonNotClicked)
			stopButtonNotClicked=false;
		
		graphView.removeAllSeries();
		if(first.getStatus()==AsyncTask.Status.RUNNING){
			first.cancel(true);
		}
		
		db.close();
		c.close();
		btnRunDiagno.setEnabled(true);
		btnStopDiagno.setEnabled(false);
	}




	@Override
	protected void onStop() {
		super.onStop();
		Log.d("inStop", "stop");
		graphView.removeAllSeries();

	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("inDestroy", "destroy");
	}


	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnRunDiagno:
			plotGraph();
			break;
		case R.id.btnStopDiagno:
			clearGraph();
		default:
			break;
		}
	}

	public void plotGraph(){
		btnRunDiagno.setEnabled(false);
		btnStopDiagno.setEnabled(true);
		graph2LastXValue=10;
		new SecondThread().execute();
	}

	public void clearGraph(){
		btnStopDiagno.setEnabled(false);
		btnRunDiagno.setEnabled(true);
		stopButtonNotClicked=false;
		graphView.setViewPort(1, 9);
		graphView.removeAllSeries();

	}

	public ArrayList<Name_ID_Age_Sex> getData(SQLiteDatabase db) {
		ArrayList<Name_ID_Age_Sex> list=new ArrayList<Name_ID_Age_Sex>();
		String []columns= new String[]{"timestamp","x_data","y_data","z_data"};
		c=db.query("Name_Id_Age_Sex", columns, null, null, null, null, null);
		int timestamp=c.getColumnIndex("timestamp");
		int xVal=c.getColumnIndex("x_data");
		int yVal=c.getColumnIndex("y_data");
		int zVal=c.getColumnIndex("z_data");
		c.moveToLast();
		for(int i=0;i<9;i++){
			c.moveToPrevious();
		}
		while(!c.isAfterLast()){
			long ln=c.getLong(timestamp);
			float fl1=c.getFloat(xVal);
			float fl2=c.getFloat(yVal);
			float fl3=c.getFloat(zVal);
			Name_ID_Age_Sex nm=new Name_ID_Age_Sex(ln,fl1,fl2,fl3);
			list.add(nm);
			c.moveToNext();
		}
		return list;
	}	

	protected void createDB(){

		
		Log.d("Ext",DB_PATH);
		
		try{
			db=this.openOrCreateDatabase(DB_PATH+"/AccelDB",MODE_PRIVATE, null);
			db.enableWriteAheadLogging();
			db.beginTransactionNonExclusive();
			try {

					db.execSQL("create table if not exists Name_Id_Age_Sex (" 
							+ " timestamp integer PRIMARY KEY , " 
							+ " x_data real, "
							+ " y_data real, "
							+ " z_data real ); " );

				db.setTransactionSuccessful(); //commit your changes
			}
			catch (SQLiteException e) {

			}
			finally {
				db.endTransaction();
				db.close();
			}		
		}catch (SQLException e){
			Toast.makeText(HealthDiagnosis.this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
	}

	//First Thread : Collect accelerometer data for 10 seconds and add it to the database
	class FirstThread extends AsyncTask<String, Long, Void> implements SensorEventListener{

		@Override
		public void onSensorChanged(SensorEvent sensorEvent) {
			Sensor mySensor = sensorEvent.sensor;

			if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				float acceleration[]=removeGravity(sensorEvent);
				float x = acceleration[0];
				float y = acceleration[1];
				float z = acceleration[2];

				long curTime = System.currentTimeMillis();

				try {
				
					db=HealthDiagnosis.this.openOrCreateDatabase(DB_PATH+"/AccelDB",MODE_PRIVATE, null);
					db.enableWriteAheadLogging();
					db.beginTransactionNonExclusive();
					db.execSQL( "insert into Name_Id_Age_Sex(timestamp, x_data, y_data, z_data) values ("+curTime+", "+x+","+y+","+z+" );" );
					accelManage.unregisterListener(this);
					db.setTransactionSuccessful(); 
				}
				catch (SQLiteException e) {
				}
				finally {
					db.endTransaction();
					db.close();
				}

			}
		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		@Override
		protected Void doInBackground(String... params) {
			accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			senseAccel = accelManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			int sampleCounter=0;
			while(sampleCounter<10){
				accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);
				try {
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				sampleCounter++;
			}
			return null;
		}
		@Override
		protected void onProgressUpdate(Long... values) {
			super.onProgressUpdate(values);		
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			btnRunDiagno.setEnabled(true);
			btnStopDiagno.setEnabled(false);
		}


	}

	//Second Thread : To display data for the 10 seconds
	//periodically update database and reflect it on the graphview every second
	class SecondThread extends AsyncTask<String, Long, Void> implements SensorEventListener{

		@Override
		public void onSensorChanged(SensorEvent sensorEvent) {
			Sensor mySensor = sensorEvent.sensor;

			if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				
				float acceleration[]=removeGravity(sensorEvent);
				float x = acceleration[0];
				float y = acceleration[1];
				float z = acceleration[2];
				
				long curTime = System.currentTimeMillis();

				try {
					db=HealthDiagnosis.this.openOrCreateDatabase(DB_PATH+"/AccelDB",MODE_PRIVATE, null);
					db.enableWriteAheadLogging();
					db.beginTransactionNonExclusive();
					db.execSQL( "insert into Name_Id_Age_Sex(timestamp, x_data, y_data, z_data) values ("+curTime+", "+x+","+y+","+z+" );" );
					accelManage.unregisterListener(this);
					db.setTransactionSuccessful(); 
				}
				catch (SQLiteException e) {
				}
				finally {
					db.endTransaction();
					db.close();
				}
			}
		}
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}


		//display data for the 10 seconds on the graphView
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			try{			
				db=HealthDiagnosis.this.openOrCreateDatabase(DB_PATH+"/AccelDB",MODE_PRIVATE, null);
				db.enableWriteAheadLogging();
				db.beginTransactionNonExclusive();
				try {

					al=getData(db);
					array=new Name_ID_Age_Sex[al.size()];
					al.toArray(array);
				}
				catch (SQLiteException e) {

				}
				finally {
					db.endTransaction();
					
					db.close();
				}		
			}catch (SQLException e){
				Toast.makeText(HealthDiagnosis.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}

			gvd_x=new GraphViewData[array.length];
			gvd_y=new GraphViewData[array.length];
			gvd_z=new GraphViewData[array.length];

			for(int i=0;i<gvd_x.length;i++){
				gvd_x[i]=new GraphViewData(i+1,array[i].getxVal());
				gvd_y[i]=new GraphViewData(i+1,array[i].getyVal());
				gvd_z[i]=new GraphViewData(i+1,array[i].getzVal());
			}
			healthGraphX=new GraphViewSeries("X data",null,gvd_x);
			healthGraphY=new GraphViewSeries("Y data",null,gvd_y);
			healthGraphZ=new GraphViewSeries("Z data",null,gvd_z);

			healthGraphX.getStyle().color = Color.WHITE;
			healthGraphY.getStyle().color = Color.RED;
			healthGraphZ.getStyle().color = Color.CYAN;

			graphView.addSeries(healthGraphX);
			graphView.addSeries(healthGraphY);
			graphView.addSeries(healthGraphZ);

		}

		//Update database every second
		@Override
		protected Void doInBackground(String... params) {
			accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			senseAccel = accelManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			while(stopButtonNotClicked){
				accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);
				publishProgress();
				try {
					Thread.sleep(1000);
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			stopButtonNotClicked=true;
			return null;
		}

		//update the UI every second
		@Override
		protected void onProgressUpdate(Long... values) {
			super.onProgressUpdate(values);	
			graph2LastXValue++;
			//			c.close();

			try{			
				db=HealthDiagnosis.this.openOrCreateDatabase(DB_PATH+"/AccelDB",MODE_PRIVATE, null);
				db.enableWriteAheadLogging();
				db.beginTransactionNonExclusive();
				try {

					String []columns= new String[]{"timestamp","x_data","y_data","z_data"};
					c=db.query("Name_Id_Age_Sex", columns, null, null, null, null, null);
					c.moveToLast();
					healthGraphX.appendData(new GraphViewData(graph2LastXValue, c.getFloat(c.getColumnIndex("x_data"))), true, 10);
					healthGraphY.appendData(new GraphViewData(graph2LastXValue, c.getFloat(c.getColumnIndex("y_data"))), true, 10);
					healthGraphZ.appendData(new GraphViewData(graph2LastXValue, c.getFloat(c.getColumnIndex("z_data"))), true, 10);

				}
				catch (SQLiteException e) {

				}
				finally {
					c.close();
					db.endTransaction();
					db.close();
				}		
			}catch (SQLException e){
				Toast.makeText(HealthDiagnosis.this, e.getMessage(), Toast.LENGTH_LONG).show();
			}

		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}		
	}

	//Method for removing Gravity effects from the accelerometer data
	private float[] removeGravity(SensorEvent event){
		final float alpha = 0.8f;
		
		float linear_acceleration[]=new float[3];

		gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
		gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
		gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];
		
		Log.d("gravity[0]", ""+gravity[0]);
		Log.d("gravity[1]", ""+gravity[1]);
		Log.d("gravity[2]", ""+gravity[2]);

		linear_acceleration[0] = event.values[0] - gravity[0];
		linear_acceleration[1] = event.values[1] - gravity[1];
		linear_acceleration[2] = event.values[2] - gravity[2];

		return linear_acceleration;
	}
}
