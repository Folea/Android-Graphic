package com.xepient.dnnanalytics;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.TextView;


public class DatePickerFragment extends DialogFragment {
	
	private OnNewDateSet dateSetListener;
	
	public interface OnNewDateSet {
		public void onDateSet(Calendar date);
	}
 
	public static DatePickerFragment newInstance(Calendar date, Calendar maxDate, Calendar minDate){
		DatePickerFragment datePickerFragment = new DatePickerFragment();
		
		Bundle args = new Bundle();
		args.putInt("dateYear", date.get(Calendar.YEAR));
		args.putInt("dateMonth", date.get(Calendar.MONTH));
		args.putInt("dateDay", date.get(Calendar.DAY_OF_MONTH));
		if (maxDate != null) {
			args.putInt("MaxYear", maxDate.get(Calendar.YEAR));
			args.putInt("MaxMonth", maxDate.get(Calendar.MONTH));
			args.putInt("MaxDay", maxDate.get(Calendar.DAY_OF_MONTH));
		}
		if (minDate != null) {
			args.putInt("MinYear", minDate.get(Calendar.YEAR));
			args.putInt("MinMonth", minDate.get(Calendar.MONTH));
			args.putInt("MinDay", minDate.get(Calendar.DAY_OF_MONTH));
		}		
		datePickerFragment.setArguments(args);
		return datePickerFragment;	
	}
	
	public void setOnNewDateSet(OnNewDateSet dateSetListener){
		this.dateSetListener = dateSetListener;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {		
		final DatePickerDialog dateDialog = new DatePickerDialog(getActivity(), null,
				getArguments().getInt("dateYear"),
				getArguments().getInt("dateMonth"),
				getArguments().getInt("dateDay"));
		dateDialog.setCancelable(true);	
		
		
		if (getArguments().containsKey("MaxYear")) {
			Calendar maxDate = Calendar.getInstance();
			maxDate.set(getArguments().getInt("MaxYear"),
					getArguments().getInt("MaxMonth"),
					getArguments().getInt("MaxDay"));
			dateDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
		}	
		
		if (getArguments().containsKey("MinYear")) {
			Calendar minDate = Calendar.getInstance();
			minDate.set(getArguments().getInt("MinYear"),
					getArguments().getInt("MinMonth"),
					getArguments().getInt("MinDay"));
			dateDialog.getDatePicker().setMinDate(minDate.getTimeInMillis());
		}	
				
		dateDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Calendar date = Calendar.getInstance();
				date.set(dateDialog.getDatePicker().getYear(),
						dateDialog.getDatePicker().getMonth(),
						dateDialog.getDatePicker().getDayOfMonth());
				dateSetListener.onDateSet(date);
			}
		});
		
		dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new OnClickListener() {		
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dateDialog.dismiss();
			}
		});	
		
		return dateDialog;	
	}	
} 
