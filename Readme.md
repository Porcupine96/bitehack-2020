# CodeVillains (BITEhack 2020)

Team: Dawid Godek, Jakub Darul, Łukasz Kaźmierczak

# Functionality

Chatbot created for automating issue creation in ticketing systems (like `JIRA`).
It can be used by clients of software house who often don't provide enough
information for developers when creating bug issues.
We chose to use `Facebook Messenger` as communication channel, but application
could support more channels (`WhatsApp`, custom-build chat, `Slack`) in the future.

Chatbot during conversation with user extracts following pieces of information:

- issue name
- part of application where issue happened
- precondition
- expected behavior
- actual (erroneous) behavior
- priority of issue
- incident date
- issue description (additional)

Chatbot also has an ability to warn user if issue that is being created looks
like a duplicate.

## Screenshots

<img src="https://scontent-waw1-1.xx.fbcdn.net/v/t1.15752-9/s1080x2048/81667692_816359952172356_2271203891813023744_n.jpg?_nc_cat=104&_nc_ohc=8LU6cflD9nYAQkpdWri7gTNug3y95CzMPZx_UQRDi4FaOmTajhAkPoHkQ&_nc_ht=scontent-waw1-1.xx&_nc_tp=1&oh=050df00c493fbf481ee78071f22e9985&oe=5E911FD6" data-canonical-src="https://scontent-waw1-1.xx.fbcdn.net/v/t1.15752-9/s1080x2048/81667692_816359952172356_2271203891813023744_n.jpg?_nc_cat=104&_nc_ohc=8LU6cflD9nYAQkpdWri7gTNug3y95CzMPZx_UQRDi4FaOmTajhAkPoHkQ&_nc_ht=scontent-waw1-1.xx&_nc_tp=1&oh=050df00c493fbf481ee78071f22e9985&oe=5E911FD6" width="200" height="400" /> <img src="https://scontent-waw1-1.xx.fbcdn.net/v/t1.15752-9/s1080x2048/82048969_777197112777996_4431901247217336320_n.jpg?_nc_cat=100&_nc_ohc=XdnVG-O1bjIAQkx0KgiZ69NyAA48yy0pZPkh8YdD0UKqsbqJ6A73057gw&_nc_ht=scontent-waw1-1.xx&_nc_tp=1&oh=174c6bf27dc62557adca52499f09025a&oe=5E909585" data-canonical-src="https://scontent-waw1-1.xx.fbcdn.net/v/t1.15752-9/s1080x2048/82048969_777197112777996_4431901247217336320_n.jpg?_nc_cat=100&_nc_ohc=XdnVG-O1bjIAQkx0KgiZ69NyAA48yy0pZPkh8YdD0UKqsbqJ6A73057gw&_nc_ht=scontent-waw1-1.xx&_nc_tp=1&oh=174c6bf27dc62557adca52499f09025a&oe=5E909585" width="200" height="400" />  <img src="https://scontent-waw1-1.xx.fbcdn.net/v/t1.15752-0/p480x480/82021589_1064425523934174_8326629169492393984_n.jpg?_nc_cat=109&_nc_ohc=8oh-9X0SiiIAQnZTV_UfnoaVd2sL2MfraYkfr_K3aNsrRtIkOLCIzuqvQ&_nc_ht=scontent-waw1-1.xx&_nc_tp=1&oh=8f29a896dc9356609f0e3f6fedc788aa&oe=5E965A7E" data-canonical-src="https://scontent-waw1-1.xx.fbcdn.net/v/t1.15752-0/p480x480/82021589_1064425523934174_8326629169492393984_n.jpg?_nc_cat=109&_nc_ohc=8oh-9X0SiiIAQnZTV_UfnoaVd2sL2MfraYkfr_K3aNsrRtIkOLCIzuqvQ&_nc_ht=scontent-waw1-1.xx&_nc_tp=1&oh=8f29a896dc9356609f0e3f6fedc788aa&oe=5E965A7E" width="200" height="400" /> 



# Implementation:

Chatbot is implemented as 3 deployable services which correspond to 3 project
directories in our repository:

## hackathon-backend

### Overview

Service responsible for integration with `Facebook Messenger` and `JIRA`.
It controls conversation flow, uses [ner-service](#ner-service) to extract desired
pieces of information from messages. Exposes API used by webview in [bite-frontend](#bite-frontend)

### Technologies

- Scala
- Akka
- Akka Http

## ner-service

### Overview

Service responsible for extracting issue data from text.
Communicates with [hackathon-backend](#hackathon-backend) using gRPC.
Extracts data using NLP techniques, as well as simple heuristics. Uses word2vec
embeddings and cosine similarity to detect duplicates between issues.

### Technologies

- Python
- SpaCy

## bite-frontend

### Overview

Service serving webview for reviewing data extracted from conversation
before creating issue.

### Technologies

- ReasonML
- React
