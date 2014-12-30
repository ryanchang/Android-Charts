/*
 * OnTouchGestureListener.java
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

import android.graphics.PointF;
import android.view.MotionEvent;

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
 * @version v1.0 2014/06/23 15:52:21
 * 
 */
public class OnTouchGestureListener {

	/**
	 * 手势监听器的回调方法. 当手势探测器探测到触屏事件后,通过改回调方法告手势监听器,产生了触屏事件.
	 * 然后触屏事件在通过ITouchable接口的回调方法,将触屏事件按告诉被点击的图标,画出十字线.
	 * 
	 * @param touchable
	 * @param event
	 */
	public void onTouchDown(ITouchable touchable, MotionEvent event) {
		if (touchable != null) {
			touchable.touchDown(new PointF(event.getX(), event.getY()));
		}
	}

	public void onTouchMoved(ITouchable touchable, MotionEvent event) {
		if (touchable != null) {
			touchable.touchMoved(new PointF(event.getX(), event.getY()));
		}
	}

	public void onTouchUp(ITouchable touchable, MotionEvent event) {
		if (touchable != null) {
			touchable.touchUp(new PointF(event.getX(), event.getY()));
		}
	}
}
