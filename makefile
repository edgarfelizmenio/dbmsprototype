all: init test clean

init:
	@clear
	mkdir -p "bin"

page: init
	javac -d "bin" -sourcepath src src/dbms/diskspacemanager/page/*.java

diskspacemgr: init page
	javac -d "bin" -sourcepath src src/dbms/diskspacemanager/*.java

policies: init diskspacemgr
	javac -d "bin" -sourcepath src src/dbms/buffermanager/policies/*.java

buffermgr: init diskspacemgr policies
	javac -d "bin" -sourcepath src src/dbms/buffermanager/*.java

tests: init diskspacemgr buffermgr
	javac -d "bin" -sourcepath src src/testmodule/tests/*.java

main_program: init tests
	javac -d "bin" -sourcepath src src/testmodule/Main.java

test: main_program
	java -classpath "bin" testmodule.Main 2> run.log
	
clean: test
	rm -r "bin"
	@echo "Test finished. View run.log for details."
