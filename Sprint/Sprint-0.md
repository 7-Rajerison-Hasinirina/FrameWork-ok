# Sprint-0 : Fondations du framework

## 1. Contexte

Le sprint 0 correspond au démarrage du framework MVC léger. L’objectif est de poser les bases de:

- la découverte des contrôleurs,
- la création de la table des routes,
- la réception d’une requête HTTP,
- la résolution de l’URL vers une classe + une méthode,
- l’invocation de cette méthode.

C’est la base sur laquelle les sprints suivants viennent ajouter :
- le rendu de vue,
- les objets de formulaire,
- les fichiers uploadés,
- la session,
- l’authentification,
- les rôles.

---

## 2. Objectif du sprint 0

Le but est simple :

- on récupère toutes les classes annotées comme contrôleurs,
- on lit les méthodes annotées par @UrlMapping,
- on conserve un mapping URL -> controller -> méthode,
- ensuite, lorsqu’un client fait une requête, le framework sait où aller.

Le principe est :

Requête HTTP -> FrontServletController -> recherche dans la table de routage -> invocation de la méthode ciblée.

---

## 3. Classes créées lors du sprint 0

### 3.1. L’annotation de mapping
Fichier : [src/annotations/UrlMapping.java](../src/annotations/UrlMapping.java)

Cette annotation sert à marquer une méthode comme route disponible.

```java
package annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UrlMapping {
    String value();
    String METHOD() default "GET";
}
```

Rôle :
- value = URL de la route (ex : /liste)
- METHOD = méthode HTTP (GET, POST, ...) 

---

### 3.2. L’annotation de contrôleur
Fichier : [src/annotations/Controllers.java](../src/annotations/Controllers.java)

```java
package annotations;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controllers {
}
```

Rôle :
- une classe annotée @Controllers est reconnue comme contrôleur par le framework.

---

### 3.3. La table de routage
Fichier : [src/models/Mapping.java](../src/models/Mapping.java)

```java
package models;

public class Mapping {
    private String nomClasse;
    private String nomMethode;

    public Mapping(String nomClasse, String nomMethode) {
        this.nomClasse = nomClasse;
        this.nomMethode = nomMethode;
    }

    public String getNomClasse() {
        return nomClasse;
    }

    public String getNomMethode() {
        return nomMethode;
    }
}
```

Rôle :
- on stocke le nom de la classe et le nom de la méthode correspondante.

---

### 3.4. La clé de route : URL + méthode HTTP
Fichier : [src/models/UrlMethode.java](../src/models/UrlMethode.java)

```java
package models;

public class UrlMethode {
    private final String url;
    private final String methode;

    public UrlMethode(String url, String methode) {
        this.url = url;
        this.methode = methode;
    }

    @Override
    public boolean equals(Object obj) {
        // compare url + methode
    }

    @Override
    public int hashCode() {
        // hash basé sur url + methode
    }
}
```

Rôle :
- permet de distinguer deux routes identiques mais avec des méthodes différentes,
- ex : /test avec GET et /test avec POST.

---

### 3.5. Le listener qui initialise tout
Fichier : [src/listener/FrameworkContextListener.java](../src/listener/FrameworkContextListener.java)

C’est ici que le framework repère les contrôleurs et construit les routes au démarrage.

```java
@Override
public void contextInitialized(ServletContextEvent sce) {
    ServletContext context = sce.getServletContext();
    String controllerPackage = context.getInitParameter("Controllers");

    try {
        Map<UrlMethode, Mapping> routes = buildRoutes(controllerPackage);
        context.setAttribute("routes", routes);
        context.log("Framework initialized successfully with " + routes.size() + " routes.");
    } catch (Exception e) {
        throw new RuntimeException("Erreur lors du chargement des contrôleurs : " + e.getMessage(), e);
    }
}
```

Rôle :
- lit le package des contrôleurs depuis la configuration,
- scan les classes annotées,
- scan les méthodes annotées @UrlMapping,
- enregistre les routes dans le ServletContext.

---

### 3.6. Le contrôleur frontal
Fichier : [src/controllers/FrontServletController.java](../src/controllers/FrontServletController.java)

C’est la “porte d’entrée” du framework.

