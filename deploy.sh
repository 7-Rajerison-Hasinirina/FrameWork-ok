

#!/bin/bash

# Définition des variables
APP_NAME="FrameWork"
SRC_DIR="src/main/java"
WEB_DIR="src/main/webapp"
BUILD_DIR="build"
LIB_DIR="src/main/webapp/WEB-INF/lib"
TOMCAT_DIR="/home/hasina/DOSSIERL2/tomcat/"
TOMCAT_LIB="$TOMCAT_DIR/lib"
TOMCAT_WEBAPPS="$TOMCAT_DIR/webapps"

clear



# Construction du CLASSPATH
SERVLET_API="$TOMCAT_LIB/servlet-api.jar:$TOMCAT_LIB/jsp-api.jar:lib/mysql-connector-j-9.4.0.jar"
CLASSPATH="$SERVLET_API:$OJDBC_JAR"

# Nettoyage et création des répertoires
echo "Nettoyage..."
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/WEB-INF/classes"
mkdir -p "$BUILD_DIR/WEB-INF/lib"

# Compilation des fichiers Java
echo "Compilation des classes Java..."
find "$SRC_DIR" -name "*.java" > sources.txt

javac -cp "$CLASSPATH" -d "$BUILD_DIR/WEB-INF/classes" @sources.txt

if [ $? -ne 0 ]; then
    echo "Erreur lors de la compilation"
    rm sources.txt
    exit 1
else
    echo "Compilation réussie"
fi

rm sources.txt

# Copie des fichiers web (JSP, web.xml, etc.)
echo "Copie des fichiers web..."
cp -r "$WEB_DIR"/* "$BUILD_DIR/"

# Copie des bibliothèques (ojdbc.jar) dans WEB-INF/lib
echo "Copie des bibliothèques..."
if [ -d "$LIB_DIR" ]; then
    cp "$LIB_DIR"/*.jar "$BUILD_DIR/WEB-INF/lib/" 2>/dev/null
    echo "Bibliothèques copiées (incluant ojdbc)"
fi

# Génération du fichier WAR
echo "Création du fichier WAR..."
cd "$BUILD_DIR" || exit
jar -cf "$APP_NAME.war" *
cd ..

if [ ! -f "$BUILD_DIR/$APP_NAME.war" ]; then
    echo "Erreur : Échec de la création du WAR"
    exit 1
fi

echo "WAR créé : $BUILD_DIR/$APP_NAME.war"

# Génération du fichier JAR
echo "Création du fichier JAR..."

# Supprimer le JAR existant s'il existe
if [ -f "$APP_NAME.jar" ]; then
    rm "$APP_NAME.jar"
    echo "Ancien JAR supprimé"
fi

# Créer un vrai JAR de bibliothèque
cd "$BUILD_DIR/WEB-INF/classes" || exit

jar -cf "../../../$APP_NAME.jar" .

cd ../../..

if [ ! -f "$APP_NAME.jar" ]; then
    echo "Erreur : Échec de la création du JAR"
    exit 1
fi

echo "JAR créé : $APP_NAME.jar"

# Déploiement dans Tomcat
echo "Déploiement sur Tomcat..."

# Arrêt de l'ancienne application si elle existe
if [ -d "$TOMCAT_WEBAPPS/$APP_NAME" ]; then
    rm -rf "$TOMCAT_WEBAPPS/$APP_NAME"
    echo "Ancienne version supprimée"
fi

# Copie du WAR
cp -f "$BUILD_DIR/$APP_NAME.war" "$TOMCAT_WEBAPPS/"

if [ $? -eq 0 ]; then
    echo ""
    echo "Déploiement terminé avec succès !"
    echo "Application disponible sur : http://localhost:8080/$APP_NAME"
    echo ""
    echo "Redémarrez Tomcat si nécessaire :"
    echo "   $TOMCAT_DIR/bin/shutdown.sh"
    echo "   $TOMCAT_DIR/bin/startup.sh"
    echo ""
else
    echo "Erreur lors du déploiement"
    exit 1
fi