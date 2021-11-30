package compiler;

import org.antlr.v4.runtime.ParserRuleContext; // need to debug every rule
import lexparse.*; //classes for lexer parser
import org.objectweb.asm.*;  //classes for generating bytecode
import org.objectweb.asm.Opcodes; //Explicit import for ASM bytecode constants
import java.util.*; //classes for hashmaps

import static org.objectweb.asm.Opcodes.*;



public class myListener extends KnightCodeBaseListener{

	private ClassWriter cw;  //class level ClassWriter 
	private MethodVisitor mainVisitor; //class level MethodVisitor
	private String programName; //name of the class and the output file (used by ASM)
	private boolean debug; //flag to indicate debug status

	public myListener(String programName, boolean debug){
	       
		this.programName = programName;
		this.debug = debug;

	}//end constructor
	
	
	//Start of our code:
	
	
	//"???" == not sure if correct. Maybe change later.
	//??? Expected datatypes to be seen in parse tree.
	public static final String INT = "INTEGER";
	public static final String STR = "STRING";
	public int newLocation = 0; //Integer value used to assign the location value of new variables.
	public String SymbolTableVar;
	public String SymbolTableVal;
	public int operand1;
	public int operand2;
	public String currVarValue; //Value associated with variable
	public String identifier;
	public int intVarValue;
	public int varLocation;
	public int operand1Loc;
	public int operand2Loc;

	
	public class variable {	
	
		public int location = 0;//Position of variable in memory. Necessary for stacking
		public String varType = "";
		public String varValue = "";
		
		//Desired variable constructor. Requires the variable's datatype and the variable's value as parameters.
		public variable(String varType, String varValue) {
			this.varType = varType;
			this.varValue = varValue;
			
		}//end variable constructor
		
		public variable(){
		}
	}//end variable class
	
	
	
	
	//Creation of our symbol table using a hashmap. Composed of a string (being the variable's name) and a variable object which has the variable's type and the variable's value.
	public HashMap<String, variable> SymbolTable = new HashMap<String, variable>();
	
	
	//End of our code.
	
	
	//"*--*" == delete later when necessary. These will be personal comments for us to clarify code.
	//*--* List of various listener methods.


	public void setupClass(){
		
		//Set up the classwriter
		cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC,this.programName, null, "java/lang/Object",null);
	
