package com.example.healthtrack;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

public class HealthMonitoring extends Activity implements OnClickListener{

	Button btnStartHealthDiagno,btnStartDiagnoContinousWave;
	EditText etPatientID,etPatientAge,etPatientName;
	RadioGroup rgSex;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.healthmonitoring);
		btnStartHealthDiagno=(Button)findViewById(R.id.btnStartDiagnosisActivity);
		btnStartDiagnoContinousWave=(Button)findViewById(R.id.btnContinuousWave);
		
		btnStartDiagnoContinousWave.setVisibility(View.INVISIBLE);
		
		etPatientID=(EditText)findViewById(R.id.etPatientID);
		etPatientAge=(EditText)findViewById(R.id.etPatientAge);
		etPatientName=(EditText)findViewById(R.id.etPatientName);
		rgSex=(RadioGroup)findViewById(R.id.rgSex);
		
		btnStartHealthDiagno.setOnClickListener(this);
		btnStartDiagnoContinousWave.setOnClickListener(this);
		
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnStartDiagnosisActivity:
			startDiagnosisActivity();	
			break;
		case R.id.btnContinuousWave:
			startDiagnoSineWave();	
			break;
	
		default:
			break;
		}
	}
	
	public void startDiagnosisActivity(){
		if(!etPatientName.getText().toString().matches("") && !etPatientAge.getText().toString().matches("") && !etPatientID.getText().toString().matches("") && rgSex.getCheckedRadioButtonId()!=-1){
			Intent intent=new Intent(this,HealthDiagnosis.class);
			startActivity(intent);
		}
		else{
			Toast.makeText(this, "All the fields are necessary", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	public void startDiagnoSineWave(){
		if(!etPatientName.getText().toString().matches("") && !etPatientAge.getText().toString().matches("") && !etPatientID.getText().toString().matches("") && rgSex.getCheckedRadioButtonId()!=-1){
			Intent intent=new Intent(this,ContinuousWaves.class);
			startActivity(intent);
		}
		else{
			Toast.makeText(this, "All the fields are necessary", Toast.LENGTH_SHORT).show();
		}
		
	}

	
}
