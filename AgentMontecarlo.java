import java.util.ArrayList;

/**
 * Created by ryoto on 2017/10/30.
 */
public class AgentMontecarlo extends AgentSevens {
	String name = "monte ";
	AgentMontecarlo() {
		name = "monte ";
	}
	
	Card strategy(Sevens sevens, int printDepth) {
		Cards playableAndHold = sevens.playableCards().getIntersection(sevens.turnPlayer.hand);
		if (sevens.turnPlayer.hand.size() == 1 && playableAndHold.size() == 1) return playableAndHold.get(0);
		int M = Test.M;
		if (sevens.turnPlayer.nums.get(Sevens.PASS_INDEX) > 1) {
			playableAndHold.add(Card.createCard(Card.PASS_RANK, Card.SPECIAL_SUIT));
		}
		ArrayList<AgentSevens> agents = new ArrayList<>();
		for (int index = 0 ; index < sevens.players.size() ; index++) {
			agents.add(new AgentSevens());
		}
		Sevens simSevens;
		ArrayList<Integer> playersHandSize = new ArrayList<>();
		for (Player p : sevens.players) playersHandSize.add(p.hand.size());
		//Player player;
		Cards cards = new Cards();
//		Result result;
		ArrayList<Integer> scores ;
		MontecarloSimulation montecarloSimulation =montecarlo(sevens, printDepth, playableAndHold, M, agents, playersHandSize, cards);
		scores= montecarloSimulation.myScores;
		
		simHistories.add(montecarloSimulation.histories);
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
	
	MontecarloSimulation montecarlo(Sevens sevens, int printDepth, Cards playableAndHold, int m, ArrayList<AgentSevens> agents, ArrayList<Integer> playersHandSize, Cards cards) {
		int score;
		Sevens simSevens;
		Player player;
		Result result;
		String string="";
		ArrayList<Integer>scores = new ArrayList<>();
		MontecarloSimulation montecarloSimulation =new MontecarloSimulation();
		History history;
		ArrayList<History> histories=new ArrayList<>();
		Cards simCards;
		for (Card card : playableAndHold) {
			score = 0;
			for (int loop = 0 ; loop < this.simNum / playableAndHold.size() ; loop++) {
				simSevens = new Sevens();
				simSevens.setupSevens(sevens.players, sevens.deck, sevens.layout,sevens.allCards, (sevens.fakeTurn + 1) % sevens.players.size(), sevens.totalTurn + 1, MyUtil.SIM, sevens.playersOrder, sevens.history, agents);
				for (int index = 0 ; index < sevens.players.size() ; index++) {
					player = simSevens.players.get(index);
					if (sevens.playersOrder.get(sevens.fakeTurn) != index) {
						cards = cards.getUnion(player.hand);
						player.hand = new Cards();
					}
				}
				cards.shuffle();
				simCards=cards.deepCopy();
				for (int index = 0 ; index < sevens.players.size() ; index++) {
					if (sevens.playersOrder.get(sevens.fakeTurn) == index) continue;
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
				history.addPage(sevens.fakeTurn, sevens.totalTurn, Cards.getReadonlyCard(card), string, simSevens.players);
				simSevens.history = history;
				simSevens.history.simTurn = sevens.totalTurn;
				result = simSevens.startSevens(printDepth + 1);
				int tmp = scores.size() - 1;
				score += result.scores.get(sevens.playersOrder.get(sevens.fakeTurn));
				result.history.scores=result.scores;
				histories.add(result.history);
			}
			scores.add(score);
		}
		montecarloSimulation.histories=histories;
		montecarloSimulation.myScores =scores;
		return montecarloSimulation;
	}
	
	String getName() {
		return name;
	}
	
	class MontecarloSimulation {
		ArrayList<History> histories;
		ArrayList<Integer> myScores;
	}
}
