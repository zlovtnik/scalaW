# Integração Scala GraphQL com Oracle

Um servidor GraphQL seguro construído com Scala, Akka HTTP e integração com banco de dados Oracle, com autenticação Okta e Apache Camel para operações ETL.

## Funcionalidades

- API GraphQL com implementação Sangria
- Autenticação segura usando Okta
- Integração com banco de dados Oracle
- Apache Camel para operações ETL com processamento paralelo
- Servidor Akka HTTP rodando na porta 4444
- Spray JSON para manipulação de requisições/respostas

## Pré-requisitos

- Scala 2.13.12
- sbt 1.10.11
- Oracle Database (testado com Oracle 21c)
- Conta de Desenvolvedor Okta
- Java Azul Zulu 21 (https://www.azul.com/downloads/?version=java-21-lts&package=jdk)

## Configuração

A aplicação é configurada através do arquivo `src/main/resources/application.conf`:

```hocon
akka {
  http {
    server {
      port = 4444
    }
  }
}

oracle {
  url = "jdbc:oracle:thin:@//localhost:1521/ORCLPDB"
  username = "system"
  password = "oracle"
  pool {
    max-connections = 10
    min-connections = 2
  }
}

okta {
  org-url = "https://dev-123456.okta.com"
  client-id = "seu-client-id"
  client-secret = "seu-client-secret"
}

camel {
  routes = [
    {
      id = "etl-route"
      from = "direct:start"
      to = "jdbc:oracle"
      parallel-processing = true
    }
  ]
}
```

## Começando

1. Clone o repositório
2. Atualize a configuração no `application.conf` com suas credenciais do Oracle e Okta
3. Execute a aplicação:

```bash
sbt run
```

O servidor será iniciado em `http://localhost:4444`

## API GraphQL

### Exemplo de Consulta

```graphql
{
  hello
}
```

Resposta:
```json
{
  "data": {
    "hello": "Olá, GraphQL!"
  }
}
```

## Estrutura do Projeto

```
src/
├── main/
│   ├── resources/
│   │   └── application.conf
│   └── scala/
│       └── com/
│           └── example/
│               ├── config/
│               │   ├── DatabaseConfig.scala
│               │   └── OktaConfig.scala
│               ├── etl/
│               │   └── CamelETL.scala
│               ├── graphql/
│               │   ├── GraphQLServer.scala
│               │   └── SchemaDefinition.scala
│               ├── security/
│               │   └── OktaAuthentication.scala
│               └── Main.scala
└── test/
    └── scala/
        └── com/
            └── example/
                └── MainSpec.scala
```

## Dependências

- Akka HTTP 10.5.3
- Sangria GraphQL 4.0.2
- Okta SDK 3.0.0
- Oracle JDBC 21.8.0.0
- Apache Camel 3.20.5
- Circe 0.14.6

## Segurança

A aplicação utiliza Okta para autenticação. Todas as requisições GraphQL devem incluir um token de autenticação válido no cabeçalho.

## Operações ETL

O Apache Camel é utilizado para operações ETL com suporte a processamento paralelo. As rotas ETL são configuradas no `application.conf`.

## Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo LICENSE para detalhes. 