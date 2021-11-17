package compiler;

import org.antlr.v4.runtime.ParserRuleContext; // need to debug every rule
import lexparse.*; //classes for lexer parser
import org.objectweb.asm.*;  //classes for generating bytecode
import org.objectweb.asm.Opcodes; //Explicit import for ASM bytecode constants


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
	
	
	public class variable {	
	
		//Desired variable constructor. Requires the variable's datatype and the variable's value as parameters.
		public variable(String varType, String varValue) {
			this.varType = varType;
			this.varValue = varValue;
		}//end variable constructor
		
	}//end variable class
	
	
	
	
	//Creation of our symbol table using a hashmap. Composed of a string (being the variable's name) and a variable object which has the variable's type and the variable's value.
	public HashMap<String, variable> SymbolTable = new HashMap<String,variable>;
	
	
	//End of our code.
	
	
	//"*--* == delete later when necessary. These will be personal comments for us to clarify code.
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
		
	}
	
	@Override 
	public void exitDeclare(KnightCodeParser.DeclareContext ctx) {
	
	}
	
	@Override
	public void enterVariable(KnightCodeParser.VariableContext ctx) {
	
		variable var = new variable();
		
		var.varType = ctx.getChild(0).getText();
		String identifier = ctx.getChild(1).getText();
		
		
	}
	
	@Override 
	public void exitVariable(KnightCodeParser.VariableContext ctx) { 
	
	
	
	}
	
	@Override 
	public void enterBody(KnightCodeParser.BodyContext ctx) { 
	
	}
	
	@Override 
	public void exitBody(KnightCodeParser.BodyContext ctx) { 
	
	}
	
	@Override 
	public void enterSetvar(KnightCodeParser.SetvarContext ctx) { 
	
	}
	
	@Override 
	public void exitSetvar(KnightCodeParser.SetvarContext ctx) { 
	
	}
	
	
	
	




























}//end class
