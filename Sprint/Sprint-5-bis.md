# Sprint-5-bis : Finalisation du rendu MVC

## Objectif
Terminer le mécanisme de passage des données, afin que la vue reçoive bien les attributs et s’affiche correctement.

## Contexte
Le sprint 5 montre le principe ; le sprint 5-bis le finalise en rendant le mécanisme stable et réutilisable.

## Flux complet
Le flux devient :

1. requête HTTP,
2. route trouvée,
3. méthode du contrôleur exécutée,
4. retour `ModelView`,
5. ajout des attributs dans la requête,
6. forward vers la JSP,
7. affichage dans la page.

## Exemple de logique
```java
@UrlMapping(value = "/liste", METHOD = "GET")
public ModelView liste() {
    ModelView mv = new ModelView("liste");
    mv.addAttribute("message", "Bonjour");
    return mv;
}
```

Dans la JSP :

```jsp
<h1>${message}</h1>
```

## Rôle de la vue
La vue ne sait pas comment calculer les données. Elle reçoit simplement les informations déjà préparées par le contrôleur.

## Résultat attendu
Le framework devient véritablement MVC :

- contrôleur : logique,
- modèle : données,
- vue : affichage.

## Conclusion
Le sprint 5-bis clôt la logique de passage de données et prépare le terrain pour les fonctionnalités suivantes : JSON, formulaire, session, sécurité.
