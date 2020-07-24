package kcsj.game;
/***
 * @time 2020年7月22日15:39:18
 * 
 * 1. 通过猜拳决定电脑和游戏者谁先落子, 如果平局,则再进行一局猜拳 对玩家输入的数据进行判断, 如果非法 或 为空(直接点击确定) 或者点击取消, 则为 0 
 * 2. 设置游戏难度为三个等级, 简单, 中等, 高难 棋盘的尺寸分别为 13 11 9 对玩家输入的数据进行判断, 如果非法 或为空(直接点击确定) 或者点击取消, 则为棋盘默认尺寸为 13 
 * 3. 添加电脑胜利判断方法
 * 4. 游戏胜负后, 是否继续游戏, 并在计分板上修改相应的胜局得分
 * 5. 添加和局的情况, 当棋盘的棋子已经布满了, 就显示提示框提示当前和局
 * 6. 创新: 添加computerAI, 通过遍历所有的赢法棋谱, 并计算某点落子的得分情况, 决策最高分的点就是电脑落子的点。
 * 			电脑胜利的分数激励, 大于围堵玩家(也就是落子在玩家棋谱)的分数激励
 * 			 如果当前棋盘已经胜利棋谱已不存在(也就是当前棋盘为和局), 则电脑通过遍历剩余的位置, 有空位就落子
 * 
 * 小细节: 棋子在线上中间, 添加了判断胜负的条件,当鼠标在画布内，才监听 落子.不会数组越界。
 */
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Game extends JFrame{
	
	JFrame frame = new JFrame("五子棋游戏");  //定义一个窗口对象
	Computer computer = new Computer();  //创建Computer对象
	Person person = new Person();	//创建Person对象
	
	int viewTop = 150;//棋盘窗口顶部距离
	
	int qipanSize = 13;	//设置棋盘尺寸 //奇数尺寸的棋盘
	int quanResult = 0;  //存储猜拳结果，0表示电脑胜利，1表示游戏者胜利
	
	int[][]  datas = new int[qipanSize][qipanSize];  //10*10/qipanSize * qipanSize二维数组，存储落子，0：无落子，1：黑子落子（电脑），2:白子落子（游戏者）
	int x,y;  //玩家落子点

	JPanel jPanel = null;  //绘图的画布	
//-------------------------------------------------
	boolean[][][] wins = new boolean[qipanSize][qipanSize][580];  //设置赢法棋谱的数组
	int cnt = 0; //赢的可能有cnt种
	//一维数组，数组下标 k 代表 第 k 种赢法。数组值为当前已经练成棋子的数量  computerWin[k] = 2;// computer 在第 k 种赢法，已经下了2 颗棋子
	int[] computerWin = new int[580];
	int[] personWin = new int[580];
	
	//二维数组，棋谱，qipanSize * qipanSize的值，数组下标为当前 的棋子位置，数组的值 为当前权值
	int[][] computerScore = new int [qipanSize][qipanSize];
	int[][] personScore = new int [qipanSize][qipanSize];
	
	//最高分数
	int maxScore;
	//保存计算机最高分数的坐标，也就是计算机最终落子
	int u, v;
	
	int winGame = 2;// 1代表电脑获胜，2代表玩家获胜
	
	int step;//计步
//-------------------------------------------------
	public static void main(String[] args) {
		Game game = new Game();
		//game.inputName();       //1、弹出输入框，输入游戏者姓名
		//game.setLevel();        //2、 设置难度1-3 简单中等 高难,修改棋盘尺寸
		game.setGameFrame();    //3、创建画布
		game.setFrame();        //4、创建游戏窗口，并将绘制好的画布添加到游戏窗口中
		//game.caiQuan();         //5、猜拳决定谁先落子
		game.initWin();         //6、初始化赢法数组
		game.startGame();       //7、开始游戏
	}
	
//--------------弹出输入框，输入游戏者姓名-----------
	public void inputName(){
		String name = JOptionPane.showInputDialog("请输入你的姓名，开始游戏：");
		person.setPerName(name);	
	}
	
//-----------------创建一块画布，重写paint方法，用Graphics g画笔在画布上画画--------------
	public void setGameFrame(){
		 jPanel = new JPanel(){
			@Override
			public void paint(Graphics g) {
				//-----画背景图-----
				BufferedImage image = null;
				try {
					image=ImageIO.read(new File("bg_1.jpg"));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				g.drawImage(image, 0, 0, this);	
				g.setColor(Color.white);
				g.setFont(new Font("黑体", 40, 40));
				g.drawString(computer.getComName()+"\n VS \n"+person.getPerName(),300,50);
				
				g.setColor(Color.green);
				//----------score----------分数-------------
				g.drawString(computer.getComScore()+"\n : \n"+person.getPerScore(),400,100);
				
				/*//-----------悔棋--------------------------
				 * g.drawString("悔棋", 865, 150); //斜线 g.drawLine(865, 115, 945, 155);
				 * //两条横线，两条竖线 g.drawLine(865, 115, 945, 115); g.drawLine(865, 155, 945, 155);
				 * g.drawLine(865, 115, 865, 155); g.drawLine(945, 115, 945, 155);
				 */
				
				//-----画棋盘 10*10-----qipanSize * qipanSize
				g.setColor(Color.black);
				for(int i=0;i < datas.length;i++){
					g.drawLine(50, viewTop+60*i, 50+(qipanSize-1)*60, viewTop+60*i);
				}
				for(int j=0;j < datas.length;j++){
					g.drawLine(50+j*60, viewTop, 50+j*60, viewTop+(qipanSize-1)*60);
				}
				//-----画棋子------qipanSize * qipanSize
				for(int i=0;i<datas.length;i++){
					for(int j=0;j<datas.length;j++){
						if(datas[i][j]==1){
							g.setColor(Color.black);
							g.fillOval(i*60+50-15, j*60+viewTop-15, 30, 30);
							g.setColor(Color.blue);
							g.drawOval(i*60+50-15, j*60+viewTop-15, 30, 30);
						}else if(datas[i][j]==2){
							g.setColor(Color.white);
							g.fillOval(i*60+50-15, j*60+viewTop-15, 30, 30);
						}
					}
				}
			}  
		};	
	}
	
//--------------创建游戏窗口-------------
	public void setFrame(){
		//设置窗口大小
		frame.setSize(840, 950);
		//设置窗口显示坐标
		frame.setLocation(650, 50);
		//设置窗口不可缩小放大
		frame.setResizable(false);
		//设置窗口关闭程序也一起结束运行
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);			
		//将画布放在窗口中
		frame.add(jPanel);
		//设置窗口显示 必须放在最后
		frame.setVisible(true);
	}
//--------------设置 level ---------------------
	public void setLevel() {
		//简单，中等，高难
		//null 是没输入。
		String str = JOptionPane.showInputDialog("设置棋盘难度(1简单 2中等 3高难)");
		if (str != null && str.length() != 0) {
			int level =  Integer.parseInt(str);
			if(level == 1) {
				qipanSize = 13;
			}else if(level == 2) {
				qipanSize = 11;
			}else if(level == 3) {
				qipanSize = 9;
			}else {
				qipanSize = 13;//输入不合法，全部13处理
			}
		}else {
			qipanSize = 13;
		}
		datas = new int[qipanSize][qipanSize]; 
	}
//---------------通过猜拳决定谁先落子----------------
	public void caiQuan(){//quanResult = 0;  //存储猜拳结果，0表示电脑胜利，1表示游戏者胜利
		int perRes = person.getPerQuan();
		int comRes = computer.getComQuan();
		//0:石头; 1:剪刀; 2:布	
		if (perRes == comRes) {
			//和局规则：1.随机玩家或者电脑先手 2.再猜拳//1.随机先手quanResult = (int)(Math.random()*2);//[0,1]//2.回调
			JOptionPane.showMessageDialog(frame, "当前和局，请再猜拳一次");
			caiQuan();
		}else if(comRes == 0 && perRes == 1 || comRes == 1 && perRes == 2 || comRes == 2 && perRes == 0) {
			quanResult = 0;  //存储猜拳结果，0表示电脑胜利，1表示游戏者胜利
			JOptionPane.showMessageDialog(frame, "电脑先手");
		}else {
			quanResult = 1;
			JOptionPane.showMessageDialog(frame, "玩家先手");
		}
	}
//-------------------开始游戏，落子--------------------
	public void startGame(){
		//根据猜拳结果判断谁先落子
		if(quanResult==0){//电脑先落子 调用comLuoZi()//comLuoZi();//落子
			//初始落子，让他在棋盘中间下。奇数尺寸的棋盘
			datas[datas.length/2][datas.length/2] = 1;
			frame.repaint();
			judgeStep();
		}
		//游戏者落子
		//给窗口添加鼠标点击的事件监听，每次点击都触发重写的mousePressed事件
		frame.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	x=e.getX();
            	y=e.getY();
            	//让鼠标监听的范围在下棋内
            	if(x <= 50+60*(qipanSize) && x >= 35 && y >= (viewTop-15) && y <= viewTop+60*(qipanSize)) {
            		//判断点击的点离哪个交叉点（落子点）最近
                	x=Math.round((x-50)/60);
                	y=Math.round((y-viewTop)/60);
                	if(datas[x][y]==0){
                		datas[x][y] = 2;
                		frame.repaint();//刷新画布
                		judgeStep();
                		System.out.println("下白子, 白子下完"+step);
                		personDownChess_judgeWin(person.getPerName());//判断玩家是否输赢
                    	computerAI();//电脑落子
                	} else {
                		JOptionPane.showMessageDialog(frame,"已经落子的地方不能下棋，请重新落子");
                	}
            	}
            }
        });	
	
	}
