/**
 * Created by ryoto on 2017/10/17.
 */
public class MyUtil {
	static int count = 0;
	boolean print = false;
	static final int DEBUG = 3;
	static final int SIM = 2;
	static final int PLAY = 1;
	static final int ALWAYS = 0;
	static MyUtil debug = new MyUtil(0);
	static MyUtil sim = new MyUtil(0);
	static MyUtil play = new MyUtil(0);
	static MyUtil always = new MyUtil(1);
	
	MyUtil() {
	}
	
	MyUtil(boolean print) {
		this.print = print;
	}
	MyUtil(int print){
		this.print=(print!=0);
	}
	
	public void p() {
	}
	
	public void p(Object obj, boolean print) {
		if (print) System.out.print(obj);
	}
	
	public void p(Object obj) {
		if (print) System.out.print(obj);
	}
	
	public void pf(String format, Object... args) {
		if (print) System.out.printf(format, args);
	}
	
	public void pln() {
		if (print) System.out.println();
	}
	
	public void pln(Object obj, boolean print) {
		if (print) System.out.println(obj);
	}
	
	public void pln(Object obj) {
		if (print) System.out.println(obj);
	}
	
	public void debug() {
		if (print) System.out.println(count++);
	}
	
	static void dp(Object object, int depth) {
		switch (depth) {
			case ALWAYS:
				always.p(object);
				break;
			case PLAY:
				play.p(object);
				break;
			case SIM:
				sim.p(object);
				break;
			case DEBUG:
				debug.p(object);
				break;
			default:
				
				break;
		}
	}
	
	static void dpln(Object object, int depth) {
		switch (depth) {
			case ALWAYS:
				always.pln(object);
				break;
			case PLAY:
				play.pln(object);
				break;
			case SIM:
				sim.pln(object);
				break;
			case DEBUG:
				debug.pln(object);
				break;
			default:
				break;
		}
	}
	
	static void dpf(String format, int depth, Object... args) {
		switch (depth) {
			case ALWAYS:
				always.pf(format, args);
				break;
			case PLAY:
				play.pf(format, args);
				break;
			case SIM:
				sim.pf(format, args);
				break;
			case DEBUG:
				debug.pf(format, args);
				break;
			default:
				break;
		}
	}
}
