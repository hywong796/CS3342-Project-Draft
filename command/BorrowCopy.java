package command;

public class BorrowCopy extends RecordedCommand {
    
    @Override
    public void execute(String[] commandParts) {
        System.out.println("Borrow copy functionality not yet implemented");
    }

    @Override
    public void undoMe() {
        System.out.println("Undo functionality not yet implemented for BorrowCopy");
    }

    @Override
    public void redoMe() {
        System.out.println("Redo functionality not yet implemented for BorrowCopy");
    }
}
