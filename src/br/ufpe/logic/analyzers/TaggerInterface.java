package br.ufpe.logic.analyzers;
public interface TaggerInterface {
	
	public abstract String etiquetar(String texto);

	public abstract String[] getLemmas();
	
	public abstract String[] getTags();
	
	public abstract String[] getTokens();
	
	public abstract String getName();

}