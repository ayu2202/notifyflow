# NotifyFlow — Architecture Diagram

> Replace or supplement this with a PNG export at `diagrams/architecture.png` for the README image reference.

---

## Mermaid Diagram

Renderable on GitHub, GitLab, Notion, and any Mermaid-compatible viewer.

```mermaid
graph TD
    Client([🖥 Client]):::client -->|HTTP POST| GW[Gateway Service<br/>port 8083]:::service

    GW -->|Route /api/events| US[User Service<br/>port 8081]:::service
    GW -->|/demo/event via WebClient| US

    US -->|Persist| DB[(H2 Database)]:::storage
    US -->|Publish Event| RMQ{RabbitMQ<br/>port 5672}:::broker

    RMQ -->|Consume Event| NS[Notification Service<br/>port 8082]:::service
    NS -->|Log| LOG[✅ Notification Sent]:::output

    subgraph Observability
        direction LR
        PROM[Prometheus<br/>port 9090]:::obs
        GRAF[Grafana<br/>port 3000]:::obs
        ZIP[Zipkin<br/>port 9411]:::obs
    end

    GW -.->|Metrics| PROM
    US -.->|Metrics| PROM
    NS -.->|Metrics| PROM
    PROM -.->|Data Source| GRAF
    GW -.->|Traces| ZIP
    US -.->|Traces| ZIP
    NS -.->|Traces| ZIP

    classDef client fill:#e1f5fe,stroke:#0288d1
    classDef service fill:#e8f5e9,stroke:#388e3c
    classDef broker fill:#fff3e0,stroke:#f57c00
    classDef storage fill:#f3e5f5,stroke:#7b1fa2
    classDef obs fill:#fce4ec,stroke:#c62828
    classDef output fill:#e0f2f1,stroke:#00695c
```

---

## Text Diagram

For environments that do not render Mermaid:

```
                      ┌──────────────────────────────────────────────┐
                      │            NotifyFlow Platform               │
                      └──────────────────────────────────────────────┘

  ┌────────┐       ┌───────────────┐      ┌──────────────┐       ┌──────────────┐
  │        │ HTTP  │               │ HTTP │              │ AMQP  │              │
  │ Client │──────▶│    Gateway    │─────▶│    User      │──────▶│   RabbitMQ   │
  │        │       │    Service    │      │   Service    │       │              │
  └────────┘       │   (8083)     │      │   (8081)    │       └──────┬───────┘
                   └───────────────┘      └──────┬───────┘              │
                                                 │                     │ AMQP
                                                 ▼                     ▼
                                          ┌──────────────┐   ┌──────────────────┐
                                          │ H2 Database  │   │  Notification    │
                                          │ (in-memory)  │   │  Service (8082)  │
                                          └──────────────┘   └──────────────────┘

  ┌──────────────────────────────────────────────────────────────────────────────┐
  │                         Observability Layer                                  │
  │                                                                              │
  │   Prometheus (9090) ──▶ Grafana (3000)         Zipkin (9411)                 │
  │   Scrapes /actuator/prometheus                  Collects distributed traces  │
  │   from all services                             across all services          │
  └──────────────────────────────────────────────────────────────────────────────┘
```

---

## Data Flow Summary

```
Step  Action                                    Protocol
────  ──────────────────────────────────────    ────────
 1    Client sends POST /demo/event             HTTP
 2    Gateway transforms → POST /api/events     HTTP
 3    User Service persists event to H2         JDBC
 4    User Service publishes to events.exchange AMQP
 5    RabbitMQ routes to events.queue           AMQP
 6    Notification Service consumes message     AMQP
 7    Notification Service logs delivery        Log
```

---

## How to Create a PNG

Export a PNG from the Mermaid diagram above using any of:

- [Mermaid Live Editor](https://mermaid.live) — paste the diagram, click export PNG
- [draw.io](https://app.diagrams.net) — for custom styled diagrams
- VS Code extension: **Markdown Preview Mermaid Support**

Save the output to `diagrams/architecture.png` and the README will display it automatically.
