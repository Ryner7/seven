/**
 * Created by ryoto on 2017/10/17.
 */
public class SevensAgent {
	String name = "default";
	
	SevensAgent() {
		name="default";
	}
	
	Card strategy(Sevens sevens,int printDepth) {
		return simpleStrategy(sevens);
	}
	
	Card simpleStrategy(Sevens sevens) {
		return simpleStrategy(sevens.playableCards().getIntersection(sevens.turnPlayer.hand));
	}
	
	Card simpleStrategy(Cards playable) {
		playable.sortRankSuit();
		Cards sortedCards = new Cards();
		Card max;
		while (playable.size() > 0) {
			max = playable.get(0);
			for (Card card : playable) {
				if (Math.abs(max.rank - 7) < Math.abs(card.rank - 7)) {
					max = card;
				}
			}
			sortedCards.add(max);
			playable.remove(max);
		}
		while (sortedCards.size() > 0) playable.add(sortedCards.remove(0));
		// 入出力エラーがありうる
		if (playable.size() < 1) {
			for ( ; ; ) {
				System.out.print(" ");
			}
		}
		return playable.get(0);
	}
	
	SevensAgent deepCopy() {
		SevensAgent agent = new SevensAgent();
		return agent;
	}
	String getName(){
		return this.name;
	}
}
