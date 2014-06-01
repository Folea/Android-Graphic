package com.xepient.dnnanalytics;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractCollection;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;


public class AnalyticsController {

	private HashMap<String, Integer> valuesMap = new HashMap<String, Integer>();
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	
	public AnalyticsController(Calendar startDate, Calendar endDate) {
		putElementsValuesList(startDate, endDate);
	}
	
	private void putElementsValuesList(final Calendar startDate,
			final Calendar endDate) {
		Calendar date = Calendar.getInstance();
		date.set(startDate.get(Calendar.YEAR), 
				startDate.get(Calendar.MONTH),
				startDate.get(Calendar.DAY_OF_MONTH));
		while (date.before(endDate)) {
			valuesMap.put(dateFormat.format(date.getTime()), 0 + (int) (Math.random() * 100));
			date.add(Calendar.DATE, 1);
		} 
		valuesMap.size();
	}
	
	private void addNewValuesToList(Calendar startDate, Calendar endDate) {
		Calendar date = Calendar.getInstance();
		if (!valuesMap.containsKey(dateFormat.format(startDate.getTime()))) {
			date.set(startDate.get(Calendar.YEAR),
					startDate.get(Calendar.MONTH),
					startDate.get(Calendar.DAY_OF_MONTH));
			while (!valuesMap.containsKey(dateFormat.format(date.getTime()))) {
				valuesMap.put(dateFormat.format(date.getTime()), 0 + (int) (Math.random() * 100));
				date.add(Calendar.DATE, 1);
			}
		}
		if (!valuesMap.containsKey(dateFormat.format(endDate.getTime()))) {
			date.set(endDate.get(Calendar.YEAR),
					endDate.get(Calendar.MONTH),
					endDate.get(Calendar.DAY_OF_MONTH));
			date.set(Calendar.MILLISECOND, 0);
			while (!valuesMap.containsKey(dateFormat.format(date.getTime()))) {
				valuesMap.put(dateFormat.format(date.getTime()), 0 + (int) (Math.random() * 100));
				date.add(Calendar.DATE, -1);
			}
		}
	}
	
	private LinkedList<DateValueStruct> newLinkedList(HashMap<String, Integer> valuesMap, Calendar start,
			Calendar end) {
		LinkedList<DateValueStruct> newLinkedList = new LinkedList<DateValueStruct>();
		Calendar date = Calendar.getInstance();
		date.set(start.get(Calendar.YEAR),
				start.get(Calendar.MONTH),
				start.get(Calendar.DAY_OF_MONTH));
		while (date.before(end)) {
			if (valuesMap.containsKey(dateFormat.format(date.getTime()))) {
				newLinkedList.addLast(new DateValueStruct(date, valuesMap.get(dateFormat.format(date.getTime()))));
				date.add(Calendar.DATE, 1);
			}		
		}
		return newLinkedList;
	}
	
	public AbstractCollection<DateValueStruct> getValues(Calendar startDate, Calendar endDate) {
		addNewValuesToList(startDate, endDate);
		return newLinkedList(valuesMap, startDate, endDate);
	}
}
