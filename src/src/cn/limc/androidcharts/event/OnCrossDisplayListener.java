package cn.limc.androidcharts.event;

import cn.limc.androidcharts.entity.DotInfo;

public interface OnCrossDisplayListener {
	// 十字显示的时候的回调方方法.
	public void crossDisplay(boolean isDisplay, DotInfo dotInfo);
}
