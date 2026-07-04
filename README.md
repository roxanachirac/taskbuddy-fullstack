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

### 1. Rularea Backend-ului (Spring Boot)
1. Navighează în folderul backend:
   `cd TaskBuddy`
2. Deschide fișierul src/main/resources/application.properties și configurează conexiunea la baza ta de date locală:
    ```spring.datasource.url=jdbc:postgresql://localhost:5432/taskbuddy_db
        spring.datasource.username=utilizatorul_tau
        spring.datasource.password=parola_ta
        spring.jpa.hibernate.ddl-auto=update```
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
