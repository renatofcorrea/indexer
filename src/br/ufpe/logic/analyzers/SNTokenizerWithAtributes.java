package br.ufpe.logic.analyzers;


import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;



public class SNTokenizerWithAtributes extends SNTokenizer {
	TypeAttribute ptTypeAtt = null;
	OffsetAttribute offsetAtt = null;
	//ArrayList finalType = null;
	//ArrayList finalOffset = null;
	
	public SNTokenizerWithAtributes(Reader input, String fn) {
		super(input, fn);
		this.ptTypeAtt = (TypeAttribute)addAttribute(TypeAttribute.class);
		this.offsetAtt = (OffsetAttribute)addAttribute(OffsetAttribute.class);
	}

	
	public SNTokenizerWithAtributes(Reader input, HashSet astopWordsList,String fn){
		super(input,astopWordsList,fn);
		this.ptTypeAtt = (TypeAttribute)addAttribute(TypeAttribute.class);
		this.offsetAtt = (OffsetAttribute)addAttribute(OffsetAttribute.class);
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
		termAtt.copyBuffer(word.toCharArray(),0,word.length());
		this.ptTypeAtt.setType(temp.type());
		this.offsetAtt.setOffset(temp.startOffset(),temp.endOffset());
		//System.out.println("increment:"+temp.term()+":"+temp.startOffset()+":"+temp.endOffset());
		this.tokenIndex++;
		return true;
		
	}


	
	void addToFinalToken(ArrayList result, HashMap gramType, HashMap gramOffset){
		this.finalToken = new ArrayList();
		//this.finalType = new ArrayList();
		//System.out.println("size: "+tokenString.size());
		for(int i=0;i<result.size();i++){
			String pattern = (String)result.get(i);
			String type = (String)gramType.get(pattern);
			int offset = (Integer)gramOffset.get(pattern);
			//Token temp = new Token(pattern,offset-pattern.length(), offset, type); //the field name here doesn't matter, it will follow the one in Document.add
			Token temp = new Token(pattern.toCharArray(),0,pattern.length(),offset-pattern.length(),offset); //offset-pattern.length()+1
			temp.setType("SN");
			temp.setPositionIncrement(1);
			//Token temp = new Token(pattern+"\t"+type+"\t"+offset,i, i+1,fieldname); //the field name here doesn't matter, it will follow the one in Document.add
			this.finalToken.add(temp);

			//System.out.println(temp.term()+"\ttype: "+type);
			if(output!=null)
				output.println((String)result.get(i));
		}
	}
	
