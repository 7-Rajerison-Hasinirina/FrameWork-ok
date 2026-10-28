# Sprint-10-bis - Gestion des rôles

## Objectif
Ajouter la notion de rôle au framework, en se basant sur la session déjà existante.

L’objectif est d’autoriser ou de refuser certaines actions selon le rôle de l’utilisateur connecté.

Exemples :
- role admin -> adhésion
- role user -> consultation
- role manager -> consultation + modification

## Principe
On suppose qu’après login, la session contient :
- un attribut `user` pour identifier l’utilisateur
- un attribut `role` pour son rôle

Exemple :
```java
session.setAttribute("user", "alice");
session.setAttribute("role", "admin");
```

## Annotation proposée
On ajoute une nouvelle annotation pour protéger les actions selon le rôle :

```java
package annotations;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresRole {
    String[] value();
    String accessDeniedPage() default "/denied.jsp";
    String message() default "Accès refusé pour ce rôle.";
}
```

Exemple d’utilisation :
```java
@RequiresRole({"admin", "manager"})
@UrlMapping(value = "/adhesion", METHOD = "GET")
public ModelView adhesion() {
    return new ModelView("adhesion");
}
```

## Logique de contrôle
Avant d’exécuter une action protégée :
1. récupérer la session
2. vérifier que `user` existe
3. vérifier que `role` existe
4. comparer le rôle de l’utilisateur avec les rôles autorisés
5. si le rôle ne correspond pas, refuser l’accès

Pseudo-code :
```java
String role = (String) session.getAttribute("role");
String[] rolesAutorises = annotation.value();

if (role == null || !Arrays.asList(rolesAutorises).contains(role)) {
    // accès refusé
}
```

## Cas à tester par l’application de test
### 1. role admin -> accès autorisé à adhesion
- se connecter en tant qu’admin
- vérifier que la session contient :
  - `user=alice`
  - `role=admin`
- appeler `/adhesion`
- résultat attendu : accès autorisé

### 2. role user -> accès refusé à adhesion
- se connecter avec role user
- appeler `/adhesion`
- résultat attendu : refus d’accès

### 3. role manager -> accès autorisé à modification
- se connecter avec role manager
- appeler `/modification`
- résultat attendu : accès autorisé

### 4. role user -> accès refusé à modification
- se connecter avec role user
- appeler `/modification`
- résultat attendu : refus d’accès

### 5. session vide
- sans `user` ni `role`
- appeler une action protégée
- résultat attendu : refus d’accès

## Critères d’acceptation
- Le framework vérifie la session avant toute action protégée
- Le framework récupère le rôle depuis la session
- Le rôle est comparé aux rôles autorisés par l’annotation
- Une action avec rôle interdit est bloquée
- Une action avec rôle autorisé est exécutée normalement
- Les cas `null` et session vide sont gérés sans `NullPointerException`

## Exemple de test applicatif
```java
@Controllers
public class RoleController {

    @UrlMapping(value = "/login", METHOD = "POST")
    public ModelView login(HttpServletRequest request, @RequestParam("user") String user,
            @RequestParam("role") String role) {
        HttpSession session = request.getSession(true);
        session.setAttribute("user", user);
        session.setAttribute("role", role);
        return new ModelView("dashboard");
    }

    @RequiresRole({"admin", "manager"})
    @UrlMapping(value = "/adhesion", METHOD = "GET")
    public ModelView adhesion() {
        return new ModelView("adhesion");
    }

    @RequiresRole({"manager"})
    @UrlMapping(value = "/modification", METHOD = "GET")
    public ModelView modification() {
        return new ModelView("modification");
    }
}
```

## Livrable attendu
Le développeur doit fournir :
1. une annotation `@RequiresRole`
2. une vérification du rôle dans le framework avant l’invocation
3. un blocage propre si le rôle n’est pas autorisé
4. un test fonctionnel sur au moins les cas :
   - admin -> autorisé
   - user -> refusé
   - manager -> autorisé
   - session vide -> refusé
