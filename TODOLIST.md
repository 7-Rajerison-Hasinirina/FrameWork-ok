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
