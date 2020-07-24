package kcsj.game;

import java.util.Random;

public class Computer {

	private String comName = "诸葛亮";  //电脑姓名
	private int comScore = 0;  //电脑分数
	private int comQuan;   //电脑出的拳
	
	//给comName、comScore、comQuan定义set和get方法
	public String getComName() {
		return comName;
	}
	public void setComName(String comName) {
		this.comName = comName;
	}
	public int getComScore() {
		return comScore;
	}
//	public void setComScore(int comScore) {
//		this.comScore = comScore;
//	}
	public void setComScore() {
		this.comScore ++;
	}
	public int getComQuan() {
		//补充代码
		//剪刀0 石头1 布2
		this.comQuan = (int)(Math.random()*3);//[0,1,2]	
		return comQuan;
	}
}
