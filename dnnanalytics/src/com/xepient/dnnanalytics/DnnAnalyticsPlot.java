package com.xepient.dnnanalytics;

import java.text.DecimalFormat;
import java.util.AbstractCollection;
import java.util.Calendar;
import java.util.LinkedList;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;

import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.androidplot.util.ValPixConverter;
import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;
import com.androidplot.Plot;
import com.example.dnnanalytics.R;


public class DnnAnalyticsPlot implements OnTouchListener {
	
	public interface OnDateRangeChange {
		public void rangeDateChange();		
	}
	
	private static final int TEXT_SIZE = 20;
	private static final int RANGE_LINE_STROKE = 5;
	private static final int NONE = 0;
	private static final int ONE_FINGER_DRAG = 1;
	private static final int TWO_FINGERS_DRAG = 2;
	private static final int TICKS_PER_RANGE_LABEL = 1;
	private static final int TICKS_PER_DOMAIN_LABEL = 2;
	private static final int MIN_Y_GRAPHIC = 0;
	private static final int ZOOM_RANGE = 1;
	private static final int MAX_Y_GRAPHIC = 100;
	private static final int SIZE_PER_NUMBER = 15;
	private static final int ONE_DAY = 1000 * 60 * 60 * 24;
	private int domainLabelWidth = 20;
	private Calendar startDate;
	private Calendar endDate;
	private XYPlot dnnPlot;
	private SimpleXYSeries serie;
    private PointF minXY;
    private PointF maxXY;
    private OnDateRangeChange onDateRangeChange;
    private AbstractCollection<DateValueStruct> dateValueList = new LinkedList<DateValueStruct>();
 	private int mode = NONE;
	private PointF firstFinger;
	private float distBetweenFingers;
	private int maxValue;
	private float minX = 2;
	private float maxX = 31;
	private float scrollControl = 0;
	private boolean actualDate = true;

	public DnnAnalyticsPlot(XYPlot dnnAnalyticsGraphView, AbstractCollection<DateValueStruct> dateValueList,
			OnDateRangeChange onDateTextViewSetListener, Calendar startDate, Calendar endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.onDateRangeChange = onDateTextViewSetListener;
		dnnPlot =  dnnAnalyticsGraphView;
		this.dateValueList = dateValueList;
		initDnnView();
	}
	
	public void setValuesList(AbstractCollection<DateValueStruct> dateValueList) {
		this.dateValueList = dateValueList;
		clearSerie(serie);
		populateSeries(serie, this.dateValueList);
		dnnPlot.redraw();
	}
	
	private void clearSerie(SimpleXYSeries serie) {
		while (serie.size() != 0) {
			serie.removeFirst();
		}
	}
	
	private void populateSeries(SimpleXYSeries series, AbstractCollection<DateValueStruct> dateValueList) {
		maxValue = 0;
		for (int i = 0; i < dateValueList.toArray().length; i++) {
			if (((DateValueStruct) dateValueList.toArray()[i]).getValue() > maxValue) {
				maxValue = ((DateValueStruct) dateValueList.toArray()[i]).getValue();
			}
			series.addLast(i, ((DateValueStruct) dateValueList.toArray()[i]).getValue());
		}
		dnnPlot.getGraphWidget().setRangeLabelWidth(((Integer) maxValue).toString().length() * SIZE_PER_NUMBER);
		dnnPlot.setRangeStep(XYStepMode.INCREMENT_BY_VAL, (maxValue + (100 - (maxValue % 100))) / 5);
		///////////////
		resizeWidth();
		////////////
    }
	
	private void resizeWidth(){
		minXY = new PointF(minX,
        		dnnPlot.getCalculatedMinY().floatValue());
        maxXY = new PointF(maxX,
        		dnnPlot.getCalculatedMaxY().floatValue());
        float domainSpan = maxXY.x - minXY.x;
	    clampToDomainBounds(domainSpan);
	}
	
