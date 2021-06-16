import javax.swing.*;
import java.io.*;
import java.util.HashMap;

public class Main {
    static FileWriter fw;

    static HashMap<String, String> opCode = new HashMap<>() {{
        put("add",  "0");
        put("sub",  "1");
        put("and",  "2");
        put("or" ,  "3");
        put("nor",  "4");
        put("addi", "5");
        put("beq",  "6");
        put("jmp",  "7");
        put("lw", "8");
        put("sw", "9");
    }};

    static HashMap<String, String> registers  = new HashMap<>() {{
        put("$t0", "0");
        put("$t1", "1");
        put("$t2", "2");
        put("$t3", "3");
        put("$t4", "4");
        put("$t5", "5");
        put("$t6", "6");
        put("$t7", "7");
        put("$t8", "8");
        put("$t9", "9");
        put("$t10", "a");
        put("$t11", "b");
        put("$t12", "c");
        put("$t13", "d");
        put("$t14", "e");
        put("$t15", "f");
    }};

    static HashMap<String, String> labels = new HashMap<>();

    public static void main(String[] args) {
        fw = makeFile();
        read();
        try {
            fw.close();
        } catch (IOException e) {
            System.out.println("Failed to make the output file.");
        }
    }

    public static void read() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.showOpenDialog(null);
        while (true) {
            File file = fileChooser.getSelectedFile();
            if (!file.getName().endsWith(".asm")) {
                showDialog("Please choose the correct file.");
                continue;
            }
            readLabels(file);
            read(file);
            break;
        }

    }

    private static String getIm(String number, int bytes) {
        int n = Integer.parseInt(number);
        if (n >= (Math.pow(16, bytes)))
            throw new IllegalStateException("Immediate numbers should be smaller than " + (Math.pow(16, bytes)) + ".");

        StringBuilder ans = new StringBuilder(Integer.toHexString(n) + "");
        while (ans.length() < bytes) {
            ans.insert(0, "0");
        }

        return ans.toString();
    }

    private static String toHex(String[] params) {
        try {
            switch (params[0]) {
                case "add":
                    return opCode.get("add") + registers.get(params[1]) + registers.get(params[2])
                            + registers.get(params[3]) + "FFFF";
                case "sub":
                    return opCode.get("sub") + registers.get(params[1]) + registers.get(params[2])
                            + registers.get(params[3]) + "FFFF";
                case "addi":
                    return opCode.get("addi") + registers.get(params[1]) + registers.get(params[2]) +
                            getIm(params[2], 4) + "F";
                case "and":
                    return opCode.get("and") + registers.get(params[1]) + registers.get(params[2])
                            + registers.get(params[3]) + "FFFF";
                case "or":
                    return opCode.get("or") + registers.get(params[1]) + registers.get(params[2])
                            + registers.get(params[3]) + "FFFF";
                case "nor":
                    return opCode.get("nor") + registers.get(params[1]) + registers.get(params[2])
                            + registers.get(params[3]) + "FFFF";
                case "jmp":
                    if (labels.containsKey(params[1]))
                        return opCode.get("jmp") + labels.get(params[1]) + "FFF";
                    else
                        return opCode.get("jmp") + getIm(params[1], 4) + "FFF";
                case "beq":
                    if(labels.containsKey(params[3]))
                        return opCode.get("beq") + registers.get(params[1]) + registers.get(params[2]) +
                                labels.get(params[3]) + "F";
                    else
                        return opCode.get("beq") + registers.get(params[1]) + registers.get(params[2]) +
                                getIm(params[3], 4) + "F";
                case "sw":
                    return opCode.get("sw") + registers.get(params[1]) + registers.get(params[2])
                            + getIm(params[3], 4) + "F";
                case "lw":
                    return opCode.get("lw") + registers.get(params[1]) + registers.get(params[2])
                            + getIm(params[3], 4) + "F";
            }
        } catch (Exception e) {
            System.out.println("Code is not valid.");
        }
        return null;
    }

    private static FileWriter makeFile() {
        try {
            File file = new File("output.hex");
            if (file.exists())
                file.delete();
            file.createNewFile();
            return new FileWriter(file);
        } catch (IOException e) {
            System.out.println("Failed to make the output file.");
        }
        return null;
    }

    private static void write(String hex) {
        try {
            fw.write(hex + "\n");
        } catch (IOException e) {
            System.out.println("Failed to make the output file");
        }
    }

    private static void read(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String row = "";
            while (true) {
                row = reader.readLine();
                if (row == null)
                    break;
                row = row.trim().toLowerCase();
                if(row.contains("#"))
                    row = row.substring(0, Math.max(row.indexOf("#") - 1, 0));
                if(row.length() == 0)
                    continue;

                System.out.println(row);
                if (!row.endsWith(":")) {
                    String[] params = row.replaceAll(",", "").split("[ \t]+");
                    write(toHex(params));
                }
            }
        } catch (IOException e) {
            showDialog("File is damaged.");
        }
    }

    private static void readLabels(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String row = "";
            int lineCount = 1;
            while (true) {
                row = reader.readLine();
                if (row == null)
                    break;
                row = row.trim().toLowerCase();
                if(row.contains("#"))
                    row = row.substring(0, Math.max(row.indexOf("#") - 1, 0));

                if (row.endsWith(":")) {
                    labels.put(row.substring(0, row.length() - 1), getIm(lineCount + "", 4));
                }
                else {
                    if (!row.equals(""))
                        lineCount++;
                }
            }
        } catch (IOException e) {
            showDialog("File is damaged.");
        }
    }

    private static void showDialog(String text) {
        JOptionPane.showMessageDialog(null, text);
    }
}