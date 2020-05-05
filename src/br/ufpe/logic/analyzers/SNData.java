package br.ufpe.logic.analyzers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;



//classificar sns quanto ao nível, ok
//pontuar sns, ok
//ordenar sndata de acordo com critérios definidos (alfabetico,posicao inicial, nivel, pontuacao),ok
//a melhorar regras de extração de sn, dos problemas abaixo:
//a/AD participação/SU em/PR o/AD projeto/VBSU internacional/AJ math-net/SU, ok
//A participação no projeto internacional math-net. partido! NÃO MAIS,ok
//remover regra ou casamento de regra de sn contendo virgula, ok
//como/VBAV a/AD simplicidade/SU em/PR a/AD descrição/SU de/PR os/ADPR recursos/SU SN? Não Regra add, ok.
//substituir " por espaço antes da etiquetação, em tokenize, ok

//tornar extraiSNTextoEtiquetado operacional para todos os etiquetadores
//Nomes Próprios: 
//melhorar regras de extração de nomes próprios, capitalizados dentro de sn máximos, remover sns parciais dos nomes
//tratar diferente NP ou guardar em atributo type que se trata de nome próprio
//Normalização: 
//lemma ou radicalização, remoção de stopwords de sn
//filtro para remover determinantes, pronomes, adv  e regras de lopes
//quebrar sn na conjunção e (exceto nome próprio) ou sn e sn sprep ou sn e sn (com adj final)

//sns que precisam ser processados são os que são grandes(num palavras,num chars) com nivel baixo
//hipotese,melhores descritores estão dentro de sns máximos
//hipótese, os melhores sns estão no final dos sns máximos, nomes próprios (=),poucos casos no inicio e ambos

//remocao do espaço antes do sinal de pontuação
//adicionar contração a o

//frequencia de disparo das regras
//ordem de regras disparadas para construção de SN

//sri -> autocomplete (search, suggestions), recursos did you mean, spell checker, highlight

public class SNData implements Comparable<SNData>, Comparator<SNData> {
	protected String sn;
    //protected int indiceInicio;//implementado via funções
    //protected int tf;//implementado via funções
    protected int nivel=0;
    protected String snnorm=null;
    protected Vector<Integer> pos=null;//implementado como fila sem repetições (não ordenado)
    
    static final Comparator<SNData> RANK_ORDER = new Comparator<SNData>() {
    	public int compare(SNData e1, SNData e2) {
    		return (int) Math.signum(e1.getRank() - e2.getRank())*-1;//decrescente
    	}
    };
    
    static final Comparator<SNData> LEVEL_ORDER = new Comparator<SNData>() {
    	public int compare(SNData e1, SNData e2) {
    		return (int) Math.signum(e1.getNivel() - e2.getNivel())*-1;//decrescente
    	}
    };
    
    static final Comparator<SNData> ALPHABETIC_ORDER = new Comparator<SNData>() {
    	public int compare(SNData e1, SNData e2) {
    		return e1.getSN().compareToIgnoreCase(e2.getSN());
    	}
    };
    
    static final Comparator<SNData> POSITION_ORDER = new Comparator<SNData>() {
    	public int compare(SNData e1, SNData e2) {
    		return e1.compareTo(e2);//usando função abaixo
    	}
    };
    
    @Override
	public int compareTo(SNData o) {
		//levando em conta posição
		int ordi = this.getIndiceInicio() - o.getIndiceInicio();
		if(ordi == 0 && !this.sn.equalsIgnoreCase(o.sn)){
			ordi = this.sn.length() - o.sn.length();
		    ordi /= Math.abs(ordi);
		}
		return ordi;
	}
	
	@Override
	public int compare(SNData o1, SNData o2) {
		// TODO Auto-generated method stub
		return o1.compareTo(o2);//usando função acima
	}
	
    
    SNData() {
        this.sn = new String();
        this.pos = new Vector<Integer>();
    }

	SNData(String s, int i) {
		    
	        this.sn = s;
	        //this.indiceInicio = i;
	        this.pos = new Vector<Integer>();
	        this.pos.add(new Integer(i));
	}
	    
	SNData(Map.Entry<String, Integer> e) {
	        this.sn = e.getKey();
	        //this.indiceInicio = e.getValue();
	        this.pos = new Vector<Integer>();
	        this.pos.add(e.getValue());
	}

    
    
	public String getSN() {
		return sn;
	}

	public void setSN(String sn) {
		this.sn = sn;
	}

