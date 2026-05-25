# Guía de Despliegue

## Requisitos previos

- Cuenta en [Docker Hub](https://hub.docker.com/)
- Cuenta en [AWS](https://aws.amazon.com/)
- Instancia EC2 corriendo (Ubuntu 22.04 recomendado)
- Base de datos Oracle Cloud (Autonomous Database) creada
- Repositorio en GitHub con el código del proyecto

---

## 1. Configurar Oracle Cloud (Autonomous Database)

1. Ingresar a [Oracle Cloud Console](https://cloud.oracle.com/) y crear un **Autonomous Transaction Processing (ATP)** database.
2. Una vez creada la BD, hacer clic en **DB Connection** y descargar el **Wallet** (archivo ZIP).
3. Extraer el ZIP. Los archivos importantes son:
   - `tnsnames.ora` — contiene los nombres TNS de conexión
   - `cwallet.sso`, `ewallet.p12` — certificados de seguridad
4. Anotar el **TNS name** que aparece en `tnsnames.ora`. Ejemplo:

```
formativadb_high = (description= (retry_count=20)(retry_delay=3)...)
formativadb_low  = (description= ...)
```

   Usar el sufijo `_high` para la aplicación (más recursos) o `_low` (menor consumo de créditos).

5. Crear las credenciales de usuario en Oracle (schema con privilegios de creación de tablas).

### 1.1 Verificar el Wallet localmente (opcional pero recomendado)

Antes de subir el wallet a EC2, verificar que los archivos esenciales estén presentes:

```bash
ls /ruta/local/wallet/
# Debe mostrar al menos: cwallet.sso  ewallet.p12  ojdbc.properties  sqlnet.ora  tnsnames.ora
```

Confirmar que `sqlnet.ora` apunta a la carpeta correcta:

```ini
# sqlnet.ora — este valor se sobrescribe en runtime con WALLET_PATH
WALLET_LOCATION = (SOURCE = (METHOD = file)(METHOD_DATA = (DIRECTORY = "$(TNS_ADMIN)")))
SSL_SERVER_DN_MATCH = yes
```

> El driver Oracle lee la ubicación del wallet desde la variable de entorno `TNS_ADMIN`
> (que en `application.properties` se pasa como `WALLET_PATH`).
> No hace falta modificar `sqlnet.ora` manualmente.

---

---

## 1.5 Probar la conexión localmente (antes de deployar)

Si quieres probar la app en tu máquina con la BD real antes de subir a EC2:

1. Tener Java 21 y Maven instalados.
2. Exportar las variables de entorno apuntando al wallet descargado:

```bash
export DB_TNS_NAME=formativadb_high
export WALLET_PATH=/ruta/local/wallet
export DB_USERNAME=tu_usuario
export DB_PASSWORD=tu_contraseña
```

3. Compilar y ejecutar:

```bash
mvn clean package -DskipTests
java -jar target/formativa-cloud-0.0.1-SNAPSHOT.jar
```

4. Verificar que la app levante sin errores de conexión:

```bash
curl http://localhost:8080/api/cursos
# Debe responder [] o una lista de cursos (no un error 500)
```

---

## 2. Preparar la instancia EC2

### 2.1 Crear la instancia

1. Ir a **AWS Console → EC2 → Launch Instance**.
2. Seleccionar **Ubuntu Server 22.04 LTS** como AMI.
3. Elegir tipo de instancia (mínimo `t2.micro` para pruebas).
4. En **Security Group**, abrir los puertos:
   - **22** (SSH) — solo desde tu IP
   - **8080** (aplicación) — desde cualquier IP o solo desde tu IP
5. Crear o seleccionar un **Key Pair** (.pem) y guardarlo en lugar seguro.
6. Lanzar la instancia y copiar la **IP pública** (o DNS público).

### 2.2 Instalar Docker en EC2

Conectarse a la instancia vía SSH:

```bash
ssh -i tu-clave.pem ubuntu@<IP_PUBLICA_EC2>
```

Instalar Docker:

```bash
sudo apt-get update
sudo apt-get install -y docker.io
sudo systemctl enable docker
sudo systemctl start docker
# agregar el usuario ubuntu al grupo docker para no usar sudo siempre
sudo usermod -aG docker ubuntu
# aplicar el cambio de grupo sin cerrar sesion
newgrp docker
```

### 2.3 Subir el Wallet de Oracle a EC2

Desde tu máquina local, copiar los archivos del Wallet:

```bash
# crear el directorio en EC2
ssh -i tu-clave.pem ubuntu@<IP_PUBLICA_EC2> "sudo mkdir -p /opt/oracle/wallet && sudo chown ubuntu:ubuntu /opt/oracle/wallet"

# copiar el contenido del wallet
scp -i tu-clave.pem /ruta/local/wallet/* ubuntu@<IP_PUBLICA_EC2>:/opt/oracle/wallet/
```

---

## 3. Configurar Docker Hub

1. Crear una cuenta en [Docker Hub](https://hub.docker.com/) si no tienes una.
2. Ir a **Account Settings → Security → New Access Token**.
3. Crear un token con permisos de lectura/escritura y guardarlo (no se muestra de nuevo).

---

## 4. Configurar los Secrets en GitHub

Ir al repositorio en GitHub → **Settings → Secrets and variables → Actions → New repository secret** y agregar:

| Nombre del Secret    | Valor                                                                       |
|----------------------|-----------------------------------------------------------------------------|
| `DOCKERHUB_USERNAME` | Tu usuario de Docker Hub                                                    |
| `DOCKERHUB_TOKEN`    | El token de acceso creado en Docker Hub                                     |
| `EC2_HOST`           | IP pública de tu instancia EC2                                              |
| `EC2_USER`           | `ubuntu` (o el usuario que uses en tu instancia)                            |
| `EC2_SSH_KEY`        | Contenido completo del archivo `.pem` (incluye las líneas `-----BEGIN...`)  |
| `DB_TNS_NAME`        | El TNS name de Oracle (ej. `formativadb_high`)                              |
| `DB_USERNAME`        | Usuario de la base de datos Oracle                                          |
| `DB_PASSWORD`        | Contraseña del usuario de la base de datos Oracle                           |

> **Nota sobre `WALLET_PATH`:** no hace falta agregarlo como secret. El workflow ya pasa
> `-e WALLET_PATH=/app/wallet` fijo al contenedor, que coincide con el volumen montado
> en `-v /opt/oracle/wallet:/app/wallet`.

---

## 5. Hacer el primer deploy

1. Asegurarse de que el código esté en la rama `main`.
2. Hacer un push a `main`:

```bash
git add .
git commit -m "primer deploy"
git push origin main
```

3. Ir a **GitHub → Actions** y observar cómo corren los dos jobs:
   - `Build Docker Image y Push a Docker Hub`
   - `Deploy en EC2`

4. Si todo sale bien, la aplicación estará disponible en:

```
http://<IP_PUBLICA_EC2>:8080/api/cursos
```

---

## 6. Verificar que la aplicación esté corriendo

Conectarse a EC2 y revisar el contenedor:

```bash
ssh -i tu-clave.pem ubuntu@<IP_PUBLICA_EC2>
docker ps                          # el contenedor debe aparecer como "Up"
docker logs formativa-cloud        # buscar "Started FormativaCloudApplication"
docker logs formativa-cloud 2>&1 | grep -i "error\|exception"  # filtrar errores
```

Probar el endpoint desde EC2 mismo (descarta problemas de red/firewall):

```bash
curl -s http://localhost:8080/api/cursos
```

---

## 7. Endpoints disponibles

| Método | URL                      | Descripción                          |
|--------|--------------------------|--------------------------------------|
| GET    | `/api/cursos`            | Lista todos los cursos disponibles   |
| POST   | `/api/cursos`            | Agrega un nuevo curso                |
| POST   | `/api/inscripciones`     | Inscribe un estudiante en cursos     |

### Ejemplo: Agregar un curso

```bash
curl -X POST http://<IP>:8080/api/cursos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Java con Spring Boot",
    "instructor": "Ana García",
    "duracion": 40,
    "costo": 150.00
  }'
```

### Ejemplo: Inscribir estudiante

```bash
curl -X POST http://<IP>:8080/api/inscripciones \
  -H "Content-Type: application/json" \
  -d '{
    "estudianteId": 1,
    "cursoIds": [1, 2]
  }'
```

---

## 8. Troubleshooting frecuente

### La app no conecta a Oracle (`ORA-` en los logs)

```bash
docker logs formativa-cloud 2>&1 | grep -i "ORA-\|TNS-\|wallet"
```

| Error | Causa probable | Solución |
|-------|---------------|----------|
| `TNS:could not resolve the connect identifier` | `DB_TNS_NAME` mal escrito | Verificar el nombre exacto en `tnsnames.ora` |
| `ORA-01017: invalid username/password` | Credenciales incorrectas | Revisar `DB_USERNAME` y `DB_PASSWORD` en secrets |
| `IO Error: Invalid wallet location` | Wallet no montado | Verificar que `/opt/oracle/wallet` en EC2 no está vacío |
| `Could not open Wallet` | Archivos del wallet corruptos | Descargar el wallet de nuevo desde Oracle Cloud |

### Verificar que el wallet está en EC2

```bash
ssh -i tu-clave.pem ubuntu@<IP_PUBLICA_EC2>
ls -la /opt/oracle/wallet/
# Debe mostrar: cwallet.sso  ewallet.p12  ojdbc.properties  sqlnet.ora  tnsnames.ora
```

### Reiniciar el contenedor manualmente

```bash
docker stop formativa-cloud && docker rm formativa-cloud
# Luego volver a ejecutar el docker run del paso de deploy
```
