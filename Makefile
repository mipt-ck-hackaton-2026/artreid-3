.PHONY: help build clean test run run-reload docker-up docker-down docker-logs docker-build

# Default target
.DEFAULT_GOAL := help

help: ## Show this help message
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build the project using Gradle (skips tests)
	./gradlew build -x test

clean: ## Clean the Gradle build directory
	./gradlew clean

test: ## Run unit and integration tests
	./gradlew test

run: ## Run the Spring Boot application locally
	./gradlew bootRun

run-reload: ## Run the Spring Boot application with continuous build (auto-reload)
	./gradlew bootRun -t

docker-up: ## Start the Docker Compose environment in the background
	docker compose up -d

docker-down: ## Stop and remove the Docker Compose environment
	docker compose down

docker-logs: ## Tail the logs of the Docker Compose environment
	docker compose logs -f

docker-restart: docker-down docker-up ## Restart the Docker Compose environment

docker-build: ## Build or rebuild the Docker Compose services
	docker compose build

all: clean build test docker-build docker-up ## Clean, build, test, and start the Docker environment
