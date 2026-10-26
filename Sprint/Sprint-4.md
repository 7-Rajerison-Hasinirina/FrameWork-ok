# Sprint-4 : Utilisation du listener pour initialiser le framework

## Objectif
Déplacer l’initialisation des routes depuis le `init()` classique vers un listener Servlet compatible avec une startup plus propre.

## Problème avant
Avant, on mettait souvent la logique d’initialisation dans la méthode `init()` d’une servlet. Cela rendait le projet moins flexible et moins standard.

## Solution retenue
Le framework utilise [src/listener/FrameworkContextListener.java](../src/listener/FrameworkContextListener.java), qui implémente `ServletContextListener`.

```java
@WebListener
public class FrameworkContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // initialisation des routes
    }
}
```

## Rôle du listener
Au démarrage du contexte web, il :

- lit le package des contrôleurs,
- repère les classes annotées,
- trouve les méthodes annotées `@UrlMapping`,
- construit la table des routes,
- la stocke dans le `ServletContext`.

```java
context.setAttribute("routes", routes);
```

## Avantage
Cette solution est plus propre et plus conforme à l’architecture Java EE / Jakarta EE. Elle assure que le framework est prêt dès le lancement de l’application.

## Résultat attendu
Le contexte est initialisé avec une table de routage complète, et la servlet principale peut ensuite traiter les requêtes sans recalculer les routes à chaque appel.

## Conclusion
Le sprint 4 fait passer le framework d’un mode “init manuel” à un mode “initialisation automatique au démarrage”, ce qui rend le projet plus propre et plus maintenable.
