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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

public class Q5_Page {
	

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
	   
	   private void pageQuery(int pageNumber, int pageSize) throws Exception {
		    Directory directory = FSDirectory.open(Paths.get("index"));
		    DirectoryReader reader = DirectoryReader.open(directory);
	        IndexSearcher indexSearcher = new IndexSearcher(reader);

	        // 分页信息
	        int start = (pageNumber - 1) * pageSize;
	        int end = start + pageSize;

	        // 构建查询对象,搜索
	        Query query = new TermQuery(new Term("sellPoint", "apple"));
	        TopDocs topDocs = indexSearcher.search(query, end);
	        
	        int totalRecord = topDocs.totalHits;
	        int totalPage = totalRecord % pageSize == 0 ? totalRecord / pageSize : totalRecord / pageSize + 1;

	        System.out.println("总记录数：" + totalRecord);
	        System.out.println("总页数：" + totalPage);
	        System.out.println("---------------");
	        
	        ScoreDoc[] docs = topDocs.scoreDocs;
	        for (int i = start; i < end; i++) {
	            ScoreDoc doc = docs[i];
	            Document document = indexSearcher.doc(doc.doc);
	            System.out.println("ID: " + document.get("id"));
	            System.out.println("Title: " + document.get("title"));
	            System.out.println("SellPoint: " + document.get("sellPoint"));
	            System.out.println("Price: " + document.get("price"));
	            System.out.println("---------------");
	        }
	    }
	    
	    @Test
	    public void pageQueryTest() throws Exception {
	    	this.pageQuery(2, 5);
	    }

}