	private void initDnnView() {
		Resources r = dnnPlot.getResources();
		dnnPlot.setOnTouchListener(this);
		dnnPlot.getGraphWidget().setTicksPerDomainLabel(TICKS_PER_DOMAIN_LABEL);
		dnnPlot.setTicksPerRangeLabel(TICKS_PER_RANGE_LABEL);
		
		dnnPlot.getGraphWidget().getBackgroundPaint().setColor(Color.TRANSPARENT);
		dnnPlot.getGraphWidget().getGridBackgroundPaint().setColor(Color.TRANSPARENT);
		
		//Domain
		dnnPlot.getGraphWidget().getDomainGridLinePaint().setColor(Color.TRANSPARENT);
		dnnPlot.getGraphWidget().getDomainSubGridLinePaint().setColor(Color.TRANSPARENT);
		dnnPlot.getGraphWidget().getDomainLabelPaint().setColor(r.getColor(R.color.graphic_text_color));
		dnnPlot.getGraphWidget().getDomainOriginLinePaint().setColor(Color.TRANSPARENT);
		
		//Range 
		dnnPlot.getGraphWidget().getRangeGridLinePaint().setColor(r.getColor(R.color.limit_color));
		dnnPlot.getGraphWidget().getRangeSubGridLinePaint().setColor(Color.TRANSPARENT);
		dnnPlot.getGraphWidget().getRangeOriginLinePaint().setColor(r.getColor(R.color.limit_color));
		dnnPlot.getGraphWidget().getRangeOriginLabelPaint().setColor(r.getColor(R.color.graphic_text_color));
		dnnPlot.getGraphWidget().getRangeGridLinePaint().setStrokeWidth(RANGE_LINE_STROKE);
		dnnPlot.getGraphWidget().getRangeOriginLinePaint().setStrokeWidth(RANGE_LINE_STROKE);
		
		//Text
		dnnPlot.getLayoutManager().remove(dnnPlot.getLegendWidget());
		dnnPlot.getGraphWidget().getRangeLabelPaint().setTextSize(TEXT_SIZE);
		dnnPlot.getGraphWidget().getDomainLabelPaint().setTextSize(TEXT_SIZE);
		dnnPlot.getGraphWidget().getRangeOriginLabelPaint().setTextSize(TEXT_SIZE);

		dnnPlot.getGraphWidget().setRangeValueFormat(
                new DecimalFormat("####"));
		dnnPlot.getGraphWidget().setDomainValueFormat(new Format() {

	        @Override
	        public StringBuffer format(Object obj, 
	                StringBuffer toAppendTo, 
	                FieldPosition pos) {
	        	
	        	final DateFormat dateFormat;
	            int index = Math.round(((Number) obj).floatValue());
	            Calendar date = Calendar.getInstance();
        		date.set(((DateValueStruct) dateValueList.toArray()[index]).getDate().get(Calendar.YEAR),
        				((DateValueStruct) dateValueList.toArray()[index]).getDate().get(Calendar.MONTH),
        				((DateValueStruct) dateValueList.toArray()[index]).getDate().get(Calendar.DAY_OF_MONTH));
        		if (((endDate.getTimeInMillis() - startDate.getTimeInMillis()) / (1000 * 60 * 60 * 24)) < 31) {
        			if ((date.get(Calendar.DAY_OF_MONTH) >= 1 && date.get(Calendar.DAY_OF_MONTH) <= 4) 
        					|| (date.get(Calendar.DAY_OF_MONTH) >= 27 && date.get(Calendar.DAY_OF_MONTH) <= 31)) {
        				dateFormat = new SimpleDateFormat("dd MMM");
        				return new StringBuffer(dateFormat.format(date.getTime()));
        			} else if (date.get(Calendar.MONTH) == 11 || date.get(Calendar.MONTH) == 0) {
        				dateFormat = new SimpleDateFormat("dd MMM yy");
        				return new StringBuffer(dateFormat.format(date.getTime()));
        			} else {
        				dateFormat = new SimpleDateFormat("dd");
        				return new StringBuffer(dateFormat.format(date.getTime()));
        			}
        		} else {
        			if (date.get(Calendar.MONTH) == 11 || date.get(Calendar.MONTH) == 0) {
        				dateFormat = new SimpleDateFormat("MMM yy");
        				return new StringBuffer(dateFormat.format(date.getTime()));
        			} else {
        				dateFormat = new SimpleDateFormat("MMM");
        				return new StringBuffer(dateFormat.format(date.getTime()));
        			}
        		}  
	        }

			@Override
			public Object parseObject(String string, ParsePosition position) {
				return null;
			}
			
		});
		
		dnnPlot.getGraphWidget().setDomainLabelWidth(domainLabelWidth);
		
		dnnPlot.setRangeLabel("");
		dnnPlot.setDomainLabel("");
		dnnPlot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
		
		startDate.add(Calendar.DATE, -2);
		
		serie = new SimpleXYSeries("Downloads");
		LineAndPointFormatter serieFormat = new LineAndPointFormatter(r.getColor(R.color.graphic_lines),
        		r.getColor(R.color.graphic_lines),
                null,
                null);
		
        populateSeries(serie, dateValueList);        
   
		dnnPlot.addSeries(serie, serieFormat);
		
		dnnPlot.redraw();
		dnnPlot.calculateMinMaxVals();
        minXY = new PointF(minX,
        		dnnPlot.getCalculatedMinY().floatValue());
        maxXY = new PointF(maxX,
        		dnnPlot.getCalculatedMaxY().floatValue());
        dnnPlot.setRangeBoundaries(MIN_Y_GRAPHIC, MAX_Y_GRAPHIC, BoundaryMode.FIXED);
        //dnnPlot.setRangeStep(XYStepMode.SUBDIVIDE, 5);
        endDate.add(Calendar.DATE, 2); // Controll that endDate can be 2 days bigger than actual Date.
        
	
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent event) {
	    switch (event.getAction() & MotionEvent.ACTION_MASK) {
	        case MotionEvent.ACTION_DOWN: // Start gesture // if push
	            firstFinger = new PointF(event.getX(), event.getY()); // position where the screen was touch
	            mode = ONE_FINGER_DRAG;
	            break;
	        case MotionEvent.ACTION_UP:
	        case MotionEvent.ACTION_POINTER_UP: // 
	            mode = NONE;
	            break;
	        case MotionEvent.ACTION_POINTER_DOWN: // second finger // push with second finger
	            distBetweenFingers = spacing(event);
	            // the distance check is done to avoid false alarms
	            if (distBetweenFingers > 5f) { //if distance between fingers is more than 5.0
	                mode = TWO_FINGERS_DRAG;
	            }
	            break;
	        case MotionEvent.ACTION_MOVE: // if it move
	            if (mode == ONE_FINGER_DRAG) {  // only a finger
	                PointF oldFirstFinger = firstFinger; //save first finger position
	                firstFinger = new PointF(event.getX(), event.getY()); //firts finger new position
	                scroll(oldFirstFinger.x - firstFinger.x); //scroll distance between old finger and first
	                dnnPlot.setDomainBoundaries(minXY.x, maxXY.x,
	                        BoundaryMode.FIXED);
	                //dnnPlot.setRangeBoundaries(MIN_Y_GRAPHIC, maxXY.y + 1, BoundaryMode.FIXED);
	                dnnPlot.redraw();

	            } else if (mode == TWO_FINGERS_DRAG) {
	                float oldDist = distBetweenFingers; // save old dist between fingers
	                distBetweenFingers = spacing(event); // new dist = the the distance between finger at the moment
	                if (distBetweenFingers > oldDist) {
		                zoomin(oldDist / distBetweenFingers); // zoom %
	                } else if (distBetweenFingers < oldDist) {
	                	zoomout(oldDist / distBetweenFingers);
	                }

	                dnnPlot.setDomainBoundaries(minXY.x, maxXY.x, // set new limits
	                        BoundaryMode.FIXED);
	               // dnnPlot.setRangeBoundaries(MIN_Y_GRAPHIC, maxXY.y + 1, BoundaryMode.FIXED); // Set Min and Max Limits
	                dnnPlot.redraw();
	            }
	            break;
	    }
	    return true;
	}

