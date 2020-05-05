package br.ufpe.logic.analyzers;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
//import org.apache.lucene.analysis.tokenattributes.TermAttribute;  //lucene 2.9.0
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;



public class SNTokenizer extends Tokenizer{
	HashSet stopWords = null;
	//HashMap openTag = null;
	String context;
	ArrayList finalToken  = null;
	int tokenIndex = 0;
	public static final String punc = ".!?;()[]\"";
	public static PrintStream output = null;
	private static final String [] etiquetadores = {"TreeTagger","Cogroo","AeliusStanfordMM","LXTagger","MXPOST","Ogma"};//ordem aproximada de desempenho
	//TermAttribute termAtt=null; //lucene 2.9.0
	CharTermAttribute termAtt=null;
	public static String fieldname;
	private static String tagger = null;
	
	public static String[] getTaggerNames() {
		return etiquetadores;
	}

	
	public static String getTagger() {
		return tagger;
	}
	public static void setTagger(String tagger) {
		SNTokenizer.tagger = tagger;
	}
	
	@Override
	public void reset() throws IOException{
		super.reset();
		this.tokenIndex = 0;
		//finalToken  = null;
	}
	
	

	public SNTokenizer(Reader input, String fn) {
		super(input);
		this.fieldname = fn;
		StringBuffer contextBuffer = new StringBuffer();
		try{
			super.setReader(input);
			this.reset();
			input.reset();
			int c = this.input.read();
			while(c!=-1){
				contextBuffer.append((char)c);
				c = this.input.read();
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
		this.context = contextBuffer.toString();
		//System.out.println(context);
		//get the tokens for every sentence
		if(contextBuffer.length()>3){

			this.tokenize(context);
		}
//		this.termAtt = (TermAttribute) addAttribute(TermAttribute.class); //lucene 2.9.0
		this.termAtt = (CharTermAttribute) addAttribute(CharTermAttribute.class);

	    this.tokenIndex = 0;
	}
	public SNTokenizer(Reader input, HashSet astopWordsList,String fn){
		this(input,fn);
		this.stopWords = astopWordsList;
	}
	
	public static void setPrint(PrintStream out){
		output = out;
	}

	public static void closeOut(){
		output.close();
	}

	@Override
	public boolean incrementToken() throws IOException {
		clearAttributes();

		if (this.finalToken==null || this.tokenIndex>=this.finalToken.size()) {
			return false;
		}
		Token temp = (Token)this.finalToken.get(this.tokenIndex);
		//this.termAtt.setTermBuffer(temp.term());
		String word = temp.toString();
		termAtt.copyBuffer(word.toCharArray(),0,word.length());//
		//System.out.println("increment:"+temp.term()+":"+temp.startOffset()+":"+temp.endOffset());
		this.tokenIndex++;
		return true;
	}

	
	/**
	 * tokenize the context, lower case all the words
	 * Unigram will be extracted, which has POS tag: VB, JJ, NN, or their extentions.
	 * 
	 * @param aContext
	 * @return
	 */
	void tokenizeOld(String aContext){ //define as tokens, o método a alterar é este
		System.out.println("super tokenize");
		ArrayList result = new ArrayList();
		HashSet grams = new HashSet();
		String res=null;
		//String[] segments = new String[3];
		//for(int i=0;i<segments.length;i++)
		//	segments[i] = "";
		try{
			//ArrayList tokenContext = new ArrayList();
			//ArrayList tagContext = new ArrayList();
			//getContext( aContext,  tokenContext,tagContext);
			//String res = JOgma.etiquetar(aContext);
			res = etiquetar(aContext);
			Vector<String> snsf = new Vector<String>(JOgma.extraiSNTextoEtiquetado(res).keySet());
			grams.addAll(snsf);
			result.addAll(grams);
		}catch(Exception e){
			System.out.println("content:"+aContext);
			System.out.println("tagged content:"+ res);
			e.printStackTrace();
		}

		addToFinalToken(result);
	}
	
	void tokenize(String aContext){ //define as tokens, o método a alterar é este
		System.out.println("super tokenize");
		aContext = aContext.replace("/", ","); //substitui /
	    aContext = aContext.replace("\"", " ");//substitui aspas
	    
		List<SNData> result = new ArrayList<SNData>();
		//HashSet grams = new HashSet();
		String res=null;
		try{
			res = etiquetar(aContext);
			result = JOgma.extraiSNOrdenadoTextoEtiquetado(res);//TODO: adicionar possibilidade de extração de sn aninhados
			
		}catch(Exception e){
			System.out.println("content:"+aContext);
			System.out.println("tagged content:"+ res);
			e.printStackTrace();
		}

		addToFinalToken(result);
	}
	
	
	
	/**
	 * @param aContext
	 * @return
	 */
	protected String etiquetar(String aContext) {
		//etiquetar de outros etiquetadores para o português do Brasil neste 
		    
			if(tagger .equals("TreeTagger"))//treetagger do macmorpho nilc muito bom e rápido
				return JTreeTagger.getInstance().etiquetar(aContext);
//			else if(tagger.equals("AeliusStanfordMM"))//StanfordPOS
//				return JStanfordTagger.getInstance().etiquetar(aContext);
//			else if(tagger.equals("LXTagger"))//MXPOST pt portugal
//				return JMXPOSTTagger.getInstance(JMXPOSTTagger.getModels()[0]).etiquetar(aContext);
//			else if(tagger.equals("MXPOST"))//MXPOST, TODO: adicionar outro MXPOST de Lemar
//				return JMXPOSTTagger.getInstance(JMXPOSTTagger.getModels()[1]).etiquetar(aContext);
//			else if(tagger.equals("MAC-MORPHO80-20"))//MXPOST nilc macmorpho
//				return JMXPOSTTagger.getInstance(JMXPOSTTagger.getModels()[2]).etiquetar(aContext);
			else //if(tagger.equals("Ogma")){
				return JOgma.etiquetar(aContext);//retorna mais de uma tag por palavra, chamar deriva
//			}
//			else if(tagger.equals("Cogroo"))//(opennlp) bom, mas demasiadamente lento
//				return TextAnalyzerCogroo.getInstance().etiquetar(aContext);
//			else
//				return TextAnalyzerCogroo.getInstance().etiquetar(aContext); //default
	
	}
	
	
	/**
	 * tokenize the context, filter out numbers and some stopwords.
	 * @param aContext
	 * @param tokens
	 * @param tags
	 */
	public void getContext(String aContext, ArrayList tokens, ArrayList tags){
	//separa tokens, tags e filtra stopwords	
		String[] chunks = aContext.split(" ");
		int i=0;
		for(;i<chunks.length;i++){
			if(chunks[i].length()>1){
				String[] chunks2 = chunks[i].split("/");//word/tag/lemma
				if(chunks2.length==3){
					String tempWord = chunks2[2].trim().toLowerCase();
					if(!this.stopWords.contains(tempWord)){
						tokens.add(tempWord);
						tags.add(chunks2[1]);
					}
				}
			}
		}
	}

	

	void addToFinalToken(List<SNData> result){
		System.out.println("super addToFinalToken.");
		this.finalToken = new ArrayList();
		//System.out.println("size: "+tokenString.size());
		for(int i=0;i<result.size();i++){
			Token temp = new Token((String)result.get(i).getSN(),
					i, i+1, this.fieldname); //the field name here doesn't matter, it will follow the one in Document.add
			this.finalToken.add(temp);
			if(output!=null)
				output.println((String)result.get(i).getSN());
		}
	}


	/**
	 * @param texto
	 * @return texto formatado para tokenização e etiquetagem
	 */
	public static String formatText(String texto) {
		texto = texto.replace("/",",");
		String [] pontChars = new String [] {",", ".", ":",";","!","?","(",")","[","]","\""};
		for(int i = 0; i < pontChars.length; i++)
			texto = texto.replace(pontChars[i]," "+pontChars[i]+" ");
		texto = texto.replace('\t', ' ');
		texto = texto.replace('\n', ' ');
		return texto;
	}

	

}
