package br.ufpe.logic.analyzers;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.Analyzer.TokenStreamComponents;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
//import org.apache.lucene.analysis.tokenattributes.TermAttribute; //lucene 2.9.0
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;


public final class SNAnalyser extends Analyzer {
	private static HashSet<String> stopWords = null;

	Tokenizer tokenizador;
	
	public SNAnalyser(String stopFile) throws IOException{
		init(stopFile);
	}
	
	private static synchronized void carregarStopWords(HashSet stopWords){
		if(SNAnalyser.stopWords == null){
			SNAnalyser.stopWords = stopWords;
			//include puctuations as stopwords
			String punc = "-'`.!?;/\\\"[]<>{}&()";//remain , : ()
			for(int i=0;i<punc.length();i++){
				SNAnalyser.stopWords.add(punc.substring(i,i+1));
			}
		}
	}

	private static synchronized void init(String stopFile) throws IOException{
		//Load stopwords
		if(stopWords == null){
			stopWords = new HashSet<String>();
			BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(stopFile)));
			String line = input.readLine();
			while(line!=null){
				stopWords.add(line);
				line = input.readLine();
			}
			input.close();
			//include puctuations as stopwords
			String punc = "-'`.!?;/\\\"[]<>{}&()=+%";//remain , : ()
			for(int i=0;i<punc.length();i++){
				stopWords.add(punc.substring(i,i+1));
			}
		}
	}
	
	public void setReader(Reader input) throws IOException{
		tokenizador.setReader(input);
	}
	
	
	
	
