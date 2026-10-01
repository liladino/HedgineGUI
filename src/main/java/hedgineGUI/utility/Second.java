package utility;

public class Second {
	public int time;
	public Second(int t){
		time = t;
	}

	@Override
	public String toString(){
		return ((Integer)time).toString();
	}
}
