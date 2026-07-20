# Todolist: fRAMEWORK

- Objectif: 
    - Affichage de l'url (requete) : cela veut dire: tout les requetes qu'on a fait sur la navigateur ( sur un projet qui a utilse ce framework , on affiche tout ) 
        ex: on a un Projet ListeEtudiant qui utilse ce framework, et en ajoutanta sur l;url: http://localhost:8080/ListeEtudiant/afficher , on affiche tout les requetes qui ont été fait sur ce projet (ex: http://localhost:8080/ListeEtudiant/afficher , ...
    - on affiche les url

- Requete -> AffControllerServlet -> PrintUrl()

- Comment ?:
    - 1- Concernant au framework, on doit creeer un AffControllerServlet et dedans:
        * contenent les methodes doget et dopost
        * On affiche tout les url ( requete) qui passe sur ce ,
    - 2- Et apres avoir tout cree, et si on a tout fini: , on va alors creer un FrameWork.jar 

- Lorsqu'on a termine la creation de ce framwork , la creation de Framwork.jar :
    - Creation de projet ProjetTest qui utilses tomcat
    - Puis on ajoute dans le lib de projet ce Framwork.jar
    - Dans WEB-INF, on a web.xml, on ajoute le servlet et le mapping de ce servlet (AffControllerServlet) , ( on va appeler dans ce web.xml ce AffControllerServlet pavec /* , et apres, a chaque fois qu'on a fait une requete, toutes ces requetes passent toujours dans le AffControllerServlet , et dans ce servlet, on affiche tout les url ( requete) qui passe sur ce servlet )



# Sprint - 2 : Creation de UrlMapping 
Fonctionnalités: 
- Objectif : On tape un url sur la barre de navigation -> puis on voit si cet url existe et on va afficher dans quelle classe et quelle méthode il se trouve   
sinon , on affiche , url indefini

- comment ?
Création Annotations pour les méthode:         
    * On a une methode annote par @UrlMapping ( ex:/liste/tout )

Critères : 
Partie test : 
On a : ClassseController avec @Controller 
Et on a une méthode : Liste() annote par @UrlMapping ( ex:/liste) par exemple 
Cette méthode est annoté àvec @UrlMapping ( paramètres : /liste/tout ) 

-> jerena n liste ana cobtrolleru 
-> verifiena hoe ty v controller 
-> jerena daol ndray n méthode any
-> verifiena hoe misy Annotations anle @UrlMapping vé 
     Ra misy : de Alaina le valeur 
     F attendu : 
     - url ....
        - controller ( classe ) 
        - méthode 

-> misy variable miampy ndray ao amle FrontServlet

## Remarques:
// Comment va t-on gerer pour que si dans une controller , on pourra aovoir a un url qui sont les meme mais avec des methodes differents, pexemple l'un execute post et l'un execute get, comment on va faire pour que le framework puisse differencier les deux methodes ?
la solutio est de creer une classe avec trois attributs:
    - url
    - methode
    - 


# Sprint- 3 ( suite de sprint 2) - ok
- Situation actuelle:
on a ClasseController
        - UrlMapping("/test")
        -public void test(){}

- Problematique:
    - meme url 
        - methode1 ( post)
        - methode2 ( get )

- Solutions:

# Sprint-3-bis - ok
- On appelle la methode 

- Fonctionnalites attendues

# Sprint-4 - ok
- On utilise Listener 
    - avant on met dans init
    - maintennat, on le mettre dans Listener 

# Sprint 5
Hoan sprint 4, Ilay tanjona dia mandefa donné am page amzay. 

Manao donné statique ao anaty méthode ---> antsoina Ilay methode am alalan Ilay url --> request.addAttribute no mandefa data --> return Ilay view andefasan Ilay data --> mankan am jsp --> Alaina Ilay data dia affichena

Dans le projetTest
- Objectif: envoi de donnees dans une page
    - avant , on ne fait que d'afficher la methode associe a un url , et on invoke justement
    - maintenant , on va envoyer des donnees dans une page
- On cree une fonciton List<String> liste(){ List<String> listes = new ..., return listes et l'envoir vert  }
- Puis on appelle cette methode a partir de l'url 
- request.addAttribute evnoi le donnees vers le view ( le jsp )
- On recupere le donnee puis on les affiche


# Sprint-5-bis

- fini