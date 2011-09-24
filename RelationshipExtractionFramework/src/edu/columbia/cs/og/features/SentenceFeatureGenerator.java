package edu.columbia.cs.og.features;

import java.util.List;

import edu.columbia.cs.cg.sentence.Sentence;
import edu.columbia.cs.og.features.featureset.FeatureSet;
import edu.columbia.cs.og.structure.OperableStructure;

public abstract class SentenceFeatureGenerator<E extends FeatureSet> extends FeatureGenerator<E> {

	@Override
	protected final E extractFeatures(OperableStructure s) {
		return extractFeatures(s.getCandidateSentence().getSentence());
	}

	protected abstract E extractFeatures(Sentence sentence);

	@Override
	protected final FeatureSet getFeatures(OperableStructure s,
			Class<? extends FeatureGenerator> featureGeneratorClass) {
		return s.getCandidateSentence().getSentence().getFeatures(featureGeneratorClass);
	}

	@Override
	protected final void setFeatures(OperableStructure s,
			Class<? extends FeatureGenerator> featureGeneratorClass,
			FeatureSet fs) {
		
		s.getCandidateSentence().getSentence().setFeatures(featureGeneratorClass,fs);
		
	}

	protected abstract List<FeatureGenerator> retrieveRequiredFeatureGenerators();
}
