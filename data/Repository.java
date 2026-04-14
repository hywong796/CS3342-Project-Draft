package data;

import java.io.*;
import java.util.ArrayList;

/* role:
read files
load data into files
*/
@SuppressWarnings("unused")
public class Repository {
    private static ArrayList<Library> LibraryData = new ArrayList<>();
    private static ArrayList<BookRecord> BookRecordData = new ArrayList<>();
    private static ArrayList<BookCopy> BookCopyData = new ArrayList<>();

    private static String LIB_FILE = "txt/library.txt";
    private static String RECORD_FILE = "txt/bookRecord.txt";
    private static String COPY_FILE = "txt/bookCopy.txt"; 

    //getters
    public static ArrayList<Library> getLibraryData() {
        return LibraryData;
    }
    public static ArrayList<BookRecord> getBookRecordData() {
        return BookRecordData;
    }
    public static ArrayList<BookCopy> getBookCopyData() {
        return BookCopyData;
    }

    //setters
    public static void setLibraryData(ArrayList<Library> data) {
        LibraryData = data;
    }
    public static void setBookRecordData(ArrayList<BookRecord> data) {
        BookRecordData = data;
    }
    public static void setBookCopyData(ArrayList<BookCopy> data) {
        BookCopyData = data;
    }

    public static void storeToFiles() {
        // 1. 存圖書館
        LibraryData = LibraryNetwork.getInstance();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LIB_FILE))) {
            
            for (Library lib:LibraryData){
                writer.write(lib.getLibraryID() + "|"+
                lib.getName() + "|"+
                lib.getAddress() + "|"+
                lib.getPhone() + "|"+
                lib.getEmail());
                writer.newLine();
            }
            System.out.println("✅ LIBRARY STORATION SUCCESS!!!");
        }catch (Exception e){
            System.out.println("❌ FUCK, HERE WE GO AGAIN (Library): "+e.getMessage());
        }

        // 2. 存書籍目錄
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RECORD_FILE))) {
            for (Library lib : LibraryData) {
                for (BookRecord record : getBookRecordData()) {
                    writer.write(
                        record.getOwner() + "|" +
                        record.getIsbn() + "|" +
                        record.getTitle() + "|" +
                        record.getAuthor() + "|" +
                        record.getLanguage() + "|" +
                        record.getCategory() + "|" +
                        record.getPublishingYear() + "|" +
                        record.getBorrowCount() + "|" +
                        record.getCopyCounter() + "|" +
                        record.getTotalCopies() + "|" +
                        record.getAvailableCopies()
                    );
                    writer.newLine();
                }
            }
            System.out.println("✅ BOOK STORATION SUCCESS！");
        } catch (IOException e) {
            System.out.println("❌ FAIL TO STORE THE BOOK: " + e.getMessage());
        }
    }

    public static void loadFromFiles() {
        // 1. 讀取圖書館資料 (呼叫底下的輔助方法即可，不需要重複寫程式碼)
        loadLibraryHelper(); 

        // 2. 讀取書籍目錄
        File recordFile = new File(RECORD_FILE); // 💡 修正 1：變數改名為 recordFile 避免重複
        if (!recordFile.exists()) return;

        BookRecordData.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(recordFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] p = line.split("\\|");
                if (p.length == 11) {
                    BookRecord record = new BookRecord(
                        p[0], p[1], p[2], p[3], p[4], p[5], 
                        java.time.Year.parse(p[6]), //
                        Integer.parseInt(p[7]),     // borrowCount
                        Integer.parseInt(p[8]),     // copyCounter
                        Integer.parseInt(p[9]),     // totalCopies
                        Integer.parseInt(p[10])     // availableCopies
                    );
                    BookRecordData.add(record);
                    
                    LibraryNetwork.getInstance().stream()
                        .filter(lib -> lib.getLibraryID().equals(p[0]))
                        .findFirst()
                        .ifPresent(lib -> lib.addRecord(record)); // 
                }
            }
            System.out.println("✅success to load！");
        } catch (Exception e) {
            System.out.println("❌Book fail to load: " + e.getMessage());
        }
    }

    private static void loadLibraryHelper() {
        File file = new File(LIB_FILE);
        if (!file.exists()) return;
        LibraryData.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 5) {
                    Library lib = new Library(parts[0], parts[1], parts[2], parts[3], parts[4]);
                    LibraryData.add(lib);
                }
            }
            LibraryNetwork.getInstance().clear();
            LibraryNetwork.getInstance().addAll(LibraryData);
        } catch (IOException e) { 
            System.out.println("❌Library load fail: " + e.getMessage());
        }
    }
}