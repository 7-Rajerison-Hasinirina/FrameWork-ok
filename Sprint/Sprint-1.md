# Sprint-1 : Premier contrôleur frontal

## Objectif
Le but du sprint 1 est de faire entrer toutes les requêtes dans un point d’entrée unique, afin de les traiter et de commencer la logique du framework MVC.

## Contexte
Avant d’avoir un vrai routing complet, le framework doit savoir qu’une requête HTTP passe toujours par une servlet principale. C’est le point de départ de toute architecture web légère.

## Partie technique

### 1. La servlet principale
Fichier concerné : [src/controllers/FrontServletController.java](../src/controllers/FrontServletController.java)

Cette classe hérite de `HttpServlet` et reçoit les requêtes HTTP. Elle joue le rôle d’intercepteur central.

```java
@Override
protected void doGet(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
    processRequest(req, res);
}

@Override
protected void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
    processRequest(req, res);
}
```

### 2. Traitement centralisé
La méthode `processRequest(...)` est le cœur du sprint 1. Elle analyse:

- la méthode HTTP utilisée,
- l’URL demandée,
- le contexte de l’application,
- la route cible.

```java
String httpMethod = req.getMethod().toUpperCase();
String requestUri = req.getRequestURI();
String contextPath = req.getContextPath();
String routeUrl = requestUri.substring(contextPath.length());
```

## Rôle du sprint 1
Le framework commence à organiser le flux :

Requête HTTP -> FrontServletController -> traitement -> identification de la route.

C’est la base de l’architecture MVC : une seule entrée pour toutes les demandes.

## Résultat attendu
- toutes les requêtes passent par le contrôleur frontal,
- l’URL est interprétée centralement,
- le framework est prêt à associer cette URL à une action plus tard.

## Conclusion
Le sprint 1 ne fait pas encore tout le routing, mais il pose la structure fondamentale : un point d’entrée unique qui centralise les requêtes.
