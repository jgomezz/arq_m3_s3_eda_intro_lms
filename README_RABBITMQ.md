# MIGRACIÓN A RABBITMQ

- Workflow

<img src="images/rabbitmq_workflow.png" alt="RabbitMQ" />

- Clases a modificar

<img src="images/rabbitmq_class.png" alt="RabbitMQ" />

## **I.- Creación del servidor de RabbitMQ**

1. Requisitos : Tener instalado Docker Desktop : https://www.docker.com/products/docker-desktop/


2. Crear el docker compose para RabbitMQ : docker-compose.yml

docker-compose.yml

```yaml
services:
  rabbitmq:
    image: rabbitmq:3-management
    container_name: lms-rabbitmq
    ports:
      - "5672:5672"       # Puerto para conexiones AMQP
      - "15672:15672"     # Puerto para la interfaz de administración
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin123
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
volumes:
  rabbitmq_data:
```