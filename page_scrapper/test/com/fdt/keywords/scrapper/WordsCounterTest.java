package com.fdt.keywords.scrapper;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

public class WordsCounterTest {
	
	private String text1;
	private String text2;
	
	@Before
	public void init(){
		text1 = "HIGH VELOCITY Flying a Falcon Corporate Jet into dangerous conflict zones for a business mogul supplying weapons to arms dealers, First Officer Kate Harrison launches into a world where nothing is what it seems, and the one man she doesn't trust might be the only one who can save her when the world spins out of control. And the weapon in his possession might very well be the most incendiary weapon of all. Once ignited, passion isn't something she can escape....\r\n\r\n"+
				"FULL COMMITTMENT Still tormented by a past he can't change, Greg Bentley has been living the life of another man for so long that he's almost forgotten who he was before. With a score to settle and a secret to keep, he'd sooner sell his soul to the devil than blow his cover. Working undercover for a newly-formed branch of the National Security Administration and Homeland Security, his identity and mission cannot be compromised, but keeping that secret might be a death sentence for the woman he would risk everything to save.\r\n\r\n"+
				"Locked in a desperate race against time to stop the sale of Tomahawk missiles fitted with nuclear warheads to insurgents in Afghanistan, Kate and Greg must learn to trust each other in a world where nothing is what it seems and love is the most dangerous illusion of all."+
				"where can i download High Altitude free ebook pdf kindle online textbook epub electronic book High Altitude full ebook review amazon ebay collections for android or mobile High Altitude for iphone, ipad txt format version, file with page numbers You can also buy order purchase High Altitude Kindle Edition with Audio Multimedia CD Video Hardcover New or used, Mass market paperback, cheap Audiobook price";
		
		text2 = "a,about,above,after,again,against,all,am,an,and,any,are,aren't,as,at,be,because,been,before,being,below,between,both,but,by,can't,cannot,could,couldn't,did,didn't,do,does,doesn't,doing,don't,down,during,each,few,for,from,further,had,hadn't,has,hasn't,have,haven't,having,he,he'd,he'll,he's,her,here,here's,hers,herself,him,himself,his,how,how's,i,i'd,i'll,i'm,i've,if,in,into,is,isn't,it,it's,its,itself,let's,me,more,most,mustn't,my,myself,no,nor,not,of,off,on,once,only,or,other,ought,our,ours,ourselves,out,over,own,same,shan't,she,she'd,she'll,she's,should,shouldn't,so,some,such,than,that,that's,the,their,theirs,them,themselves,then,there,there's,these,they,they'd,they'll,they're,they've,this,those,through,to,too,under,until,up,very,was,wasn't,we,we'd,we'll,we're,we've,were,weren't,what,what's,when,when's,where,where's,which,while,who,who's,whom,why,why's,with,won't,would,wouldn't,you,you'd,you'll,you're,you've,your,yours,yourself,yourselves";
		//text2 = "";
	}
	@Test
	public void testHashCodeEquals()  {
		WordsCounter wCounter = new WordsCounter(text1, text2, true, new IEncoder(){

			@Override
            public String encode(String string) {
                return "_" + string + "_";
            }

            @Override
            public String decode(String string) {
                return string.substring(1, string.length()-1);
            }
            
		});
		
		ArrayList<Word> sorted = wCounter.getSortedDictionary(2);
		
		for(Word word : sorted){
			System.out.println(word);
		}
	}
}
