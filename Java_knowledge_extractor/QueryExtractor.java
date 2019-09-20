package WSC;

/**
 * 
 */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import module.graph.resources.InputDependencies;
import module.graph.resources.Preprocessor;
import module.graph.resources.ResourceHandler;

/**
 * @author Arpit Sharma
 * @date Jun 11, 2017
 *
 */
public class QueryExtractor {

	private HashMap<String,String> mapOfConns= null;
	//	private DependencyParserResource dpr = null;
	private ResourceHandler resourceHandler = null;
	private Preprocessor preprocessorInstance = null;

	/**
	 * 
	 */
	public QueryExtractor() {
		mapOfConns = populateConnFile("types_of_discourse_disint.txt");
		//		dpr = new DependencyParserResource();
		resourceHandler = ResourceHandler.getInstance();
		preprocessorInstance = Preprocessor.getInstance();

	}

	public static void main(String[] args){
		QueryExtractor qe = new QueryExtractor();
		String sent = "The man could not lift his son because he was so weak.";
		JSONArray a = qe.extractQueriesFromAText(sent);
		System.out.println(a.toString());
		System.exit(0);
	}
	
	
	public void writeToFile(JSONArray array, String outFile){
		try (FileWriter file = new FileWriter(outFile)) {
			file.write(array.toJSONString());
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public JSONArray extractQueriesJSONFileCOPA(JSONArray inputArr){
		JSONArray result = new JSONArray();
		
		for(int i=0;i<inputArr.size();i++){
			JSONObject obj = (JSONObject) inputArr.get(i);
			String premise = (String) obj.get("premise");
			String alt1 = (String) obj.get("alternative1");
			String alt2 = (String) obj.get("alternative2");
			String rel = (String) obj.get("relation");
			
			String sentAlt1 = "";
			String sentAlt2 = "";
			if(rel.equalsIgnoreCase("cause")){
				sentAlt1 = premise + " because " + alt1;
				sentAlt2 = premise + " because " + alt2;
			}else{
				sentAlt1 = alt1 + " because " + premise;
				sentAlt2 = alt2 + " because " + premise;
			}
			
			ArrayList<String> queriesAlt1 = getQueriesWithConn(sentAlt1, "");
			String queryAlt1 = getQueriesWithoutConn(sentAlt1, "");
			if(!queryAlt1.equals("")){
				queriesAlt1.add(queryAlt1);
			}
			HashSet<String> qsAlt1 = new HashSet<String>(queriesAlt1);
			
			ArrayList<String> queriesAlt2 = getQueriesWithConn(sentAlt2, "");
			String queryAlt2 = getQueriesWithoutConn(sentAlt2, "");
			if(!queryAlt2.equals("")){
				queriesAlt2.add(queryAlt2);
			}
			HashSet<String> qsAlt2 = new HashSet<String>(queriesAlt2);
			
			JSONObject ob = obj;
			obj.put("queries_alt1", qsAlt1);
			ob.put("queries_alt2", qsAlt2);
			
			result.add(obj);
		}
		
		return result;
	}
	
	public JSONArray extractQueriesFromJSONFile(JSONArray inputArr){
		JSONArray result = new JSONArray();
		
		for(int i=0;i<inputArr.size();i++){
			JSONObject obj = (JSONObject) inputArr.get(i);
			String ws_sent = (String) obj.get("question");
			
			ArrayList<String> queries = getQueriesWithConn(ws_sent, "");
			String query = getQueriesWithoutConn(ws_sent, "");
			if(!query.equals("")){
				queries.add(query);
			}
			
			HashSet<String> qs = new HashSet<String>(queries);
			
			JSONObject ob = new JSONObject();
			ob.put("question", ws_sent);
			ob.put("queries", qs);
			
			result.add(ob);
		}
		
		return result;
	}
	
	public JSONArray extractQueriesFromAText(String text){
		JSONArray result = new JSONArray();

		ArrayList<String> queries = getQueriesWithConn(text, "");
		String query = getQueriesWithoutConn(text, "");
		if(!query.equals("")){
			queries.add(query);
		}

		HashSet<String> qs = new HashSet<String>(queries);

		JSONObject ob = new JSONObject();
		ob.put("question", text);
		ob.put("queries", qs);

		result.add(ob);

		return result;
	}

	
	public JSONArray readJSONFile(String input_file){
		JSONArray jsonArr = null;
        JSONParser parser = new JSONParser();
        try {     
            Object obj = parser.parse(new FileReader(input_file));
            jsonArr =  (JSONArray) obj;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }	
        
        return jsonArr;
	}
	
	
	private String getQueriesWithoutConn(String sent, String phrase){
		String query = "";
		sent = preprocessorInstance.preprocessText(sent, resourceHandler, null, null);
		sent = sent.replaceAll("\\.", " \\.");
		sent = sent.replaceAll(" +", " ");

		InputDependencies inDeps = resourceHandler.getSyntacticDeps(sent, false, 0);
		HashMap<String,String> posMap = inDeps.getPosMap();
		//		sent = PreprocessText.preprocess(sent);
		//		phrase = PreprocessText.preprocess(phrase);
		//		ArrayList<DiscParsedSent> listOfSentParts = getPartsAndRel(sent);

		String[] words = sent.split(" ");

		String lastWord = "";
		int indx = 1;
		boolean first_word = true;
		for(String word : words){
			if(!word.equals(".") && !unwantedWord(word)){
				if(posMap.containsKey(word+"-"+indx)){
					if(posMap.get(word+"-"+indx).startsWith("NNP")
							|| posMap.get(word+"-"+indx).startsWith("DT")
							|| posMap.get(word+"-"+indx).startsWith("W")
							|| posMap.get(word+"-"+indx).startsWith("P")
							|| posMap.get(word+"-"+indx).startsWith("NN")
							|| posMap.get(word+"-"+indx).startsWith(":")){
						if(!lastWord.equals(" * ")&&!first_word){
							query+=" * ";
						}else{
							first_word = false;
						}
						lastWord = " * ";
					}else{
						first_word = false;
						if(!lastWord.equals(" * ")){
							query+=" "+word;
						}else{
							query+=word;
						}
						lastWord = word;
					}
				}
			}
			indx++;
		}
		
		if(!query.endsWith(" * ")){
			query = query += " * ";
		}

		return ("\" * " + query + "\"");
	}
	
	private boolean unwantedWord(String word){
		String uWords = "was;is;am;has";
		String[] tmpArr = uWords.split(";");
		for(String w : tmpArr){
			if(w.equalsIgnoreCase(word)){
				return true;
			}
		}
		
		return false;
	}

	private ArrayList<String> getQueriesWithConn(String sent, String phrase){
		ArrayList<String> result = new ArrayList<String>();
		sent = preprocessorInstance.preprocessText(sent, resourceHandler, null, null);
		sent = sent.replaceAll("\\.", " \\.");
		sent = sent.replaceAll(" +", " ");
		
		InputDependencies inDeps = resourceHandler.getSyntacticDeps(sent, false, 0);
		HashMap<String,String> posMap = inDeps.getPosMap();
		//		sent = PreprocessText.preprocess(sent);
		//		phrase = PreprocessText.preprocess(phrase);
		ArrayList<DiscParsedSent> listOfSentParts = getPartsAndRel(sent);

		for(DiscParsedSent dps : listOfSentParts){
			String query = "";
			String lastWord = "";
			String[] words = dps.getPart1();
			int indx = dps.getPart1Indx()+1;
			for(String word : words){
				if(!word.equals(".") && !unwantedWord(word)){
					if(posMap.containsKey(word+"-"+indx)){
						if(posMap.get(word+"-"+indx).startsWith("NNP")
								|| posMap.get(word+"-"+indx).startsWith("DT")
								|| posMap.get(word+"-"+indx).startsWith("W")
								|| posMap.get(word+"-"+indx).startsWith("P")
								|| posMap.get(word+"-"+indx).startsWith("NN")
								|| posMap.get(word+"-"+indx).startsWith(":")){
							if(!lastWord.equals(" * ")){
								query+=" * ";
							}
							lastWord = " * ";
						}else{
							if(!lastWord.equals(" * ")){
								query+=" "+word;
							}else{
								query+=word;
							}
							lastWord = word;
						}
					}
				}
				indx++;
			}
			if(!query.endsWith(" * ")){
				query = query += " * ";
			}

			query+= dps.getDiscConn();
			lastWord = "";
			words = dps.getPart2();
			indx = dps.getPart2Indx()+1;
			for(String word : words){
				if(!word.equals(".") && !unwantedWord(word)){
					if(posMap.containsKey(word+"-"+indx)){
						if(posMap.get(word+"-"+indx).startsWith("NNP")
								|| posMap.get(word+"-"+indx).startsWith("DT")
								|| posMap.get(word+"-"+indx).startsWith("W")
								|| posMap.get(word+"-"+indx).startsWith("P")
								|| posMap.get(word+"-"+indx).startsWith("NN")
								|| posMap.get(word+"-"+indx).startsWith(":")){
							if(!lastWord.equals(" * ")){
								query+=" * ";
							}
							lastWord = " * ";
						}else{
							if(!lastWord.equals(" * ")){
								query+=" "+word;
							}else{
								query+=word;
							}
							lastWord = word;
						}
					}
				}
				indx++;
			}

			if(!query.endsWith(" * ")){
				query = query += " * ";
			}
			
			result.add("\""+query+"\"");
		}

		return result;
	}


	private ArrayList<DiscParsedSent> getPartsAndRel(String sent){
		ArrayList<DiscParsedSent> result = new ArrayList<DiscParsedSent>();
		String[] words = sent.split(" ");
		int index = 0;
		for(String word : words){
			if(mapOfConns.containsKey(word.toLowerCase())){
				DiscParsedSent dps = new DiscParsedSent();
				if(index!=0){
					dps.setPart1(Arrays.copyOfRange(words, 0, index));
					dps.setPart1Indx(0);
				}

				if(index!=words.length-1){
					dps.setPart2(Arrays.copyOfRange(words, index+1, words.length));
					dps.setPart2Indx(index+1);
				}

				dps.setDiscConn(word);
				dps.setConnType(mapOfConns.get(word.toLowerCase()));
				result.add(dps);
			}
			index++;
		}
		return result;
	}

	private HashMap<String,String> populateConnFile(String file){
		HashMap<String,String> result = new HashMap<String, String>();
		try(BufferedReader br = new BufferedReader(new FileReader(file))){
			String line = null;
			while((line=br.readLine())!=null){
				String[] tmpArr = line.split("\t");
				if(tmpArr.length==2){
					result.put(tmpArr[0], tmpArr[1]);
				}
			}
		}catch(IOException e){
			e.printStackTrace();
		}
		return result;
	}

}

