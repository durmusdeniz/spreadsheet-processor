import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class Spreadsheet {

	public static void main(String[] args) {

		Spreadsheet.CreateSpreadSheet.get();
	}

	public class InvalidSpreadsheetIndexException extends Exception{
		public InvalidSpreadsheetIndexException(String invalidIndexMsg){
			super(invalidIndexMsg);
		}
	}

	private class Cell{
		private Double value;
		private boolean isProcessed = false;
		private String content;

		public Cell(String content){
			this.content = content;
		}

		public Cell setContent(String content){
			this.content = content;
			return this;
		}

		public Cell setProcessed(boolean isProcessed){
			this.isProcessed  = isProcessed;
			return this;
		}


		public Cell setValue(Double value){
			this.value = value;
			return this;
		}

		public Double getValue() {
			return value;
		}


		public boolean isProcessed() {
			return isProcessed;
		}

		public String getContent() {
			return content;
		}
	}

	private Cell[][] cells;
	private int x;
	private int y;

	public Spreadsheet(){
		populateCells();
		printSheetData();
	}

	private void processSheet(){
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				processCell(cells[i][j],null);
			}
		}
	}
	private Double processCell(Cell sheetCell,Set<Cell> processingStack) {
		if(processingStack == null)
		{
			processingStack = new LinkedHashSet<>();
		}

		if(sheetCell.isProcessed())
		{
			return sheetCell.getValue();
		}

		if(!sheetCell.isProcessed() && !processingStack.contains(sheetCell))
		{
			processingStack.add(sheetCell);


			String[] fields = sheetCell.getContent().split(" ");

			Stack<Double> operands = new Stack<>();

			for(String field : fields) {

				if(field.equals("+")){
					operands.push(operands.pop() + operands.pop());
				} else if (field.equals("*")){
					operands.push(operands.pop() * operands.pop());
				} else if (field.equals("/")){

					double divisor = operands.pop();
					double dividend = operands.pop();

					operands.push( dividend / divisor);
				}
				else if (field.equals("-")){
					double subtractor = operands.pop();
					double subtractee = operands.pop();

					operands.push( subtractee - subtractor);

				}
				else if (isNumber.apply(field)){
					operands.push(Double.parseDouble(field));
				}else if (!isValidField.apply(field)){
					System.out.println(field + " is not a valid value.");
					System.exit(1);
				}
				else {
					try{
						operands.push(processCell(getCell(field),processingStack));
					}catch (InvalidSpreadsheetIndexException in){
						System.out.println(in.getMessage());
						System.exit(1);
					}

				}
			}

			sheetCell.setValue(operands.pop()).setProcessed(true);


		} else {
			StringJoiner sj = new StringJoiner("\n");
			sj.add("Cycle Detected on Cell with Value: "+sheetCell.content).add("Cycling Cells:  ");
			processingStack.stream().map(cell -> cell.content).forEach(sj::add);
			System.out.println(sj.toString());
			System.exit(1);
		}

		return sheetCell.getValue();
	}


	public Cell getCell(String s) throws InvalidSpreadsheetIndexException{
			int x = (int)s.charAt(0) % 65;
			if(isNumber.apply(s.substring(1))){
				int y = Integer.parseInt(s.substring(1))-1;
				return cells[x][y];
			}

			throw new InvalidSpreadsheetIndexException("Invalid Spreadsheet Index Detected: " + s +"\nApplication supports indexes with a single letter only.");
	}


	private void printSheetData(){
		System.out.println(y+" "+x);
		for (int i = 0; i < x; i++) {
			for (int j = 0; j < y; j++) {
				if(i==x-1 && j==y-1)
					System.out.printf("%.5f", cells[i][j].value);
				else
					System.out.printf("%.5f%n", cells[i][j].value);
			}
		}
	}


	private  void populateCells() {
		try
		{

			Scanner sc = new Scanner(System.in);

			String[] fields;
			int[] size = new int[2];
			if (sc.hasNextLine()) {
				fields = sc.nextLine().split(" ");

				if (fields.length != 2) {
					throw new IllegalArgumentException("Invalid Size");
				} else {
					for (int i = 0; i < fields.length; i++)
						size[i] = Integer.parseInt(fields[i]);
					this.cells = new Cell[size[1]][size[0]];
					this.y = size[0];
					this.x = size[1];
				}

			}

			int rowIndex = 0,colIndex = 0,cellCount=0;
			while (sc.hasNextLine()) {
				String line = sc.nextLine();
				if (line.isEmpty())
					break;
				this.cells[rowIndex][colIndex] = this.new Cell(line);
				cellCount++;
				colIndex++;
				if(colIndex==this.y)
				{
					colIndex = 0;
					rowIndex++;
				}
			}

			if (cellCount != size[0]*size[1])
				throw new IllegalArgumentException("No of cells doesn't match the given size");
		}
		catch(Exception e){
			System.out.println("Error occurred in while reading values");
			System.exit(1);
		}

		processSheet();
	}


	private Function<String, Boolean> isNumber = input -> input != null && Pattern
		.compile("-?\\d+(\\.\\d+)?")
		.matcher(input)
		.matches();

	private Function<String, Boolean> isValidField = field -> {
		String initial = field.replaceAll("[^A-Z].*", "");
		String numeric =field.split(initial)[1];
		return initial.length() == 1 && isNumber.apply(numeric);
	};

	public static Supplier<Spreadsheet> CreateSpreadSheet = Spreadsheet::new;

}

