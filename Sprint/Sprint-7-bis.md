# Sprint-7-bis : Binding automatique d’objet

## Objectif
Récupérer un objet Java directement à partir des champs d’un formulaire, sans écrire tout le mapping manuellement.

## Contexte
Au lieu d’avoir plusieurs arguments simples, on veut récupérer un objet `Etudiant` rempli automatiquement.

## Exemple attendu
```java
@UrlMapping(value = "/saveEtudiant", METHOD = "POST")
public ModelView save(Etudiant etudiant) {
    ModelView mv = new ModelView("resultat");
    mv.addAttribute("etudiant", etudiant);
    return mv;
}
```

## Ce que fait le framework
Dans [src/utilitaires/ParamBinder.java](../src/utilitaires/ParamBinder.java), la méthode `bindObject(...)` :

- instancie l’objet,
- lit ses champs,
- cherche les valeurs correspondantes dans la requête,
- remplit l’objet automatiquement.

```java
Object instance = type.getDeclaredConstructor().newInstance();
String rawValue = resolveRequestValue(req, fieldName);
Object value = ParamConverter.convert(rawValue, field.getType());
```

## Exemple de classe métier
```java
public class Etudiant {
    private String nom;
    private String prenom;
    private int age;
}
```

Si le formulaire contient `nom`, `prenom`, `age`, le framework remplit automatiquement les champs de l’objet.

## Résultat attendu
Le contrôleur reçoit un objet déjà valorisé, prêt à être utilisé.

## Conclusion
Le sprint 7-bis est la version avancée du binding : il permet de manipuler des objets complets au lieu de variables isolées.
