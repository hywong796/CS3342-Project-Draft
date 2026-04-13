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
    static void undoOneCommand() {
        if (!undoList.isEmpty()) {
            undoList.remove(undoList.size()-1).undoMe();
        } else {
            System.out.println("Nothing to undo");
        }
    }

    static void redoOneCommand() {
        if (!redoList.isEmpty()) {
            redoList.remove(redoList.size()-1).redoMe();
        } else {
            System.out.println("Nothing to redo");
        }
    }
}