```java
@SuppressWarnings("unchecked")
protected void processRequest(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
    Map<UrlMethode, Mapping> routes = (Map<UrlMethode, Mapping>) getServletContext().getAttribute("routes");

    String httpMethod = req.getMethod().toUpperCase();
    String requestUri = req.getRequestURI();
    String contextPath = req.getContextPath();
    String routeUrl = requestUri.substring(contextPath.length());

    Mapping mapping = routes.get(new UrlMethode(routeUrl, httpMethod));

    if (mapping == null) {
        writeNotFound(res, routeUrl, httpMethod);
        return;
    }

    executeMappedAction(req, res, mapping);
}
```

Rôle :
- reçoit toutes les requêtes,
- récupère la route demandée,
- recherche la bonne correspondance dans la table,
- appelle la méthode du contrôleur.

---

## 4. Le fonctionnement exact du sprint 0

### Étape 1 : démarrage du framework
Le listener est déclenché par le conteneur servlet.

- il récupère le paramètre global "Controllers",
- il lit le package des contrôleurs,
- il construit la table des routes.

### Étape 2 : lecture des classes
Le framework parcourt toutes les classes du package cible.

- s’il trouve une classe annotée @Controllers,
- il regarde ses méthodes,
- s’il trouve une méthode annotée @UrlMapping,
- il ajoute cette association à la map.

### Étape 3 : envoi d’une requête
Le client appelle une URL comme :

```text
http://localhost:8080/MonApp/test
```

Puis le FrontServletController reçoit la requête.

### Étape 4 : correspondance URL + méthode
Le framework construit la clé :

```java
new UrlMethode("/test", "GET")
```

et la compare dans la table de routage.

### Étape 5 : invocation de la méthode
Si la route existe, il trouve :
- la classe du contrôleur,
- la méthode du contrôleur,
- puis il l’invoque par réflexion.

---

## 5. Exemple concret de route

Supposons une classe simple :

```java
package controllers;

import annotations.Controllers;
import annotations.UrlMapping;

@Controllers
public class TestController {

    @UrlMapping(value = "/test", METHOD = "GET")
    public void afficher() {
        System.out.println("Bonjour depuis le framework");
    }
}
```

Le framework construira une route équivalente à :

```text
URL = /test
HTTP = GET
Classe = TestController
Méthode = afficher
```

Et quand le navigateur demande /test, le framework saura exactement où aller.

---

## 6. Rôle de ce sprint dans l’ensemble du projet

Le sprint 0 est la base du projet. Sans cette étape :

- pas de découverte des contrôleurs,
- pas de routage,
- pas d’invocation des méthodes,
- pas de logique MVC.

C’est la fondation sans laquelle les sprints suivants ne peuvent pas fonctionner.

---

## 7. Réponse courte pour expliquer au prof

> Le sprint 0 consiste à créer la base du framework MVC. On a commencé par les annotations @Controllers et @UrlMapping, puis la table des routes dans le Listener, et enfin le FrontServletController qui reçoit les requêtes, cherche l’URL correspondante et exécute la méthode du contrôleur. C’est la première étape qui permet au framework de savoir quelle classe et quelle méthode traiter pour une URL donnée.

---

## 8. Emplacement des blocs de code importants

- Contrôleur frontal : [src/controllers/FrontServletController.java](../src/controllers/FrontServletController.java)
- Initialisation du framework : [src/listener/FrameworkContextListener.java](../src/listener/FrameworkContextListener.java)
- Mapping route : [src/models/Mapping.java](../src/models/Mapping.java)
- Clé de route : [src/models/UrlMethode.java](../src/models/UrlMethode.java)
- Annotation de route : [src/annotations/UrlMapping.java](../src/annotations/UrlMapping.java)
- Annotation de contrôleur : [src/annotations/Controllers.java](../src/annotations/Controllers.java)
- Outils de réflexion : [src/utilitaires/Utilitaires.java](../src/utilitaires/Utilitaires.java)

---

## 9. Conclusion

Le sprint 0 n’a pas pour but d’ajouter de la logique métier avancée, mais de poser la structure fondamentale du framework :

- repérage des contrôleurs,
- repérage des routes,
- résolution des requêtes,
- invocation des bonnes méthodes.

C’est la base sur laquelle tout le reste du framework est construit.
