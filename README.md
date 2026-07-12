# 🚀 TaskBuddy - Enterprise Full-Stack Task Management Application

TaskBuddy este o aplicație modernă de gestionare a task-urilor, dezvoltată pe o arhitectură full-stack decuplată (Monorepo). Proiectul integrează funcționalități inteligente bazate pe Inteligență Artificială (OpenAI), securitate robustă și un flux modern de DevOps complet containerizat, fiind pregătit din start pentru mediile de producție.

---

## 🏗️ Arhitectura Sistemului & DevOps

Proiectul este structurat ca un **Monorepo**, orchestrat complet nativ în containere izolate și automatizat prin mecanisme de Continuous Integration:

* **`/TaskBuddy`** - Server API RESTful dezvoltat în Spring Boot 3, securizat și integrat cu inteligență artificială.
* **`/taskbuddy-frontend`** - Interfață SPA modernă dezvoltată în React + TypeScript, servită printr-un proxy invers Nginx în mediul containerizat.
* **`CI/CD Pipeline`** - Validare automată a calității codului, tipizării TypeScript și asamblării Docker la fiecare Push/PR prin GitHub Actions.

---

## 🛠️ Stack-ul Tehnologic

### Backend
* **Java 21** (Mecanisme moderne și performante)
* **Spring Boot 3.x** (Spring Web, Spring Data JPA)
* **Spring Security & Basic Auth** (Protecție granulară pe rutele API)
* **OpenAI Service** (Integrare AI pentru ordonare inteligentă și descompunere de task-uri)
* **PostgreSQL** (Bază de date relațională robustă)
* **Maven** (Orchestrare dependențe)

### Frontend
* **React 19** & **TypeScript** (Cod predictibil, tipizat puternic și fără warning-uri)
* **Vite** (Tool de build și bundling ultra-rapid)
* **Nginx** (Proxy invers utilizat în Docker pentru rutare și eliminarea conflictelor CORS în producție)
* **Modern CSS3** (Design responsive, curat și fluid)

---

## ⚡ Pornirea Instantă în Prodezvoltare (Recomandat prin Docker)

Datorită containerizării complete, nu este nevoie să instalezi manual Java, Node.js sau PostgreSQL pe mașina ta locală. Totul pornește izolat și gata configurat.

### Pre-cerințe
* **Docker** și **Docker Compose** instalate.

### Pașii de pornire:

1.  **Clonează repository-ul:**
    ```bash
    git clone [https://github.com/utilizatorul-tau/taskBuddy-fullstack.git](https://github.com/utilizatorul-tau/taskBuddy-fullstack.git)
    cd taskBuddy-fullstack
    ```

2.  **Configurarea Variablelor de Mediu (`.env`):**
    Creează un fișier numit `.env` în rădăcina proiectului și adaugă secretele necesare:
    ```env
    DB_NAME=taskbuddy
    DB_USER=postgres
    DB_PASSWORD=parola_ta_securizata_aici
    OPENAI_API_KEY=sk-proj-CheiaTaRealaOpenAI
    ```

3.  **Lansarea aplicației:**
    Rulează comanda de orchestrare:
    ```bash
    docker compose up --build
    ```

🎉 **Gata!** Aplicația este orchestrată, rețelele interne Docker sunt legate, iar platforma este live:
* 🌐 **Interfața React (Frontend + Nginx Proxy):** Accesează direct [http://localhost](http://localhost) (Port implicit 80)
* ☕ **API REST (Backend):** Rulează securizat în spate pe portul `8080`, proxy-ul Nginx routând cererile din `/api/**` în mod transparent pentru a evita problemele de CORS.

---

## 🛠️ Metoda Alternativă: Rularea Manuală (Fără Docker)

Dacă dorești să rulezi serviciile separat în mod nativ pe sistemul de operare:

### 1. Baza de Date (PostgreSQL)
1. Intră în consola psql: `sudo -i -u postgres psql`
2. Rulează comenzile de inițializare:
   ```sql
   CREATE DATABASE taskbuddy;
   CREATE USER postgres WITH PASSWORD 'parola_ta';
   GRANT ALL PRIVILEGES ON DATABASE taskbuddy TO postgres;