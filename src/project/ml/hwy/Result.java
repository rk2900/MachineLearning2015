package project.ml.hwy;

public class Result {
	private String id,method;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	private int type;
	private double score;
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public Result(String id, String method, int type, double score) {
		super();
		this.id = id;
		this.method = method;
		this.type = type;
		this.score = score;
	}
}
