// Script de lancement automatique
// run_changelog.sh
#!/bin/bash

echo "🚀 Génération automatique du changelog..."

# Vérifier qu'on est dans un repo Git
if [ ! -d ".git" ]; then
    echo "❌ Erreur: Pas dans un repository Git"
    exit 1
fi

# Vérifier que Git est installé
if ! command -v git &> /dev/null; then
    echo "❌ Erreur: Git n'est pas installé"
    exit 1
fi

# Vérifier que Java est installé
if ! command -v javac &> /dev/null; then
    echo "❌ Erreur: Java n'est pas installé"
    exit 1
fi

# Créer les dossiers nécessaires
mkdir -p build/classes

# Compiler le projet Java
echo "🔨 Compilation..."
find src -name "*.java" -print0 | xargs -0 javac -d build/classes

if [ $? -ne 0 ]; then
    echo "❌ Erreur de compilation"
    exit 1
fi

# Exécuter le générateur
echo "📝 Génération du changelog..."
java -cp "build/classes" com.changelog.generator.AutoChangelogGenerator

if [ $? -eq 0 ]; then
    echo "✅ Changelog généré avec succès!"
    
    # Optionnel: Ajouter au git
    if [ -f "CHANGELOG.md" ]; then
        echo ""
        read -p "Voulez-vous commiter le changelog? (y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            git add CHANGELOG.md
            git commit -m "docs: update changelog"
            echo "✅ Changelog committé!"
        fi
    fi
else
    echo "❌ Erreur lors de la génération"
    exi