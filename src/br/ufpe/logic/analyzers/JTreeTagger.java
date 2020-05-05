package br.ufpe.logic.analyzers;

import java.io.IOException;
import java.util.Collections;

import org.annolab.tt4j.*;

import java.util.*;

public class JTreeTagger implements TaggerInterface {
	private static JTreeTagger myInstance = null;
	 private static String myModel = null;
	private static String[] models = {"pt.par:iso8859-1",//gamallo
		 "Trained80:iso8859-1"};//nilc mac-morpho 80%TRN 20%TST --- default
    
	private TreeTaggerWrapper<String> tt = null;
	private static String ttHome = "res/TreeTagger";
	private String textoEtiquetado = null;
	private String textoLemas = null;
	private String textoTokens=null;
	private String textoTags=null;

	public static JTreeTagger getInstance() {  
		int indexModel = 1; //default model mac-morpho
	      if (myInstance == null) {  
	    	  myModel = models[indexModel];
	    	  myInstance = new JTreeTagger(myModel);   
	      }   
	      return myInstance;   
	   }
	
	public static JTreeTagger getModelInstance(String model){
    	if (myInstance == null) {   
	    	  myInstance = new JTreeTagger(model);
	    	  myModel = model;
	    	}else if(!myModel.equals(model)) {
	    	  myInstance = new JTreeTagger(model);
	    	  myModel = model;
	    	}
	      return myInstance; 
    }
	
	public static JTreeTagger getInstance(String caminhotthome){
		JTreeTagger.ttHome = caminhotthome;
		System.setProperty("treetagger.home", ttHome);
    	if (myInstance == null) {   
	    	  myInstance = JTreeTagger.getInstance();
	    	}
	      return myInstance; 
    }
	
	
	private JTreeTagger(String model){
		
		tt = new TreeTaggerWrapper<String>();
		textoEtiquetado = new String();
		textoLemas = new String();
		textoTokens = new String();
		textoTags = new String();
		try {
		     //tt.setModel("english.par:iso8859-1");
			 //tt.setModel("pt.par:iso8859-1");
			//tt.setModel("Trained80:iso8859-1");
			tt.setModel(model);
		    tt.setHandler(new TokenHandler<String>() {
		         

				public void token(String token, String pos, String lemma) {
		        	 //textoEtiquetado = textoEtiquetado+ token+"/"+pos+" ";
		        	 textoEtiquetado = textoEtiquetado+ getOgmaFormat(token,pos,lemma)+" ";
		        	 textoTokens = textoTokens + token + "/";
		        	 textoLemas = textoLemas +lemma+ "/";
		        	 textoTags = textoTags +pos+ "/";
		         }
		     });
		 } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		 finally {
		     //tt.destroy();
		 }
	}
	
	 public static String[] getModels() {
			return models;
		}

	
	public String[] getTokens() {
		return textoTokens.split("/");
	}
	
	public String[] getTags() {
		return textoTags.split("/");
	}

