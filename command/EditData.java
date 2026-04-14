package command;

public class EditData extends RecordedCommand {
    
    @Override
    public void execute(String[] commandParts) {
        System.out.println("Edit functionality not yet implemented");
    }

    @Override
    public void undoMe() {
        System.out.println("Undo functionality not yet implemented for EditData");
    }

    @Override
    public void redoMe() {
        System.out.println("Redo functionality not yet implemented for EditData");
    }
}
