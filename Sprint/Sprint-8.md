# Sprint 8 - Upload de fichier

## Objectif
Le framework doit détecter automatiquement si un formulaire contient un fichier, le traiter en multipart, puis enregistrer le fichier dans un dossier configurable via un fichier properties.

## Étapes de mise en œuvre

### 1. Détecter un formulaire multipart
- Rôle : reconnaître qu’un formulaire est envoyé avec `enctype="multipart/form-data"`.
- Code concerné : `src/utilitaires/ParamBinder.java`
- Ce que fait le code :
  - vérifie `req.getContentType()`
  - détecte si le paramètre attendu est de type `Part` ou `File`
  - appelle `req.getPart(nomChamp)` au lieu de `req.getParameter(nomChamp)`

### 2. Lier le fichier au contrôleur
- Rôle : permettre au développeur d’écrire une action avec un paramètre fichier.
- Exemple autorisé :

```java
@UrlMapping(value = "/upload", METHOD = "POST")
public ModelView upload(@RequestParam(value = "nom") String nom,
                       @RequestParam(value = "fichier") Part fichier) {
    ModelView mv = new ModelView();
    mv.setUrl("/upload.jsp");
    mv.addObject("nom", nom);
    mv.addObject("fichier", fichier.getSubmittedFileName());
    return mv;
}
```

- Point important : le binder détecte tout seul si la donnée correspond à un fichier ou à un champ normal.

### 3. Stocker le fichier dans le dossier configuré
- Rôle : enregistrer l’upload dans le répertoire défini par le projet.
- Code concerné : `src/utilitaires/UploadUtil.java`
- Ce que fait le code :
  - lit le dossier d’upload depuis `framework.properties`
  - crée le dossier s’il n’existe pas
  - copie le flux binaire du `Part` dans ce dossier

### 4. Configurer le chemin du dossier via properties
- Rôle : rendre le dossier d’upload configurable sans coder en dur le chemin dans Java.
- Fichier : `framework.properties`
- Exemple :

```properties
upload.directory=uploads
```

- Le framework essaie aussi de lire :
  - `upload.directory`
  - `upload.folder`
  - `upload.path`
  - `framework.upload.directory`

### 5. Intégrer le properties dans le JAR
- Rôle : garantir que le fichier de config est bien présent dans le framework embarqué.
- Code concerné : `deploy.sh`
- Ce que fait le script :
  - copie `framework.properties` dans `bin/`
  - puis package le dossier dans `framework.jar`

### 6. Vérification fonctionnelle
- Rôle : vérifier que le cas réel est bien couvert.
- Test de régression créé : `tests/UploadBinderTest.java`
- Cas vérifié :
  - formulaire avec `multipart/form-data`
  - champ texte + fichier
  - binder détecte le fichier et le renvoie comme `Part`

---

## Rôle de chaque fichier dans le sprint

- `src/utilitaires/ParamBinder.java`
  - détecte le type de paramètre
  - distingue champ texte et fichier
  - appelle la bonne méthode de lecture (`getParameter` ou `getPart`)

- `src/utilitaires/UploadUtil.java`
  - lit le chemin de stockage
  - crée le dossier
  - sauvegarde le fichier uploadé

- `framework.properties`
  - contient le chemin du dossier de destination

- `deploy.sh`
  - inclut la configuration dans le JAR du framework

- `tests/UploadBinderTest.java`
  - teste le cas réel d’upload multipart

---

## Exemple d’usage dans l’application de test

```java
@UrlMapping(value = "/save", METHOD = "POST")
public ModelView save(@RequestParam(value = "nom") String nom,
                     @RequestParam(value = "fichier") Part fichier) {
    ModelView mv = new ModelView();
    File saved = UploadUtil.saveUploadedFile(fichier);
    mv.setUrl("/result.jsp");
    mv.addObject("nom", nom);
    mv.addObject("fichier", saved.getName());
    return mv;
}
```

Cela permet d’envoyer un formulaire contenant des champs texte et un fichier, puis de stocker le fichier dans le dossier défini par `framework.properties`.

## Résultat attendu
- le framework détecte automatiquement si le formulaire contient un fichier ;
- un `Part` ou un `File` est correctement bindé ;
- le fichier est enregistré dans `uploads/` (ou le dossier défini dans le properties) ;
- l’application de test peut ensuite utiliser ce fichier pour le traiter ou l’afficher.
