<%@page import="br.ufpe.logic.analyzers.*"%>
<%@page import="java.util.List"%>  
<%@page import="javax.servlet.http.HttpServletResponse"%>

<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>ESN</title>
  </head>
  <body>
    <h1>Marca palavras e sintagmas nominais de um texto</h1>
    <form action="Etiquetar" method="POST" >
      <p> Digite ou cole o texto na área abaixo:<br/>
          
   <TEXTAREA name="texto"  rows="20" cols="80" maxlength="60000"></TEXTAREA>
            
      </p>
      <p> 
        <input type="submit" name="Submeter" value="Submeter texto" />
        <input type="reset" name="Resetar" value="Resetar" />
      </p>
    </form>
    
    <br/><br/>

    <%
    String caminho = this.getServletContext().getRealPath("/WEB-INF");
    String texto   = request.getParameter("texto");
    String stopFile = caminho + "/res/sn_stoplist.txt";
    if (texto == null || texto.trim().length() == 0) {
    %>
      Digite e submeta algum texto para visualizar as marcações.<br><br><br>
    <%
    } else {
    	texto = texto.replaceAll("[ \n\t\r]{2,}"," ");
    	String texto_etq = JTreeTagger.getInstance(caminho+"/res/TreeTagger/").etiquetar(texto);//get texto etiquetado
    	String[] palavras = texto_etq.split(" ");
		String palavra = null;
		String etq="";
		for(int i=0; i < palavras.length; i++)//foreach (String palavra in palavras)
		{
			palavra = palavras[i];
			String[] PC = palavra.split("/");
			//cores lawngreen (DET) gray(PL CJ) black(PN) blueviolet (NP) olivedrab (NC)
			if (PC.length>1)
				if (PC[1].equals("NP")||PC[1].equals("SU")) {
				etq += " <font color=\"maroon\"><b>" + PC[0].trim()+"</b><sub><small>/"+PC[1].trim()+ "</small></sub></font>";
			}else if (PC[1].equals("AJ")) {
				etq += " <font color=\"red\"><b>" + PC[0].trim()+"</b><sub><small>/"+PC[1].trim()+ "</small></sub></font>";
			}else if (PC[1].equals("AV")) {
				etq += " <font color=\"darkorange\"><b>" + PC[0].trim()+"</b><sub><small>/"+PC[1].trim()+ "</small></sub></font>";
			}else if (PC[1].equals("VP")) {
				etq += " <font color=\"darkgreen\"><b>" + PC[0].trim()+"</b><sub><small>/"+PC[1].trim()+ "</small></sub></font>";
			}else if (PC[1].equals("VB")) {
				etq += " <font color=\"blue\"><b>" + PC[0].trim()+"</b><sub><small>/"+PC[1].trim()+ "</small></sub></font>";
			}else
				etq+= " " + PC[0].trim()+"<sub><small>/"+PC[1].trim()+ "</small></sub>";;
		}
    	
    	List<String> sns = SNAnalyser.extrairSintagmasNominais(new SNAnalyser(stopFile),texto);
    	String resultado = JOgma.identificaSNTextoEtiquetado(texto_etq);
    	//codigo abaixo adaptado de JOgma.extraiSNIdentificado()
    	palavras = resultado.split(" ");
		palavra = null;
		String SN="";
		String marcados="";
		for(int i=0; i < palavras.length; i++)//foreach (String palavra in palavras)
		{
			palavra = palavras[i];
			String[] PC = palavra.split("/");
			if (PC.length>1)
				if (PC[1].equals("SN")) {
				PC[0]=JOgma.substituiContracoes(PC[0]);
				SN = " "+PC[0].replace("_"," ").replace("+"," ").replace("="," ")+" ";
				marcados += " <font color=\"maroon\"><b><i>" + SN.trim()+ "</i></b></font>";
			}else
				marcados += " " + JOgma.substituiContracoes(PC[0]).replace("_"," ").replace("+"," ").replace("="," ")+" ";
		}
    %>
      <b>Texto etiquetado: </b> <%=etq%><br/><br/>
      <b>Texto com SNs marcados: </b> <%=marcados%><br/><br/> 
      <b>Sintagmas nominais relevantes: </b> <%=sns%><br/><br/>
    <%
    }
    %>
    
    
    <a href="../esn">Tentar novamente?</a>
  </body>
</html>