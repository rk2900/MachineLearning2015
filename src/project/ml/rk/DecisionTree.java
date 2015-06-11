package project.ml.rk;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.DecisionTreeTrainer;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.FeatureSequence2FeatureVector;
import cc.mallet.pipe.Input2CharSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.Target2Label;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;
import project.ml.hwy.Data;
import project.ml.hwy.Label;
import project.ml.hwy.Model;
import project.ml.hwy.Result;

public class DecisionTree extends Model {

	public Pipe pipe;
	public InstanceList instances;
	public ClassifierTrainer decisionTree;
	public Classifier classifier;

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

        pipeList.add(new FeatureSequence2FeatureVector());// Collapse word order into a "feature vector"
        
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
	}

	@Override
	protected List<Result> predict(List<Data> dataList) {
		ArrayList<Result> results = new ArrayList<Result>();
		InstanceList testList = new InstanceList(pipe);
		for (Data data : dataList) {
			Instance inst = new Instance(data.getContent(), -5, data.getWeiboId(), null);
			testList.addThruPipe(inst);
		}
		ArrayList<Classification> classifications = classifier.classify(testList);
		for (Classification classification : classifications) {
//			System.out.println("CorrectLabel: "+classification.valueOfCorrectLabel());
//			System.out.println("Labeling: "+classification.getLabeling());
//			System.out.println("Label vector: "+classification.getLabelVector());
			Labeling labeling = classification.getLabeling();
//			System.out.println(classification.getInstance().getName()+"\t"+labeling.getBestLabel());
			Result result = new Result(classification.getInstance().getName().toString(), "Desition Tree", Integer.parseInt(labeling.getBestLabel().toString()), 0.0);
			results.add(result);
		}
		
		return results;
	}

	@Override
	protected void train(Map<Data, Label> trainList) {
		System.out.println(trainList.size());
		trainList = normDataList(trainList, 0.0);
		System.out.println(trainList.size());
		
		Set<Data> dataSet = trainList.keySet();
		for (Data data : dataSet) {
			Label label = trainList.get(data);
//			System.out.println(data.getWeiboId()+"\t"+label.getIsreview()+"\t"+data.getContent());
			Instance instance = new Instance(data.getContent(), label.getIsreview(), data.getWeiboId(), null);
			instances.addThruPipe(instance);
		}
		InstanceList[] splitList = instances.split(new double[] {1.0, 0.0});
		
		decisionTree = new DecisionTreeTrainer();
		classifier = decisionTree.train(splitList[0]);
		
		System.out.println ("The training accuracy is "+ classifier.getAccuracy (splitList[0]));
		System.out.println ("The testing accuracy is "+ classifier.getAccuracy (splitList[1]));
		
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