		//Use local MethodVisitor to create the constructor for the object
		MethodVisitor mv=cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0); //load the first local variable: this
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V",false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1,1);
        mv.visitEnd();
       	
		//Use global MethodVisitor to write bytecode according to entries in the parsetree	
	 	mainVisitor = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,  "main", "([Ljava/lang/String;)V", null, null);
        mainVisitor.visitCode();

	}//end setupClass

	public void closeClass(){
		//Use global MethodVisitor to finish writing the bytecode and write the binary file.
		mainVisitor.visitInsn(Opcodes.RETURN);
		mainVisitor.visitMaxs(3, 3);
		mainVisitor.visitEnd();

		cw.visitEnd();

        byte[] b = cw.toByteArray();

        Utilities.writeFile(b,this.programName+".class");
        
        System.out.println("Done!");

	}//end closeClass


	public void enterFile(KnightCodeParser.FileContext ctx){

		System.out.println("Enter program rule for first time");
		setupClass();
	}

	public void exitFile(KnightCodeParser.FileContext ctx){

		System.out.println("Leaving program rule. . .");
		closeClass();

	}

	/**
	 * Prints context string. Used for debugging purposes
	 * @param ctx
	 */
	private void printContext(String ctx){
		System.out.println(ctx);
	}

	@Override 
	public void enterEveryRule(ParserRuleContext ctx){ 
		if(debug) printContext(ctx.getText());
	}

	@Override
	public void enterPrint(KnightCodeParser.PrintContext ctx){

		System.out.println("Enter print");
		String output = ctx.getChild(1).getText();
		//output = output.substring(5,output.length());
		mainVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		mainVisitor.visitLdcInsn(output);
		mainVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",  "println", "(Ljava/lang/String;)V", false);

	}//end enterWrite_stmt

	@Override 
	public void exitPrint(KnightCodeParser.PrintContext ctx) { 
	
		System.out.println("Exit print");
	}
	
	
	// Start of our code:
	
	
	@Override 
	public void enterDeclare(KnightCodeParser.DeclareContext ctx) { 
		System.out.println("Entering Declare");
	}
	
	@Override 
	public void exitDeclare(KnightCodeParser.DeclareContext ctx) {
		System.out.println("Exiting Declare");
	}
	
	@Override
	public void enterVariable(KnightCodeParser.VariableContext ctx) {
	
		System.out.println("Entering variable");
		
		variable var = new variable();
		
		var.varType = ctx.getChild(0).getText(); //Assigning varType to left child leaf
		identifier = ctx.getChild(1).getText(); //Assigning identifier to right child leaf
		var.location = newLocation; //Assigning var object a unique location 
		SymbolTable.put(identifier, var); //Putting variable object into SymbolTable hashmap
		
		newLocation = newLocation + 1;//Incrementing newLocation by one so new variable locations can be assigned.
		
		
	}//end enterVariable
	
	@Override 
	public void exitVariable(KnightCodeParser.VariableContext ctx) { 
	
		System.out.println("Exiting variable");
	
	}//end exitVariable
	
	@Override 
	public void enterBody(KnightCodeParser.BodyContext ctx) { 
		System.out.println("Entering Body");
	}//end enterBody
	
	@Override 
	public void exitBody(KnightCodeParser.BodyContext ctx) { 
		System.out.println("Exiting Body");
	}//end exitBody
	
	@Override 
	public void enterSetvar(KnightCodeParser.SetvarContext ctx) { 
		
		System.out.println("Entering SetVar");
		SymbolTableVar = ctx.getChild(1).getText();
		SymbolTable.get(SymbolTableVar).varValue = ctx.getChild(3).getText();
		currVarValue = SymbolTable.get(SymbolTableVar).varValue;
		varLocation = SymbolTable.get(SymbolTableVar).location;
		
	}//end enterSetvar
	
	@Override 
	public void exitSetvar(KnightCodeParser.SetvarContext ctx) { 
	
		intVarValue = Integer.valueOf(currVarValue);
		System.out.print(currVarValue+"\n");
		
		
		if (identifier=="INTEGER") {
		
			mainVisitor.visitLdcInsn(intVarValue);
			mainVisitor.visitVarInsn(ISTORE, varLocation); 
			
		} else { //else if identifier == "STRING"
		
			mainVisitor.visitLdcInsn(currVarValue);
			mainVisitor.visitVarInsn(ASTORE, varLocation);
		}
		
		System.out.println("Exiting Setvar");
	}
	
	
	@Override 
   	public void enterAddition(KnightCodeParser.AdditionContext ctx){ 
		
		System.out.println("Entering Addition");
		
		//Assigning operand 1
		String op1Variable = ctx.getChild(0).getText();
		operand1 = Integer.valueOf(SymbolTable.get(op1Variable).varValue);
		operand1Loc = SymbolTable.get(op1Variable).location;
		System.out.println(operand1Loc);
		
		//Assigning operand 2
		String op2Variable = ctx.getChild(2).getText();
		operand2 = Integer.valueOf(SymbolTable.get(op2Variable).varValue);
		operand2Loc = SymbolTable.get(op2Variable).location;
		System.out.println(operand2Loc);

		mainVisitor.visitIntInsn(ILOAD, operand1Loc);
		
		mainVisitor.visitIntInsn(ILOAD, operand2Loc);
		
    	}//end enterAddition
	
   	@Override 
   	public void exitAddition(KnightCodeParser.AdditionContext ctx){ 
		
		mainVisitor.visitInsn(IADD);
		mainVisitor.visitIntInsn(ISTORE,SymbolTable.get(identifier).location);
		System.out.println(SymbolTable.get(identifier).varValue);
		System.out.println("Exiting Addition");
		
   	}//end exitAddition
	
	
	@Override public void enterSubtraction(KnightCodeParser.SubtractionContext ctx) { 
		
		System.out.println("Entering Subraction");
		
		//Assigning operand 1
		String op1Variable = ctx.getChild(0).getText();
		operand1 = Integer.valueOf(SymbolTable.get(op1Variable).varValue);
		operand1Loc = SymbolTable.get(op1Variable).location;
		System.out.println(operand1Loc);
		
		//Assigning operand 2
		String op2Variable = ctx.getChild(2).getText();
		operand2 = Integer.valueOf(SymbolTable.get(op2Variable).varValue);
		operand2Loc = SymbolTable.get(op2Variable).location;
		System.out.println(operand2Loc);

		mainVisitor.visitIntInsn(ILOAD, operand1Loc);
		
		mainVisitor.visitIntInsn(ILOAD, operand2Loc);
	}
	
	@Override public void exitSubtraction(KnightCodeParser.SubtractionContext ctx) { 
	
		mainVisitor.visitInsn(ISUB);
	
		System.out.println("Exiting Subtraction");
	}
	
	@Override public void enterMultiplication(KnightCodeParser.MultiplicationContext ctx) { 
	
		System.out.println("Entering Multiplication");
		
		//Assigning operand 1
		String op1Variable = ctx.getChild(0).getText();
		operand1 = Integer.valueOf(SymbolTable.get(op1Variable).varValue);
		operand1Loc = SymbolTable.get(op1Variable).location;
		System.out.println(operand1Loc);
		
		//Assigning operand 2
		String op2Variable = ctx.getChild(2).getText();
		operand2 = Integer.valueOf(SymbolTable.get(op2Variable).varValue);
		operand2Loc = SymbolTable.get(op2Variable).location;
		System.out.println(operand2Loc);

		mainVisitor.visitIntInsn(ILOAD, operand1Loc);
		
		mainVisitor.visitIntInsn(ILOAD, operand2Loc);
	}
	
	@Override public void exitMultiplication(KnightCodeParser.MultiplicationContext ctx) { 
	
		mainVisitor.visitInsn(IMUL);
		
		System.out.println("Exiting Multiplication");
	}
	
	@Override public void enterDivision(KnightCodeParser.DivisionContext ctx) { 
	
		System.out.println("Entering Division");
		
		//Assigning operand 1
		String op1Variable = ctx.getChild(0).getText();
		operand1 = Integer.valueOf(SymbolTable.get(op1Variable).varValue);
		operand1Loc = SymbolTable.get(op1Variable).location;
		System.out.println(operand1Loc);
		
		//Assigning operand 2
		String op2Variable = ctx.getChild(2).getText();
		operand2 = Integer.valueOf(SymbolTable.get(op2Variable).varValue);
		operand2Loc = SymbolTable.get(op2Variable).location;
		System.out.println(operand2Loc);

		mainVisitor.visitIntInsn(ILOAD, operand1Loc);
		
		mainVisitor.visitIntInsn(ILOAD, operand2Loc);
	}
	
	@Override public void exitDivision(KnightCodeParser.DivisionContext ctx) { 
		
		mainVisitor.visitInsn(IDIV);
		
		System.out.println("Exiting Division");
	}

	@Override public void enterRead(KnightCodeParser.ReadContext ctx) { }
	
	@Override public void exitRead(KnightCodeParser.ReadContext ctx) { }
	
	
	@Override public void enterLoop(KnightCodeParser.LoopContext ctx) { }
	
	@Override public void exitLoop(KnightCodeParser.LoopContext ctx) { }
	
	
	@Override public void enterComparison(KnightCodeParser.ComparisonContext ctx) { }
	
	@Override public void exitComparison(KnightCodeParser.ComparisonContext ctx) { }
	




















}//end class
