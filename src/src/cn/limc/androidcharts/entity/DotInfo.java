package cn.limc.androidcharts.entity;

public class DotInfo {
	private String time;
	private double price;
	private double avgPrice;
	private double tradeVolume;

	private double high;
	private double open;
	private double low;
	private double close;
	
	public double getHigh() {
		return high;
	}

	public void setHigh(double high) {
		this.high = high;
	}

	public double getOpen() {
		return open;
	}

	public void setOpen(double open) {
		this.open = open;
	}

	public double getLow() {
		return low;
	}

	public void setLow(double low) {
		this.low = low;
	}

	public double getClose() {
		return close;
	}

	public void setClose(double close) {
		this.close = close;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getAvgPrice() {
		return avgPrice;
	}

	public void setAvgPrice(double avgPrice) {
		this.avgPrice = avgPrice;
	}

	public double getTradeVolume() {
		return tradeVolume;
	}

	public void setTradeVolume(double tradeVolume) {
		this.tradeVolume = tradeVolume;
	}

	@Override
	public String toString() {
		return "DotInfo [time=" + time + ", price=" + price + ", avgPrice=" + avgPrice + ", tradeVolume=" + tradeVolume + "]";
	}

}
