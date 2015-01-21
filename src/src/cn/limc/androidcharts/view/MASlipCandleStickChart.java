/*
 * MASlipCandleStickChart.java
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

import java.util.List;

import cn.limc.androidcharts.entity.DateValueEntity;
import cn.limc.androidcharts.entity.LineEntity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;

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
 * @version v1.0 2014/01/21 12:03:25
 */
public class MASlipCandleStickChart extends SlipCandleStickChart {

	private List<LineEntity<DateValueEntity>> linesData;
	private List<DateValueEntity> lineData;
	private String[] titles = { "MA5", "MA10", "MA25" };
	private int textWidthSum;

	public MASlipCandleStickChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public MASlipCandleStickChart(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public MASlipCandleStickChart(Context context) {
		super(context);
	}

	@Override
	protected void calcDataValueRange() {
		super.calcDataValueRange();

		double maxValue = this.maxValue;
		double minValue = this.minValue;
		// 逐条输出MA线
		for (int i = 0; i < this.linesData.size(); i++) {
			LineEntity<DateValueEntity> line = this.linesData.get(i);
			if (line != null && line.getLineData().size() > 0) {
				// 判断显示为方柱或显示为线条
				for (int j = displayFrom; j < displayFrom + displayNumber; j++) {
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

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// draw lines
		if (null != this.linesData) {
			if (0 != this.linesData.size()) {
				drawLines(canvas);
				drawMADots(canvas);
			}
		}
	}

	protected void drawLines(Canvas canvas) {
		if (null == stickData) {
			return;
		}
		if (stickData.size() <= 0) {
			return;
		}
		float lineLength = 0f;
		if (isMoveToLetfEnd()) {
			lineLength = (dataQuadrant.getQuadrantPaddingWidth() - getMoveLeftDistance()) / displayNumber
					- stickSpacing;
			setDisplayNumber(displayNumber - getSubDisplayNum());
		} else {
			lineLength = dataQuadrant.getQuadrantPaddingWidth() / displayNumber - stickSpacing;
		}
		setLineLength(lineLength);
		float startX;
		// draw MA lines
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
			mPaint.setStrokeWidth(line.getLineWidth());
			mPaint.setAntiAlias(true);
			// set start point’s X
			if (isMoveToLetfEnd()) {
				startX = dataQuadrant.getQuadrantPaddingStartX() + lineLength / 2 + getMoveLeftDistance();
			} else {
				startX = dataQuadrant.getQuadrantPaddingStartX() + lineLength / 2;
			}
			// start point
			PointF ptFirst = null;
			for (int j = super.getDisplayFrom(); j < super.getDisplayFrom() + super.getDisplayNumber(); j++) {
				if (j > lineData.size() - 1 || i < 0) {
					return;
				}
				float value = lineData.get(j).getValue();
				// calculate Y
				float valueY = (float) ((1f - (value - minValue) / (maxValue - minValue)) * dataQuadrant
						.getQuadrantPaddingHeight()) + dataQuadrant.getQuadrantPaddingStartY();
				// if is not last point connect to previous point
				if (j > super.getDisplayFrom()) {
					canvas.drawLine(ptFirst.x, ptFirst.y, startX, valueY, mPaint);
				}
				// reset
				ptFirst = new PointF(startX, valueY);
				startX = startX + stickSpacing + lineLength;
			}
		}
	}

	@Override
	public void drawLoadMoreText(Canvas canvas) {
		Paint textPaint = new Paint();
		textPaint.setColor(Color.GRAY);
		textPaint.setTextSize(16);
		int textWidth = getTextBoundsWidth(getTextLoadMore(), textPaint);
		float moveWidth = getMoveLeftDistance();
		if (moveWidth >= textWidth + 10) {
		}
		canvas.drawText(getTextLoadMore(), dataQuadrant.getQuadrantPaddingStartX(),
				dataQuadrant.getQuadrantPaddingHeight() / 2 + dataQuadrant.getQuadrantPaddingStartY(), textPaint);
	}

	private void drawMADots(Canvas canvas) {
		String drawText = null;
		for (int i = 0; i < linesData.size(); i++) {
			LineEntity<DateValueEntity> line = (LineEntity<DateValueEntity>) linesData.get(i);
			if (line == null) {
				continue;
			}
			if (line.isDisplay() == false) {
				continue;
			}
			lineData = line.getLineData();
			if (lineData == null) {
				continue;
			}
			Paint mPaint = new Paint();
			mPaint.setColor(line.getLineColor());
			mPaint.setStrokeWidth(5f);
			mPaint.setTextSize(6 * getResources().getDisplayMetrics().scaledDensity);
			mPaint.setAntiAlias(true);
			String priceText = null;
			if (isDisplayCrossXOnTouch()) {
				priceText = (String) getAxisLineValue(touchPoint.x);
			} else {
				priceText = String.format("%.2f", lineData.get(lineData.size() - 1).getValue());
			}
			drawText = titles[i] + " " + priceText;
			canvas.drawPoint(dataQuadrant.getQuadrantPaddingStartX() + 7 * (i + 1) * 2 + textWidthSum,
					dataQuadrant.getQuadrantPaddingStartY() + 5, mPaint);
			canvas.drawText(drawText, dataQuadrant.getQuadrantPaddingStartX() + 5 + (i + 1) * 7 * 2 + textWidthSum,
					dataQuadrant.getQuadrantPaddingStartY() + 5 + getTextBoundsHeight(priceText, mPaint) / 2, mPaint);
			if (i < linesData.size() - 1) {
				textWidthSum += getTextBoundsWidth(drawText, mPaint);
			}
		}
		textWidthSum = 0;
	}

	public Object getAxisLineValue(Object value) {
		float valueLength = ((Float) value).floatValue() - dataQuadrant.getQuadrantPaddingStartX();
		String ratioValue = String.valueOf(valueLength / this.dataQuadrant.getQuadrantPaddingWidth());
		float graduate = Float.valueOf(ratioValue);
		int index = (int) Math.floor(graduate * getDisplayNumber());
		if (index + getDisplayFrom() < lineData.size()) {
			return String.format("%.2f", lineData.get(index + displayFrom).getValue());
		} else {
			return String.format("%.2f", lineData.get(lineData.size() - 1).getValue());
		}
	}

	public int getTextBoundsWidth(String text, Paint textPaint) {
		Rect bounds = new Rect();
		textPaint.getTextBounds(text, 0, text.length(), bounds);
		int height = bounds.height();
		int width = bounds.width();
		return width;
	}

	public List<LineEntity<DateValueEntity>> getLinesData() {
		return linesData;
	}

	public void setLinesData(List<LineEntity<DateValueEntity>> linesData) {
		this.linesData = linesData;
	}

}
