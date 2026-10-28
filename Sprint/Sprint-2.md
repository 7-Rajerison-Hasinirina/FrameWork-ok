# Sprint-2 : Création de UrlMapping

## Objectif
Associer une URL à une méthode d’un contrôleur, afin de savoir quelle action exécuter lorsqu’un client demande une page.

## Contexte
Le framework doit pouvoir dire :

- cette URL correspond à cette classe,
- cette méthode est la bonne méthode à exécuter,
- sinon, l’URL est inconnue.

## Les éléments créés

### 1. L’annotation de route
Fichier : [src/annotations/UrlMapping.java](../src/annotations/UrlMapping.java)

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UrlMapping {
    String value();
    String METHOD() default "GET";
}
```

Elle permet de marquer une méthode comme étant accessible par une URL.

### 2. L’annotation de contrôleur
Fichier : [src/annotations/Controllers.java](../src/annotations/Controllers.java)

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controllers {
}
```

Elle permet d’identifier les classes qui seront prises en compte comme contrôleurs.

### 3. La table de routage
Fichier : [src/models/Mapping.java](../src/models/Mapping.java)

```java
public class Mapping {
    private String nomClasse;
    private String nomMethode;
}
```

Le mapping contient :
- le nom de la classe,
- le nom de la méthode.

### 4. La clé de route
Fichier : [src/models/UrlMethode.java](../src/models/UrlMethode.java)

Cette classe permet de distinguer les routes selon :
- l’URL,
- la méthode HTTP.

Exemple :
- /test + GET
- /test + POST

Ce sont deux clés différentes.

## Processus
Le framework parcourt les classes du package configuré, repère les classes annotées `@Controllers`, puis leurs méthodes annotées `@UrlMapping`.

Ensuite, il construit une map de type :

```java
Map<UrlMethode, Mapping>
```

## Exemple
```java
@Controllers
public class TestController {

    @UrlMapping(value = "/test", METHOD = "GET")
    public void afficher() {
    }
}
```

Le framework associe alors :

- URL `/test`
- méthode `GET`
- contrôleur `TestController`
- méthode `afficher`

## Résultat attendu
- si une URL existe, la méthode correspondante est trouvée,
- si elle n’existe pas, le framework renvoie une erreur de type not found.

## Conclusion
Le sprint 2 donne au framework sa capacité de routage basique, qui est la base du fonctionnement MVC.
