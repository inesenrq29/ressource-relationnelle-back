# Installation du back-end Ressources Relationnelles
Ce document explique comment installer, configurer et lancer le back-end de l’application Ressources Relationnelles en environnement local.


## Prérequis
- Java 21
- Maven
- MariaDB
- Docker avec le containeur tools lancé (voir l'installation du tools ici: )

## Lancer la base de données
```bash
docker compose up -d
```

## Lancer spotless pour le formatage
```bash
mvn spotless:apply
```

## Lancer l’application
Pour compiler le projet et exécuter les test:
```bash
mvn clean install
```

2 lancements possibles:
### Via IntelliJ
Une configuration peut être créée avec les paramètres suivants:
- Main class: RessourceRelationnelleApplication
- Working directory: dossier back
- JDK: Java 21
- Profil actif: local
Lancer ensuite la configuration RessourceRelationnelleApplication

### Via Maven
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

ou :

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local"
```

## Lancer les tests
```bash
mvn test
```

## Ordre de lancement recommandé
Pour lancer correctement le back-end en local, l’ordre conseillé est le suivant :

1. Démarrer la base de données avec Docker.
2. Vérifier que les variables d’environnement sont configurées.
3. Appliquer le formatage avec Spotless.
4. Compiler le projet avec Maven.
5. Lancer l’application avec le profil `local`.
