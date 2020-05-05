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
		String sent = "O novo cálculo das aposentadorias resulta em valores menores do que os atuais para quem perde o benefício com menos tempo de contribuição e idade.";
		sent="Foi realizado o estudo fenológico e das síndromes de polinização e de dispersão de 46 espécies de plantas.";
		String stopFile = "res/Ogma_stoplist.txt";
		//SNAnalyser contextAnalyzer = new SNAnalyser(stopFile);
		
		
		//set o Tagger a ser usado por Tokenizer, listados em ordem de melhor desempenho
		SNTokenizer.setTagger("TreeTagger");//macmorpho -> no/CJ periódicos/AJ heterogêneos/NP distribuídos/VP cada/de  --- gamallo -> Web/VB  Biblioteca/SU Digital/AJ Brasileira/SU quebrado
//		SNTokenizer.setTagger("Cogroo");//ERROR FSADictionary:111, localiza/SU periódicos/AJ distribuídos/VP cada/AJ
		//SNTokenizer.setTagger("MXPOST");//mestrado/AJ introdutória/VB interface/VB projetos/AJ publicação/VB cada/AJ 
		
	
		//TODO:identificar nomes próprios compostos nas regras do edcer
		
		//SNTokenizer.setTagger("MAC-MORPHO80-20");//mxpost - informatização/VB descrição/VB configuração/AD adequar-se/NP 1945-1985/NP são/SN também/SN
		//SNTokenizer.setTagger("LXTagger");//mxpost - da/VB Impa/VB interface/VB pertencente/AV adequar-se/SU dos/AJ ao/AD
		//SNTokenizer.setTagger("AeliusStanfordMM");//e/AJ através/AV introdutória/VB core/PREP,  ./NPROP ./SU desenvolvido/VP pelo/AV instituto/SU
		//Loading default properties from tagger taggers/AeliusStanfordMM
		//warning: no language set, no open-class tags specified, and no closed-class tags specified; assuming ALL tags are open class tags
		//SNTokenizer.setTagger("Ogma");//erro em NPs, deriva muito pesado para resumos, tratar sn dentro sn, através/SU, usuário/AJ
		
		String [] res = new String[11];
		//ids dos resumos 0 5 1 3 4 2 6
        res[0] = sent;
      
//        RESUMO 5: 
//        	CHATAIGNIER, Maria Cecilia Pragana, SILVA, Margareth Prevot. Biblioteca 
//        	digital: a experiencia do Impa. Ciencia da Informacao, Brasilia, v.30, 
//        	n.3, p.7-12, set./dez. 2001 
//        //Biblioteca Digital: a experiência do Impa
        res[5] = new String("Relato da experiência do Impa na informatização de sua biblioteca, utilizando o software Horizon, e na construção de um servidor de preprints (dissertações de mestrado, teses de doutorado e artigos ainda não publicados) através da participação no projeto internacional Math-Net.");
        //Palavras-chave: Biblioteca digital; Horizon; Impa; Automação; Preprint; Metadados; XML; Dublin core; Math-net; MPRESS; Harvest.
     
//        RESUMO 2: 
//        	SOUZA, Terezinha Batista, CATARINO, Maria Elisabete, SANTOS, Paulo Cesar 
//        	dos. Metadados: catalogando dados na Internet. Transinformacao, Campinas, 
//        	v. 9, n. 2, p. 93-105, maio/ago. 1997 
//        //METADADOS: CATALOGANDO DADOS NA INTERNET
        res[2]= new String("Apresenta de forma introdutória questões e conceitos fundamentais sobre metadados e a estruturação da descrição padronizada de documentos eletrônicos. Discorre sobre os elementos propostos no Dublin Core e comenta os projetos de catalogação dos recursos da Internet, CATRIONA, InterCat e CALCO.");
        //Palavras-chave: metadados. catalogação e classificação eletrônica. dublin core. catriona. intercat. calco.
        
