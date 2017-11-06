import java.util.ArrayList;

/**
 * Created by ryoto on 2017/10/17.
 */
public class History {
	Cards cards = new Cards();
	ArrayList<Integer> playerTurns = new ArrayList<Integer>();
	ArrayList<Integer> totalTurns = new ArrayList<Integer>();
	ArrayList<String> strings = new ArrayList<String>();
	ArrayList<Integer> playersOrder = new ArrayList<Integer>();
	int simTurn = -1;
	ArrayList<Cards> simHands = new ArrayList<Cards>();
	ArrayList<ArrayList<Cards>> handsHistory = new ArrayList<ArrayList<Cards>>();
	ArrayList<Integer> scores;
	
	History() {
	
	}
	
	void addPage(int playerFakeTurn, int totalTurn, Card card, String str, ArrayList<Player> players) {
		this.playerTurns.add(playerFakeTurn);
		totalTurns.add(totalTurn);
		cards.add((card==null)?Card.createCard(Card.PASS_RANK, Card.SPECIAL_SUIT):card);
		strings.add(str);
		ArrayList<Cards> readonlyHands = new ArrayList<Cards>();
		for (Player player : players) {
			readonlyHands.add(Cards.getReadonlyCards(player.hand));
		}
		handsHistory.add(readonlyHands);
	}
	
	History deepCopy() {
		History history = new History();
		for (int turn : playerTurns) {
			history.playerTurns.add(turn);
		}
		for (String str : strings) {
			history.strings.add(new String(str));
		}
		for (int playerOrder : playersOrder) {
			history.playersOrder.add(playerOrder);
		}
		history.cards = this.cards.deepCopy();
		history.simTurn = simTurn;
		if (handsHistory.size() > 0) {
			for (ArrayList<Cards> hands : handsHistory) {
				ArrayList<Cards> newHands = new ArrayList<Cards>();
				if (hands.size() > 0) {
					for (Cards hand : hands) {
						newHands.add(Cards.getReadonlyCards(hand));
					}
				}
				history.handsHistory.add(newHands);
			}
		}
		return history;
	}
	
	Card getLastPlayerAction(int playerIndex) {
		for (int turn = playerTurns.size() - 1 ; turn >= 0 ; turn--) {
			if (playersOrder.get(playerTurns.get(turn)) == playerIndex) {
				return cards.get(turn);
			}
		}
		return null;
	}
	
	Card getLastPlayerAction(int playerIndex, int lowerLimitTurn) {
		if(playerTurns.size()<=0)return null;
		if(lowerLimitTurn<0)lowerLimitTurn=0;
		for (int turn = playerTurns.size() - 1 ; turn >= lowerLimitTurn ; turn--) {
			if (playersOrder.get(playerTurns.get(turn)) == playerIndex) {
				return cards.get(turn);
			}
		}
		return null;
	}
	
	Card getPlayerAction(int playerIndex, int lowerLimitTurn, int upperLimitTurn) {
		if(upperLimitTurn>playerTurns.size()-1)upperLimitTurn=playerTurns.size()-1;
		if(lowerLimitTurn<0)lowerLimitTurn=0;
		
		for (int turn = upperLimitTurn; turn >= lowerLimitTurn ; turn--) {
			if (playersOrder.get(playerTurns.get(turn)) == playerIndex) {
				return cards.get(turn);
			}
		}
		return null;
	}
	
	int getLastActionTurn(int playerIndex) {
		for (int turn = playerTurns.size() - 1 ; turn >= 0 ; turn--) {
			if (playersOrder.get(playerTurns.get(turn)) == playerIndex) {
				return turn;
			}
		}
		return -1;
	}
}
