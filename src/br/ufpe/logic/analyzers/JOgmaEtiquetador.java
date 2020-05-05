package br.ufpe.logic.analyzers;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

public class JOgmaEtiquetador {
	private static JOgmaEtiquetador myInstance = null;
	private WordList gramatica = null;
	private WordList nomes = null;
	private WordList verbos = null;
	
	
	private JOgmaEtiquetador(){
		
		gramatica = new WordList("res/Ogma-GRAMATICA-sort.csv");
		nomes = new WordList("res/Ogma-NOMES-sort.csv");
		verbos = new WordList("res/Ogma-VERBOS-sort.csv");
		
	}
	public static JOgmaEtiquetador getInstance() {   
	      if (myInstance == null) {   
	    	  myInstance = new JOgmaEtiquetador();   
	      }   
	      return myInstance;   
	   } 
	
	 public String buscaPalavra(String palavra, String tabela,
				String oleDb) {
		    
			if(tabela.equals("Gramatica"))
				return gramatica.BuscaPalavra(palavra);
			else{
				if(tabela.equals("Nomes")){
					return nomes.BuscaPalavra(palavra);
				}
			else	return verbos.BuscaPalavra(palavra);
		}
	 }
	 
	//reduz plural para singular
	public static String removePlural(String Palavra)
		{
			String result = Palavra;
			if (Palavra.endsWith("ões"))
			{
				result = Palavra.substring((0), (0) + (Palavra.length() - 3)) + "ão";
			}
			else
			{
				if (Palavra.endsWith("s"))
				{
					result = Palavra.substring((0), (0) + (Palavra.length() - 1));
					if (Palavra.endsWith("ei"))
					{
						result = Palavra.substring((0), (0) + (Palavra.length() - 2)) + "il";
					}
					if (Palavra.endsWith("i"))
					{
						result = Palavra.substring((0), (0) + (Palavra.length() - 1)) + "l";
					}
					if (Palavra.endsWith("n"))
					{
						result = Palavra.substring((0), (0) + (Palavra.length() - 1)) + "m";
					}
				}
			}
			// feminino para masculino?
			//if (Palavra.endsWith("a"))
			//{
			//	result = Palavra.substring((0), (0) + (Palavra.length() - 1)) + "o";
			//}
			return result;
		}

}
