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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.Test;

public class Q4_QueryParser {

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
	   
	   private void searchByLanguage(String field, String language) throws Exception {
		    Directory directory = FSDirectory.open(Paths.get("index"));
		    DirectoryReader reader = DirectoryReader.open(directory);
	        IndexSearcher indexSearcher = new IndexSearcher(reader);

	        Analyzer analyzer = new StandardAnalyzer();
	        QueryParser parser = new QueryParser(field, analyzer);
	        Query query = parser.parse(language);
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
	        reader.close();
	        directory.close();
	    }
	
	   
	//词查询
	@Test
	public void term() throws Exception {
		searchByLanguage("sellPoint", "apple");
	}
	
	//范围查询，字典顺序排序
	@Test
	public void termRange() throws Exception {
	    searchByLanguage("title", "[a TO b]");
	}
	
	//数字范围查询 ，QueryParser不会建立 NumericRangeQuery
	@Test
	public void numericRange() throws Exception {
	    searchByLanguage("price", "{1.01 TO 5.01]");
	}
	
	//前缀搜索
    @Test
    public void wildcard() throws Exception {
    	//StandardAnalyzer已经下面语句分词，所以搜不到结果
    	searchByLanguage("title", "apple phone 1*");// title:apple title:phone title:1*
    	//只在末尾有一个*号，QueryParser优化为PrefixQuery
    	searchByLanguage("title", "appl*");// title:appl*
    	//通配符
    	searchByLanguage("title", "a?pl*");// title:a?pl*
    }
    
    //布尔操作符
    @Test
    public void booleanOperator() throws Exception {
    	//+sellPoint:apple +sellPoint:phone
    	searchByLanguage("sellPoint", "apple AND phone");
    }
    
    //短语查询，双引号内的文本会促使分析器转换为PharseQuery
    @Test
    public void pharse() throws Exception {
    	// sellPoint:"苹 果 手 机 13"
    	searchByLanguage("sellPoint", "\"苹果手机13\"");
    }
    
    //模糊查询
    @Test
    public void fuzzy() throws Exception {
    	// sellPoint:live~2
    	searchByLanguage("sellPoint", "live~");
    }
}
