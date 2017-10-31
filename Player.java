import java.util.ArrayList;

/**
 * Created by ryoto on 2017/10/17.
 */
public class Player {
	String name;
	SevensAgent agent;
	Cards hand =new Cards();
	ArrayList<Integer> nums;
	int key;
	Player(int argKey){
		key=argKey;
	}
	Player(String name, int key, ArrayList<Integer> numbers) {
		this.name = name;
		nums=new ArrayList<Integer>();
		for(Integer num:numbers){
			nums.add(num);
		}
	}
	
	Player deepCopy(){
		Player clone=new Player(this.name,0,this.nums);
		clone.hand=this.hand.deepCopy();
		clone.agent=this.agent.deepCopy();
		clone.nums=new ArrayList<Integer>();
		for(Integer num:nums){
			clone.nums.add(new Integer(num));
		}
		return clone;
	}
	
}
