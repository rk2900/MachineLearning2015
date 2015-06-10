package project.ml.rk;

import basic.algorithm.Classification;
import basic.algorithm.WinSVM;
import basic.format.DenseFeature;
import basic.format.Feature;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;

import libsvm.svm_node;
import libsvm.svm_problem;

import org.netlib.util.doubleW;
import org.netlib.util.intW;

import project.ml.hwy.Data;
import project.ml.hwy.Label;
import project.ml.hwy.Model;
import project.ml.hwy.Result;

public class TopicModel extends Model {
	
	public Pipe pipe;
	public InstanceList instances;
	public ParallelTopicModel model;
	public Classification classificationModel;

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
        pipeList.add(new TokenSequenceRemoveStopwords(false, false));

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
	}

	@Override
	protected List<Result> predict(List<Data> dataList) {
		// Create a new instance named "ABFJ33" with empty target and source fields.
		StringBuilder topicText = new StringBuilder("我 在 实验室 吃 火锅 。");
		InstanceList testing = new InstanceList(instances.getPipe());
		testing.addThruPipe(new Instance(topicText.toString(), -1, "ABFJ33", null));
		
		TopicInferencer inferencer = model.getInferencer();
		double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
		System.out.println("0\t" + testProbabilities[0]);
		
		ArrayList<Result> result =  new ArrayList<Result>();
		return result;
	}

	@Override
	protected void train(Map<Data, Label> trainList) {
		Set<Data> dataSet = trainList.keySet();
		LinkedList<Data> datas = new LinkedList<Data>();
		LinkedList<Label> labels = new LinkedList<Label>();
		
		for (Data data : dataSet) {
			Label label = trainList.get(data);
			datas.add(data);
			labels.add(label);
			Instance instance = new Instance(data.getContent(), label.getIsreview(), data.getWeiboId(), null);
			instances.addThruPipe(instance);
		}
		
		int numTopics = 100;
		model = new ParallelTopicModel(numTopics, 1.0, 0.01);
		
		model.addInstances(instances);
		model.setNumThreads(2);
		
		model.setNumIterations(1500);
		try {
			model.estimate();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String cmd = "-t 3 -h 0 -b 1";
		classificationModel = new WinSVM("lib/winsvm/", cmd, "-b 1");
		classificationModel.setNFeature(numTopics);
		
		for(int i=0; i<instances.size(); i++ ) {
			System.out.println("Instance "+i);
			Instance inst = instances.get(i);
			System.out.println(inst.getLabeling().getBestValue());
			double[] topicDistribution = model.getTopicProbabilities(i);
			
			Feature f = new DenseFeature();
			f.setSize(numTopics);
			f.setResult(labels.get(i).getIsreview()); // 1, -1, 0
			int count = 0;
			for (double d : topicDistribution) {
				f.setValue(count++, d);
			}
			classificationModel.addTrain(f);
		}
		
		classificationModel.train();
		
	}
}
