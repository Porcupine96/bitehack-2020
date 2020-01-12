# Chatbot for JIRA

# Functionality

## Target audience

## TODO

TODO

## Screenshots

TODO

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

## bite-frontend