//        RESUMO 3: 
//        	FAGUNDES, Maria Lucia Figueiredo, PRADO, Gilberto dos Santos. Videoteca 
//        	digital : a experiencia da videoteca multimeios do IA/UNICAMP. 
//        	Transinformacao, Campinas, v.11, n.3, p. 293-299, set./dez. 1999 
//        //VIDEOTECA DIGITAL: A EXPERIÊNCIA DA VIDEOTECA MULTIMEIOS DO IA/UNICAMP
        res[3] = new String("Apresenta a implantação de recursos multimídia e interface Web no banco de dados desenvolvido para a coleção de vídeos da Videoteca Multimeios, pertencente ao Departamento de Multimeios do Instituto de Artes da UNICAMP. Localiza a discussão conceitual no universo das bibliotecas digitais e propõe alterações na configuração atual de seu banco de dados.");
        //Palavras-chave: biblioteca digital. Internet. metadados. sistemas multimídia. sistemas de recuperação da informação. videodigital. 

        //res[3]= " Quanto aos materiais nanoestruturados, suas propriedades podem ser controladas a partir de parâmetros préestabelecidos como o tamanho e a fração volumétrica das nanopartículas, e este controle os torna promissores para aplicações nas mais diversas áreas da Nanofotônica.";
//        RESUMO 4: 
//        	SOUZA, Marcia Isabel Fugisawa, VENDRUSCULO, Laurimar Goncalves, MELO, 
//        	Geane Cristina. Metadados para a descricao de recursos de informacao 
//        eletronica: utilizacao do padrao Dublin Core. Ciencia da Informacao, 
//        Brasilia, v. 29, n. 1, p. 93-102, jan./abr. 2000 
//        //Metadados para a descrição de recursos de informação eletrônica:utilização do padrão Dublin Core
        res[4] = new String("Este artigo aborda a necessidade de adoção de padrões de descrição de recursos de informação eletrônica, particularmente, no âmbito da Embrapa Informática Agropecuária. O Rural Mídia foi desenvolvido utilizando o modelo Dublin Core (DC) para descrição de seu acervo, acrescido de pequenas adaptações introduzidas diante da necessidade de adequar-se a especificidades meramente institucionais. Este modelo de metadados baseado no Dublin Core, adaptado para o Banco de Imagem, possui características que endossam a sua adoção, como a simplicidade na descrição dos recursos, entendimento semântico universal (dos elementos), escopo internacional e extensibilidade (o que permite sua adaptação as necessidades adicionais de descrição).");
        //Metadados; Dublin Core; Informação eletrônica; Recursos de informação; Catalogação de recursos eletrônicos
        
//        RESUMO 1: 
//        	CUNHA, Murilo Bastos da. Biblioteca digital: bibliografia 
//        	internacional anotada. Ciencia da Informacao, Brasilia, v.26, n.2, p.195-213, maio/ago. 1997 
//        //BIBLIOTECA DIGITAL: BIBLIOGRAFIA INTERNACIONAL ANOTADA
        res[1] = new String("Bibliografia internacional seletiva e anotada sobre bibliotecas digitais. Aborda os seguintes aspectos: a) Visionários, principais autores que escreveram sobre a biblioteca do futuro, no período de 1945-1985; b) conceituação de biblioteca digital; c) projetos em andamento na Alemanha, Austrália, Brasil, Canadá, Dinamarca, Espanha, Estados Unidos, Franca, Holanda, Japão, Nova Zelândia, Reino Unido, Suécia e Vaticano; d) aspectos técnicos relativos a construção de uma biblioteca digital: arquitetura do sistema, conversão de dados e escaneamento, marcação de textos, desenvolvimento de coleções, catalogação, classificação/indexação, metadados, referencia, recuperação da informação, direitos autorais e preservação da informação digital; e) principais fontes de reuniões técnicas específicas, lista de discussão, grupos e centros de estudos, cursos e treinamento.");
        //Palavras-chave: biblioteca digital. arquitetura de biblioteca digital. conversão de dados. escaneamento. marcação de textos. sgml. catalogação. desenvolvimento de coleções. classificação. documento digital. metadados. recuperação da informação. z3950. direito autoral. preservação da informação. protocolo de comunicação z39.50.
        
