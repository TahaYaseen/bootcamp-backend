#!/bin/bash
# Run Frontend Script for iTrade Bootcamp Project

# Ensure using correct Node version
echo "🔄 Setting Node.js version to 20..."
source ~/.nvm/nvm.sh
nvm install 20
nvm use 20

# Navigate to frontend directory and start Angular server
cd "$(dirname "$0")/frontend"

echo "🚀 Starting Angular development server..."
npm start