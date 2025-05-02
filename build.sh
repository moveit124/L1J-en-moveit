#!/usr/bin/env bash

# Exit if any command fails
set -e

echo "Starting build process..."

# Clean previous build
echo "Running: ant clean"
ant clean

# Compile the source
echo "Running: ant compile"
ant compile

# Create the jar
echo "Running: ant jar"
ant jar

echo "Build completed successfully!" 