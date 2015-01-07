/*
 * SlipStickChart.java
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

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.View.OnLongClickListener;
import cn.limc.androidcharts.entity.ColoredStickEntity;
import cn.limc.androidcharts.entity.IMeasurable;
import cn.limc.androidcharts.entity.IStickEntity;
import cn.limc.androidcharts.event.IGestureDetector;
import cn.limc.androidcharts.event.ISlipable;
import cn.limc.androidcharts.event.OnSlipGestureListener;
import cn.limc.androidcharts.event.SlipGestureDetector;
import cn.limc.androidcharts.mole.StickMole;
import cn.limc.androidcharts.view.SlipLineChart.PerformClick;

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
 * @version v1.0 2014-1-20 下午2:03:08
 * 
 */
public class SlipStickChart extends StickChart implements ISlipable {

	public static final int DEFAULT_DISPLAY_FROM = 0;
	public static final int DEFAULT_DISPLAY_NUMBER = 50;
	public static final int DEFAULT_MIN_DISPLAY_NUMBER = 20;
	public static final int DEFAULT_ZOOM_BASE_LINE = ZOOM_BASE_LINE_CENTER;

	protected int displayFrom = DEFAULT_DISPLAY_FROM;
	protected int displayNumber = DEFAULT_DISPLAY_NUMBER;
	protected int minDisplayNumber = DEFAULT_MIN_DISPLAY_NUMBER;
	protected int zoomBaseLine = DEFAULT_ZOOM_BASE_LINE;

	protected OnSlipGestureListener onSlipGestureListener = new OnSlipGestureListener();
	protected SlipGestureDetector<ISlipable> slipGestureDetector = new SlipGestureDetector<ISlipable>(this);
	private String textLoadMore;

	/**
	 * <p>
	 * Constructor of SlipStickChart
	 * </p>
	 * <p>
	 * SlipStickChart类对象的构造函数
	 * </p>
	 * <p>
	 * SlipStickChartのコンストラクター
	 * </p>
	 * 
	 * @param context
	 */
	public SlipStickChart(Context context) {
		super(context);
	}

