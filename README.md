# Hiring project

The project has an Angular frontend and a Spring Boot backend.

For the CI pipeline, Docker setup, test commands, and Git workflow, see
[docs/DEVOPS.md](docs/DEVOPS.md).

## Run the full stack with Docker

Requirements: Docker Desktop with Docker Compose enabled.

```powershell
docker compose up --build -d
docker compose ps
```

Open the frontend at <http://localhost:4200>. Spring Boot is available at
<http://localhost:8989>; MySQL and MongoDB use persistent Docker volumes.
The Compose JWT key and database password are local-development defaults only.
Set real values in a local `.env` file before sharing or deploying this stack.

Stop the containers and keep the database files:

```powershell
docker compose down
```

To also erase the local test databases, run `docker compose down -v`.

Optional integrations can be enabled in `.env` with `GEMINI_API_KEY`,
`MAIL_USERNAME`, `MAIL_PASSWORD`, and the `LINKEDIN_*` variables documented
below. Never commit `.env` or real credentials.

## Tests and CI pipeline

Run backend tests:

```powershell
cd hiring
./mvnw.cmd test
```

Run frontend tests and a production build:

```powershell
cd front/architectui-angular-theme-free
npm ci --legacy-peer-deps
npm run test:ci
npm run build -- --configuration production
```

`test:ci` runs the focused recruitment dashboard tests. The project also has
older generated component specs that still need dependency mocks before they
can be included in the CI test set.

GitHub Actions runs the backend tests, Angular tests, production frontend build,
then builds both Docker images for each push and pull request targeting
`main`, `master`, or `develop`. It checks the images but does not publish them
or deploy the application.

## Suggested Git workflow for the PFE

Create a short-lived feature branch for each change, then push it and open a
pull request to `develop` (or `main` if the repository has no `develop` branch):

```powershell
git checkout -b feature/nom-de-la-fonction
git add .
git commit -m "feat: describe the change"
git push -u origin feature/nom-de-la-fonction
```

Merge only after the **CI** checks pass. Use `fix:` for corrections and
`docs:` for documentation changes. Keep API keys, passwords, `.env`, and
generated CV files out of Git.

## Backend configuration

Run the backend from `hiring`. It reads database and email credentials from environment variables. Local MySQL defaults remain `localhost:3306/hiring1` with user `root` and no password; override them with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` when needed.

Set a private Base64 JWT key before starting the backend. In PowerShell, generate one with:

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET_BASE64 = [Convert]::ToBase64String($bytes)
$rng.Dispose()
```

Set `MAIL_USERNAME` and `MAIL_PASSWORD` to the SMTP account and app password when email delivery is needed. Then start Spring Boot:

For AI-assisted CV drafting, set `GEMINI_API_KEY` on the backend. The model defaults to `gemini-3.8-flash`; override it with `GEMINI_MODEL` if needed. The candidate's saved CV details are sent to Gemini to rewrite experience, project, skill, and education content. The generated draft remains editable and must be reviewed and validated by the candidate before applying.

## LinkedIn offer sharing

The admin offer list can publish an organic post to the logged-in administrator's personal LinkedIn feed. The post includes a public URL to the offer detail page, where candidates can apply in this app. Configure `LINKEDIN_CLIENT_ID`, `LINKEDIN_CLIENT_SECRET`, `LINKEDIN_REDIRECT_URI`, and `PUBLIC_APP_URL`; optionally set `LINKEDIN_API_VERSION` (default `202609`). Register the exact callback URL `https://<public-api-host>/api/integrations/linkedin/callback` in the LinkedIn Developer application, enable the required OpenID Connect scopes and `w_member_social` permission, and ensure the LinkedIn account grants consent. LinkedIn API access and scopes depend on the products approved for that Developer application.

This integration shares a link in the member's feed; it does not create a native LinkedIn Jobs listing. LinkedIn restricts its Job Posting API to approved partners. See [LinkedIn Posts API](https://learn.microsoft.com/en-us/linkedin/marketing/community-management/shares/posts-api?view=li-lms-2026-09), [OAuth authorization code flow](https://learn.microsoft.com/en-us/linkedin/shared/authentication/authorization-code-flow), and [Job Posting API access requirements](https://learn.microsoft.com/en-us/linkedin/talent/job-postings/api/overview?view=li-lts-2025-10).

```powershell
cd hiring
.\mvnw.cmd spring-boot:run
```

Keep the JWT key and mail credentials private. Save the generated key securely and reuse it on later launches; changing it invalidates existing login tokens. Use the same JWT key for every backend instance that needs to accept each other's tokens.

## Frontend

```powershell
cd front/architectui-angular-theme-free
npm install
npm start
```