//---------计步-----------
	public void judgeStep() {
		step++;
		if (step == qipanSize * qipanSize) {
			step = 0;
			JOptionPane.showMessageDialog(frame, "●ω● 旗鼓相当鸭~  当前和局!");
			playAgain();
		}
	}
//------新的一局---------
	public void playAgain() {
		//清除步数
		//清除棋盘数据
		for (int i = 0; i < datas.length; i++) {
			for (int j = 0; j < datas.length; j++) {
				datas[i][j] = 0;
			}
		}
		//清除最高分，计算机坐标数据 //清除分数 computerScore personScore
		//清除当前落子 computerWin personWin
		for (int i = 0; i < 580; i++) {
			computerWin[i] = 0;
			personWin[i] = 0;
		}
		
		frame.repaint();
		
		if(JOptionPane.showConfirmDialog(frame, "(づ￣3￣)づ╭❤～还玩么? 胜者先手哦~") != 0) System.exit(0);
		
		//判断是谁赢的，下一句就先手
		if (winGame == 1) {
			datas[datas.length/2][datas.length/2] = 1;
		}
		frame.repaint();
	}
//---------------------------------------
	public void initWin() {//初始化棋盘赢法数组
		//所有的横线
		for (int i = 0; i < qipanSize; i++) {
			for (int j = 0; j < qipanSize-4; j++) {
				for (int k = 0; k < 5; k++) {
					wins[i][j+k][cnt] = true;
				}
				cnt++;
			}
		}
		//所有的竖线
		for (int i = 0; i < qipanSize; i++) {
			for (int j = 0; j < qipanSize-4; j++) {
				for (int k = 0; k < 5; k++) {
					wins[j+k][i][cnt] = true;
				}
				cnt++;
			}
		}
		//所有的正斜线
		for (int i = 0; i < qipanSize-4; i++) {
			for (int j = 0; j < qipanSize-4; j++) {
				for (int k = 0; k < 5; k++) {
					wins[i+k][j+k][cnt] = true;
				}
				cnt++;
			}
		}
		//所有的反斜线
		for (int i = 0; i < qipanSize-4; i++) {
			for (int j = qipanSize-1; j > 3; j--) {
				for (int k = 0; k < 5; k++) {
					wins[i+k][j-k][cnt] = true;
				}
				cnt++;
			}
		}
		System.out.println("当前"+qipanSize+" * "+qipanSize+"棋盘的规模 解法共: "+cnt);
	}
	
	public void personDownChess_judgeWin(String str) {
		//玩家落子 判断输赢
		for (int k = 0; k < cnt; k++) {
			if (wins[x][y][k]) {
				personWin[k]++;
				computerWin[k] = -10000;//这个棋谱，玩家已落子。所以，这个棋谱电脑是不可以copy这个，设置为 6，异常处理，也就是 相对 对方而言少一种
				if (personWin[k] >= 5) {
					//第k种赢法，都5子，此时胜利
					//System.out.println("你赢了！！！！！！");
					JOptionPane.showMessageDialog(frame, "恭喜"+str+"获胜~");
					person.setPerScore();
					winGame = 2;
					playAgain();
				}
			}
		}
	}
	public void computerDownChess_judgeWin() {
		//计算机落子，和上面一样
		for (int k = 0; k < cnt; k++) {
			if (wins[u][v][k]) {//判断当前走的位置，是否有解，如果这个位置，的这个棋谱有解，就让玩家的棋子 = 6，
				computerWin[k]++;
				personWin[k] = -10000;
				if (computerWin[k] >= 5) {
					//第k中赢法，都实现了，
					//System.out.println("计算机赢了！！！！！！");
					JOptionPane.showMessageDialog(frame, "电脑获胜");
					computer.setComScore();
					winGame = 1;
					playAgain();
				}
			}
		}
	}
