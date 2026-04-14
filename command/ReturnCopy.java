package command;

public class ReturnCopy extends RecordedCommand {
    
    @Override
    public void execute(String[] commandParts) {
        System.out.println("Return copy functionality not yet implemented");
    }

    @Override
    public void undoMe() {
        System.out.println("Undo functionality not yet implemented for ReturnCopy");
    }

    @Override
    public void redoMe() {
        System.out.println("Redo functionality not yet implemented for ReturnCopy");
    }
}
