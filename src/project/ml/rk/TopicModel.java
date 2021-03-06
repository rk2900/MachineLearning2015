package project.ml.rk;

import basic.FileOps;
import basic.algorithm.Classification;
import basic.algorithm.WinSVM;
import basic.format.DenseFeature;
import basic.format.Feature;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.topics.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import project.ml.hwy.Data;
import project.ml.hwy.Label;
import project.ml.hwy.Model;
import project.ml.hwy.Result;

public class TopicModel extends Model {
	
	public Pipe pipe;
	public InstanceList instances;
	public InstanceList totalInstances;
	public ParallelTopicModel model;
	public Classification classificationModel;
	public double svmThresholdRatio = 0.27;
	int numTopics = 50;
	double auxilaryRatio = 0.4;
	
	int positiveDataNum=0, negativeDataNum=0;

    public Pipe buildPipe() {
        ArrayList pipeList = new ArrayList();

        // Read data from File objects
        pipeList.add(new Input2CharSequence("UTF-8"));

        // Regular expression for what constitutes a token.
        //  This pattern includes Unicode letters, Unicode numbers, 
        //   and the underscore character. Alternatives:
        //    "\\S+"   (anything not whitespace)
        //    "\\w+"    ( A-Z, a-z, 0-9, _ )
        //    "[\\p{L}\\p{N}_]+|[\\p{P}]+"   (a group of only letters and numbers OR
        //                                    a group of only punctuation marks)
        Pattern tokenPattern =
            Pattern.compile("[\\p{L}\\p{N}_]+");

        // Tokenize raw strings
        pipeList.add(new CharSequence2TokenSequence(tokenPattern));

        // Normalize all tokens to all lowercase
        pipeList.add(new TokenSequenceLowercase());

        // Remove stopwords from a standard English stoplist.
        //  options: [case sensitive] [mark deletions]
        
//        pipeList.add(new TokenSequenceRemoveStopwords(false, false));
        String stopwordsList = "data/stopwords-utf8.txt";
//        pipeList.add( new TokenSequenceRemoveStopwords(new File(stopwordsList), "UTF-8", false, false, false) );

        // Rather than storing tokens as strings, convert 
        //  them to integers by looking them up in an alphabet.
        pipeList.add(new TokenSequence2FeatureSequence());

        // Do the same thing for the "target" field: 
        //  convert a class label string to a Label object,
        //  which has an index in a Label alphabet.
        pipeList.add(new Target2Label());

        return new SerialPipes(pipeList);
    }
	
	@Override
	protected void init() {
		pipe = buildPipe();
		instances= new InstanceList(pipe);
		totalInstances = new InstanceList(pipe);
	}

	@Override
	protected List<Result> predict(List<Data> dataList) {
		// Create a new instance named "ABFJ33" with empty target and source fields.
		LinkedList<Result> results =  new LinkedList<Result>();
		TopicInferencer inferencer = model.getInferencer();
		InstanceList test = new InstanceList(pipe);
		
		LinkedList<Feature> features = new LinkedList<Feature>();
		for(int i=0; i<dataList.size(); i++) {
			Data data = dataList.get(i);
			Instance inst = new Instance(data.getContent(), 1, data.getWeiboId(), null);
			test.addThruPipe(inst);
			double[] topicProbabilities = inferencer.getSampledDistribution(test.get(i), 100, 10, 10);
			Feature f = new DenseFeature();
			f.setSize(numTopics);
			f.setResult(0);
			int count=0;
			for (double d : topicProbabilities) {
				f.setValue(count++, d);
			}
			features.add(f);
		}
		
		ArrayList<Double> labels = classificationModel.predict(features);
		double max=0,min=1;
		for (Double d : labels) {
			max = d>max?d:max;
			min = d<min?d:min;
		}
		
		double threshold = min+(max-min)*svmThresholdRatio;
		for(int iter=0; iter<dataList.size(); iter++) {
			double ranking = labels.get(iter);
			System.out.print(ranking+",");
			Result result = new Result(dataList.get(iter).getWeiboId(), "TopicModel", ranking>threshold?1:-1, 0.0);
			results.add(result);
		}
		
		return results;
	}

