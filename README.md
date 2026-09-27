# SPRITE
Projet Spring MVC framework

Envoyer des API comme des API REST via JSON dans le controller 
- Creer nouvelle annotation @APIREST
- checker s' il existe ou pas
- Utiliser l'annotation @APIREST sur les méthodes du controller pour les marquer comme des endpoints API REST
- Valeur de retour de la méthode du controller sera automatiquement convertie en JSON si c'est string sinon retour Object sera converti en JSON via Jackson