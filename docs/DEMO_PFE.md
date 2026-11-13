# Guide de démonstration du projet Hiring

## Objectif fonctionnel

La plateforme accompagne une candidature depuis la consultation d’une offre
jusqu’à la décision du recruteur. Le candidat complète son dossier, passe un
quiz si le recruteur l’y invite, puis peut être convoqué à un entretien s’il
obtient au moins 70 %.

## Déroulement conseillé de la démonstration

1. **Recruteur : créer une offre et son quiz.** Depuis l’espace administrateur,
   créer un quiz, puis une offre qui lui est associée.
2. **Candidat : s’inscrire à l’offre.** Dans la liste des offres, ouvrir une
   offre et utiliser le bouton d’inscription. Créer un compte candidat.
3. **Candidat : préparer son dossier.** Compléter les coordonnées et le CV,
   enregistrer les expériences, les compétences, les projets et les études,
   puis valider le CV.
4. **Recruteur : examiner la candidature.** Dans la page des candidats, ouvrir
   le bouton **Dossier** pour consulter le profil et le CV. Accepter le candidat
   pour le quiz.
5. **Candidat : passer le quiz.** Se connecter à son compte, ouvrir le quiz
   accepté et soumettre les réponses. Un score d’au moins 70 % valide l’étape.
6. **Recruteur : planifier l’entretien.** La candidature réussie apparaît dans
   le tableau. Choisir une date et une heure, puis confirmer l’entretien.
7. **Candidat : consulter son avancement.** Le profil affiche le statut de la
   candidature et les prochaines étapes.

Pour une démonstration fluide, préparer une offre ouverte avec un quiz, un
compte admin et un compte candidat neuf. Une candidature déjà utilisée peut
avoir des étapes déjà terminées.

## Architecture technique

- **Frontend :** application Angular 11. Elle affiche les offres, les écrans
  d’inscription et de connexion, le profil candidat, les quiz et le tableau
  administrateur.
- **Backend :** API REST Spring Boot 3.4 en Java 17. Elle applique les règles
  métier, contrôle les accès par rôles et émet des jetons JWT à la connexion.
- **MySQL :** comptes, rôles, données structurées de CV et progression du
  candidat.
- **MongoDB :** offres, quiz, réponses et candidatures.

Quelques routes représentatives :

- `POST /post/{postId}/register` crée un compte candidat associé à une offre.
- `PUT /api/profile/update-cv` enregistre le CV structuré du candidat.
- `POST /api/profile/validate-cv` valide son CV.
- `POST /api/progress/accept-quiz/{userId}/{postId}` autorise le candidat à
  passer le quiz.
- `POST /api/quizzes/submit` enregistre les réponses et le score.
- `PATCH /api/admin/candidatures/{id}/accept` programme l’entretien d’une
  candidature admissible.

Les routes d’administration exigent un JWT avec le rôle `ADMIN`. Les actions
du candidat exigent un compte `CANDIDATE`. Le backend conserve l’application
et son score ; Angular présente l’état courant et appelle les routes métier.

## Tests et chaîne DevOps

Le workflow GitHub Actions (`.github/workflows/ci.yml`) démarre lors d’un push
sur `master`, `main`, `develop` ou `feature/**`, ainsi qu’à l’ouverture d’une
Pull Request. Il exécute les tests Maven, trois tests Angular ciblés, le build
Angular de production, puis construit les images Docker backend et frontend.
La construction d’images vérifie leur packaging ; elle ne les publie pas dans
un registre et ne déploie pas l’application.

Pour démarrer les quatre services localement (Angular, Spring Boot, MySQL et
MongoDB), depuis la racine du projet :

```powershell
docker compose up --build -d
```

L’application est ensuite disponible sur <http://localhost:4200>. Les valeurs
secrètes d’intégration (Gemini, SMTP et LinkedIn) sont facultatives pour le
parcours de base et doivent être configurées dans un fichier `.env` local pour
activer ces fonctions. Ne pas utiliser les valeurs locales de démonstration
en production.
