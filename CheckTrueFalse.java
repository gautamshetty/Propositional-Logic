import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

/**
 */
public class CheckTrueFalse {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		if (args.length != 3) {
			// takes three arguments
			System.out.println("Usage: java CheckTrueFalse [wumpus-rules-file] [additional-knowledge-file] [input_file]\n");
			exit_function(0);
		}
		
		String wumpusFileName = args[0];
		String addlKnowFileName = args[1];
		String stmtFileName = args[2];
		
		// create some buffered IO streams
		String buffer;
		BufferedReader inputStream;
		
		// create the knowledge base and the statement
		LogicalExpression knowledge_base = new LogicalExpression();
		LogicalExpression statement = new LogicalExpression();
		
		// open the wumpus_rules.txt
		try {
			inputStream = new BufferedReader(new FileReader(wumpusFileName));

			// load the wumpus rules
			System.out.println("loading the wumpus rules...");
			knowledge_base.setConnective("and");

			while ((buffer = inputStream.readLine()) != null) {
				if (!(buffer.startsWith("#") || (buffer.equals("")))) {
					// the line is not a comment
					LogicalExpression subExpression = readExpression(buffer);
					knowledge_base.setSubexpression(subExpression);
				} else {
					// the line is a comment. do nothing and read the next line
				}
			}

			// close the input file
			inputStream.close();

		} catch (Exception e) {
			System.out.println("failed to open " + wumpusFileName);
			e.printStackTrace();
			exit_function(0);
		}
		// end reading wumpus rules

		// read the additional knowledge file
		try {
			inputStream = new BufferedReader(new FileReader(addlKnowFileName));

			// load the additional knowledge
			System.out.println("loading the additional knowledge...");

			// the connective for knowledge_base is already set. no need to set it again.
			// i might want the LogicalExpression.setConnective() method to check for that
			// knowledge_base.setConnective("and");

			while ((buffer = inputStream.readLine()) != null) {
				if (!(buffer.startsWith("#") || (buffer.equals("")))) {
					LogicalExpression subExpression = readExpression(buffer);
					knowledge_base.setSubexpression(subExpression);
				} else {
					// the line is a comment. do nothing and read the next line
				}
			}

			// close the input file
			inputStream.close();

		} catch (Exception e) {
			System.out.println("failed to open " + addlKnowFileName);
			e.printStackTrace();
			exit_function(0);
		}
		// end reading additional knowledge

		// check for a valid knowledge_base
		if (!valid_expression(knowledge_base)) {
			System.out.println("invalid knowledge base");
			exit_function(0);
		}

		// print the knowledge_base
		knowledge_base.print_expression("\n");

		// read the statement file
		try {
			inputStream = new BufferedReader(new FileReader(stmtFileName));

			System.out.println("\n\nLoading the statement file...");
			// buffer = inputStream.readLine();

			// actually read the statement file
			// assuming that the statement file is only one line long
			while ((buffer = inputStream.readLine()) != null) {
				if (!buffer.startsWith("#")) {
					// the line is not a comment
					statement = readExpression(buffer);
					break;
				} else {
					// the line is a commend. no nothing and read the next line
				}
			}

			// close the input file
			inputStream.close();

		} catch (Exception e) {
			System.out.println("failed to open " + stmtFileName);
			e.printStackTrace();
			exit_function(0);
		}
		// end reading the statement file

		// check for a valid statement
		if (!valid_expression(statement)) {
			System.out.println("invalid statement");
			exit_function(0);
		}

		// print the statement
		statement.print_expression("");
		// print a new line
		System.out.println("\n");
		
		boolean kbAlpha = ttEntails(knowledge_base, statement);
		
		LogicalExpression notStatement = new LogicalExpression();
		notStatement.setConnective("not");
		notStatement.getSubexpressions().add(statement);
		
		boolean kbNotAlpha = ttEntails(knowledge_base, notStatement);
		
