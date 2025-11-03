#!/bin/bash

# Quick BCrypt password hasher for testing
# Usage: ./generate-password.sh yourpassword

if [ -z "$1" ]; then
  echo "Usage: ./generate-password.sh <password>"
  exit 1
fi

cd "$(dirname "$0")"

# Use the auth service to generate hash
./mvnw -q compile exec:java -Dexec.mainClass="com.eadgequry.auth.PasswordHashGenerator" -Dexec.args="$1" 2>/dev/null