//----------五子棋，数组寻找------------------------------
	public void computerAI() {//五子棋的赢法数组
		
		//清除最高分，计算机坐标数据
		maxScore = 0;
		//出现无解的情况，棋子只能瞎走了，预防 3*3 / 5*5 等小棋盘极易出现错误
//		u = (int)(Math.random()*qipanSize);
//		v = (int)(Math.random()*qipanSize);
		//对上面的优化，遇到空位就可以下
		initPosition:
		for (int i = 0; i < datas.length; i++) {
			for (int j = 0; j < datas.length; j++) {
				if (datas[i][j] == 0) {
					u = i;
					v = j;
					break initPosition;
				}
			}
		}
		//清除分数 computerScore personScore  //由于定义数组的时候，就是以棋盘尺寸定义。所以遍历也使用棋盘尺寸
		for (int i = 0; i < qipanSize; i++) {
			for (int j = 0; j < qipanSize; j++) {
				computerScore[i][j] = 0;
				personScore[i][j] = 0;
			}
		}
		//x, y是用户点击落子
		for (int i = 0; i < qipanSize; i++) {
			for (int j = 0; j < qipanSize; j++) {
				if (datas[i][j] == 0) {//说明可以落子
					for (int k = 0; k < cnt; k++) {
						if (wins[i][j][k]) {	//第k种赢法
							//原则：电脑胜利的分数激励, 大于围堵玩家(也就是落子在玩家棋谱)的分数激励
							if (personWin[k] == 1) {	//玩家当前在相应的棋谱上已有1个落子
								personScore[i][j] += 200;	//加200分
							} else if (personWin[k] == 2) {	//玩家当前在相应的棋谱上已有2个落子
								personScore[i][j] += 400;
							} else if (personWin[k] == 3) {
								personScore[i][j] += 2000;
							}else if (personWin[k] == 4) {
								personScore[i][j] += 10000;
							}
							
							//电脑的落子的得分
							if (computerWin[k] == 1) {	//电脑当前在相应的棋谱上已有1个落子
								computerScore[i][j] += 220;
							} else if (computerWin[k] == 2) {	//电脑当前在相应的棋谱上已有2个落子
								computerScore[i][j] += 420;
							} else if (computerWin[k] == 3) {
								computerScore[i][j] += 2100;
							}else if (computerWin[k] == 4) {
								computerScore[i][j] += 20000;
							}
						}
					}
					//上面代码，遍历了 cnt 种棋谱，并对其进行加分增加权值，所以我们要分别取出两个权值的大小。来衡量 一个坐标，能让这个权值最大，
					//接下来。来通过 personScore 来衡量 最优解坐标
					if (personScore[i][j] > maxScore) {
						maxScore = personScore[i][j];
						u = i;
						v = j;
					}else if (personScore[i][j] == maxScore) {//当出现分值一样的时候，(找个特例) 我们需要进一步，通过computer 来衡量出最优坐标
						if (computerScore[i][j] > computerScore[u][v]) {
							u = i;
							v = j;
						}
					}
					//接下来。来通过 computerScore 来衡量 最优解坐标
					if (computerScore[i][j] > maxScore) {
						maxScore = computerScore[i][j];
						u = i;
						v = j;
					} else if (computerScore[i][j] == maxScore) {
						if (personScore[i][j] > personScore[u][v]) {
							u = i;
							v = j;
						}
					}
				}
			}
		}
		
		//上面循环得到 最优 u v
		if(datas[u][v] == 0){
			datas[u][v] = 1;
			frame.repaint();
			judgeStep();
			System.out.println("下黑子, 黑子下完"+step);
			computerDownChess_judgeWin();
		}

	}
}