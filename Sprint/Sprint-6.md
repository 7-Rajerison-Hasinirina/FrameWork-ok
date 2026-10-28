# Sprint-6 : Annotation @ApiRest et retour JSON

## Objectif
Permettre au framework de renvoyer directement des objets ou des chaînes sous forme JSON, sans passer par une JSP.

## Contexte
Certaines API doivent répondre en JSON au client, par exemple pour un frontend JavaScript ou un appel AJAX.

## L’annotation
Fichier : [src/annotations/ApiRest.java](../src/annotations/ApiRest.java)

```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiRest {
}
```

## Traitement dans FrontServletController
Dans [src/controllers/FrontServletController.java](../src/controllers/FrontServletController.java), si la méthode est annotée `@ApiRest`, le framework ne fait plus de forward vers une JSP.

```java
if (method.isAnnotationPresent(ApiRest.class)) {
    res.setContentType("application/json;charset=UTF-8");
    Gson gson = new Gson();
    out.print(gson.toJson(result));
}
```

## Exemple
```java
@ApiRest
@UrlMapping(value = "/api/client", METHOD = "GET")
public List<String> getClients() {
    return List.of("Alice", "Bob");
}
```

Le navigateur reçoit :

```json
["Alice","Bob"]
```

## Rôle du sprint 6
Le framework devient capable de servir à la fois :
- des vues HTML classiques,
- des réponses JSON pour API.

## Conclusion
Le sprint 6 ajoute la dimension API REST au framework, ce qui ouvre la voie à des applications web plus modernes et plus interactives.
