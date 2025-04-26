# ![RealWorld Example App](logo.png)

> ### [Ktor](https://github.com/ktorio/ktor/) codebase containing real world examples (CRUD, auth, advanced patterns, etc) that adheres to the [RealWorld](https://github.com/gothinkster/realworld) spec and API.

### [Demo](https://demo.realworld.io/)&nbsp;&nbsp;&nbsp;&nbsp;[RealWorld](https://github.com/gothinkster/realworld)

This codebase was created to demonstrate a fully fledged fullstack application built with *
*[Ktor](https://github.com/ktorio/ktor/)** including
CRUD operations, authentication, routing, pagination, and more.

We've gone to great lengths to adhere to the **[Ktor](https://github.com/ktorio/ktor/)** community styleguides & best
practices.

For more information on how to this works with other frontends/backends, head over to
the [RealWorld](https://github.com/gothinkster/realworld) repo.

# How it works

## Features

Here's a list of features included in this project:

| Name                                                                   | Description                                                                        |
|------------------------------------------------------------------------|------------------------------------------------------------------------------------|
| [Routing](https://start.ktor.io/p/routing)                             | Provides a structured routing DSL                                                  |
| [Status Pages](https://start.ktor.io/p/status-pages)                   | Provides exception handling for routes                                             |
| [Default Headers](https://start.ktor.io/p/default-headers)             | Adds a default set of headers to HTTP responses                                    |
| [Call Logging](https://start.ktor.io/p/call-logging)                   | Logs client requests                                                               |
| [Koin](https://start.ktor.io/p/koin)                                   | Provides dependency injection                                                      |
| [kotlinx.serialization](https://start.ktor.io/p/kotlinx-serialization) | Handles JSON serialization using kotlinx.serialization library                     |
| [Content Negotiation](https://start.ktor.io/p/content-negotiation)     | Provides automatic content conversion according to Content-Type and Accept headers |
| [Exposed](https://start.ktor.io/p/exposed)                             | Adds Exposed database to your application                                          |
| [Jackson](https://start.ktor.io/p/ktor-jackson)                        | Handles JSON serialization using Jackson library                                   |
| [Authentication](https://start.ktor.io/p/auth)                         | Provides extension point for handling the Authorization header                     |
| [Authentication JWT](https://start.ktor.io/p/auth-jwt)                 | Handles JSON Web Token (JWT) bearer authentication scheme                          |
| [CORS](https://start.ktor.io/p/cors)                                   | Enables Cross-Origin Resource Sharing (CORS)                                       |

# Getting started

## Building & Running

To build or run the project, use one of the following tasks:

| Task                          | Description                                                          |
|-------------------------------|----------------------------------------------------------------------|
| `./gradlew test`              | Run the tests                                                        |
| `./gradlew build`             | Build everything                                                     |
| `buildFatJar`                 | Build an executable JAR of the server with all dependencies included |
| `buildImage`                  | Build the docker image to use with the fat JAR                       |
| `publishImageToLocalRegistry` | Publish the docker image locally                                     |
| `run`                         | Run the server                                                       |
| `runDocker`                   | Run using the local docker image                                     |

If the server starts successfully, you'll see the following output:

```
2024-12-04 14:32:45.584 [main] INFO  Application - Application started in 0.303 seconds.
2024-12-04 14:32:45.682 [main] INFO  Application - Responding at http://0.0.0.0:8080
```
