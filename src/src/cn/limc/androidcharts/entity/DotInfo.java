package cn.limc.androidcharts.entity;

public class DotInfo {
	private int time;
	private double price;
	private double avgPrice;
	private double tradeVolume;

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
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
