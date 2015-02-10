package com.example.healthtrack;

public class Name_ID_Age_Sex {

	long timestamp;
	double xVal;
	double yVal;
	double zVal;
	
	public Name_ID_Age_Sex(long timestamp,double xVal, double yVal, double zVal) {
		super();
		this.timestamp=timestamp;
		this.xVal = xVal;
		this.yVal = yVal;
		this.zVal = zVal;
	}
	
	public double getTimeStamp() {
		return timestamp;
	}
	public void setTimeStamp(long timeStamp) {
		this.timestamp = timeStamp;
	}
	public double getxVal() {
		return xVal;
	}
	public void setxVal(double xVal) {
		this.xVal = xVal;
	}
	public double getyVal() {
		return yVal;
	}
	public void setyVal(double yVal) {
		this.yVal = yVal;
	}
	public double getzVal() {
		return zVal;
	}
	public void setzVal(double zVal) {
		this.zVal = zVal;
	}
	
	
	
}
