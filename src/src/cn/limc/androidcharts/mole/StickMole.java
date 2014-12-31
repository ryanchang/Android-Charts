/*
 * StickMole.java
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


package cn.limc.androidcharts.mole;

import cn.limc.androidcharts.common.IChart;
import cn.limc.androidcharts.entity.IMeasurable;
import cn.limc.androidcharts.view.DataGridChart;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;

/** 
 * <p>en</p>
 * <p>jp</p>
 * <p>cn</p>
 *
 * @author limc 
 * @version v1.0 2014/06/19 16:39:05 
 *  
 */
public abstract class StickMole extends RectMole {

	public static final int DEFAULT_STICK_SPACING = 1;
	public static final int DEFAULT_STICK_STROKE_WIDTH = 0;
	/**
	 * <p>
	 * Default price up stick's fill color
	 * </p>
	 * <p>
	 * 値上がりローソクの色のデフォルト値
	 * </p>
	 * <p>
	 * 默认阳线的填充颜色
	 * </p>
	 */
	public static final int DEFAULT_POSITIVE_STICK_FILL_COLOR = Color.RED;
	/**
	 * <p>
	 * Default price down stick's fill color
	 * </p>
	 * <p>
	 * 値下りローソクの色のデフォルト値
	 * </p>
	 * <p>
	 * 默认阴线的填充颜色
	 * </p>
	 */
	public static final int DEFAULT_NEGATIVE_STICK_FILL_COLOR = Color.GREEN;

	/**
	 * <p>
	 * Default price no change stick's color (cross-star,T-like etc.)
	 * </p>
	 * <p>
	 * クローススターの色のデフォルト値
	 * </p>
	 * <p>
	 * 默认十字线显示颜色
	 * </p>
	 */
	public static final int DEFAULT_CROSS_STAR_COLOR = Color.LTGRAY;
	/**
	 * <p>
	 * Price up stick's fill color
	 * </p>
	 * <p>
	 * 値上がりローソクの色
	 * </p>
	 * <p>
	 * 阳线的填充颜色
	 * </p>
	 */
	private int positiveStickFillColor = DEFAULT_POSITIVE_STICK_FILL_COLOR;
	/**
	 * <p>
	 * Price down stick's fill color
	 * </p>
	 * <p>
	 * 値下りローソクの色
	 * </p>
	 * <p>
	 * 阴线的填充颜色
	 * </p>
	 */
	private int negativeStickFillColor = DEFAULT_NEGATIVE_STICK_FILL_COLOR;
	
	/**
	 * <p>
	 * Price no change stick's color (cross-star,T-like etc.)
	 * </p>
	 * <p>
	 * クローススターの色（価格変動無し）
	 * </p>
	 * <p>
	 * 十字线显示颜色（十字星,一字平线,T形线的情况）
	 * </p>
	 */
	private int crossStarColor = DEFAULT_CROSS_STAR_COLOR;
	
	/**
	 * <p>
	 * default color for display stick border
	 * </p>
	 * <p>
	 * 表示スティックのボーダーの色のデフォルト値
	 * </p>
	 * <p>
	 * 默认表示柱条的边框颜色
	 * </p>
	 */
	public static final int DEFAULT_STICK_BORDER_COLOR = Color.YELLOW;

	/**
	 * <p>
	 * default color for display stick
	 * </p>
	 * <p>
	 * 表示スティックの色のデフォルト値
	 * </p>
	 * <p>
	 * 默认表示柱条的填充颜色
	 * </p>
	 */
	public static final int DEFAULT_STICK_FILL_COLOR = Color.WHITE;

	/**
	 * <p>
	 * Color for display stick border
	 * </p>
	 * <p>
	 * 表示スティックのボーダーの色
	 * </p>
	 * <p>
	 * 表示柱条的边框颜色
	 * </p>
	 */
	protected int stickBorderColor = DEFAULT_STICK_BORDER_COLOR;

	/**
	 * <p>
	 * Color for display stick
	 * </p>
	 * <p>
	 * 表示スティックの色
	 * </p>
	 * <p>
	 * 表示柱条的填充颜色
	 * </p>
	 */
	protected int stickFillColor = DEFAULT_STICK_FILL_COLOR;
	
	protected int stickStrokeWidth = DEFAULT_STICK_STROKE_WIDTH;
	protected int stickSpacing = DEFAULT_STICK_SPACING;
	
	private IMeasurable stickData;
	
	/** 
	 * <p>Constructor of StickMole</p>
	 * <p>StickMole类对象的构造函数</p>
	 * <p>StickMoleのコンストラクター</p>
	 *
	 * @param inRect 
	 */
	public void setUp(IChart chart ,IMeasurable data, float from , float width) {
		super.setUp(chart);
		this.setPro();
		setStickData(data);
		left = from;
		right = from + width - stickSpacing;
	}
	
	/* (non-Javadoc)
	 * 
	 * @param canvase 
	 * @see cn.limc.androidcharts.mole.IMole#draw(android.graphics.Canvas) 
	 */
	public void draw(Canvas canvas) {	
		
		Paint mPaintPositive = new Paint();
		mPaintPositive.setColor(positiveStickFillColor);

		Paint mPaintNegative = new Paint();
		mPaintNegative.setColor(negativeStickFillColor);

		Paint mPaintCross = new Paint();
		mPaintCross.setColor(crossStarColor);
		
		Paint mPaintFill = new Paint();
		mPaintFill.setStyle(Style.FILL);
		mPaintFill.setColor(stickFillColor);
		
		if (width() >= 2f && width() >= 2 * stickStrokeWidth) {
			if (stickStrokeWidth  > 0) {
				Paint mPaintBorder = new Paint();
				mPaintBorder.setStyle(Style.STROKE);
				mPaintBorder.setStrokeWidth(stickStrokeWidth);
				mPaintBorder.setColor(stickBorderColor);
				
				canvas.drawRect(left + stickStrokeWidth, top + stickStrokeWidth, right - stickStrokeWidth, bottom - stickStrokeWidth, mPaintFill);
				canvas.drawRect(left + stickStrokeWidth, top + stickStrokeWidth, right - stickStrokeWidth, bottom - stickStrokeWidth, mPaintBorder);
			}else{
				canvas.drawRect(left, top, right, bottom, mPaintFill);
			}
		} else {
			canvas.drawLine(left, top, left, bottom, mPaintFill);	
		}
	}

	/**
	 * @return the stickData
	 */
	public IMeasurable getStickData() {
		return stickData;
	}

	/**
	 * @param stickData the stickData to set
	 */
	public void setStickData(IMeasurable stickData) {
		this.stickData = stickData;
		DataGridChart chart = (DataGridChart)getInChart();
		float highY = (float) ((1f - (stickData.getHigh() - chart.getMinValue()) / (chart.getMaxValue() - chart.getMinValue()))
				* (chart.getDataQuadrant().getQuadrantPaddingHeight()) + chart.getDataQuadrant().getQuadrantPaddingStartY());
		float lowY = (float) ((1f - (stickData.getLow() - chart.getMinValue()) / (chart.getMaxValue() - chart.getMinValue()))
				* (chart.getDataQuadrant().getQuadrantPaddingHeight()) + chart.getDataQuadrant().getQuadrantPaddingStartY());
		
		top = highY;
		bottom = lowY;
	}
	
	public abstract void setPro();
}