	@Override
	protected void train(Map<Data, Label> trainList) {
		trainList = normDataList(trainList, 0.0);
		
		Set<Data> dataSet = trainList.keySet();
		LinkedList<Data> datas = new LinkedList<Data>();
		LinkedList<Label> labels = new LinkedList<Label>();
		
		for (Data data : dataSet) {
			Label label = trainList.get(data);
			if(label.getIsreview()==0)
				continue;
			datas.add(data);
			labels.add(label);
			
			Instance instance = new Instance(data.getContent(), label.getIsreview(), data.getWeiboId(), null);
			instances.addThruPipe(instance);
			totalInstances.add(instance);
		}
		
		LinkedList<String> lines = FileOps.LoadFilebyLine("data/weibo_timeline.txt");
		int lineCount = 0;
		for (String string : lines) {
			if(lineCount++ >= trainList.size()*auxilaryRatio)
				break;
			String content = string.substring(17, string.length());
			String weiboId = string.substring(0, 17);
			Instance instance = new Instance(content, 1, weiboId, null);
			totalInstances.addThruPipe(instance);
		}
		
		model = new ParallelTopicModel(numTopics, 1.0, 0.01);
		
		model.addInstances(totalInstances);
		model.setNumThreads(16);
		
		model.setNumIterations(1500);
		try {
			model.estimate();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		printTopic();
		
		/* Classifier Part */
		String cmd = "-t 0 -h 0 -b 1";
		classificationModel = new WinSVM("lib/winsvm/", cmd, "-b 1");
//		classificationModel = new LinearRegression(numTopics, instances.size(), 0.3, 0.0);
		classificationModel.setNFeature(numTopics);
		
		System.out.println();
		for(int i=0; i<instances.size(); i++ ) {
			double[] topicDistribution = model.getTopicProbabilities(i);
			Feature f = new DenseFeature();
			f.setSize(numTopics);
			int isReview = labels.get(i).getIsreview();
			int temp = isReview>0?positiveDataNum++:negativeDataNum++;
			f.setResult(isReview); // 1, -1
			int count = 0;
			System.out.print(isReview+"\t");
			for (double d : topicDistribution) {
				f.setValue(count++, d);
				System.out.print(d+"\t");
			}
			classificationModel.addTrain(f);
			System.out.println();
		}
		
		classificationModel.train();
	}
	
	public void printTopic() {
		Alphabet dataAlphabet = instances.getDataAlphabet();
		Formatter out = new Formatter(new StringBuilder(), Locale.US);
		double[] topicDistribution = model.getTopicProbabilities(0);
		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
		for (int topic = 0; topic < numTopics; topic++) {
			Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
			
			out = new Formatter(new StringBuilder(), Locale.US);
			out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
			int rank = 0;
			while (iterator.hasNext() && rank < 50) {
				IDSorter idCountPair = iterator.next();
				out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
				rank++;
			}
			System.out.println(out);
		}
	}
	
	public Map<Data, Label> normDataList(Map<Data, Label> trainList, double ratio) {
		Set<Data> dataSet = trainList.keySet();
		int pNum=0, nNum=0;
		Set<Data> keys = new HashSet<Data>();
		for (Data d : dataSet) {
			keys.add(d);
			Label l = trainList.get(d);
			int temp = l.getIsreview()>0?pNum++:nNum++;
		}
		int c=0;
		int var = (int) ((pNum-nNum)*ratio);
		for (Data d : keys) {
			Label l = trainList.get(d);
			if(l.getIsreview()*var > 0) {
				trainList.remove(d);
				c++;
			}
			if(c >= Math.abs(var))
				break;
		}
		
		return trainList;
	}
}
