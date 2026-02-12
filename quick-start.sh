#!/bin/bash

# Test Data Management Tool - Quick Start Script

set -e

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║     Test Data Management Tool - Quick Start                   ║"
echo "║     AI-Powered Test Data Generation with Spring Boot          ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Check prerequisites
echo "📋 Checking prerequisites..."

# Check Java
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 17 or higher."
    exit 1
fi

JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi
echo "✅ Java $JAVA_VERSION found"

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven not found. Please install Maven 3.6 or higher."
    exit 1
fi
echo "✅ Maven found"

# Check for OpenAI API key
if [ -z "$OPENAI_API_KEY" ]; then
    echo ""
    echo "⚠️  OpenAI API key not found in environment."
    echo "   You have two options:"
    echo ""
    echo "   1. Use OpenAI (Recommended for best results):"
    echo "      export OPENAI_API_KEY='your-api-key-here'"
    echo ""
    echo "   2. Use Ollama (Free, local LLM):"
    echo "      Install: curl https://ollama.ai/install.sh | sh"
    echo "      Run: ollama pull llama2"
    echo "      Then start app with: mvn spring-boot:run -Dspring.profiles.active=ollama"
    echo ""
    read -p "Do you want to continue without OpenAI? (y/n): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo ""
echo "🏗️  Building project..."
mvn clean install -DskipTests

echo ""
echo "🚀 Starting application..."
echo "   Access the API at: http://localhost:8080"
echo "   H2 Console: http://localhost:8080/h2-console"
echo ""
echo "📚 Quick commands to try:"
echo ""
echo "   # List schemas"
echo "   curl http://localhost:8080/api/v1/schemas"
echo ""
echo "   # Generate 5 users"
echo "   curl -X POST http://localhost:8080/api/v1/data/quick-generate/User/5"
echo ""
echo "   # Generate 10 orders"
echo "   curl -X POST http://localhost:8080/api/v1/data/quick-generate/Order/10"
echo ""
echo "Press Ctrl+C to stop the application"
echo ""
echo "════════════════════════════════════════════════════════════════"
echo ""

# Start the application
if [ -z "$OPENAI_API_KEY" ]; then
    echo "⚠️  Starting with Ollama (make sure Ollama is running)"
    mvn spring-boot:run -Dspring.profiles.active=ollama
else
    echo "✅ Starting with OpenAI"
    mvn spring-boot:run
fi