	/**
	 * <p>
	 * Constructor of SlipStickChart
	 * </p>
	 * <p>
	 * SlipStickChart类对象的构造函数
	 * </p>
	 * <p>
	 * SlipStickChartのコンストラクター
	 * </p>
	 * 
	 * @param context
	 * @param defStyle
	 * @param attrs
	 */
	public SlipStickChart(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	/**
	 * <p>
	 * Constructor of SlipStickChart
	 * </p>
	 * <p>
	 * SlipStickChart类对象的构造函数
	 * </p>
	 * <p>
	 * SlipStickChartのコンストラクター
	 * </p>
	 * 
	 * @param context
	 * @param attrs
	 */
	public SlipStickChart(Context context, AttributeSet attrs) {
		super(context, attrs);
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
		if (moveContinue && moveToLetfEnd) {
			drawLoadMoreText(canvas);
		}
	}

	// 本部分的画柱体的方法不会被调用.
	@Override
	protected void drawSticks(Canvas canvas) {
		if (null == stickData) {
			return;
		}
		if (stickData.size() == 0) {
			return;
		}
		float stickWidth = dataQuadrant.getQuadrantPaddingWidth() / getDisplayNumber() - stickSpacing;
		float stickX = dataQuadrant.getQuadrantPaddingStartX();

		for (int i = getDisplayFrom(); i < getDisplayTo(); i++) {
			IMeasurable stick = stickData.get(i);
			StickMole mole = (StickMole) provider.getMole();
			mole.setUp(this, stick, stickX, stickWidth);
			mole.draw(canvas);
			// next x
			stickX = stickX + stickSpacing + stickWidth;
		}
	}

	protected float olddistance = 0f;
	protected float newdistance = 0f;

	protected PointF startPointA;
	protected PointF startPointB;
	private boolean slipStickChartUnclickable;
	private boolean mHasPerformedLongPress;
	private CheckForLongPress mPendingCheckForLongPress;
	private PerformClick mPerformClick;
	private MotionEvent event;
	private boolean displayCrossLongPressed;
	private int mWindowAttachCount;
	private OnLongClickListener longClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			setDisplayCrossXOnTouch(true);
			setDisplayCrossYOnTouch(false);
			mInLongPress = true;
			slipGestureDetector.setPerformLongClick(true);
			slipGestureDetector.onTouchEvent(event);
			return true;
		}
	};
	private SelectSectionOnTouchMoveListener onTouchMoveListener;
	private boolean mInLongPress;
	private float initialX;
	private float finalY;
	private float initialY;
	private float finalX;
	private boolean isCheckingLongPress;
	private boolean moveContinue;
	private boolean moveToLetfEnd;
	private int subDisplayNum;
	private int count;
	private boolean isToLoadMore;
	private boolean moveRightIsForbidden;

	public boolean isMoveContinue() {
		return moveContinue;
	}

	public void setMoveContinue(boolean moveContinue) {
		this.moveContinue = moveContinue;
	}

	public boolean isMoveToLetfEnd() {
		return moveToLetfEnd;
	}

	public void setMoveToLetfEnd(boolean moveToLetfEnd) {
		this.moveToLetfEnd = moveToLetfEnd;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isValidTouchPoint(event.getX(), event.getY())) {
			return false;
		}
		if (null == stickData || stickData.size() == 0) {
			return false;
		}
		if (slipStickChartUnclickable) {
			return false;
		}
		if (displayCrossLongPressed) {
			setOnLongClickListener(longClickListener);
		}
		if (event.getAction() == MotionEvent.ACTION_HOVER_EXIT) {
			moveContinue = false;
			setPressed(false);
			closeCrossXAndY();
		} else if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
			moveContinue = false;
			setPressed(false);
			mInLongPress = false;
			closeCrossXAndY();
		} else if (event.getAction() == MotionEvent.ACTION_UP) {
			if (isToLoadMore) {
				// 在这里添加加载更多
				onTouchMoveListener.touchToLoadMore(stickData.get(0).getDate());
				setTextLoadMore("正在加载");
				moveRightIsForbidden = true;
				// setDisplayNumber(displayNumber + count);
				isToLoadMore = false;
				this.invalidate();
			}
			moveContinue = false;
			mInLongPress = false;
			closeCrossXAndY();
			boolean focusTaken = false;
			if (isFocusable() && isFocusableInTouchMode() && !isFocused()) {
				focusTaken = requestFocus();
			}
			if (!mHasPerformedLongPress) {
				removeCallbacks(mPendingCheckForLongPress);
				if (!focusTaken) {
					if (mPerformClick == null) {
						mPerformClick = new PerformClick();
					}
					performClick();
				}
			}
		} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
			slipGestureDetector.setStickScaleValue(dataQuadrant.getQuadrantPaddingWidth() / displayNumber);
			mHasPerformedLongPress = false;
			initialX = event.getX();
			initialY = event.getY();
			setPressed(true);
			mInLongPress = false;
			this.event = event;
			checkForLongClick(0);
		} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			moveContinue = true;
			finalX = event.getX();
			finalY = event.getY();
			if (calDistance() > 20) {
				setPressed(false);
				slipGestureDetector.setPerformLongClick(false);
			} else {
				slipGestureDetector.setPerformLongClick(true);
			}
			if (mInLongPress) {
				slipGestureDetector.setPerformLongClick(true);
			}
		}
		return slipGestureDetector.onTouchEvent(event);
	}

	private float calDistance() {
		float x = finalX - initialX;
		float y = finalY - initialY;
		return (float) Math.sqrt(x * x + y * y);
	}

	private void closeCrossXAndY() {
		isCheckingLongPress = false;
		setDisplayCrossXOnTouch(false);
		setDisplayCrossYOnTouch(false);
		mInLongPress = false;
		setPressed(false);
		slipGestureDetector.setPerformLongClick(false);
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
				isCheckingLongPress = false;
				if (performLongClick()) {
					mHasPerformedLongPress = true;
				}
			}
		}

		public void rememberWindowAttachCount() {
			mOriginalWindowAttachCount = mWindowAttachCount;
		}
	}

	class PerformClick implements Runnable {

		@Override
		public void run() {
			performClick();
		}
	}

	public void moveRight() {
		if (moveRightIsForbidden) {
			return;
		}
		int dataSize = stickData.size();
		if (getDisplayFrom() >= SLIP_STEP) {
			setDisplayFrom(getDisplayFrom() - SLIP_STEP);
		}
		if (getDisplayFrom() < SLIP_STEP) {
			setDisplayFrom(0);
		}
		if (displayFrom == 0) {
			// 这里设置一个监听器,监听划到最末,加载更多的数据
			if (moveContinue) {
				isToLoadMore = true;
				displayNumber -= 1;
				count++;
				moveToLetfEnd = true;
				setTextLoadMore("加载更多");
				if (count == 5) {
					this.invalidate();
					moveRightIsForbidden = true;
				}
				return;
			}
		}
		float[] values = onTouchMoveListener.touchMove(displayFrom, displayNumber);
		setMaxValue(values[0]);
		setMinValue(values[1]);
		// 处理displayFrom越界
		this.postInvalidate();
		if (onDisplayCursorListener != null) {
			onDisplayCursorListener.onCursorChanged(this, getDisplayFrom(), getDisplayNumber());
		}
	}

	// 动态的加载纵轴
	@Override
	protected void drawVerticalLine(Canvas canvas) {

	}

	public void drawLoadMoreText(Canvas canvas) {
		return;
	}

	public boolean isSlipStickChartUnclickable() {
		return slipStickChartUnclickable;
	}

	public void setSlipStickChartUnclickable(boolean slipStickChartUnclickable) {
		this.slipStickChartUnclickable = slipStickChartUnclickable;
	}

	public float getMoveLeftDistance() {
		return (dataQuadrant.getQuadrantPaddingWidth() / displayNumber) * (count);
	}

	public void moveLeft() {
		int dataSize = stickData.size();
		if (getDisplayFrom() <= SLIP_STEP) {
			setDisplayFrom(displayFrom + SLIP_STEP);
		} else if (getDisplayFrom() > SLIP_STEP && getDisplayFrom() < dataSize) {
			setDisplayFrom(displayFrom + SLIP_STEP);
		}
		if (getDisplayTo() >= dataSize) {
			setDisplayFrom(dataSize - displayNumber);
		}
		float[] values = onTouchMoveListener.touchMove(displayFrom, displayNumber);
		setMaxValue(values[0]);
		setMinValue(values[1]);
		// 处理displayFrom越界
		// if (getDisplayTo() >= dataSize) {
		// setDisplayFrom(dataSize - getDisplayNumber());
		// }
		this.postInvalidate();
		// Listener
		if (onDisplayCursorListener != null) {
			onDisplayCursorListener.onCursorChanged(this, getDisplayFrom(), getDisplayNumber());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.limc.androidcharts.view.StickChart#zoomIn()
	 */
	@Override
	public void zoomIn() {
		if (getDisplayNumber() > getMinDisplayNumber()) {
			// 区分缩放方向
			if (zoomBaseLine == ZOOM_BASE_LINE_CENTER) {
				setDisplayNumber(this.displayNumber - ZOOM_STEP);
				setDisplayFrom(displayFrom + ZOOM_STEP / 2);
			} else if (zoomBaseLine == ZOOM_BASE_LINE_LEFT) {
				setDisplayNumber(getDisplayNumber() - ZOOM_STEP);
			} else if (zoomBaseLine == ZOOM_BASE_LINE_RIGHT) {
				setDisplayNumber(getDisplayNumber() - ZOOM_STEP);
				setDisplayFrom(getDisplayFrom() + ZOOM_STEP);
			}
			// 处理displayNumber越界
			if (getDisplayNumber() < getMinDisplayNumber()) {
				setDisplayNumber(getMinDisplayNumber());
			}
			if (getDisplayFrom() <= ZOOM_STEP / 2) {
				setDisplayFrom(0);
				setZoomBaseLine(ZOOM_BASE_LINE_RIGHT);
			}
			// 处理displayFrom越界
			if (getDisplayTo() >= stickData.size()) {
				setDisplayFrom(stickData.size() - getDisplayNumber());
				setZoomBaseLine(ZOOM_BASE_LINE_LEFT);
			}
			float[] values = onTouchMoveListener.touchZoom(displayFrom, displayNumber);
			setMaxValue(values[0]);
			setMinValue(values[1]);
			this.invalidate();
			// if (onDisplayCursorListener != null) {
			// onDisplayCursorListener.onCursorChanged(this, getDisplayFrom(),
			// getDisplayNumber());
			// }
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see cn.limc.androidcharts.view.StickChart#zoomOut()
	 */
	@Override
	public void zoomOut() {
		if (getDisplayNumber() < stickData.size() - 1) {
			if (getDisplayNumber() + ZOOM_STEP > stickData.size() - 1) {
				setDisplayNumber(stickData.size() - 1);
				setDisplayFrom(0);
			} else {
				// 区分缩放方向
				if (zoomBaseLine == ZOOM_BASE_LINE_CENTER) {
					setDisplayNumber(getDisplayNumber() + ZOOM_STEP);
					if (getDisplayFrom() > 1) {
						setDisplayFrom(getDisplayFrom() - ZOOM_STEP / 2);
					} else {
						setDisplayFrom(0);
					}
				} else if (zoomBaseLine == ZOOM_BASE_LINE_LEFT) {
					setDisplayNumber(getDisplayNumber() + ZOOM_STEP);
				} else if (zoomBaseLine == ZOOM_BASE_LINE_RIGHT) {
					setDisplayNumber(getDisplayNumber() + ZOOM_STEP);
					if (getDisplayFrom() > ZOOM_STEP) {
						setDisplayFrom(getDisplayFrom() - ZOOM_STEP);
					} else {
						setDisplayFrom(0);
					}
				}
			}
			if (getDisplayTo() >= stickData.size()) {
				setDisplayFrom(stickData.size() - getDisplayFrom());
				setZoomBaseLine(ZOOM_BASE_LINE_RIGHT);
			} else if (getDisplayFrom() <= 0) {
				setDisplayFrom(0);
				setZoomBaseLine(ZOOM_BASE_LINE_LEFT);
			}
			float[] values = onTouchMoveListener.touchZoom(displayFrom, displayNumber);
			setMaxValue(values[0]);
			setMinValue(values[1]);
			this.invalidate();
			// Listener
			// if (onDisplayCursorListener != null) {
			// onDisplayCursorListener.onCursorChanged(this, getDisplayFrom(),
			// getDisplayNumber());
			// }
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @return
	 * 
	 * @see cn.limc.androidcharts.view.StickChart#getDisplayFrom()
	 */
	@Override
	public int getDisplayFrom() {
		return displayFrom;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @param displayFrom
	 * 
	 * @see cn.limc.androidcharts.view.StickChart#setDisplayFrom(int)
	 */
	@Override
	public void setDisplayFrom(int displayFrom) {
		this.displayFrom = displayFrom;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @return
	 * 
	 * @see cn.limc.androidcharts.view.StickChart#getDisplayTo()
	 */
	@Override
	public int getDisplayTo() {
		return displayFrom + displayNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @param displayTo
	 * 
	 * @see cn.limc.androidcharts.view.StickChart#setDisplayTo(int)
	 */
	@Override
	public void setDisplayTo(int displayTo) {
		// TODO
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @return
	 * 
	 * @see cn.limc.androidcharts.view.StickChart#getDisplayNumber()
	 */
	@Override
	public int getDisplayNumber() {
		return displayNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @param displayNumber
	 * 
	 * @see cn.limc.androidcharts.view.StickChart#setDisplayNumber(int)
	 */
	@Override
	public void setDisplayNumber(int displayNumber) {
		this.displayNumber = displayNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @return
	 * 
	 * @see cn.limc.androidcharts.view.StickChart#getMinDisplayNumber()
	 */
	@Override
	public int getMinDisplayNumber() {
		return minDisplayNumber;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @param minDisplayNumber
	 * 
	 * @see cn.limc.androidcharts.view.StickChart#setMinDisplayNumber(int)
	 */
	@Override
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

	public boolean isMoveRightIsForbidden() {
		return moveRightIsForbidden;
	}

	public void setMoveRightIsForbidden(boolean moveRightIsForbidden) {
		this.moveRightIsForbidden = moveRightIsForbidden;
	}

	public void setOnSlipGestureListener(OnSlipGestureListener listener) {
		this.onSlipGestureListener = listener;

	}

	public int getSubDisplayNum() {
		return subDisplayNum;
	}

	public void setSubDisplayNum(int subDisplayNum) {
		this.subDisplayNum = subDisplayNum;
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

	public boolean isDisplayCrossLongPressed() {
		return displayCrossLongPressed;
	}

	public void setDisplayCrossLongPressed(boolean displayCrossLongPressed) {
		this.displayCrossLongPressed = displayCrossLongPressed;
	}

	public interface SelectSectionOnTouchMoveListener {
		float[] touchMove(int j, int displayNum);

		float[] touchZoom(int displayForm, int displayNum);

		void touchToLoadMore(int date);
	}

	public void setOnSelectSectionOnTouchMoveListener(SelectSectionOnTouchMoveListener onTouchMoveListener) {
		this.onTouchMoveListener = onTouchMoveListener;
	}

	public String getTextLoadMore() {
		return textLoadMore;
	}

	public void setTextLoadMore(String textLoadMore) {
		this.textLoadMore = textLoadMore;
	}

}
