//import com.sun.org.apache.xpath.internal.operations.String;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class PRP {

    // Map for different bits
    static final HashMap<Integer, List<String>> bitsMap = new HashMap<Integer, List<String>>(){{
        put(1, new ArrayList<String>(Arrays.asList("0","1")));
        put(2, new ArrayList<String>(Arrays.asList("00","01","10","11")));
        put(3, new ArrayList<String>(Arrays.asList("000","001","010","100","011","101","110","111")));
        put(4, new ArrayList<String>(Arrays.asList("0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111")));
    }};

    public static HashMap<String, HashMap<String, String>> result2Map = new HashMap<>();

    public static void main(String[] args) {

        if(args.length > 0) {

            // Function 1: Call generatePermutations method and store result in permutations.txt
            if(args[0].equals("permutegen")) {
                int n = Integer.parseInt(args[1]);
                String permutationsPath = args[2];

                // calling generatePermutations method to get all the permutations
                List<List<String>> permutations = generatePermutations(n);

                StringBuilder result1 = new StringBuilder();
                int num = 1;
                result1.append("d ");
                for(String p: permutations.get(0)) {
                    result1.append(p).append(" ");
                }
                result1.append("\n");
                for(List<String> permutation: permutations) {
                    result1.append("f").append(num).append("(d) ");
                    for(String p: permutation) {
                        result1.append(p).append(" ");
                    }
                    result1.append("\n");
                    num++;
                }

                System.out.println("permutegen: " +result1);
                // Writing the result to permutations.txt
                if(result1 != null) {
                    writeTextToFile(permutationsPath, result1.toString());
                }
            }

            // Function 2: Call generatePseudoPerm method and store result in pseudo_permutations.txt
            if(args[0].equals("prpgen")) {
                int n = Integer.parseInt(args[1]);
                int l = Integer.parseInt(args[2]);
                String pseudoPermPath = args[3];
                String result2 = generatePseudoPerm(n, l);
                System.out.println("prpgen: " +result2);
                // Writing the result to pseudo_permutations.txt
                if(result2 != null) {
                    writeTextToFile(pseudoPermPath, result2);
                }
            }

            // Function 3: Call enc_cbc method and store result in ciphertext.txt
            if(args[0].equals("enc_cbc")) {
                String m = args[1];
                int l = Integer.parseInt(args[2]);
                String k = args[3];
                String preudoPermPath = args[4];
                String ciphertextPath = args[5];

                enc_cbc(m, l, k, preudoPermPath, ciphertextPath);
            }

            // Function 4: Call dec_cbc method and print the message
            if(args[0].equals("dec_cbc")) {
                int l = Integer.parseInt(args[1]);
                String k = args[2];
                String preudoPermPath = args[3];
                String ciphertextPath = args[4];

                dec_cbc(l, k, preudoPermPath, ciphertextPath);

            }

            // Function 5: Call enc_ecb method and store result in ciphertext.txt
            if(args[0].equals("enc_ecb")) {
                String m = args[1];
                int l = Integer.parseInt(args[2]);
                String k = args[3];
                String preudoPermPath = args[4];
                String ciphertextPath = args[5];

                enc_ecb(m, l, k, preudoPermPath, ciphertextPath);

            }
        }
    }

    /*
    method: generatePermutations
    input: n(int)
    output: List<List<String>> - all the permutations
    desc.: take n and generates all the permutations by calling permute
     */
    public static List<List<String>> generatePermutations(int n) {
        if(n > 0 && n <= 3) {
            List<String> arr = bitsMap.get(n);
            return permute(arr);
        }
        return null;
    }

    /*
    method: permute
    input: List<String> arr - input bits
    output: List<List<String>> - all the permutations
    desc.: take arr and generates all the permutations by calling permuteHelper
     */
    public static List<List<String>> permute(List<String> arr) {
        List<List<String>> list = new ArrayList<>();
        permuteHelper(list, new ArrayList<>(), arr);
        return list;
    }

    /*
    method: permuteHelper
    input: List<List<String>> list, List<String> resultList, List<String> arr
    output: void
    desc.: generates permutations for permutation family using backtracking
     */
    public static void permuteHelper(List<List<String>> list, List<String> resultList, List<String> arr){

        // Base case
        if(resultList.size() == arr.size()){
            list.add(new ArrayList<>(resultList));
        }
        else{
            for(int i = 0; i < arr.size(); i++){

                if(resultList.contains(arr.get(i)))
                {
                    continue;
                }
                resultList.add(arr.get(i));
                permuteHelper(list, resultList, arr);
                resultList.remove(resultList.size() - 1);
            }
        }
    }

    /*
    method: generatePseudoPerm
    input: n(int), l(int)
    output: String - pseudo-random permutations
    desc.: generates permutations for pseudo-permutation
     */
    public static String generatePseudoPerm(int n, int l) {
        List<String> keys = bitsMap.get(l);
        List<String> dList = bitsMap.get(n);
        StringBuilder result2 = new StringBuilder();
        result2.append("d ").append(dList);
        result2.append("\n");
        //HashMap<String, HashMap<String, String>> result2Map = new HashMap<>();
        List<String> givenList = new ArrayList<String>(dList);
        for(String key: keys) {
            result2.append(key).append(" ");
            HashMap<String, String> map = new HashMap<>();
            for(String d: dList) {
                Random rand = new Random();
                int randomIndex = rand.nextInt(givenList.size());
                String randomElement = givenList.get(randomIndex);
                givenList.remove(randomIndex);
                result2Map.getOrDefault(key, new HashMap<String, String>()).put(d, randomElement);
                result2.append(randomElement).append(" ");
            }
            givenList = new ArrayList<String>(dList);
            result2.append("\n");
        }
        return result2.toString();
    }

    /*
    method: enc_cbc
    input: String m - message, int l, String k - key, String pseudoPermPath, String ciphertextPath
    output: void
    desc.: implements encryption with Block Cipher in CBC mode and stores the result in ciphertext.txt
     */
    public static void enc_cbc(String m, int l, String k, String pseudoPermPath, String ciphertextPath) {
        HashMap<String, HashMap<String, String>> map = new HashMap<>();

        List<String> row = readTextFromFile(pseudoPermPath);
        String line1 = row.get(0).substring(3, row.get(0).length()-1);
        String line1List[] = line1.split(", ");
        for(int i=1; i<row.size(); i++) {
            String line[] = row.get(i).split(" ");
            map.put(line[0], new HashMap<String, String>());
            for(int j=1; j<line.length; j++) {
                map.get(line[0]).put(line1List[j-1], line[j]);
            }
        }
        /*System.out.println("map \n"+map );
        for(String keys: map.keySet()) {
            System.out.println(keys);
        }*/

        //generatePseudoPerm(4,4);
        HashMap<String, String> permutationsMap = map.get(k);
        //System.out.println("map \n"+result2Map );
        //permutationsMap = result2Map.get(k);
        String iv = "";
        for(int i=0;i<l;i++){
            if(Math.random()>0.5) {
                iv += "1";
            } else {
                iv += "0";
            }
        }
        String initialIv = iv;
        StringBuilder cipherblock = new StringBuilder();
        StringBuilder ciphertext = new StringBuilder();
        List<String> blockList = new ArrayList<>();
        for (int i = 0; i < m.length(); i += l) {
            blockList.add(m.substring(i, i + l));

        }
        //System.out.println("blockList"+blockList);
        for (String block: blockList) {
            for (int j = 0; j < l; j++) {
                if (block.charAt(j) == iv.charAt(j)) {
                    cipherblock.append("0");
                } else {
                    cipherblock.append("1");
                }
            }
            //System.out.println("cipherblock"+cipherblock);
            //System.out.println("permutationsMap"+permutationsMap);

            iv = permutationsMap.get(cipherblock.toString());
            //System.out.println("iv"+iv);

            ciphertext.append(iv);
            cipherblock = new StringBuilder();
        }

        //System.out.println("iv: "+iv);
        System.out.println("enc_cbc: "+ciphertext);
        StringBuilder result3 = new StringBuilder();
        result3.append(initialIv).append(" ").append(ciphertext);
        writeTextToFile(ciphertextPath, result3.toString());

    }

    /*
    method: dec_cbc
    input: int l, String k - key, String pseudoPermPath - path, String ciphertextPath - path
    output: void
    desc.: implements decryption with Block Cipher in CBC mode
     */
    public static void dec_cbc(int l, String k, String pseudoPermPath, String ciphertextPath) {
        HashMap<String, HashMap<String, String>> map = new HashMap<>();

        List<String> row = readTextFromFile(pseudoPermPath);
        String line1 = row.get(0).substring(3, row.get(0).length()-1);
        String line1List[] = line1.split(", ");
        for(int i=1; i<row.size(); i++) {
            String line[] = row.get(i).split(" ");
            map.put(line[0], new HashMap<String, String>());
            for(int j=1; j<line.length; j++) {
                map.get(line[0]).put(line1List[j-1], line[j]);
            }
        }

        List<String> result3 = readTextFromFile(ciphertextPath);
        String[] ivCiphertext = result3.get(0).split(" ");
        String iv = ivCiphertext[0];
        String ciphertext = ivCiphertext[1];

        StringBuilder messageblock = new StringBuilder();
        StringBuilder message = new StringBuilder();
        List<String> blockList = new ArrayList<>();
        for (int i = 0; i < ciphertext.length(); i += l) {
            blockList.add(ciphertext.substring(i, i + l));
        }
        //System.out.println("blockList"+blockList);
        for (String block: blockList)  {
            String inverse = "";
            HashMap<String, String> map2 = map.get(k);
            for(String key: map2.keySet()) {
                if(map2.get(key).equals(block)) {
                    inverse = key;
                }
            }

            for (int j = 0; j < l; j++) {
                if (inverse.charAt(j) == iv.charAt(j)) {
                    messageblock.append("0");
                } else {
                    messageblock.append("1");
                }
            }
            message.append(messageblock);
            messageblock = new StringBuilder();
            iv = block;
        }
        System.out.println("dec_cbc: "+message);
    }

    /*
        method: enc_ecb
        input: String m - message, int l, String k - key, String pseudoPermPath - path, String ciphertextPath - path
        output: void
        desc.: implements encryption with Block Cipher in ECB mode and stores the result in ciphertext.txt
         */
    public static void enc_ecb(String m, int l, String k, String pseudoPermPath, String ciphertextPath) {
        HashMap<String, HashMap<String, String>> map = new HashMap<>();

        List<String> row = readTextFromFile(pseudoPermPath);
        String line1 = row.get(0).substring(3, row.get(0).length()-1);
        String line1List[] = line1.split(", ");
        for(int i=1; i<row.size(); i++) {
            String line[] = row.get(i).split(" ");
            map.put(line[0], new HashMap<String, String>());
            for(int j=1; j<line.length; j++) {
                map.get(line[0]).put(line1List[j-1], line[j]);
            }
        }
        /*System.out.println("map \n"+map );
        for(String keys: map.keySet()) {
            System.out.println(keys);
        }*/

        //generatePseudoPerm(4,4);
        HashMap<String, String> permutationsMap = map.get(k);
        //System.out.println("map \n"+result2Map );
        //permutationsMap = result2Map.get(k);
//        String iv = "";
//        for(int i=0;i<l;i++){
//            if(Math.random()>0.5) {
//                iv += "1";
//            } else {
//                iv += "0";
//            }
//        }
//        String initialIv = iv;
        StringBuilder cipherblock = new StringBuilder();
        StringBuilder ciphertext = new StringBuilder();
        List<String> blockList = new ArrayList<>();
        for (int i = 0; i < m.length(); i += l) {
            blockList.add(m.substring(i, i + l));

        }
        //System.out.println("blockList"+blockList);
        for (String block: blockList) {
            ciphertext.append(permutationsMap.get(block));
        }

        System.out.println("enc_ecb: "+ ciphertext);
        writeTextToFile(ciphertextPath, ciphertext.toString());

    }

    /*
    method: readTextFromFile
    input: path(String) - path of the file to read from
    output: plaintext(String) - text from file
    desc.: reads the text message from file
     */
    public static List<String> readTextFromFile(String path) {
        List<String> text = new ArrayList<>();
        try {
            File fis = new File(path);
            Scanner fileSc = new Scanner(fis);
            while (fileSc.hasNextLine()) {
                text.add(fileSc.nextLine());
            }
            fileSc.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return text;
    }

    /*
    method: writeTextToFile
    input: path(String) - path of the file to write to, text(String) - text to write in the file
    output: null
    desc.: writes the text message into file
     */
    public static void writeTextToFile(String path, String text) {
        File file = new File(path);
        FileWriter fw = null;
        try {
            fw = new FileWriter(file);
            fw.write(text);
            System.out.println("Wrote "+text+" to "+path);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
