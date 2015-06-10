package project.ml.rk;

import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;

import java.util.*;
import java.util.regex.*;
import java.io.*;

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
		for (Data data : dataSet) {
			Label label = trainList.get(data);
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
		
		// Topic distribution of the first instance
//		System.out.println("Topic distribution of the first instance");
//		Alphabet dataAlphabet = instances.getDataAlphabet();
//		FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
//		LabelSequence topics = model.getData().get(0).topicSequence;
//		
//		Formatter out = new Formatter(new StringBuilder(), Locale.CHINA);
//		for (int position=0; position < tokens.getLength(); position++) {
//			out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
//		}
//		System.out.println(out);
		
		for(int i=0; i<instances.size(); i++ ) {
			double[] topicDistribution = model.getTopicProbabilities(i);
			int topicCount = 0;
			for (double d : topicDistribution) {
				System.out.print(topicCount+": "+d+"\t");
				topicCount++;
			}
		}
		
//		System.out.println("==============");
//		double[] topicDistribution = model.getTopicProbabilities(0);
//		ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
//		
//		for (int topic=0; topic < numTopics; topic++) {
//			Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
//			out = new Formatter(new StringBuilder(), Locale.CHINA);
//			out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
//			int rank = 0;
//			while(iterator.hasNext() && rank < 5) {
//				IDSorter idCountPair = iterator.next();
//				out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
//				rank++;
//			}
//			System.out.println(out);
//		}
		
		
		
		
		
		
	}
}
