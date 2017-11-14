import com.sun.jndi.toolkit.ctx.AtomicDirContext;

import java.util.ArrayList;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by ryoto on 2017/10/17.
 */
public class Test {
	static final int M = 10;//シミュレーション回数
	static final int gameNum=10;//ゲーム回数
	static final boolean connected1And14 = true;
	static final int cardSize = 52;
	
	//static final int C=10;
	public static void main(String args[]) {
		Calendar cal = Calendar.getInstance();
		String fileName = "./log/"
				+ cal.get(Calendar.MONTH) + "_"
				+ cal.get(Calendar.DAY_OF_MONTH) + "_"
				+ cal.get(Calendar.HOUR_OF_DAY) + "_"
				+ cal.get(Calendar.MINUTE) + "_"
				+ cal.get(Calendar.SECOND) + ".txt";
		try {
			FileWriter fw = new FileWriter(fileName, true);
			ArrayList<Double> results = new ArrayList<>();
			for (int count = 0 ; count < 5 ; count++) {
				results.add(0.0);
			}
			for (int count = 1 ; count < 4 ; count++) {
				
				int[] Agents = {0, 0, 4 - count, count, 0};
				//int[] Agents = {4, 0, 0, 0, 0};
				ArrayList<Double> agentsResult = test(gameNum, Agents, fw);
				for (int index = 0 ; index < agentsResult.size() ; index++) {
					Double agentResult = agentsResult.get(index);
					results.set(index, agentResult + results.get(index));
				//	System.out.println(results.get(index));
				}
			}
			MyUtil.always.pln();
			fw.write("\n");
			for (int index = 0 ; index < results.size() ; index++) {
				switch (index) {
					case -1:
						//player.agent=new AgentManual();
						break;
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
					default:
						MyUtil.always.p("another ");
						fw.write("another ");
						break;
				}
				MyUtil.always.p(String.format("%.5f\n",(results.get(index)/3)));
				fw.write(String.format("%.5f\n",(results.get(index)/3)));
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
					player.agent = new AgentMontecarlo();
					break;
				case 3:
					player.agent = new AgentUpp();
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
			score=((double) 2 * scores.get(count)) / (N * (players.size() - 1));
			MyUtil.always.pf("%.5f\n", score);
			fw.write(String.format("%.5f\n", score));
			agentsResult.set(agentList.get(count), score+agentsResult.get(agentList.get(count)));
		}
		fw.write("\n");
		for (count = 0; count < argAgentList.length ; count++) {
			if (argAgentList[count] == 0) {
			
			} else {
				agentsResult.set(count, agentsResult.get(count) /  argAgentList[count]);
			//	System.out.println(agentsResult.get(count)+" //");
			}
		}
		return agentsResult;
//		for (int alpha = 0; alpha < players.size(); alpha++) {
//			player = players.get(alpha);
//			MyUtil.always.p(player.name + " ");
//			for (Integer time : player.rankTimes(sevens.ps.size())) {
//				System.out.printf("%3f ", ((double) time) / (N));
//			}
//			myUtil.pln(" " + playersCopy.get(alpha).agent.getName());
//		}
	}
}
