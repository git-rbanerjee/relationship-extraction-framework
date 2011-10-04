package edu.columbia.cs.cg.document.loaders.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;

import edu.columbia.cs.cg.document.Document;
import edu.columbia.cs.cg.document.loaders.DocumentLoader;
import edu.columbia.cs.cg.document.preprocessing.Preprocessor;
import edu.columbia.cs.cg.document.segmentator.DocumentSegmentator;
import edu.columbia.cs.cg.document.tagger.Tagger;
import edu.columbia.cs.cg.relations.RelationshipType;

public class RawDocumentLoader extends DocumentLoader{

	private Preprocessor preprocessor;
	private Tagger[] taggers;
	private DocumentSegmentator docSegmentator;

	public RawDocumentLoader(Set<RelationshipType> relationshipTypes, Preprocessor preprocessor, DocumentSegmentator docSegmentator, Tagger...taggers) {
		super(relationshipTypes);
		this.preprocessor = preprocessor;
		this.taggers = taggers;
		this.docSegmentator = docSegmentator;
	}

	@Override
	public Set<Document> load(File file) {
		
		throw new UnsupportedOperationException();
		
	}

	public Document load(Reader reader){

		BufferedReader br = new BufferedReader(reader);
		
		try {
			
			StringBuilder sb = new StringBuilder();
			
			String line = br.readLine();
			
			if (line!=null)
				sb.append(line);
			
			while ((line = br.readLine())!=null){
				
				sb.append("\n" + line);
				
			}
			
			br.close();
			
			return load(sb.toString());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
		
	}

	public Document load(String content){
		
		String preprocessedContent = preprocessor.process(content);
		
		Document d = new Document(docSegmentator.segmentate(preprocessedContent));
		
		for (Tagger tagger : taggers) {
			
			tagger.enrich(d);
			
		}
		
		return d;
	}

}
