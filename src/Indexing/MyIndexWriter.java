package Indexing;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import Classes.Path;
import Classes.Term_item;

public class MyIndexWriter {
	// I suggest you to write very efficient code here, otherwise, your memory cannot hold our corpus...
	
	//indicate what type doc
	private String doc_type;
	//writer for dictionary term file
	private FileWriter wr_term;
	//writer for posting file
	private FileWriter wr_posting;
	//writer for no-id transformation file
	private FileWriter wr_transform;
	
	//count for document number
	private int doc_count;
	//count for posting file number
	private int file_count;
	//count for term number
	private int term_count;
	
	//map for term file
	private Map<String, Term_item> Terms;
	//map for posting file
	private Map<Integer, TreeMap<Integer, Integer>> Posting;
	//map for transform file
	private Map<String, Integer> Transforms;
	
	public MyIndexWriter(String type) throws IOException {
		// This constructor should initiate the FileWriter to output your index files
		// remember to close files if you finish writing the index
		
		if (type.equals("trecweb")) {
			wr_term = new FileWriter(Path.IndexWebDir + "DictionaryTerm.txt");
			//initiate the first posting block
			wr_posting = new FileWriter(Path.IndexWebDir + "midway//webPosting1.txt");
			wr_transform = new FileWriter(Path.IndexWebDir + "Transform.txt");
			
			doc_type = type;
			
			doc_count = 0;
			file_count = 0;
			term_count = 0;
			
			Terms = new HashMap<String, Term_item>();
			Posting = new TreeMap<Integer, TreeMap<Integer, Integer>>();
			Transforms = new HashMap<String, Integer>();
		} else {
			wr_term = new FileWriter(Path.IndexTextDir + "DictionaryTerm.txt");
			//initiate the first posting block
			wr_posting = new FileWriter(Path.IndexTextDir + "midway//textPosting1.txt");
			wr_transform = new FileWriter(Path.IndexTextDir + "Transform.txt");
			
			doc_type = type;
			
			doc_count = 0;
			file_count = 0;
			term_count = 0;
			
			Terms = new HashMap<String, Term_item>();
			Posting = new TreeMap<Integer, TreeMap<Integer, Integer>>();
			Transforms = new HashMap<String, Integer>();
		}
	}
	
	public void IndexADocument(String docno, String content) throws IOException {
		// you are strongly suggested to build the index by installments
		// you need to assign the new non-negative integer docId to each document, which will be used in MyIndexReader
		doc_count++;
		//build transform
		Transforms.put(docno, doc_count);
		
		//handle content
		StringTokenizer st = new StringTokenizer(content, " ");
		while (st.hasMoreElements()) {
			String new_term = st.nextToken();
			//if is a new term
			if(Terms.containsKey(new_term) && Posting.containsKey(Terms.get(new_term).getTermid())) {
				//cf++
				Term_item temp_item = Terms.get(new_term);
				temp_item.addCf();
				
				//if is a new document
				TreeMap<Integer, Integer> temp_map = Posting.get(temp_item.getTermid());
				if (temp_map.containsKey(doc_count)) {
					temp_map.put(doc_count, temp_map.get(doc_count) + 1);
				}
				else {
					temp_map.put(doc_count, 1);
				}
			}
			else if (Terms.containsKey(new_term) && !Posting.containsKey(Terms.get(new_term).getTermid())) {
				//cf++
				Term_item temp_item = Terms.get(new_term);
				temp_item.addCf();
				
				//update posting
				int term_id = Terms.get(new_term).getTermid();
				TreeMap<Integer, Integer> new_map = new TreeMap<Integer, Integer>();
				new_map.put(doc_count, 1);
				Posting.put(term_id, new_map);
			}
			else {
				//update dictionary
				term_count++;
				Term_item new_termitem = new Term_item(term_count, 1);
				Terms.put(new_term, new_termitem);
				
				//update posting
				TreeMap<Integer, Integer> new_map = new TreeMap<Integer, Integer>();
				new_map.put(doc_count, 1);
				Posting.put(term_count, new_map);
			}
		}
		
		if (doc_type.equals("trecweb") && doc_count % 50000 == 0) {

			if (file_count >= 1) {
				wr_posting = new FileWriter(Path.IndexWebDir + "midway//webPosting" + Integer.toString(file_count+1) +".txt");
			}
			
			for (int i : Posting.keySet()) {
				wr_posting.append(i + "\n");
				TreeMap<Integer, Integer> temp_map = Posting.get(i);
				for (int j : temp_map.keySet()) {
					wr_posting.append(j + " " + temp_map.get(j) + ";");
				}
				wr_posting.append("\n");
			}
			wr_posting.close();
			Posting.clear();
			file_count++;
		}
		
		if (doc_type.equals("trectext") && doc_count % 150000 == 0) {
			if (file_count >= 1) {
				wr_posting = new FileWriter(Path.IndexTextDir + "midway//textPosting" + Integer.toString(file_count+1) + ".txt");
			}
			
			for (int i : Posting.keySet()) {
				wr_posting.append(i + "\n");
				TreeMap<Integer, Integer> temp_map = Posting.get(i);
				for (int j : temp_map.keySet()) {
					wr_posting.append(j + " " + temp_map.get(j) + ";");
				}
				wr_posting.append("\n");
			}
			wr_posting.close();
			Posting.clear();
			file_count++;
		}
	}
	
