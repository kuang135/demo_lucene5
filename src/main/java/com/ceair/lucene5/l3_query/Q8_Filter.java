package com.ceair.lucene5.l3_query;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

public class Q8_Filter {
	

	   //初始化数据
	   @Test
	   public void create() throws Exception{
	       Directory directory = FSDirectory.open(Paths.get("index"));
	       Analyzer analyzer = new StandardAnalyzer();
	       IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
	       indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);//设置每次都重新创建
	       IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
	       List<Document> docs = new ArrayList<Document>();
		   	for (int i = 0; i < 100; i++) {
		   		Document doc = new Document();
		   		doc.add(new LongField("id", i, Field.Store.YES));
		   		doc.add(new StringField("title", "apple phone " + i, Field.Store.YES));
		   		doc.add(new TextField("sellPoint", "苹果手机" + i +" is very good, phone we all like apple.", Field.Store.YES));
		   		doc.add(new FloatField("price", i + 0.01f, Field.Store.YES));
		   		docs.add(doc);
		   	}
	       indexWriter.addDocuments(docs);
	       indexWriter.close();
	       directory.close();
	   }
	   
	   /*
	    * Lucene5中filter被query代替，filter是一种特殊的query 
	    */
	   @Test
	   public void filter() throws Exception {
		    Directory directory = FSDirectory.open(Paths.get("index"));
	        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));
	        
	        NumericRangeQuery<Float> filter1 = NumericRangeQuery.newFloatRange("price", 13.01f, 15.01f, false, true);
	        PrefixQuery filter2 = new PrefixQuery(new Term("title", "apple phone 1"));

	        BooleanQuery query = new BooleanQuery.Builder()
	    		.add(filter1, Occur.FILTER)
	    		.add(filter2, Occur.FILTER)
	    		.build();
	        
	        TopDocs topDocs = indexSearcher.search(query, 10);
	        System.out.println("查询语句：" + query.toString());
	        System.out.println("总命中数：" + topDocs.totalHits + "\r\n");
	        
	        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
	        	System.out.println("评分：" + scoreDoc.score);
	            Document doc = indexSearcher.doc(scoreDoc.doc);
	            System.out.println("商品ID：" + doc.get("id"));
	            System.out.println("商品标题：" + doc.get("title"));
	            System.out.println("商品卖点：" + doc.get("sellPoint"));
	            System.out.println("商品价格：" + doc.get("price") + "\r\n");
	        }
	        indexSearcher.getIndexReader().close();
	        directory.close();
	    }
	    
}