	public int getIndiceInicio() {
		//return indiceInicio;
		if(pos.size() >=1)
		return this.pos.get(0).intValue();
		else
			return -1;
	}
	
	public int getIndiceInicio(int posesq) {
		//return indiceInicio;
		if(pos.size() ==1)
		return this.pos.get(0).intValue();
		else if(pos.size() > 1){
			posesq = (posesq >= 0)?posesq:0;
			return this.pos.get(posesq).intValue();
		}
		//else
		return -1;
		
	}
	

	public void setIndiceInicio(int indiceInicio) {
		//this.indiceInicio = indiceInicio;
		this.addPosition(indiceInicio);
	}
	
	public float getRank(){
		//				1a(0) 1b(1)  2    3    4  5+ recebe pontuação de 1a
		float[] pesos = {0.4f,1.4f,1.2f,1.1f,0.8f};
		int nivel = getNivel();
		int indice = (nivel>=0 && nivel<=4)?nivel:0;
		return getFreq() * pesos[indice];
	}
	
	public Vector<Integer> getPositions(){
		return pos;
	}
	
	public boolean addPosition(int indice){
		Integer e = new Integer(indice);
		if(pos.contains(e))
			return false;
		else
		return pos.add(e);
	}
			
		//equals is used in most collections to determine if a collection contains a given element.
		//The equals() method is also used when removing elements
		@Override
		public boolean equals(Object o){
			if(o == null)                return false;
		    if(!(o instanceof SNData)) return false;
			SNData ox = (SNData) o;
			//return (Math.abs(this.indiceInicio - ox.indiceInicio) < Math.max(20, this.sn.length())) && this.sn.equalsIgnoreCase(ox.sn);
			return this.sn.equalsIgnoreCase(ox.sn);
		}
		
		//The hashCode() method of objects is used when you insert them into a HashTable, HashMap or HashSet.
		@Override
		public int hashCode(){
		    return sn.hashCode(); //this.indiceInicio * sn.hashCode();
		  }
		
		public static Map<String,SNData> converttoSNDataMap(Map<String,Integer> m){
			Map<String,SNData> result = new HashMap<String,SNData>();
//			Iterator<String> keys = m.keySet().iterator();
			for(Map.Entry<String, Integer> e: m.entrySet()){
				result.put(e.getKey(), new SNData(e));
			}
			return result;
		}
		
	public int getFreq() {
		if(pos!= null)
		return pos.size();
		else
			return -1;
	}
	
	public void setFreq(int ntf) {
		//this.tf = ntf;
		System.out.println("Função sem efeito, use addPosition!");
	}
	
	public void setNivel(int n) {
		this.nivel = n;
	}
	
	public int getNivel() {
		return this.nivel;
	}
	
	@Override
	public String toString() {
		// TODO Imprime somente a primeira posição do sn no texto
		return "["+ this.sn +","+ this.nivel +","+ this.getFreq() +","+ this.getIndiceInicio() +"]";
	}
	
	public static Vector<SNData> concat (Vector<Vector<SNData>> snet){
		Vector<SNData> result = new Vector<SNData> ();
		for (Vector<SNData> p : snet) {
	        result.addAll(p);
	    }
		return result;
	}

		
		public static void main(String[] args){
			HashMap<String, SNData> people = new HashMap<String, SNData>();

		    SNData jim = new SNData("Jim", 25);
		    jim.setNivel(1);
		    SNData scott = new SNData("Scott", 23);
		    scott.setNivel(2);
		    SNData anna = new SNData("Anna", 28);
		    anna.setNivel(0);

		    people.put(jim.getSN(), jim);
		    people.put(scott.getSN(), scott);
		    people.put(anna.getSN(), anna);

		    // not yet sorted
		    List<SNData> peopleByAge = new ArrayList<SNData>(people.values());

		    //ordenando por posicao
		    Collections.sort(peopleByAge);
		    for (SNData p : peopleByAge) {
		        System.out.println(p.getSN() + "\t" + p.getIndiceInicio());
		    }
		    
		    //ordenando alfabético
		    Collections.sort(peopleByAge, SNData.ALPHABETIC_ORDER);
		    for (SNData p : peopleByAge) {
		        System.out.println(p.getSN() + "\t" + p.getIndiceInicio());
		    }
		    
		  //ordenando por pontuação
		    Collections.sort(peopleByAge, SNData.RANK_ORDER);
		    for (SNData p : peopleByAge) {
		        System.out.println(p.getSN() + "\t" + p.getIndiceInicio()+ "\t" + p.getRank());
		    }
		}

		
	    
	    
	}

