/*
 * SlipLineChart.java
 * Android-Charts
 *
 * Created by limc on 2014.
 *
 * Copyright 2011 limc.cn All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.limc.androidcharts.view;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import cn.limc.androidcharts.entity.DateValueEntity;
import cn.limc.androidcharts.entity.LineEntity;
import cn.limc.androidcharts.event.IGestureDetector;
import cn.limc.androidcharts.event.ISlipable;
import cn.limc.androidcharts.event.IZoomable;
import cn.limc.androidcharts.event.OnSlipGestureListener;
import cn.limc.androidcharts.event.OnZoomGestureListener;
import cn.limc.androidcharts.event.SlipGestureDetector;

/**
 * <p>
 * en
 * </p>
 * <p>
 * jp
 * </p>
 * <p>
 * cn
 * </p>
 * 
 * @author limc
 * @version v1.0 2014/01/21 14:20:35
 * 
 */
public class SlipLineChart extends GridChart implements IZoomable, ISlipable {

	public static final int DEFAULT_LINE_ALIGN_TYPE = ALIGN_TYPE_CENTER;

	public static final int DEFAULT_DISPLAY_FROM = 0;
	public static final int DEFAULT_DISPLAY_NUMBER = 50;
	public static final int DEFAULT_MIN_DISPLAY_NUMBER = 20;
	public static final int DEFAULT_ZOOM_BASE_LINE = ZOOM_BASE_LINE_CENTER;

	protected int displayFrom = DEFAULT_DISPLAY_FROM;
	protected int displayNumber = DEFAULT_DISPLAY_NUMBER;
	protected int minDisplayNumber = DEFAULT_MIN_DISPLAY_NUMBER;
	protected int zoomBaseLine = DEFAULT_ZOOM_BASE_LINE;

	/**
	 * <p>
	 * data to draw lines
	 * </p>
	 * <p>
	 * ラインを書く用データ
	 * </p>
	 * <p>
	 * 绘制线条用的数据
	 * </p>
	 */
	protected List<LineEntity<DateValueEntity>> linesData;

	/**
	 * <p>
	 * min value of Y axis
	 * </p>
	 * <p>
	 * F Y軸の最小値
	 * </p>
	 * <p>
	 * Y的最小表示值
	 * </p>
	 */
	protected double minValue;

	/**
	 * <p>
	 * max value of Y axis
	 * </p>
	 * <p>
	 * Y軸の最大値
	 * </p>
	 * <p>
	 * Y的最大表示值
	 * </p>
	 */
	protected double maxValue;

	protected int lineAlignType = DEFAULT_LINE_ALIGN_TYPE;

	protected OnZoomGestureListener onZoomGestureListener = new OnZoomGestureListener();
	protected OnSlipGestureListener onSlipGestureListener = new OnSlipGestureListener();

