package Indexing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeMap;

import Classes.Path;
import Classes.Term_item;


public class MyIndexReader {
	//you are suggested to write very efficient code here, otherwise, your memory cannot hold our corpus...
	
	//reader
	private BufferedReader dictionary_rd;
	private BufferedReader posting_rd;
	private BufferedReader transform_rd;
	
	private String dict_line;
	private String posting_line;
	private String tran_line;
	
	//store 
	private HashMap<String, Term_item> dict_table;
	private HashMap<String, Integer> trans_table;
	private TreeMap<Integer, String> posting_block;
	
	public MyIndexReader( String type ) throws IOException {
		//read the index files you generated in task 1
		//remember to close them when you finish using them
		//use appropriate structure to store your index
		
		if (type.equals("trecweb")) {
			dictionary_rd = new BufferedReader(new FileReader(Path.IndexWebDir + "DictionaryTerm.txt"));
			posting_rd = new BufferedReader(new FileReader(Path.IndexWebDir + "Posting.txt"));
			posting_rd.mark((int) (new File(Path.IndexWebDir + "Posting.txt").length() + 1));
			transform_rd = new BufferedReader(new FileReader(Path.IndexWebDir + "Transform.txt"));
		}
		else {
			dictionary_rd = new BufferedReader(new FileReader(Path.IndexTextDir + "DictionaryTerm.txt"));
			posting_rd = new BufferedReader(new FileReader(Path.IndexTextDir + "Posting.txt"));
			posting_rd.mark((int) (new File(Path.IndexTextDir + "Posting.txt").length() + 1));
			transform_rd = new BufferedReader(new FileReader(Path.IndexTextDir + "Transform.txt"));
		}
		dict_table = new HashMap<String, Term_item>();
		trans_table = new HashMap<String, Integer>();
		posting_block = new TreeMap<Integer, String>();
		
		//store dictionary
		dict_line = dictionary_rd.readLine();
		while (dict_line != null) {
			String[] dl_split = dict_line.split(" ");
			Term_item temp_itme = new Term_item(Integer.parseInt(dl_split[1]), Integer.parseInt(dl_split[2]));
			dict_table.put(dl_split[0], temp_itme);
			
			dict_line = dictionary_rd.readLine();
		}
		//store transform
		tran_line = transform_rd.readLine();
		while (tran_line != null) {
			String[] tl_split = tran_line.split(" ");
			trans_table.put(tl_split[0], Integer.parseInt(tl_split[1]));
			
			tran_line = transform_rd.readLine();
		}
		//store posting block
		posting_line = posting_rd.readLine();
		int i = 0;
		while (i < 50000 && posting_line != null) {
			int id = Integer.parseInt(posting_line);
			posting_line = posting_rd.readLine();
			posting_block.put(id, posting_line);
			
			posting_line = posting_rd.readLine();
			i++;
		}
		System.out.println("OK");
	}
	
	//get the non-negative integer dociId for the requested docNo
	//If the requested docno does not exist in the index, return -1
	public int GetDocid( String docno ) {
		
		if (trans_table.containsKey(docno)) {
			return trans_table.get(docno);
		}
		else {
			return -1;
		}
	}

	// Retrieve the docno for the integer docid
	public String GetDocno( int docid ) {
		String result = null;
		
		for (String i : trans_table.keySet()) {
			if (trans_table.get(i) == docid) {
				result = i;
			}
		}
		
		return result;
	}
	
	/**
	 * Get the posting list for the requested token.
	 * 
	 * The posting list records the documents' docids the token appears and corresponding frequencies of the term, such as:
	 *  
	 *  [docid]		[freq]
	 *  1			3
	 *  5			7
	 *  9			1
	 *  13			9
	 * 
	 * ...
	 * 
	 * In the returned 2-dimension array, the first dimension is for each document, and the second dimension records the docid and frequency.
	 * 
	 * For example:
	 * array[0][0] records the docid of the first document the token appears.
	 * array[0][1] records the frequency of the token in the documents with docid = array[0][0]
	 * ...
	 * 
	 * NOTE that the returned posting list array should be ranked by docid from the smallest to the largest. 
	 * 
	 * @param token
	 * @return
	 */
	public int[][] GetPostingList( String token ) throws IOException {
		if (!dict_table.containsKey(token))
			return null;
		
		int[][] result = null;
		int target_id = dict_table.get(token).getTermid();
		//if current block has this token
		if (posting_block.containsKey(target_id)) {
			StringTokenizer st = new StringTokenizer(posting_block.get(target_id), ";");
			result = new int[st.countTokens()][2];
			
			int i = 0;
			while (st.hasMoreTokens()) {
				String[] token_split = st.nextToken().split(" ");
				result[i][0] = Integer.parseInt(token_split[0]);
				result[i][1] = Integer.parseInt(token_split[1]);
				i++;
			}
		}
		//find the block has this token
		else {
			if (target_id < posting_block.firstKey()){
				posting_rd.reset();
				posting_line = posting_rd.readLine();
			}
			//find the block
			while (!posting_block.containsKey(target_id)) {
				posting_block.clear();
				int i = 0;
				while (i < 50000 && posting_line != null) {
					int id = Integer.parseInt(posting_line);
					posting_line = posting_rd.readLine();
					posting_block.put(id, posting_line);
					
					posting_line = posting_rd.readLine();
					i++;
				}
			}
			//get the result
			StringTokenizer st = new StringTokenizer(posting_block.get(target_id), ";");
			result = new int[st.countTokens()][2];
			
			int i = 0;
			while (st.hasMoreTokens()) {
				String[] token_split = st.nextToken().split(" ");
				result[i][0] = Integer.parseInt(token_split[0]);
				result[i][1] = Integer.parseInt(token_split[1]);
				i++;
			}
		}
		
		return result;
	}

	// Return the number of documents that contains the token.
	public int GetDocFreq( String token ) throws IOException {
		if (!dict_table.containsKey(token))
			return 0;
		
		int result = 0;
		int target_id = dict_table.get(token).getTermid();
		//if current block has this token
		if (posting_block.containsKey(target_id)) {
			StringTokenizer st = new StringTokenizer(posting_block.get(target_id), ";");
			result = st.countTokens();
		}
		//find the block has this token
		else {
			if (target_id < posting_block.firstKey()){
				posting_rd.reset();
				posting_line = posting_rd.readLine();
			}
			
			//find the block
			while (!posting_block.containsKey(target_id)) {
				posting_block.clear();
				int i = 0;
				while (i < 50000 && posting_line != null) {
					int id = Integer.parseInt(posting_line);
					posting_line = posting_rd.readLine();
					posting_block.put(id, posting_line);
					
					posting_line = posting_rd.readLine();
					i++;
				}
			}
			//get the result
			StringTokenizer st = new StringTokenizer(posting_block.get(target_id), ";");
			result = st.countTokens();
		}
		
		return result;
	}
	
	// Return the total number of times the token appears in the collection.
	public long GetCollectionFreq( String token ) throws IOException {
		if (!dict_table.containsKey(token))
			return 0;
		
		return dict_table.get(token).getCf();
	}
	
	public void Close() throws IOException {
		posting_rd.close();
		dictionary_rd.close();
		transform_rd.close();
	}
	
}