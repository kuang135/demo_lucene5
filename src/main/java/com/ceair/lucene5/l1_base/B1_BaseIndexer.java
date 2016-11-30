package com.ceair.lucene5.l1_base;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

/*
   	词典						倒排表(Posting List)
	lucene			-->		文档2,文档3,文档4,文档6,
	solr			-->		文档1,文档3,文档5
	elasticsearch	-->		文档5,文档8
 */
/*
	Document -- 要索引的文档
	Field -- 文档的类型，比如标题，作者，内容，路径等
		存储：是否在返回的结果中显示该字段
		分词：是否用该字段的部分内容进行搜索，比如商品的ID不需要分词
 */
public class B1_BaseIndexer {
	
	 //创建文档
	 private List<Document> createDocuments(){
    	List<Document> list = new ArrayList<Document>();
    	
    	// 构造Document
    	Document doc1 = new Document();
        //设置文档字段，Store表示是否存储到索引库中
    	//索引库中如果不存储，搜索结果不会返回；如果返回的数据不需要显示出来，那么在不需要存入索引库，设为NO就行
    	//TextField和StringField的区别：StringField将字符串认为是一个整体，不能被切分；而TextField中的字符串可以被切分
    	doc1.add(new StringField("id", "1a2b3c", Field.Store.YES));
    	doc1.add(new TextField("content", "Students should be allowed to go out with their friends, but not allowed to drink beer.", Field.Store.YES));
    	doc1.add(new StringField("path", "/usr/local/files/doc1.txt", Field.Store.YES));
        
    	list.add(doc1);
        
    	Document doc2 = new Document();
        doc2.add(new StringField("id", "2a2b2c", Field.Store.YES));
    	doc2.add(new TextField("content", "My friend Jerry went to school to see his students but found them drunk which is not allowed.", Field.Store.YES));
    	doc2.add(new StringField("path", "/usr/local/files/doc2.txt", Field.Store.YES));
    	
    	list.add(doc2);
        return list;
    }
    
    //创建索引
    @Test
    public void create() throws Exception{
        //索引位置
        Directory directory = FSDirectory.open(Paths.get("index"));
        //定义分析器(标准分析器)
        Analyzer analyzer = new StandardAnalyzer();
        //定义索引配置
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);//设置每次都重新创建
        
        //定义索引对象
        IndexWriter indexWriter = new IndexWriter(directory, indexWriterConfig);
        //创建索引
        List<Document> docs=this.createDocuments();
        //将文档写入索引库
        indexWriter.addDocuments(docs);
        //关闭索引
        indexWriter.close();
        directory.close();
    }
    
    //索引搜索
    @Test
    public void search() throws Exception{
		  //定义索引位置
		  Directory directory = FSDirectory.open(Paths.get("index"));
		  //创建IndexReader
		  IndexReader reader = DirectoryReader.open(directory);
		  //构造索引搜索器
		  IndexSearcher indexSearcher = new IndexSearcher(reader);
		  //构造查询对象
		  Analyzer analyzer = new StandardAnalyzer(); // 定义分词器(标准分词器)
		  //创建IndexReader
	      QueryParser parser = new QueryParser("content", analyzer);
	      //// 构造查询对象，分词查询
	      Query query = parser.parse("allowed to drink beer"); 
	      //打印查询语法
	      System.out.println("查询语句: " + query.toString());
		  //查询
		  TopDocs topDocs = indexSearcher.search(query, 10);
		  System.out.println("总命中数: " + topDocs.totalHits + "\r\n");
		  
		  for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
		       System.out.println("得分：" + scoreDoc.score);
			   Document doc = indexSearcher.doc(scoreDoc.doc);
			   System.out.println("id: " + doc.get("id"));
			   System.out.println("content: " + doc.get("content"));
			   System.out.println("path: " + doc.get("path"));
			   System.out.println("------------------------------");
		  }
		  reader.close();
		  directory.close();
    }

}
