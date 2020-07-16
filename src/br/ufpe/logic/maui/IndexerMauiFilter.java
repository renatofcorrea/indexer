package br.ufpe.logic.maui;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.entopix.maui.filters.MauiFilter;
import com.entopix.maui.util.Candidate;
import com.entopix.maui.utils.MauiFileUtils;

import br.ufpe.logic.analyzers.SNAnalyser;

public class IndexerMauiFilter extends MauiFilter {

	private static final long serialVersionUID = 900775736457835043L;
	
	public HashMap<String, Candidate> getCandidates(String text) {
		String stopFile = MauiFileUtils.getRootPath() + "/res/sn_stoplist.txt";
		List<String> sns = null;
		try {
			sns = SNAnalyser.extrairSintagmasNominais(new SNAnalyser(stopFile), text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String stringSNS = "";
		Iterator<String> it = sns.iterator();
		
		while (it.hasNext()) {
			stringSNS += it.next() + "\n";
		}
		
		return super.getCandidates(stringSNS);
	}
}
