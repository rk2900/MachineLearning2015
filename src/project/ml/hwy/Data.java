package project.ml.hwy;

public class Data {
	private String weiboId,poi,from,time,content,fileName;
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Data(String weiboId, int like, int retweet, int comment, int pic, String poi, String from, String time,
			String content, String fileName) {
		super();
		this.weiboId = weiboId;
		this.poi = poi;
		this.from = from;
		this.time = time;
		this.content = content;
		this.like = like;
		this.retweet = retweet;
		this.comment = comment;
		this.pic = pic;
		this.fileName = fileName;
	}
	public String getWeiboId() {
		return weiboId;
	}
	public void setWeiboId(String weiboId) {
		this.weiboId = weiboId;
	}
	public String getPoi() {
		return poi;
	}
	public void setPoi(String poi) {
		this.poi = poi;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public int getLike() {
		return like;
	}
	public void setLike(int like) {
		this.like = like;
	}
	public int getRetweet() {
		return retweet;
	}
	public void setRetweet(int retweet) {
		this.retweet = retweet;
	}
	public int getComment() {
		return comment;
	}
	public void setComment(int comment) {
		this.comment = comment;
	}
	public int getPic() {
		return pic;
	}
	public void setPic(int pic) {
		this.pic = pic;
	}
	private int like,retweet,comment,pic;
}
