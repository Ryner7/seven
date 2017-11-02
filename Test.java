import java.util.ArrayList;

/**
 * Created by ryoto on 2017/10/17.
 */
public class Test {
	static final int M=1000;
	public static void main(String args[]) {
		test(100);
	}
	public static void test(int N){
		MyUtil.always.pln("\n---Sevens---");
		ArrayList<Player> players = new ArrayList<Player>();
		int count;
		int P = 4;
		Player player;
		for (count = 0; count < P ; count++) {
			players.add(new Player(count));
		}
		int[] agentList = {1, 1, 1, 2};
		for (count = 0; count < P ; count++) {
			player = players.get(count);
			
			switch (agentList[count]) {
				case -1:
					//player.agent=new AgentManual();
					break;
				case 0:
					player.agent = new SevensAgent();
					break;
				case 1:
					player.agent=new AgentRandom();
					break;
				case 2:
					player.agent=new AgentMontecarlo();
					break;
				case 3:
					player.agent=new AgentUpp();
					break;
				default:
					player.agent = new SevensAgent();
					break;
			}
			player.name = new String(player.agent.getName() + " " + count);
			player.key=count;
		}
		Sevens sevens = new Sevens();
		Result result;
		ArrayList<Integer> scores = new ArrayList<Integer>();
		for (Player p : players) {
			scores.add(0);
		}
		for (int loop = 0 ; loop < N ; loop++) {
			MyUtil.always.p(  "---"+loop + "---\n");
			result = sevens.playSevens(players);
			for (int index = 0 ; index < scores.size() ; index++) {
				scores.set(index, scores.get(index) + result.scores.get(index));
			}
		}
		MyUtil.always.pln();
		for (count = 0; count < scores.size() ; count++) {
			MyUtil.always.p(players.get(count).name + " " );
			MyUtil.always.pf("%.5f\n",((double) 2*scores.get(count)) / (N * (players.size() - 1)));
		}
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