	public static String getOgmaFormat(String token, String pos, String lemma){
		
		if(myModel.equals(models[1])){//nilc mac-morpho http://www.nilc.icmc.usp.br/macmorpho/macmorpho-manual.pdf
			if(pos.equalsIgnoreCase("N")||pos.startsWith("N|")|| pos.equalsIgnoreCase("NPROP")){
//    			return token+"/NP*";
//    		}else if(){
    			if("de do da dos das".contains(token.toLowerCase())){
    				int i = token.indexOf("o");
    				int j = token.indexOf("a");
    				if(i >=0)
    					return "de/PR "+token.substring(i)+"/AD";	
    				else if(j>=0)
    					return "de/PR "+token.substring(j)+"/AD";
    				else if (token.indexOf("e") >=0)
    					return token.toLowerCase()+"/PR";
    				else
    					return token+"/NP";
    			}else if (token.equalsIgnoreCase("em"))
					return token.toLowerCase()+"/PR";
    			else if("no na nos nas".contains(token.toLowerCase())){
    				int i = token.indexOf("o");
    				int j = token.indexOf("a");
    				if(i >=0)
    					return "em/PR "+token.substring(i)+"/AD";	
    				else if(j>=0)
    					return "em/PR "+token.substring(j)+"/AD";
    				else
    					return token+"/NP";
    			}else if("pelo pela pelos pelas".contains(token.toLowerCase()))
    					return token+"/PR";
    			else if("à às ao aos".contains(token.toLowerCase()))
					return token+"/PR";
    			else
    			return token+"/NP";
		}else if(".,:;!?()[]{}\"".contains(pos)){
			return token+"/PN";
		}else if(pos.startsWith("V")||pos.startsWith("VAUX")){
				return token+"/VB";
		}else if(pos.equalsIgnoreCase("PCP")){
//			if(token.endsWith("ida")||token.endsWith("ido")||token.endsWith("ada")||token.endsWith("ado")||token.endsWith("idas")||token.endsWith("idos")||token.endsWith("adas")||token.endsWith("ados"))
//			return token+"/VP"; //contempla VP - verbo participio
//			else				
			return token+"/VP";
		}else if(pos.startsWith("ADJ")){
			return token+"/AJ";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
		}else if(pos.equalsIgnoreCase("PROADJ")){
			return token+"/de";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
		}else if(pos.equalsIgnoreCase("PROSUB")){//PRONOME SUBSTANTIVO
			return token+"/PI";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
		}else if(pos.equalsIgnoreCase("PROPESS")){
			return token+"/PP";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
		}else if(pos.equalsIgnoreCase("PRO-KS")  || pos.equalsIgnoreCase("PRO-KS-REL")){
			//PRONOME CONECTIVO SUBORDINATIVO (PRO-KS), PRONOME CONECTIVO SUBORDIN. RELATIVO (PRO-KS-REL)
			return token+"/PL";
		}else if(pos.equalsIgnoreCase("PDEN")){
			return token+"/ct";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
		}else if(pos.startsWith("ADV")){
			return token+"/AV";
		}else if(pos.startsWith("KC")|| pos.startsWith("KS")){//CONJUNÇÃO SUBORDINATIVA (KS) //CONJUNÇÃO COORDENATIVA (KC) || pos.equalsIgnoreCase("KS")
			if("e em".contains(token.toLowerCase()))
				return token+"/CJ";
			else if("no na nos nas do da dos das".contains(token.toLowerCase())){
				int i = token.indexOf("o");
				int j = token.indexOf("a");
				if(token.indexOf("n") == 0){
				if(i >=0)
					return "em/PR "+token.substring(i)+"/AD";	
				else if(j>=0)
					return "em/PR "+token.substring(j)+"/AD";
				else
					return token+"/CJ";
				}else {//if(token.indexOf("d") == 0){
					if(i >=0)
						return "de/PR "+token.substring(i)+"/AD";	
					else if(j>=0)
						return "de/PR "+token.substring(j)+"/AD";
					else
						return token+"/CJ";
				} 
			}else if("pelo pela pelos pelas".contains(token.toLowerCase()))
				return token+"/PR";
			else
				return token+"/ct";
		}else if(pos.startsWith("PREP")){
			if(pos.equals("PREP|+")){
				if(lemma.contains("@card"))
					return token + "/NC";
				else
					return token + "/PR";
			}
			else if(pos.endsWith("+DET")){
				int i = token.indexOf("o");
				int j = token.indexOf("a");
				if(i >=0)
					return lemma.toLowerCase()+"/PR "+token.substring(i)+"/AD";	
				else
					return lemma.toLowerCase()+"/PR "+token.substring(j)+"/AD";
			}else  if((token.startsWith("no") || token.startsWith("na")) && token.length() >= 2 && token.length()<=3){
				int i = token.indexOf("o");
				int j = token.indexOf("a");
				if(i >=0)
					return "em/PR "+token.substring(i)+"/AD";	
				else if(j>=0)
					return "em/PR "+token.substring(j)+"/AD";
				else
					return token+"/PR";
			}else
				return token+"/PR";
		}else if(pos.equalsIgnoreCase("ART")){
			if(token.startsWith("um")||token.startsWith("Um")) 
				return token+"/AI";
			else 
				return token +"/AD";
		}else if(pos.equalsIgnoreCase("CARD")||pos.equalsIgnoreCase("NUM")||pos.equalsIgnoreCase("SENT")||pos.equalsIgnoreCase("CUR")){
			return token+"/NC";
		}else{
			System.out.println("NAO CONTEMPLADA: "+token + " "+pos + " "+lemma);
			return token+"/NR";
		}
		}else if(myModel.equals(models[0])){//gamallo
			if(pos.equalsIgnoreCase("NOM")){
				return token+"/SU";
			}else if(pos.equalsIgnoreCase("ADJ")){//pronomes possessivos como adjetivo
				return token+"/AJ";
			}else if(pos.equalsIgnoreCase("QUOTE") || pos.equalsIgnoreCase("VIRG")|| pos.equalsIgnoreCase("SENT")){
				return token+"/PN";
			}else if(pos.startsWith("V")){
				if(pos.equalsIgnoreCase("V")){
					if(token.endsWith("ida")||token.endsWith("ido")||token.endsWith("ada")||token.endsWith("ado")||token.endsWith("idas")||token.endsWith("idos")||token.endsWith("adas")||token.endsWith("ados"))
					return token+"/VP"; //contempla VP - verbo participio
					else
					return token+"/VB";
				}else{//V+P
					return token+"/VB";				
				}
			}else if(pos.equalsIgnoreCase("P")){
				return token+"/de";//PD,PI,PP, (PS) (PS em geral categorizado como adjetivo)
			}else if(pos.equalsIgnoreCase("PR") || pos.equalsIgnoreCase("CONJSUB")){
				return token+"/PL";
			}else if(pos.equalsIgnoreCase("ADV")){
				return token+"/AV";
			}else if(pos.equalsIgnoreCase("CONJ")){
				return token+"/CJ";
			}else if(pos.startsWith("PRP")){
				if(pos.endsWith("+DET")){
					int i = token.indexOf("o");
					int j = token.indexOf("a");
					if(i >=0)
						return lemma.toLowerCase()+"/PR "+token.substring(i)+"/AD";	
					else
						return lemma.toLowerCase()+"/PR "+token.substring(j)+"/AD";
				}else
					return token+"/PR";
			}else if(pos.equalsIgnoreCase("DET")){
				if(lemma.equalsIgnoreCase("a") || lemma.equalsIgnoreCase("o"))
					return token+"/AD";
				else if(lemma.equalsIgnoreCase("um"))
					return token+"/AI";
				else
					return token +"/de";
			}else if(pos.equalsIgnoreCase("CARD")){
				return token+"/NC";
			}else{
				System.out.println("NAO CONTEMPLADA: "+token + " "+pos + " "+lemma);
				return token+"/NR";
			}
		}else{
			return null;
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
	
	/* (non-Javadoc)
	 * @see TaggerInterface#etiquetar(java.lang.String)
	 */
	@Override
	public String etiquetar(String texto){
		textoEtiquetado = new String();
		textoLemas = new String();
		texto = formatText(texto);
		
		String [] words = texto.split(" ");
		
		try{
			tt.process(Arrays.asList(words));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TreeTaggerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		 finally {
		     //tt.destroy();
		 }
		return this.textoEtiquetado;
	}
	
	public void destroy(){
		tt.destroy();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*		 Point TT4J to the TreeTagger installation directory. The executable is expected
		 // in the "bin" subdirectory - in this example at "/opt/treetagger/bin/tree-tagger"
		 System.setProperty("treetagger.home", "c:\\TreeTagger");
		 TreeTaggerWrapper tt = new TreeTaggerWrapper<String>();
		 try {
		     //tt.setModel("english.par:iso8859-1");
			 tt.setModel("pt.par:iso8859-1");
		     tt.setHandler(new TokenHandler<String>() {
		         public void token(String token, String pos, String lemma) {
		        	 int count = 1;
		             System.out.println(token+"\t"+pos+"\t"+lemma);
		         }
		     });
		     //tt.process(Arrays.asList(new String[] {"This", "is", "a", "test", "."}));
		     tt.process(Arrays.asList(new String[] {"Isto", "é", "um", "teste", "."}));
		 } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TreeTaggerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 finally {
		     tt.destroy();
		 }*/
		JTreeTagger jt = JTreeTagger.getModelInstance(JTreeTagger.getModels()[1]);
		System.out.println(jt.etiquetar("Isto é um teste."));
		String fr1= new String("Eu comi minha maçã hoje.");
		String fr2= new String("Meu carro quebrou.");
		String sent = new String("O novo cálculo das aposentadorias resulta em valores menores do que os atuais para quem perde o benefício com menos tempo de contribuição e idade.");
		System.out.println(JTreeTagger.getInstance().etiquetar(fr1));
		System.out.println(JTreeTagger.getInstance().etiquetar(fr2));
		
		String [] res = new String[7];
        res[0] =  "No período de abril de 2005 a novembro de 2006 foram estudadas a distribuição temporal, a partilha do habitat, a reprodução e a atividade vocal em uma assembleia de anfíbios anuros na Fazenda Serra da Esperança, município de Lebon Régis, Estado de Santa Catarina. Os objetivos do trabalho foram verificar a importância da pluviosidade e da temperatura na distribuição temporal das espécies na assembleia, analisar a ocupação do habitat, realizar a análise acústica do repertório vocal das espécies e testar a influência da temperatura do ar e do tamanho e massa dos machos vocalizantes sobre os parâmetros acústicos. Foram encontradas 32 espécies na área de estudo, a maior riqueza de anfíbios registrada para o Estado. A taxonomia de pelo menos sete dessas espécies é incerta, podendo tratar-se de táxons ainda não descritos na literatura. A temperatura apresentou uma forte influência na distribuição temporal das espécies. O número de espécies em atividade de vocalização e reprodução foi relacionado às variações da temperatura mensal média, mínima e máxima, significando que nos meses mais quentes foram encontradas mais espécies em atividade de vocalização e reprodução. Foi documentada atividade reprodutiva em 14 espécies e um total de nove modos reprodutivos na assembleia. A comparação das vocalizações de 23 espécies da assembleia com descrições de vocalizações de outras assembleias indicou diferenças que sugerem a existência de espécies ainda não descritas na área de estudo. Também foram documentadas variações intraespecíficas nos cantos em decorrência do tamanho e massa dos machos cantores e em função da temperatura do ar. Encontraram-se influências da massa e tamanho do macho cantor na frequência dominante do canto de anúncio, e também da temperatura do ar na duração das notas. A riqueza de espécies da assembleia apresentou forte semelhança biogeográfica com áreas de Floresta Ombrófila Mista dos Estados de Santa Catarina, Paraná e Rio Grande do Sul. A presença de possíveis novas espécies e da espécie Pleurodema bibroni, classificada na categoria quase ameaçada, salienta a importância da conservação deste bioma altamente degradado e demonstra a nossa carência de conhecimento acerca da anurofauna catarinense.";
        res[1]= new String("Apresenta de forma introdutória questões e conceitos fundamentais sobre metadados e a estruturação da descrição padronizada de documentos eletrônicos. Discorre sobre os elementos propostos no Dublin Core e comenta os projetos de catalogação dos recursos da Internet, CATRIONA, InterCat e CALCO.");
		res[2] = new String("Bibliografia internacional seletiva e anotada sobre bibliotecas digitais. Aborda os seguintes aspectos: a) Visionários, principais autores que escreveram sobre a biblioteca do futuro, no período de 1945-1985; b) conceituação de biblioteca digital; c) projetos em andamento na Alemanha, Austrália, Brasil, Canadá, Dinamarca, Espanha, Estados Unidos, Franca, Holanda, Japão, Nova Zelândia, Reino Unido, Suécia e Vaticano; d) aspectos técnicos relativos a construção de uma biblioteca digital: arquitetura do sistema, conversão de dados e escaneamento, marcação de textos, desenvolvimento de coleções, catalogação, classificação/indexação, metadados, referencia, recuperação da informação, direitos autorais e preservação da informação digital; e) principais fontes de reuniões técnicas especificas, lista de discussão, grupos e centros de estudos, cursos e treinamento.");
		res[3] = new String("Apresenta a implantação de recursos multimídia e interface Web no banco de dados desenvolvido para a coleção de vídeos da Videoteca Multimeios, pertencente ao Departamento de Multimeios do Instituto de Artes da UNICAMP. Localiza a discussão conceitual no universo das bibliotecas digitais e propõe alterações na configuração atual de seu banco de dados.");
		res[4] = new String("Este artigo aborda a necessidade de adoção de padrões de descrição de recursos de informação eletrônica, particularmente, no âmbito da Embrapa Informática Agropecuária. O Rural Mídia foi desenvolvido utilizando o modelo Dublin Core (DC) para descrição de seu acervo, acrescido de pequenas adaptações introduzidas diante da necessidade de adequar-se a especificidades meramente institucionais. Este modelo de metadados baseado no Dublin Core, adaptado para o Banco de Imagem, possui características que endossam a sua adoção, como a simplicidade na descrição dos recursos, entendimento semântico universal (dos elementos), escopo internacional e extensibilidade (o que permite sua adaptação as necessidades adicionais de descrição).");
		res[5] = new String("Relato da experiência do Impa na informatização de sua biblioteca, utilizando o software Horizon, e na construção de um servidor de preprints (dissertações de mestrado, teses de doutorado e artigos ainda não publicados) através da participação no projeto internacional Math-Net.");
		res[6] = new String("Descreve as opções tecnológicas e metodológicas para atingir a interoperabilidade no acesso a recursos informacionais eletrônicos, disponíveis na Internet, no âmbito do projeto da Biblioteca Digital Brasileira em Ciência e Tecnologia, desenvolvido pelo Instituto Brasileiro de Informação em Ciência e Tecnologia(IBCT). Destaca o impacto da Internet sobre as formas de publicação e comunicação em C&T e sobre os sistemas de informação e bibliotecas. São explicitados os objetivos do projeto da BDB de fomentar mecanismos de publicação pela comunidade brasileira de C&T, de textos completos diretamente na Internet, sob a forma de teses, artigos de periódicos, trabalhos em congressos, literatura \"cinzenta\",ampliando sua visibilidade e acessibilidade nacional e internacional, e também de possibilitar a interoperabilidade entre estes recursos informacionais brasileiros em C&T, heterogêneos e distribuídos, através de acesso unificado via um portal, sem a necessidade de o usuário navegar e consultar cada recurso individualmente.");
		for(int i=0; i < res.length;i++){
			System.out.println(JTreeTagger.getInstance().etiquetar(res[i]));
		}
		
		//pela/NP
		
		String result = JTreeTagger.getInstance().etiquetar(res[0]);//6-4-3 //BANCO DE DADOS não é capturado x2, ok para idcat2
		System.out.println(result);
		System.out.println(JOgma.extraiSNTextoEtiquetado(result).toString());//ogma força a barra substituindo "do que" por "que"
		
	}

	/* (non-Javadoc)
	 * @see TaggerInterface#getLemmas()
	 */
	@Override
	public String[] getLemmas() {
		return textoLemas.split("/");
	}

	/* Return name of tagger
	 * @see TaggerInterface#getLemmas()
	 */
	@Override
	public String getName() {
		
		return "TreeTagger";
	}

}
