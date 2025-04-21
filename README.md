# Scala GraphQL Oracle Integration

A secure GraphQL server built with Scala, Akka HTTP, and Oracle database integration, featuring Okta authentication and Apache Camel for ETL operations.

## Features

- GraphQL API with Sangria implementation
- Secure authentication using Okta
- Oracle database integration
- Apache Camel for ETL operations with parallel processing
- Akka HTTP server running on port 4444
- Spray JSON for request/response handling

## Prerequisites

- Scala 2.13.12
- sbt 1.10.11
- Oracle Database (tested with Oracle 21c)
- Okta Developer Account
- Java Azul Zulu 21 (https://www.azul.com/downloads/?version=java-21-lts&package=jdk)

## Configuration

The application is configured through `src/main/resources/application.conf`:

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
  client-id = "your-client-id"
  client-secret = "your-client-secret"
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

## Getting Started

1. Clone the repository
2. Update the configuration in `application.conf` with your Oracle and Okta credentials
3. Run the application:

```bash
sbt run
```

The server will start on `http://localhost:4444`

## GraphQL API

### Example Query

```graphql
{
  hello
}
```

Response:
```json
{
  "data": {
    "hello": "Hello, GraphQL!"
  }
}
```

## Project Structure

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

## Dependencies

- Akka HTTP 10.5.3
- Sangria GraphQL 4.0.2
- Okta SDK 3.0.0
- Oracle JDBC 21.8.0.0
- Apache Camel 3.20.5
- Circe 0.14.6

## Security

The application uses Okta for authentication. All GraphQL requests must include a valid authentication token in the header.

## ETL Operations

Apache Camel is used for ETL operations with parallel processing support. The ETL routes are configured in `application.conf`.

## License

This project is licensed under the MIT License - see the LICENSE file for details. 