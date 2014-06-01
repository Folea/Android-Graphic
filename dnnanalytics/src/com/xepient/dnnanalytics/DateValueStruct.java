package com.xepient.dnnanalytics;

import java.util.Calendar;

public class DateValueStruct {
	private Calendar date;
	private int value;
	
	public DateValueStruct(Calendar date, int value) {
		this.date = Calendar.getInstance();
		this.date.set(date.get(Calendar.YEAR), 
				date.get(Calendar.MONTH),
				date.get(Calendar.DAY_OF_MONTH));
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public Calendar getDate() {
		return date;
	}
}
