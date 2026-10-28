# Sprint-7 : Formulaires et binding simple des paramètres

## Objectif
Récupérer les données d’un formulaire HTML directement dans les paramètres d’une méthode contrôleur, sans écrire de code de parsing manuel.

## Contexte
Un formulaire HTML envoie des données sous forme de champs nom / valeur. Le framework doit les relier automatiquement aux arguments de la méthode.

## Exemple visé
```java
@UrlMapping(value = "/save", METHOD = "POST")
public ModelView save(String nom, String prenom, int age) {
    ModelView mv = new ModelView("resultat");
    mv.addAttribute("nom", nom);
    return mv;
}
```

## Partie technique
Le code de binding est dans [src/utilitaires/ParamBinder.java](../src/utilitaires/ParamBinder.java).

Il vérifie le type du paramètre et récupère la valeur dans la requête :

```java
String rawValue = resolveRequestValue(req, paramName);
return ParamConverter.convert(rawValue, parameterType);
```

## Cas gérés
- `String`,
- `int`, `double`,
- `boolean`,
- `Date`,
- autres types simples.

## Exemple de formulaire
```html
<form method="post" action="/MonApp/save">
    <input type="text" name="nom">
    <input type="text" name="prenom">
    <input type="number" name="age">
    <button>Ajouter</button>
</form>
```

Le framework associe les champs `nom`, `prenom`, `age` aux arguments Java correspondants.

## Résultat attendu
Les valeurs saisies dans le formulaire sont directement disponibles dans la méthode du contrôleur.

## Conclusion
Le sprint 7 permet au framework de gérer la récupération des données de formulaire de manière simple et pratique.
