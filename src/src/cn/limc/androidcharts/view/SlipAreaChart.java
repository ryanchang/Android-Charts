/*
 * SlipAreaChart.java
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

import org.apache.http.util.LangUtils;

import cn.limc.androidcharts.entity.DateValueEntity;
import cn.limc.androidcharts.entity.LineEntity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
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
 * @version v1.0 2014/01/22 16:19:37
 * 
 */
public class SlipAreaChart extends SlipLineChart {

	private double closingPrice;
	private int displayDataSize;
	private double maxChangPrice;
	private int shadowAreaColor;


	/**
	 * <p>
	 * Constructor of SlipAreaChart
	 * </p>
	 * <p>
	 * SlipAreaChart类对象的构造函数
	 * </p>
	 * <p>
	 * SlipAreaChartのコンストラクター
	 * </p>
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public SlipAreaChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	/**
	 * <p>
	 * Constructor of SlipAreaChart
	 * </p>
	 * <p>
	 * SlipAreaChart类对象的构造函数
	 * </p>
	 * <p>
	 * SlipAreaChartのコンストラクター
	 * </p>
	 * 
	 * @param context
	 * @param attrs
	 */
	public SlipAreaChart(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	/**
	 * <p>
	 * Constructor of SlipAreaChart
	 * </p>
	 * <p>
	 * SlipAreaChart类对象的构造函数
	 * </p>
	 * <p>
	 * SlipAreaChartのコンストラクター
	 * </p>
	 * 
	 * @param context
	 */
	public SlipAreaChart(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
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
		super.onDraw(canvas);
		// draw lines
		drawAreas(canvas);
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
	protected void drawAreas(Canvas canvas) {
		if (null == linesData) {
			return;
		}
		// distance between two points
		float lineLength;
		// start point‘s X
		float startX;

		// draw lines
		for (int i = 0; i < linesData.size(); i++) {
			if (i == 1) {
				return;
			}

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
			mPaint.setColor(line.getShadowAreaColor());
			mPaint.setAlpha(70);
			mPaint.setAntiAlias(true);

			// set start point’s X
			if (lineAlignType == ALIGN_TYPE_CENTER) {
				lineLength = (dataQuadrant.getQuadrantPaddingWidth() / displayNumber);
				startX = dataQuadrant.getQuadrantPaddingStartX() + lineLength / 2;
			} else {
				lineLength = (dataQuadrant.getQuadrantPaddingWidth() / (displayNumber - 1));
				startX = dataQuadrant.getQuadrantPaddingStartX();
			}

			Path linePath = new Path();

			int count = 0;
			float valueY = dataQuadrant.getQuadrantPaddingHeight() + dataQuadrant.getQuadrantPaddingStartY();
			for (int j = displayFrom; j < displayFrom + lineData.size(); j++) {
				if (lineData.get(j) == null) {
					startX = startX + lineLength;
					linePath.lineTo(startX, valueY);
					continue;
				}
				float value = lineData.get(j).getValue();
				if (value == 0) {
					if (j == 0) {
						linePath.moveTo(startX, dataQuadrant.getQuadrantPaddingEndY());
						linePath.lineTo(startX, valueY);
						startX = startX + lineLength;
					} else {
						startX = startX + lineLength;
						linePath.lineTo(startX, valueY);
					}
					continue;
				}
				count++;
				valueY = dataQuadrant.getQuadrantPaddingHeight() / 2
						- (float) (((value - closingPrice) / maxChangPrice) * (dataQuadrant.getQuadrantPaddingHeight() / 2))
						+ dataQuadrant.getQuadrantPaddingStartY();

				// if is not last point connect to previous point
				if (j == displayFrom || count == 1) {
					linePath.moveTo(startX, dataQuadrant.getQuadrantPaddingEndY());
					linePath.lineTo(startX, valueY);
				} else if (j == displayFrom + displayDataSize - 1) {
					linePath.lineTo(startX, valueY);
					// getQuadrantPaddingEndY 表示
					linePath.lineTo(startX, dataQuadrant.getQuadrantPaddingEndY());
				} else {
					linePath.lineTo(startX, valueY);
				}
				startX = startX + lineLength;
			}
			// linePath.lineTo(startX, valueY);
			// getQuadrantPaddingEndY 表示
			linePath.lineTo(startX, dataQuadrant.getQuadrantPaddingEndY());
			linePath.close();
			canvas.drawPath(linePath, mPaint);
		}
	}

	public void setDisplayDataSizeInChild(int dataSize) {
		this.displayDataSize = dataSize;
		super.setDisplayDataSize(dataSize);
	}

	public void setClosingPrice(double closingPrice) {
		super.setClosingPrice(closingPrice);
		this.closingPrice = closingPrice;

	}

	public void setMaxChangPrice(float maxChangPrice) {
		super.setMaxChangPrice(maxChangPrice);
		this.maxChangPrice = maxChangPrice;

	}


}
