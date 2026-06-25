# CHAT project

all files are present due to download and start for testing
functionality:

- all data stored in PostgreSQL (rooms, private chats, users, messages)
- authentication - users can create an account or use the login functionality with before created credentials
- messaging

## Docker

#### all related containers for run are present

`db - postgresql`
`amqp - rabbitmq`
`api - java backend`

#### Build the container command

```Bash
    docker compose up -build
```
