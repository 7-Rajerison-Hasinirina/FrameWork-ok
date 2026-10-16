voici mon test dans monn app de test :

/home/hasina/DOSSIER_L3/S5/FRAMEWORK-L2-L3/AppTest/src/main/webapp/WEB-INF/views/etudiant-form.jsp


    @UrlMapping(value = "/etudiant/save", METHOD = "POST")
    public ModelView save(Etudiant etudiant) {
        ModelView mv = new ModelView("etudiant-resultat");
        mv.addAttribute("etudiant", etudiant);
        return mv;
    }

package model;

public class Etudiant {
    private int id;
    private String nom;
    private String matricule;

    public Etudiant() {
    }

    public Etudiant(int id, String nom, String matricule) {
        this.id = id;
        this.nom = nom;
        this.matricule = matricule;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getMatricule() {
        return matricule;
    }

    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }
}

    ici , on peut recuperer  ces champs et que tout ces parametres sont inserer direcement dans les attributs de etudiant 
    mais je vous demande 
    comment va t-on gerer si on aura un autre classe Matiere ( id, nom )
    et que dana la formulaire on ajoute nom aussi 

    et comment va t-on diffferencier nom de la matiere et nom de l'etudiant  dans le controller sachant que dans le controller , on recupere juste Matiere matiere, Etudiant etudiant 

    est ce capable de faire cela notre framework 

