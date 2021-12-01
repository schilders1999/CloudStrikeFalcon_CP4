	Welcome to the Cloudstrike Falcon Compile for Knightcode

	This compiler supports addition, subtraction, multiplication, and division of Integers. It also supports using String variables. Users can compile programs written in Knightcode that accept inputs from the command line, and complete programs that use loops or comparison. 
	This compiler was created by Spencer Childers and Drake Hovsepian, with technical assistance by Emil Berglund. 

	To start using the compiler, navigate to this project's main directory using a command line application.
Use the command "ant build-grammar" to generate grammar .java files from the rules already provided for Knightcode.
Use the command "ant compile-grammar" to compile these .java files into usable class files.
Use the command "ant compile" to compile the compiler, listener, and utilities .java files responsible for translation and interpretation of Knightcode programs.
Use the command "ant clean-grammar" to delete the generated and compiled grammar files.
Use the command "ant clean" to delete the compiler, listener, and utilities .class files.

	To view a parse tree of a Knightcode program, navigate to the project's main directory and type "grun lexparse.KnightCode file tests/program.kc -gui" where 'program' is the name of the Knightcode file.

	To compile a Knightcode program, navigate to the project's main directory and type "java compiler/CompilerTest tests/program.kc output/output" Where:
'tests/program.kc' is the folder/filename of the Knightcode program and, 
'output/output' is the folder/desired filename of the new .class file that the compiler will create.

	Once a Knightcode program has been compiled, it should be available to be run with the standard 'java' command from the output folder.

