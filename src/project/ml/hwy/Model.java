package project.ml.hwy;

import java.util.List;
import java.util.Map;

public abstract class Model {
	protected abstract void init();
	protected abstract List<Result> predict(List<Data> dataList);
	protected abstract void train(Map<Data, Label> trainList);
	public List<Result> run(Map<Data, Label> trainList, List<Data> testingSet)
	{
		this.init();
		this.train(trainList);
		List<Result> results = this.predict(testingSet);
		return results;
	}
}
