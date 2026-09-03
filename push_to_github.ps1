git rm -r --cached app/build --ignore-unmatch
git rm -r --cached build --ignore-unmatch
git add .
git commit -m "feat: Initial commit for VibeScan Android App"
git branch -M main
git push -u origin main
