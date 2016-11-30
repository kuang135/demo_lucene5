package com.ceair.lucene5.l3_query;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.NumericDocValuesField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

public class Q6_Sort {
	

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
		   		doc.add(new NumericDocValuesField("id", i));
		   		doc.add(new StringField("title", "apple phone " + i, Field.Store.YES));
		   		doc.add(new TextField("sellPoint", "苹果手机" + i +" is very good, phone we all like apple.", Field.Store.YES));
		   		doc.add(new FloatField("price", i + 0.01f, Field.Store.YES));
		   		doc.add(new FloatDocValuesField("price", i + 0.01f));
		   		docs.add(doc);
		   	}
	       indexWriter.addDocuments(docs);
	       indexWriter.close();
	       directory.close();
	   }
	   
	   private void sortQuery(Query query, Sort sort) throws Exception {
		    Directory directory = FSDirectory.open(Paths.get("index"));
	        IndexSearcher indexSearcher = new IndexSearcher(DirectoryReader.open(directory));

	        TopDocs topDocs = indexSearcher.search(query, 10, sort);
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
	    
	   /*
	    * 默认情况下，接受sort对象参数的search方法不会对匹配文档进行打分
	    * 价格降序，id升序
	    */
	    @Test
	    public void sortTest() throws Exception {
	    	Query query = new TermQuery(new Term("sellPoint", "apple"));
	    	SortField sortField1 = new SortField("id", SortField.Type.LONG, false);
	    	SortField sortField2 = new SortField("price", SortField.Type.FLOAT, true);
	    	Sort sort = new Sort(sortField2, sortField1);
	    	sortQuery(query, sort);
	    }

}