	public void Close() throws IOException {
		// close the index writer, and you should output all the buffered content (if any).
		// if you write your index into several files, you need to fuse them here.
		
		//write the last block
		if (doc_type.equals("trectext")) {
			wr_posting = new FileWriter(Path.IndexTextDir + "midway//textPosting" + Integer.toString(file_count+1) + ".txt");
		}
		else {
			wr_posting = new FileWriter(Path.IndexWebDir + "midway//webPosting" + Integer.toString(file_count+1) +".txt");
		}
		for (int i : Posting.keySet()) {
			wr_posting.append(i + "\n");
			TreeMap<Integer, Integer> temp_map = Posting.get(i);
			for (int j : temp_map.keySet()) {
				wr_posting.append(j + " " + temp_map.get(j) + ";");
			}
			wr_posting.append("\n");
		}
		wr_posting.close();
		Posting.clear();
		
		//write dictionary and transform file
		for (String i : Terms.keySet()) {
			wr_term.append(i + " ");
			wr_term.append(Terms.get(i).getTermid() + " ");
			wr_term.append(Terms.get(i).getCf() + "\n");
		}
		wr_term.close();
		Terms.clear();
		for (String i : Transforms.keySet()) {
			wr_transform.append(i + " ");
			wr_transform.append(Transforms.get(i) + "\n");
		}
		wr_transform.close();
		Transforms.clear();
		
		//Posting
		HashMap<Integer, String> Posting_menu = new HashMap<Integer, String>();
		
		//writer
		FileWriter wr_finalPosting;
		//reader
		BufferedReader reader;
		String read_line;
		if (doc_type.equals("trecweb")) {
			wr_finalPosting = new FileWriter(Path.IndexWebDir + "Posting.txt");
		}
		else {
			wr_finalPosting = new FileWriter(Path.IndexTextDir + "Posting.txt");
		}
		
		for (int i = 1; i <= 4; i++) {
			if (doc_type.equals("trecweb")) {
				reader = new BufferedReader(new FileReader(Path.IndexWebDir + "midway//webPosting" + i + ".txt"));
				read_line = reader.readLine();
			}
			else {
				reader = new BufferedReader(new FileReader(Path.IndexTextDir + "midway//textPosting" + i + ".txt"));
				read_line = reader.readLine();
			}
			
			while (read_line != null) {
				//id
				int id = Integer.parseInt(read_line);
				//content
				read_line = reader.readLine();
				
				if (Posting_menu.containsKey(id))
					Posting_menu.put(id, Posting_menu.get(id)+read_line);
				else
					Posting_menu.put(id, read_line);
				
				//get next line
				read_line = reader.readLine();
			}
			reader.close();
		}
		
		for (int i : Posting_menu.keySet()) {
			wr_finalPosting.append(i + "\n" + Posting_menu.get(i) + "\n");
		}
		wr_finalPosting.close();
	}
}
