package project.ml.hwy;

import java.util.List;

public abstract class Model {
	protected abstract void init();
	protected abstract List<Result> predict(List<Data> dataList);
	protected abstract void train(List<Data> dataList);
	public List<Result> run(List<Data> trainningSet, List<Data> testingSet)
	{
		this.init();
		this.train(trainningSet);
		List<Result> results = this.predict(testingSet);
		return results;
	}
}
