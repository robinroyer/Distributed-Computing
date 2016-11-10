## Infonuagique TP 2


#### Compiler le projet

- Depuis le dossier pincipal :
```bash
ant
```


#### Lancer un serveur de calculs
- Lancer le `rmiregistry` depuis le dossier `/bin` :
```bash
rmiregistry 5010 &
```

- Lancer le server depuis le dossier principal :
```bash
# ./calculousServer IP PORT CONFIDENCE CAPACITY
./calculousServer 127.0.0.1 5010 0 5
```

#### Lancer le repartiteur de calculs

- Configuration de l'IP / Port des serveurs de calculs du repartiteur dans `config/servers.config`:
```bash
127.0.0.1 5010
127.0.0.1 5011
190.0.0.1 5010
```


- Depuis le dossier pincipal en mode "protégé" :
```bash
./repartitor -S
# => les calculs effectués seront verifiés par un 2eme serveur
```

- Depuis le dossier pincipal en mode "non-protégé" :
```bash
./repartitor
# => les calculs effectués seront supposés correctes
```

#### Utilisation du repartiteur

Une fois le repartiteur lancé, il attend 2 types de commandes:
- `compute FILENAME` -> Repartit les calcules du fichier `FILENAME` sur les serveurs de calcul.

- `exit` -> met fin au programme
