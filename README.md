### 시스템 구성도

```mermaid
flowchart LR
    A[Client<br/>React] -->|HTTP / JSON| B[API Server<br/>Spring Boot]
    B -->|JPA| C[(Database<br/>MySQL / H2)]
    B -->|Image Upload| D[(AWS S3)]
