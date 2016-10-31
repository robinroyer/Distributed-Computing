package ca.polymtl.inf4410.tp2.server;

public class CalculServerStatistics {

	int lastNumberOfOperationsSent;
	int lastOperationResult;
	boolean lastOperationOverloaded;
	
	// 0 - Repartiteur deux modes : un safe (no verif) et un non safe (activation verif)
	// 1 - Une liste de calcul unitaire avec tous les calculs du fichier
	// 2 - Chaque Thread par serveur recolte X taches unitaires qu il supprime de la liste des taches unitaires
	// X == 2 au depart, puis incrementation en fonction de la capacite
	// 3 - Chaque serveur effectue ses calculs : 
	// Soit je suis optimise, soit je ne le suis pas encore : 2 calculs si ok 4 si ok 8 when 
	// KO on reduit de 1 et boolean optimiser = true. Si le serveur n a pas la capacite, on repousse dans 
	// la liste de tache unitaire. 
	// 4 - Quand jai fini ma tache, je la pousse dans la liste des taches a verifier 
	// Tache : liste de plusieurs calculs + resultat + serveur origine 
	// 5 - Quand le serveur est disponible : soit il prend des taches a faire (pas de check) en priorite
	// soit il va chercher des taches a verifier (check serveur origine different) => SEMAPHORE SUR LES DEUX LISTES !!
	// 6 - Si le serveur doit verifier une tache, si la tache est trop grosse pour moi, je la diminue
	// dans le retour du repartiteur pour que le serveur finisse par la verifier.
	// 7 - Si le resultat du nouveau calcul est OK => remove task a verifier + incrementation du compteur de calculs
	// et mise a jour du resultat final. Sinon, on re pousse les calculs dans la liste des taches unitaires
	// 8 - Si le compteur de calcul est egal au nombre de calculs de la liste initial alors on peut terminer
	// l'execution du thread, sinon, on sleep avant de relancer le check dans les deux listes.

	public CalculServerStatistics() {
		// TODO Auto-generated constructor stub
	}

}
