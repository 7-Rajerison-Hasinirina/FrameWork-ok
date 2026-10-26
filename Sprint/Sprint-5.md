# Sprint-5 : Envoi de données vers la vue

## Objectif
Le framework doit permettre de transmettre des données depuis le contrôleur vers une page JSP, puis de les afficher dans la vue.

## Contexte
Avant, le framework pouvait invoquer une méthode et afficher simplement le résultat. Le sprint 5 ajoute la notion de données métier envoyées à la vue.

## Élément clé : ModelView
Fichier : [src/models/ModelView.java](../src/models/ModelView.java)

```java
public class ModelView {
    private String url;
    private HashMap<String, Object> data;

    public void addAttribute(String key, Object value) {
        this.data.put(key, value);
    }
}
```

Rôle :
- stocke la vue cible,
- stocke les données à transmettre,
- permet de faire `addAttribute("nom", valeur)`.

## Utilisation
Le contrôleur construit un objet `ModelView`, puis envoie des données vers la vue :

```java
ModelView mv = new ModelView("etudiant-resultat");
mv.addAttribute("etudiant", etudiant);
return mv;
```

## Traitement dans FrontServletController
Dans [src/controllers/FrontServletController.java](../src/controllers/FrontServletController.java), la méthode `renderModelView(...)` fait :

```java
for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
    req.setAttribute(entry.getKey(), entry.getValue());
}

String viewPath = prefix + modelView.getUrl() + suffix;
RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
dispatcher.forward(req, res);
```

## Résultat attendu
Le contrôleur peut envoyer des données dans la requête HTTP, puis la JSP récupère et affiche ces données.

## Conclusion
Le sprint 5 transforme le framework d’un simple exécuteur de méthodes en un système MVC complet, avec transmission de données vers la vue.
