import java.util.ArrayList;
import java.util.Random;

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
		
		simulation = upp(sevens, printDepth, playableAndHold, M, agents, playersHandSize, cards);
		
		scores = simulation.myScores;
		prevSimHistories = simulation.histories;
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
	
	String getName() {
		return name;
	}
	
	MontecarloSimulation upp(Sevens sevens, int printDepth, Cards playableAndHold, int m, ArrayList<AgentSevens> agents, ArrayList<Integer> playersHandSize, Cards cards) {
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
			if (sevens.turn == index) {
				handsNum.add(0);
			} else {
				secret = secret.getUnion(sevens.players.get(index).hand);
				handsNum.add(sevens.players.get(index).hand.size());
			}
		}
		ArrayList<ArrayList<ArrayList<Integer>>> probabilities = probabilityEstimation(prevSimHistories, handsNum, secret);
		ArrayList<Cards> world, worldCopy;
		
		for (Card card : playableAndHold) {
			score = 0;
			for (int loop = 0 ; loop < m / playableAndHold.size() ; loop++) {
				simSevens = new Sevens();
				simSevens.setupSevens(sevens.players, sevens.deck, sevens.layout, (sevens.turn + 1) % sevens.players.size(), sevens.totalTurn + 1, MyUtil.SIM, sevens.playersOrder, sevens.history, agents);
				for (int index = 0 ; index < sevens.players.size() ; index++) {
					player = simSevens.players.get(index);
					if (sevens.turn != index) {
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
					world.get(index).showCards(0);
					MyUtil.always.pln();
					if (sevens.turn != index) {
						test = test.getUnion(worldCopy.get(index));
					}
				}
				test.sortSuitRank();
				test = test;
				for (int index = 0 ; index < sevens.players.size() ; index++) {
					if (sevens.turn == index) continue;
					player = simSevens.players.get(index);
					player.hand = world.get(index);
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
				result = simSevens.startSevens(printDepth + 1);
				int tmp = scores.size() - 1;
				score += result.scores.get(sevens.playersOrder.get(sevens.turn));
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
	
	
	ArrayList<ArrayList<ArrayList<Integer>>> probabilityEstimation(ArrayList<History> prevSimHistories, ArrayList<Integer> handsNum, Cards secret) {//世界の確率推定
		ArrayList<ArrayList<ArrayList<Integer>>> playerRankSuitProbability = new ArrayList<>();
		int player, rank, suit, index, playerNum = handsNum.size();
		ArrayList<ArrayList<Integer>> playerRankProbability;
		ArrayList<Integer> playerProbability;
		int probability;
		for (suit = 1; suit < 5 ; suit++) {
			playerRankSuitProbability.add(new ArrayList<ArrayList<Integer>>());
			for (rank = 1; rank < 14 ; rank++) {
				playerProbability = new ArrayList<Integer>();
				playerRankSuitProbability.get(suit - 1).add(playerProbability);
				for (player = 0; player < playerNum ; player++) {
					if (secret.containsCard(rank, suit) && (handsNum.get(player) != 0)) {
						//まだ出てないカード&&プレイヤーの手札が有る
						playerProbability.add(playerNum * Test.M);
					} else {
						playerProbability.add(0);
					}
				}
			}
		}
		if (prevSimHistories != null) {
			for (History prevSimHistory : prevSimHistories) {
				for (index = 0; index < playerNum ; index++) {
					int xxx = prevSimHistory.scores.get(index);
					for (Card card : prevSimHistory.simHands.get(index)) {
						Cards hand = prevSimHistory.simHands.get(index);
						probability = playerRankSuitProbability.get(card.suit - 1).get(card.rank - 1).get(index);
						if (secret.containsCard(card.rank, card.suit) && (handsNum.get(index) != 0)) {
							int yyy = probability * 2 - playerNum + 1 + xxx;
							if (yyy <= 0) System.out.println("error");
							playerRankSuitProbability.get(card.suit - 1).get(card.rank - 1).set(index, yyy);
						}
					}
				}
			}
		}
		return playerRankSuitProbability;
	}
//		ArrayList<ArrayList<ArrayList<Integer>>> probabilityEstimation(ArrayList<History> prevSimHistories, ArrayList<Integer> handsNum, Cards secret) {//世界の確率推定
//ArrayList<ArrayList<ArrayList<Integer>>> playerRankSuitProbability = new ArrayList<>();
//	int player, rank, suit, index, playerNum = handsNum.size();
//	ArrayList<ArrayList<Integer>> playerRankProbability;
//	ArrayList<Integer> playerProbability;
//	int probability;
//		for (suit = 1; suit < 5 ; suit++) {
//		playerRankSuitProbability.add(new ArrayList<ArrayList<Integer>>());
//		for (rank = 1; rank < 14 ; rank++) {
//			playerProbability = new ArrayList<Integer>();
//			playerRankSuitProbability.get(suit - 1).add(playerProbability);
//			for (player = 0; player < playerNum ; player++) {
//				if (secret.containsCard(rank, suit) && (handsNum.get(player) != 0)) {
//					//まだ出てないカード&&プレイヤーの手札が有る
//					playerProbability.add(playerNum * Test.M);
//				} else {
//					playerProbability.add(0);
//				}
//			}
//		}
//	}
//		if (prevSimHistories != null) {
//		for (History prevSimHistory : prevSimHistories) {
//			for (index = 0; index < playerNum ; index++) {
//				int xxx = prevSimHistory.scores.get(index);
//				for (Card card : prevSimHistory.simHands.get(index)) {
//					Cards hand = prevSimHistory.simHands.get(index);
//					probability = playerRankSuitProbability.get(card.suit - 1).get(card.rank - 1).get(index);
//					if (secret.containsCard(card.rank, card.suit) && (handsNum.get(index) != 0)) {
//						int yyy = probability * 2 - playerNum+1 + xxx;
//						playerRankSuitProbability.get(card.suit - 1).get(card.rank - 1).set(index, yyy);
//					}
//				}
//			}
//		}
//	}
//		return playerRankSuitProbability;
//}
//
	
	ArrayList<Cards> genWorld(int size, ArrayList<ArrayList<ArrayList<Integer>>> playerRankSuitProbability, ArrayList<Integer> handsNum) {
		//自分・上がった他プレイヤーもリストに含む
		ArrayList<Cards> playersCards = new ArrayList<>();
		for (int count = 0 ; count < size ; count++) {
			playersCards.add(new Cards());
		}
		int sum, randNum, rank, suit, probability;
		Random rand = new Random();
		for (suit = 1; suit < 5 ; suit++) {
			ArrayList<ArrayList<Integer>> playerRankProbability = playerRankSuitProbability.get(suit - 1);
			for (rank = 1; rank < 14 ; rank++) {
				ArrayList<Integer> playerProbability = playerRankProbability.get(rank - 1);
				sum = 0;
				
				for (int count = 0 ; count < playerProbability.size() ; count++) {
					if (handsNum.get(count) <= playersCards.get(count).size()) {
						;
					} else {
						sum += playerProbability.get(count);
					}
				}
				if (sum == 0) continue;
				randNum = rand.nextInt(sum);
				sum = 0;
				for (int index = 0 ; index < playerProbability.size() ; index++) {
					if (handsNum.get(index) <= playersCards.get(index).size()) {
						probability = 0;
					} else {
						probability = playerProbability.get(index);
					}
					if (probability == 0) continue;
					sum += probability;
					if (randNum <= sum) {
						playersCards.get(index).add(Card.createCard(rank, suit));
						break;
					}
				}
				
			}
		}
		return playersCards;
	}
}
