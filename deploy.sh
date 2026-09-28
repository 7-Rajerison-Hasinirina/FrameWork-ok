#!/bin/bash

# 1. Définition des variables
JAR_NAME="framework.jar"
SRC_DIR="src"
BIN_DIR="bin"

# 2. Nettoyage des anciens dossiers
echo "🧹 Nettoyage des anciennes compilations..."
rm -rf "$BIN_DIR"
rm -f "$JAR_NAME"
mkdir "$BIN_DIR"

echo "⚙️ Compilation des fichiers Java..."

# Utilisation de find pour lister tous les fichiers .java
find "$SRC_DIR" -name "*.java" > sources.txt

# Sélection d'un JDK compatible Jakarta Servlet / Spring 6 (Java 17+)
JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}"
if [ ! -x "$JAVA_HOME/bin/javac" ]; then
    for candidate in \
        /usr/lib/jvm/java-21-openjdk-amd64 \
        /usr/lib/jvm/java-17-openjdk-amd64 \
        /usr/lib/jvm/jdk-17-oracle-x64 \
        /usr/lib/jvm/java-11-openjdk-amd64; do
        if [ -x "$candidate/bin/javac" ]; then
            JAVA_HOME="$candidate"
            break
        fi
    done
fi

export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"

# Exclusion du vieux JAR servlet-api.jar (Java EE 8 / class version 52) qui est incompatible
# avec les imports Jakarta Servlet utilisés dans le projet.
CLASSPATH=$(find "lib" -maxdepth 1 -type f -name '*.jar' ! -name 'servlet-api.jar' -printf '%p:' | sed 's/:$//')

if [ -z "$CLASSPATH" ]; then
    echo "❌ Aucun JAR compatible trouvé dans le dossier 'lib'."
    exit 1
fi

"$JAVA_HOME/bin/javac" -cp "$CLASSPATH" -d "$BIN_DIR" @sources.txt

COMPILE_STATUS=$?
rm -f sources.txt

if [ $COMPILE_STATUS -eq 0 ]; then
    echo "✅ Compilation réussie. Création du fichier JAR..."
    
    # Déplacement dans le dossier bin pour empaqueter
    cd "$BIN_DIR" || exit
    
    # Création du JAR avec toutes les classes du framework
    jar -cvf "../$JAR_NAME" .
    cd ..
    
    echo "🎉 Le fichier '$JAR_NAME' est prêt et à jour !"
else
    echo "❌ Échec de la compilation. Vérifiez vos imports ou la présence des JARs Spring dans le dossier 'lib'."
    exit 1
fi