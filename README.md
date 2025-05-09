# Small blogger service app

# Tecnologías y Herramientas

## Backend y Testing
- Java 17
- Spring Boot 3.x
- JUnit 5
- TestContainers con LocalStack
- Python con boto3

## Containerización y Orquestación
- Docker
- Docker Compose
- Amazon ECS (Fargate - Serverless)

## Base de Datos y Mensajería
- Amazon DynamoDB
    - Utilización de Global Secondary Indexes (GSI)
- Amazon SQS
    - Cola simple sin DLQ configurada

## Servicios AWS
- AWS Lambda (Python/boto3)
- AWS CloudWatch
- AWS Secrets Manager
- IAM
    - Usuario IAM dev (permisos desarrollo)
    - Usuario IAM admin dev

## CI/CD y Control de Versiones
- GitHub Actions
- GitHub

## Followers
### Create Follower

curl --location '[http://small-tweet-54411064.us-east-2.elb.amazonaws.com:8085/v1/followers](http://small-tweet-54411064.us-east-2.elb.amazonaws.com:8085/v1/followers)'
--header 'Content-Type: application/json'
--data '{ "author_username": "user22", "followed": "user2" }'

## Tweets
### Create Tweet

curl --location '[http://small-tweet-54411064.us-east-2.elb.amazonaws.com:8085/v1/tweets](http://small-tweet-54411064.us-east-2.elb.amazonaws.com:8085/v1/tweets)'
--header 'Content-Type: application/json'
--data '{ "username": "user1", "content": "full testing from my app" }'

## Timeline
### Get User Timeline

curl --location --request GET '[http://small-tweet-54411064.us-east-2.elb.amazonaws.com:8085/v1/timeline/user2](http://small-tweet-54411064.us-east-2.elb.amazonaws.com:8085/v1/timeline/user2)'
--header 'Content-Type: application/json'

## API Reference

| Endpoint | Method | Description                 | Request Body |
|----------|--------|-----------------------------|--------------|
| `/v1/followers` | POST | Crea relacion entre usuario | `{"author_username": string, "followed": string}` |
| `/v1/tweets` | POST | Crea un nuevo tweet         | `{"username": string, "content": string}` |
| `/v1/timeline/{username}` | GET | Obtiene user timeline       | - |

### Ejecucion local
Se puede ejecutar siguiendo estos pasos:
- mvn clean install -DskipTests, o sin esta ultima opcion si queremos ejecutar los tests
- Los test usan test container junit 5 y prueban desde el controlador usando localstack para simular servicios de aws
- Se puede hacer uso del docker compose presente en el root del proyecto haciendo 
docker-compose up --build

-Luego validar que este arriba con el health
http://localhost:8085/actuator/health

Como uno de los trade off descripto en la documentacion, el timeline es async y hace uso de un fs lambda en aws, por lo
que local sera complicado verificar ese ultimo endpoint, pero si se podra usar la version desplegada. Los endpoints para
crear seguidores y tweets si se podran ver local