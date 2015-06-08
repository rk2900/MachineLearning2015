package project.ml.hwy;

public class Label {
	private String query,weiboId;
	private int isreview,reviewratio;
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getWeiboId() {
		return weiboId;
	}
	public void setWeiboId(String weiboId) {
		this.weiboId = weiboId;
	}
	public int getIsreview() {
		return isreview;
	}
	public void setIsreview(int isreview) {
		this.isreview = isreview;
	}
	public int getReviewratio() {
		return reviewratio;
	}
	public void setReviewratio(int reviewratio) {
		this.reviewratio = reviewratio;
	}
	public Label(String query, String weiboId, int isreview, int reviewratio) {
		super();
		this.query = query;
		this.weiboId = weiboId;
		this.isreview = isreview;
		this.reviewratio = reviewratio;
	}
	
}
