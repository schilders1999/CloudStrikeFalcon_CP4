/**
*Listener that contains various methods from the KnightCodeBaseListener to provide functionality
* when encountering certain nodes within the parse tree.
*
*@author Spencer Childers
*@author Drake Hovsepian
*@version 1.0
*Programming Project 4
*CS 322 Compilers Construction
*Fall 2021
**/
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
	
	public static final String INT = "INTEGER";
	public static final String STR = "STRING";
	public int newLocation = 0; //Integer value used to assign the location value of new variables.
	public String identifier;



	public variable var1 = new variable();
	public variable var2 = new variable();
	
	//Variable class used to create variables
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
	/**
	 * Used to put variable identifier and variable object in symbol table
	 * @param ctx
	 */
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
	
	
	String key;
	int currLoc = 0;
	
	@Override 
	/**
	 * Used to assign values to variables
	 * @param ctx
	 */
	public void enterSetvar(KnightCodeParser.SetvarContext ctx) { 
		
		System.out.println("Entering SetVar");
		
		key = ctx.getChild(1).getText();
		var1 = SymbolTable.get(key);
	}//end enterSetvar
	
	@Override 
	public void exitSetvar(KnightCodeParser.SetvarContext ctx) { 
	
		int store = var1.location;
		
		
		if (var1.varType.equalsIgnoreCase("INTEGER")) {
		
			mainVisitor.visitVarInsn(ISTORE, store); 
			
		} 
		
		else if (var1.varType.equalsIgnoreCase("STRING")) { //else if identifier == "STRING"
		
			mainVisitor.visitVarInsn(ASTORE, store);
		}
		
		System.out.println("Exiting Setvar");
	}
	
	public int num = 0;
	
	@Override
	/**
	 * Pushes memory location of operand on to stack
	 * @param ctx
	 */
	public void enterNumber(KnightCodeParser.NumberContext ctx){
		
		num = Integer.valueOf(ctx.getText());
		mainVisitor.visitIntInsn(SIPUSH, num);
	
	}
	
	@Override 
	public void exitNumber(KnightCodeParser.NumberContext ctx) { }
	
	String keyID = "";
	
	@Override 
	/**
	 * Loads the memory location of a predefined variable
	 * @param ctx
	 */
	public void enterId(KnightCodeParser.IdContext ctx) { 
		
		System.out.println("Entering ID");
		keyID = ctx.getText();
		var2 = SymbolTable.get(keyID);
		currLoc = var2.location;
		mainVisitor.visitIntInsn(ILOAD, currLoc);
	
	}
	
	@Override 
	public void exitId(KnightCodeParser.IdContext ctx) { 
		System.out.println("Exiting ID");
	}
	
	
	@Override 
	/**
	 * Methods for addition
	 * @param ctx
	 */
   	public void enterAddition(KnightCodeParser.AdditionContext ctx){ 
		
		System.out.println("Entering Addition");
		
		
		
    	}//end enterAddition
	
   	@Override 
   	public void exitAddition(KnightCodeParser.AdditionContext ctx){ 
		
		mainVisitor.visitInsn(IADD);
		
		System.out.println("Exiting Addition");
		
   	}//end exitAddition
	
	
	@Override 
	/**
	 * Methods for subtraction
	 * @param ctx
	 */
	public void enterSubtraction(KnightCodeParser.SubtractionContext ctx) { 
		
		System.out.println("Entering Subraction");
		
		
	}
	
	@Override 
	public void exitSubtraction(KnightCodeParser.SubtractionContext ctx) { 
	
		mainVisitor.visitInsn(ISUB);
	
		System.out.println("Exiting Subtraction");
	}
	
	@Override 
	/**
	 * Methods for Multiplication
	 * @param ctx
	 */
	public void enterMultiplication(KnightCodeParser.MultiplicationContext ctx) { 
	
		System.out.println("Entering Multiplication");
		
	}
	
	@Override 
	public void exitMultiplication(KnightCodeParser.MultiplicationContext ctx) { 
	
		mainVisitor.visitInsn(IMUL);
		
		System.out.println("Exiting Multiplication");
	}
	
	@Override 
	/**
	 * Methods for Division
	 * @param ctx
	 */
	public void enterDivision(KnightCodeParser.DivisionContext ctx) { 
	
		System.out.println("Entering Division");
		
	}
	
	@Override 
	public void exitDivision(KnightCodeParser.DivisionContext ctx) { 
		
		mainVisitor.visitInsn(IDIV);
		
		System.out.println("Exiting Division");
	}
	
	boolean printBull = true;
	boolean printInt = false;
	
	@Override
	/**
	 * Methods used for printing. Checks if item being printed is a string or int before printing.
	 * @param ctx
	 */
	public void enterPrint(KnightCodeParser.PrintContext ctx){

		System.out.println("Enter print");
		String output = ctx.getChild(1).getText();
		//output = output.substring(5,output.length());
		
		keyID = ctx.getChild(1).getText();
		
	
		mainVisitor.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
		
		if(SymbolTable.containsKey(keyID)){
			printBull = false;
			var2 = SymbolTable.get(keyID);
			
			if (var2.varType.equalsIgnoreCase("INTEGER")){
				printInt = true;
			}else{
				printInt = false;
			}
			
		} 
		else {
			printBull = true;
		}
		
		

	}//end enterWrite_stmt

	@Override 
	public void exitPrint(KnightCodeParser.PrintContext ctx) { 
	
		if(printBull) {
			mainVisitor.visitLdcInsn(ctx.getChild(1).getText());
			mainVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",  "println", "(Ljava/lang/String;)V", false);
		}
		else{
			
			if(printInt){
				mainVisitor.visitVarInsn(ILOAD, var2.location);
				mainVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",  "println", "(I)V", false);
			}
			else{
				mainVisitor.visitVarInsn(ALOAD, var2.location);
				mainVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream",  "println", "(Ljava/lang/String;)V", false);
			}
		}	
				
			
			
		System.out.println("Exit print");
	}
	
	public int scanLoc = -1;
	
	@Override 
	/**
	 * Creates a new scanner which is stored in a memory location
	 * @param ctx
	 */
	public void enterRead(KnightCodeParser.ReadContext ctx) {
	
		System.out.println("Enter read");
		if(scanLoc == -1) {
		
			scanLoc = newLocation; 
			newLocation++;
			
			mainVisitor.visitTypeInsn(NEW, "java/util/Scanner");
			mainVisitor.visitInsn(DUP);
			mainVisitor.visitFieldInsn(GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");
			mainVisitor.visitMethodInsn(INVOKESPECIAL, "java/util/Scanner", "<init>", "(Ljava/io/InputStream;)V" , false);
			
			mainVisitor.visitVarInsn(ASTORE, scanLoc);
			
			
		}//end if
		
		
		
	 }//end enterRead
	
	@Override 
	/**
	 * Reads and stores an item entered at the command line. Uses if statement to determine whether item is a string or an integer.
	 * @param ctx
	 */
	public void exitRead(KnightCodeParser.ReadContext ctx) {
		variable var3 = SymbolTable.get(ctx.getChild(1).getText());
		
		mainVisitor.visitVarInsn(ALOAD, scanLoc);
		
		if ( var3.varType.equalsIgnoreCase("INTEGER")) {
		
			mainVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "nextInt", "()I", false);
			mainVisitor.visitVarInsn(ISTORE, var3.location);
			
			mainVisitor.visitVarInsn(ALOAD, scanLoc);
			mainVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "nextLine", "()Ljava/lang/String;", false);
			mainVisitor.visitInsn(POP);
		} else {
			mainVisitor.visitMethodInsn(INVOKEVIRTUAL, "java/util/Scanner", "nextLine", "()Ljava/lang/String", false);
			mainVisitor.visitVarInsn(ASTORE, var3.location);
			
		}
		
		SymbolTable.put(ctx.getChild(1).getText(), var3);
		
		System.out.println("Exiting Read");
		
		
	}//end exitRead
	
	Label startLoop = new Label();
	Label endLoop = new Label();
	
	@Override 
	/**
	 * Methods used to perform while loops.
	 * @param ctx
	 */
	public void enterLoop(KnightCodeParser.LoopContext ctx) { 
	
		System.out.println("Entering Loop");
		
		
		var1 = SymbolTable.get(ctx.getChild(1).getText());
		int firstComp = var1.location;
		
		
		
		int testVar = Integer.valueOf(ctx.getChild(3).getText());
		System.out.println(testVar);
		//int secondComp = var2.location;
		int secondComp = testVar;
		
		mainVisitor.visitIntInsn(ILOAD, firstComp);
		mainVisitor.visitIntInsn(ILOAD, secondComp);
		
		/**
		if (SymbolTable.containsKey(var2)){
			
			System.out.println("!");
			mainVisitor.visitVarInsn(ALOAD, secondComp);
		}
		else{
			var2 = ctx.getChild(3).getText();
			mainVisitor.visitIntInsn(ILOAD, secondComp);
		}
		***/
		
		
		String compSign = ctx.getChild(2).getChild(0).getText();
		
		if(compSign == ">") {
			mainVisitor.visitJumpInsn(IF_ICMPLE, endLoop);
			
		}
		else if (compSign == "<") {
			mainVisitor.visitJumpInsn(IF_ICMPGE, endLoop);
			
		}
		else if (compSign == "=") {
			mainVisitor.visitJumpInsn(IF_ICMPNE, endLoop);
			
		}
		else if (compSign == "<>") {
			mainVisitor.visitJumpInsn(IF_ICMPEQ, endLoop);
			
		}
		
		mainVisitor.visitLabel(startLoop);

		
			
	}//end enterLoop
	
	@Override 
	public void exitLoop(KnightCodeParser.LoopContext ctx) {
	
		mainVisitor.visitJumpInsn(GOTO, startLoop);
		mainVisitor.visitLabel(endLoop);
	
		System.out.println("Exiting Loop");
	 }//end exitLoop
	
	
	

	


















}//end class
