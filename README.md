# FOR UPC - Portal Informativo Académico (Backend)

API RESTful desarrollada para gestionar el Portal Informativo Académico de la Universidad Provincial de Córdoba (Sede Laboulaye). El sistema maneja la base de datos, la autenticación de usuarios y la lógica de negocio para dos roles principales: estudiantes (consumen el feed de novedades) y administradores (gestionan usuarios y publican anuncios).

##  Tecnologías Utilizadas

* **Lenguaje y Framework:** Java 17+ con Spring Boot 3
* **Seguridad:** Spring Security con JWT (JSON Web Tokens)
* **Base de Datos:** PostgreSQL (alojada en Supabase)
* **Documentación:** Swagger (Springdoc OpenAPI)
* **Testing:** JUnit 5, MockMvc y base de datos H2 (en memoria)


## Enlaces del proyecto

- **Frontend (Vercel):** https://for-upc-frontend.vercel.app/
- **Backend (Render):** https://for-upc-backend.onrender.com/
- **Documentación API (Swagger UI):** https://for-upc-backend.onrender.com/swagger-ui/index.html
- **Especificación OpenAPI (JSON):** https://for-upc-backend.onrender.com/v3/api-docs

### Documentación de la API

La API REST de For-UPC está documentada mediante Swagger UI y OpenAPI 3.1.

Permite visualizar los endpoints disponibles, sus métodos HTTP, los parámetros de entrada y las respuestas.

La API utiliza autenticación JWT y distingue los roles ESTUDIANTE y ADMINISTRADOR.

[Acceder a la documentación interactiva de Swagger](https://for-upc-backend.onrender.com/swagger-ui/index.html)


##  Usuarios de Prueba

Para probar los endpoints protegidos en Swagger o en el sistema frontend, utilizar las siguientes credenciales:

* **Administrador:**
  * Email: [email_admin_prueba]
  * Contraseña: [password_admin_prueba]

* **Estudiante:**
  * Email: [email_estudiante_prueba]
  * Contraseña: [password_estudiante_prueba]

##  Cómo correr el proyecto en local

1. Clonar el repositorio y entrar a la carpeta:
git clone https://github.com/[usuario]/[repositorio-back].git
cd For-UPC-backend

2. Configurar Variables de Entorno:
Las credenciales no se suben al repositorio por seguridad. Se deben configurar como variables de entorno en el sistema o en el archivo de configuración local (`application.properties`).
Ejemplos de variables requeridas (sin valores reales):
DB_URL=jdbc:postgresql://aws-0-sa-east-1.pooler.supabase.com:6543/postgres
DB_USER=postgres.[tu_usuario]
DB_PASSWORD=[tu_password_de_supabase]
JWT_SECRET=[clave]
APP_FRONTEND_URL=http://localhost:5173

3. Ejecutar la aplicación:
Utilizar el IDE (IntelliJ IDEA / Eclipse) para ejecutar la clase principal, o correr el siguiente comando en la terminal:
mvn spring-boot:run

El servidor backend estará disponible en http://localhost:8080.

##  Pruebas Automatizadas (JUnit y MockMvc)

El proyecto cuenta con pruebas automáticas para verificar la seguridad, la generación del token y el control de acceso por roles. Los tests se ejecutan en un entorno aislado usando una base de datos en memoria (H2).

Para correr la batería de pruebas, ejecutar el siguiente comando en la consola:
mvn test
