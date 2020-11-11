package correcter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Main {

    public static void main(String[] args) throws IOException {
        System.out.print("Write a mode: ");
        Scanner scanner = new Scanner(System.in);
        String mode = scanner.nextLine();
        if ("encode".equals(mode)) {
            encode();
        } else if ("send".equals(mode)) {
            send();
        } else if ("decode".equals(mode)) {
            decode();
        }
    }

    private static void encode() throws IOException {
        String fileContent = readFileToBinary("send.txt");

        List<Byte> encodedContent = new LinkedList<>();
        for (int i = 0; i < fileContent.length(); i+=4) {
            int max = Math.min(i + 4, fileContent.length());
            String bitsToEncode = fileContent.substring(i, max);
            byte[] encodedBits = new byte[8];
            switch (bitsToEncode.length()) {
                case 4: encodedBits[6] = Byte.parseByte(bitsToEncode.substring(3, 4));
                case 3: encodedBits[5] = Byte.parseByte(bitsToEncode.substring(2, 3));
                case 2: encodedBits[4] = Byte.parseByte(bitsToEncode.substring(1, 2));
                default: encodedBits[2] = Byte.parseByte(bitsToEncode.substring(0, 1));
            }
            encodedBits[0] = setParity(encodedBits[2], encodedBits[4], encodedBits[6]);
            encodedBits[1] = setParity(encodedBits[2], encodedBits[5], encodedBits[6]);
            encodedBits[3] = setParity(encodedBits[4], encodedBits[5], encodedBits[6]);
            StringBuilder byteAsStringEncoded = new StringBuilder();
            for (byte bit : encodedBits) {
                byteAsStringEncoded.append(bit);
            }
            System.out.println(byteAsStringEncoded);
            int encodedByte = Integer.valueOf(byteAsStringEncoded.toString(), 2);
            encodedContent.add((byte) encodedByte);
        }

        byte[] toOutput = new byte[encodedContent.size()];
        for (int i = 0; i < toOutput.length; i++) {
            toOutput[i] = encodedContent.get(i);
        }

        writeToFile(toOutput, "encoded.txt");
    }

    private static byte setParity(byte... bits) {
        int oneCount = 0;
        for (byte bit : bits) {
            if (bit == 1) {
                oneCount++;
            }
        }
        return (byte) (oneCount % 2);
    }

    private static void send() throws IOException {
        String encodedContent = readFileToBinary("encoded.txt");

        int randInt = 0;
        Random random = new Random();

        int i = 0;
        String[] corruptedByte = new String[8];

        int j = 0;
        byte[] sentContent = new byte[encodedContent.length() / 8];

        for (String bit : encodedContent.split("")) {
            if (i == 0) {
                randInt = random.nextInt(8);
            }
            if (i == randInt) {
                bit = "1".equals(bit) ? "0" : "1";
            }
            corruptedByte[i] = bit;
            i++;
            if (i > 7) {
                sentContent[j] = (byte) (int) Integer.valueOf(String.join("", corruptedByte), 2);
                i = 0;
                j++;
            }
        }

        writeToFile(sentContent, "received.txt");
    }

    private static void decode() throws IOException {
        String receivedContent = readFileToBinary("received.txt");

        StringBuilder decodedContent = new StringBuilder();
        for (int i = 0; i < receivedContent.length(); i+=8) {
            String[] byteToDecode = receivedContent.substring(i, i + 8).split("");
            decodedContent.append(decodeByte(byteToDecode));
        }

        byte[] toOutput = new byte[decodedContent.length() / 8];
        for (int i = 0; i < decodedContent.length(); i+=8) {
            int max = Math.min(i + 8, decodedContent.length());
            if (decodedContent.substring(i, max).length() == 8) {
                toOutput[i / 8] = (byte) (int) Integer.valueOf(decodedContent.substring(i, max), 2);
            }
        }

        writeToFile(toOutput, "decoded.txt");
    }

    private static String decodeByte(String[] byteToDecode) {
        boolean p0 = checkParity(byteToDecode[0], byteToDecode[2], byteToDecode[4], byteToDecode[6]);
        boolean p1 = checkParity(byteToDecode[1], byteToDecode[2], byteToDecode[5], byteToDecode[6]);
        boolean p3 = checkParity(byteToDecode[3], byteToDecode[4], byteToDecode[5], byteToDecode[6]);
        boolean p7 = "0".equals(byteToDecode[7]);
        if (p7) {
            if (!p0 && !p1 && p3) {
                byteToDecode[2] = invertByte(byteToDecode[2]);
            } else if (!p0 && p1 && !p3) {
                byteToDecode[4] = invertByte(byteToDecode[4]);
            } else if (p0 && !p1 && !p3) {
                byteToDecode[5] = invertByte(byteToDecode[5]);
            } else if (!p0 && !p1 && !p3) {
                byteToDecode[6] = invertByte(byteToDecode[6]);
            }
        }
        return byteToDecode[2] + byteToDecode[4] + byteToDecode[5] + byteToDecode[6];
    }

    private static String invertByte(String s) {
        return "0".equals(s) ? "1" : "0";
    }

    private static boolean checkParity(String parity, String... bits) {
        int oneCount = 0;
        for (String bit : bits) {
            if ("1".equals(bit)) {
                oneCount++;
            }
        }
        return String.valueOf(oneCount % 2).equals(parity);
    }

    public static String readFileToBinary(String fileName) throws IOException {
        Path inputStream = Paths.get(fileName);
        byte[] fileContent = Files.readAllBytes(inputStream);
        StringBuilder convertToBinary = new StringBuilder();
        for (byte symbol : fileContent) {
            int unsignedInt = Byte.toUnsignedInt(symbol);
            String byteAsString = Integer.toBinaryString(unsignedInt);
            convertToBinary.append(String.format("%8s", byteAsString).replaceAll(" ", "0"));
        }
        return convertToBinary.toString();
    }

    public static void writeToFile(byte[] arrayBytes, String fileName) throws IOException {
        OutputStream outputStream = new FileOutputStream(fileName, false);
        outputStream.write(arrayBytes);
        outputStream.close();
    }
}