	private void zoomin(float scale) {
		float domainSpan = maxXY.x - minXY.x; // distance between points
	    float offset = scale / 10.0f;
	    if (domainSpan > 1) {
	    	minX += offset;
	    	maxX -= offset;
	    	resizeWidth();
	    	scrollControl += Math.abs(offset);
	    	if (scrollControl > 1) {
	    		startDate.add(Calendar.DATE, (int) scrollControl);
	    		endDate.add(Calendar.DATE, -(int) scrollControl);
	    		scrollControl -= (int) scrollControl;
	    		onDateRangeChange.rangeDateChange();
	    	}
	    }
	    //domainSpan = maxXY.x - minXY.x; // distance between points
	    //clampToDomainBounds(domainSpan);
	    dnnPlot.calculateMinMaxVals();
	}
	
	private void zoomout(float scale) {
		/*float domainSpan = maxXY.x - minXY.x; // distance between points
	    float offset = scale / 10.0f;
	    scrollControl += Math.abs(offset);
    	maxX += offset;
	    resizeWidth(); 
	    if (scrollControl > 1) {
	    	startDate.add(Calendar.DATE, -(int) scrollControl);
	    	endDate.add(Calendar.DATE, +(int) scrollControl);
	    	if (!endDate.before(Calendar.getInstance())) {
	    		endDate.add(Calendar.DATE, -(int) scrollControl);
	    	}
	    	scrollControl -= (int) scrollControl;
	    	onDateRangeChange.rangeDateChange();
	    	
	    }
	    dnnPlot.calculateMinMaxVals();*/
	}

