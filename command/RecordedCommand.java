package command;

import java.util.ArrayList;

public abstract class RecordedCommand implements Command{
    //lists
    private static ArrayList<RecordedCommand> undoList = new ArrayList<>();
    private static ArrayList<RecordedCommand> redoList = new ArrayList<>();

    //child class implementation
    public abstract void undoMe();
    public abstract void redoMe();

    //add command to list
    protected static void addUndoCommand(RecordedCommand commandToRecord) {
        undoList.add(commandToRecord);
    }
    protected static void addRedoCommand(RecordedCommand commandToRecord) {
        redoList.add(commandToRecord);
    }

    //clear redo list
    protected static void clearRedoList() {
        redoList.clear();
    }

    //undo
    public static void undoOneCommand() {
        if (!undoList.isEmpty()) {
            RecordedCommand command = undoList.remove(undoList.size() - 1);
            command.undoMe();
            redoList.add(command);
            
        } else {
            System.out.println("Nothing to undo");
        }
    }

    //redo
    public static void redoOneCommand() {
        if (!redoList.isEmpty()) {
            RecordedCommand command = redoList.remove(redoList.size() - 1);
            command.redoMe();
            undoList.add(command);
        } else {
            System.out.println("Nothing to redo");
        }
    }
}
