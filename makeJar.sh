#!/bin/bash
# Script pour compiler le projet et faire un jar sprite pour envoyer a tomcat

TOMCAT_LIB="servlet-api.jar"

# Utilisez des JARs de code compilé, pas des *-javadoc.jar.
REFLECTIONS=".m2/org/reflections/reflections/0.9.11/reflections-0.9.11.jar"
JAVA_ASSIST=".m2/org/javassist/javassist/3.21.0-GA/javassist-3.21.0-GA.jar"
GUAVA=".m2/com/google/guava/guava/20.0/guava-20.0.jar"
SLF4J="/usr/share/java/slf4j-api.jar"

mkdir -p bin

echo "Compilation des fichiers Java..."
javac -cp "$TOMCAT_LIB:$REFLECTIONS:$JAVA_ASSIST:$GUAVA:$SLF4J" -d bin $(find src -name "*.java")

# Si la compilation a réussi
if [ $? -eq 0 ]; then
    echo "Compilation réussie. Création du JAR..."
    
    cd bin
    # Création du JAR sans classe Main car c'est une servlet
    jar cf ../sprite.jar *
    cd ..
    
    echo "Envoi de sprite.jar vers AppTEST..."
    if cp sprite.jar "/mnt/storage/snapd/S4/Web Dynamique/AppTEST/lib/"; then
        echo "Le projet a été compilé, le fichier sprite.jar a été créé et déployé avec succès."
    else
        echo "Erreur lors du déploiement vers AppTEST/lib. Le fichier sprite.jar a quand même été créé."
        exit 1
    fi

else
    echo "Erreur lors de la compilation. Le JAR n'a pas été généré."
    exit 1
fi