	/**
	 * tokenize the context, lower case all the words
	 * Unigram will be extracted, which has POS tag: VB, JJ, NN, or their extentions.
	 * 
	 * @param aContext
	 * @return
	 */
	void tokenize(String aContext){ //define as tokens, o método a alterar é este
		System.out.println("sub tokenize");
		aContext = aContext.replace("/", ","); //substitui /
	    aContext = aContext.replace("\"", " ");//substitui aspas
	    
		try{
			//O metodo etiquetar chama  o etiquetador parametrizado em Analyzer através
			// da chamada ao método setTagger de Tokenizer
			//-----Etiquetando o texto
			String res2 = etiquetar(aContext);
			System.out.println(res2);
			//----Extraindo os sintagmas nominais do texto etiquetado
			List<SNData> lsns = JOgma.extraiSNOrdenadoTextoEtiquetado(res2);
//			Vector<String> sns =new Vector<String>(hm.keySet());
//			Vector<Integer> snsind =new Vector<Integer>(hm.values());
//			List<SNData> lsns = new ArrayList<SNData>(SNData.converttoSNDataMap(hm).values());
			//sns contém os sintagmas nominais como aparecem no texto
			//TODO: Neste local pode ser incluido código para eliminar stopwords e lematizar ou reduzir ao radical
			//ver LemmaAnalyzer como exemplo
			//org.apache.lucene.analysis.pt contem stemmers para portugues
			Collections.sort(lsns);
			//Obtendo atributos dos sintagmas nominais
			//String[] chunks = sns.toArray(new String[sns.size()]);
			//Obtendo offsets e type (tag do SN)
			ArrayList resultTokens = new ArrayList();
			ArrayList resultTags = new ArrayList();
			ArrayList resultOffsets = new ArrayList();
			int endOffset= aContext.length();
			int index = 0;
			aContext = aContext.toLowerCase();
			int delta = -1;
			int iaprox = -1;
			for(SNData sni: lsns){
				String tempWord = sni.getSN().toLowerCase();
				if(tempWord.length()<=2 || this.punc.indexOf(tempWord)>=0){//if there is a letter or punc, break.
					System.out.println("Passando e pulando pontuação ou sn muito curtos (duas ou menos letras isoladas): "+tempWord);
					break;
				}
				//if(!this.stopWords.contains(tempWord) && this.openTag.get(chunks2[1])!=null && !Util.isNumber(tempWord)){
				//because we will use phrase filter, so we don't need to filter it here now.
				if(this.stopWords!=null && this.stopWords.contains(tempWord)){
					System.out.println("Passando e pulando sn stopword: "+tempWord);
					break;
				}
				if(tempWord.length() > 1){
					//resultTags.add(chunks2[1]);
					int indexW = aContext.indexOf(tempWord,sni.getIndiceInicio());
					if(indexW < 0){
						if(delta == -1)
							delta = tempWord.length();
						iaprox = sni.getIndiceInicio() - delta;//-10 correcao de erro no indice devido a contracoes não substituidas
						iaprox = (iaprox>=0 && iaprox < aContext.length())?iaprox:0;

						indexW = aContext.indexOf(tempWord,iaprox);
						if(indexW < 0){
							indexW = aContext.indexOf(tempWord,endOffset);//0
							if(indexW >= 0)
								delta =  sni.getIndiceInicio() - indexW;
						}
					}

					if(indexW >=0 && indexW < aContext.length()){
						resultTokens.add(tempWord);
						resultTags.add("SN");
						index = indexW;
						endOffset = index+tempWord.length();
						resultOffsets.add(endOffset);
						index = endOffset;
						//System.out.println("SNTokenizer.tokenize():"+tempWord+" start:"+indexW+" end: "+endOffset);
					}else
					{
						//TODO: reconstruindo os indices, dando pau!!!!!!!!!!!!!!!!!
						//problema na conversão de contrações não realizadas no texto original tratar à, de este
						int e=-1, s=-1;
						String[] ss = tempWord.split(" ");
						int [] iss = new int[ss.length];
						iaprox = (iaprox>=0)?iaprox:0;

						for(int i=0; i< ss.length; i++){
							if(ss[i].length() > 3){
								iss[i] = aContext.indexOf(ss[i],iaprox);
								if(iss[i] >=0)
									iaprox = iss[i]+ss[i].length();
							}
							else
								iss[i] = -1;

						}


						int indexfirst = -1;
						int indexend = - 1;
						int init = 0;
						for(int i=0; i< ss.length; i++){
							//if(iss[i] >= 0 && iss[i-1] >=0 && iss[i] > iss[i-1] && (iss[i] - iss[i-1] - 1)== ss[i-1].length()){
							if(iss[i]>=0){
								if(indexfirst < 0)
									indexfirst = i;
								else if(indexfirst >= 0  && iss[indexfirst]>iss[i]){
									iss[indexfirst]=-1;
									indexfirst = i;
								}

								if(indexend < i)
									indexend  = i;
								//}else{
								else if(indexend > 0 && iss[i] >= iss[indexend]){
									iss[indexend]=-1;
									indexend = i;
								}
								//else if(indexend > 0 && iss[i] < (iss[i-1] + ss[i-1].length()+1))
								//	iss[i]=-1;

								//									if(indexend > 0 && (indexend - indexfirst) > 1){
								//										iss[i]= -1;
								//										indexend = -1;
								//									}


							}
						}

						if(indexfirst >= 0 && indexend>= indexfirst){
							//Reconstrua indice inicial
							if(indexfirst >= 0){
								s = iss[indexfirst];
								if(indexfirst != 0)
									for(int i=indexfirst-1; i >=0; i--){
										s -= ss[i].length()+1;
										if(aContext.charAt(s)==' ' || Character.isAlphabetic(aContext.charAt(s-1)))
											s++;

									}
							}
							//Reconstrua indice final
							if(indexend >= 0){
								e = iss[indexend];
								if(indexend < ss.length-1)
									for(int i=indexend; i < ss.length; i++){
										e += ss[i].length()+1;
									}
								int tempi = aContext.indexOf(ss[indexend],e);
								if(e != tempi && tempi >= 0)
									e = aContext.indexOf(ss[indexend],s);
							}
							e += ss[ss.length - 1].length();
							s = (s>=0)?s:0;

							String subs = aContext.substring(s,(e<aContext.length() && e > 0)?e:(aContext.length()-1));//*****StringIndexOutOfBounds
							int d = StringUtils.indexOfDifference(subs,tempWord);
							System.out.println("SNTokenizer.tokenize(): Não encontrado: "+tempWord+ " Encontrado: "+subs);
							if (StringUtils.getLevenshteinDistance(subs, tempWord) <= 6){//espaços antes do sinal de pontuação tb
								delta =  sni.getIndiceInicio() - s; 
								index = s;
								endOffset = e;
								resultTags.add("SN");
								resultOffsets.add(endOffset);
								index = endOffset;
								resultTokens.add(subs);
							}else{
								System.out.println("\n===>Não contornado!!!");
							}
						}
					}
				}
			}

			HashMap tags = new HashMap();
			HashMap offsets = new HashMap();
			//formatando
			for(int i = 0;i<resultTokens.size();i++){
				//tokens.add((String)resultTokens.get(i));
				tags.put((String)resultTokens.get(i),(String)resultTags.get(i));
				offsets.put((String)resultTokens.get(i),(Integer)resultOffsets.get(i));
			}
			addToFinalToken(resultTokens, tags, offsets);

		}catch(Exception e){
			System.out.println("content:"+aContext);
			e.printStackTrace();
		}


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

	

	/**
	 * Because it may has -1, so it is in the form of a,b;c,d;e,f
	 * @param offset
	 * @return
	 */
	public static int[] parseOffset(String offset){
		int [] offsets = new int[6];
		String[] chunks = offset.split(";");
		for(int i=0;i<3;i++){
			String[] chunks2 = chunks[i].split(",");
			offsets[2*i] = Integer.parseInt(chunks2[0]);
			offsets[2*i+1]=Integer.parseInt(chunks2[1]);
		}
		return offsets;
	}

	public static int[] parseOffsetPair(String offset){
		int[] offsets = new int[4];
		String[] chunks = offset.split(";");
		for(int i=0;i<chunks.length;i++){
			String[] chunks2 = chunks[i].split("-");
			offsets[2*i] = Integer.parseInt(chunks2[0]);
			offsets[2*i+1]=Integer.parseInt(chunks2[1]);
		}
		return offsets;
	}

	/**
	 * Get the more previous start offset
	 * @param pre
	 * @param current
	 * @return
	 */
	int getStart(int pre, int current){
		int start=current;
		if(pre!=start){
			if(start==-1){
				if(pre!=-1)
					start = pre;
			}else{
				if(pre!=-1 && pre<start)
					start = pre;
			}
		}
		return start;
	}

	/**
	 * Get the bigger range offset.
	 * @param pre
	 * @param current
	 * @return
	 */
	int getEnd(int pre, int current){
		int end = current;
		if(pre!=end){
			if(end==-1){
				if(pre!=-1)
					end = pre;
			}else{
				if(pre!=-1 && pre>end)
					end = pre;
			}
		}
		return end;
	}

	boolean isOverlap(int start1, int start2, int end1, int end2){
		if(start1<=start2 && start2<=end1)
			return true;
		if(start2<=start1 && start1<=end2)
			return true;
		return false;
	}

}
	


