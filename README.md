# TaskBuddy - Full-Stack Task Management Application

TaskBuddy este o aplicație modernă de gestionare a task-urilor, dezvoltată cu o arhitectură decuplată (Monorepo), având un backend robust în Java și un frontend interactiv și rapid în React.

Proiectul este conceput respectând bunele practici din industrie, fiind pregătit pentru extinderea către funcționalități avansate.

## 🏗️ Arhitectura Proiectului

Aplicația este structurată sub formă de **Monorepo**, împărțită în două module principale:
* `/backend` - Serverul API dezvoltat în Spring Boot 3 și conectat la o bază de date relațională PostgreSQL.
* `/frontend` - Interfața grafică (SPA) dezvoltată în React 19 utilizând Vite pentru build și optimizare.

## 🛠️ Tehnologii Utilizate

### Backend
* **Java 17+**
* **Spring Boot 3.x** (Spring Web, Spring Data JPA)
* **PostgreSQL** (Bază de date relațională)
* **Maven** (Gestionarea dependențelor)

### Frontend
* **React 19**
* **TypeScript** (Pentru un cod sigur și tipizat puternic)
* **Vite** (Tool de dezvoltare ultra-rapid)
* **CSS3** (Stilizare modernă și responsive)

## 🚀 Pornirea Proiectului în Dezvoltare Locală

### Pre-cerințe
* Java 17 sau mai nou instalat
* Node.js (v18+) și npm
* PostgreSQL rulând local

### Pregătirea Bazei de Date
1. Rulează în terminal următoarea comandă pentru a intra în consola PostgreSQL:
```sudo -i -u postgres psql```
2. Acum, copiază și rulează aceste 3 comenzi SQL (apasă Enter după fiecare);
```CREATE DATABASE taskbuddy_db;```
```CREATE USER taskbuddy_user WITH PASSWORD 'pune_parola_ta_aici';```
```GRANT ALL PRIVILEGES ON DATABASE taskbuddy_db TO pune_un_nume_userului_aici;```
3. Pentru a ieși din consola PostgreSQL, tastează: `\q`


### 1. Rularea Backend-ului (Spring Boot)
1. Navighează în folderul backend:
   `cd TaskBuddy`
   2. Seteaza environment variable la tine in sistemul de operare (user-ul si parola create la punctul 2 de la baza de date):
       ```echo 'export DB_PASSWORD="parola_ta_reala_aici"' >> ~/.bashrc
            source ~/.bashrc
       ```
      ```echo 'export DB_USERNMAE="usernameul_tau_real_aici"' >> ~/.bashrc
            source ~/.bashrc
       ```
3. Lansează serverul Spring Boot în execuție:
   `./mvnw spring-boot:run`
## Serverul va porni pe portul 8080

### 2. Rularea Frontend-ului (React)
1. Deschide un terminal nou în rădăcina proiectului și accesează folderul de frontend:
    `cd taskbuddy-frontend`
2. Instalează modulele Node.js necesare:
    `npm install`
3. Lansează serverul de dezvoltare în mod reactiv:
    `npm run dev`
### Aplicatia va fi accesibila in broswer la http://localhost:5173