//        RESUMO 6: 
//        	MARCONDES, Carlos Henrique, SAYAO, Luis Fernando. Integracao e 
//        	interoperabilidade no acesso a recursos informacionais eletrônicos em 
//        	C&T: a proposta da Biblioteca Digital Brasileira.. Ciencia da Informacao, 
//        	Brasilia, v.30, n.3, p.24-33, set./dez. 2001 
        //Integração e interoperabilidade no acesso a recursos informacionais eletrônicos em C&T: a proposta da Biblioteca Digital Brasileira
        res[6] = new String("Descreve as opções tecnológicas e metodológicas para atingir a interoperabilidade no acesso a recursos informacionais eletrônicos, disponíveis na Internet, no âmbito do projeto da Biblioteca Digital Brasileira em Ciência e Tecnologia, desenvolvido pelo Instituto Brasileiro de Informação em Ciência e Tecnologia(IBCT). Destaca o impacto da Internet sobre as formas de publicação e comunicação em C&T e sobre os sistemas de informação e bibliotecas. São explicitados os objetivos do projeto da BDB de fomentar mecanismos de publicação pela comunidade brasileira de C&T, de textos completos diretamente na Internet, sob a forma de teses, artigos de periódicos, trabalhos em congressos, literatura \"cinzenta\",ampliando sua visibilidade e acessibilidade nacional e internacional, e também de possibilitar a interoperabilidade entre estes recursos informacionais brasileiros em C&T, heterogêneos e distribuídos, através de acesso unificado via um portal, sem a necessidade de o usuário navegar e consultar cada recurso individualmente.");
		//Palavras-chave: Bibliotecas digitais; Publicações eletrônicas; Arquivos abertos; Interoperabilidade; Metadados; Padrões; Tecnologia da informação; Informação em ciência e tecnologia; Comunicação científica; Acesso à informação.
        
        /* RESUMO 7: 
PACHECO, Roberto Carlos dos Santos, Kern, Vinicius Medina. Uma ontologia 
comum para a integracao de bases de informacoes e conhecimento sobre 
ciencia e tecnologia. Ciencia da Informacao, Brasilia, v.30, n.3, p.56-
63, set./dez. 2001 19
PALAVRAS CHAVES: ONTOLOGIA ; METADADOS ; LINGUAGENS DE MARCACAO ; INTEGRACAO DE SISTEMAS ; INFOMETRIA ; BIBLIOMETRIA ; BASES DE CONHECIMENTO ; GESTAO DO CONHECIMENTO
Palavras-chave: ontology. metadata. Systems integration. Informetry. bibliometry. Knowledgebases. knowledge management. ontologia. metadados. linguagens de marcação. integração de sistemas. markup languages. informetria. bibliometria. base de conhecimento.  
 
         */
        // Os/AD documentos/NP e/CJ indicadores/AJ de/PR atividade/NP científica/AJ vs  atividade/NP científica/AJ requeridos/VP por/PR pesquisadores/NP
        res[7]=new String("A produção de documentos científicos cresce em ritmo acelerado, da mesma forma que a demanda por busca, verificação, recuperação e análise destes documentos. Esta demanda não pode ser atendida satisfatoriamente pelas ferramentas disponíveis. Os documentos e indicadores de atividade científica requeridos por pesquisadores, bibliotecas e outros agentes só podem ser obtidos se houver a integração de sistemas de informações. Este artigo descreve uma iniciativa brasileira que potencializa a integração de sistemas de informações sobre ciência e tecnologia: a Linguagem de Marcação da Plataforma Lattes (LMPL), definida pelo consenso de peritos de várias instituições de ensino superior. Apresenta-se o problema da integração de sistemas. Discute-se também a iniciativa de criar uma ontologia comum para a informação sobre ciência e tecnologia. São aventadas possibilidades presentes e futuras para os sistemas de informações sobre ciência e tecnologia a partir da disponibilidade da LMPL.");
        
         /* RESUMO 8: 
PAVANI, Ana M. B. A model of multilingual digital library. Ciencia da 
Informacao, Brasilia, v.30, n.3, p.73-81, set./dez. 2001 
PALAVRAS CHAVES: BIBLIOTECA DIGITAL MULTILINGUE ; METADADOS ; BASES DE DADOS MULTILINGUES 
Palavras-chave: Multilingual digital library. metadata. Multilingual database. biblioteca digital multilíngüe. metadados. base de dados multilíngües  
         */
        //A/AD motiviação/NP para/PR tal/de biblioteca/NP digital/AJ
        res[8]=new String("Este trabalho aborda o problema de bibliotecas digitais multilíngües. A motiviação para tal biblioteca digital decorre da diversidade de línguas dos usuários da Internet, bem como da diversidade dos autores do conteúdo, de autores de livros eletrônicos para elaboradores de cursos. São discutidas as definições básicas de tal sistema, as especificações de sua funcionalidade e a identificação dos itens que ele comporta. Apresenta-se o impacto do multilingüismo em cada um dos aspectos anteriores. Um estudo de caso de uma biblioteca digital multilíngüe – no Sistema Maxwell, na PUC-Rio – é descrito nas últimas seções. Suas principais características são descritas e é mostrado o status atual de sua biblioteca digital.");
        
        /* RESUMO 9
         * BAPTISTA, Ana Alice, MACHADO, Altamiro Barbosa. Um Gato preto num quarto escuro - falando sobre metadados. Revista de Biblioteconomia de 
Brasilia, Brasilia, v.25, n.1, p.77-90, jan/jun 2001 
PALAVRAS CHAVES: PUBLICACO EM LINHA ; METADADOS ; XML ; DUBLIN CORE ; RDF ; ESQUEMA RDF. 
Palavras-chave: metadata. xml. RDF Schema. Publicação em linha. metadados. dublin core. rdf. Esquema RDF. Online publishing  
         */ 
        res[9]=new String("Metadados significa, basicamente, dados sobre os dados. Num ambiente caótico como a Internet, os dados já não são suficientes: são precisos metadados para a descrição semântica dos recursos. No entanto, os metadados per se são também insuficientes. Como acontece noutras áreas e, como acontece também em relação à descrição dos recursos físicos, a padronização é um elemento chave na sua utilização em grande escala. O Dublin Core (DC) e o Resource Description Framework (RDF) são duas recomendações de dois organismos diferentes: a DCMI (Dublin Core Metadata Initiative) e o W3C (World Wide Web Consortium). De forma a poder ser utilizado em larga escala, a DCMI optou por definir de forma ampla a semântica do DC, deixando as questões ligadas à sintaxe abertas e indefinidas. Esta é a razão pela qual o DC e o RDF combinam tão bem: o RDF traz as regras sintácticas nas quais os DC pode ser embebido. Neste artigo, faremos uma descrição geral do DC, do RDF e do RDF Schema, mostrando também alguns exemplos da duas aplicação. Posteriormente serão retiradas algumas conclusões sobre a sua aplicabilidade a falaremos sobre o seu futuro.");
         /* RESUMO 10: 
CUNHA, Murilo Bastos da. Desafios na construcao de uma biblioteca digital. Ciencia da Informacao, Brasilia, v. 28, n. 3, p. 255-266, 
set./dez. 1999 
PALAVRAS CHAVES: BIBLIOTECA DIGITAL ; UNIVERSITARIA ; DESENVOLVIMENTO DE COLECOES ; SERVICO DE AQUISICAO ; CATALOGACAO ; CLASSIFICACAO ; REFERENCIA ; COMUTACAO BIBLIOGRAFICA ; SERVICOS TECNICOS 
Palavras-chave: digital library. academic library. Collection development. Library acquisition. cataloging. classification. reference. Interlibrary loan. Technical services. preservation. User education. biblioteca digital. Biblioteca universitária.   
         *  
         */
        res[10]=new String("Análise dos principais problemas que poderão ocorrer nos diversos aspectos da biblioteca universitária durante e após a implantação da biblioteca digital. Os aspectos estudados são instalações físicas; aquisição, desenvolvimento de coleções e comutação bibliográfica; catalogação, classificação e indexação; referência; preservação e tecnologia.");
        
	for(int i=0; i < res.length;i++){
//	int i = 0;
			StringReader sr = new StringReader(res[i]);
			displayTokensWithFullDetails(new SNAnalyser(stopFile),res[i]);
		}
		
		//contextAnalyzer.close();
		
		//Faz quase o mesmo da função acima, imprimindo somente as tokens
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
