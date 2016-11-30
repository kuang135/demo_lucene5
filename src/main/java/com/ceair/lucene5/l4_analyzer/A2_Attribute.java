package com.ceair.lucene5.l4_analyzer;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.junit.Test;

public class A2_Attribute {
	
	private void analyzer(Analyzer analyzer, String text) throws IOException {
		TokenStream tokenStream = analyzer.tokenStream("fieldName", new StringReader(text));
		
		//词汇单元对应的文本
		CharTermAttribute termAtt = tokenStream.addAttribute(CharTermAttribute.class);
		//位置增量(默认为为1)
		PositionIncrementAttribute posIncrAtt = tokenStream.addAttribute(PositionIncrementAttribute.class);
		//起始字符和终止字符的偏移量
		OffsetAttribute offsetAtt = tokenStream.addAttribute(OffsetAttribute.class);
		//词汇单元类型(默认为word)
		TypeAttribute typeAtt = tokenStream.addAttribute(TypeAttribute.class);
		
		tokenStream.reset();
		int position = 0;
		while(tokenStream.incrementToken()) {
			int increamt = posIncrAtt.getPositionIncrement();
			if (increamt > 0) {
				position = position + increamt;
				System.out.print(position + ": ");
			}
			System.out.println("[" + 
						termAtt.toString() + ":" + 
						offsetAtt.startOffset() + "->" + 
						offsetAtt.endOffset() + ":" +
						typeAtt.type() + "]");
		}
		tokenStream.end();
		tokenStream.close();
	}
	
	
	@Test
	public void standardAnalyzer() throws IOException {
		analyzer(new StandardAnalyzer(), "我28，我是JAVA开发。He likes playing basketball.");
	}


}
