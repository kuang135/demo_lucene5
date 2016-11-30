package com.ceair.lucene5.l2_index;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;


/* 	
 * 	通过FieldType类去设置类型
 * 		索引(IndexOptions)：是否根据该字段进行搜索，比如商品的图片地址不需要索引
 *		存储(Field.Store)：是否在返回的结果中显示该字段
 *		分词(setTokenized)：是否用该字段的部分内容进行搜索，比如商品的ID不需要分词
 *		等等
 */
/*
 * 	IndexOptions
 * 		NONE -- 不索引
 * 		DOCS -- 索引文档
 * 		DOCS_AND_FREQS -- 索引文档和term的在文档中的频率
 * 		DOCS_AND_FREQS_AND_POSITIONS -- 索引文档和term在文档中的频率，位置
 * 		DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS -- 索引文档和term在文档中的频率，位置，偏移	
 */
/*	
 * 	Field.Store
 * 		YES -- 存在字段的值，用于搜索命中之后的显示
 * 		NO	-- 不存储字段的值，比如只想获取某个文件的名称，那么庞大的文件内容就可以不存储
 */
/*	
 * 	StringField: 索引文档，不分词，是否存储可设置
 * 		setOmitNorms(true); -- 不会在索引中存储 norms 信息，norms信息记录了索引中的 index-time boost 信息
 * 		setIndexOptions(IndexOptions.DOCS);
 * 		setTokenized(false);
 */
/*
 * 	TextField：索引文档,term频率,位置，分词，是否存储可设置
 * 		setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
 * 		setTokenized(true);
 * 	构造参数为 Reader，TokenStream 时不存储
 */
/*
 * 	LongField：索引文档，分词，是否存储可设置
 * 		setTokenized(true);
 * 		setOmitNorms(true);
 * 		setIndexOptions(IndexOptions.DOCS);
 * 		setNumericType(FieldType.NumericType.LONG);
 */
public class I2_Field {
	
	private List<Document> createDocuments() throws IOException{
    	List<Document> list = new ArrayList<Document>();
    	
    	Document doc1 = new Document();
    	//默认
    	doc1.add(new StringField("id", "id111", Field.Store.YES));
    	doc1.add(new LongField("lastModified", new Date().getTime(), Field.Store.YES));
    	doc1.add(new TextField("content", "good good study", Field.Store.YES));
        
    	list.add(doc1);
        
    	Document doc2 = new Document();
        doc2.add(new StringField("id", "id222", Field.Store.YES));
        doc2.add(new LongField("lastModified", new Date().getTime(), Field.Store.NO));
        BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get("data\\data1.txt")), StandardCharsets.UTF_8));
    	doc2.add(new TextField("content", reader));
    	//一个字段可以放多个值
    	doc2.add(new StringField("author", "韩寒", Field.Store.YES));
    	doc2.add(new StringField("author", "小六", Field.Store.YES));
    	
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
    
    @Test
    public void searchAll() throws Exception{
		  //定义索引位置
		  Directory directory = FSDirectory.open(Paths.get("index"));
		  //创建IndexReader
		  IndexReader reader = DirectoryReader.open(directory);
		  //构造索引搜索器
		  IndexSearcher indexSearcher = new IndexSearcher(reader);
		  //查询
		  Query query = new MatchAllDocsQuery();
		  System.out.println("查询语句: " + query.toString());
		  TopDocs topDocs = indexSearcher.search(query, 10);
		  System.out.println("命中总数: " + topDocs.totalHits + "\r\n");
		  
		  for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
			   Document doc = indexSearcher.doc(scoreDoc.doc);
			   System.out.println("id: " + doc.get("id"));
			   System.out.println("lastModified: " + doc.get("lastModified"));
			   System.out.println("content: " + doc.get("content"));
			   String[] authors = doc.getValues("author");
			   for (String author : authors) {
				   System.out.println("author: " + author);
			   }
			   System.out.println("------------------------------");
		  }
		  reader.close();
		  directory.close();
    }

}
