#!/bin/bash

# Final comprehensive fix for remaining compilation errors
# Handles all remaining entity reference issues

cd "$(dirname "$0")"

echo "🔧 Running final comprehensive entity reference fix..."

# Replace ALL remaining User references with UserEntity
find src/main/java -name "*.java" -type f | xargs sed -i 's/private UserResponse mapToUserResponse(User /private UserResponse mapToUserResponse(UserEntity /g'
find src/main/java -name "*.java" -type f | xargs sed -i 's/throws SmartRoadException(User /throws SmartRoadException(UserEntity /g'

# Fix any remaining entity type references
find src/main/java -name "*.java" -type f | xargs sed -i 's/<OrganizationMember,/<OrganizationMemberEntity,/g'
find src/main/java -name "*.java" -type f | xargs sed -i 's/JpaRepository<OrganizationMember,/JpaRepository<OrganizationMemberEntity,/g'

# Fix imports for remaining entities
find src/main/java -name "*.java" -type f | xargs sed -i 's/import.*\.User;/import com.nextenti.services.domain.entity.UserEntity;/g'
find src/main/java -name "*.java" -type f | xargs sed -i 's/import.*\.OrganizationMember;/import com.nextenti.services.domain.entity.OrganizationMemberEntity;/g'

echo "✅ Final fix script complete!"
echo ""
echo "📋 Next steps:"
echo "1. Run: ./gradlew clean compileJava"
echo "2. If errors remain, they'll be in OAuth2Service or similar auth services"
echo "3. Those services may need targeted manual fixes"
echo ""
echo "🚀 Once compilation passes:"
echo "   ./gradlew clean build"
