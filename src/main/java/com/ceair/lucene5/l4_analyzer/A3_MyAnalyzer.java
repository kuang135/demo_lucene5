package com.ceair.lucene5.l4_analyzer;

import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class A3_MyAnalyzer extends Analyzer {

	/*	
	 * 	Tokenizer：用于创建初始词汇单元序列(分割)
	 * 	TokenFilter：修改词汇单元(过滤)，可以是任意数量
	 */
	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		WhitespaceTokenizer source = new WhitespaceTokenizer();
		A3_MyLengthFilter result = new A3_MyLengthFilter(source, 3, 6);
		return new TokenStreamComponents(source, result);
	}
	
	
	 public static void main(String[] args) throws IOException {
	     final String text = "This is a demo of the TokenStream API";
	     @SuppressWarnings("resource")
	     A3_MyAnalyzer analyzer = new A3_MyAnalyzer();
	     TokenStream stream = analyzer.tokenStream("fieldName", new StringReader(text));
	     CharTermAttribute termAtt = stream.addAttribute(CharTermAttribute.class);
	     try {
	    	 stream.reset();
	    	 while (stream.incrementToken()) {
	    		 System.out.println(termAtt.toString());
	    	 }
	    	 stream.end();
	     } finally {
	    	 stream.close();
	     }
	 }

}
