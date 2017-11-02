import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ryoto on 2017/10/17.
 */
public class SevensAgent {
	String name = "default";
	ArrayList<ArrayList<History>> simHistories = new ArrayList<ArrayList<History>>();
	
	SevensAgent() {
		name = "default";
	}
	
	Card strategy(Sevens sevens, int printDepth) {
		return simpleStrategy(sevens);
	}
	
	Card simpleStrategy(Sevens sevens) {
		return simpleStrategy(sevens.playableCards().getIntersection(sevens.turnPlayer.hand));
	}
	
	Card simpleStrategy(Cards playable) {//出せるカードの中から最も7から離れたカードを出す
		playable.sortRankSuit();
		Cards maxCards = new Cards();
		Card max;
		
		max = playable.get(0);
		for (Card card : playable) {
			if (Math.abs(max.rank - 7) < Math.abs(card.rank - 7)) {
				max = card;
			}
		}
		for(Card card:playable){
			if(Math.abs(max.rank - 7) == Math.abs(card.rank - 7)){
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
	
	SevensAgent deepCopy() {
		SevensAgent agent = new SevensAgent();
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
