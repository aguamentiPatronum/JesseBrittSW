Team Names

Lodermeier, Ryan

Davis, Britt

Njuguna, Jesse W

<<<<<<< HEAD
Steps....
 TO TEST THE CUSTOM CHECKS:
 
  1. Clone the repo: git clone https://github.com/byrdsmyth/cougar-nation-swtesting
  2. Import as a project to eclipse from the directory which you cloned into
  3. In eclipse, right-click on the folder net.sf.eclipsecs.sample
  4. Select run-as... Eclipse Application
  5. In the second Eclipse that pops up, click Import...
  6. Browse to the location where you cloned the repository
  7. Open the tests folder
  8. Click on testCode and hit Finish
  9. Once the project is imported, double-click on it to open it up
  10. Go to the Eclipse menu > Preferences > Checkstyle
  11. Click New... and give it a name, then hit apply and close
  12. Right-click on the testCode folder in the Project Explorer
  13. Go to properties > Checkstyle
  14. Check the box for "activate checkstyle for this project"
  15. In the drop-down menu, select the new configuration you just made
  16. Click Configure...
  17. Scroll down to My Custom Checks and hit the arrow to expand
  18. Add all the custom check and hit finish
  19. Hit apply and close, and yes when asked to rebuild the project
  20. Go to the Window menu > show > other > select the two views under checkstyle
  21. You should now see the Checkstyle error for our custom checks

SETUP STEPS FOR USING GITHUB:
First Time:
1)git config -global user.name "username"
2)git config -global user.email "email"

After inital setup, get the repo going and walk through basic work flow

1) git init (eother in directory of planned repo or give it the path)
2a) If first time with Git Hub create your keys in Linux then add to Git Hub. 
2) git clone with ssh key
3) git pull origin master
4) git checkout -b <branch> (do not need -b if branch already exists)
5) git add <files>
6) git commit -m <comment>
7) git push origin <branch name>
8) Handle to pull request on GitHub
9) Git pull origin master 

Options
1) git reset --hard -> reset to origin master
2) git reset -> soft reset
3) update .gitignore if you would like to not include diretories or files.  It is recursive
4) setup keys from command line: ssh-keygen -t rsa -b 4096 -C "your email".  Then go to /home/.ssh/id_rsa.pub.  Copy that text and enter it into GitHub or BitBucket as a new key.  This will allow both git and GitHub/BitBucket to talk to each other
