package Indexing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import Classes.Path;

public class PreProcessedCorpusReader {
	
	//reader for read file
	private BufferedReader reader;
	//current read line
	private String cur_line;
	
	public PreProcessedCorpusReader(String type) throws IOException {
		// This constructor opens the pre-processed corpus file, Path.ResultHM1 + type
		// You can use your own version, or download from http://crystal.exp.sis.pitt.edu:8080/iris/resource.jsp
		// Close the file when you do not use it any more
		
		//read results.type
		reader = new BufferedReader(new FileReader(Path.ResultHM1 + type));
		//get the first line
		cur_line = reader.readLine();
	}
	

	public Map<String, String> NextDocument() throws IOException {
		// read a line for docNo, put into the map with <"DOCNO", docNo>
		// read another line for the content , put into the map with <"CONTENT", content>
		
		//if is the end of file
		if (cur_line == null) {
			reader.close();
			return null;
		}
		
		Map<String, String> doc = new HashMap<String, String>();
		
		doc.put("DOCNO", cur_line);
		cur_line = reader.readLine();
		
		doc.put("CONTENT", cur_line);
		cur_line = reader.readLine();
		
		return doc;
	}

}
