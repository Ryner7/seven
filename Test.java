import com.sun.jndi.toolkit.ctx.AtomicDirContext;

import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Scanner;

/**
 * Created by ryoto on 2017/10/17.
 * <p>
 * <p>
 * 忙しい人のためのIntelliJ IDEAショートカット集（´-`）
 * https://qiita.com/yoppe/items/f7cbeb825c071691d3f2
 */
public class Test {
	static final int M = 10;//シミュレーション回数
	static final int gameNum = 100;//ゲーム回数
	static final boolean connected1And13 = false;
	static final int cardSize = 52;
	static final int uppEstimationType = 0;
	static final int weightType = 0;
	static final int monteNum = 1000,
			uppNum = 1000,
			modNum = 1000;
	
	
	static  int uppVal = 1;
	
	
	static int Ms;
	static int playerNum = 4;
	
	//static final int C=10;
	public static void main(String args[]) {
		if(args.length<1){}
		else{uppVal=Integer.parseInt(args[0]);}
		
		Calendar cal = Calendar.getInstance();
		Scanner scan = new Scanner(System.in);
		String str = scan.next();
		String fileName = (System.getProperty("os.name").toLowerCase().startsWith("mac") ? "./log/" : "../log/")
				+ String.format("%02d", cal.get(Calendar.MONTH)) + "_"
				+ String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + "_"
				+ String.format("%02d", cal.get(Calendar.HOUR_OF_DAY)) + "_"
				+ String.format("%02d", cal.get(Calendar.MINUTE)) + "_"
				+ String.format("%02d", cal.get(Calendar.SECOND)) + ".txt";
		
		try {
			FileWriter fw = new FileWriter(fileName, true);
			ArrayList<Double> results = new ArrayList<>();
			fw.write(str);
			fw.write("simNum= " + M + "\n");
			fw.write("gameNum= " + gameNum + "\n");
			fw.write("uppEstimationType= " + uppEstimationType + "\n");
			fw.write("weightType= " + weightType + "\n");
			fw.write("uppVal= " + uppVal + "\n");
			fw.write("\n");
			for (int count = 0 ; count < 6 ; count++) {
				results.add(0.0);
			}
			for (int count = 1 ; count < 4 ; count++) {
				
				int[] Agents = {0, 0, count, 4 - count, 0, 0};
				//int[] Agents = {4, 0, 0, 0, 0,0};
				//int[] Agents = {0, 0, 3, 0, 0,1};
				
				ArrayList<Double> agentsResult = test(gameNum, Agents, fw);
				for (int index = 0 ; index < agentsResult.size() ; index++) {
					Double agentResult = agentsResult.get(index);
					results.set(index, agentResult + results.get(index));
					//System.out.println(results.get(index));
				}
			}
			MyUtil.always.pln();
			fw.write("\n");
			for (int index = 0 ; index < results.size() ; index++) {
				switch (index) {
					
					case 0:
						MyUtil.always.p("default ");
						fw.write("default ");
						break;
					case 1:
						MyUtil.always.p("random  ");
						fw.write("random  ");
						break;
					case 2:
						MyUtil.always.p("monte   ");
						fw.write("monte   ");
						break;
					case 3:
						MyUtil.always.p("upp     ");
						fw.write("upp     ");
						break;
					case 4:
						MyUtil.always.p("modupp  ");
						fw.write("modupp     ");
						break;
					case 5:
						MyUtil.always.p("manual  ");
						fw.write("manual  ");
						break;
					default:
						MyUtil.always.p("another ");
						fw.write("another ");
						break;
				}
				MyUtil.always.p(String.format("%.5f \t", (results.get(index) / 3)));
				fw.write(String.format("%.5f \t", (results.get(index) / 3)));
				switch (index) {
					case -1:
						//player.agent=new AgentManual();
						break;
					case 0:
						MyUtil.always.p("" + AgentSevens.simNum);
						fw.write("" + AgentSevens.simNum);
						break;
					case 1:
						MyUtil.always.p("" + AgentRandom.simNum);
						fw.write("" + AgentRandom.simNum);
						break;
					case 2:
						MyUtil.always.p("" + AgentMontecarlo.simNum);
						fw.write("" + AgentMontecarlo.simNum);
						break;
					case 3:
						MyUtil.always.p("" + AgentUpp.simNum);
						fw.write("" + AgentUpp.simNum);
						break;
					case 4:
						MyUtil.always.p("" + AgentModupp.simNum);
						fw.write("" + AgentModupp.simNum);
						break;
					case 5:
						MyUtil.always.p("" + AgentManual.simNum);
						fw.write("" + AgentManual.simNum);
						break;
					default:
						MyUtil.always.p("another ");
						fw.write("another ");
						break;
				}
				MyUtil.always.p("\n");
				fw.write("\n");
			}
			
			fw.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public static ArrayList<Double> test(int N, int[] argAgentList, FileWriter fw) throws IOException {
		MyUtil.always.pln("\n---Sevens---");
		ArrayList<Player> players = new ArrayList<Player>();
		int count;
		int P = 4;
		double score;
		Player player;
		Result.EstimationAccuracy.init();
		for (count = 0; count < P ; count++) {
			players.add(new Player(count));
		}
		ArrayList<Double> agentsResult = new ArrayList<>();
		ArrayList<Integer> agentList = new ArrayList<>();
		for (count = 0; count < argAgentList.length ; count++) {
			int num = argAgentList[count];
			while (num > 0) {
				agentList.add(count);
				num--;
			}
		}
		//int[] agentList = {1, 1, 1, 2};
		for (count = 0; count < P ; count++) {
			player = players.get(count);
			agentsResult.add(0.0);
			switch (agentList.get(count)) {
				case -1:
					//player.agent=new AgentManual();
					break;
				case 0:
					player.agent = new AgentSevens();
					break;
				case 1:
					player.agent = new AgentRandom();
					break;
				case 2:
					AgentMontecarlo monteAgent = new AgentMontecarlo();
					monteAgent.monteSimNum = monteNum;
					player.agent = monteAgent;
					break;
				case 3:
					AgentUpp uppAgent = new AgentUpp();
					uppAgent.uppSimNum = uppNum;
					player.agent = uppAgent;
					break;
				case 4:
					AgentModupp modAgent = new AgentModupp();
					modAgent.modSimNum = modNum;
					player.agent = modAgent;
					break;
				case 5:
					AgentManual manual = new AgentManual();
					//manual=modSimNum = modNum;
					player.agent = manual;
					break;
				default:
					player.agent = new AgentSevens();
					break;
			}
			player.name = new String(player.agent.getName() + " " + count);
			player.key = count;
		}
		Sevens sevens = new Sevens();
		Result result;
		ArrayList<Integer> scores = new ArrayList<Integer>();
		for (Player p : players) {
			scores.add(0);
		}
		
		for (int loop = 0 ; loop < N ; loop++) {
			MyUtil.always.p("---" + loop + "---\n");
			result = sevens.playSevens(players);
			for (int index = 0 ; index < scores.size() ; index++) {
				scores.set(index, scores.get(index) + result.scores.get(index));
			}
		}
		MyUtil.always.pln();
		for (count = 0; count < scores.size() ; count++) {
			MyUtil.always.p(players.get(count).name + " ");
			fw.write(players.get(count).name + " ");
			score = ((double) 2 * scores.get(count)) / (N * (players.size() - 1));
			MyUtil.always.pf("%.5f\n", score);
			fw.write(String.format("%.5f\n", score));
			int agent = agentList.get(count);
			agentsResult.set(agent, score + agentsResult.get(agent));
		}
		fw.write("\n");
		for (count = 0; count < argAgentList.length ; count++) {
			if (argAgentList[count] == 0) {
			
			} else {
				agentsResult.set(count, agentsResult.get(count) / argAgentList[count]);
				//System.out.println(agentsResult.get(count)+" //");
			}
		}
		Result.EstimationAccuracy.outputData();
		return agentsResult;
		//for (int alpha = 0; alpha < players.size(); alpha++) {
		//player = players.get(alpha);
		//MyUtil.always.p(player.name + " ");
		//for (Integer time : player.rankTimes(sevens.ps.size())) {
		//System.out.printf("%3f ", ((double) time) / (N));
		//}
		//myUtil.pln(" " + playersCopy.get(alpha).agent.getName());
		//}
	}
	
	static double weight(int turn, int max) {
		int xxx = (turn) / max;//(52+playerNum*3);
		switch (weightType) {
			default:
			case 0:
				return 1;
			case 1:
				return Math.sin((Math.PI / 2) * xxx);
			case 2:
				return Math.cos((Math.PI / 2) * xxx);
			case 3:
				return Math.sin((Math.PI) * xxx);
			case 4:
				return Math.cos((Math.PI) * xxx);
			case 5:
				return xxx;
			case 6:
				return 1 - xxx;
		}
	}
}