		//Print the result and save the result to result.txt file.
		printResult(kbAlpha, kbNotAlpha);
		
	} // end of main

	/**
	 * Prints the result and also writes the result to file result.txt
	 * @param kbAlpha Boolean variable which specifies if KB entails alpha.
	 * @param kbNotAlpha Boolean variable which specifies if KB entails not alpha.
	 */
	public static void printResult(boolean kbAlpha, boolean kbNotAlpha) {
		
		String result = "possibly true, possibly false";
		
		if (kbAlpha == true && kbNotAlpha == false)
			result = "definitely true";
		else if (kbAlpha == false && kbNotAlpha == true)
			result = "definitely false";
		else if (kbAlpha == true && kbNotAlpha == true)
			result = "both true and false";
		
		System.out.println("Result : " + result);
		
		BufferedWriter output = null;
		
		try {
			output = new BufferedWriter(new FileWriter("result.txt"));
			output.write(result);
		} catch (IOException e) {
			System.out.println("\nProblem writing to the output file!\n" + "Try again.");
			e.printStackTrace();
		} finally {
			try {
				if (output != null)
					output.close();
			} catch (IOException ioExp) {
				ioExp.printStackTrace();
			}
		}
	}
	
	/**
	 * ttEntails function - returns true if kb entails alpha else returns false.
	 * @param kb knowledge base.
	 * @param alpha alpha statement
	 * @return returns true if kb entails alpha else returns false.
	 */
	private static boolean ttEntails(LogicalExpression kb, LogicalExpression alpha) {
		
		return ttCheckAll(kb, alpha, getSymbols(), 0, new ArrayList());
	}
	
	/**
	 * ttCheckAll function - creates all the possible models.
	 * @param kb knowledge base.
	 * @param alpha alpha statement.
	 * @param symbolsList symbol list
	 * @param symbCount symbol count.
	 * @param model model object.
	 * @return
	 */
	private static boolean ttCheckAll(LogicalExpression kb, LogicalExpression alpha, List symbolsList, int symbCount, List model) {

		if (model.size() == symbolsList.size()) {
			
			if (plTrue(kb, symbolsList, model))
				return plTrue(alpha, symbolsList, model);
			else
				return true;
			
		} else {
			
			int index = symbCount++;
			String symbol = (String) symbolsList.get(index);
			Boolean symbolValue =  getValuefromAddlKnowFile(kb, symbol); //(Boolean) addlFileModel.get(index);
			
			if (symbolValue != null && symbolValue.booleanValue() == true) {
				List trueList = new ArrayList(model);
				trueList.add(true);
				
				return ttCheckAll(kb, alpha, symbolsList, symbCount, trueList);
			} else if (symbolValue != null && symbolValue.booleanValue() == false) {
				List falseList = new ArrayList(model);
				falseList.add(false);
				
				return ttCheckAll(kb, alpha, symbolsList, symbCount, falseList);
			} else { 
				List trueList = new ArrayList(model);
				trueList.add(true);
				
				List falseList = new ArrayList(model);
				falseList.add(false);
				
				return (ttCheckAll(kb, alpha, symbolsList, symbCount, trueList)
						&& ttCheckAll(kb, alpha, symbolsList, symbCount, falseList));
			}
		}
	}
	
	private static Boolean getValuefromAddlKnowFile(LogicalExpression kb, String symbol) {
		
		LogicalExpression subExpr = null, childSubExpr = null;
		for (int i = kb.getSubexpressions().size() - 1; i > kb.getSubexpressions().size() - 48; i--) {
			
			subExpr = (LogicalExpression) kb.getSubexpressions().get(i);
			
			if (subExpr != null && subExpr.getConnective() != null && subExpr.getConnective().equals("not")) {
				childSubExpr = (LogicalExpression) subExpr.getSubexpressions().get(0);
				if (childSubExpr.getUniqueSymbol().equals(symbol))
					return new Boolean(false);
			} else {
				if (subExpr.getUniqueSymbol().equals(symbol))
					return new Boolean(true);
			}
		}
		
		return null;
	}
	
	private static List getModelListFromAddlKnowFile(LogicalExpression kb, List symbols) {
		
		List model = new ArrayList();
		for (int j = 0; j < symbols.size(); j++)
			model.add(null);
		
		LogicalExpression subExpr = null, childSubExpr = null;
		for (int i = kb.getSubexpressions().size() - 1; i > kb.getSubexpressions().size() - 48; i--) {
			
			subExpr = (LogicalExpression) kb.getSubexpressions().get(i);
			
			if (subExpr != null && subExpr.getConnective() != null && subExpr.getConnective().equals("not")) {
				
				childSubExpr = (LogicalExpression) subExpr.getSubexpressions().get(0);
				if (childSubExpr.getUniqueSymbol() != null) {
					model.set(symbols.indexOf(childSubExpr.getUniqueSymbol()), new Boolean(false));
				}
				
			} else {
				
				if (subExpr.getUniqueSymbol() != null)
					model.set(symbols.indexOf(childSubExpr.getUniqueSymbol()), new Boolean(true));
				
			}
		}
		
		return model;
	}
	
	private static boolean plTrue(LogicalExpression logicExp, List symbols, List model) {
		
		if (logicExp.getConnective() == null) {
			return ((Boolean) model.get(symbols.indexOf(logicExp.getUniqueSymbol()))).booleanValue();
		}
		
		LogicalExpression subExpr = (LogicalExpression) logicExp.getSubexpressions().firstElement();
		boolean plTrueValue = plTrue(subExpr, symbols, model);
		boolean subPlTrueValue = false;
		String operator = logicExp.getConnective().trim();
		if (operator.equals("not")) // In case of not operator, there will be just one subexpression.
			return !plTrueValue;
		
		for (int i = 1; i < logicExp.getSubexpressions().size(); i++) {
			
			subExpr = (LogicalExpression) logicExp.getSubexpressions().get(i);
			subPlTrueValue = plTrue(subExpr, symbols, model);
			
			if (operator.equals("and"))
				plTrueValue &= subPlTrueValue;
			else if (operator.equals("or"))
				plTrueValue |= subPlTrueValue;
			else if (operator.equals("xor"))
				plTrueValue ^= subPlTrueValue;
			else if (operator.equals("if")) {
				if (plTrueValue == true && subPlTrueValue == false)
					plTrueValue = false;
				else
					plTrueValue = true;
			} else if (operator.equals("iff")) {
				if ((plTrueValue == true && subPlTrueValue == true)
						|| (plTrueValue == false && subPlTrueValue == false))
					plTrueValue = true;
				else
					plTrueValue = false;
			}
		}
		
		return plTrueValue;
	}
	
	private static List getSymbols() {

		List symbols = new ArrayList();
		String posSym = "MSPB";

		char symChar;
		for (int c = 0; c < posSym.length(); c++) {

			symChar = posSym.charAt(c);
			for (int i = 0; i < 4; i++) {
				for (int j = 0; j < 4; j++) {
					symbols.add(symChar + "_" + (i + 1) + "_" + (j + 1));
				}
			}
		}

		return symbols;
	}
	
	/*
	 * this method reads logical expressions if the next string is a: - '(' => then
	 * the next 'symbol' is a subexpression - else => it must be a unique_symbol
	 * 
	 * it returns a logical expression
	 * 
	 * notes: i'm not sure that I need the counter
	 * 
	 */
	public static LogicalExpression readExpression(String input_string) {
		LogicalExpression result = new LogicalExpression();

		// testing
		// System.out.println("readExpression() beginning -"+ input_string +"-");
		// testing
		// System.out.println("\nread_exp");

		// trim the whitespace off
		input_string = input_string.trim();

		if (input_string.startsWith("(")) {
			// its a subexpression

			String symbolString = "";

			// remove the '(' from the input string
			symbolString = input_string.substring(1);
			// symbolString.trim();

			// testing
			// System.out.println("readExpression() without opening paren -"+ symbolString +
			// "-");

			if (!symbolString.endsWith(")")) {
				// missing the closing paren - invalid expression
				System.out.println("missing ')' !!! - invalid expression! - readExpression():-" + symbolString);
				exit_function(0);

			} else {
				// remove the last ')'
				// it should be at the end
				symbolString = symbolString.substring(0, (symbolString.length() - 1));
				symbolString.trim();

				// testing
				// System.out.println("readExpression() without closing paren -"+ symbolString +
				// "-");

				// read the connective into the result LogicalExpression object
				symbolString = result.setConnective(symbolString);

				// testing
				// System.out.println("added connective:-" + result.getConnective() + "-: here
				// is the string that is left -" + symbolString + "-:");
				// System.out.println("added connective:->" + result.getConnective() + "<-");
			}

			// read the subexpressions into a vector and call setSubExpressions( Vector );
			result.setSubexpressions(read_subexpressions(symbolString));

		} else {
			// the next symbol must be a unique symbol
			// if the unique symbol is not valid, the setUniqueSymbol will tell us.
			result.setUniqueSymbol(input_string);

			// testing
			// System.out.println(" added:-" + input_string + "-:as a unique symbol:
			// readExpression()" );
		}

		return result;
	}

	/*
	 * this method reads in all of the unique symbols of a subexpression the only
	 * place it is called is by read_expression(String, long)(( the only
	 * read_expression that actually does something ));
	 * 
	 * each string is EITHER: - a unique Symbol - a subexpression - Delineated by
	 * spaces, and paren pairs
	 * 
	 * it returns a vector of logicalExpressions
	 * 
	 * 
	 */

	public static Vector<LogicalExpression> read_subexpressions(String input_string) {

		Vector<LogicalExpression> symbolList = new Vector<LogicalExpression>();
		LogicalExpression newExpression;// = new LogicalExpression();
		String newSymbol = new String();

		// testing
		// System.out.println("reading subexpressions! beginning-" + input_string
		// +"-:");
		// System.out.println("\nread_sub");

		input_string.trim();

		while (input_string.length() > 0) {

			newExpression = new LogicalExpression();

			// testing
			// System.out.println("read subexpression() entered while with
			// input_string.length ->" + input_string.length() +"<-");

			if (input_string.startsWith("(")) {
				// its a subexpression.
				// have readExpression parse it into a LogicalExpression object

				// testing
				// System.out.println("read_subexpression() entered if with: ->" + input_string
				// + "<-");

				// find the matching ')'
				int parenCounter = 1;
				int matchingIndex = 1;
				while ((parenCounter > 0) && (matchingIndex < input_string.length())) {
					if (input_string.charAt(matchingIndex) == '(') {
						parenCounter++;
					} else if (input_string.charAt(matchingIndex) == ')') {
						parenCounter--;
					}
					matchingIndex++;
				}

				// read untill the matching ')' into a new string
				newSymbol = input_string.substring(0, matchingIndex);

				// testing
				// System.out.println( "-----read_subExpression() - calling readExpression with:
				// ->" + newSymbol + "<- matchingIndex is ->" + matchingIndex );

				// pass that string to readExpression,
				newExpression = readExpression(newSymbol);

				// add the LogicalExpression that it returns to the vector symbolList
				symbolList.add(newExpression);

				// trim the logicalExpression from the input_string for further processing
				input_string = input_string.substring(newSymbol.length(), input_string.length());

			} else {
				// its a unique symbol ( if its not, setUniqueSymbol() will tell us )

				// I only want the first symbol, so, create a LogicalExpression object and
				// add the object to the vector

				if (input_string.contains(" ")) {
					// remove the first string from the string
					newSymbol = input_string.substring(0, input_string.indexOf(" "));
					input_string = input_string.substring((newSymbol.length() + 1), input_string.length());

					// testing
					// System.out.println( "read_subExpression: i just read ->" + newSymbol + "<-
					// and i have left ->" + input_string +"<-" );
				} else {
					newSymbol = input_string;
					input_string = "";
				}

				// testing
				// System.out.println( "readSubExpressions() - trying to add -" + newSymbol + "-
				// as a unique symbol with ->" + input_string + "<- left" );

				newExpression.setUniqueSymbol(newSymbol);

				// testing
				// System.out.println("readSubexpression(): added:-" + newSymbol + "-:as a
				// unique symbol. adding it to the vector" );

				symbolList.add(newExpression);

				// testing
				// System.out.println("read_subexpression() - after adding: ->" + newSymbol +
				// "<- i have left ->"+ input_string + "<-");

			}

			// testing
			// System.out.println("read_subExpression() - left to parse ->" + input_string +
			// "<-beforeTrim end of while");

			input_string.trim();

			if (input_string.startsWith(" ")) {
				// remove the leading whitespace
				input_string = input_string.substring(1);
			}

			// testing
			// System.out.println("read_subExpression() - left to parse ->" + input_string +
			// "<-afterTrim with string length-" + input_string.length() + "<- end of
			// while");
		}
		return symbolList;
	}

	/*
	 * this method checks to see if a logical expression is valid or not a valid
	 * expression either: ( this is an XOR ) - is a unique_symbol - has: -- a
	 * connective -- a vector of logical expressions
	 * 
	 */
	public static boolean valid_expression(LogicalExpression expression) {

		// checks for an empty symbol
		// if symbol is not empty, check the symbol and
		// return the truthiness of the validity of that symbol

		if (!(expression.getUniqueSymbol() == null) && (expression.getConnective() == null)) {
			// we have a unique symbol, check to see if its valid
			return valid_symbol(expression.getUniqueSymbol());

			// testing
			// System.out.println("valid_expression method: symbol is not empty!\n");
		}

		// symbol is empty, so
		// check to make sure the connective is valid

		// check for 'if / iff'
		if ((expression.getConnective().equalsIgnoreCase("if"))
				|| (expression.getConnective().equalsIgnoreCase("iff"))) {

			// the connective is either 'if' or 'iff' - so check the number of connectives
			if (expression.getSubexpressions().size() != 2) {
				System.out.println("error: connective \"" + expression.getConnective() + "\" with "
						+ expression.getSubexpressions().size() + " arguments\n");
				return false;
			}
		}
		// end 'if / iff' check

		// check for 'not'
		else if (expression.getConnective().equalsIgnoreCase("not")) {
			// the connective is NOT - there can be only one symbol / subexpression
			if (expression.getSubexpressions().size() != 1) {
				System.out.println("error: connective \"" + expression.getConnective() + "\" with "
						+ expression.getSubexpressions().size() + " arguments\n");
				return false;
			}
		}
		// end check for 'not'

		// check for 'and / or / xor'
		else if ((!expression.getConnective().equalsIgnoreCase("and"))
				&& (!expression.getConnective().equalsIgnoreCase("or"))
				&& (!expression.getConnective().equalsIgnoreCase("xor"))) {
			System.out.println("error: unknown connective " + expression.getConnective() + "\n");
			return false;
		}
		// end check for 'and / or / not'
		// end connective check

		// checks for validity of the logical_expression 'symbols' that go with the
		// connective
		for (Enumeration e = expression.getSubexpressions().elements(); e.hasMoreElements();) {
			LogicalExpression testExpression = (LogicalExpression) e.nextElement();

			// for each subExpression in expression,
			// check to see if the subexpression is valid
			if (!valid_expression(testExpression)) {
				return false;
			}
		}

		// testing
		// System.out.println("The expression is valid");

		// if the method made it here, the expression must be valid
		return true;
	}

	/** this function checks to see if a unique symbol is valid */
	//////////////////// this function should be done and complete
	// originally returned a data type of long.
	// I think this needs to return true /false
	// public long valid_symbol( String symbol ) {
	public static boolean valid_symbol(String symbol) {
		if (symbol == null || (symbol.length() == 0)) {

			// testing
			// System.out.println("String: " + symbol + " is invalid! Symbol is either Null
			// or the length is zero!\n");

			return false;
		}

		for (int counter = 0; counter < symbol.length(); counter++) {
			if ((symbol.charAt(counter) != '_') && (!Character.isLetterOrDigit(symbol.charAt(counter)))) {

				System.out.println("String: " + symbol + " is invalid! Offending character:---" + symbol.charAt(counter)
						+ "---\n");

				return false;
			}
		}

		// the characters of the symbol string are either a letter or a digit or an
		// underscore,
		// return true
		return true;
	}

	private static void exit_function(int value) {
		System.out.println("exiting from checkTrueFalse");
		System.exit(value);
	}
}
