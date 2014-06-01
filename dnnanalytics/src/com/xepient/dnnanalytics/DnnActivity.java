package com.xepient.dnnanalytics;

import java.text.DateFormat;
import java.util.Calendar;

import com.androidplot.xy.XYPlot;
import com.example.dnnanalytics.R;
import com.xepient.dnnanalytics.DatePickerFragment.OnNewDateSet;
import com.xepient.dnnanalytics.DnnAnalyticsPlot.OnDateRangeChange;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.support.v4.app.FragmentActivity;

public class DnnActivity extends FragmentActivity {
	
	private Calendar startDate;
	private Calendar endDate;
	private TextView textViewToSet;
	private DateFormat dateFormat;
	private AnalyticsController controller;
	private OnNewDateSet startDateListener;
	private OnNewDateSet endDateListener;
	private OnDateRangeChange onDateRangeChange;
	private DnnAnalyticsPlot dnnAnalyticsGraphic;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

    	// capture our View elements
        ImageButton startPickDateButton = (ImageButton) findViewById(R.id.pickDate);
        ImageButton endPickDateButton = (ImageButton) findViewById(R.id.pickDate1);
        
        dateFormat = DateFormat.getDateInstance();
	        
        // set start and end date to the current time
        endDate = Calendar.getInstance();
        startDate = endDate.getInstance();
        startDate.set(endDate.get(Calendar.YEAR), 
				endDate.get(Calendar.MONTH),
				endDate.get(Calendar.DAY_OF_MONTH));
        startDate.add(Calendar.MONTH, -1);
        
        controller = new AnalyticsController(startDate, endDate);
        
        onDateRangeChange = new OnDateRangeChange() {
			
			@Override
			public void rangeDateChange() {
				textViewToSet = (TextView) findViewById(R.id.StartPickDate);
		        textViewToSet.setText(dateFormat.format(startDate.getTime()));
		        textViewToSet = (TextView) findViewById(R.id.EndPickDate);
		        textViewToSet.setText(dateFormat.format(endDate.getTime()));
				dnnAnalyticsGraphic.setValuesList(controller.getValues(startDate, endDate));
			}
        };
        
        textViewToSet = (TextView) findViewById(R.id.StartPickDate);
        textViewToSet.setText(dateFormat.format(startDate.getTime()));
        textViewToSet = (TextView) findViewById(R.id.EndPickDate);
        textViewToSet.setText(dateFormat.format(endDate.getTime()));
        
        dnnAnalyticsGraphic = new DnnAnalyticsPlot((XYPlot) findViewById(R.id.mySimpleXYPlot),
        		controller.getValues(startDate, endDate), onDateRangeChange, startDate, endDate);
		
        // add a click listener to the button
        startPickDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showStartDatePicker();		
			}
		});
        
        endPickDateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showEndDatePicker();		
			}
		});      
        
        startDateListener = new OnNewDateSet() {
			
			@Override
			public void onDateSet(Calendar date) {
				startDate.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
						date.get(Calendar.DAY_OF_MONTH));
				dnnAnalyticsGraphic.setValuesList(controller.getValues(startDate, endDate));  	
			  	textViewToSet.setText(dateFormat.format(date.getTime()));
			}
		};	
		endDateListener = new OnNewDateSet() {
			
			@Override
			public void onDateSet(Calendar date) {
				endDate.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH),
						date.get(Calendar.DAY_OF_MONTH));
				dnnAnalyticsGraphic.setValuesList(controller.getValues(startDate, endDate));  	
			  	textViewToSet.setText(dateFormat.format(date.getTime()));
			}
		};
    }
	
	private void showStartDatePicker() {
		DatePickerFragment date = DatePickerFragment.newInstance(startDate, endDate, null);
		textViewToSet = (TextView) findViewById(R.id.StartPickDate);
		date.setOnNewDateSet(startDateListener);
		date.show(getSupportFragmentManager(), "Start Date Picker");
	
	}
	
	private void showEndDatePicker() {
		DatePickerFragment date = DatePickerFragment.newInstance(endDate, Calendar.getInstance(), startDate);
		textViewToSet = (TextView) findViewById(R.id.EndPickDate);
		date.setOnNewDateSet(endDateListener);
		date.show(getSupportFragmentManager(), "End Date Picker");
	}
   
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	getMenuInflater().inflate(R.menu.dnn, menu);
    	return true;
    }
}
