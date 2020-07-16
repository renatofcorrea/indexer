<%@page import="br.ufpe.logic.analyzers.*"%>
<%@page import="java.util.*"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="java.math.RoundingMode"%>
<%@page import="org.apache.commons.math3.util.Precision" %>
<%@page import="javax.servlet.http.HttpServletResponse"%>
<%@page import="com.entopix.maui.core.*" %>
<%@page import="com.entopix.maui.utils.*" %>
<%@page import="com.entopix.maui.util.*" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
	<title>Etiquetador MAUI-PT</title>
	<script src="//ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js"></script>
	<style>
		body {
			background-color: #075E54;
		}
		h2 {
			font-family: arial;
			color: white;
		}
		p {
			font-family: arial;
			color: white;
		}
		table {
			color: white;
		}
		.center {
			margin: auto;
			width: 50%;
		}
		.inputarea {
			background-color: #128C7E;
			padding: 50px;
		}
		.whitebox {
			margin: auto;
			border: 10px solid teal;
			padding: 20px;
			background-color: #FFFFFF;
		}
	</style>
</head>
<body>
	<div class="inputarea center">
		<h2>Marca palavras e sintagmas nominais de um texto</h2>
    	<form action="Etiquetar" method="POST" >
    	<p> Digite ou cole o texto na �rea abaixo:</p>
    		<textarea id="textarea" name="texto"  rows="20" cols="80" maxlength="60000" placeholder="Insira o texto aqui..."></textarea>
      	<p>
        	<input type="submit" name="Submeter" value="Submeter texto" />
        	<input type="reset" name="Resetar" value="Resetar" />
      	</p>
    	</form>
	</div> <br/>
	<%
		String caminho = this.getServletContext().getRealPath("/WEB-INF");
	   	String texto = request.getParameter("texto");
	   	
	    Object obj = MauiFileUtils.deserializeObject(caminho + "/data/models/standard_model");
	    ModelWrapper model = null;
	    if (obj instanceof ModelWrapper) model = (ModelWrapper) obj;
	    else throw new Exception("Invalid model class");
	    MPTCore.setModel(model);
	    MPTCore.setVocabPath(caminho + "/data/vocabulary/TBCI-SKOS_pt.rdf");
		
	    String stopFile = caminho + "/res/sn_stoplist.txt";
	    if (texto == null || texto.trim().length() == 0) {
			%> <p>Digite e submeta algum texto para visualizar as marca��es.</p><br> <%
	 	} else { //else 1
      	
      	//INDEXING
      	texto = texto.replaceAll("[ \n\t\r]{2,}"," ");
      	String texto_etq = JTreeTagger.getInstance(caminho + "/res/TreeTagger/").etiquetar(texto);//get texto etiquetado
      	String[] palavras = texto_etq.split(" ");
  		String palavra = null;
  		String etq= "";
  		for (int i = 0; i < palavras.length; i++)//foreach (String palavra in palavras)
  		{
		  	palavra = palavras[i];
		  	String[] PC = palavra.split("/");
		  	//cores lawngreen (DET) gray(PL CJ) black(PN) blueviolet (NP) olivedrab (NC)
		  	if (PC.length > 1) {
		  		if (PC[1].equals("NP")||PC[1].equals("SU")) etq += " <font color=\"maroon\"><b>" + PC[0].trim()+"</b><sub><small>/"+PC[1].trim()+ "</small></sub></font>";
		  		else if (PC[1].equals("AJ")) etq += " <font color=\"red\"><b>" + PC[0].trim()+"</b><sub><small>/"+PC[1].trim()+ "</small></sub></font>";
		  		else if (PC[1].equals("AV")) etq += " <font color=\"darkorange\"><b>" + PC[0].trim()+"</b><sub><small>/"+PC[1].trim()+ "</small></sub></font>";
		  		else if (PC[1].equals("VP")) etq += " <font color=\"darkgreen\"><b>" + PC[0].trim()+"</b><sub><small>/"+PC[1].trim()+ "</small></sub></font>";
		  		else if (PC[1].equals("VB")) etq += " <font color=\"blue\"><b>" + PC[0].trim()+"</b><sub><small>/"+PC[1].trim()+ "</small></sub></font>";
		  		else etq+= " " + PC[0].trim()+"<sub><small>/"+PC[1].trim()+ "</small></sub>";
		  	}
  		}
      	
      	List<String> sns = SNAnalyser.extrairSintagmasNominais(new SNAnalyser(stopFile), texto); //SNS
      	String resultado = JOgma.identificaSNTextoEtiquetado(texto_etq);
      	//codigo abaixo adaptado de JOgma.extraiSNIdentificado()
      	palavras = resultado.split(" ");
  		palavra = null;
  		String SN="";
  		String marcados="";
  		for (int i = 0; i < palavras.length; i++)//foreach (String palavra in palavras)
  		{
		  	palavra = palavras[i];
		  	String[] PC = palavra.split("/");
		  	if (PC.length > 1) {
		  		if (PC[1].equals("SN")) {
		  			PC[0]=JOgma.substituiContracoes(PC[0]);
		  			SN = " "+PC[0].replace("_"," ").replace("+"," ").replace("="," ")+" ";
		  			marcados += " <font color=\"maroon\"><b><i>" + SN.trim()+ "</i></b></font>";
		  		} else marcados += " " + JOgma.substituiContracoes(PC[0]).replace("_"," ").replace("+"," ").replace("="," ") + " ";
		  	}
  		}
  		
  		//KEYWORD EXTRACTION
      	ArrayList<Topic> topics = MPTCore.runMauiWrapperOnString(texto);
  		ArrayList<Topic> snTopics = MPTCore.runMauiWrapperOnString(String.join("/n", sns)); //concatena sns
  	    List<String> candidates = null;
        Topic t = null;
        for (int i = 0; i < topics.size(); i++) {
        	t = topics.get(i);
        	topics.set(i, new Topic(t.getTitle(),t.getId(), Precision.round(t.getProbability(), 3)));
        }
        for (int i = 0; i < snTopics.size(); i++) {
        	t = snTopics.get(i);
        	snTopics.set(i, new Topic(t.getTitle(),t.getId(), Precision.round(t.getProbability(), 3)));
        }
        pageContext.setAttribute("topicsList", topics);
        pageContext.setAttribute("SNtopicsList", snTopics);
          
      	Collection<Candidate> c = model.getFilter().getCandidates(texto).values();
      	Iterator<Candidate> it = c.iterator();
      	candidates = new ArrayList<String>();
      	while (it.hasNext()) {
      		candidates.add(it.next().getTitle());
      	}
 %>
    <table class= "center" border="1">
	<thead>
		<tr>
			<th>Palavra-Chave</th>
			<th>Probabilidade</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${topicsList}" var="t">
			<tr>
				<td>${t.title}</td>
				<td>${t.probability}</td>
			</tr>
		</c:forEach>
	</tbody>
	</table>
	
	<table class= "center" border="1">
	<thead>
		<tr>
			<th>Palavra-Chave (SNS)</th>
			<th>Probabilidade</th>
		</tr>
	</thead>
	<tbody>
		<c:forEach items="${SNtopicsList}" var="v">
			<tr>
				<td>${v.title}</td>
				<td>${v.probability}</td>
			</tr>
		</c:forEach>
	</tbody>
	</table>
	
	<br/>
	
    <div class="whitebox">
    	<b>Candidatos do Vocabul�rio: </b> <%=candidates%><br/><br/>
	    <b>Texto etiquetado: </b> <%=etq%><br/><br/>
		<b>Texto com SNs marcados: </b> <%=marcados%><br/><br/> 
	    <b>Sintagmas nominais relevantes: </b> <%=sns%><br/><br/>
    </div>
    <a href="../esn">Tentar novamente?</a>
    
    <%
    }  //end of else 1
    %>
</body>
</html>