package kr.co.justk.kakaotalkdiary.Util;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Kyewon on 2016. 7. 2..
 *
 * 카톡대화창에서 SD카드로 내보내기 한 txt파일을 불러온다
 * 핸드폰내에 카톡 메세지 저장 위치 - > /KakaoTalk/Chats/에 폴더 별로 저장됨 (폴더별 안에 kakaoTalkChats.txt)
 */
public class LoadKatalkFile {
    private final String TAG = "LoadKatalkFile";
    String path = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/KakaoTalk";
    File chatFiles = new File(path);
    File talkFiles = new File(path+"/Chats");

    String folderName = null;
    String filePath = null;
    FileInputStream fis = null;
    String str = null;
    StringBuffer strBuffer = null;
    String txtStr = null;

    Pattern namePattern, datePattern, talkDatePattern;
    Matcher nameMatcher, dateMatcher, talkDateMatcher;

    String talkDate = null;
    int max = 0;
    int min = 99999999;
    String fDate = null;
    String lDate = null;

    /**
     * Chats 폴더 여부 체크
     */
    public boolean ischeckedChatFolder(){
        if(!chatFiles.exists()){
            Log.e(TAG,"no chat folder");
            return false;
        }else{
            Log.i(TAG,"exists chat folder");
            return true;
        }
    }

    /**
     * 카카오톡 텍스트 파일을 읽어들여 stringbuffer에 쓴다
     * 이후, regexParsingData 함수로 연결
     */
    public void getKatalkChatFile() {
        if (talkFiles.listFiles().length > 0) { //SD카드 내에 카톡대화 파일이 있다면
            for (File folderList : talkFiles.listFiles()) { //Chats 폴더 내 모든 폴더 리스트
                folderName = folderList.getName();
                //String tmpFolderName = folderList.getName();
                //String saveYear = tmpFolderName.substring(16, 20);
                //String saveMonth = tmpFolderName.substring(21, 23);
                //String saveDay = tmpFolderName.substring(24, 26);
                filePath = talkFiles + "/" + folderName;

                File txtFile = new File(filePath);
                for (File f : txtFile.listFiles()) {
                    if (f.getName().equals("KakaoTalkChats.txt")) {
                        Log.i(TAG, "exist kakaoTalkChats.txt");

                        try {
                            fis = new FileInputStream(filePath + "/"
                                    + "KakaoTalkChats.txt");
                            BufferedReader bufferReader = new BufferedReader(
                                    new InputStreamReader(fis));

                            //TODO : file을 다 읽고 데이터 처리 vs file을 데이터 처리하면서 읽기
                            while ((str = bufferReader.readLine()) != null) {
                                strBuffer.append(str);
                            }
                            Log.i(TAG,strBuffer.toString());
                            regexParsingData(strBuffer);

                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }catch (IOException e){
                            e.printStackTrace();
                        } //try-catch문
                    }
                }//textFile for문
            }//FolderList for문
        }else{
            Log.e(TAG, "empty FolderList");
        }
    }

    /**
     * 읽은 텍스트파일을 regex Pattern 사용하여 정리
     */
    public void regexParsingData(StringBuffer strBuffer){
        txtStr = strBuffer.toString();
        namePattern = Pattern.compile("님과 카카오톡 대화$");
        datePattern = Pattern.compile("^[0-9][0-9][0-9][0-9]년 ([0-9][0-9]|[0-9])월 ([0-9][0-9]|[0-9])일 (오전|오후) ([0-9][0-9]|[0-9]):[0-9][0-9]$");
        nameMatcher = namePattern.matcher(txtStr);
        dateMatcher = datePattern.matcher(txtStr);

        if (nameMatcher.find()) {
            Log.i(TAG, "in find()");
            int i = txtStr.indexOf("님과 카카오톡 대화") - 1;
            String name = txtStr.substring(0, i);
            Log.i(TAG, "name :   " + name);

            if (name.contains(",")) {
                //TODO : 이름에 ,포함했으면 단톡(현재는 단톡처리 x)
                Log.e(TAG, "The name is included (,)");
            } else {
                Log.i(TAG, "Insert userName db");
                // db에 저장
                // && 갠톡만 리스트에 보이기
            }
        }

        if(dateMatcher.find()) {
            int year = txtStr.indexOf("년");
            int month = txtStr.indexOf("월");
            int day = txtStr.indexOf("일");
            int pm = txtStr.indexOf("오후");
            String talkYear = txtStr.substring(0, year);
            String talkMonth = txtStr.substring(year + 2, month);
            String talkDay = txtStr.substring(month + 2, day);
            String checkTalkAmPm = null;

            talkDatePattern = Pattern.compile("^"
                    + talkYear + "년 " + talkMonth
                    + "월 " + talkDay + "일");
            talkDateMatcher = talkDatePattern.matcher(txtStr);

            if (talkDateMatcher.find()) {
                if (pm == -1) {
                    checkTalkAmPm = "오전";
                    pm = txtStr.indexOf("오전");
                } else {
                    checkTalkAmPm = "오후";
                }

                int colonDelimiter = txtStr.indexOf(":");
                String talkHour = txtStr.substring(
                        pm + 3, colonDelimiter);
                String talkMinute = txtStr.substring(
                        colonDelimiter + 1, colonDelimiter + 3);

                // 오후일 +12
                if (checkTalkAmPm.equals("오후")) {
                    int in = Integer
                            .parseInt(talkHour);
                    if (in == 12) {
                        in = 0;
                    }
                    talkHour = Integer.toString(Integer
                            .parseInt(talkHour) + 12);
                }

                Log.i(TAG, "substring   "
                        + talkYear + "-"
                        + talkMonth + "-" + talkDay
                        + "-" + checkTalkAmPm + "-"
                        + talkHour + ":"
                        + talkMinute);

                int beforecomma = txtStr.indexOf(",");
                int aftername = txtStr.indexOf(":", 20);// 2번째 :

                String talkName = txtStr.substring(
                        beforecomma + 2, aftername - 1);
                String talk = txtStr.substring(aftername + 2);

                // 숫자개수 맞춰주기
                if (talkMonth.length() == 1) {
                    talkMonth = "0" + talkMonth;
                }
                if (talkDay.length() == 1) {
                    talkDay = "0" + talkDay;
                }
                if (talkHour.length() == 1) {
                    talkHour = "0" + talkHour;
                }
                if (talkMinute.length() == 1) {
                    talkMinute = "0" + talkMinute;
                }

                talkDate = talkYear + talkMonth
                        + talkDay;
                int parseIntTalkDate = Integer.parseInt(talkDate);

                // 마지막대화날짜
                if (parseIntTalkDate > max) {
                    max = parseIntTalkDate;
                }
                // 처음대화날짜
                if (parseIntTalkDate < min) {
                    min = parseIntTalkDate;
                }

                Log.i(TAG, "max = " + max + "   min = " + min);
                fDate = Integer.toString(min);
                lDate = Integer.toString(max);

                Log.i(TAG, "talkName =>" + talkName
                        + "date =>" + talkDate
                        + "talkHour =>" + talkHour
                        + "talkMinute =>" + talkMinute
                        + "talk =>" + talk);

                talkDate = Integer.toString(parseIntTalkDate);
                String talkTime = talkHour + talkMinute;

                Log.i(TAG, "talktime  ===>" + talkTime);
                if (talk.contains("'")) {
                    talk = talk.replace("'", "''");
                }
                // db에 저장
                // talkCount, totalCount 증가

            }//talkDateMatcher.find()
        }//dateMatcher.find()

    }

}
