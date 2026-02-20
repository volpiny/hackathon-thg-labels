#!/bin/bash

# Ensure we're in the project root
cd /Users/volpiny/Desktop/thg-label-manager

# Start the Docker containers (Database, Storage, and Backend)
echo "Starting Docker containers..."
docker-compose up --build -d

echo "Wait for containers to initialize..."
sleep 5

echo "Check container status:"
docker-compose ps

echo "------------------------------------------------"
echo "Backend: http://localhost:8080"
echo "Database: localhost:5432"
echo "MinIO Console: http://localhost:9001"
echo "------------------------------------------------"
echo "To run the Angular frontend locally:"
echo "cd frontend/label-manager-ui"
echo "npm install && npm start"
