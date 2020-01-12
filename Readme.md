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

![](https://scontent-waw1-1.xx.fbcdn.net/v/t1.15752-9/s1080x2048/82326365_2608140839282255_3412679742367727616_n.jpg?_nc_cat=100&_nc_oc=AQmNQpK4TeeTMayKs8ERfWEMmknOtj6aR1hXRD56YzyjhCHrJ8tY0TEtM4oiiTHPPbc&_nc_ht=scontent-waw1-1.xx&_nc_tp=1&oh=f9b22e5cab3f08e9896ec2bb5695e92c&oe=5EA1AAF7 =250x)

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
