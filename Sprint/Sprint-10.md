# Sprint-10 : Authentification

## Objectif
Vérifier qu’un utilisateur est bien connecté avant d’exécuter une action protégée.

## Contexte
Le framework a déjà la session. Maintenant, il doit vérifier si cette session contient bien l’information d’authentification nécessaire.

## L’annotation @AuthRequired
Fichier : [src/annotations/AuthRequired.java](../src/annotations/AuthRequired.java)

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AuthRequired {
    String value() default "user";
    String loginPage() default "";
    String message() default "Vous devez vous connecter";
}
```

## Comportement
Avant d’exécuter l’action, le contrôleur frontal vérifie :

```java
AuthRequired authRequired = getAuthRequirement(controllerClass, matchedMethod);
if (authRequired != null && !isAuthenticated(req, authRequired.value())) {
    handleAuthenticationFailure(req, res, authRequired);
    return;
}
```

## Logique
Le framework vérifie si la clé configurée dans la session existe.

Exemple :
- `user` présent dans la session => utilisateur connecté,
- absent => accès refusé.

## Exemple d’utilisation
```java
@AuthRequired(value = "user", loginPage = "/login.jsp")
@UrlMapping(value = "/profil", METHOD = "GET")
public ModelView profil() {
    return new ModelView("profil");
}
```

## Résultat attendu
- utilisateur connecté : accès autorisé,
- utilisateur non connecté : redirection vers la page de login ou message d’erreur.

## Conclusion
Le sprint 10 apporte la première couche de sécurité fonctionnelle : l’authentification avant accès à certaines actions.
