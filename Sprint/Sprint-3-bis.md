# Sprint-3-bis : Invocation de la méthode trouvée

## Objectif
Une fois la route trouvée, le framework doit appeler la méthode correspondante de la bonne classe.

## Contexte
La table de routage donne la classe et la méthode, mais le code doit encore les invoquer réellement.

## Partie technique
Dans [src/controllers/FrontServletController.java](../src/controllers/FrontServletController.java), on retrouve le flux suivant :

1. on récupère la route demandée,
2. on charge la classe du contrôleur,
3. on récupère la méthode demandée,
4. on l’invoque par réflexion.

```java
Class<?> controllerClass = Class.forName(controllerPackage + "." + mapping.getNomClasse());
Object controller = springContext.getBean(controllerClass);

Method matchedMethod = findMatchingMethod(controllerClass, mapping.getNomMethode());
Object[] args = ParamBinder.bindParams(req, matchedMethod);
Object result = matchedMethod.invoke(controller, args);
```

## Point important
Le framework utilise la réflexion Java pour appeler dynamiquement une méthode dont le nom est connu seulement au moment de l’exécution.

## Exemple
Si l’URL est `/client/list`, la table de routage peut contenir :

- classe : `ClientController`
- méthode : `list`

Le framework charge ce contrôleur puis appelle `list()`.

## Résultat attendu
- la route est résolue,
- la classe est chargée,
- la méthode ciblée est exécutée,
- le résultat est ensuite rendu selon le type de retour.

## Conclusion
Le sprint 3-bis termine le mécanisme de routing opérationnel : la route n’est plus seulement “déclarée”, elle est aussi exécutée.
