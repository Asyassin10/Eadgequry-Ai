#!/bin/bash

# =================================================================
# Script to Generate Test Coverage and Upload to SonarQube
# =================================================================

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Services to analyze
SERVICES=("auth" "user-profile" "notification")

# Function to print colored output
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Function to check if SonarQube is running
check_sonarqube() {
    print_info "Checking if SonarQube is running..."

    if docker ps | grep -q "sonarqube"; then
        print_success "SonarQube is running"

        # Wait for SonarQube to be ready
        print_info "Waiting for SonarQube to be ready..."
        timeout=300  # 5 minutes timeout
        elapsed=0

        while ! curl -s http://localhost:9000/api/system/status | grep -q '"status":"UP"'; do
            if [ $elapsed -ge $timeout ]; then
                print_error "SonarQube failed to start within $timeout seconds"
                exit 1
            fi
            echo -n "."
            sleep 5
            elapsed=$((elapsed + 5))
        done
        echo ""
        print_success "SonarQube is ready!"
    else
        print_error "SonarQube is not running!"
        print_info "Please start SonarQube using: docker-compose up -d sonarqube sonarqube-db"
        exit 1
    fi
}

# Function to run tests and generate coverage for a service
run_coverage() {
    local service=$1

    print_info "=========================================="
    print_info "Processing service: $service"
    print_info "=========================================="

    if [ ! -d "$service" ]; then
        print_warning "Service directory '$service' not found. Skipping..."
        return
    fi

    cd "$service"

    # Clean and run tests
    print_info "Running tests and generating coverage..."
    mvn clean test jacoco:report

    if [ $? -eq 0 ]; then
        print_success "Tests completed successfully for $service"

        # Check if coverage report was generated
        if [ -f "target/site/jacoco/jacoco.xml" ]; then
            print_success "Coverage report generated: target/site/jacoco/jacoco.xml"

            # Display coverage summary
            if [ -f "target/site/jacoco/index.html" ]; then
                print_info "HTML Report: $(pwd)/target/site/jacoco/index.html"
            fi
        else
            print_warning "Coverage report not found!"
        fi
    else
        print_error "Tests failed for $service!"
        cd ..
        exit 1
    fi

    cd ..
}

# Function to upload to SonarQube
upload_to_sonar() {
    local service=$1

    print_info "=========================================="
    print_info "Uploading $service to SonarQube"
    print_info "=========================================="

    if [ ! -d "$service" ]; then
        print_warning "Service directory '$service' not found. Skipping..."
        return
    fi

    cd "$service"

    # Upload to SonarQube using Maven plugin
    print_info "Uploading to SonarQube..."
    mvn sonar:sonar

    if [ $? -eq 0 ]; then
        print_success "Successfully uploaded $service to SonarQube"
    else
        print_error "Failed to upload $service to SonarQube!"
    fi

    cd ..
}

# Main execution
main() {
    print_info "Starting Coverage Generation and SonarQube Analysis"
    print_info "======================================================"

    # Check if SonarQube is running
    check_sonarqube

    # Parse command line arguments
    if [ "$1" == "--service" ] && [ -n "$2" ]; then
        # Run for specific service
        print_info "Running coverage for specific service: $2"
        run_coverage "$2"
        upload_to_sonar "$2"
    elif [ "$1" == "--coverage-only" ]; then
        # Only generate coverage, don't upload
        print_info "Generating coverage reports only (no SonarQube upload)"
        for service in "${SERVICES[@]}"; do
            run_coverage "$service"
        done
    else
        # Run for all services
        print_info "Running coverage for all services"

        # Generate coverage for all services
        for service in "${SERVICES[@]}"; do
            run_coverage "$service"
        done

        # Upload all to SonarQube
        for service in "${SERVICES[@]}"; do
            upload_to_sonar "$service"
        done
    fi

    print_success "======================================================"
    print_success "All tasks completed successfully!"
    print_info "View SonarQube dashboard at: http://localhost:9000"
    print_info ""
    print_info "Projects:"
    print_info "  - eadgequry-auth"
    print_info "  - eadgequry-user-profile"
    print_info "  - eadgequry-notification"
}

# Show usage
if [ "$1" == "--help" ] || [ "$1" == "-h" ]; then
    echo "Usage: ./run-coverage.sh [OPTIONS]"
    echo ""
    echo "Options:"
    echo "  --service <name>    Run coverage for specific service (auth, user-profile, notification)"
    echo "  --coverage-only     Generate coverage reports only (don't upload to SonarQube)"
    echo "  --help, -h          Show this help message"
    echo ""
    echo "Examples:"
    echo "  ./run-coverage.sh                    # Run all services"
    echo "  ./run-coverage.sh --service auth     # Run only auth service"
    echo "  ./run-coverage.sh --coverage-only    # Generate coverage without SonarQube upload"
    exit 0
fi

# Run main function
main "$@"
