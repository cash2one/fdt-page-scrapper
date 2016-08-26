package com.fdt.keywords.scrapper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.fdt.utils.Utils;

public class WordsCounter 
{
	//private static final String REPLACE_REGEXP = "[^0-9a-zA-Zа-яА-Я\\s\\%\\$\\-\\']+";
	private static final String REPLACE_REGEXP = "[^0-9a-zA-Zа-яА-Я\\s\\'\\r\\n\\.]+";

	//private static final String SPLIT_STR = "[\\s\\:\\-\\'\"\\%\\$\\-]+";
	private static final String SPLIT_STR = "[\\s\\:\\-\"\\%\\$\\-,]+";

	private String[] defaultStopWords = new String[]{ "a","about","above","after","again","against","all","am","an","and","any","are","aren't",
			"as","at","be","because","been","before","being","below","between","both","but","by","can't",
			"cannot","could","couldn't","did","didn't","do","does","doesn't","doing","don't","down","during",
			"each","few","for","from","further","had","hadn't","has","hasn't","have","haven't","having",
			"he","he'd","he'll","he's","her","here","here's","hers","herself","him","himself","his",
			"how","how's","i","i'd","i'll","i'm","i've","if","in","into","is","isn't","it","it's",
			"its","itself","let's","me","more","most","mustn't","my","myself","no","nor","not",
			"of","off","on","once","only","or","other","ought","our","ours","ourselves","out","over",
			"own","same","shan't","she","she'd","she'll","she's","should","shouldn't","so","some","such",
			"than","that","that's","the","their","theirs","them","themselves","then","there","there's",
			"these","they","they'd","they'll","they're","they've","this","those","through","to","too",
			"under","until","up","very","was","wasn't","we","we'd","we'll","we're","we've","were","weren't",
			"what","what's","when","when's","where","where's","which","while","who","who's","whom","why",
			"why's","with","won't","would","wouldn't","you","you'd","you'll","you're","you've","your",
			"yours","yourself","yourselves"
	};


	private HashMap<String, Word> dict = new HashMap<String, Word>();

	private HashMap<String, String> stopDict = new HashMap<String, String>();

	public WordsCounter(String text, String stopWordsStr, boolean useStopWords) {
		this(text, stopWordsStr, useStopWords, null);
	}

	public WordsCounter(String text, String stopWordsStr, boolean useStopWords, IEncoder encoder) {
		super();
		if(useStopWords){
			loadStopList(stopWordsStr.split(SPLIT_STR));
		}
		loadDictionary(text, encoder);
	}

	private void loadStopList(){
		loadStopList(defaultStopWords);
	}

	private void loadStopList(String[] stopWords){
		for(String stop : stopWords){
			stopDict.put(stop, "");
		}
	}

	private void loadDictionary(String text){
		loadDictionary(text, null);
	}

	private void loadDictionary(String text, IEncoder encoder){
		String newText = text.toLowerCase().replaceAll(REPLACE_REGEXP, " ").replaceAll("\\s+", " ");
		String[] srtWords = newText.split(SPLIT_STR);

		for(String strWord : srtWords)
		{
			
			strWord = strWord.replaceAll("\\.$", "").replaceAll("^\\.", "");
			
			if(!"".equals(strWord.trim())){
				if(!stopDict.containsKey(strWord))
				{
					if(dict.containsKey(strWord))
					{
						dict.get(strWord).incCount();
					}else
					{
						Word word = null;
						if(encoder != null){
							word = new Word(strWord, encoder);
							dict.put(strWord, word);
						}else{
							word = new Word(strWord);
							dict.put(strWord, word);
						}
					}
				}
			}
		}
	}

	public ArrayList<Word> getSortedDictionary(int minCount)
	{
		ArrayList<Word> sorted = new ArrayList<Word>(dict.values());

		Collections.sort(sorted);

		ArrayList<Word> limited = new ArrayList<Word>();

		for(Word word : sorted){
			if(word.getCount() >= minCount){
				limited.add(word);
			}else{
				break;
			}
		}

		return limited;
	}

	public ArrayList<Word> getSortedDictionary()
	{
		return getSortedDictionary(1);
	}
}
