package com.mica.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.swing.JOptionPane;

import com.mica.main.Akcija;
import com.mica.main.Controller;
import com.mica.main.Klijent;
import com.mica.main.PoljeAkcija;
import com.mica.main.Pozicija;
import com.mica.main.Stanje;

public class DeepReinforcementLearning {
	private ArrayList<Ishod> replayMemory;
	//private HashMap<String, Double> tezine;
	
	private double epsilon; 
	//private double zanemarivanje =  0.9;
	
	private int SUBSET_OF_REPLAY_MEMORY_MAX_SIZE = 500;
	
	private Random random = new Random();
	
	private Klijent klijent;
	
	
	public DeepReinforcementLearning() {
		replayMemory = new ArrayList<Ishod>();
		//tezine = new HashMap<String, Double>();
		this.epsilon = 0.10;
	}
	
	public void isprazniReplayMemory() {
		replayMemory.clear();
	}

	public PoljeAkcija getNovoSelektovanoPoljeIAkcija(Stanje stanje) {
		ArrayList<PoljeAkcija> mogucaSelektovanaPoljaIAkcije = stanje.getMogucaSelektovanaPoljaIAkcijeZaDatoStanje();
		if(mogucaSelektovanaPoljaIAkcije.isEmpty()) {
			return null;
		}
		
		
        if (ReinforcementLearning.baciNovcic(this.epsilon)) {
        	// TODO: bolje bi bilo prvo proveriti moze li se doci u stanje koje jos nikad nismo posetili,
        	// ako ima takvih, izabracemo random stanje od tih neposecenih, a ako nema neposecenih, tj. sva su posecena,
        	// onda izabrati random od svih mogucih stanja 
        	int randomIndeks = this.random.nextInt(mogucaSelektovanaPoljaIAkcije.size());
        	//qVrednostiZaMoguceAkcije = null;

        	return mogucaSelektovanaPoljaIAkcije.get(randomIndeks);
        }
        
        //postaviQVrednostiZaMoguceAkcije(stanje, mogucaSelektovanaPoljaIAkcije);
        
        PoljeAkcija pa = getAkcijaPoPolotici(stanje, mogucaSelektovanaPoljaIAkcije);
        /*if(pa == null) {
        	return null;
        }*/
        return pa;
	}

