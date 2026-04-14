package command;

public class FinishCopyProcessing extends RecordedCommand {
    
    @Override
    public void execute(String[] commandParts) {
        System.out.println("Finish copy processing functionality not yet implemented");
    }

    @Override
    public void undoMe() {
        System.out.println("Undo functionality not yet implemented for FinishCopyProcessing");
    }

    @Override
    public void redoMe() {
        System.out.println("Redo functionality not yet implemented for FinishCopyProcessing");
    }
}
