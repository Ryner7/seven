import java.util.ArrayList;

/**
 * Created by ryoto on 2017/11/24.
 */
public class AgentCheat extends  AgentUpp {
	//String name = "upp   ";
	
	AgentCheat() {
		name = "cheat   ";
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
		ArrayList<Integer> scores;
		MontecarloSimulation simulation;
		
		simulation = upp(sevens, printDepth, playableAndHold, M, agents, playersHandSize, cards, sevens.history);
		MontecarloSimulation montecarloSimulation = montecarlo(sevens, printDepth, playableAndHold, M, agents, playersHandSize, cards);
		scores = simulation.myScores;
		prevSimHistories = montecarloSimulation.histories;
		simHistories.add(prevSimHistories);
		
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
	MontecarloSimulation upp(Sevens sevens, int printDepth, Cards playableAndHold, int m, ArrayList<AgentSevens> agents, ArrayList<Integer> playersHandSize, Cards cards, History realHistory) {
		int score;
		Sevens simSevens;
		Player player;
		Result result;
		String string = "";
		ArrayList<Integer> scores = new ArrayList<>();
		MontecarloSimulation simulation = new MontecarloSimulation();
		History history;
		ArrayList<History> histories = new ArrayList<>();
		Cards secret = new Cards();
		ArrayList<Integer> handsNum = new ArrayList<>();
		for (int index = 0 ; index < sevens.players.size() ; index++) {
			if (sevens.playersOrder.get(sevens.fakeTurn) == index) {
				handsNum.add(0);
			} else {
				secret = secret.getUnion(sevens.players.get(index).hand);
				handsNum.add(sevens.players.get(index).hand.size());
			}
		}
		ArrayList<ArrayList<ArrayList<Integer>>> probabilities = probabilityEstimation(prevSimHistories, handsNum, secret, realHistory, sevens.playersOrder.get(sevens.fakeTurn), sevens.totalTurn);
		ArrayList<Cards> world, worldCopy;
		
		for (Card card : playableAndHold) {
			score = 0;
			for (int loop = 0 ; loop < this.simNum / playableAndHold.size() ; loop++) {
				simSevens = new Sevens();
				simSevens.setupSevens(sevens.players, sevens.deck, sevens.layout, sevens.allCards, (sevens.fakeTurn + 1) % sevens.players.size(), sevens.totalTurn + 1, MyUtil.SIM, sevens.playersOrder, sevens.history, agents);
				for (int index = 0 ; index < sevens.players.size() ; index++) {
					player = simSevens.players.get(index);
					if (sevens.playersOrder.get(sevens.fakeTurn) != index) {
						cards = cards.getUnion(player.hand);
						player.hand = new Cards();
					}
				}
				cards.sortSuitRank();
				world = genWorld(sevens.players.size(), probabilities, handsNum);
				worldCopy = Cards.cardsListDeepCopy(world);
				Cards test = new Cards();
				for (int index = 0 ; index < sevens.players.size() ; index++) {
					world.get(index).sortSuitRank();
//					world.get(index).showCards(0);
//					MyUtil.always.pln();
//					if (sevens.playersOrder.get(sevens.fakeTurn) != index) {
					test = test.getUnion(worldCopy.get(index));
//					}
				}
				test.sortSuitRank();
				for (int index = 0 ; index < sevens.players.size() ; index++) {
					if (sevens.playersOrder.get(sevens.fakeTurn) == index) continue;
					player = simSevens.players.get(index);
					player.hand = sevens.players.get(index).hand.deepCopy();
					player.hand.sortSuitRank();
				}
				test = test.getUnion(sevens.layout.deepCopy());
				test = test.getUnion(sevens.turnPlayer.hand.deepCopy());
				test.checkDuplication();
				test.sortSuitRank();
				test = test;
				if (card.rank == Card.PASS_RANK && card.suit == Card.SPECIAL_SUIT) {
					simSevens.turnPlayer.nums.set(Sevens.PASS_INDEX, simSevens.turnPlayer.nums.get(Sevens.PASS_INDEX) - 1);
					string = Sevens.PASS_STR;
					
				} else {
					simSevens.layout.add(card);
					simSevens.turnPlayer.hand.removeCard(card);
					string = Sevens.PLAY_STR;
					
				}
				history = sevens.history.deepCopy();
				history.addPage(sevens.fakeTurn, sevens.totalTurn, Cards.getReadonlyCard(card), string, simSevens.players);
				simSevens.history = history;
				simSevens.history.simTurn = sevens.totalTurn;
				result = simSevens.startSevens(printDepth + 1);
				int tmp = scores.size() - 1;
				score += result.scores.get(sevens.playersOrder.get(sevens.fakeTurn));
				history.simHands = worldCopy;
				history.scores = result.scores;
				histories.add(result.history);
			}
			scores.add(score);
		}
		simulation.histories = histories;
		simulation.myScores = scores;
		return simulation;
	}
}