	private PoljeAkcija getAkcijaPoPolotici(Stanje stanje, ArrayList<PoljeAkcija> mogucaSelektovanaPoljaIAkcije) {
		String odgovor = null;
		Akcija akcija = null;
		klijent = new Klijent();
		
		try {
			odgovor = klijent.posaljiPodatkeISacekajOdgovor("predict|" + stanje.toStringForNN() + ":" 
					+ getSelektovnaPoljaStr(mogucaSelektovanaPoljaIAkcije), true);
			String[] tokens = odgovor.split(";");
			int indexOfAction = Integer.parseInt(tokens[0].trim());
			akcija = Akcija.values()[indexOfAction];
			
			int a = Integer.parseInt(tokens[1].trim());
			Pozicija pozicijaSelektovanogPolja;
			if(a != -1) {
				pozicijaSelektovanogPolja = new Pozicija(a / Controller.BROJ_POLJA_U_KRUGU, a % Controller.BROJ_POLJA_U_KRUGU);
			}
			else {
				pozicijaSelektovanogPolja = new Pozicija(-1, -1);
			}
			
			PoljeAkcija poljeAkcija = getPoljeAkcija(mogucaSelektovanaPoljaIAkcije, akcija, pozicijaSelektovanogPolja);
			return poljeAkcija;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	private PoljeAkcija getPoljeAkcija(ArrayList<PoljeAkcija> mogucaSelektovanaPoljaIAkcije, Akcija akcija, Pozicija pozicijaSelektovanogPolja) {
		for (PoljeAkcija poljeAkcija : mogucaSelektovanaPoljaIAkcije) {
			if(poljeAkcija.getAkcija() == akcija) {
				if(pozicijaSelektovanogPolja.getX() == -1 && pozicijaSelektovanogPolja.getY() == -1) {
					return poljeAkcija;
				}
				else if(poljeAkcija.getPolje().getPozicija().equals(pozicijaSelektovanogPolja)) {
					return poljeAkcija;
				}
			}
		}
		
		return null;
	}

	private String getSelektovnaPoljaStr(ArrayList<PoljeAkcija> mogucaSelektovanaPoljaIAkcije) {
		PoljeAkcija selektovanoPoljeAkcija;
		int selektovanoPoljeInt;
		
		HashMap<Integer, ArrayList<Akcija>> hm = new HashMap<Integer, ArrayList<Akcija>>();
		
		for(int i = 0; i < mogucaSelektovanaPoljaIAkcije.size(); i++) {
			selektovanoPoljeAkcija = mogucaSelektovanaPoljaIAkcije.get(i);
			if(selektovanoPoljeAkcija.getPolje() != null) {
				selektovanoPoljeInt = selektovanoPoljeAkcija.getPolje().getPozicija().getX() * Controller.BROJ_POLJA_U_KRUGU 
						+ selektovanoPoljeAkcija.getPolje().getPozicija().getY();
			}
			else {
				selektovanoPoljeInt = -1;
			}
			
			if(!hm.containsKey(selektovanoPoljeInt)) {
				hm.put(selektovanoPoljeInt, new ArrayList<Akcija>());
			}
			hm.get(selektovanoPoljeInt).add(selektovanoPoljeAkcija.getAkcija());
			
		}
		
		StringBuilder sb = new StringBuilder("");
		StringBuilder sb2;
		
		for (Integer key : hm.keySet()) {
			sb.append(",");
			sb2 = new StringBuilder("");
			for (Akcija akcija : hm.get(key)) {
				sb2.append(";");
				sb2.append(akcija.ordinal());
			}
			sb.append(key);
			sb.append(".");
			sb.append(sb2.substring(1));
			
		}
		return sb.substring(1);
	}

	public void sacuvajNoviIshodUReplayMemory(Stanje staroStanje, Akcija akcija, Stanje novoStanje, double nagrada) {
		Ishod ishod = new Ishod(staroStanje, akcija, nagrada, novoStanje);
		replayMemory.add(ishod);
	}
	
	public void odradiTreningNadSlucajnoOdabranimPodskupom() {
		List<Ishod> subset = getSubsetOfReplayMemory();
		klijent = new Klijent();
		String odgovor = null;
		
		try {
			odgovor = klijent.posaljiPodatkeISacekajOdgovor("train|"+getStringForSubset(subset), true);
		} catch (Exception e) {
			//JOptionPane.showMessageDialog(null, "Server je prekinuo konekciju i nije vratio odgovor.", "Greska", JOptionPane.ERROR_MESSAGE);
			System.out.println("Server je prekinuo konekciju i nije vratio odgovor.");
			return;
		}
		
		if(odgovor.equals("error")) {
			JOptionPane.showMessageDialog(null, "Greska prilikom treniranja neuronske mreze", "Greska", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private String getStringForSubset(List<Ishod> subset) {
		StringBuilder sb = new StringBuilder("");
		for (Ishod ishod : subset) {
			sb.append(",");
			sb.append(ishod.toStringForNN());
		}
		
		return sb.substring(1);
	}
	
	public void azurirajTezineUNeuronskojMrezi() {
		klijent = new Klijent();
		String odgovor = null;
		
		try {
			odgovor = klijent.posaljiPodatkeISacekajOdgovor("update", true);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(odgovor.equals("error")) {
			JOptionPane.showMessageDialog(null, "Greska prilikom azuriranja tezina u neuronskoj mrezi", "Greska", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public List<Ishod> getSubsetOfReplayMemory() {
		int rmSize = replayMemory.size();
		ArrayList<Ishod> subset = new ArrayList<Ishod>(replayMemory);
		//Collections.copy(subset, replayMemory);
		Collections.shuffle(subset);
		
		System.out.println("ReplayMemory size: " + rmSize);
		
		if(rmSize > SUBSET_OF_REPLAY_MEMORY_MAX_SIZE) {
			System.out.println("Subset size: " + SUBSET_OF_REPLAY_MEMORY_MAX_SIZE);
			return subset.subList(0, SUBSET_OF_REPLAY_MEMORY_MAX_SIZE);
		}
		
		System.out.println("Subset size: " + subset.size());
		return subset;
	}

	public double getEpsilon() {
		return epsilon;
	}

	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}
	
	
}
