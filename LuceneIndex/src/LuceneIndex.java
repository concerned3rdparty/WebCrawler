import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;




import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
//import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;



class WebDocument {
	public String title;
	public String body;
	public String url;	
	public WebDocument(String t, String b, String u) {
		this.title = t;
		this.body = b;
		this.url = u;
	}
}


public class LuceneIndex {
	@SuppressWarnings("deprecation")
	public static final String INDEX_DIR = "Indexed_Pages";
	
	public static void main(String[] args) throws CorruptIndexException, IOException {	
		index();	
		search("ucr",5);
	}
	
	public static void createIndex(IndexWriter writer) 
	{
		String line="";
		String[] pair= null;	
		String title="";
		String body="";
		BufferedReader br= null;
		
		try {
			br = new BufferedReader(new FileReader("pages/pages/lookup.txt"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		try {
			while (br.ready()){
				try{
				line= br.readLine();
				
				// pair[0] = URL and pair[1]= filename
			    pair = line.split(";");
			 //   System.out.println("URL: " + pair[0]);
			    
			    String file = "pages/"+pair[1].substring(2);
			 //   System.out.println("FILE: "+file);
			    
			    File fn= new File(file);
			    org.jsoup.nodes.Document doc = Jsoup.parse(fn, "UTF-8");
			    
			    //extract the body 
			    Elements bodyelem = doc.getElementsByTag("body");
			    if (bodyelem.isEmpty())
			    	body="";
			    else
			    	body= bodyelem.get(0).text();
			    

			    //extract the title
			    title= doc.title();
			    //System.out.println("Title: " + title);
			    //System.out.println("Body: " + body);
			    
			    WebDocument page= new WebDocument(title,body,pair[0]);
			    
			    Document luceneDoc = new Document();	
			    luceneDoc.add(new Field("text", page.body, Field.Store.YES, Field.Index.ANALYZED));
			    luceneDoc.add(new Field("url", page.url, Field.Store.YES, Field.Index.NO));
			    luceneDoc.add(new Field("title", page.title, Field.Store.YES, Field.Index.ANALYZED));
			    writer.addDocument(luceneDoc);
				}
				
				catch(Exception ex){
					ex.printStackTrace();
				}
			} // end while
			
		}catch (IOException e) {
			e.printStackTrace();
		}
	
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void index () {
		
		
		File index = new File(INDEX_DIR);	
		IndexWriter writer = null;
		try {	
			IndexWriterConfig indexConfig = new IndexWriterConfig(Version.LUCENE_34, new StandardAnalyzer(Version.LUCENE_35));
			writer = new IndexWriter(FSDirectory.open(index), indexConfig);
			System.out.println("Indexing to directory '" + index + "'...");	
						
			createIndex(writer);
			
			writer.close();
			System.out.println("Indexing Successful");
			
		} 
		catch (Exception ex) {
			ex.printStackTrace();
		} 
		finally {
			if (writer !=null)
				try {
					writer.close();
				} catch (CorruptIndexException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}	
	
public static TopDocs search (String queryString, int topk) throws CorruptIndexException, IOException {
		
		IndexReader indexReader = IndexReader.open(FSDirectory.open(new File(INDEX_DIR)));
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		QueryParser queryparser = new QueryParser(Version.LUCENE_34, "text", new StandardAnalyzer(Version.LUCENE_34));

		try {
			StringTokenizer strtok = new StringTokenizer(queryString, " ~`!@#$%^&*()_-+={[}]|:;'<>,./?\"\'\\/\n\t\b\f\r");
			String querytoparse = "";
			while(strtok.hasMoreElements()) {
				String token = strtok.nextToken();
				querytoparse += "text:" + token + "^1" + "title:" + token+ "^1.5";
				//querytoparse += "text:" + token;
			}		
			Query query = queryparser.parse(querytoparse);
			//System.out.println(query.toString());
			TopDocs results = indexSearcher.search(query, topk);
			System.out.println(results.scoreDocs.length);	
			System.out.println(indexSearcher.doc(results.scoreDocs[0].doc).getField("text").stringValue());
			
			
			return results;			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			//indexSearcher.close();
		}
		return null;
	}
}	
