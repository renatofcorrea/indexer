package br.ufpe.logic.analyzers;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.acl.LastOwnerException;
import java.util.Scanner;
import java.util.Vector;

public class JPalavras {
	private static JPalavras myInstance = null;
	private String strurlbase = null;
	private String metainfFlat = null;
	private String metainfTree = null;
	private String metainfCG = null;
	private String ftext = null;
	private HTTPContentHandler httpch;

	private String sn = null;

	private String getOgmaFormat(String s) {
		String frase = new String();
		String flemma = new String();
		String fsem = new String();
		System.out.println(s);
		// Tratar codificação UTF-8, não mais necessário o palavras trabalha
		// como iso-8859-1
		// sequencia <dl>
		// <dt> <b> <font color= "maroon"> word </font> </b>
		// <font color= "*"> maroon [lemma] </font> {&lt;annotation&gt;} blue
		// <b> tag </b> morpho </font> {darkgreen @function-dependency} </font>
		// ...if pontuaction only the caracter in the line, except some times:
		// maroon <b>,</b> marron [,] &lt;annotation&gt; PU darkgreen @CO
		// <dl>
		final String otdt = "<dt>"; // marca inicio da descrição de um termo
		final String olemma = "[";// marca inicio do lema de um termo
		final String clemma = "]";
		final String otf = "<font ";
		final String ctf = "</font>";
		final String ocode = "&lt;";
		final String ccode = "&gt;";
		final String otbold = "<b>";
		final String ctbold = "</b>";
		final String bdepend = "@";
		final String ctdl = "</dl>";// marca fim do texto
		final int eof = s.indexOf(ctdl);
		

		int i = 0, f = 0, l = 0, k = 0;
		boolean hasTerms = true;
		String descr = "";
		String token = "";
		String lemma = "";
		String sem = "";//semantic annotation
		String tag = "";
		String morpho = "";
		String dep = "";
		while (hasTerms) {
			k=l=-1;
			i = s.indexOf(otdt, f);
			tag = "";
			token = "";
			lemma = "";
			morpho = "";
			sem = "";
			dep = "";
			if (i < 0)
				break;
			f = s.indexOf(otdt, i + otdt.length());
			f = (f >= 0) ? f : eof;
			descr = s.substring(i, f);
			l = descr.indexOf(olemma);
			if (l >= 0) {
				token = descr.substring(0, l);
				token = removeTags(token);// token
				k = descr.indexOf(clemma);
				if (k >= 0) {
					lemma = descr.substring(l + 1, k);// lemma
					// k = descr.indexOf(clemma);
					k = descr.indexOf(ctf, k) + ctf.length();
					l = descr.indexOf(otf, k);
					if (l > 0) {
						sem = descr.substring(k, l);
						sem = replaceHTMLChars(sem);
						sem = sem.trim();// anotação semântica
					}
					k = descr.indexOf(otbold, k);
					if(k<0)
						l=-1;
					else
					l = descr.indexOf(ctbold, k);
					if (k > 0 && l >= 0) {
						tag = descr.substring(k + otbold.length(), l);// java.lang.StringIndexOutOfBoundsException
						tag = removeTags(tag);// não funciona para todos os
												// casos tags antes
						tag = replaceHTMLChars(tag);// tag
					} else {
						tag = sem.substring(sem.lastIndexOf(" ")).trim();
						if (tag.contains("<"))
							tag = "";

					}
					if (tag.isEmpty()) {
						if (token.indexOf(".,!?:;'\"") >= 0) {
							tag = "PN";
							lemma = token;
						}
					}
					if(l > 0) {
					k = l + ctbold.length();
					l = descr.indexOf(ctf, k);
					}
					if (l > 0) {
						morpho = descr.substring(k, l);
					}
					k = descr.indexOf(bdepend, l);
					l = descr.lastIndexOf(ctf);
					if (k > 0) {
						dep = descr.substring(k, l);
						dep = removeTags(dep);
						dep = replaceHTMLChars(dep);// tag
					}
				}

			} else {

				l = descr.indexOf(ctbold);
				token = descr.substring(0, l);
				token = removeTags(token);
				tag = "";
				lemma = token;
				if (tag.isEmpty()) {
					if (".,!?:;'\"".indexOf(token) >= 0)
						tag = "PN";
				}
			}
//TODO: separador de tokens pode ser espaço em branco ou nova linha, parametrizar
			frase = frase + getOgmaTags(tag, token, lemma, morpho) +" ";// "\n";// ainda
			//frase = frase + token + ";" + lemma + ";" +sem+ ";" + tag + ";" + morpho + ";" + dep  + "\n";
																			// há
																			// outras
																			// tags
																			// a
																			// frente
			flemma = flemma + lemma + ",";
			fsem = fsem + sem + ",";
		}
		return frase;

	}
	