//	@Override  //lucene 2.9.0
//	public TokenStream tokenStream(String fieldName, Reader reader) {
//		TokenStream stream = null;
//		stream = new SNTokenizerWithAtributes(reader, fieldName);//***
//		return stream;
//	}
	
	
//	@Override  //lucene 2.9.0
//	public TokenStream reusableTokenStream(String fieldName, Reader reader)
//			throws IOException {
//		Tokenizer tokenizer = (Tokenizer) getPreviousTokenStream();
//		if (tokenizer == null) {
//			tokenizer = new SNTokenizerWithAtributes(reader,"context");//***
//			setPreviousTokenStream(tokenizer);
//		} else
//			tokenizer.reset(reader);
//		return tokenizer;
//	}
	
	public static void displayTokens(Analyzer analyzer,
			String text) throws IOException {
			displayTokens(analyzer.tokenStream("contents", new StringReader(text)));
			}
	
	public static void displayTokens(TokenStream stream)
			throws IOException {
			//TermAttribute term = (TermAttribute) stream.addAttribute(TermAttribute.class); //lucene 2.9.0
		CharTermAttribute term = (CharTermAttribute) stream.addAttribute(CharTermAttribute.class);	
		while(stream.incrementToken()) {
			//System.out.print("[" + term.term() + "] "); //lucene 2.9.0
			System.out.print("[" + term.toString() + "] ");
			}
	}
	public static void displayTokensWithFullDetails(Analyzer analyzer,String text)
					throws IOException {
		StringReader rt= new StringReader(text);
		TokenStream stream = analyzer.tokenStream("contents",rt);
		stream.reset();
//		TermAttribute term = (TermAttribute) stream.addAttribute(TermAttribute.class);
		CharTermAttribute term = (CharTermAttribute) stream.addAttribute(CharTermAttribute.class);
		PositionIncrementAttribute posIncr = (PositionIncrementAttribute) stream.addAttribute(PositionIncrementAttribute.class);
		OffsetAttribute offset = (OffsetAttribute) stream.addAttribute(OffsetAttribute.class);
		TypeAttribute type = (TypeAttribute) stream.addAttribute(TypeAttribute.class);
		int position = 0;
		while(stream.incrementToken()) {
			int increment = posIncr.getPositionIncrement();
			if (increment > 0) {
				position = position + increment;
				System.out.println();
				System.out.print(position + ": ");
			}
			System.out.print("[" +
					//term.term() + ":" +
					term.toString() + ":" +
					offset.startOffset() + "->" +
					offset.endOffset() + ":" +
					type.type() + "] ");
		}
		System.out.println();
		stream.end();
		stream.close();
		
	}
	
	@Override
	protected TokenStreamComponents createComponents(String arg0, Reader arg1) {
		tokenizador = new SNTokenizerWithAtributes(arg1,arg0);
			return new TokenStreamComponents(tokenizador);
		
	}
	
	public static List<String> extrairSintagmasNominais(Analyzer analyzer,String text)
			throws Exception {
		//SNTokenizer.setTagger("Cogroo");
		SNTokenizer.setTagger("TreeTagger");
		List<String> palavras = new ArrayList<String>();
		StringReader rt= new StringReader(text);
		TokenStream stream = ((SNAnalyser)analyzer).tokenStream("contents",rt);
		try {
			stream.reset();
			CharTermAttribute term = (CharTermAttribute) stream.addAttribute(CharTermAttribute.class);
			while(stream.incrementToken()) {
				palavras.add(term.toString());
			}
			
		} catch (Exception e) {
			throw e;
		}finally{
			
			if(stream!= null){
				stream.end();
				stream.close();
				rt.close();
			}
		}
		
		
		return palavras;
	}
	
	public static void main(String[] args) throws IOException {
		String sent = "O novo c�lculo das aposentadorias resulta em valores menores do que os atuais para quem perde o benef�cio com menos tempo de contribui��o e idade.";
		sent="Foi realizado o estudo fenol�gico e das s�ndromes de poliniza��o e de dispers�o de 46 esp�cies de plantas.";
		String stopFile = "res/Ogma_stoplist.txt";
		//SNAnalyser contextAnalyzer = new SNAnalyser(stopFile);
		
		
		//set o Tagger a ser usado por Tokenizer, listados em ordem de melhor desempenho
		SNTokenizer.setTagger("TreeTagger");//macmorpho -> no/CJ peri�dicos/AJ heterog�neos/NP distribu�dos/VP cada/de  --- gamallo -> Web/VB  Biblioteca/SU Digital/AJ Brasileira/SU quebrado
//		SNTokenizer.setTagger("Cogroo");//ERROR FSADictionary:111, localiza/SU peri�dicos/AJ distribu�dos/VP cada/AJ
		//SNTokenizer.setTagger("MXPOST");//mestrado/AJ introdut�ria/VB interface/VB projetos/AJ publica��o/VB cada/AJ 
		
	
		//TODO:identificar nomes pr�prios compostos nas regras do edcer
		
		//SNTokenizer.setTagger("MAC-MORPHO80-20");//mxpost - informatiza��o/VB descri��o/VB configura��o/AD adequar-se/NP 1945-1985/NP s�o/SN tamb�m/SN
		//SNTokenizer.setTagger("LXTagger");//mxpost - da/VB Impa/VB interface/VB pertencente/AV adequar-se/SU dos/AJ ao/AD
		//SNTokenizer.setTagger("AeliusStanfordMM");//e/AJ atrav�s/AV introdut�ria/VB core/PREP,  ./NPROP ./SU desenvolvido/VP pelo/AV instituto/SU
		//Loading default properties from tagger taggers/AeliusStanfordMM
		//warning: no language set, no open-class tags specified, and no closed-class tags specified; assuming ALL tags are open class tags
		//SNTokenizer.setTagger("Ogma");//erro em NPs, deriva muito pesado para resumos, tratar sn dentro sn, atrav�s/SU, usu�rio/AJ
		
		String [] res = new String[11];
		//ids dos resumos 0 5 1 3 4 2 6
        res[0] = sent;
      
//        RESUMO 5: 
//        	CHATAIGNIER, Maria Cecilia Pragana, SILVA, Margareth Prevot. Biblioteca 
//        	digital: a experiencia do Impa. Ciencia da Informacao, Brasilia, v.30, 
//        	n.3, p.7-12, set./dez. 2001 
//        //Biblioteca Digital: a experi�ncia do Impa
        res[5] = new String("Relato da experi�ncia do Impa na informatiza��o de sua biblioteca, utilizando o software Horizon, e na constru��o de um servidor de preprints (disserta��es de mestrado, teses de doutorado e artigos ainda n�o publicados) atrav�s da participa��o no projeto internacional Math-Net.");
        //Palavras-chave: Biblioteca digital; Horizon; Impa; Automa��o; Preprint; Metadados; XML; Dublin core; Math-net; MPRESS; Harvest.
     
//        RESUMO 2: 
//        	SOUZA, Terezinha Batista, CATARINO, Maria Elisabete, SANTOS, Paulo Cesar 
//        	dos. Metadados: catalogando dados na Internet. Transinformacao, Campinas, 
//        	v. 9, n. 2, p. 93-105, maio/ago. 1997 
//        //METADADOS: CATALOGANDO DADOS NA INTERNET
        res[2]= new String("Apresenta de forma introdut�ria quest�es e conceitos fundamentais sobre metadados e a estrutura��o da descri��o padronizada de documentos eletr�nicos. Discorre sobre os elementos propostos no Dublin Core e comenta os projetos de cataloga��o dos recursos da Internet, CATRIONA, InterCat e CALCO.");
        //Palavras-chave: metadados. cataloga��o e classifica��o eletr�nica. dublin core. catriona. intercat. calco.
        
//        RESUMO 3: 
//        	FAGUNDES, Maria Lucia Figueiredo, PRADO, Gilberto dos Santos. Videoteca 
//        	digital : a experiencia da videoteca multimeios do IA/UNICAMP. 
//        	Transinformacao, Campinas, v.11, n.3, p. 293-299, set./dez. 1999 
//        //VIDEOTECA DIGITAL: A EXPERI�NCIA DA VIDEOTECA MULTIMEIOS DO IA/UNICAMP
        res[3] = new String("Apresenta a implanta��o de recursos multim�dia e interface Web no banco de dados desenvolvido para a cole��o de v�deos da Videoteca Multimeios, pertencente ao Departamento de Multimeios do Instituto de Artes da UNICAMP. Localiza a discuss�o conceitual no universo das bibliotecas digitais e prop�e altera��es na configura��o atual de seu banco de dados.");
        //Palavras-chave: biblioteca digital. Internet. metadados. sistemas multim�dia. sistemas de recupera��o da informa��o. videodigital. 

        //res[3]= " Quanto aos materiais nanoestruturados, suas propriedades podem ser controladas a partir de par�metros pr�estabelecidos como o tamanho e a fra��o volum�trica das nanopart�culas, e este controle os torna promissores para aplica��es nas mais diversas �reas da Nanofot�nica.";
//        RESUMO 4: 
//        	SOUZA, Marcia Isabel Fugisawa, VENDRUSCULO, Laurimar Goncalves, MELO, 
//        	Geane Cristina. Metadados para a descricao de recursos de informacao 
//        eletronica: utilizacao do padrao Dublin Core. Ciencia da Informacao, 
//        Brasilia, v. 29, n. 1, p. 93-102, jan./abr. 2000 
//        //Metadados para a descri��o de recursos de informa��o eletr�nica:utiliza��o do padr�o Dublin Core
        res[4] = new String("Este artigo aborda a necessidade de ado��o de padr�es de descri��o de recursos de informa��o eletr�nica, particularmente, no �mbito da Embrapa Inform�tica Agropecu�ria. O Rural M�dia foi desenvolvido utilizando o modelo Dublin Core (DC) para descri��o de seu acervo, acrescido de pequenas adapta��es introduzidas diante da necessidade de adequar-se a especificidades meramente institucionais. Este modelo de metadados baseado no Dublin Core, adaptado para o Banco de Imagem, possui caracter�sticas que endossam a sua ado��o, como a simplicidade na descri��o dos recursos, entendimento sem�ntico universal (dos elementos), escopo internacional e extensibilidade (o que permite sua adapta��o as necessidades adicionais de descri��o).");
        //Metadados; Dublin Core; Informa��o eletr�nica; Recursos de informa��o; Cataloga��o de recursos eletr�nicos
        
//        RESUMO 1: 
//        	CUNHA, Murilo Bastos da. Biblioteca digital: bibliografia 
//        	internacional anotada. Ciencia da Informacao, Brasilia, v.26, n.2, p.195-213, maio/ago. 1997 
//        //BIBLIOTECA DIGITAL: BIBLIOGRAFIA INTERNACIONAL ANOTADA
        res[1] = new String("Bibliografia internacional seletiva e anotada sobre bibliotecas digitais. Aborda os seguintes aspectos: a) Vision�rios, principais autores que escreveram sobre a biblioteca do futuro, no per�odo de 1945-1985; b) conceitua��o de biblioteca digital; c) projetos em andamento na Alemanha, Austr�lia, Brasil, Canad�, Dinamarca, Espanha, Estados Unidos, Franca, Holanda, Jap�o, Nova Zel�ndia, Reino Unido, Su�cia e Vaticano; d) aspectos t�cnicos relativos a constru��o de uma biblioteca digital: arquitetura do sistema, convers�o de dados e escaneamento, marca��o de textos, desenvolvimento de cole��es, cataloga��o, classifica��o/indexa��o, metadados, referencia, recupera��o da informa��o, direitos autorais e preserva��o da informa��o digital; e) principais fontes de reuni�es t�cnicas espec�ficas, lista de discuss�o, grupos e centros de estudos, cursos e treinamento.");
        //Palavras-chave: biblioteca digital. arquitetura de biblioteca digital. convers�o de dados. escaneamento. marca��o de textos. sgml. cataloga��o. desenvolvimento de cole��es. classifica��o. documento digital. metadados. recupera��o da informa��o. z3950. direito autoral. preserva��o da informa��o. protocolo de comunica��o z39.50.
        
//        RESUMO 6: 
//        	MARCONDES, Carlos Henrique, SAYAO, Luis Fernando. Integracao e 
//        	interoperabilidade no acesso a recursos informacionais eletr�nicos em 
//        	C&T: a proposta da Biblioteca Digital Brasileira.. Ciencia da Informacao, 
//        	Brasilia, v.30, n.3, p.24-33, set./dez. 2001 
        //Integra��o e interoperabilidade no acesso a recursos informacionais eletr�nicos em C&T: a proposta da Biblioteca Digital Brasileira
        res[6] = new String("Descreve as op��es tecnol�gicas e metodol�gicas para atingir a interoperabilidade no acesso a recursos informacionais eletr�nicos, dispon�veis na Internet, no �mbito do projeto da Biblioteca Digital Brasileira em Ci�ncia e Tecnologia, desenvolvido pelo Instituto Brasileiro de Informa��o em Ci�ncia e Tecnologia(IBCT). Destaca o impacto da Internet sobre as formas de publica��o e comunica��o em C&T e sobre os sistemas de informa��o e bibliotecas. S�o explicitados os objetivos do projeto da BDB de fomentar mecanismos de publica��o pela comunidade brasileira de C&T, de textos completos diretamente na Internet, sob a forma de teses, artigos de peri�dicos, trabalhos em congressos, literatura \"cinzenta\",ampliando sua visibilidade e acessibilidade nacional e internacional, e tamb�m de possibilitar a interoperabilidade entre estes recursos informacionais brasileiros em C&T, heterog�neos e distribu�dos, atrav�s de acesso unificado via um portal, sem a necessidade de o usu�rio navegar e consultar cada recurso individualmente.");
		//Palavras-chave: Bibliotecas digitais; Publica��es eletr�nicas; Arquivos abertos; Interoperabilidade; Metadados; Padr�es; Tecnologia da informa��o; Informa��o em ci�ncia e tecnologia; Comunica��o cient�fica; Acesso � informa��o.
        
        /* RESUMO 7: 
PACHECO, Roberto Carlos dos Santos, Kern, Vinicius Medina. Uma ontologia 
comum para a integracao de bases de informacoes e conhecimento sobre 
ciencia e tecnologia. Ciencia da Informacao, Brasilia, v.30, n.3, p.56-
63, set./dez. 2001 19
PALAVRAS CHAVES: ONTOLOGIA ; METADADOS ; LINGUAGENS DE MARCACAO ; INTEGRACAO DE SISTEMAS ; INFOMETRIA ; BIBLIOMETRIA ; BASES DE CONHECIMENTO ; GESTAO DO CONHECIMENTO
Palavras-chave: ontology. metadata. Systems integration. Informetry. bibliometry. Knowledgebases. knowledge management. ontologia. metadados. linguagens de marca��o. integra��o de sistemas. markup languages. informetria. bibliometria. base de conhecimento.  
 
         */
        // Os/AD documentos/NP e/CJ indicadores/AJ de/PR atividade/NP cient�fica/AJ vs  atividade/NP cient�fica/AJ requeridos/VP por/PR pesquisadores/NP
        res[7]=new String("A produ��o de documentos cient�ficos cresce em ritmo acelerado, da mesma forma que a demanda por busca, verifica��o, recupera��o e an�lise destes documentos. Esta demanda n�o pode ser atendida satisfatoriamente pelas ferramentas dispon�veis. Os documentos e indicadores de atividade cient�fica requeridos por pesquisadores, bibliotecas e outros agentes s� podem ser obtidos se houver a integra��o de sistemas de informa��es. Este artigo descreve uma iniciativa brasileira que potencializa a integra��o de sistemas de informa��es sobre ci�ncia e tecnologia: a Linguagem de Marca��o da Plataforma Lattes (LMPL), definida pelo consenso de peritos de v�rias institui��es de ensino superior. Apresenta-se o problema da integra��o de sistemas. Discute-se tamb�m a iniciativa de criar uma ontologia comum para a informa��o sobre ci�ncia e tecnologia. S�o aventadas possibilidades presentes e futuras para os sistemas de informa��es sobre ci�ncia e tecnologia a partir da disponibilidade da LMPL.");
        
         /* RESUMO 8: 
PAVANI, Ana M. B. A model of multilingual digital library. Ciencia da 
Informacao, Brasilia, v.30, n.3, p.73-81, set./dez. 2001 
PALAVRAS CHAVES: BIBLIOTECA DIGITAL MULTILINGUE ; METADADOS ; BASES DE DADOS MULTILINGUES 
Palavras-chave: Multilingual digital library. metadata. Multilingual database. biblioteca digital multil�ng�e. metadados. base de dados multil�ng�es  
         */
        //A/AD motivia��o/NP para/PR tal/de biblioteca/NP digital/AJ
        res[8]=new String("Este trabalho aborda o problema de bibliotecas digitais multil�ng�es. A motivia��o para tal biblioteca digital decorre da diversidade de l�nguas dos usu�rios da Internet, bem como da diversidade dos autores do conte�do, de autores de livros eletr�nicos para elaboradores de cursos. S�o discutidas as defini��es b�sicas de tal sistema, as especifica��es de sua funcionalidade e a identifica��o dos itens que ele comporta. Apresenta-se o impacto do multiling�ismo em cada um dos aspectos anteriores. Um estudo de caso de uma biblioteca digital multil�ng�e � no Sistema Maxwell, na PUC-Rio � � descrito nas �ltimas se��es. Suas principais caracter�sticas s�o descritas e � mostrado o status atual de sua biblioteca digital.");
        
        /* RESUMO 9
         * BAPTISTA, Ana Alice, MACHADO, Altamiro Barbosa. Um Gato preto num quarto escuro - falando sobre metadados. Revista de Biblioteconomia de 
Brasilia, Brasilia, v.25, n.1, p.77-90, jan/jun 2001 
PALAVRAS CHAVES: PUBLICACO EM LINHA ; METADADOS ; XML ; DUBLIN CORE ; RDF ; ESQUEMA RDF. 
Palavras-chave: metadata. xml. RDF Schema. Publica��o em linha. metadados. dublin core. rdf. Esquema RDF. Online publishing  
         */ 
        res[9]=new String("Metadados significa, basicamente, dados sobre os dados. Num ambiente ca�tico como a Internet, os dados j� n�o s�o suficientes: s�o precisos metadados para a descri��o sem�ntica dos recursos. No entanto, os metadados per se s�o tamb�m insuficientes. Como acontece noutras �reas e, como acontece tamb�m em rela��o � descri��o dos recursos f�sicos, a padroniza��o � um elemento chave na sua utiliza��o em grande escala. O Dublin Core (DC) e o Resource Description Framework (RDF) s�o duas recomenda��es de dois organismos diferentes: a DCMI (Dublin Core Metadata Initiative) e o W3C (World Wide Web Consortium). De forma a poder ser utilizado em larga escala, a DCMI optou por definir de forma ampla a sem�ntica do DC, deixando as quest�es ligadas � sintaxe abertas e indefinidas. Esta � a raz�o pela qual o DC e o RDF combinam t�o bem: o RDF traz as regras sint�cticas nas quais os DC pode ser embebido. Neste artigo, faremos uma descri��o geral do DC, do RDF e do RDF Schema, mostrando tamb�m alguns exemplos da duas aplica��o. Posteriormente ser�o retiradas algumas conclus�es sobre a sua aplicabilidade a falaremos sobre o seu futuro.");
         /* RESUMO 10: 
CUNHA, Murilo Bastos da. Desafios na construcao de uma biblioteca digital. Ciencia da Informacao, Brasilia, v. 28, n. 3, p. 255-266, 
set./dez. 1999 
PALAVRAS CHAVES: BIBLIOTECA DIGITAL ; UNIVERSITARIA ; DESENVOLVIMENTO DE COLECOES ; SERVICO DE AQUISICAO ; CATALOGACAO ; CLASSIFICACAO ; REFERENCIA ; COMUTACAO BIBLIOGRAFICA ; SERVICOS TECNICOS 
Palavras-chave: digital library. academic library. Collection development. Library acquisition. cataloging. classification. reference. Interlibrary loan. Technical services. preservation. User education. biblioteca digital. Biblioteca universit�ria.   
         *  
         */
        res[10]=new String("An�lise dos principais problemas que poder�o ocorrer nos diversos aspectos da biblioteca universit�ria durante e ap�s a implanta��o da biblioteca digital. Os aspectos estudados s�o instala��es f�sicas; aquisi��o, desenvolvimento de cole��es e comuta��o bibliogr�fica; cataloga��o, classifica��o e indexa��o; refer�ncia; preserva��o e tecnologia.");
        
	for(int i=0; i < res.length;i++){
//	int i = 0;
			StringReader sr = new StringReader(res[i]);
			displayTokensWithFullDetails(new SNAnalyser(stopFile),res[i]);
		}
		
		//contextAnalyzer.close();
		
		//Faz quase o mesmo da fun��o acima, imprimindo somente as tokens
//		TokenStream tokens = contextAnalyzer.tokenStream("context",reader);
////		TermAttribute termAtt = (TermAttribute) tokens.addAttribute(TermAttribute.class);
//		CharTermAttribute termAtt = (CharTermAttribute) tokens.addAttribute(CharTermAttribute.class);
//		tokens.reset();
//		// print all tokens until stream is exhausted
//		while (tokens.incrementToken()) {
////			System.out.println("token:"+termAtt.term());
//			System.out.println("token:"+termAtt.toString());
//		}
//		tokens.end();
//		tokens.close();

//				
	}
}
