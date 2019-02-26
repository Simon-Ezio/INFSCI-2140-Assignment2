package Classes;

/*
 * this class is for storing term id and collection frequency for each term
 */
public class Term_item {
	private int term_id;
	private int cf;
	
	public Term_item(int x, int y) {
		this.term_id = x;
		this.cf = y;
	}
	
	public void setTermid (int x) {
		this.term_id = x;
	}
	
	public int getTermid () {
		return term_id;
	}
	
	public void setCf (int x) {
		this.cf = x;
	}
	
	public int getCf () {
		return cf;
	}
	//collection frequency add 1
	public void addCf () {
		this.cf = this.cf + 1;
	}
}
