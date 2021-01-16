import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class Main {

    public  String run(float percent) throws IOException {
        String result=" ";
        long dataSize = getStopLine(percent);
        ArrayList<Account> accounts = createAccounts(dataSize);
        //printAccounts(accounts);
        bayesianClassification(accounts,dataSize);
        return result;
    }

    private void bayesianClassification(ArrayList<Account> accounts, long dataSize) {
        long trainingSetSize = getTrainingSetSize(dataSize); //works on 75% only!!
        int yesOccurrence = getClassLabelYesOcc(accounts, trainingSetSize); //check not =0 to complete calcs
        float yesProb = (float)yesOccurrence / trainingSetSize;

        float noProb = 1 - yesProb;
        long noOccurrence = trainingSetSize - yesOccurrence;
        /*System.out.println("tr = "+trainingSetSize);
        System.out.println("yes prob = "+yesProb);
        System.out.println("no prob = "+noProb);
        System.out.println("yes occ = "+yesOccurrence);
        System.out.println("no occ = "+noOccurrence);
        */
        Map<String, Integer> attrFreqGivenNo = getTrainingData(accounts,trainingSetSize,"no");
        Map<String, Integer> attrFreqGivenYes = getTrainingData(accounts,trainingSetSize,"yes");
        //System.out.println(Arrays.toString(attrFreqGivenNo.entrySet().toArray()));
        //System.out.println(Arrays.toString(attrFreqGivenYes.entrySet().toArray()));


        System.out.printf("%-18s%-18s%-18s%-18s%-18s%-18s", "AGE","JOB","MARITAL","EDUCATION","HOUSING","BANK DEPOSIT ACCEPTANCE");
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------------------------------");

        //getTestingData(accounts,dataSize,trainingSetSize);
        ArrayList<Account> classifiedAccounts= new ArrayList<>();
        for(int i = (int) trainingSetSize; i < trainingSetSize+10; i++){   //change 2 to 10
            Account classifiedAccount = accounts.get(i);
            float yesProbTesting =calcProbTesting(classifiedAccount,yesProb, yesOccurrence,attrFreqGivenYes);
            float noProbTesting =calcProbTesting(classifiedAccount,noProb, noOccurrence,attrFreqGivenNo);
           // System.out.println("yesProbTesting = "+yesProbTesting);
            //System.out.println("noProbTesting = "+noProbTesting);
                for(int j = 0; j < 5; j++) {
                    System.out.printf("%-18s",classifiedAccount.info.get(j) );
                }


            if(yesProbTesting > noProbTesting){
                System.out.printf("%-18s"," result is YES");
                classifiedAccount.acceptance="yes";
            }
            else if(noProbTesting>yesProbTesting){
                System.out.printf("%-18s"," result is NO");
                classifiedAccount.acceptance="no";
            }else{
                System.out.printf("%-18s"," result is Yes");
                classifiedAccount.acceptance="yes";
            }
            classifiedAccounts.add(classifiedAccount);
            System.out.println();
        }
        float rightClassification=0;
        int found=0;
        for(int i = (int) trainingSetSize; i < trainingSetSize+10; i++) {
            int j=0;
            if(classifiedAccounts.get(j).info.equals(accounts.get(i).info) ){
                found++;
                if(classifiedAccounts.get(j).acceptance.equals(accounts.get(i).acceptance)){
                    rightClassification++;
                }
            }
            j++;
        }
        //System.out.println("right c is "+rightClassification);
        //System.out.println("found c is "+found);
        float accuracy= (float) (rightClassification/found);
        System.out.println("-------------------------------------------------------------------------------------------------------------");
        System.out.println("accuracy is "+accuracy);
        }
    private float calcProbTesting(Account a, float classLabelProb, float classLabelOccurrence, Map<String, Integer> attrFreqGivenClassLabel){
        if(classLabelOccurrence == 0)
            return 0;

        float classLabelProbTesting = classLabelProb;
        for( int i = 0; i < 5; i++){
                if(attrFreqGivenClassLabel.containsKey(a.info.get(i))) {
                    int attrOccurrence = attrFreqGivenClassLabel.get(a.info.get(i));
                    classLabelProbTesting *= (attrOccurrence / classLabelOccurrence);
                }
                else
                {
                    classLabelOccurrence ++;
                    classLabelProbTesting *= (1/classLabelOccurrence);

                }
        }

        return  classLabelProbTesting;
    }
    private ArrayList<Account> getTestingData(ArrayList<Account> accounts,long dataSize, long trainingSize){
        /*for(int i = (int) trainingSize; i < trainingSize+2; i++){   //change 2 to 10
            Account a = accounts.get(i);
        }*/
        return accounts;
    }
    private Map<String, Integer> getTrainingData(ArrayList<Account> accounts, long trainingSetSize, String classLabel) {
        List<String> ageValues = new ArrayList<>();
        List<String> jobValues = new ArrayList<>();
        List<String> maritalValues = new ArrayList<>();
        List<String> educationValues = new ArrayList<>();
        List<String> housingValues = new ArrayList<>();

            for(int j = 0; j < trainingSetSize; j++) {  //fix to training size
                if(accounts.get(j).acceptance.equals(classLabel)) {
                    ageValues.add(accounts.get(j).info.get(0));
                    jobValues.add(accounts.get(j).info.get(1));
                    maritalValues.add(accounts.get(j).info.get(2));
                    educationValues.add(accounts.get(j).info.get(3));
                    housingValues.add(accounts.get(j).info.get(4));
                }
            }
        HashSet<String> ageUniqueValues = new HashSet<>(ageValues);
        HashSet<String> jobUniqueValues = new HashSet<>(jobValues);
        HashSet<String> maritalUniqueValues = new HashSet<>(maritalValues);
        HashSet<String> educationUniqueValues = new HashSet<>(educationValues);
        HashSet<String> housingUniqueValues = new HashSet<>(housingValues);

        Map<String, Integer> attrFreq = new HashMap<>();

        for (String age : ageUniqueValues) {
            attrFreq.put(age,Collections.frequency(ageValues,age));
        }
        for (String job : jobUniqueValues) {
            attrFreq.put(job,Collections.frequency(jobValues,job));//ex: technician,30 times
        }
        for (String marital : maritalUniqueValues) {
            attrFreq.put(marital,Collections.frequency(maritalValues,marital));
        }
        for (String education : educationUniqueValues) {
            attrFreq.put(education,Collections.frequency(educationValues,education));
        }
        for (String housing : housingUniqueValues) {
            attrFreq.put(housing,Collections.frequency(housingValues,housing));
        }
        //System.out.println(Arrays.toString(attrFreq.entrySet().toArray()));

               /* List<List<String>> featuresLists = new ArrayList<>();
                List<String> featureList = new ArrayList<>();
                for(int i =0 ; i < trainingSetSize; i++){
                    featureList.add(accounts.get(i).info.get(0));
                }*/
        /*for (HashSet h : infoHashSets) {
            System.out.println(h);
        }for (String h : ageUniqueValues) {System.out.println(h);}*/

        /*for(int i=0; i < trainingSetSize; i++){
            for(int j=0; j < infoHashSets.size(); j++){
                for(int j=0; j < infoHashSets.size(); j++) {

                }
            }
        }*/
       /*
        List<String> arr= Arrays.asList(accounts.);
        arr.add("30");
        arr.add("40");
        System.out.println("freq is "+Collections.frequency(arr,"0"));
*/
       // }

        return attrFreq;
    }

    private int getClassLabelYesOcc(ArrayList<Account> accounts, long trainingSetSize) {
        int yesOccurence=0;
        for(int i = 0; i < trainingSetSize; i++){
            if(accounts.get(i).acceptance.equals("yes"))
                yesOccurence++;
        }
        //System.out.println("yesOcc is "+yesOccurance);
        //yesProb = yesOccurance / trainingSetSize;
        //System.out.println("yesProb is "+yesProb);
        return yesOccurence;
    }

    private ArrayList<Account> createAccounts( long stopLine) throws IOException {
        ArrayList<Account> accounts = readInput(stopLine);
        //splitData(readInput(stopLine));
        return accounts;
    }

    private long getTrainingSetSize( long dataSize) { //split into training and testing Data
        long trainingsize = Math.round((75/100.0)*dataSize);
       // System.out.println("dataSize is "+dataSize);
        //System.out.println("trainingSize is "+trainingsize);
        //ArrayList<Account> trainingSet = new ArrayList<>();
        return trainingsize;
    }

    private long getStopLine( float percentage) {
        double totalLines;
        long stopLine;

        totalLines = 4522;
        stopLine = Math.round((percentage/100.0)*totalLines);
        //System.out.println("stop line is "+stopLine);

        return stopLine;
    }
    public ArrayList<Account> readInput(long stopLine) throws IOException {
        try {
            String file ="Bank_dataset.xls";
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheetAt(0);
            HSSFRow row;

            ArrayList<Account> accounts = new ArrayList<>();
            int rows;
            rows = (int) stopLine;

            float ageSum = 0;
            for(int r = 1; r < rows; r++) {
                row = sheet.getRow(r);
                if (row != null) {
                    String stringAge = row.getCell(0).toString();
                    ageSum += Float.valueOf(stringAge);

                }
            }
            float ageMean = ageSum / rows;
            for(int r = 1; r < rows; r++) {
                row = sheet.getRow(r);
                if(row != null) {
                    Account a = new Account();
                    String stringAge = row.getCell(0).toString();
                    float age= Float.valueOf(stringAge);

                    if(age >= ageMean)
                        a.info.add("senior");
                    else
                        a.info.add("youth");

                    a.info.add(row.getCell(1).toString());//job
                    a.info.add(row.getCell(2).toString());//marital
                    a.info.add(row.getCell(3).toString());//education
                    a.info.add(row.getCell(6).toString());//housing
                    a.acceptance = row.getCell(16).toString();
                    accounts.add(a);
                }
            }
           // printAccounts(accounts);
            return accounts;
        } catch(Exception ioe) {
            ioe.printStackTrace();
        }
        return null;
    }
    public void printAccounts(ArrayList<Account> accounts){
        System.out.printf("%-18s%-18s%-18s%-18s%-18s%-18s", "AGE","JOB","MARITAL","EDUCATION","HOUSING","ACCEPTANCE");
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------------------------");
        for(Account a : accounts ){
            for(int j = 0; j < 5; j++) {
                System.out.printf("%-18s",a.info.get(j) );
            }
            System.out.println(a.acceptance);

        }
    }

}
