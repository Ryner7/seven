import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ryoto on 2017/10/17.
 */
public class AgentSevens {
	String name = "default";
	ArrayList<ArrayList<History>> simHistories = new ArrayList<ArrayList<History>>();
	ArrayList<History> prevSimHistories;
	static int simNum=0;
	AgentSevens() {
		name = "default";
	}
	
	Card strategy(Sevens sevens, int printDepth) {
		return simpleStrategy(sevens);
	}
	
	Card simpleStrategy(Sevens sevens) {//出せるカードの中から最も7から離れたカードを出す
		Cards playable = sevens.playableCards().getIntersection(sevens.turnPlayer.hand);
		playable.sortRankSuit();
		Cards maxCards = new Cards();
		Card max;
		Cards layout = sevens.layout;
		max = playable.get(0);
		for (Card card : playable) {
			if (distanceFromSeven(layout, max) < distanceFromSeven(layout, card)) {
				max = card;
			}
		}
		for (Card card : playable) {
			if (distanceFromSeven(layout, max) == (distanceFromSeven(layout, card))) {
				maxCards.add(card);
			}
		}
		// 入出力エラーがありうる
		if (playable.size() < 1) {
			for ( ; ; ) {
				System.out.print(" ");
			}
		}
		Random rnd = new Random();
		int index = rnd.nextInt(maxCards.size());
		return maxCards.get(index);
	}
	
	int distanceFromSeven(Cards layout, Card target) {
		if (!Test.connected1And13) {
			return Math.abs(target.rank - 7);
		} else {
			boolean[] reachEnd = {false, false, false, false, false, false, false, false};
			int flag = 0;
			Cards cards;
			cards = new Cards();
			int direction = -1;
			int pos = 7, suit = target.suit;
			while (pos != 0) {
				if (!layout.containsCard(pos, target.suit)) {
					flag = 1;
					break;
				}
				//myUtil.pln();
				pos += direction;
			}
			boolean[] connect = {false, false};
			if (flag == 0) {
				connect[0] = true;
			}
			pos = 7;
			direction = 1;
			flag = 0;
			while (pos != 0) {
				if (!layout.containsCard(pos, target.suit)) {
					flag = 1;
					break;
				}
				//myUtil.pln();
				pos += direction;
			}
			if (flag == 0) {
				connect[1] = true;
			}
			
			if (!connect[0] && !connect[1]) {
				return Math.abs(target.rank - 7);
			} else if (!connect[0] && connect[1]) {
				return Math.abs(target.rank + 6);
			} else if (connect[0] && !connect[1]) {
				return Math.abs(20 - target.rank);
			} else {
				for ( ; ; ) System.out.println("ERROR at SevensAgent");
				
			}
		}
	}
	
	AgentSevens deepCopy() {
		AgentSevens agent = new AgentSevens();
		return agent;
	}
	
	String getName() {
		return this.name;
	}
	
	ArrayList<History> beforeHistories(int myBuckTurn) {
		int turn = simHistories.size() - 1 - myBuckTurn;
		if (turn < 0 || myBuckTurn < 0) return new ArrayList<History>();
		return simHistories.get(simHistories.size() - 1 - myBuckTurn);
	}
}
