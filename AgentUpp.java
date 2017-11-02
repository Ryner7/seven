import java.util.ArrayList;

/**
 * Created by ryoto on 2017/10/30.
 */
public class AgentUpp extends AgentMontecarlo {
	//String name = "upp   ";
	
	AgentUpp() {
		name = "upp   ";
	}
	
	Card strategy(Sevens sevens, int printDepth) {
		Cards playableAndHold = sevens.playableCards().getIntersection(sevens.turnPlayer.hand);
		if (sevens.turnPlayer.hand.size() == 1 && playableAndHold.size() == 1) return playableAndHold.get(0);
		int M = Test.M;
		if (sevens.turnPlayer.nums.get(Sevens.PASS_INDEX) > 1) {
			playableAndHold.add(Card.createCard(Card.PASS_RANK, Card.SPECIAL_SUIT));
		}
		ArrayList<SevensAgent> agents = new ArrayList<SevensAgent>();
		for (int index = 0 ; index < sevens.players.size() ; index++) {
			agents.add(new SevensAgent());
		}
		Sevens simSevens;
		ArrayList<Integer> playersHandSize = new ArrayList<Integer>();
		for (Player p : sevens.players) playersHandSize.add(p.hand.size());
		Player player;
		Cards cards = new Cards();
		Result result;
		ArrayList<Integer> scores = new ArrayList<Integer>();
		int score = 0;
		History history;
		String string = "";
		ArrayList<History> histories = new ArrayList<History>();
		simHistories.add(histories);
		for (Card card : playableAndHold) {
			score = 0;
			for (int loop = 0 ; loop < M / playableAndHold.size() ; loop++) {
				simSevens = new Sevens();
				simSevens.setupSevens(sevens.players, sevens.deck, sevens.layout, (sevens.turn + 1) % sevens.players.size(), sevens.totalTurn + 1, MyUtil.SIM, sevens.playersOrder, sevens.history, agents);
				for (int index = 0 ; index < sevens.players.size() ; index++) {
					player = simSevens.players.get(index);
					if (sevens.turn != index) {
						cards = cards.getUnion(player.hand);
						player.hand = new Cards();
					}
				}
				cards.shuffle();
				
				for (int index = 0 ; index < sevens.players.size() ; index++) {
					if (sevens.turn == index) continue;
					player = simSevens.players.get(index);
					for (int count = 0 ; count < playersHandSize.get(index) ; count++) {
						if (playersHandSize.get(index) == 0) break;
						player.hand.add(cards.remove(0));
					}
					player.hand.sortSuitRank();
				}
				if (card.rank == Card.PASS_RANK && card.suit == Card.SPECIAL_SUIT) {
					simSevens.turnPlayer.nums.set(Sevens.PASS_INDEX, simSevens.turnPlayer.nums.get(Sevens.PASS_INDEX) - 1);
					string = "pass";
				} else {
					simSevens.layout.add(card);
					simSevens.turnPlayer.hand.removeCard(card);
					string = "play";
				}
				history = sevens.history.deepCopy();
				history.addPage(sevens.turn, sevens.totalTurn, card, string, simSevens.players);
				simSevens.history = history;
				simSevens.history.simTurn = sevens.totalTurn;
				result = simSevens.startSevens(MyUtil.SIM);
				int tmp = scores.size() - 1;
				score += result.scores.get(sevens.playersOrder.get(sevens.turn));
				histories.add(result.history);
			}
			scores.add(score);
		}
//		simHistories.add(histories);
		int max = -1;
		int maxIndex = -1;
		for (int index = 0 ; index < scores.size() ; index++) {
			if (max < scores.get(index)) {
				max = scores.get(index);
				maxIndex = index;
			}
		}
		return playableAndHold.get(maxIndex);
	}
	
	String getName() {
		return name;
	}
	
	double stateValueEstimation(Sevens sevens, int printDepth, Cards playableAndHold, int M,ArrayList<SevensAgent> agents, ArrayList<Integer>playersHandSize,Cards cards){
		ArrayList<Integer> scores=montecarlo(sevens, printDepth, playableAndHold, M, agents, playersHandSize, cards).scores;
		int max = -1;
		int maxIndex = -1;
		for (int index = 0 ; index < scores.size() ; index++) {
			if (max < scores.get(index)) {
				max = scores.get(index);
				maxIndex = index;
			}
		}
		return (double)max/M;
	}
	
	Cards probabilityEstimation(ArrayList<History> ns){//世界の確率推定
		int size=ns.size();
		ArrayList<Integer> _W=new ArrayList<Integer>();
		ArrayList<Integer> _tn=new ArrayList<Integer>();
		ArrayList<Boolean> _Fn=new ArrayList<Boolean>();
		int c=1;
		
		for(int index=0;index<size;index++){
			_W.add((_Fn.get(index))?(1+_tn.get(index)*c):0);
		}
	//	ArrayList<Cards>
		return null;
	}
	class cardsProbability{
		ArrayList<Cards> cardsList;
	}
	Simulation upp(){
		return null;
	}
	
}