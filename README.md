# CivilizationMC - Plugin de Civilization Minecraft revisité

Un plugin complet de civilisation/faction pour serveurs Minecraft (Paper 1.20.4) permettant aux joueurs de créer et gérer des civilisations complexes avec des systèmes de territoires, d'économie, de diplomatie et de guerre.

## Fonctionnalités principales

### Gestion des Civilisations
- **Création et gestion** de civilisations avec système de niveaux (1-8)
- **Hiérarchie des rôles** : Leader → Officier → Membre → Recrue
- **Système d'invitations** pour rejoindre une civilisation
- **Bannières personnalisées** pour chaque civilisation

### Système de Territoires
- **Claim de chunks** pour protéger vos terres
- **Carte ASCII** colorée (`/cv map`) affichant les territoires
- **Protection des blocs** contre les joueurs non-autorisés
- **Système de trust** avec permissions granulaires

### Économie
- **Banque de civilisation** avec dépôts et retraits
- **Système de niveaux** : améliorez votre civilisation pour débloquer plus de claims
- **Intégration Vault** pour la gestion monétaire
- **Historique des transactions**

### Diplomatie et Guerre
- **Système d'alliances** entre civilisations
- **Déclaration de guerre** avec période de préparation
- **Score de guerre** et système de paix
- **Protection PVP** configurable par territoire

### Interface Graphique (GUI)
- Interface de gestion des membres
- Interface de gestion des claims
- Informations détaillées sur les civilisations

## Commandes

### Commandes Joueurs (`/cv` ou `/civilization`)

| Commande | Description |
|----------|-------------|
| `/cv create <nom>` | Créer une nouvelle civilisation |
| `/cv disband` | Dissoudre sa civilisation (Leader uniquement) |
| `/cv info [nom]` | Afficher les informations d'une civilisation |
| `/cv list` | Lister les civilisations |
| `/cv invite <joueur>` | Inviter un joueur |
| `/cv join [id]` | Accepter une invitation |
| `/cv leave` | Quitter sa civilisation |
| `/cv kick <joueur>` | Expulser un membre |
| `/cv promote <joueur>` | Promouvoir un membre |
| `/cv demote <joueur>` | Rétrograder un membre |
| `/cv transfer <joueur>` | Transférer le leadership |
| `/cv claim` | Claim le chunk actuel |
| `/cv unclaim` | Libérer le chunk actuel |
| `/cv map` | Afficher la carte des territoires |
| `/cv bank balance` | Voir le solde de la banque |
| `/cv bank deposit <montant>` | Déposer de l'argent |
| `/cv bank withdraw <montant>` | Retirer de l'argent |
| `/cv upgrade` | Améliorer la civilisation |
| `/cv ally <add\|remove\|list> [nom]` | Gérer les alliances |
| `/cv war <nom> [raison]` | Déclarer la guerre |
| `/cv peace <nom>` | Proposer la paix |
| `/cv sethome` | Définir le home de la civilisation |
| `/cv home` | Se téléporter au home |
| `/cv members` | Ouvrir le GUI des membres |
| `/cv claims` | Ouvrir le GUI des claims |

### Commandes Admin (`/cvadmin`)

| Commande | Description |
|----------|-------------|
| `/cvadmin reload` | Recharger la configuration |
| `/cvadmin save` | Sauvegarder les données |
| `/cvadmin backup` | Créer une sauvegarde |
| `/cvadmin list` | Lister toutes les civilisations |
| `/cvadmin info <nom>` | Informations admin d'une civilisation |
| `/cvadmin delete <nom>` | Supprimer une civilisation |
| `/cvadmin setlevel <nom> <niveau>` | Définir le niveau |
| `/cvadmin setmoney <nom> <montant>` | Définir le solde |
| `/cvadmin addmoney <nom> <montant>` | Ajouter/retirer des fonds |
| `/cvadmin forcejoin <joueur> <civ>` | Forcer un joueur à rejoindre |
| `/cvadmin debug` | Afficher les informations de debug |

## Permissions

| Permission | Description |
|------------|-------------|
| `civilization.use` | Accès aux commandes de base |
| `civilization.create` | Créer une civilisation |
| `civilization.admin` | Accès aux commandes admin |
| `civilization.bypass.protection` | Ignorer les protections de claims |
