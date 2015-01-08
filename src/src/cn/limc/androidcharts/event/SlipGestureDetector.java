/*
 * SlipGestureDetector.java
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

package cn.limc.androidcharts.event;

import org.apache.http.util.LangUtils;

import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;

public class SlipGestureDetector<T extends ISlipable> extends ZoomGestureDetector<IZoomable> {
	protected PointF startPointA;
	protected PointF startPointB;
	// private MotionEvent initalEvent;
	private float initalX;

	private OnSlipGestureListener onSlipGestureListener;
	private boolean performLongClick;
	private float mStickScaleValue;

	public boolean isPerformLongClick() {
		return performLongClick;
	}

	public void setPerformLongClick(boolean performLongClick) {
		this.performLongClick = performLongClick;
	}

	public SlipGestureDetector(ISlipable slipable) {
		super(slipable);
		if (slipable != null) {
			this.onSlipGestureListener = slipable.getOnSlipGestureListener();
		}
	}

	public boolean onTouchEvent(MotionEvent event) {
		int pointers = event.getPointerCount();
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		// 设置拖拉模式
		case MotionEvent.ACTION_DOWN:
			initalX = event.getX();
			if (pointers > 1) {
				touchMode = TOUCH_MODE_MULTI;
			} else {
				touchMode = TOUCH_MODE_SINGLE;
			}
			break;
		case MotionEvent.ACTION_UP:
			startPointA = null;
			startPointB = null;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			startPointA = null;
			startPointB = null;
		case MotionEvent.ACTION_POINTER_DOWN:
			olddistance = calcDistance(event);
			if (olddistance > MIN_DISTANCE) {
				touchMode = TOUCH_MODE_MULTI;
				startPointA = new PointF(event.getX(0), event.getY(0));
				startPointB = new PointF(event.getX(1), event.getY(1));
			}
			return true;
		case MotionEvent.ACTION_MOVE:
			if (touchMode == TOUCH_MODE_SINGLE) {
				final float finalX = event.getX();
				// MotionEvent finalEvent = event;
				if (performLongClick) {
					return super.onTouchEvent(event);
				} else {
					if (finalX - initalX >= mStickScaleValue) {
						if (onSlipGestureListener != null) {
							onSlipGestureListener.onMoveRight((ISlipable) instance, event);
						}
					} else if (initalX - finalX >= mStickScaleValue) {
						if (onSlipGestureListener != null) {
							onSlipGestureListener.onMoveLeft((ISlipable) instance, event);
						}
					}
					initalX = finalX;
					// initalEvent = finalEvent;
					return true;
				}
			} else if (touchMode == TOUCH_MODE_MULTI) {
				newdistance = calcDistance(event);
				if (Math.abs(newdistance - olddistance) > MIN_DISTANCE) {
					if (onZoomGestureListener != null) {
						if (newdistance > olddistance) {
							onZoomGestureListener.onZoomIn((IZoomable) instance, event);
						} else {
							onZoomGestureListener.onZoomOut((IZoomable) instance, event);
						}
					}
				}
				olddistance = newdistance;
				return true;
				// startPointA = new PointF(event.getX(), event.getY());
				// startPointB = new PointF(event.getX(1), event.getY(1));
			}
			break;
		}
		return super.onTouchEvent(event);
	}

	private float calEventDistance(MotionEvent initalEvent, MotionEvent finalEvent) {
		float xSpan = initalEvent.getX() - finalEvent.getX();
		float ySpan = initalEvent.getY() - finalEvent.getY();
		return (float) Math.sqrt(xSpan * xSpan + ySpan * ySpan);
	}

	public void setStickScaleValue(float stickScaleValue) {
		mStickScaleValue = stickScaleValue;
	}
}