	private void scroll(float pan) {
	    float domainSpan = maxXY.x - minXY.x; // distance 
	    float step = domainSpan / dnnPlot.getWidth();
	    float offset = pan * step * 2; // distance between finger * % of increment
	    minXY.x = minXY.x + offset; 
	    maxXY.x = maxXY.x + offset;
	    scrollControl += offset;
	    if (Math.abs(scrollControl) >= 1) {
	    	startDate.add(Calendar.DATE, (int) scrollControl);
	    	endDate.add(Calendar.DATE, (int) scrollControl);
	    	if (!endDate.before(Calendar.getInstance())) {
	    		startDate.add(Calendar.DATE, -(int) scrollControl);
	        	endDate.add(Calendar.DATE, -(int) scrollControl);
	    	} else {
	    		onDateRangeChange.rangeDateChange();
	    	}
	    	scrollControl -= (int) scrollControl;
	    }
	    
	    clampToDomainBounds(domainSpan);
	    //dnnPlot.calculateMinMaxVals();
	}  
	
	private void clampToDomainBounds(float domainSpan) { // check that the new values are between the limits,
		//if they are�t between the limits, set them to default values
		float leftBoundary = serie.getX(0).floatValue(); // serie left limit
		float rightBoundary = serie.getX(serie.size() - 1).floatValue(); // serie right limit
		// enforce left scroll boundary:
		if (minXY.x < leftBoundary) {
			minXY.x = leftBoundary; // if minXY.x is lower then the left limit do ...
			maxXY.x = leftBoundary + domainSpan; // and max limit
		} else if (maxXY.x > serie.getX(serie.size() - 1).floatValue()) { 
			maxXY.x = rightBoundary;
			minXY.x = rightBoundary - domainSpan;
		}
	}

	private float spacing(MotionEvent event) { //calculate distance between two points
	    float x = event.getX(0) - event.getX(1);
	    float y = event.getY(0) - event.getY(1);
	    return FloatMath.sqrt(x * x + y * y);
	}	
}