	private String getCVSFormat(String s) {//chame em Etiquetar ao invés de getOgmaFormat
		String frase = new String();
		String flemma = new String();
		String fsem = new String();
		System.out.println(s);
		// Tratar codificação UTF-8, não mais necessário o palavras trabalha
		// como iso-8859-1
		// sequencia <dl>
		// <dt> <b> <font color= "maroon"> word </font> </b>
		// <font color= "*"> maroon [lemma] </font> {&lt;annotation&gt;} blue
		// <b> tag </b> morpho </font> {darkgreen @function-dependency} </font>
		// ...if pontuaction only the caracter in the line, except some times:
		// maroon <b>,</b> marron [,] &lt;annotation&gt; PU darkgreen @CO
		// <dl>
		final String otdt = "<dt>"; // marca inicio da descrição de um termo
		final String olemma = "[";// marca inicio do lema de um termo
		final String clemma = "]";
		final String otf = "<font ";
		final String ctf = "</font>";
		final String ocode = "&lt;";
		final String ccode = "&gt;";
		final String otbold = "<b>";
		final String ctbold = "</b>";
		final String bdepend = "@";
		final String ctdl = "</dl>";// marca fim do texto
		final int eof = s.indexOf(ctdl);
		

		int i = 0, f = 0, l = 0, k = 0;
		boolean hasTerms = true;
		String descr = "";
		String token = "";
		String lemma = "";
		String sem = "";//semantic annotation
		String tag = "";
		String morpho = "";
		String dep = "";
		while (hasTerms) {
			k=l=-1;
			i = s.indexOf(otdt, f);
			tag = "";
			token = "";
			lemma = "";
			morpho = "";
			sem = "";
			dep = "";
			if (i < 0)
				break;
			f = s.indexOf(otdt, i + otdt.length());
			f = (f >= 0) ? f : eof;
			descr = s.substring(i, f);
			l = descr.indexOf(olemma);
			if (l >= 0) {
				token = descr.substring(0, l);
				token = removeTags(token);// token
				k = descr.indexOf(clemma);
				if (k >= 0) {
					lemma = descr.substring(l + 1, k);// lemma
					// k = descr.indexOf(clemma);
					k = descr.indexOf(ctf, k) + ctf.length();
					l = descr.indexOf(otf, k);
					if (l > 0) {
						sem = descr.substring(k, l);
						sem = replaceHTMLChars(sem);
						sem = sem.trim();// anotação semântica
					}
					k = descr.indexOf(otbold, k);
					if(k<0)
						l=-1;
					else
					l = descr.indexOf(ctbold, k);
					if (k > 0 && l >= 0) {
						tag = descr.substring(k + otbold.length(), l);// java.lang.StringIndexOutOfBoundsException
						tag = removeTags(tag);// não funciona para todos os
												// casos tags antes
						tag = replaceHTMLChars(tag);// tag
					} else {
						tag = sem.substring(sem.lastIndexOf(" ")).trim();
						if (tag.contains("<"))
							tag = "";

					}
					if (tag.isEmpty()) {
						if (token.indexOf(".,!?:;'\"") >= 0) {
							tag = "PN";
							lemma = token;
						}
					}
					if(l > 0) {
					k = l + ctbold.length();
					l = descr.indexOf(ctf, k);
					}
					if (l > 0) {
						morpho = descr.substring(k, l);
					}
					k = descr.indexOf(bdepend, l);
					l = descr.lastIndexOf(ctf);
					if (k > 0) {
						dep = descr.substring(k, l);
						dep = removeTags(dep);
						dep = replaceHTMLChars(dep);// tag
					}
				}

			} else {

				l = descr.indexOf(ctbold);
				token = descr.substring(0, l);
				token = removeTags(token);
				tag = "";
				lemma = token;
				if (tag.isEmpty()) {
					if (".,!?:;'\"".indexOf(token) >= 0)
						tag = "PN";
				}
			}
			//TODO: separador de tokens pode ser espaço em branco ou nova linha, parametrizar
			//frase = frase + getOgmaTags(tag, token, lemma, morpho) +" ";// "\n";// ainda
			frase = frase + token + ";" + lemma + ";" +sem+ ";" + tag + ";" + morpho + ";" + dep  + "\n";
																			// há
																			// outras
																			// tags
																			// a
																			// frente
			flemma = flemma + lemma + ",";
			fsem = fsem + sem + ",";
		}
		return frase;

	}

	private static String getOgmaTags(String tag, String token, String lemma,
			String morpho) {
		String[] subtags = tag.split(" ");
		int nst = subtags.length;
		String pos = subtags[nst - 1];
		// adicionar código se split de token > 1 e = em lemma, modificar token
		// para {tokens =} até a penultima token
		if (lemma.indexOf('=') >= 0) {// nt > 1 &&
			// String [] tokens = token.split(" ");
			// int nt = tokens.length;
			token = token.replace(' ', '=');
		}
		if (".,!?:;'\"()".indexOf(token) >= 0 || tag.equals("PU"))
			return token + "/PN";
		else if (pos.equalsIgnoreCase("N")) {
			return token + "/SU";
		} else if (pos.equalsIgnoreCase("PROP")) {
			return token + "/NP";
		} else if (pos.equalsIgnoreCase("ADJ")) {
			return token + "/AJ";
		} else if (pos.equalsIgnoreCase("")) {
			return token + "/PN";
		} else if (pos.startsWith("V")) {
			if (pos.equalsIgnoreCase("V")) {
				if (morpho.contains("PCP") || token.endsWith("ido")
						|| token.endsWith("ado") || token.endsWith("ada")|| token.matches(".*(i|a)(d|t)(o|a)[s]?"))
					return token + "/VP"; // contempla VP - verbo participio
				else
					return token + "/VB";
			} else {// V+P
				return token + "/VB";
			}
		} else if (pos.equalsIgnoreCase("DET")) {
			if (lemma.equalsIgnoreCase("o") || lemma.equalsIgnoreCase("a"))
				return token + "/AD";
			else if (lemma.equalsIgnoreCase("um"))
				return token + "/AI";
			else
				// Pronome determinativo
				return token + "/PD";// (PS),PD,PI, PL
		} else if (pos.equalsIgnoreCase("PERS")) {
			return token + "/PP";
		} else if (pos.equalsIgnoreCase("SPEC")) {
			return token + "/PL";// PD,PI, PL
		} else if (pos.equalsIgnoreCase("ADV")) {
			return token + "/AV";
		} else if (pos.equalsIgnoreCase("KS") || pos.equalsIgnoreCase("KC")) {
			return token + "/CJ";
		} else if (pos.startsWith("PRP")) {
			if (pos.endsWith("+DET")) {
				int i = token.indexOf("o");
				int j = token.indexOf("a");
				if (i >= 0)
					return lemma.toLowerCase() + "/PR " + token.substring(i)
							+ "/AD";
				else
					return lemma.toLowerCase() + "/PR " + token.substring(j)
							+ "/AD";
			} else
				return token + "/PR";
		} else if (pos.equalsIgnoreCase("NUM")) {
			return token + "/NC";
		} else {
			System.out.println("NAO CONTEMPLADA: " + token + " " + pos + " "
					+ lemma);
			return token + "/NR";
		}
	}

	private static String replaceHTMLChars(String text) {
		text = text.replace("&lt;", "<");
		text = text.replace("&gt;", ">");
		return text;
	}

	private static String removeTags(String text) {
		// Retirando tudo que tiver entre < e >
		int i1, i2; // i1 == index_de_< i2 ==index_de_>
		String separador = "";
		i1 = text.indexOf('<');
		while (i1 != -1) {
			// System.out.println("Inicio de uma tag...");
			i2 = text.indexOf('>', i1);
			if (i2 != -1) {
				// System.out.println("Fim de uma tag...");
				text = text.substring(0, i1) + separador
						+ text.substring(i2 + 1);
			}
			i1 = text.indexOf('<', i1);
		}
		return text.trim();
	}

