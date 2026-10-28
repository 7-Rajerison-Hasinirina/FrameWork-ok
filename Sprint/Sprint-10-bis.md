# Sprint-10-bis : Gestion des rôles utilisateurs

## Objectif
Restreindre les actions selon le rôle de l’utilisateur : admin, manager, user, etc.

## Contexte
Après l’authentification, on ajoute le contrôle du rôle pour gérer les droits d’accès.

## L’annotation @RequiresRole
Fichier : [src/annotations/RequiresRole.java](../src/annotations/RequiresRole.java)

```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresRole {
    String[] value();
    String accessDeniedPage() default "";
    String message() default "Accès refusé";
}
```

## Vérification dans FrontServletController
Avant l’exécution d’une action, le framework vérifie la présence du rôle dans la session :

```java
RequiresRole requiresRole = getRequiresRole(controllerClass, matchedMethod);
if (requiresRole != null && !hasRequiredRole(req, requiresRole.value())) {
    handleRoleFailure(req, res, requiresRole);
    return;
}
```

## Exemple
```java
@RequiresRole({"admin", "manager"})
@UrlMapping(value = "/adhesion", METHOD = "GET")
public ModelView adhesion() {
    return new ModelView("adhesion");
}
```

### Rôles possibles
- admin : accès à certaines actions sensibles,
- manager : accès à certaines actions de gestion,
- user : accès limité selon les besoins.

## Résultat attendu
- si le rôle est autorisé : action exécutée,
- si le rôle n’est pas autorisé : redirection ou message d’accès refusé.

## Conclusion
Le sprint 10-bis termine le mécanisme de sécurité du framework en ajoutant la gestion des droits par rôle, ce qui permet d’aligner le projet sur une logique de contrôles d’accès plus professionnelle.
