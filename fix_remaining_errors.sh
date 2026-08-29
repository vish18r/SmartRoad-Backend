#!/bin/bash

# Comprehensive fix script for remaining compilation errors
# Replaces all old entity names with new Entity-suffixed names throughout the codebase

cd "$(dirname "$0")"

echo "🔧 Running comprehensive entity reference fix..."

# Find and replace across all Java files in src/main
find src/main/java -name "*.java" -type f | while read file; do
    # More aggressive replacements for entity types
    sed -i 's/<Organization>/<OrganizationEntity>/g' "$file"
    sed -i 's/<OrganizationMember>/<OrganizationMemberEntity>/g' "$file"
    sed -i 's/<Client>/<ClientEntity>/g' "$file"
    sed -i 's/<Project>/<ProjectEntity>/g' "$file"
    sed -i 's/<Road>/<RoadEntity>/g' "$file"
    sed -i 's/<RoadSection>/<RoadSectionEntity>/g' "$file"
    sed -i 's/<User>/<UserEntity>/g' "$file"
    sed -i 's/<OAuthState>/<OAuthStateEntity>/g' "$file"

    # Replace method parameters and variables
    sed -i 's/Organization /OrganizationEntity /g' "$file"
    sed -i 's/OrganizationMember /OrganizationMemberEntity /g' "$file"
    sed -i 's/Client /ClientEntity /g' "$file"
    sed -i 's/Project /ProjectEntity /g' "$file"
    sed -i 's/Road /RoadEntity /g' "$file"
    sed -i 's/RoadSection /RoadSectionEntity /g' "$file"
    sed -i 's/OAuthState /OAuthStateEntity /g' "$file"
done

# Fix JPQL queries in @Query annotations
find src/main/java -name "*.java" -type f | while read file; do
    sed -i 's/FROM Organization /FROM OrganizationEntity /g' "$file"
    sed -i 's/FROM OrganizationMember /FROM OrganizationMemberEntity /g' "$file"
    sed -i 's/FROM Client /FROM ClientEntity /g' "$file"
    sed -i 's/FROM Project /FROM ProjectEntity /g' "$file"
    sed -i 's/FROM Road /FROM RoadEntity /g' "$file"
    sed -i 's/FROM RoadSection /FROM RoadSectionEntity /g' "$file"
    sed -i 's/FROM User /FROM UserEntity /g' "$file"
    sed -i 's/FROM OAuthState /FROM OAuthStateEntity /g' "$file"
done

echo "✅ Fix script complete!"
echo "Now run: ./gradlew clean compileJava"