	protected IGestureDetector slipGestureDetector = new SlipGestureDetector<ISlipable>(this);
	protected OnLongClickListener longClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			Log.i("info", "66666666666666666666666666");
			setDisplayCrossXOnTouch(true);
			setDisplayCrossYOnTouch(true);
			slipGestureDetector.onTouchEvent(event);
			return true;
		}
	};

	private int currentIndex;

	/*
	 * (non-Javadoc)
	 * 
	 * @param context
	 * 
	 * @see cn.limc.androidcharts.view.GridChart#GridChart(Context)
	 */
	public SlipLineChart(Context context) {
		super(context);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @param context
	 * 
	 * @param attrs
	 * 
	 * @param defStyle
	 * 
	 * @see cn.limc.androidcharts.view.GridChart#GridChart(Context,
	 * AttributeSet, int)
	 */
	public SlipLineChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @param context
	 * 
	 * @param attrs
	 * 
	 * 
	 * 
	 * @see cn.limc.androidcharts.view.GridChart#GridChart(Context,
	 * AttributeSet)
	 */
	public SlipLineChart(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected void calcDataValueRange() {
		double maxValue = Double.MIN_VALUE;
		double minValue = Double.MAX_VALUE;
		// 逐条输出MA线
		for (int i = 0; i < this.linesData.size(); i++) {
			LineEntity<DateValueEntity> line = this.linesData.get(i);
			if (line != null && line.getLineData().size() > 0) {
				// 判断显示为方柱或显示为线条
				for (int j = displayFrom; j < displayFrom + displayDataSize; j++) {
					DateValueEntity lineData = line.getLineData().get(j);
					if (lineData.getValue() < minValue) {
						minValue = lineData.getValue();
					}
					if (lineData.getValue() > maxValue) {
						maxValue = lineData.getValue();
					}
				}
			}
		}

		this.maxValue = maxValue;
		this.minValue = minValue;
	}

	// 计算值的范围
	protected void calcValueRangePaddingZero() {
		double maxValue = this.maxValue;
		double minValue = this.minValue;

		if (maxValue > minValue) {
			// if ((maxValue - minValue) < 10. && minValue > 1.) {
			// this.maxValue = (long) (maxValue + 1);
			// this.minValue = (long) (minValue - 1);
			// } else {
			// this.maxValue = (long) (maxValue + (maxValue - minValue) * 0.1);
			// this.minValue = (long) (minValue - (maxValue - minValue) * 0.1);
			//
			// if (this.minValue < 0) {
			// this.minValue = 0;
			// }
			// }
			this.maxValue = maxValue;
			this.minValue = minValue;
		} else if ((long) maxValue == (long) minValue) {
			if (maxValue <= 10 && maxValue > 1) {
				this.maxValue = maxValue + 1;
				this.minValue = minValue - 1;
			} else if (maxValue <= 100 && maxValue > 10) {
				this.maxValue = maxValue + 10;
				this.minValue = minValue - 10;
			} else if (maxValue <= 1000 && maxValue > 100) {
				this.maxValue = maxValue + 100;
				this.minValue = minValue - 100;
			} else if (maxValue <= 10000 && maxValue > 1000) {
				this.maxValue = maxValue + 1000;
				this.minValue = minValue - 1000;
			} else if (maxValue <= 100000 && maxValue > 10000) {
				this.maxValue = maxValue + 10000;
				this.minValue = minValue - 10000;
			} else if (maxValue <= 1000000 && maxValue > 100000) {
				this.maxValue = maxValue + 100000;
				this.minValue = minValue - 100000;
			} else if (maxValue <= 10000000 && maxValue > 1000000) {
				this.maxValue = maxValue + 1000000;
				this.minValue = minValue - 1000000;
			} else if (maxValue <= 100000000 && maxValue > 10000000) {
				this.maxValue = maxValue + 10000000;
				this.minValue = minValue - 10000000;
			}
		} else {
			this.maxValue = 0;
			this.minValue = 0;
		}
	}

	protected void calcValueRangeFormatForAxis() {
		int rate = 1;

		if (this.maxValue < 3000) {
			rate = 1;
		} else if (this.maxValue >= 3000 && this.maxValue < 5000) {
			rate = 5;
		} else if (this.maxValue >= 5000 && this.maxValue < 30000) {
			rate = 10;
		} else if (this.maxValue >= 30000 && this.maxValue < 50000) {
			rate = 50;
		} else if (this.maxValue >= 50000 && this.maxValue < 300000) {
			rate = 100;
		} else if (this.maxValue >= 300000 && this.maxValue < 500000) {
			rate = 500;
		} else if (this.maxValue >= 500000 && this.maxValue < 3000000) {
			rate = 1000;
		} else if (this.maxValue >= 3000000 && this.maxValue < 5000000) {
			rate = 5000;
		} else if (this.maxValue >= 5000000 && this.maxValue < 30000000) {
			rate = 10000;
		} else if (this.maxValue >= 30000000 && this.maxValue < 50000000) {
			rate = 50000;
		} else {
			rate = 100000;
		}

		// 等分轴修正
		if (this.latitudeNum > 0 && rate > 1 && (long) (this.minValue) % rate != 0) {
			// 最大值加上轴差
			this.minValue = (long) this.minValue - ((long) (this.minValue) % rate);
		}
		// 等分轴修正
		if (this.latitudeNum > 0 && (long) (this.maxValue - this.minValue) % (this.latitudeNum * rate) != 0) {
			// 最大值加上轴差
			this.maxValue = (long) this.maxValue + (this.latitudeNum * rate) - ((long) (this.maxValue - this.minValue) % (this.latitudeNum * rate));
		}
	}

	protected void calcValueRange() {
		if (null == this.linesData) {
			this.maxValue = 0;
			this.minValue = 0;
			return;
		}
		if (this.linesData.size() > 0) {
			this.calcDataValueRange();
			this.calcValueRangePaddingZero();
		} else {
			this.maxValue = 0;
			this.minValue = 0;
		}
		this.calcValueRangeFormatForAxis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * <p>Called when is going to draw this chart<p> <p>チャートを書く前、メソッドを呼ぶ<p>
	 * <p>绘制图表时调用<p>
	 * 
	 * @param canvas
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		initAxisY();
		initAxisX();
		super.onDraw(canvas);
		// draw lines
		if (null != this.linesData) {
			drawLines(canvas);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @param value
	 * 
	 * @see cn.limc.androidcharts.view.GridChart#getAxisXGraduate(Object)
	 */
	@Override
	public String getAxisXGraduate(Object value) {
		float graduate = Float.valueOf(super.getAxisXGraduate(value));
		int index = (int) Math.floor(graduate * displayNumber);
		this.currentIndex = index;
		if (index >= displayNumber) {
			index = displayNumber - 1;
		} else if (index < 0) {
			index = 0;
		}
		// if (index < linesData.size()) {
		// return "";
		// }
		index = index + displayFrom;
		if (null == this.linesData) {
			return "";
		}
		LineEntity<DateValueEntity> line = (LineEntity<DateValueEntity>) linesData.get(0);
		if (line == null) {
			return "";
		}
		if (line.isDisplay() == false) {
			return "";
		}
		List<DateValueEntity> lineData = line.getLineData();
		if (lineData == null) {
			return "";
		}
		if (index > lineData.size() - 1) {
			return "";
		}
		return String.valueOf(lineData.get(index).getDate());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @param value
	 * 
	 * @see cn.limc.androidcharts.view.GridChart#getAxisYGraduate(Object)
	 */
	@Override
	public String getAxisYGraduate(Object value) {
		// float graduate = Float.valueOf(super.getAxisYGraduate(value));
		// return String.valueOf(Math.floor(graduate * (maxValue - minValue) +
		// minValue));
		Log.i("info", "Line.getAxisYGraduate");
		return getGraduate(value);
	}

	private String getGraduate(Object value) {
		float valueLength = ((Float) value).floatValue() - dataQuadrant.getQuadrantPaddingStartY();
		double pointPrice = ((valueLength - this.dataQuadrant.getQuadrantPaddingHeight() / 2) / this.dataQuadrant.getQuadrantPaddingHeight() / 2)
				* maxChangPrice + closingPrice;
		BigDecimal bd = new BigDecimal(pointPrice);
		bd.setScale(2, RoundingMode.HALF_UP);
		return Double.toString(bd.doubleValue());
	}

	@Override
	public float getCrossYValue(Object value) {
		// 首先获得了crossline中纵线的位置.
		LineEntity<DateValueEntity> line = (LineEntity<DateValueEntity>) linesData.get(0);
		if (line != null && line.isDisplay()) {
			List<DateValueEntity> lineData = line.getLineData();
			if (lineData != null) {
				if (currentIndex <= lineData.size() - 1) {
					return lineData.get(currentIndex).getValue();
				}
			}
		}
		return -1;
	}

	@Override
	public float getAvgPrice(int value) {
		LineEntity<DateValueEntity> line = (LineEntity<DateValueEntity>) linesData.get(1);
		List<DateValueEntity> lineData = line.getLineData();
		if (lineData != null) {
			if (currentIndex < lineData.size()) {
				return lineData.get(currentIndex).getValue();
			}
		}
		return -1;
	}

	@Override
	public float getCrossYPostion(float value) {
		float yValue = getCrossYValue(value);
		float valueLength = dataQuadrant.getQuadrantPaddingHeight() / 2
				- (float) (((yValue - closingPrice) / maxChangPrice) * (dataQuadrant.getQuadrantPaddingHeight() / 2)) + dataQuadrant.getQuadrantPaddingStartY();
		return valueLength;
	}

	public float longitudePostOffset() {
		if (lineAlignType == ALIGN_TYPE_CENTER) {
			float lineLength = dataQuadrant.getQuadrantPaddingWidth() / displayNumber;
			return (this.dataQuadrant.getQuadrantPaddingWidth() - lineLength) / (longitudeTitles.size() - 1);
		} else {
			return this.dataQuadrant.getQuadrantPaddingWidth() / (longitudeTitles.size() - 1);
		}
	}

	public float longitudeOffset() {
		if (lineAlignType == ALIGN_TYPE_CENTER) {
			float lineLength = dataQuadrant.getQuadrantPaddingWidth() / displayNumber;
			return dataQuadrant.getQuadrantPaddingStartX() + lineLength / 2;
		} else {
			return dataQuadrant.getQuadrantPaddingStartX();
		}
	}

	/**
	 * <p>
	 * initialize degrees on Y axis
	 * </p>
	 * <p>
	 * Y軸の目盛を初期化
	 * </p>
	 * <p>
	 * 初始化Y轴的坐标值
	 * </p>
	 */
	protected void initAxisY() {
		// 注释掉从数据中计算最大值和最小值
		// this.calcValueRange();
		if (isHasTitlesBothSides()) {
			List<String> rightTitles = initRightYAxisTitles();
			setOtherSideLatitudeTitles(rightTitles);
		}
		List<String> titleY = initYAxisTitle();
		super.setLatitudeTitles(titleY);
	}

	private List<String> initRightYAxisTitles() {
		List<String> titles = new ArrayList<String>();
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i <= this.getLatitudeNum(); i++) {
			if (i == 0) {
				double changePer = maxChangPrice / closingPrice;
				builder.append("-").append(String.format("%.2f", changePer * 100d)).append("%");
				titles.add(builder.toString());
				builder.setLength(0);
			} else if (i == 2) {
				builder.append("0.00").append("%");
				titles.add(builder.toString());
				builder.setLength(0);
			} else if (i == 4) {
				double changePer = maxChangPrice / closingPrice;
				builder.append(String.format("%.2f", changePer * 100d)).append("%");
				titles.add(builder.toString());
				builder.setLength(0);
			} else if (i == 1) {
				double changePer = maxChangPrice / (closingPrice * 2);
				builder.append("-").append(String.format("%.2f", changePer * 100d)).append("%");
				titles.add(builder.toString());
				builder.setLength(0);
			} else {
				double changePer = maxChangPrice / (closingPrice * 2);
				builder.append(String.format("%.2f", changePer * 100d)).append("%");
				titles.add(builder.toString());
				builder.setLength(0);
			}
		}
		return titles;
	}

	private List<String> initYAxisTitle() {
		List<String> titleY = new ArrayList<String>();
		String yPrice = null;
		for (int i = 0; i <= this.getLatitudeNum(); i++) {
			if (i == 0) {
				double price = Double.parseDouble(String.format("%.2f", closingPrice - maxChangPrice));
				titleY.add(Double.toString(price));
			} else if (i == 2) {
				titleY.add(String.format("%.2f", closingPrice));
			} else if (i == 4) {
				double price = closingPrice + maxChangPrice;
				titleY.add(String.format("%.2f", price));
			} else if (i == 1) {
				titleY.add(String.format("%.2f", closingPrice - maxChangPrice / 2));
			} else {
				titleY.add(String.format("%.2f", closingPrice + maxChangPrice / 2));
			}
		}
		return titleY;
	}

	private void rawMethodTitleY(List<String> titleY) {
		float average = (int) ((maxValue - minValue) / this.getLatitudeNum());
		// calculate degrees on Y axis
		for (int i = 0; i < this.getLatitudeNum(); i++) {
			String value = String.valueOf((int) Math.floor(minValue + i * average));
			if (value.length() < super.getLatitudeMaxTitleLength()) {
				while (value.length() < super.getLatitudeMaxTitleLength()) {
					value = " " + value;
				}
			}
			titleY.add(value);
		}
		// calculate last degrees by use max value
		String value = String.valueOf((int) Math.floor(((int) maxValue)));
		if (value.length() < super.getLatitudeMaxTitleLength()) {
			while (value.length() < super.getLatitudeMaxTitleLength()) {
				value = " " + value;
			}
		}
		titleY.add(value);
	}

	/**
	 * <p>
	 * initialize degrees on Y axis
	 * </p>
	 * <p>
	 * Y軸の目盛を初期化
	 * </p>
	 * <p>
	 * 初始化Y轴的坐标值
	 * </p>
	 */
	protected void initAxisX() {
		ArrayList<String> titleX = initXAxisDate();
		// rawMethod(titleX);
		super.setLongitudeTitles(titleX);
	}

	private void rawMethod(ArrayList<String> titleX) {
		if (null != linesData && linesData.size() > 0) {
			float average = displayNumber / this.getLongitudeNum();
			for (int i = 0; i < this.getLongitudeNum(); i++) {
				int index = (int) Math.floor(i * average);
				if (index > displayNumber - 1) {
					index = displayNumber - 1;
				}
				index = index + displayFrom;
				// title x只能显示当前时间之前 若要修改,还需修改
				if (index < displayDataSize) {
					titleX.add(String.valueOf(linesData.get(0).getLineData().get(index).getDate()));
				}
				// if
				// (String.valueOf(linesData.get(0).getLineData().get(index).getDate()).length()
				// >= 4) {
				// titleX.add(String.valueOf(linesData.get(0).getLineData().get(index).getDate()).substring(4));
				// } else if
				// (String.valueOf(linesData.get(0).getLineData().get(index).getDate()).length()
				// == 3) {
				// titleX.add(String.valueOf(linesData.get(0).getLineData().get(index).getDate()).substring(3));
				// } else if
				// (String.valueOf(linesData.get(0).getLineData().get(index).getDate()).length()
				// == 2) {
				// titleX.add(String.valueOf(linesData.get(0).getLineData().get(index).getDate()).substring(2));
				// } else if
				// (String.valueOf(linesData.get(0).getLineData().get(index).getDate()).length()
				// == 1) {
				// titleX.add(String.valueOf(linesData.get(0).getLineData().get(index).getDate()).substring(1));
				// } else if
				// (String.valueOf(linesData.get(0).getLineData().get(index).getDate()).length()
				// == 1) {
				// titleX.add(String.valueOf(linesData.get(0).getLineData().get(index).getDate()));
				// }
			}
			titleX.add(String.valueOf(linesData.get(0).getLineData().get(displayFrom + displayDataSize - 1).getDate()));
		}
	}

	private ArrayList<String> initXAxisDate() {
		ArrayList<String> titleX = new ArrayList<String>();
		String xAxisDate = null;
		for (int i = 0; i <= this.getLongitudeNum(); i++) {
			if (i == 0) {
				xAxisDate = "09:30";
			} else if (i == 1) {
				xAxisDate = "10:30";
			} else if (i == 2) {
				xAxisDate = "11:30/13:00";
			} else if (i == 3) {
				xAxisDate = "14:00";
			} else if (i == 4) {
				xAxisDate = "15:00";
			}
			titleX.add(xAxisDate);
		}
		return titleX;
	}

	/**
	 * <p>
	 * draw lines
	 * </p>
	 * <p>
	 * ラインを書く
	 * </p>
	 * <p>
	 * 绘制线条
	 * </p>
	 * 
	 * @param canvas
	 */
	protected void drawLines(Canvas canvas) {
		if (null == this.linesData) {
			return;
		}
		// distance between two points
		float lineLength;
		// start point‘s X
		float startX;

		// draw lines
		for (int i = 0; i < linesData.size(); i++) {
			LineEntity<DateValueEntity> line = (LineEntity<DateValueEntity>) linesData.get(i);
			if (line == null) {
				continue;
			}
			if (line.isDisplay() == false) {
				continue;
			}
			List<DateValueEntity> lineData = line.getLineData();
			if (lineData == null) {
				continue;
			}
			Paint mPaint = new Paint();
			mPaint.setColor(line.getLineColor());
			mPaint.setAntiAlias(true);
			// set start point’s X
			if (lineAlignType == ALIGN_TYPE_CENTER) {
				lineLength = (dataQuadrant.getQuadrantPaddingWidth() / displayNumber);
				startX = dataQuadrant.getQuadrantPaddingStartX() + lineLength / 2;
			} else {
				lineLength = (dataQuadrant.getQuadrantPaddingWidth() / (displayNumber - 1));
				startX = dataQuadrant.getQuadrantPaddingStartX();
			}
			// start point
			PointF ptFirst = null;
			float valueY = dataQuadrant.getQuadrantPaddingHeight() - dataQuadrant.getQuadrantStartY();
			for (int j = displayFrom; j < displayFrom + lineData.size(); j++) {
				if (lineData.get(j) == null) {
					startX = startX + lineLength;
					ptFirst = new PointF(startX, valueY);
					canvas.drawLine(ptFirst.x, ptFirst.y, startX, valueY, mPaint);
					continue;
				}
				float value = lineData.get(j).getValue();
				if (value == 0) {
					startX = startX + lineLength;
					ptFirst = new PointF(startX, valueY);
					canvas.drawLine(ptFirst.x, ptFirst.y, startX, valueY, mPaint);
					continue;
				}
				valueY = dataQuadrant.getQuadrantPaddingHeight() / 2
						- (float) (((value - closingPrice) / maxChangPrice) * (dataQuadrant.getQuadrantPaddingHeight() / 2))
						+ dataQuadrant.getQuadrantPaddingStartY();
				// if is not last point connect to previous point
				if (j > displayFrom) {
					canvas.drawLine(ptFirst.x, ptFirst.y, startX, valueY, mPaint);
				}
				// reset
				ptFirst = new PointF(startX, valueY);
				startX = startX + lineLength;
			}
		}
	}

	protected float olddistance = 0f;
	protected float newdistance = 0f;

	protected PointF startPointA;
	protected PointF startPointB;

	private int displayDataSize;

	private double closingPrice;

	private float maxChangPrice;

	private int shadowAreaColor;

	private boolean canHandleTouchEvent;

	private boolean mHasPerformedLongPress;

	private CheckForLongPress mPendingCheckForLongPress;
	private int mWindowAttachCount;

	private PerformClick mPerformClick;
	private MotionEvent event;

	private boolean displayCrossLongPressed;

	public boolean isDisplayCrossLongPressed() {
		return displayCrossLongPressed;
	}

	public void setDisplayCrossLongPressed(boolean displayCrossLongPressed) {
		this.displayCrossLongPressed = displayCrossLongPressed;
	}

	public boolean isCanHandleTouchEvent() {
		return canHandleTouchEvent;
	}

	public void setCanHandleTouchEvent(boolean canHandleTouchEvent) {
		this.canHandleTouchEvent = canHandleTouchEvent;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (displayCrossLongPressed) {
			setOnLongClickListener(longClickListener);
		}
		if (!isValidTouchPoint(event.getX(), event.getY())) {
			return false;
		}
		if (null == linesData || linesData.size() == 0) {
			return false;
		}
		if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			setPressed(false);
			setDisplayCrossXOnTouch(false);
			setDisplayCrossYOnTouch(false);
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			// setPressed(false);
			setDisplayCrossXOnTouch(false);
			setDisplayCrossYOnTouch(false);
			boolean focusTaken = false;
			if (isFocusable() && isFocusableInTouchMode() && !isFocused()) {
				focusTaken = requestFocus();
			}
			if (!mHasPerformedLongPress) {
				removeCallbacks(mPendingCheckForLongPress);
				Log.i("info", "33333333333333333333");
				if (!focusTaken) {
					if (mPerformClick == null) {
						mPerformClick = new PerformClick();
					}
					performClick();
				}
			}
		} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
			mHasPerformedLongPress = false;
			setPressed(true);
			this.event = event;
			checkForLongClick(0);
		}
		return slipGestureDetector.onTouchEvent(event);
	}

	class PerformClick implements Runnable {

		@Override
		public void run() {
			performClick();
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public boolean isInScrollingContainer() {
		ViewParent p = getParent();
		while (p != null && p instanceof ViewGroup) {
			if (((ViewGroup) p).shouldDelayChildPressedState()) {
				return true;
			}
			p = p.getParent();
		}
		return false;
	}

	private void checkForLongClick(int delayOffset) {
		mHasPerformedLongPress = false;
		if (mPendingCheckForLongPress == null) {
			mPendingCheckForLongPress = new CheckForLongPress();
		}
		// mPendingCheckForLongPress.rememberWindowAttachCount();
		postDelayed(mPendingCheckForLongPress, ViewConfiguration.getLongPressTimeout() - delayOffset);
	}

	class CheckForLongPress implements Runnable {

		private int mOriginalWindowAttachCount;

		public void run() {
			if (isPressed() && displayCrossLongPressed) {
				if (performLongClick()) {
					mHasPerformedLongPress = true;
				}
			}
		}

		public void rememberWindowAttachCount() {
			mOriginalWindowAttachCount = mWindowAttachCount;
		}
	}

	// @Override
	// protected boolean isValidTouchPoint(float x, float y) {
	// if (x < dataQuadrant.getQuadrantPaddingStartX() || x >
	// dataQuadrant.getQuadrantPaddingEndX()) {
	// return false;
	// }
	// if (y < dataQuadrant.getQuadrantPaddingStartY() || y >
	// dataQuadrant.getQuadrantPaddingEndY()) {
	// return false;
	// }
	// if (currentIndex >= linesData.size()) {
	// return false;
	// }
	// return true;
	// }

	/**
	 * <p>
	 * calculate the distance between two touch points
	 * </p>
	 * <p>
	 * 複数タッチしたポイントの距離
	 * </p>
	 * <p>
	 * 计算两点触控时两点之间的距离
	 * </p>
	 * 
	 * @param event
	 * @return float
	 *         <p>
	 *         distance
	 *         </p>
	 *         <p>
	 *         距離
	 *         </p>
	 *         <p>
	 *         距离
	 *         </p>
	 */
	protected float calcDistance(MotionEvent event) {
		if (event.getPointerCount() <= 1) {
			return 0f;
		} else {
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			return FloatMath.sqrt(x * x + y * y);
		}
	}

	public void moveRight() {
		int dataSize = linesData.get(0).getLineData().size();
		if (displayFrom + displayNumber < dataSize - SLIP_STEP) {
			displayFrom = displayFrom + SLIP_STEP;
		} else {
			displayFrom = dataSize - displayNumber;
		}

		// 处理displayFrom越界
		if (displayFrom + displayNumber >= dataSize) {
			displayFrom = dataSize - displayNumber;
		}

		this.postInvalidate();

		// //Listener
		// if (onSlipGestureListener != null) {
		// onSlipGestureListener.onSlip(SLIP_DIRECTION_RIGHT, displayFrom,
		// displayNumber);
		// }
	}

	public void moveLeft() {
		int dataSize = linesData.get(0).getLineData().size();

		if (displayFrom <= SLIP_STEP) {
			displayFrom = 0;
		} else if (displayFrom > SLIP_STEP) {
			displayFrom = displayFrom - SLIP_STEP;
		} else {

		}

		// 处理displayFrom越界
		if (displayFrom + displayNumber >= dataSize) {
			displayFrom = dataSize - displayNumber;
		}

		this.postInvalidate();

		// //Listener
		// if (onSlipGestureListener != null) {
		// onSlipGestureListener.onSlip(SLIP_DIRECTION_LEFT, displayFrom,
		// displayNumber);
		// }
	}

	/**
	 * <p>
	 * Zoom in the graph
	 * </p>
	 * <p>
	 * 拡大表示する。
	 * </p>
	 * <p>
	 * 放大表示
	 * </p>
	 */
	public void zoomIn() {
		if (displayNumber > minDisplayNumber) {
			// 区分缩放方向
			if (zoomBaseLine == ZOOM_BASE_LINE_CENTER) {
				displayNumber = displayNumber - ZOOM_STEP;
				displayFrom = displayFrom + ZOOM_STEP / 2;
			} else if (zoomBaseLine == ZOOM_BASE_LINE_LEFT) {
				displayNumber = displayNumber - ZOOM_STEP;
			} else if (zoomBaseLine == ZOOM_BASE_LINE_RIGHT) {
				displayNumber = displayNumber - ZOOM_STEP;
				displayFrom = displayFrom + ZOOM_STEP;
			}

			// 处理displayNumber越界
			if (displayNumber < minDisplayNumber) {
				displayNumber = minDisplayNumber;
			}

			// 处理displayFrom越界
			if (displayFrom + displayNumber >= linesData.get(0).getLineData().size()) {
				displayFrom = linesData.get(0).getLineData().size() - displayNumber;
			}

			this.postInvalidate();

			// //Listener
			// if (onZoomGestureListener != null) {
			// onZoomGestureListener.onZoom(ZOOM_IN, displayFrom,
			// displayNumber);
			// }
		}
	}

	/**
	 * <p>
	 * Zoom out the grid
	 * </p>
	 * <p>
	 * 縮小表示する。
	 * </p>
	 * <p>
	 * 缩小
	 * </p>
	 */
	public void zoomOut() {
		int dataSize = linesData.get(0).getLineData().size();

		if (displayNumber < dataSize - 1) {
			if (displayNumber + ZOOM_STEP > dataSize - 1) {
				displayNumber = dataSize - 1;
				displayFrom = 0;
			} else {
				// 区分缩放方向
				if (zoomBaseLine == ZOOM_BASE_LINE_CENTER) {
					displayNumber = displayNumber + ZOOM_STEP;
					if (displayFrom > ZOOM_STEP / 2) {
						displayFrom = displayFrom - ZOOM_STEP / 2;
					} else {
						displayFrom = 0;
					}
				} else if (zoomBaseLine == ZOOM_BASE_LINE_LEFT) {
					displayNumber = displayNumber + ZOOM_STEP;
				} else if (zoomBaseLine == ZOOM_BASE_LINE_RIGHT) {
					displayNumber = displayNumber + ZOOM_STEP;
					if (displayFrom > ZOOM_STEP) {
						displayFrom = displayFrom - ZOOM_STEP;
					} else {
						displayFrom = 0;
					}
				}
			}

			if (displayFrom + displayNumber >= dataSize) {
				displayNumber = dataSize - displayFrom;
			}

			this.postInvalidate();

			// //Listener
			// if (onZoomGestureListener != null) {
			// onZoomGestureListener.onZoom(ZOOM_OUT, displayFrom,
			// displayNumber);
			// }
		}
	}

	/**
	 * @return the minValue
	 */
	public double getMinValue() {
		return minValue;
	}

	/**
	 * @param minValue
	 *            the minValue to set
	 */
	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	/**
	 * @return the maxValue
	 */
	public double getMaxValue() {
		return maxValue;
	}

	/**
	 * @param maxValue
	 *            the maxValue to set
	 */
	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	/**
	 * @return the displayFrom
	 */
	public int getDisplayFrom() {
		return displayFrom;
	}

	/**
	 * @param displayFrom
	 *            the displayFrom to set
	 */
	public void setDisplayFrom(int displayFrom) {
		this.displayFrom = displayFrom;
	}

	/**
	 * @return the displayNumber
	 */
	public int getDisplayNumber() {
		return displayNumber;
	}

	/**
	 * 设置总共的数据数目.
	 * 
	 * @param dataSize
	 */
	public void setDisplayDataSize(int dataSize) {
		this.displayDataSize = dataSize;
	}

	@Override
	public void setClosingPrice(double closingPrice) {
		this.closingPrice = closingPrice;
		super.setClosingPrice(closingPrice);
	}

	@Override
	public void setMaxChangPrice(float maxChangPrice) {
		this.maxChangPrice = maxChangPrice;
		super.setMaxChangPrice(maxChangPrice);
	}

	/**
	 * @param displayNumber
	 *            the displayNumber to set
	 */
	public void setDisplayNumber(int displayNumber) {
		this.displayNumber = displayNumber;
	}

	/**
	 * @return the minDisplayNumber
	 */
	public int getMinDisplayNumber() {
		return minDisplayNumber;
	}

	/**
	 * @param minDisplayNumber
	 *            the minDisplayNumber to set
	 */
	public void setMinDisplayNumber(int minDisplayNumber) {
		this.minDisplayNumber = minDisplayNumber;
	}

	/**
	 * @return the zoomBaseLine
	 */
	public int getZoomBaseLine() {
		return zoomBaseLine;
	}

	/**
	 * @param zoomBaseLine
	 *            the zoomBaseLine to set
	 */
	public void setZoomBaseLine(int zoomBaseLine) {
		this.zoomBaseLine = zoomBaseLine;
	}

	/**
	 * @return the linesData
	 */
	public List<LineEntity<DateValueEntity>> getLinesData() {
		return linesData;
	}

	/**
	 * @param linesData
	 *            the linesData to set
	 */
	public void setLinesData(List<LineEntity<DateValueEntity>> linesData) {
		this.linesData = linesData;
	}

	/**
	 * @return the lineAlignType
	 */
	public int getLineAlignType() {
		return lineAlignType;
	}

	/**
	 * @param lineAlignType
	 *            the lineAlignType to set
	 */
	public void setLineAlignType(int lineAlignType) {
		this.lineAlignType = lineAlignType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @param listener
	 * 
	 * @see
	 * cn.limc.androidcharts.event.IZoomable#setOnZoomGestureListener(cn.limc
	 * .androidcharts.event.OnZoomGestureListener)
	 */
	public void setOnZoomGestureListener(OnZoomGestureListener listener) {
		this.onZoomGestureListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @param listener
	 * 
	 * @see
	 * cn.limc.androidcharts.event.ISlipable#setOnSlipGestureListener(cn.limc
	 * .androidcharts.event.OnSlipGestureListener)
	 */
	public void setOnSlipGestureListener(OnSlipGestureListener listener) {
		this.onSlipGestureListener = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @return
	 * 
	 * @see cn.limc.androidcharts.event.ISlipable#getOnSlipGestureListener()
	 */
	public OnSlipGestureListener getOnSlipGestureListener() {
		return onSlipGestureListener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @return
	 * 
	 * @see cn.limc.androidcharts.event.IZoomable#getOnZoomGestureListener()
	 */
	public OnZoomGestureListener getOnZoomGestureListener() {
		return onZoomGestureListener;
	}

}
