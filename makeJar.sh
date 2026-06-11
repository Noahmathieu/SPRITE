#!/bin/bash
#script pour compiler le projet et faire un jar sprite pour envoyer a tomcat

# Modifiez ce chemin pour pointer vers le servlet-api.jar de votre Tomcat
TOMCAT_LIB="/home/noah/tomcat/lib/servlet-api.jar"

# On utilise find pour récupérer les fichiers .java dans les sous-dossiers (comme GetServlet/Getlink.java)
javac -cp "$TOMCAT_LIB" -d bin $(find src -name "*.java")

    # Envoyer le sprite.jar vers /mnt/storage/snapd/S4/Web Dynamique/AppTEST
    cp sprite.jar /mnt/storage/snapd/S4/Web\ Dynamique/AppTEST/lib

# Si la compilation a réussi
if [ $? -eq 0 ]; then
    cd bin
    # Création du JAR sans classe Main car c'est une servlet
    jar cf ../sprite.jar *
    cd ..
    echo "Le projet a été compilé et le fichier sprite.jar a été créé."
else
    echo "Erreur lors de la compilation."
fi