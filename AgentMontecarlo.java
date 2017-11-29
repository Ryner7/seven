import java.util.ArrayList;

/**
 * Created by ryoto on 2017/10/30.
 */
public class AgentMontecarlo extends AgentSevens {
	String name = "monte ";
	int monteSimNum = 100;
	
	AgentMontecarlo() {
		name = "monte ";
	}
	
	Card strategy(Sevens sevens, int printDepth) {
		Cards playableAndHold = sevens.playableCards().getIntersection(sevens.turnPlayer.hand);
		
		//手札枚数が一枚で出せるときはそれを返す．
		//if (sevens.turnPlayer.hand.size() == 1 && playableAndHold.size() == 1) return playableAndHold.get(0);
		
		
		//パスが残っている場合は，パスを選択肢に入れる
		if (sevens.turnPlayer.nums.get(Sevens.PASS_INDEX) > 1) {
			playableAndHold.add(Card.createCard(Card.PASS_RANK, Card.SPECIAL_SUIT));
		}
		
		//相手のエージェントの戦略を決める
		ArrayList<AgentSevens> agents = new ArrayList<>();
		for (int index = 0 ; index < sevens.players.size() ; index++) {
			agents.add(new AgentSevens());
		}
		
		
		Sevens simSevens;
		
		//手札の枚数をカウント
		ArrayList<Integer> playersHandSize = new ArrayList<>();
		for (Player p : sevens.players) playersHandSize.add(p.hand.size());
		//Player player;
		Cards cards = new Cards();
		//Result result;
		ArrayList<Integer> scores;
		
		//シミュレーション
		MontecarloSimulation montecarloSimulation = montecarlo(sevens, printDepth, playableAndHold, agents, playersHandSize, cards);
		scores = montecarloSimulation.myScores;
		
		//シミュレーションの履歴を残す
		simHistories.add(montecarloSimulation.histories);
		
		//最も値の高いカードを取り出す
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
	
	MontecarloSimulation montecarlo(Sevens sevens, int printDepth, Cards playableAndHold, ArrayList<AgentSevens> agents, ArrayList<Integer> playersHandSize, Cards cards) {
		int score;
		Sevens simSevens;
		Player player;
		Result result;
		String string = "";
		ArrayList<Integer> scores = new ArrayList<>();
		MontecarloSimulation montecarloSimulation = new MontecarloSimulation();
		History history;
		ArrayList<History> histories = new ArrayList<>();
		ArrayList<Cards> simHands;
		
		for (Card card : playableAndHold) {//可能な行動全てに対して
			score = 0;
			for (int loop = 0 ; loop < this.monteSimNum / playableAndHold.size() ; loop++) {
				//同じ回数シミュレーションを行う
				
				//シミュレーション環境を整える
				simSevens = new Sevens();
				simSevens.setupSevens(sevens.players, sevens.deck, sevens.layout, sevens.allCards, (sevens.fakeTurn + 1) % sevens.players.size(), sevens.totalTurn + 1, MyUtil.SIM, sevens.playersOrder, sevens.history, agents);
				
				//他のプレイヤーの手札を取り"出す"
				for (int index = 0 ; index < sevens.players.size() ; index++) {
					player = simSevens.players.get(index);
					if (sevens.playersOrder.get(sevens.fakeTurn) != index) {
						cards = cards.getUnion(player.hand);
						player.hand = new Cards();
					}
				}
				
				//カードをシャッフルする
				cards.shuffle();
				simHands = new ArrayList<>();
				
				//手札を配る
				for (int index = 0 ; index < sevens.players.size() ; index++) {
					if (sevens.playersOrder.get(sevens.fakeTurn) == index) {//自分
						//continue;
						simSevens.players.get(index).hand = sevens.players.get(index).hand.deepCopy();
					} else {//他者
						player = simSevens.players.get(index);
						//カードを全て配る
						for (int count = 0 ; count < playersHandSize.get(index) ; count++) {
							if (playersHandSize.get(index) == 0) break;
							player.hand.add(cards.remove(0));
						}
						player.hand.sortSuitRank();
					}
					simHands.add(Cards.getReadonlyCards(simSevens.players.get(index).hand));
				}
				//自分の行動がパスの場合
				if (card.rank == Card.PASS_RANK && card.suit == Card.SPECIAL_SUIT) {
					simSevens.turnPlayer.nums.set(Sevens.PASS_INDEX, simSevens.turnPlayer.nums.get(Sevens.PASS_INDEX) - 1);
					//simSevens.turnPlayer.hand.removeCard(Card.PASS_RANK, Card.SPECIAL_SUIT);
					string = Sevens.PASS_STR;
				} else {//自分の行動≠パス
					simSevens.layout.add(card);
					simSevens.turnPlayer.hand.removeCard(card);
					string = Sevens.PLAY_STR;
				}
				
				//シミュレーションの履歴をとるために，履歴をコピーして行動を追加する．
				history = sevens.history.deepCopy();
				history.addPage(sevens.fakeTurn, sevens.totalTurn, Cards.getReadonlyCard(card), string, simSevens.players);
				simSevens.history = history;
				simSevens.history.simTurn = sevens.totalTurn;
				
				//シミュレーション開始
				result = simSevens.startSevens(printDepth + 2);
				int tmp = scores.size() - 1;
				
				//スコアをとる
				score += result.scores.get(sevens.playersOrder.get(sevens.fakeTurn));
				result.history.scores = result.scores;
				result.history.simHands = simHands;
				histories.add(result.history);
			}
			scores.add(score);
		}
		montecarloSimulation.histories = histories;
		montecarloSimulation.myScores = scores;
		return montecarloSimulation;
	}
	
	String getName() {
		return name;
	}
	
	int countMatchNum(ArrayList<Player> realPlayers, ArrayList<Player> imaginedPlayers, int excludedIndex) {
		//一致した枚数をカウント
		int sum = 0;
		for (int index = 0 ; index < realPlayers.size() ; index++) {
			if (index == excludedIndex) continue;//自分の分を除く
			sum += Cards.matchNum(realPlayers.get(index).hand, imaginedPlayers.get(index).hand);
		}
		return sum;
	}
	
	class MontecarloSimulation {
		ArrayList<History> histories;
		ArrayList<Integer> myScores;
	}
}
