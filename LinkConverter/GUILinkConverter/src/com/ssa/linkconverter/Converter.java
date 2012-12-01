package com.ssa.linkconverter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;

public class Converter {

    private static final Logger log = Logger.getLogger(Converter.class);
    private static final String randomValues[] = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    private static final int alfaLenght = 26;
    private static final int numericLenght = 36;
    private String inputDirName = "input";
    private String outputDirName = "output";
    private String cfgFilePath = "converter.ini";
    private boolean useNumeric = false;
    private int minLenght = 2;
    private int maxLenght = 8;
    private boolean isReadKeysFromFile = false;
    private String keysFilePath = "";
    private ArrayList<String> keysCollection = new ArrayList<String>();

    public static void main(String[] args) {
        try {
            /*Converter converter = new Converter();
             converter.run();*/
        } catch (RuntimeException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Converter(
            boolean isReadKeysFromFile,
            String keysFilePath,
            String inputDirName,
            String outputDirName,
            boolean useNumeric,
            int minLenght,
            int maxLenght) {
        super();
        this.inputDirName = inputDirName;
        this.outputDirName = outputDirName;
        this.useNumeric = useNumeric;
        this.minLenght = minLenght;
        this.maxLenght = maxLenght;
        this.isReadKeysFromFile = isReadKeysFromFile;
        this.keysFilePath = keysFilePath;

        if (isReadKeysFromFile) {
            readKeys();
        }

        System.out.println("minLenght: " + minLenght);
        System.out.println("maxLenght: " + maxLenght);
        System.out.println("useNumeric: " + useNumeric);
    }

    private void readKeys() {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(new File(keysFilePath));
            br = new BufferedReader(fr);

            String line = br.readLine();
            while (line != null) {
                keysCollection.add(line.trim());
                line = br.readLine();
            }
        } catch (FileNotFoundException e) {
            log.error("Reading PROPERTIES file: FileNotFoundException exception occured", e);
        } catch (IOException e) {
            log.error("Reading PROPERTIES file: IOException exception occured", e);
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Throwable e) {
                log.warn("Error while initializtion", e);
            }
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (Throwable e) {
                log.warn("Error while initializtion", e);
            }
        }
    }

    public void run() throws Exception {
        try{
        File outputDir = new File(outputDirName);
        for (File inputFile : findFiles()) {
            File outputFile = new File(outputDir, "out_" + inputFile.getName());
            processFile(inputFile, outputFile);
        }
        }finally{
        saveKeysToFile();
        }
    }

    private File[] findFiles() throws Exception {
        File inputDir = new File(inputDirName);
        if (inputDir.isDirectory()) {
            return inputDir.listFiles();
        } else {
            throw new Exception("Must exist directory with name: " + inputDirName);
        }
    }

    private void processFile(File input, File output) throws Exception {
        FileInputStream fis = null;
        InputStreamReader in = null;
        BufferedReader bri = null;

        FileOutputStream fos = null;
        OutputStreamWriter out = null;
        BufferedWriter bwo = null;
        try {
            //open file
            fis = new FileInputStream(input);
            in = new InputStreamReader(fis, "UTF-8");
            bri = new BufferedReader(in);

            //file to write result
            fos = new FileOutputStream(output);
            out = new OutputStreamWriter(fos);
            bwo = new BufferedWriter(out);

            String str = bri.readLine();
            while (str != null) {
                //write to file
                bwo.write(convertLink(str));
                str = bri.readLine();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            //close all streams
            if (bri != null) {
                try {
                    bri.close();
                } catch (IOException e) {
                }
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
            if (bwo != null) {
                try {
                    bwo.close();
                } catch (IOException e) {
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private String convertLink(String str) throws Exception {
        StringBuilder result = new StringBuilder("<a href=\"");
        result.append(str).append("\">").append(generateRandomString()).append("</a>\r\n");
        return result.toString();
    }

    private String generateRandomString() throws Exception {
        StringBuilder resultStr = new StringBuilder();
        if (!isReadKeysFromFile) {
            int lenght = useNumeric ? numericLenght : alfaLenght;
            Random rnd = new Random();
            int randomStrLenght = minLenght + rnd.nextInt(maxLenght - minLenght+1);
            for (int i = 0; i < randomStrLenght; i++) {
                resultStr.append(randomValues[rnd.nextInt(lenght)]);
            }
        } else {
            if (keysCollection.size() > 0) {
                return keysCollection.remove(0);
            } else {
                throw new Exception("Keys file is empty. Add new keys and continue");
            }
        }

        return resultStr.toString();
    }

    private void saveKeysToFile() {
        BufferedWriter bufferedWriter = null;
        try {
            //Construct the BufferedWriter object
            bufferedWriter = new BufferedWriter(new FileWriter(keysFilePath, false));
            for (String key : keysCollection) {
                bufferedWriter.write(key);
                bufferedWriter.newLine();
            }

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            //Close the BufferedWriter
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.flush();
                    bufferedWriter.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void loadProperties(String cfgFilePath) {
        synchronized (this) {
            InputStream is = null;
            try {
                is = new FileInputStream(new File(cfgFilePath));
                //properties.load(is);
            } catch (FileNotFoundException e) {
                System.out.println("Reading PROPERTIES file: FileNotFoundException exception occured: " + e.getMessage());
            } catch (IOException e) {
                System.out.println("Reading PROPERTIES file: IOException exception occured: " + e.getMessage());
            } finally {
                try {
                    is.close();
                } catch (Throwable e) {
                    System.out.println("Error while initializtion:" + e.getStackTrace());
                }
            }
        }
    }
}
