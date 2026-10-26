# Sprint-3 : Gestion des méthodes GET/POST sur une même URL

## Objectif
Gérer le cas où plusieurs méthodes utilisent la même URL, mais avec des méthodes HTTP différentes.

## Problème
Le framework doit pouvoir différencier :

- `/test` en GET,
- `/test` en POST.

Sans cette distinction, il ne sait pas quelle méthode appeler.

## Solution mise en place
Le framework crée une clé de route avec deux informations :

- l’URL,
- la méthode HTTP.

C’est la raison pour laquelle la classe [src/models/UrlMethode.java](../src/models/UrlMethode.java) contient ces deux attributs.

```java
new UrlMethode(url, httpMethod)
```

Exemple :

```java
new UrlMethode("/test", "GET")
new UrlMethode("/test", "POST")
```

Ce sont deux objets différents, donc deux routes différentes.

## Intégration dans le code
Dans [src/listener/FrameworkContextListener.java](../src/listener/FrameworkContextListener.java), le listener construit les routes ainsi :

```java
UrlMapping annotation = method.getAnnotation(UrlMapping.class);
String url = annotation.value();
String httpMethod = annotation.METHOD().toUpperCase();
UrlMethode key = new UrlMethode(url, httpMethod);
```

Ensuite, la servlet front reçoit la requête et reconstruit la clé correspondante :

```java
Mapping mapping = routes.get(new UrlMethode(routeUrl, httpMethod));
```

## Exemple
```java
@UrlMapping(value = "/test", METHOD = "GET")
public void afficher() { }

@UrlMapping(value = "/test", METHOD = "POST")
public void enregistrer() { }
```

Le framework distingue bien les deux appels, sans ambiguïté.

## Résultat attendu
- GET /test -> méthode `afficher()`
- POST /test -> méthode `enregistrer()`

## Conclusion
Le sprint 3 résout un point clé de la routage HTTP : la même URL peut être utilisée par plusieurs méthodes selon la méthode HTTP envoyée.
