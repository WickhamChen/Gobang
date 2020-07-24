package kcsj.game;

import javax.swing.JOptionPane;

public class Person {
	
	private String perName;  //游戏者姓名
	private int perScore = 0;  //游戏者分数
	private int perQuan;   //游戏者出的拳
	
	//给perName、perScore、perQuan变量定义set和get方法
	public String getPerName() {
		return perName;
	}
	public void setPerName(String perName) {
		this.perName = perName;
	}
	public int getPerScore() {
		return perScore;
	}
//	public void setPerScore(int perScore) {
//		this.perScore = perScore;
//	}
	public void setPerScore() {
		this.perScore ++;
	}
	public int getPerQuan() {
		//如果不输入的话, 或者输入非法, 默认是0, 
		int res = 0;
		String str=JOptionPane.showInputDialog("输入您想出的拳：0石头 1剪刀 2布");
		if (str != null && str.length() != 0) {
			res = Integer.parseInt(str);
			if (res < 0 || res > 2) {
				res = 0;
			}
		}
		return res;
	}
}
