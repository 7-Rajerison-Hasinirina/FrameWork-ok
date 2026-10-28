# Sprint-9 : Session utilisateur avec MySession

## Objectif
Gérer la session HTTP de l’utilisateur afin de stocker des informations comme un identifiant, une information de connexion ou un statut utilisateur.

## Contexte
Le framework a besoin d’un mécanisme simple pour garder des données sur le client pendant une session web. C’est là qu’intervient `MySession`.

## La classe MySession
Fichier : [src/models/MySession.java](../src/models/MySession.java)

```java
public class MySession {
    private final HttpSession session;

    public void set(String key, Object value) {
        session.setAttribute(key, value);
    }

    public Object get(String key) {
        return session.getAttribute(key);
    }
}
```

## Rôle
Le développeur peut :
- écrire un objet dans la session,
- le lire dans une autre action,
- le supprimer,
- invalider la session.

## Utilisation
```java
MySession session = MySession.fromRequest(req);
session.set("user", "admin");
Object user = session.get("user");
```

Le framework récupère la session HTTP réelle du navigateur et l’encapsule dans une API plus simple à utiliser.

## Lien avec le framework
Dans [src/utilitaires/ParamBinder.java](../src/utilitaires/ParamBinder.java), la méthode `bindParams(...)` gère les paramètres de type `HttpSession` et `MySession`.

```java
if (parameterType.equals(HttpSession.class)) {
    return req.getSession();
}

if (parameterType.equals(MySession.class)) {
    return MySession.fromRequest(req);
}
```

## Résultat attendu
Le contrôleur peut facilement accéder à la session sans manipuler directement le `HttpSession` brut.

## Conclusion
Le sprint 9 apporte la gestion de session, qui est indispensable pour les fonctionnalités de connexion, de profil utilisateur et d’accès restreint.
