#!/bin/bash
sed -i '' "s/version := .*/version := \"${VERSION}\"/" build.sbt
sed -i '' "s/hackathon-backend:.*/hackathon-backend:${VERSION}/" docker-compose.yml
