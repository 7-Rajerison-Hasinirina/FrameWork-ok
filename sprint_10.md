# Sprint 10 - Authentification simple

## Objectif
Ajouter la notion d'authentification au framework en s'appuyant sur la session déjà mise en place.

Le but est simple :
- si un client n'est pas authentifié, il ne peut pas exécuter certaines actions ;
- si un client est authentifié, il peut accéder à ces actions normalement ;
- on ne gère pas encore les rôles, seulement l'authentification basique.

## Idée fonctionnelle
Comme dans Spring Security, on va définir une annotation de protection :
- @AuthRequired

Cette annotation peut être placée :
- sur une classe controller ;
- ou sur une méthode de controller.

Quand une requête arrive :
1. le framework récupère la session ;
2. il vérifie si l'attribut d'authentification existe ;
3. si l'attribut est absent, la requête est bloquée ;
4. sinon, l'action est exécutée normalement.

## Règle de base
Pour une version simple, on prend comme convention :
- l'utilisateur est authentifié si la session contient un attribut nommé : user

Exemple :
```java
session.setAttribute("user", "alice");
```

Donc, dans la logique du framework :
```java
if (session == null || session.getAttribute("user") == null) {
    // utilisateur non authentifié
}
```

## Annotation proposée
Créer une annotation dans le package `annotations` :

```java
package annotations;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthRequired {
    String value() default "user";
    String loginPage() default "/login.jsp";
    String message() default "Vous devez vous connecter pour accéder à cette ressource.";
}
```

## Comportement attendu
### Cas 1 : utilisateur connecté
- La session contient `user`
- L’action est autorisée
- La méthode est exécutée

### Cas 2 : utilisateur non connecté
- La session ne contient pas `user`
- L’action est refusée
- Le framework redirige vers la page de login, par exemple :
  `/login.jsp`

## Exemple de controller
```java
package controllers;

import annotations.AuthRequired;
import annotations.Controllers;
import annotations.UrlMapping;
import models.ModelView;
import models.MySession;

@Controllers
public class UserController {

    @UrlMapping(value = "/login", METHOD = "POST")
    public ModelView login(MySession session, String nom) {
        session.set("user", nom);
        return new ModelView("dashboard");
    }

    @AuthRequired("user")
    @UrlMapping(value = "/dashboard", METHOD = "GET")
    public ModelView dashboard() {
        return new ModelView("dashboard");
    }
}
```

## Ce que le développeur doit tester
### Test 1 - accès sans connexion
- ouvrir l’URL : `/dashboard`
- sans session active
- résultat attendu : redirection vers `/login.jsp` ou message 401/403

### Test 2 - accès avec connexion
- faire un appel de login pour créer la session
- exemple : `/login` avec paramètre `nom=alice`
- puis ouvrir `/dashboard`
- résultat attendu : accès autorisé

### Test 3 - annotation sur méthode
- une action protégée uniquement par `@AuthRequired` sur la méthode
- sans session : refus
- avec session : autorisé

### Test 4 - annotation sur classe
- mettre `@AuthRequired` au niveau du controller
- toutes les méthodes du controller deviennent protégées
- si la session est vide, aucune méthode ne peut s’exécuter

## Critères d’acceptation
- Une route protégée est refusée si la session ne contient pas l’utilisateur
- Une route protégée est autorisée si la session contient l’utilisateur
- Le framework doit s’appuyer sur la session déjà existante (`MySession` / `HttpSession`)
- Le comportement est cohérent avec la logique Spring Boot : un accès protégé nécessite une authentification préalable

## Point important
L’objectif de ce sprint est seulement la protection d’accès, pas encore la gestion des rôles.

Donc :
- `user` authentifié -> accès autorisé
- `user` absent -> accès refusé
- `admin`, `manager`, `role` ne sont pas encore gérés dans ce sprint

## Livrable attendu
Le développeur doit fournir :
1. une annotation `@AuthRequired`
2. une vérification dans `FrontServletController`
3. une redirection ou blocage si l’utilisateur n’est pas connecté
4. une validation avec au moins 2 cas de test :
   - connecté
   - non connecté
