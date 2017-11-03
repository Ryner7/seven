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
		
		Simulation simulation = upp(sevens, printDepth, playableAndHold, M, agents, playersHandSize, cards);
		scores = simulation.scores;
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
	
	double stateValueEstimation(Sevens sevens, int printDepth, Cards playableAndHold, int M, ArrayList<AgentSevens> agents, ArrayList<Integer> playersHandSize, Cards cards) {
		ArrayList<Integer> scores = montecarlo(sevens, printDepth, playableAndHold, M, agents, playersHandSize, cards).scores;
		int max = -1;
		int maxIndex = -1;
		for (int index = 0 ; index < scores.size() ; index++) {
			if (max < scores.get(index)) {
				max = scores.get(index);
				maxIndex = index;
			}
		}
		return (double) max / M;
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
						playerProbability.add(playerNum * Test.M / Test.C);
					} else {
						playerProbability.add(0);
					}
				}
			}
		}
		for (History prevSimHistory : prevSimHistories) {
			for (index = 0; index < playerNum ; index++) {
				for (Card card : prevSimHistory.simHands.get(index)) {
					probability = playerRankSuitProbability.get(card.suit).get(card.rank).get(index);
					playerRankSuitProbability.get(card.suit).get(card.rank).set(index, probability + prevSimHistory.scores.get(index));
				}
			}
		}
		return playerRankSuitProbability;
	}
	
	Simulation upp(Sevens sevens, int printDepth, Cards playableAndHold, int m, ArrayList<AgentSevens> agents, ArrayList<Integer> playersHandSize, Cards cards) {
		int score;
		Sevens simSevens;
		Player player;
		Result result;
		String string = "";
		ArrayList<Integer> scores = new ArrayList<>();
		Simulation simulation = new Simulation();
		History history;
		ArrayList<History> histories = new ArrayList<>();
		Cards secret = new Cards();
		ArrayList<Integer> handsNum = new ArrayList<>();
		for (int index = 0 ; index < sevens.players.size() ; index++) {
			secret.getIntersection(sevens.players.get(index).hand);
			handsNum.add(sevens.players.get(index).hand.size());
		}
		ArrayList<ArrayList<ArrayList<Integer>>> probabilities = probabilityEstimation(prevSimHistories, handsNum, secret);
		ArrayList<Cards> world;
		
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
				
				world = genWorld(sevens.players.size() - 1, probabilities);
				
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
				histories.add(result.history);
			}
			scores.add(score);
		}
		simulation.histories = histories;
		simulation.scores = scores;
		return simulation;
	}
	
	ArrayList<ArrayList<Integer>> worldProbability() {
		ArrayList<Integer> probability = new ArrayList<>();
		
		return null;
	}
	
	ArrayList<Cards> genWorld(int size, ArrayList<ArrayList<ArrayList<Integer>>> playerRankSuitProbability) {
		//自分・上がった他プレイヤーもリストに含む
		ArrayList<Cards> playersCards = new ArrayList<>();
		for (int count = 0 ; count < size ; count++) {
			playersCards.add(new Cards());
		}
		int sum, randNum, rank, suit;
		Random rand = new Random();
		for (suit = 1; suit < 5 ; suit++) {
			ArrayList<ArrayList<Integer>> playerRankProbability = playerRankSuitProbability.get(suit - 1);
			for (rank = 1; rank < 14 ; rank++) {
				ArrayList<Integer> playerProbability = playerRankProbability.get(rank - 1);
				sum = 0;
				for (int probability : playerProbability) {
					sum += probability;
				}
				if (sum == 0) continue;
				randNum = rand.nextInt(sum);
				sum = 0;
				for (int index = 0, probability ; index < playerProbability.size() ; index++) {
					probability = playerProbability.get(index);
					sum += probability;
					if (randNum <= sum) {
						playersCards.get(index).add(Card.createCard(rank, suit));
					}
				}
			}
		}
		return playersCards;
	}
}