	private JPalavras() {
		sn = "";
		strurlbase = "http://beta.visl.sdu.dk/cgi-bin/visl.pt.cgi?"; // flat
																		// &parser=parse
																		// ->
																		// &parser=dep-eb
		metainfFlat = "&parser=parse&visual=niceline&heads=&symbol=&multisearch=&searchtype=&inputlang=pt";
		metainfTree = "&parser=tree&visual=vertical&heads=&symbol=default&multisearch=&searchtype=&inputlang=pt";
		metainfCG = "&parser=tree&visual=source&heads=&symbol=cg&multisearch=&searchtype=&inputlang=pt";
		// "&parser=tree&visual=source&heads=&symbol=default&multisearch=&searchtype=&inputlang=pt";
		httpch = new HTTPContentHandler();
		URLConnection.setContentHandlerFactory(httpch);
	}

	public static JPalavras getInstance() {
		if (myInstance == null) {
			myInstance = new JPalavras();
		}

		return myInstance;
	}

	public String Etiquetar(String texto) {
		try {
			ftext = "text=" + URLEncoder.encode(texto, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String metainf = ftext + metainfFlat;
		String tagtext = new String();
		tagtext = getResponse(metainf);
		// Convertendo para ASCII
		// Charset utf8charset = Charset.forName("UTF-8");
		// Charset iso88591charset = Charset.forName("ISO-8859-1");
		// try {
		// tagtext = new String(tagtext.getBytes("ASCII"));
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// }

		// System.out.println(tagtext); //imprime o arquivo recebido
		if (tagtext == null)
			return tagtext;
		else
			return getOgmaFormat(tagtext);
	}
	
	public String EtiquetarCVSFormat(String texto) {
		try {
			ftext = "text=" + URLEncoder.encode(texto, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String metainf = ftext + metainfFlat;
		String tagtext = new String();
		tagtext = getResponse(metainf);
		// Convertendo para ASCII
		// Charset utf8charset = Charset.forName("UTF-8");
		// Charset iso88591charset = Charset.forName("ISO-8859-1");
		// try {
		// tagtext = new String(tagtext.getBytes("ASCII"));
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// }

		// System.out.println(tagtext); //imprime o arquivo recebido
		if (tagtext == null)
			return tagtext;
		else
			return getCVSFormat(tagtext);
	}

	public String getSNs(String texto) {
		sn = "";
		// texto = texto.replace(",",";");//só dá certo para frases com
		// enumeração e por isso muitas vírgulas, caso contrário, gera erros
		String tagtext = getTreeFile(texto, "source", "default");
		// System.out.println(tagtext);
		// Arquivo recebido
		String[] linhas = tagtext.split("\n");
		// return getSNsFromTree(texto);
		int i = 0;
		int j = 0;
		String[] sninfo = null;
		while (i < linhas.length) {
			sninfo = null;
			sninfo = getSNsFromTreeSource(linhas, i);// melhor implementação
			if (sninfo == null) {
				// i++;
				// continue;
				break;
			} else {
				if (!sninfo[1].isEmpty()) {
					sn += "/" + sninfo[1];
					System.out.print(sninfo[5] + "/");
				}
				j = Integer.parseInt(sninfo[0]);
			}
			if (j > i)
				i = j;
			else
				i++;
		}// end while
			// System.out.println(sn);
		System.out.println(" ");
		return sn;
		// getSNsFromFlat não operacional
		// return getSNsFromFlat(texto);
	}

	// load file content in a string
	static String readFile(String path, Charset encoding) {
		// encoding = Charset.defaultCharset();
		// encoding = StandardCharsets.UTF_8;
		// encoding = StandardCharsets.ISO_8859_1;

		byte[] encoded = null;
		try {
			encoded = Files.readAllBytes(Paths.get(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new String(encoded, encoding);
	}

	public String getFlatFile(String texto) {
		// veja tese de agnaldo para extracao de sns do texto etiquetado:
		// http://www.bibliotecadigital.ufmg.br/dspace/bitstream/handle/1843/BUOS-9RQHC6/tese_doutorado___entregue_no_cd_em_28112014.pdf?sequence=1
		// Entretanto a saída do palavras que mais se aproxima é obtida abaixo,
		// mas parece ser mais difícil de tratar que tree

		// url http://beta.visl.sdu.dk/visl/pt/parsing/automatic/parse.php
		// Parser: dependency links Vizualization: default
		/*
		 * Parser: <SELECT NAME=parser> <OPTION VALUE="parse">Full
		 * morphosyntactic parse <OPTION VALUE="morphdis">Morphological tagging
		 * <OPTION VALUE="morph">Analyzer Pure <OPTION VALUE="dep-eb">dependency
		 * links <OPTION VALUE="roles">Semantic roles </SELECT>
		 * 
		 * Visualization: <SELECT NAME=visual> <OPTION VALUE="niceline">Default
		 * <OPTION VALUE="cohorts">Cohorts <OPTION VALUE="flat">Colors <OPTION
		 * VALUE="paintbox">Paintbox Game </SELECT>
		 */
		String parservalue = "dep-eb";
		String visualvalue = "niceline";
		String metainfFlat = "&parser=" + parservalue + "&visual="
				+ visualvalue
				+ "&heads=&symbol=&multisearch=&searchtype=&inputlang=pt";
		try {
			ftext = "text=" + URLEncoder.encode(texto, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		String metainf = ftext + metainfFlat;
		String tagtext = new String();
		tagtext = getResponse(metainf);
		// System.out.println(tagtext); //imprime o arquivo recebido
		if (tagtext == null)
			return tagtext;
		else
			return tagtext;
		// Arquivo recebido deveria conter
		// colunas: palavra,[forma canonica],marcacoes semanticas(<>),marcacoes
		// morfosintaticas(classe,genero,numero),relacao de
		// dependencia(#wordsseq->wordsseq)
		// ao final das marcacoes semanticas, temos os marcadores de fim de
		// sintagmas(<np-close> <np-long>), a palavra marcada como fim pode ou
		// nao ser parte do sn
		// ao final das marcacoes morfosintaticas, temos os marcadores de
		// inicio(@>N ou @&gt;N) e fim de sintagmas(@N<)
		// seria executado algo semelhante a getOgmaFormat para obter os SNs,
		// porém mais complexo que tree.
	}

	public String getSNsFromTree(String texto) {// função com pior resultado
												// devido a falhas do palavras.
		String tagtext = getTreeFile(texto, "", "");
		// Arquivo recebido
		String[] linhas = tagtext.split("\n");
		boolean maximussnonly = true;// false; -> precisa ser melhor investigado
		for (int i = 0; i < linhas.length; i++) {// itera nas linhas do arquivo
			// ignore "|-   "
			String ruido = "|-   ";
			int indexl = linhas[i].indexOf(ruido);
			if (indexl >= 0) {
				linhas[i] = linhas[i].replace(ruido, "     ");
			}

			int index = linhas[i].indexOf("(np)");
			int indexn = linhas[i].indexOf(":prop");
			int indexn1 = linhas[i].indexOf(":n");
			if (index >= 0) {// obtendo sn
				int nBars = getNumBars(linhas[i]);
				int nesp = getNumSpaces(linhas[i]);
				index = linhas[i].indexOf(":");
				String papelsn = linhas[i].substring(
						linhas[i].indexOf("-") + 1, index);
				String sn = "";
				String snLem = "";
				String snTag = "";
				String snFun = "";
				String taglw = "";
				int j = i + 1;

				while (j < linhas.length) {// itera nas linhas formadoras do
											// (np)
					// ignore "|-   "
					indexl = linhas[j].indexOf(ruido);
					if (indexl >= 0) {
						linhas[j] = linhas[j].replace(ruido, "     ");
					}

					if (linhas[j].equals(".")) {
						j++;
						continue;
					}
					int nBarsj = getNumBars(linhas[j]);
					int nespj = getNumSpaces(linhas[j]);
					if (nBarsj < nBars || (nBarsj <= nBars && nespj <= nesp))
						break;
					int indexj = linhas[j].indexOf(":");
					String papelw = linhas[j].substring(
							linhas[j].lastIndexOf("|-") + 2, indexj);
					int bindexlemma = linhas[j].indexOf("\"");

					if (bindexlemma > 0) {
						String tagw = linhas[j].substring(indexj + 1,
								linhas[j].indexOf("("));
						if (tagw.equals("pron")) {
							tagw = tagw
									+ "-"
									+ linhas[j].substring(
											linhas[j].indexOf("(") + 1,
											linhas[j].indexOf("\""));
						}
						int eindexlemma = linhas[j].indexOf("\"",
								bindexlemma + 1);
						String lemma = linhas[j].substring(bindexlemma + 1,
								eindexlemma);
						int indexw = linhas[j].indexOf("\t");
						String word = linhas[j].substring(indexw + 1);
						// se conj,prep,pron e proximo nao for palavra ou inicio
						// sn || (!linhas[j+1].contains("\"") &&
						// !linhas[j+1].contains("(np)"))
						if (((tagw.equals("conj") || tagw.equals("prp")) && sn
								.isEmpty())
								|| (tagw.equals("conj") || tagw.equals("prp") || tagw
										.startsWith("pron"))
								&& ((getNumBars(linhas[j + 1]) > 0 && getNumBars(linhas[j + 1]) < nBars) && getNumSpaces(linhas[j + 1]) < nesp)) {
							// ignore a conjunção, "prp", "pron"
							// &... || sn.isEmpty() -> efeito colateral impede
							// sn de começar com pron-det (qualquer)
						} else {
							sn += word + " ";
							snLem += lemma + " ";
							snTag += tagw + " ";
							snFun += papelw + " ";
							taglw = tagw;
						}
					} else {
						int indexap = linhas[j].indexOf("(");
						if (indexap < indexj)
							indexap = linhas[j].length();
						String tagw = linhas[j].substring(indexj + 1, indexap);
						if (tagw.equals("cl"))
							break;

					}
					j++;

				}// end while(j<linhas.length)
				if (!sn.isEmpty()) {
					if (taglw.equals("conj") || taglw.equals("prp")) {
						// || taglw.startsWith("pron") -> os mesmos.
						// tem que ser um while para ficar consistente
						sn = removelast(sn);
						snLem = removelast(snLem);
						snTag = removelast(snTag);
					}
					System.out.println(papelsn);
					System.out.println(sn);
					System.out.println(snLem);
					System.out.println(snTag);
					System.out.println(snFun);
					if (maximussnonly) {
						//
						i = j - 1;
					}
				}
			} else if (indexn >= 0 || indexn1 >= 0) {// obtendo nome próprio ou
														// substantivo isolado
				String sn = "";
				String snLem = "";
				String snTag = "";
				String snFun = "";
				index = linhas[i].indexOf(":");
				snFun = linhas[i].substring(linhas[i].indexOf("-") + 1, index);
				int bindexlemma = linhas[i].indexOf("\"");
				if (bindexlemma > 0) {
					snTag = linhas[i].substring(index + 1,
							linhas[i].indexOf("("));
					int eindexlemma = linhas[i].indexOf("\"", bindexlemma + 1);
					snLem = linhas[i].substring(bindexlemma + 1, eindexlemma);
				}
				int indexw = linhas[i].indexOf("\t");
				sn = linhas[i].substring(indexw + 1);
				if (!sn.isEmpty()) {
					System.out.println(snFun);
					System.out.println(sn);
					System.out.println(snLem);
					System.out.println(snTag);

				}
			}
		}
		return " ";
	}

	public String getSNsMaxFromTreeSource(String texto) {// melhor função,
															// apesar do
															// palavras estar
															// falhando o
															// palavras na
															// identificação.
		String tagtext = getTreeFile(texto, "source", "default");
		// Arquivo recebido
		String[] linhas = tagtext.split("\n");
		boolean maximussnonly = true;// false; -> precisa ser melhor investigado
		for (int i = 0; i < linhas.length; i++) {// itera nas linhas do arquivo
			int index = linhas[i].indexOf("(np)");
			int indexn = linhas[i].indexOf(":prop");
			int indexn1 = linhas[i].indexOf(":n");
			if (index >= 0) {// obtendo sn
				int nBars = getNumEquals(linhas[i]);

				index = linhas[i].indexOf(":");
				String papelsn = linhas[i].substring(
						linhas[i].lastIndexOf("=") + 1, index);
				String sn = "";
				String snLem = "";
				String snTag = "";
				String snFun = "";
				String taglw = "";
				int j = i + 1;
				int nBarsj = -1;
				while (j < linhas.length) {// itera nas linhas formadoras do
											// (np)
					if (linhas[j].equals(".") || linhas[j].equals(":")
							|| linhas[j].equals(",") || linhas[j].equals(")")
							|| linhas[j].equals(";")) {
						j++;
						if (sn.isEmpty())
							continue;
						else
							break;
					}
					int indexj = linhas[j].indexOf(":");
					if (indexj < 0) {
						j++;
						if (sn.isEmpty())
							continue;
						else
							break;
					}
					int oldnBarsj = nBarsj;

					nBarsj = getNumEquals(linhas[j]);
					if ((nBars == 0) && (nBarsj == 0) && (oldnBarsj <= 0))
						;
					else if (oldnBarsj > 0 && nBarsj <= nBars)
						break;

					int indexeq = linhas[j].lastIndexOf("=") + 1;
					String papelw = linhas[j].substring(indexeq, indexj);
					int bindexlemma = linhas[j].indexOf("\"");

					if (bindexlemma > 0) {
						String tagw = linhas[j].substring(indexj + 1,
								linhas[j].indexOf("("));
						if (tagw.equals("pron")) {
							tagw = tagw
									+ "-"
									+ linhas[j].substring(
											linhas[j].indexOf("(") + 1,
											linhas[j].indexOf("\""));
						}
						int eindexlemma = linhas[j].indexOf("\"",
								bindexlemma + 1);
						String lemma = linhas[j].substring(bindexlemma + 1,
								eindexlemma);
						int indexw = linhas[j].indexOf("\t");
						String word = linhas[j].substring(indexw + 1);
						// se conj,prep,pron e proximo nao for palavra ou inicio
						// sn || (!linhas[j+1].contains("\"") &&
						// !linhas[j+1].contains("(np)"))
						if (((tagw.equals("conj") || tagw.equals("prp")) && sn
								.isEmpty())
								|| ((tagw.equals("conj") || tagw.equals("prp") || tagw
										.startsWith("pron")) && ((getNumEquals(linhas[j + 1]) > 0 && getNumEquals(linhas[j + 1]) < nBars)))) {
							// ignore a conjunção, "prp", "pron"
							// impede sn de começar com conj ou prp, ou
							// finalizar com conj, prp ou pron
						} else {
							sn += word + " ";
							snLem += lemma + " ";
							snTag += tagw + " ";
							snFun += papelw + " ";
							taglw = tagw;
						}
					} else {
						int indexap = linhas[j].indexOf("(");
						if (indexap < indexj)
							indexap = linhas[j].length();
						String tagw = linhas[j].substring(indexj + 1, indexap);
						if (tagw.equals("cl"))
							break;

					}
					j++;

				}// end while(j<linhas.length)
				if (!sn.isEmpty() && sn.length() > 2) {
					if (taglw.equals("conj") || taglw.equals("prp")) {
						// || taglw.startsWith("pron") -> os mesmos.
						// tem que ser um while para ficar consistente
						sn = removelast(sn);
						snLem = removelast(snLem);
						snTag = removelast(snTag);
						snFun = removelast(snFun);
					}
					System.out.println(papelsn);
					System.out.println(sn);
					System.out.println(snLem);
					System.out.println(snTag);
					System.out.println(snFun);
					if (maximussnonly) {
						//
						i = j - 1;
					}
				}
			} else if (indexn >= 0 || indexn1 >= 0) {// obtendo nome próprio ou
														// substantivo isolado
				String sn = "";
				String snLem = "";
				String snTag = "";
				String snFun = "";
				index = linhas[i].indexOf(":");
				snFun = linhas[i].substring(linhas[i].indexOf("=") + 1, index);
				int bindexlemma = linhas[i].indexOf("\"");
				if (bindexlemma > 0) {
					snTag = linhas[i].substring(index + 1,
							linhas[i].indexOf("("));
					int eindexlemma = linhas[i].indexOf("\"", bindexlemma + 1);
					snLem = linhas[i].substring(bindexlemma + 1, eindexlemma);
				}
				int indexw = linhas[i].indexOf("\t");
				sn = linhas[i].substring(indexw + 1);
				if (!sn.isEmpty() && sn.length() > 2) {
					System.out.println(snFun);
					System.out.println(sn);
					System.out.println(snLem);
					System.out.println(snTag);

				}
			}
		}
		return " ";
	}

	public String[] getSNsFromTreeSource(String[] linhas, int inlinha) {// melhor
																		// função,
																		// apesar
																		// do
																		// palavras
																		// estar
																		// falhando
																		// o
																		// palavras
																		// na
																		// identificação.
		// sn em variavel membro

		String papelsn = "";
		String sn = "";
		String snLem = "";
		String snTag = "";
		String snFun = "";
		String taglw = "";
		// temp
		String tagw = "";
		String lemma = "";
		String papelw = "";
		boolean verbose = false;
		// boolean maximussnonly = false;//implementado recursivamente
		// delimitando com < > sns internos
		int i = 0;// indice inicio sn
		int index = -1;
		int indexn = -1;
		int indexn1 = -1;
		for (i = inlinha; i < linhas.length; i++) {// ****itera nas linhas do
													// arquivo obtendo inicio do
													// sn
			index = linhas[i].indexOf("(np)");
			indexn = linhas[i].indexOf(":prop");
			indexn1 = linhas[i].indexOf(":n");
			if (index >= 0 || indexn >= 0 || indexn1 >= 0)
				break;
		}
		if (indexn >= 0 || indexn1 >= 0) {// ******obtendo nome próprio ou
											// substantivo isolado
			index = linhas[i].indexOf(":");
			snFun = linhas[i].substring(linhas[i].lastIndexOf("=") + 1, index);
			int bindexlemma = linhas[i].indexOf("\"");
			if (bindexlemma > 0) {
				snTag = linhas[i].substring(index + 1, linhas[i].indexOf("("));
				int eindexlemma = linhas[i].indexOf("\"", bindexlemma + 1);
				snLem = linhas[i].substring(bindexlemma + 1, eindexlemma);
			}
			int indexw = linhas[i].indexOf("\t");
			sn = linhas[i].substring(indexw + 1);
			if (!sn.isEmpty() && sn.length() > 2) {
				if (verbose) {
					System.out.println(snFun);
					System.out.println(sn);
					System.out.println(snLem);
					System.out.println(snTag);
				}
				String[] sninfo = { Integer.toString(i + 1), sn, snLem, snTag,
						snFun, snFun };

				return sninfo;// +"/"+getSNsFromTreeSource(linhas,lbe);

			}
		} else if (index >= 0) {// obtendo sn
			int nBars = getNumEquals(linhas[i]);
			index = linhas[i].indexOf(":");
			papelsn = linhas[i]
					.substring(linhas[i].lastIndexOf("=") + 1, index);
			int j = i + 1;
			int nBarsj = -1;
			while (j < linhas.length) {// itera nas linhas formadoras do (np)
				int oldnBarsj = nBarsj;
				nBarsj = getNumEquals(linhas[j]);
				int indexeq = linhas[j].lastIndexOf("=") + 1;
				papelw = linhas[j].substring(indexeq, indexeq + 1);// obtendo
																	// somente
																	// um
																	// caracter
																	// do papelw
				if (papelw.contains(".") || papelw.contains(":")
						|| papelw.contains(",") || papelw.contains("(")
						|| papelw.contains(")") || papelw.contains(";")) {

					if (sn.isEmpty()) {
						j++;
						continue;
					} else
						break;
				}
				int indexj = linhas[j].indexOf(":");
				if (indexj < 0) {
					j++;
					if (sn.isEmpty())
						continue;
					else
						break;
				}
				index = linhas[j].indexOf("(np)");
				indexn = linhas[j].indexOf(":prop");
				if (index > 0 || indexn > 0) {
					nBarsj = getNumEquals(linhas[j]);
					if (nBarsj > nBars) {
						sn += " <";
						String[] sninfo1 = getSNsFromTreeSource(linhas, j);
						sn += sninfo1[1].trim() + "> ";
						j = Integer.parseInt(sninfo1[0]);
						snLem += " <" + sninfo1[2].trim() + "> ";
						snTag += " <" + sninfo1[3].trim() + "> ";
						snFun += " <" + sninfo1[4].trim() + "> ";
						taglw = "sn";
					} else
						break;
				}

				indexj = linhas[j].indexOf(":");
				nBarsj = getNumEquals(linhas[j]);
				if ((nBars == 0) && (nBarsj == 0) && (oldnBarsj <= 0))
					;
				else if (oldnBarsj > 0 && nBarsj <= nBars)
					break;

				papelw = linhas[j].substring(indexeq, indexj);

				int bindexlemma = linhas[j].indexOf("\"");
				if (bindexlemma > 0) {
					tagw = linhas[j].substring(indexj + 1,
							linhas[j].indexOf("("));
					if (tagw.equals("pron")) {
						tagw = tagw
								+ "-"
								+ linhas[j].substring(
										linhas[j].indexOf("(") + 1,
										linhas[j].indexOf("\""));
					}
					int eindexlemma = linhas[j].indexOf("\"", bindexlemma + 1);
					lemma = linhas[j].substring(bindexlemma + 1, eindexlemma);
					int indexw = linhas[j].indexOf("\t");
					String word = linhas[j].substring(indexw + 1);
					// se conj,prep,pron e proximo nao for palavra ou inicio sn
					// || (!linhas[j+1].contains("\"") &&
					// !linhas[j+1].contains("(np)"))
					if (((tagw.equals("conj") || tagw.equals("prp")) && sn
							.isEmpty())
							|| (tagw.equals("conj") || tagw.equals("prp") || tagw
									.startsWith("pron"))
							&& ((getNumEquals(linhas[j + 1]) > 0 && getNumEquals(linhas[j + 1]) < nBars))) {
						// ignore a conjunção, "prp", "pron"
						// &... || sn.isEmpty() -> efeito colateral impede sn de
						// começar com pron-det (qualquer)
					} else {
						sn += word + " ";
						snLem += lemma + " ";
						snTag += tagw + " ";
						snFun += papelw + " ";
						taglw = tagw;

					}
				} else {
					int indexap = linhas[j].indexOf("(");
					if (indexap < indexj)
						indexap = linhas[j].length();
					tagw = linhas[j].substring(indexj + 1, indexap);
					if (tagw.equals("cl"))
						break;

				}
				j++;

			}// end while(j<linhas.length) ==>encontrado fim do sn
			if (!sn.isEmpty() && sn.length() > 2) {
				if (taglw.equals("conj") || taglw.equals("prp")) {
					// || taglw.startsWith("pron") -> os mesmos.
					// tem que ser um while para ficar consistente
					sn = removelast(sn);
					snLem = removelast(snLem);
					snTag = removelast(snTag);
					snFun = removelast(snFun);
				}
				if (verbose) {
					System.out.println(papelsn);
					System.out.println(sn);
					System.out.println(snLem);
					System.out.println(snTag);
					System.out.println(snFun);
				}
				String[] sninfo = { Integer.toString(j), sn, snLem, snTag,
						snFun, papelsn };
				return sninfo;

			} else {
				String[] sninfo = { Integer.toString(j), "", "", "", "", "" };
				return sninfo;
			}
		}// end else

		return null;
	}

	/**
	 * @param texto
	 * @return
	 */
	public String getTreeFile(String texto, String visual, String convention) {
		// Para obter os sns pelo palavras use
		// http://beta.visl.sdu.dk/visl/pt/parsing/automatic/trees.php
		// veja tese da Lucelene Lopes 2012, PROBLEMAS COM ENUMERACAO, SE PERDE
		// NOMES SEPARADOS POR VIRG, virg são ignoradas e sns são agrupados em
		// um só
		// veja tese de agnaldo para extracao do texto etiquetado
		// Visualization: vertical
		// String url =
		// "http://beta.visl.sdu.dk/visl/pt/parsing/automatic/trees.php"
		// Notational convention: VLSI - Default
		/*
		 * <INPUT TYPE=HIDDEN NAME=parser VALUE=tree> Visualization: <SELECT
		 * NAME=visual> <OPTION VALUE=slant>Slant (applet) <OPTION
		 * VALUE=vertical>Vertical <OPTION VALUE=horizontal>Horizontal <OPTION
		 * VALUE=source>Source
		 * 
		 * <a NAME="include:/global/visuals-tree.inc"> </SELECT>
		 * 
		 * Notational convention <SELECT NAME=symbol> <OPTION VALUE="ultra"
		 * >VISL - ultralite <OPTION VALUE="simple" >VISL - lite <OPTION
		 * VALUE="default" SELECTED>VISL - default <OPTION VALUE="high" >VISL -
		 * extended <OPTION VALUE="cg" >CG-style
		 * 
		 * </SELECT>
		 */
		if (visual.isEmpty())
			visual = "vertical";
		if (convention.isEmpty())
			convention = "default";
		metainfTree = "&parser=tree&visual=" + visual + "&heads=&symbol="
				+ convention + "&multisearch=&searchtype=&inputlang=pt";
		try {
			ftext = "text=" + URLEncoder.encode(texto, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
		String metainf = ftext + metainfTree;
		String tagtext = new String();
		tagtext = getResponse(metainf);
		// System.out.println(tagtext); //imprime o arquivo recebido
		if (tagtext == null)
			return tagtext;
		return tagtext;
	}

	private String removelast(String sn) {
		sn = sn.trim();
		int index = sn.lastIndexOf(" ");
		if (index >= 0 && index < sn.length())
			return sn.substring(0, index);
		else
			return sn;
	}

	private int getNumSpaces(String s) {
		// ignore "|-   "
		String ruido = "|-   ";
		int indexl = s.indexOf(ruido);
		if (indexl >= 0) {
			s = s.replace(ruido, "     ");
		}
		int index = s.indexOf("|-");
		if (index < 0) {// || indexb < 0){
			return 0;
		}
		String temp = s.substring(0, index);
		temp = temp.replace("|", " ");
		int indexb = s.indexOf("|");

		// String temp = null;
		// if(indexb == 0 || getNumBars(s) >= 2){//raiz a esquerda
		// if(index == (indexb + 1))//obtendo espaço entre barras
		// return 0;
		// temp = s.substring(indexb+1, index-1);
		// }
		// else //raiz a direita
		// temp = s.substring(0, indexb); //obtendo espaço no início
		int count = 0;
		for (int i = 0; i < temp.length(); i++) {
			if (temp.charAt(i) == ' ')
				count++;
			else
				break;
		}
		return count;

	}

	private int getNumBars(String s) {
		// ignore "|-   "
		String ruido = "|-   ";
		int indexl = s.indexOf(ruido);
		if (indexl >= 0) {
			s = s.replace(ruido, "     ");
		}
		int index = s.indexOf("-");
		int count = 0;
		for (int i = 0; i < index; i++) {
			if (s.charAt(i) == '|')
				count++;
		}
		return count;
	}

	private int getNumEquals(String s) {
		int indexf = s.indexOf("=");
		int indexl = s.lastIndexOf("=");
		if (indexf < 0)
			return 0;
		int count = 0;
		for (int i = indexf; i <= indexl; i++) {
			if (s.charAt(i) == '=')
				count++;
		}
		return count;
	}

	private String getResponse(String metainf) {

		String urlstr = strurlbase + metainf;
		String str = null;
		URL urlbase = null;

		try {
			urlbase = new URL(urlstr);
		} catch (MalformedURLException e) {
			return null;
		}

		try {
			HttpURLConnection urlConn = (HttpURLConnection) urlbase
					.openConnection();

			// Se o documento esta acessivel...
			if (urlConn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				// Recupera o conteudo da URL.
				str = ((String) urlConn.getContent());
				// if(urlConn.getContentEncoding() == null)
				str = paraIso(str);
			}
		} catch (Exception e) {
			return str;
		}
		return str;
	}

	public static String paraIso(String string) {
		// String dfcharset =
		// java.nio.charset.Charset.defaultCharset().displayName();
		// System.out.println("=====>"+dfcharset);
		Charset charsetUtf8 = Charset.forName("ISO-8859-1");
		CharsetEncoder encoder = charsetUtf8.newEncoder();

		Charset charsetIso88591 = Charset.forName("UTF-8");
		CharsetDecoder decoder = charsetIso88591.newDecoder();
		String s = "";
		try {
			ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(string));

			CharBuffer cbuf = decoder.decode(bbuf);
			s = cbuf.toString();
		} catch (CharacterCodingException e) {

		}
		return s;
	}

	private String getWebPage(String strurl) {
		String content = null;
		try {

			URL url = new URL(strurl);
			URLConnection urlc = url.openConnection();

			BufferedInputStream buffer = new BufferedInputStream(
					urlc.getInputStream());
			// BufferedReader in = new BufferedReader(new
			// InputStreamReader(conn.getInputStream()));

			StringBuilder builder = new StringBuilder();

			// String data;
			// while ((data = in.readLine()) != null)
			// builder.append(data);//data sem \n
			int byteRead;
			while ((byteRead = buffer.read()) != -1)
				builder.append((char) byteRead);

			buffer.close();

			content = builder.toString();
			System.out.println(content);
			System.out.println("The size of the web page is "
					+ builder.length() + " bytes.");

		} catch (MalformedURLException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return content;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] res = new String[7];
		if (args.length >= 1) {
			Scanner scan = new Scanner(System.in);
			System.out.print("Digite o nome do arquivo: ");
			String args1 = scan.next();
			String filec = readFile(args1, StandardCharsets.ISO_8859_1);
			res[0] = filec;
			String s = JPalavras.getInstance().EtiquetarCVSFormat(res[0]);
			System.out.println(s);
			args1 = args1.replace(".txt", ".out.txt");
			
			try {
				writeFile(s, args1, StandardCharsets.ISO_8859_1);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.print("Texto etiquetado escrito no arquivo: "+args1);
			return;
		}
		String fr4 = new String(
				"O novo cálculo das aposentadorias resulta em valores menores do que os atuais para quem perde o benefício com menos tempo de contribuição e idade.");
		// res[0]=fr4;
		String res1 = new String(
				"Apresenta de forma introdutória questões e conceitos fundamentais sobre metadados e a estruturação da descrição padronizada de documentos eletrônicos. Discorre sobre os elementos propostos no Dublin Core e comenta os projetos de catalogação dos recursos da Internet, CATRIONA, InterCat e CALCO.");
		// res[1]=res1;
		String res2 = new String(
				"Bibliografia internacional seletiva e anotada sobre bibliotecas digitais. Aborda os seguintes aspectos: a) Visionários, principais autores que escreveram sobre a biblioteca do futuro, no período de 1945-1985; b) conceituação de biblioteca digital; c) projetos em andamento na Alemanha, Austrália, Brasil, Canadá, Dinamarca, Espanha, Estados Unidos, Franca, Holanda, Japão, Nova Zelândia, Reino Unido, Suécia e Vaticano; d) aspectos técnicos relativos a construção de uma biblioteca digital: arquitetura do sistema, conversão de dados e escaneamento, marcação de textos, desenvolvimento de coleções, catalogação, classificação/indexação, metadados, referencia, recuperação da informação, direitos autorais e preservação da informação digital; e) principais fontes de reuniões técnicas especificas, lista de discussão, grupos e centros de estudos, cursos e treinamento.");
		res[0] = res2;
		// res[0]=
		// "Os ambientes de Engenharia de Software que possuem um conjunto de ferramentas integradas utilizam técnicas de Inteligência Artificial.";
		// res[0]=
		// "Aspectos técnicos relativos a construção de uma biblioteca digital: arquitetura do sistema; conversão de dados e escaneamento; marcação de textos; desenvolvimento de coleções; catalogação; classificação/indexação; metadados; referencia; recuperação da informação; direitos autorais e preservação da informação digital.";
		res[0] = "A Inteligência Artificial sistematiza e automatiza tarefas intelectuais e, portanto, é potencialmente relevante para qualquer esfera da atividade intelectual humana. Softwares são produtos intangíveis e utilizam no seu processo de construção recursos intelectuais humanos, que vão desde sua especificação até sua distribuição e pleno funcionamento. Como meio de auxiliar o processo de Engenharia de Software, foram criados os ambientes de Engenharia de Software centrados no processo, que possuem um conjunto de ferramentas integradas. Baseado neste contexto, este artigo vem mostrar alguns ambientes existentes que utilizam técnicas de Inteligência Artificial e propor o uso de outras técnicas para melhorar os Ambientes de Engenharia de Software, trazendo uma maior facilidade de construção de softwares e uma maior qualidade para os mesmos.";
		// res[0]="Este artigo vem mostrar alguns ambientes existentes que utilizam técnicas de Inteligência Artificial e propor o uso de outras técnicas para melhorar os Ambientes de Engenharia de Software, trazendo uma maior facilidade de construção de softwares e uma maior qualidade para os mesmos.";
		// res[0]="Foram criados os ambientes de Engenharia de Software centrados no processo, que possuem um conjunto de ferramentas integradas.";
		// res[0]=
		// "e) principais fontes de reuniões técnicas especificas, lista de discussão grupos e centros de estudos cursos e treinamento.";
		String res3 = new String(
				"Apresenta a implantação de recursos multimídia e interface Web no banco de dados desenvolvido para a coleção de vídeos da Videoteca Multimeios, pertencente ao Departamento de Multimeios do Instituto de Artes da UNICAMP. Localiza a discussão conceitual no universo das bibliotecas digitais e propõe alterações na configuração atual de seu banco de dados.");
		// res[3]=res3;
		String res4 = new String(
				"Este artigo aborda a necessidade de adoção de padrões de descrição de recursos de informação eletrônica, particularmente, no âmbito da Embrapa Informática Agropecuária. O Rural Mídia foi desenvolvido utilizando o modelo Dublin Core (DC) para descrição de seu acervo, acrescido de pequenas adaptações introduzidas diante da necessidade de adequar-se a especificidades meramente institucionais. Este modelo de metadados baseado no Dublin Core, adaptado para o Banco de Imagem, possui características que endossam a sua adoção, como a simplicidade na descrição dos recursos, entendimento semântico universal (dos elementos), escopo internacional e extensibilidade (o que permite sua adaptação as necessidades adicionais de descrição).");
		// res[4] = res4;
		String res5 = new String(
				"Relato da experiência do Impa na informatização de sua biblioteca, utilizando o software Horizon, e na construção de um servidor de preprints (dissertações de mestrado, teses de doutorado e artigos ainda não publicados) através da participação no projeto internacional Math-Net.");
		// res[5]=res5;
		String res6 = new String(
				"Descreve as opções tecnológicas e metodológicas para atingir a interoperabilidade no acesso a recursos informacionais eletrônicos, disponíveis na Internet, no âmbito do projeto da Biblioteca Digital Brasileira em Ciência e Tecnologia, desenvolvido pelo Instituto Brasileiro de Informação em Ciência e Tecnologia(IBCT). Destaca o impacto da Internet sobre as formas de publicação e comunicação em C&T e sobre os sistemas de informação e bibliotecas. São explicitados os objetivos do projeto da BDB de fomentar mecanismos de publicação pela comunidade brasileira de C&T, de textos completos diretamente na Internet, sob a forma de teses, artigos de periódicos, trabalhos em congressos, literatura \"cinzenta\",ampliando sua visibilidade e acessibilidade nacional e internacional, e também de possibilitar a interoperabilidade entre estes recursos informacionais brasileiros em C&T, heterogêneos e distribuídos, através de acesso unificado via um portal, sem a necessidade de o usuário navegar e consultar cada recurso individualmente.");
		// res[6] = res6;
		// res[0]=
		// "São explicitados os objetivos do projeto da BDB de fomentar mecanismos de publicação pela comunidade brasileira de C&T, de textos completos diretamente na Internet, sob a forma de teses, artigos de periódicos, trabalhos em congressos, literatura \"cinzenta\",ampliando sua visibilidade e acessibilidade nacional e internacional, e também de possibilitar a interoperabilidade entre estes recursos informacionais brasileiros em C&T, heterogêneos e distribuídos, através de acesso unificado via um portal, sem a necessidade de o usuário navegar e consultar cada recurso individualmente.";

		// for(int i=0; i<res.length;i++){
		String s = JPalavras.getInstance().Etiquetar(res[0]);
		System.out.println(s);
		Vector<String> ress = new Vector<String> (JOgma.extraiSNTextoEtiquetado(s).keySet());
		System.out.println(ress.toString());
		//
		// }
		System.out.println(JPalavras.getInstance().getSNs(res[0]));
		// String s1 =
		// "O senhor Mário Moreno dos Santos faleceu ontem a tarde.";
		// System.out.println(JPalavras.getInstance().getSNs(s1));

		// Normalização de sintagmas
		// Agnaldo UFMG
		// a remoção dos quantificadores presentes no início de cada sintagma
		// String [] quantificadores = {"a", "o", "as", "os", "um", "uma",
		// "uns", "umas", "aquele","aquela", "aqueles", "aquelas", "este", "esta", "essa", "esse","essas", "esses"};
		// PTSteeming <https://code.google.com/p/ptstemmer/>
		// Morelato PUCRS
		// obter bigrams e trigrams dos sns, escolher os 10% a 20% mais frequentes no corpus
		// saida:term,lemma,head,nucleosem_tag,nucleopos_tag,nucleosint_tag,postagseq,nivelsn,nwords, tf
		// removeu artigos e pronomes
		// String [] pronomes = {"nosso","nossa","nossos","nossas","seu", "sua",
		// "seus","suas","meu", "meus","minha","minhas","deles", "delas","dele", "dela", "ele", "ela", "eles", "elas"};
		// removeu sns contendo numerais(três, 2000),simbolos(%), iniciando com pronome ou adverbio (mais,muito)
		// Foi considerado termo palavras isoladas identificados pelo PALAVRAS
		// como sujeito (S),objeto (Od, Oi, Op)
		// ou complementos de sujeito (Cs) e de objeto (Co).
		// Os sintagmas nominais são identificados com a etiqueta np pelo PALAVRAS
	}

	private static void writeFile(String s, String args1, Charset fENCODING) throws IOException{
		Writer out = null;
		out = new OutputStreamWriter(new FileOutputStream(args1), fENCODING);
	    out.write(s);
	    out.close();
		
	}

}
